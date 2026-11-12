import {EventSourcePolyfill} from 'event-source-polyfill'
import {getToken} from '@/utils/auth'
import {pushAuth, pushPing, pushSubscribe, pushUnsubscribe} from '@/api/push'
import {FRAME_EVENTS, parseHeartbeatAdvice, parseHelloPayload, PUSH_DEFAULTS} from '@/push/push-message'

/**
 * SSE 连接状态
 */
export const CONNECTION_STATUS = {
  IDLE: 'idle',
  CONNECTING: 'connecting',
  CONNECTED: 'connected',
  RECONNECTING: 'reconnecting',
  // 自动重连次数耗尽：不再后台重试，醒目提示并等待用户手动重连
  RECONNECT_FAILED: 'reconnect_failed',
  CLOSED: 'closed'
}

/**
 * SSE 推送连接管理器（单例）
 * <p>
 * 协议层统一连接流程：认证签票据 → EventSource 建连（ticket 作 query 参数，因 EventSource
 * 无法自定义 Header）→ 收 hello 帧取 sessionId → 上行 SUBSCRIBE 订阅全部主题 →
 * 周期 PING 保活。一次性 ticket 被服务端原子消费，故断线后必须由 error 回调主动换票重连，
 * 不使用 EventSource 原生自动重连（会拿已失效 ticket 反复失败）。
 * <p>
 * 登录态处理：每次建连（含重连）都实时取 getToken()，不依赖 connect 时的快照，
 * 保证 token 无感刷新后重连仍用新令牌；pushAuth 因登录态失效被拒（拦截器标记 authExpired）
 * 或本地已无 token 时停止重连，置 CLOSED 等待重新登录，避免过期凭证下的无效重连风暴；
 * 心跳上行连续失败达 heartbeatMaxFailures 阈值即判定会话已死，主动关流转换票重连自愈。
 * <p>
 * 主题与心跳/退避参数均可在 connect 时通过 options/config 覆盖（缺省取 PUSH_DEFAULTS），
 * 并提供 subscribeTopics/unsubscribeTopics 支持连接就绪后的动态增删主题。
 * </p>
 */
class SsePushClient {
  constructor() {
    this.eventSource = null
    this.ticket = null
    this.sessionId = null
    this.userId = null
    this.token = null
    this.status = CONNECTION_STATUS.IDLE
    this.heartbeatTimer = null
    this.reconnectTimer = null
    this.reconnectAttempts = 0
    this.onStatus = null
    this.onSignal = null
    // 主动关闭标记：登出/卸载时置位，阻止重连
    this.manualClose = false
    /**
     * 心跳上行连续失败计数（成功后清零，达阈值判定会话已死）
     */
    this.heartbeatFailures = 0
    /**
     * 服务端 hello 下发的最迟心跳期限（毫秒，0 表示未下发不启用陈旧守卫）
     */
    this.heartbeatDeadline = 0
    /**
     * 上次上行心跳被受理的时间戳（毫秒，只代表 HTTP 请求成功）
     */
    this.lastHeartbeatAt = 0
    /**
     * 最近一次收到服务端下行帧的时间戳（毫秒）
     * <p>
     * hello / pong / signal / 协议 error 帧都是链路双向存活的直接证据；上行 HTTP 失败时
     * 只要下行仍可达，就不能判定会话已死而关流重连。
     * </p>
     */
    this.lastDownlinkAt = 0
    /**
     * 当前订阅主题集合
     */
    this.topics = []
    /**
     * 运行时配置（PUSH_DEFAULTS 与调用方覆盖项的合并结果）
     */
    this.config = { ...PUSH_DEFAULTS }
  }

  /**
   * 建立推送连接（幂等：已连接或正在建连则忽略）
   *
   * @param {Object} opts 连接参数
   * @param {string} opts.token 当前登录令牌
   * @param {string|number} opts.userId 当前登录用户数字 id（用于缺省拼接 SIGNAL 频道 tbs.push.channel.{userId}）
   * @param {string|Array<string>} [opts.topics] 订阅主题（缺省为 topicPrefix + userId）
   * @param {Object} [opts.config] 配置覆盖项（心跳周期/心跳失败阈值/退避参数/主题前缀/下行无数据看门狗，见 PUSH_DEFAULTS）
   * @param {Function} [opts.onStatus] 连接状态变更回调 (status) => void
   * @param {Function} [opts.onSignal] 收到 SIGNAL 信令或建连成功（hello 兜底）回调 () => void，用于触发拉取
   */
  connect({ token, userId, topics, onStatus, onSignal, config }) {
    if (onStatus) this.onStatus = onStatus
    if (onSignal) this.onSignal = onSignal
    if (config) {
      this.config = { ...this.config, ...config }
    }
    this.token = token
    this.userId = userId
    this.topics = this._resolveTopics(topics)
    this.manualClose = false
    if (this.eventSource || this.status === CONNECTION_STATUS.CONNECTING) {
      return
    }
    this._open()
  }

  /**
   * 动态追加订阅主题
   * <p>
   * 连接已就绪时立即上行订阅帧；未就绪时仅登记，待 hello 握手后随全量主题统一订阅。
   * 重复主题幂等忽略。
   * </p>
   *
   * @param {string|Array<string>} topics 追加的主题
   */
  subscribeTopics(topics) {
    const resolved = this._resolveTopics(topics).filter(topic => !this.topics.includes(topic))
    if (!resolved.length) {
      return
    }
    this.topics = this.topics.concat(resolved)
    if (this.status === CONNECTION_STATUS.CONNECTED && this.sessionId) {
      resolved.forEach(topic => this._uploadSubscribe(topic))
    }
  }

  /**
   * 动态移除订阅主题（连接就绪时同步上行退订帧）
   *
   * @param {string|Array<string>} topics 移除的主题
   */
  unsubscribeTopics(topics) {
    const removed = this._resolveTopics(topics)
    if (!removed.length) {
      return
    }
    this.topics = this.topics.filter(topic => !removed.includes(topic))
    if (this.status === CONNECTION_STATUS.CONNECTED && this.sessionId) {
      removed.forEach(topic => this._uploadUnsubscribe(topic))
    }
  }

  async _open() {
    if (this.manualClose) {
      return
    }
    this._clearReconnect()
    this._setStatus(this.reconnectAttempts > 0 ? CONNECTION_STATUS.RECONNECTING : CONNECTION_STATUS.CONNECTING)
    // 每次建连都实时取登录态令牌：connect 时的 this.token 仅作兜底，
    // token 无感刷新后的重连若还用旧快照，SSE 建连必被网关拒绝且永不自愈
    const token = getToken() || this.token
    this.token = token
    let credential
    try {
      credential = await pushAuth(this.token, this.userId)
    } catch (e) {
      // 登录态失效（拦截器刷新失败后标记 authExpired）：停止换票重连，等待重新登录
      if (e && e.authExpired) {
        console.warn('[push] 登录态已失效，停止推送重连，等待重新登录')
        this._stopHeartbeat()
        this._setStatus(CONNECTION_STATUS.CLOSED)
        return
      }
      console.warn('[push] 认证签票据失败，准备重连', e)
      this._scheduleReconnect()
      return
    }
    this.ticket = credential.ticket
    this.sessionId = credential.sessionId

    const base = process.env.VUE_APP_BASE_API || ''
    const url = `${base}/private/push/sse/connect?push-ticket=${encodeURIComponent(this.ticket)}`
    // 原生 EventSource 不支持自定义请求头，而后端网关对 /private/** 要求 Authorization 头；
    // 采用 polyfill（XHR 流实现，API 与原生兼容）携带与 axios 拦截器同源的令牌头。
    // heartbeatTimeout 必须显式放宽：polyfill 默认 45 秒无数据就主动断流重连，
    // 而本链路在标签页节流/服务端推荐心跳周期拉长时，两次 PONG 间隔很容易超过 45 秒
    const es = new EventSourcePolyfill(url, {
      headers: { Authorization: this.token },
      withCredentials: true,
      heartbeatTimeout: this.config.sseHeartbeatTimeout
    })
    this.eventSource = es

    es.addEventListener(FRAME_EVENTS.HELLO, (ev) => {
      this._touchDownlink()
      const frame = this._safeParse(ev.data)
      const hello = frame && parseHelloPayload(frame.payload)
      if (!hello) {
        console.warn('[push] hello 帧解析失败，无法获取 sessionId')
        return
      }
      this.sessionId = hello.sessionId
      this._applyHeartbeatAdvice(hello)
      this._subscribeAll()
      this._startHeartbeat()
      this.reconnectAttempts = 0
      this._setStatus(CONNECTION_STATUS.CONNECTED)
      // hello 握手成功后主动拉第一页：兜底离线期间消息与丢失的信令
      if (typeof this.onSignal === 'function') {
        this.onSignal()
      }
    })

    es.addEventListener(FRAME_EVENTS.SIGNAL, () => {
      this._touchDownlink()
      // 信令本身无载荷，仅提示"该频道有新消息"，由调用方触发拉取
      if (typeof this.onSignal === 'function') {
        this.onSignal()
      }
    })

    es.addEventListener(FRAME_EVENTS.PONG, (ev) => {
      // PONG 本身就是「服务端还认这个会话」的最强证据：先无条件刷新下行活性，
      // 不能只在解析出推荐心跳值时才算收到；上行 HTTP 失败也要靠它来免误判
      this._touchDownlink()
      // 回带当前推荐心跳值，使连接存活期内可随服务端繁忙度自适应心跳节奏
      // （无推荐载荷的老服务端解析为 null，保持现有节奏不变）
      const frame = this._safeParse(ev.data)
      const advice = frame && parseHeartbeatAdvice(frame.payload)
      if (advice) {
        this._applyHeartbeatAdvice(advice)
      }
    })

    es.addEventListener(FRAME_EVENTS.SHUTDOWN, (ev) => {
      this._touchDownlink()
      // 服务端优雅停机通知：连接将在宽限期后被主动关闭。
      // 停心跳避免宽限期内上行心跳失败误判会话已死而抢先重连（此刻服务端仍在停机中），
      // 关流交由服务端完成后触发的 error 回调按退避换票重连（服务恢复后自动接回）。
      console.warn('[push] 收到服务端停机通知，连接将在宽限期后关闭，服务恢复后自动接回', ev.data)
      this._stopHeartbeat()
    })

    es.addEventListener(FRAME_EVENTS.ERROR, (ev) => {
      // 'error' 同时是 EventSource 的连接级错误事件名和服务端协议层 ERROR 帧的 SSE 事件名。
      // 服务端 reject()（订阅越权/上行守卫拒绝/topic 非法）只拒该次帧、明确不断流，这类消息事件带 data；
      // 连接级 error 不带 data。混为一谈会让一次普通的协议拒绝直接关流换票重连。
      if (ev && typeof ev.data === 'string' && ev.data) {
        this._touchDownlink()
        console.warn('[push] 协议层拒绝帧，连接保持', ev.data)
        return
      }
      // 一次性 ticket 已被消费，原生自动重连必然失败，必须换票重连
      console.warn('[push] SSE 连接异常，准备重连')
      this._closeStream()
      this._scheduleReconnect()
    })
  }

  /**
   * 归一化主题入参：字符串/数组统一为数组并过滤非法项；
   * 入参为空时回退为 per-user 缺省 SIGNAL 频道主题（topicPrefix + userId）
   *
   * @param {string|Array<string>} [topics] 主题入参
   * @returns {Array<string>} 有效主题数组，不为null
   */
  _resolveTopics(topics) {
    const list = Array.isArray(topics) ? topics : (topics ? [topics] : [])
    const valid = list.filter(topic => typeof topic === 'string' && topic.trim())
    if (valid.length) {
      return valid
    }
    return this.userId != null ? [this.config.topicPrefix + this.userId] : []
  }

  /**
   * 上行订阅全部已登记主题（hello 握手后调用）
   */
  _subscribeAll() {
    this.topics.forEach(topic => this._uploadSubscribe(topic))
  }

  _uploadSubscribe(topic) {
    pushSubscribe(this.sessionId, topic).catch(e => console.warn('[push] 订阅上行失败', topic, e))
  }

  _uploadUnsubscribe(topic) {
    pushUnsubscribe(this.sessionId, topic).catch(e => console.warn('[push] 退订上行失败', topic, e))
  }

  /**
   * 记录一次服务端下行帧到达：刷新链路活性并清零上行心跳失败计数
   * <p>
   * 上行 /private/push/sse/upload 的 200 只代表「帧被受理」，PONG 与确认帧是异步经 SSE
   * 下行下发的；判定链路存活应以下行帧为准，避免上行 HTTP 偶发失败被误判成会话已死。
   * </p>
   */
  _touchDownlink() {
    this.lastDownlinkAt = Date.now()
    this.heartbeatFailures = 0
  }

  /**
   * 采纳服务端下发的推荐心跳值（hello 握手与 pong 应答共用）：有推荐周期则覆盖本地心跳间隔，
   * 并记录最迟心跳期限供陈旧守卫使用。pong 运行期下发且周期发生变化时重建定时器以采纳新节奏
   *（hello 阶段定时器尚未启动，不会误触发重建）
   *
   * @param {Object} advice 含 heartbeatInterval/heartbeatDeadline 的推荐值（hello 帧或 pong 解析结果）
   */
  _applyHeartbeatAdvice(advice) {
    const nextInterval = advice.heartbeatInterval
    const intervalChanged = nextInterval && nextInterval !== this.config.heartbeatInterval
    if (nextInterval) {
      this.config.heartbeatInterval = nextInterval
    }
    this.heartbeatDeadline = advice.heartbeatDeadline || 0
    if (intervalChanged && this.heartbeatTimer) {
      console.info(`[push] 服务端 pong 下发新心跳周期 ${nextInterval}ms，重建心跳定时器`)
      this._startHeartbeat()
    }
  }

  _startHeartbeat() {
    this._stopHeartbeat()
    this.heartbeatFailures = 0
    this.lastHeartbeatAt = Date.now()
    // 不覆盖已有的下行时间戳：PONG 触发重建定时器时不能把「下行静默时长」抹掉
    if (!this.lastDownlinkAt) {
      this.lastDownlinkAt = Date.now()
    }
    this.heartbeatTimer = setInterval(() => {
      if (!this.sessionId) {
        return
      }
      // 最迟期限守卫改看下行帧：距最近一次收到任何服务端帧超过回收点才判定链路陈旧。
      // 上行定时器被后台标签页节流拉伸、但下行仍在收帧时不再误判
      if (this.heartbeatDeadline > 0 && Date.now() - this.lastDownlinkAt > this.heartbeatDeadline) {
        console.warn('[push] 长时间未收到任何下行帧，判定链路陈旧，关流换票重连')
        this._closeStream()
        this._scheduleReconnect()
        return
      }
      const pingStartedAt = Date.now()
      pushPing(this.sessionId).then(() => {
        this.heartbeatFailures = 0
        this.lastHeartbeatAt = Date.now()
      }).catch(e => {
        // 这次 ping 发出后若收到了 PONG 或其它下行帧，说明会话仍活着，失败只是这一路
        // 上行 HTTP 的问题（超时/刷新令牌/网关），不能据此关流重连
        if (this.lastDownlinkAt >= pingStartedAt) {
          console.warn('[push] 心跳上行失败，但下行帧仍可达，保持连接', e)
          return
        }
        console.warn('[push] 心跳上行失败且下行静默', e)
        this.heartbeatFailures++
        // 连续失败且下行也无任何帧，才判定会话已死，主动关流换票重连
        if (this.heartbeatFailures >= this.config.heartbeatMaxFailures) {
          console.warn('[push] 心跳连续失败且下行静默，判定会话已死，关流换票重连')
          this._closeStream()
          this._scheduleReconnect()
        }
      })
    }, this.config.heartbeatInterval)
  }

  _stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
  }

  _scheduleReconnect() {
    if (this.manualClose) {
      return
    }
    // 本地已无令牌（登出/会话被清）：无凭证可换，停止重连，避免过期态下无限退避循环
    if (!getToken()) {
      console.warn('[push] 本地登录态不存在，停止推送重连')
      this._stopHeartbeat()
      this._setStatus(CONNECTION_STATUS.CLOSED)
      return
    }
    // 自动重连次数耗尽：停止后台重试，置失败态醒目提示，由用户手动重连
    const max = this.config.reconnectMaxAttempts
    if (max > 0 && this.reconnectAttempts >= max) {
      console.warn(`[push] 自动重连已达最大次数(${max})，停止重连，等待手动重连`)
      this._stopHeartbeat()
      this._setStatus(CONNECTION_STATUS.RECONNECT_FAILED)
      return
    }
    this._stopHeartbeat()
    this.heartbeatFailures = 0
    this._setStatus(CONNECTION_STATUS.RECONNECTING)
    const { reconnectBaseDelay, reconnectMaxDelay, reconnectJitter } = this.config
    const delay = Math.min(reconnectMaxDelay, reconnectBaseDelay * Math.pow(2, this.reconnectAttempts)) +
      Math.random() * reconnectJitter
    this.reconnectAttempts++
    this.reconnectTimer = setTimeout(() => this._open(), delay)
  }

  _clearReconnect() {
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
  }

  _closeStream() {
    if (this.eventSource) {
      this.eventSource.close()
      this.eventSource = null
    }
  }

  _safeParse(text) {
    try {
      return JSON.parse(text)
    } catch (e) {
      return null
    }
  }

  _setStatus(status) {
    this.status = status
    if (typeof this.onStatus === 'function') {
      this.onStatus(status)
    }
  }

  /**
   * 手动重连（自动重连次数耗尽后由用户触发）：
   * 复位退避计数与主动关闭标记，关旧流后立即重新换票建连；
   * 成功后 hello 回调会把 reconnectAttempts 归零，重新拥有完整的自动重连配额
   */
  manualReconnect() {
    if (this.status === CONNECTION_STATUS.CONNECTED || this.status === CONNECTION_STATUS.CONNECTING) {
      return
    }
    this.manualClose = false
    this.reconnectAttempts = 0
    this._clearReconnect()
    this._closeStream()
    this._open()
  }

  /**
   * 主动关闭连接（登出/页面卸载调用），阻止重连
   */
  close() {
    this.manualClose = true
    this._clearReconnect()
    this._stopHeartbeat()
    this._closeStream()
    this.heartbeatFailures = 0
    // 复位退避计数：close 是会话终结（登出/卸载），下一次 connect 属全新会话，
    // 若残留 >0 会让 _open 把首下建连误标为 RECONNECTING（重连中）而非 CONNECTING（连接中）
    this.reconnectAttempts = 0
    this._setStatus(CONNECTION_STATUS.CLOSED)
  }
}

export const sseClient = new SsePushClient()
export default sseClient

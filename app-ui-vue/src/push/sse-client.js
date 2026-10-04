import { EventSourcePolyfill } from 'event-source-polyfill'
import { getToken } from '@/utils/auth'
import { pushAuth, pushSubscribe, pushUnsubscribe, pushPing } from '@/api/push'
import {
  PUSH_DEFAULTS,
  FRAME_EVENTS,
  parseHelloPayload
} from '@/push/push-message'

/**
 * SSE 连接状态
 */
export const CONNECTION_STATUS = {
  IDLE: 'idle',
  CONNECTING: 'connecting',
  CONNECTED: 'connected',
  RECONNECTING: 'reconnecting',
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
   * @param {Object} [opts.config] 配置覆盖项（心跳周期/心跳失败阈值/退避参数/主题前缀，见 PUSH_DEFAULTS）
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
    // 采用 polyfill（XHR 流实现，API 与原生兼容）携带与 axios 拦截器同源的令牌头
    const es = new EventSourcePolyfill(url, {
      headers: { Authorization: this.token },
      withCredentials: true
    })
    this.eventSource = es

    es.addEventListener(FRAME_EVENTS.HELLO, (ev) => {
      const frame = this._safeParse(ev.data)
      const hello = frame && parseHelloPayload(frame.payload)
      if (!hello) {
        console.warn('[push] hello 帧解析失败，无法获取 sessionId')
        return
      }
      this.sessionId = hello.sessionId
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
      // 信令本身无载荷，仅提示"该频道有新消息"，由调用方触发拉取
      if (typeof this.onSignal === 'function') {
        this.onSignal()
      }
    })

    es.addEventListener(FRAME_EVENTS.PONG, () => {
      // 服务端对 PING 的应答，确认链路存活；此处仅作为活性观测点
    })

    es.addEventListener(FRAME_EVENTS.ERROR, () => {
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

  _startHeartbeat() {
    this._stopHeartbeat()
    this.heartbeatFailures = 0
    this.heartbeatTimer = setInterval(() => {
      if (!this.sessionId) {
        return
      }
      pushPing(this.sessionId).then(() => {
        this.heartbeatFailures = 0
      }).catch(e => {
        console.warn('[push] 心跳上行失败', e)
        this.heartbeatFailures++
        // 连续失败达阈值：会话大概率已被服务端回收（如认证失效），下行流虽开着但上行全死，
        // 不能停留在 CONNECTED，主动关流转换票重连自愈
        if (this.heartbeatFailures >= this.config.heartbeatMaxFailures) {
          console.warn('[push] 心跳连续失败达阈值，判定会话已死，关流换票重连')
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
   * 主动关闭连接（登出/页面卸载调用），阻止重连
   */
  close() {
    this.manualClose = true
    this._clearReconnect()
    this._stopHeartbeat()
    this._closeStream()
    this.heartbeatFailures = 0
    this._setStatus(CONNECTION_STATUS.CLOSED)
  }
}

export const sseClient = new SsePushClient()
export default sseClient

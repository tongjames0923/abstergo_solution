/**
 * 推送协议帧常量与消息规范化工具
 * <p>
 * 新链路（信令+拉取）：SSE 仅承载 hello/signal/ping/pong 等协议帧，业务消息内容一律
 * 通过 /private/push/message/list 拉取并经 normalizeFetchedMessage 规范化；
 * 消息投递范围由后端 TopicPrefixNotifyReceiverResolver 按 topic 前缀解析。
 * 前端所有协议常量与解析校验集中在此，避免散落。
 * </p>
 */

/**
 * 消息类型（业务可字典驱动扩展，后端仅校验非空与长度）
 */
export const MESSAGE_TYPES = {
  NOTIFY: 'notify',
  WARNING: 'warning',
  REMINDER: 'reminder'
}

/**
 * 消息优先级
 */
export const PRIORITIES = {
  LOW: 'low',
  NORMAL: 'normal',
  HIGH: 'high',
  URGENT: 'urgent'
}

/**
 * SSE 事件名 = 帧类型小写值（与后台 FrameTypeEnum.getValue 对齐，无 push- 前缀）
 * 注：新链路下不再订阅 DATA 帧，业务消息一律由 SIGNAL 信令触发拉取获得
 */
export const FRAME_EVENTS = {
  HELLO: 'hello',
  SIGNAL: 'signal',
  ERROR: 'error',
  PING: 'ping',
  PONG: 'pong',
  SUBSCRIBE: 'subscribe',
  UNSUBSCRIBE: 'unsubscribe'
}

/**
 * SIGNAL 信令频道前缀：拼接当前登录用户数字 id 得到 per-user 频道
 * 须与后端 PushNotifyServiceImpl.CHANNEL_TOPIC_PREFIX 及设计文档 4.4/4.5 保持一致
 */
export const TOPIC_PREFIX = 'tbs.push.channel.'

/**
 * 业务 topic：全体广播（后端 TopicPrefixNotifyReceiverResolver 按用户表全量解析激活用户）
 */
export const TOPIC_BROADCAST = 'global.broadcast'

/**
 * 业务 topic 前缀：精确匹配单个用户（user.name.{userName}，后端按用户表精确查询）
 */
export const TOPIC_USER_NAME_PREFIX = 'user.name.'

/**
 * 业务 topic 前缀：按用户名前缀搜索匹配多个用户（user.search.{keyword}，后端 leftLike 查询）
 */
export const TOPIC_USER_SEARCH_PREFIX = 'user.search.'

/**
 * 心跳上行周期（毫秒）：须远小于后台空闲超时（tbs.push.heartbeat-timeout-millis，默认 600000ms 可配置），
 * 取小周期留足余量，避免网络抖动导致被判定为死连接
 */
export const HEARTBEAT_INTERVAL = 30_000

/**
 * 协议版本号（与后台 PushFrameConstants.PROTOCOL_VERSION 对齐）
 */
export const PROTOCOL_VERSION = 1

/**
 * 模块默认配置（连接层可通过 initPush 配置项逐项覆盖）
 */
export const PUSH_DEFAULTS = {
  /**
   * per-user 通知主题前缀
   */
  topicPrefix: TOPIC_PREFIX,
  /**
   * 心跳上行周期（毫秒）
   */
  heartbeatInterval: HEARTBEAT_INTERVAL,
  /**
   * 心跳连续失败阈值：达到即判定当前会话已死（服务端会话可能因认证失效/异常回收而
   * 上行全失败但下行流仍开着），主动关流转换票重连自愈
   */
  heartbeatMaxFailures: 3,
  /**
   * 重连退避基数（毫秒）
   */
  reconnectBaseDelay: 1_000,
  /**
   * 重连退避上限（毫秒）
   */
  reconnectMaxDelay: 90_000,
  /**
   * 重连退避随机抖动上限（毫秒），防惊群
   */
  reconnectJitter: 1_000
}

/**
 * 解析并校验 hello 帧 payload（字符串需二次 JSON.parse），提取会话标识
 *
 * @param {string} rawPayload hello 帧的 payload 字符串
 * @returns {Object|null} { sessionId, version } 或解析失败时 null
 */
export function parseHelloPayload(rawPayload) {
  if (!rawPayload) {
    return null
  }
  try {
    const obj = JSON.parse(rawPayload)
    if (!obj || !obj.sessionId) {
      return null
    }
    return { sessionId: obj.sessionId, version: obj.version }
  } catch (e) {
    console.warn('[push] hello 帧 payload 解析失败', e)
    return null
  }
}

/**
 * 解析并校验 SIGNAL 信令帧（payload 为空，仅表达某频道有新消息）
 * <p>
 * 信令只承载频道提示，不携带消息标识；解析仅作合法性校验与轻量事件提取。
 * </p>
 *
 * @param {string} raw SIGNAL 帧原始字符串
 * @returns {Object|null} { topic, messageId, timestamp } 或非法时 null
 */
export function parseSignalFrame(raw) {
  if (!raw) {
    return null
  }
  try {
    const obj = JSON.parse(raw)
    if (!obj || obj.type !== FRAME_EVENTS.SIGNAL) {
      return null
    }
    return {
      topic: obj.topic,
      messageId: obj.messageId,
      timestamp: typeof obj.timestamp === 'number' ? obj.timestamp : Date.now()
    }
  } catch (e) {
    console.warn('[push] SIGNAL 帧解析失败', e)
    return null
  }
}

/**
 * 将后端分页拉取返回的 PushNotifyMessageVO 规范化为前端消息对象
 * <p>
 * 字段映射：messageId→id、msgType→type、createTime→timestamp、readFlag→read；
 * 与拉取接口（/private/push/message/list）返回体一一对应，统一入列与渲染。
 * </p>
 *
 * @param {Object} vo 后端 PushNotifyMessageVO
 * @returns {Object|null} 规范化消息对象，或非法时 null
 */
export function normalizeFetchedMessage(vo) {
  if (!vo || vo.messageId == null) {
    return null
  }
  let timestamp = Date.now()
  if (typeof vo.createTime === 'number') {
    timestamp = vo.createTime
  } else if (typeof vo.createTime === 'string' && vo.createTime) {
    const parsed = new Date(vo.createTime.replace(/-/g, '/')).getTime()
    if (!Number.isNaN(parsed)) {
      timestamp = parsed
    }
  }
  return {
    id: String(vo.messageId),
    type: vo.msgType,
    title: vo.title || '',
    content: vo.content || '',
    timestamp,
    priority: Object.values(PRIORITIES).includes(vo.priority) ? vo.priority : PRIORITIES.NORMAL,
    sender: vo.sender || '',
    url: vo.url || '',
    read: Boolean(vo.readFlag)
  }
}

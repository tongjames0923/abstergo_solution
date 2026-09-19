/**
 * 推送消息结构体与协议帧常量
 * <p>
 * 约定后台 DATA 帧 payload 内承载的业务消息 JSON 结构（见 MESSAGE_EXAMPLE），
 * 以及 SSE 事件名（即帧类型小写值）。前端所有解析与校验集中在此，避免散落。
 * </p>
 */

/**
 * 消息类型（与后台 FrameTypeEnum 业务语义对齐）
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
 */
export const FRAME_EVENTS = {
  HELLO: 'hello',
  DATA: 'data',
  ERROR: 'error',
  PING: 'ping',
  PONG: 'pong',
  SUBSCRIBE: 'subscribe',
  UNSUBSCRIBE: 'unsubscribe'
}

/**
 * 订阅主题前缀：拼接当前登录用户名得到 per-user 通知主题
 */
export const TOPIC_PREFIX = 'global.system.notify.'

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
   * 重连退避基数（毫秒）
   */
  reconnectBaseDelay: 1_000,
  /**
   * 重连退避上限（毫秒）
   */
  reconnectMaxDelay: 30_000,
  /**
   * 重连退避随机抖动上限（毫秒），防惊群
   */
  reconnectJitter: 1_000
}

/**
 * 业务消息示例（DATA 帧 payload 形态），用于联调与文档对齐
 */
export const MESSAGE_EXAMPLE = {
  id: 'M1705123456789',
  type: MESSAGE_TYPES.NOTIFY,
  title: '系统通知',
  content: '您有一条新的审批待处理，请及时处理。',
  timestamp: 1705123456789,
  priority: PRIORITIES.NORMAL,
  sender: 'system',
  url: '/approve/123'
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
 * 解析并校验业务消息（DATA 帧 payload 字符串）
 * <p>
 * 校验规则：必须可解析为对象，且含必填字段 id 与 type（type 须在 MESSAGE_TYPES 内）。
 * 解析或校验失败返回 null，调用方容错忽略该条，不阻断 SSE 连接。
 * </p>
 *
 * @param {string} rawPayload DATA 帧的 payload 字符串
 * @returns {Object|null} 规范化消息对象，或非法时 null
 */
export function parsePushMessage(rawPayload) {
  if (!rawPayload) {
    return null
  }
  let obj
  try {
    obj = JSON.parse(rawPayload)
  } catch (e) {
    console.warn('[push] 业务消息 JSON 解析失败', e)
    return null
  }
  if (!obj || typeof obj !== 'object') {
    return null
  }
  if (!obj.id || !Object.values(MESSAGE_TYPES).includes(obj.type)) {
    console.warn('[push] 业务消息缺少必填字段 id/type', obj)
    return null
  }
  return {
    id: String(obj.id),
    type: obj.type,
    title: obj.title || '',
    content: obj.content || '',
    timestamp: typeof obj.timestamp === 'number' ? obj.timestamp : Date.now(),
    priority: Object.values(PRIORITIES).includes(obj.priority) ? obj.priority : PRIORITIES.NORMAL,
    sender: obj.sender || '',
    url: obj.url || ''
  }
}

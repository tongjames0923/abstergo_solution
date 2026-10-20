import sseClient, { CONNECTION_STATUS } from './sse-client'
import { registerRenderer, notifyMessage } from './renderer'
import { showPushNotification } from './notify'

/**
 * push 推送模块统一门面（业务复用唯一入口）
 * <p>
 * 编排连接层（sse-client）、渲染器注册表（renderer）与消息处理器注册表：
 * initPush 建连并订阅 SIGNAL 信令频道，信令/hello 兜底触发调用方拉取
 * （/private/push/message/list）；拉取到的消息由调用方经 notifyMessage（渲染器分发，
 * 默认弹窗）与 dispatchMessage（业务处理器分发）处理。
 * subscribeTopics/unsubscribeTopics 支持连接就绪后动态增删订阅主题，
 * registerMessageHandler 支持业务按消息类型（或 * 通配）注入自定义处理逻辑。
 * 上层集成点（store 模块、业务组件）只应依赖本文件导出的 API。
 * </p>
 */

// 注册模块默认渲染器（Element UI Notification，Set 幂等）
registerRenderer(showPushNotification)

/**
 * 消息类型 → 处理器集合（'*' 为通配类型，命中全部消息）
 */
const messageHandlers = new Map()

/**
 * 注册消息处理器
 * <p>
 * 处理器在消息完成去重后依次调用；同一 type 下的重复注册幂等忽略。
 * 处理器签名：handler(message)，抛出的异常被捕获隔离，不影响后续处理器。
 * </p>
 *
 * @param {string} type 消息类型（MESSAGE_TYPES 取值或 '*' 通配全部类型）
 * @param {Function} handler 处理器函数 (message) => void
 * @returns {Function} 取消注册函数，调用即注销该处理器
 */
export function registerMessageHandler(type, handler) {
  if (typeof type !== 'string' || !type || typeof handler !== 'function') {
    console.warn('[push] 消息处理器注册参数非法，注册被忽略')
    return () => {}
  }
  if (!messageHandlers.has(type)) {
    messageHandlers.set(type, new Set())
  }
  const handlers = messageHandlers.get(type)
  if (handlers.has(handler)) {
    return () => {}
  }
  handlers.add(handler)
  return () => unregisterMessageHandler(type, handler)
}

/**
 * 注销消息处理器
 *
 * @param {string} type 消息类型（或 '*' 通配）
 * @param {Function} handler 此前注册的处理器函数
 */
export function unregisterMessageHandler(type, handler) {
  const handlers = messageHandlers.get(type)
  if (handlers) {
    handlers.delete(handler)
    if (!handlers.size) {
      messageHandlers.delete(type)
    }
  }
}

/**
 * 向已注册的消息处理器分发一条消息
 * <p>
 * 命中顺序：精确 type 处理器在前，'*' 通配处理器在后；单个处理器异常被捕获隔离。
 * 新链路下由拉取侧（store 拉取新增消息后）调用。
 * </p>
 *
 * @param {Object} message 规范化消息对象
 */
export function dispatchMessage(message) {
  if (!message || !message.type) {
    return
  }
  const exact = messageHandlers.get(message.type)
  const wildcard = messageHandlers.get('*')
  if (!exact && !wildcard) {
    return
  }
  const handlers = []
  if (exact) {
    handlers.push(...exact)
  }
  if (wildcard) {
    handlers.push(...wildcard)
  }
  handlers.forEach(handler => {
    try {
      handler(message)
    } catch (e) {
      console.warn('[push] 消息处理器执行失败，已隔离', e)
    }
  })
}

/**
 * 初始化推送连接（登录后调用）
 * <p>
 * 建连并订阅主题；收到 SIGNAL 信令或 hello 握手成功时回调 onSignal（由调用方触发拉取）。
 * 幂等：重复调用由连接层忽略。
 * </p>
 *
 * @param {Object} options 初始化参数
 * @param {string} options.token 当前登录令牌
 * @param {string|number} options.userId 当前登录用户数字 id（缺省拼接 SIGNAL 频道 tbs.push.channel.{userId}）
 * @param {string|Array<string>} [options.topics] 订阅主题（缺省 topicPrefix + userId）
 * @param {Object} [options.config] 连接配置覆盖项（心跳/退避/主题前缀，见 PUSH_DEFAULTS）
 * @param {Function} [options.onStatus] 连接状态变更回调 (status) => void
 * @param {Function} [options.onSignal] 收到 SIGNAL 信令或建连兜底回调 () => void，用于触发拉取
 */
export function initPush({ token, userId, topics, config, onStatus, onSignal }) {
  sseClient.connect({ token, userId, topics, config, onStatus, onSignal })
}

/**
 * 动态追加订阅主题（连接未就绪时登记，握手后统一订阅）
 *
 * @param {string|Array<string>} topics 追加主题
 */
export function subscribeTopics(topics) {
  sseClient.subscribeTopics(topics)
}

/**
 * 动态移除订阅主题（连接就绪时同步上行退订）
 *
 * @param {string|Array<string>} topics 移除主题
 */
export function unsubscribeTopics(topics) {
  sseClient.unsubscribeTopics(topics)
}

/**
 * 手动重连推送连接（自动重连次数耗尽置 reconnect_failed 后由用户触发）
 * <p>
 * 复位退避计数后立即重新换票建连；幂等：已连接/连接中时无副作用。
 * </p>
 */
export function reconnectPush() {
  sseClient.manualReconnect()
}

/**
 * 关闭推送连接（登出/页面卸载），阻止自动重连
 */
export function closePush() {
  sseClient.close()
}

// 渲染器分发由拉取侧调用：通知弹窗入口（与 dispatchMessage 配对使用）
export { notifyMessage }

export { CONNECTION_STATUS }

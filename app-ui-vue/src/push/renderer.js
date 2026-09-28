/**
 * 渲染器注册表
 * <p>
 * 模块核心与 UI 库解耦的枢纽：推送消息的"如何呈现"由注册的渲染器决定，
 * 默认渲染器（Element UI Notification）由门面在初始化时注册；
 * 业务可注册额外渲染器（如站内信气泡、声音提醒），或注销默认渲染器整体替换 UI 策略。
 * 渲染器签名约定：{@code renderer(message, hooks)}，hooks.onRead(message) 供渲染器回传"用户已查看"。
 * 单个渲染器异常被捕获隔离，不影响其他渲染器与消息流水线。
 * </p>
 */

/**
 * 已注册渲染器集合（Set 保证幂等注册）
 */
const renderers = new Set()

/**
 * 注册消息渲染器（幂等：重复注册同一函数仅生效一次）
 *
 * @param {Function} renderer 渲染器函数 (message, hooks) => void
 * @returns {Function} 取消注册函数，调用即注销该渲染器
 */
export function registerRenderer(renderer) {
  if (typeof renderer !== 'function') {
    console.warn('[push] 渲染器必须为函数，注册被忽略')
    return () => {}
  }
  renderers.add(renderer)
  return () => unregisterRenderer(renderer)
}

/**
 * 注销消息渲染器
 *
 * @param {Function} renderer 此前注册的渲染器函数
 */
export function unregisterRenderer(renderer) {
  renderers.delete(renderer)
}

/**
 * 向全部已注册渲染器分发一条消息
 * <p>
 * 逐一调用渲染器并隔离异常：任一渲染器抛错仅告警跳过，不中断其余渲染器。
 * </p>
 *
 * @param {Object} message 规范化消息对象
 * @param {Object} [hooks] 交互钩子
 * @param {Function} [hooks.onRead] 用户查看或关闭消息时回调 (message) => void
 */
export function notifyMessage(message, hooks) {
  renderers.forEach(renderer => {
    try {
      renderer(message, hooks)
    } catch (e) {
      console.warn('[push] 消息渲染器执行失败，已隔离', e)
    }
  })
}

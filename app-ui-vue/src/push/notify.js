import { Notification } from 'element-ui'
import i18n from '@/i18n'
import { PRIORITIES } from '@/push/push-message'

/**
 * 通知弹窗管理器（模块默认渲染器实现）
 * <p>
 * 基于 Element UI Notification 实现右上角纵向堆叠；消息类型映射通知样式，
 * 高/紧急优先级常驻不自动关闭，普通/低优先级按时长自动消失；
 * 点击弹窗标记已读并跳转业务 url。多条堆叠通过维护全局 offset 累加实现。
 * 签名对齐渲染器注册表约定：renderer(message, hooks)，经门面注册后由 notifyMessage 统一分发。
 * </p>
 */

/**
 * 消息类型 → Notification 样式类型
 */
const TYPE_STYLE = {
  notify: 'success',
  warning: 'warning',
  reminder: 'info'
}

/**
 * 优先级 → 自动关闭时长（毫秒）：0 表示常驻
 */
const PRIORITY_DURATION = {
  [PRIORITIES.URGENT]: 0,
  [PRIORITIES.HIGH]: 0,
  [PRIORITIES.NORMAL]: 4500,
  [PRIORITIES.LOW]: 3000
}

/**
 * 当前堆叠偏移量（自顶向下累加）
 */
let stackOffset = 0

/**
 * 跳转业务页面：复用全局 Vue 实例的 router（项目统一约定，避免直接 import router 实例）
 *
 * @param {string} url 路由地址
 */
function navigate(url) {
  const app = window.vueInstance
  if (app && app.$router) {
    app.$router.push(url)
  }
}

/**
 * 展示一条推送消息弹窗（默认渲染器）
 *
 * @param {Object} message 规范化消息对象 { id, type, title, content, priority, url }
 * @param {Object} [hooks] 交互钩子
 * @param {Function} [hooks.onRead] 用户查看（点击）或关闭时回调 (message) => void，用于标记已读
 * @returns {Object} Element UI Notification 实例
 */
export function showPushNotification(message, { onRead } = {}) {
  const type = TYPE_STYLE[message.type] || 'info'
  const duration = PRIORITY_DURATION[message.priority] != null ? PRIORITY_DURATION[message.priority] : 4500
  const markRead = () => {
    if (typeof onRead === 'function') {
      onRead(message)
    }
  }

  const instance = Notification({
    title: message.title || i18n.t('push.defaultTitle'),
    message: message.content,
    type,
    duration,
    offset: stackOffset,
    onClick: () => {
      markRead()
      if (message.url) {
        navigate(message.url)
      }
      instance.close()
    }
  })

  const height = instance && instance.$el ? instance.$el.offsetHeight : 80
  stackOffset += height + 16
  instance.$on('close', () => {
    stackOffset = Math.max(0, stackOffset - (height + 16))
    markRead()
  })
  return instance
}

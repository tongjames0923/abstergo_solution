import { pushSend } from '@/api/push'
import { MESSAGE_TYPES, PRIORITIES, TOPIC_PREFIX } from '@/push/push-message'

/**
 * 系统通知发布封装层（可复用基础工具）
 * <p>
 * 面向管理侧的通知发布能力收敛：表单结构化校验、消息契约组装、按发布范围
 * （全体通配 / 逐用户 topic）投递与结果汇总、本地发布历史存取。
 * 本模块不依赖任何 Vue 组件与 UI 库，可被任意模块直接调用；
 * 用户可读文案由调用方基于返回的结构化结果自行映射 i18n。
 * </p>
 */

/**
 * 表单字段长度上限
 */
export const NOTIFY_FORM_LIMITS = {
  /**
   * 标题最大长度
   */
  title: 50,
  /**
   * 正文最大长度
   */
  content: 500
}

/**
 * 全体范围的通配主题
 */
export const ALL_SCOPE_TOPIC = TOPIC_PREFIX + '*'

/**
 * 发布历史 localStorage 键与容量上限
 */
const HISTORY_KEY = 'PUSH_NOTIFY_HISTORY'
const HISTORY_LIMIT = 50

/**
 * 组装符合消息契约的业务载荷（见 push-message.js MESSAGE_EXAMPLE）
 *
 * @param {Object} form 通知表单 { title, content, type, priority, url? }
 * @returns {Object} 消息载荷 { id, type, title, content, timestamp, priority, sender, url }
 */
export function buildNotifyMessage(form) {
  return {
    id: 'M' + Date.now() + Math.random().toString(36).slice(2, 6),
    type: form.type,
    title: form.title,
    content: form.content,
    timestamp: Date.now(),
    priority: form.priority,
    sender: 'admin',
    url: form.url || ''
  }
}

/**
 * 表单结构化校验（不抛异常，返回结构化结果，文案由调用方映射 i18n）
 * <p>
 * 校验规则：标题/正文必填且不超长；type/priority 须在契约白名单内；
 * 范围 scope 为 all 或 users，users 时用户名列表非空且每项非空字符串。
 * </p>
 *
 * @param {Object} form 通知表单 { title, content, type, priority, url?, scope, users? }
 * @returns {Object} { valid, errors: Array<{field, code}> }，code: required/tooLong/invalid/empty
 */
export function validateNotifyForm(form) {
  const errors = []
  const title = typeof form.title === 'string' ? form.title.trim() : ''
  const content = typeof form.content === 'string' ? form.content.trim() : ''

  if (!title) {
    errors.push({ field: 'title', code: 'required' })
  } else if (title.length > NOTIFY_FORM_LIMITS.title) {
    errors.push({ field: 'title', code: 'tooLong' })
  }

  if (!content) {
    errors.push({ field: 'content', code: 'required' })
  } else if (content.length > NOTIFY_FORM_LIMITS.content) {
    errors.push({ field: 'content', code: 'tooLong' })
  }

  if (!Object.values(MESSAGE_TYPES).includes(form.type)) {
    errors.push({ field: 'type', code: 'invalid' })
  }

  if (!Object.values(PRIORITIES).includes(form.priority)) {
    errors.push({ field: 'priority', code: 'invalid' })
  }

  if (form.scope !== 'all' && form.scope !== 'users') {
    errors.push({ field: 'scope', code: 'invalid' })
  } else if (form.scope === 'users') {
    const users = Array.isArray(form.users) ? form.users : []
    const valid = users.filter(item => typeof item === 'string' && item.trim())
    if (!valid.length) {
      errors.push({ field: 'users', code: 'empty' })
    }
  }

  return { valid: !errors.length, errors }
}

/**
 * 解析发布范围为目标 topic 列表
 *
 * @param {Object} form 通知表单（含 scope/users）
 * @returns {Array<{target: string, topic: string}>} 投递计划（target: '*' 表示全体，否则为用户名）
 */
function buildDeliverPlan(form) {
  if (form.scope === 'all') {
    return [{ target: '*', topic: ALL_SCOPE_TOPIC }]
  }
  const users = Array.isArray(form.users) ? form.users : []
  return users
    .filter(item => typeof item === 'string' && item.trim())
    .map(username => ({ target: username.trim(), topic: TOPIC_PREFIX + username.trim() }))
}

/**
 * 发布系统通知
 * <p>
 * 全体范围走通配主题单次投递；指定用户逐人投递（任一条失败不中断其余投递）。
 * 投递完成后写入本地发布历史。网络异常被捕获并归入该条投递的失败结果，不向上抛出。
 * </p>
 *
 * @param {Object} form 通知表单 { title, content, type, priority, url?, scope, users? }
 * @returns {Promise<Object>} { ok, message, summary: {total, delivered, failed},
 *          results: Array<{target, topic, ok, messageId?, onlineCount?, error?}>, record }
 */
export async function publishNotify(form) {
  const check = validateNotifyForm(form)
  if (!check.valid) {
    return { ok: false, message: null, summary: { total: 0, delivered: 0, failed: 0 }, results: [], record: null, validation: check }
  }

  const message = buildNotifyMessage(form)
  const plan = buildDeliverPlan(form)

  const settled = await Promise.all(plan.map(async item => {
    try {
      const receipt = await pushSend(item.topic, message)
      return { target: item.target, topic: item.topic, ok: true, messageId: receipt.messageId, onlineCount: receipt.onlineCount }
    } catch (e) {
      console.warn('[push-admin] 投递失败', item.target, e)
      return { target: item.target, topic: item.topic, ok: false, error: e && e.message }
    }
  }))

  const delivered = settled.filter(item => item.ok).length
  const failed = settled.length - delivered
  const online = settled.reduce((max, item) => (item.ok && item.onlineCount > max ? item.onlineCount : max), 0)
  const summary = { total: settled.length, delivered, failed }

  const record = {
    id: message.id,
    title: message.title,
    type: message.type,
    priority: message.priority,
    scope: form.scope,
    targets: settled.map(item => item.target),
    publishedAt: message.timestamp,
    delivered,
    failed,
    online
  }
  addHistoryRecord(record)

  return { ok: failed === 0, message, summary, results: settled, record }
}

/**
 * 读取本地发布历史（新记录在前）
 *
 * @returns {Array<Object>} 历史记录数组，读写异常时返回空数组
 */
export function getPublishHistory() {
  try {
    const raw = localStorage.getItem(HISTORY_KEY)
    const list = raw ? JSON.parse(raw) : []
    return Array.isArray(list) ? list : []
  } catch (e) {
    console.warn('[push-admin] 发布历史读取失败', e)
    return []
  }
}

/**
 * 追加一条发布历史（新记录置顶，超上限滚动淘汰最旧记录）
 *
 * @param {Object} record 发布记录（由 publishNotify 组装）
 */
export function addHistoryRecord(record) {
  try {
    const list = getPublishHistory()
    list.unshift(record)
    if (list.length > HISTORY_LIMIT) {
      list.length = HISTORY_LIMIT
    }
    localStorage.setItem(HISTORY_KEY, JSON.stringify(list))
  } catch (e) {
    console.warn('[push-admin] 发布历史写入失败', e)
  }
}

/**
 * 删除单条发布历史
 *
 * @param {string} id 记录标识（消息 id）
 */
export function removeHistoryRecord(id) {
  try {
    const list = getPublishHistory().filter(item => item.id !== id)
    localStorage.setItem(HISTORY_KEY, JSON.stringify(list))
  } catch (e) {
    console.warn('[push-admin] 发布历史删除失败', e)
  }
}

/**
 * 清空全部发布历史
 */
export function clearHistory() {
  try {
    localStorage.removeItem(HISTORY_KEY)
  } catch (e) {
    console.warn('[push-admin] 发布历史清空失败', e)
  }
}

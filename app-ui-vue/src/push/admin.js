import { sendNotify } from '@/api/push-message'
import { MESSAGE_TYPES, PRIORITIES, TOPIC_BROADCAST, TOPIC_USER_NAME_PREFIX } from '@/push/push-message'

/**
 * 系统通知发布封装层（可复用基础工具）
 * <p>
 * 面向管理侧的通知发布能力收敛：表单结构化校验、新链路 notify 接口（/private/push/message/notify）
 * 提交、按发布范围（全体广播 topic / 逐用户精确 topic）投递与结果汇总、本地发布历史存取。
 * 接收人不随表单传递，由后端 TopicPrefixNotifyReceiverResolver 按 topic 前缀查询用户表解析：
 * global.broadcast → 全体激活用户；user.name.{userName} → 按用户名精确匹配。
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
 * 发布历史 localStorage 键与容量上限
 */
const HISTORY_KEY = 'PUSH_NOTIFY_HISTORY'
const HISTORY_LIMIT = 50

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
 * 发布系统通知（新链路 notify 接口）
 * <p>
 * 全体范围走 global.broadcast topic 单次提交；指定用户逐人 user.name.{userName} topic 提交
 * （任一条失败不中断其余投递）。接收人由后端按 topic 前缀查询用户表解析并 fan-out + 发信令，
 * HTTP 请求成功不等于送达——每个目标的真实送达规模以后端返回的 receiverCount 为准：
 * receiverCount=0 表示该目标未解析到任何接收人（如用户名不存在），计入失败目标。
 * 发布完成后写入本地发布历史（delivered=解析到的接收人总数，failed=未解析到接收人的目标数）。
 * 网络异常被捕获并归入该条投递的失败结果，不向上抛出。
 * </p>
 *
 * @param {Object} form 通知表单 { title, content, type, priority, url?, scope, users? }
 * @returns {Promise<Object>} { ok, summary: {total, delivered, failed, receivers},
 *          results: Array<{target, topic, ok, receiverCount, messageId?, error?}>, record, validation? }
 */
export async function publishNotify(form) {
  const check = validateNotifyForm(form)
  if (!check.valid) {
    return {
      ok: false,
      summary: { total: 0, delivered: 0, failed: 0, receivers: 0 },
      results: [],
      record: null,
      validation: check
    }
  }

  const baseForm = {
    msgType: form.type,
    title: form.title,
    content: form.content,
    priority: form.priority,
    url: form.url || '',
    source: 'http'
  }

  const plan = form.scope === 'all'
    ? [{ target: '*', topic: TOPIC_BROADCAST }]
    : form.users
      .filter(item => typeof item === 'string' && item.trim())
      .map(username => ({ target: username.trim(), topic: TOPIC_USER_NAME_PREFIX + username.trim() }))

  const publishedAt = Date.now()
  const settled = await Promise.all(plan.map(async item => {
    try {
      const result = await sendNotify(Object.assign({}, baseForm, { topic: item.topic }))
      const receiverCount = result && typeof result.receiverCount === 'number' ? result.receiverCount : 0
      return {
        target: item.target,
        topic: item.topic,
        ok: result != null && receiverCount > 0,
        receiverCount,
        messageId: result ? result.messageId : null
      }
    } catch (e) {
      console.warn('[push-admin] 发布失败', item.target, e)
      return { target: item.target, topic: item.topic, ok: false, receiverCount: 0, error: e && e.message }
    }
  }))

  const delivered = settled.filter(item => item.ok).length
  const failed = settled.length - delivered
  const receivers = settled.reduce((sum, item) => sum + item.receiverCount, 0)
  const summary = { total: settled.length, delivered, failed, receivers }
  const okResult = settled.find(item => item.ok && item.messageId != null)

  const record = {
    id: okResult ? String(okResult.messageId) : null,
    title: form.title,
    type: form.type,
    priority: form.priority,
    scope: form.scope,
    targets: settled.map(item => item.target),
    publishedAt,
    delivered: receivers,
    failed
  }
  addHistoryRecord(record)

  return { ok: failed === 0, summary, results: settled, record }
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

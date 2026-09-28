import { MESSAGE_TYPES, PRIORITIES } from '@/push/push-message'

/**
 * 消息展示语义层（消息中心与通知预览卡共享）
 * <p>
 * 收敛与"渲染"相关的一切展示语义：类型归组与语义强调色、优先级徽章、相对时间文案、
 * 今天/昨天/更早时间分桶。数据规范化仍归 push-message.js，本层只做纯展示派生，
 * 不触碰 Vuex 与接口；PushNotificationCenter 与 NotifyPreviewCard 统一引用本层，
 * 保证两处视觉映射同构、色板不重复定义。
 * </p>
 */

/**
 * 类型语义强调色（类型导轨 / 类型圆点），键为归组后的展示类型；
 * 取值与全站 macOS 浅色布局同色系（Apple 系统色）
 */
export const TYPE_ACCENTS = {
  notify: '#34c759',
  warning: '#ff9f0a',
  reminder: '#0a84ff',
  other: '#8e8e93'
}

/**
 * 优先级徽章语义色（仅 high/urgent 两级展示徽章）
 */
export const PRIORITY_ACCENTS = {
  high: '#ff9f0a',
  urgent: '#ff3b30'
}

/**
 * 已知消息类型白名单（后端字典扩展出的未知 msgType 统一归组为 other）
 */
const KNOWN_TYPES = Object.values(MESSAGE_TYPES)

/**
 * 归组消息类型：已知类型原样返回，未知类型（后端 msgType 字典可扩展）归入 other，
 * 避免未知类型直出 i18n key 或渲染缺位
 *
 * @param {string} type 规范化后的 msgType
 * @returns {string} 'notify' | 'warning' | 'reminder' | 'other'
 */
export function resolveTypeKey(type) {
  return KNOWN_TYPES.includes(type) ? type : 'other'
}

/**
 * 类型语义强调色
 *
 * @param {string} type 规范化后的 msgType
 * @returns {string} 颜色值
 */
export function typeAccent(type) {
  return TYPE_ACCENTS[resolveTypeKey(type)] || TYPE_ACCENTS.other
}

/**
 * 优先级徽章展示参数（normal/low 不展示徽章）
 *
 * @param {string} priority 规范化后的优先级（normalize 层已将非法值回退 normal）
 * @returns {{visible: boolean, level: string, accent: string}} 徽章参数
 */
export function priorityBadge(priority) {
  if (priority !== PRIORITIES.HIGH && priority !== PRIORITIES.URGENT) {
    return { visible: false, level: priority, accent: '' }
  }
  return { visible: true, level: priority, accent: PRIORITY_ACCENTS[priority] }
}

/**
 * 相对时间文案：1 分钟内"刚刚"、1 小时内"N 分钟前"、24 小时内"N 小时前"，
 * 更早回退本地时间串；i18n 翻译函数由调用方注入，保持本层无 Vue 依赖
 *
 * @param {number} ts 毫秒时间戳（服务端 createTime 优先）
 * @param {Function} t i18n 翻译函数，如 key => this.$t(key)
 * @returns {string} 展示文案，无时间戳时返回空串
 */
export function relativeTime(ts, t) {
  if (!ts) {
    return ''
  }
  const diff = Date.now() - ts
  if (diff < 60_000) {
    return t('push.time.justNow')
  }
  if (diff < 3_600_000) {
    return Math.floor(diff / 60_000) + t('push.time.minutesAgo')
  }
  if (diff < 86_400_000) {
    return Math.floor(diff / 3_600_000) + t('push.time.hoursAgo')
  }
  return new Date(ts).toLocaleString()
}

/**
 * 某毫秒时间戳所在自然日的零点时间戳
 *
 * @param {number} ts 毫秒时间戳
 * @returns {number} 当日零点毫秒时间戳
 */
function startOfDay(ts) {
  const date = new Date(ts)
  date.setHours(0, 0, 0, 0)
  return date.getTime()
}

/**
 * 按今天 / 昨天 / 更早对消息分桶（O(n) 单次遍历）
 * <p>
 * 输入须为到达序（新→旧）排列的消息数组；输出仅包含非空分组且保持该先后顺序，
 * 分组内保持原有顺序。分组依据消息自身 timestamp（服务端 createTime），与用户
 * 本地时区一致。
 * </p>
 *
 * @param {Array<Object>} messages 规范化消息数组
 * @returns {Array<{key: string, items: Array<Object>}>} 非空分组列表
 */
export function groupByDay(messages) {
  if (!messages || !messages.length) {
    return []
  }
  const startToday = startOfDay(Date.now())
  const startYesterday = startToday - 86_400_000
  const buckets = {}
  const order = []
  messages.forEach(message => {
    const ts = message.timestamp || message.receivedAt || Date.now()
    let key = 'earlier'
    if (ts >= startToday) {
      key = 'today'
    } else if (ts >= startYesterday) {
      key = 'yesterday'
    }
    if (!buckets[key]) {
      buckets[key] = []
      order.push(key)
    }
    buckets[key].push(message)
  })
  return order.map(key => ({ key, items: buckets[key] }))
}

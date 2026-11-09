import request from '@/utils/request'

/**
 * 推送消息中心（新链路）接口封装
 * <p>
 * 与后端 PrivatePushMessageController 对齐（基础路径 /private/push/message，注意是 message 单数）；
 * 全部走项目统一 axios 实例（自动携带 token 头与语言头）。
 * 响应经 utils/request 拦截器解包：成功时 resolve 的是 AjaxResult 整包，
 * 故各方法 .then(res => res.data) 取到的是业务体本身（非再包一层 .data）。
 * </p>
 */

/**
 * 将响应业务体归一为非负整数
 * <p>
 * 后端全局把 Long 序列化为字符串以防雪花 ID 精度丢失，未读数等 Long 值可能是字符串；
 * 写入条数是 Integer 数字。统一用 Number 收敛，避免 typeof 判断把合法的字符串值吞成 0。
 * </p>
 *
 * @param {*} data 响应业务体
 * @returns {number} 非负整数
 */
function toCount(data) {
  const value = Number(data)
  return Number.isFinite(value) && value > 0 ? Math.floor(value) : 0
}

/**
 * 分页拉取当前用户消息列表（到达序倒序，含已读）
 * <p>
 * GET /private/push/message/list，入参对齐 PushMessageQueryForm（cursor/limit/readFlag）。
 * </p>
 *
 * @param {Object} params 查询参数
 * @param {number} [params.cursor] 游标：上一页响应返回的 cursor（接收行 id），首页不传
 * @param {number} [params.limit] 每页条数，默认 20，上限 100
 * @param {string} [params.readFlag] PushMessageQueryForm.READ_FLAG_ALL('all') / READ_FLAG_UNREAD('unread')
 * @returns {Promise<{list: Array, cursor: number|null, hasMore: boolean}>} 分页结果（业务体即 PushMessagePageVO）
 */
export function fetchMessages(params) {
  return request({
    url: '/private/push/message/list',
    method: 'get',
    params
  }).then(res => res.data || { list: [], cursor: null, hasMore: false })
}

/**
 * 查询当前用户未读数
 * <p>
 * GET /private/push/message/unread-count，业务体为 Long；经全局 Jackson 序列化后是字符串。
 * </p>
 *
 * @returns {Promise<number>} 未读数
 */
export function fetchUnreadCount() {
  return request({
    url: '/private/push/message/unread-count',
    method: 'get'
  }).then(res => toCount(res.data))
}

/**
 * 批量记录已读（写 READ 操作日志）
 * <p>
 * POST /private/push/message/read，请求体为 List&lt;Long&gt; 裸数组（非 {messageIds} 包装）；
 * 业务体为 Integer（实际记录条数），单次至多 100 个 ID。
 * messageIds 必须是字符串形式的雪花 ID：19 位 ID 超出 2^53，前端转 Number 会失真，
 * 后端据错误 ID 查不到接收关系将静默返回 0。后端 Jackson 已注册字符串/数字双向反序列化。
 * </p>
 *
 * @param {Array<string>} messageIds 消息ID列表（雪花ID字符串，单次 ≤ 100 条）
 * @returns {Promise<number>} 实际记录条数
 */
export function markRead(messageIds) {
  return request({
    url: '/private/push/message/read',
    method: 'post',
    data: messageIds
  }).then(res => toCount(res.data))
}

/**
 * 通用操作回传（CLICK 等，预留扩展）
 * <p>
 * POST /private/push/message/operate，请求体为 PushMessageOperateForm {messageId, opType, extInfo}；
 * messageId 同样须为字符串形式的雪花 ID。业务体为 Integer（实际写入日志条数）。
 * </p>
 *
 * @param {Object} payload 回传体 { messageId, opType, extInfo? }
 * @returns {Promise<number>} 实际写入日志条数
 */
export function operateMessage(payload) {
  return request({
    url: '/private/push/message/operate',
    method: 'post',
    data: payload
  }).then(res => toCount(res.data))
}

/**
 * 发送持久化通知（新链路发送入口，非旧全量推送 /private/push/send）
 * <p>
 * POST /private/push/message/notify，请求体为 PushNotifySendForm
 * （msgType 必填，title/content/priority/url/source 可选）。
 * 注意：接收人不随表单传递，由后端 INotifyReceiverResolver SPI 解析（默认实现为空集=无人接收）。
 * 业务体为 PushSendResultVO {messageId, topic, msgType, receiverCount, signalCount}，
 * receiverCount 是真实送达规模（HTTP 成功 ≠ 送达），0 为合法状态（仅落库无信令）。
 * </p>
 *
 * @param {Object} form PushNotifySendForm { msgType, title?, content?, priority?, url?, source?, extInfo? }
 * @returns {Promise<Object|null>} 发送结果视图 { messageId, topic, msgType, receiverCount, signalCount }
 */
export function sendNotify(form) {
  return request({
    url: '/private/push/message/notify',
    method: 'post',
    data: form
  }).then(res => res.data || null)
}

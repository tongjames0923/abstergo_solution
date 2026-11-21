/**
 * 事件编排元数据归一化工具。
 * <p>
 * 后端元数据在不同版本中存在「id/name」与「reporterId/reporterName」等命名差异，
 * 且分页与列表接口可能返回一层或多层包装。本工具统一解包并对齐字段，
 * 让页面与组件只依赖一套稳定字段，避免下拉框因字段名不一致而空白。
 * </p>
 */

/**
 * 解包列表响应
 *
 * @param {*} res 接口响应或响应 data
 * @returns {Array} 列表数据
 */
export function unwrapList(res) {
  const body = res && res.data !== undefined ? res.data : res
  if (Array.isArray(body)) {
    return body
  }
  if (body && typeof body === 'object') {
    if (Array.isArray(body.data)) {
      return body.data
    }
    if (Array.isArray(body.list)) {
      return body.list
    }
    if (Array.isArray(body.records)) {
      return body.records
    }
  }
  return []
}

/**
 * 解包分页响应
 *
 * @param {*} res 接口响应
 * @returns {{list: Array, total: number}} 列表与总数
 */
export function unwrapPage(res) {
  const body = res && res.data !== undefined ? res.data : res
  const list = unwrapList(body)
  const total = body && typeof body.total === 'number' ? body.total : list.length
  return { list, total }
}

/**
 * 归一化事件上报器摘要
 *
 * @param {*} res 接口响应
 * @returns {Array} 上报器列表
 */
export function toReporterOptions(res) {
  return unwrapList(res).map(item => {
    const reporterId = item.reporterId || item.id || ''
    return {
      reporterId,
      reporterName: item.reporterName || item.name || reporterId,
      description: item.description || '',
      eventCodes: Array.isArray(item.eventCodes) ? item.eventCodes : []
    }
  }).filter(item => item.reporterId)
}

/**
 * 归一化事件元信息
 *
 * @param {*} res 接口响应
 * @returns {Array} 事件列表
 */
export function toEventOptions(res) {
  return unwrapList(res).map(item => {
    const code = item.code || item.eventCode || ''
    return {
      code,
      name: item.name || item.eventName || code,
      label: item.label || item.name || item.eventName || code,
      description: item.description || '',
      paramTypes: Array.isArray(item.paramTypes) ? item.paramTypes
        : (Array.isArray(item.params) ? item.params : [])
    }
  }).filter(item => item.code)
}

/**
 * 归一化触发器元信息
 *
 * @param {*} res 接口响应
 * @returns {Array} 触发器列表
 */
export function toTriggerOptions(res) {
  return unwrapList(res).map(item => {
    const triggerId = item.triggerId || item.id || ''
    return {
      triggerId,
      triggerName: item.triggerName || item.name || triggerId,
      description: item.description || '',
      paramTypes: Array.isArray(item.paramTypes) ? item.paramTypes : []
    }
  }).filter(item => item.triggerId)
}

/**
 * 归一化动作元信息
 *
 * @param {*} res 接口响应
 * @returns {Array} 动作列表
 */
export function toActionOptions(res) {
  return unwrapList(res).map(item => {
    const actionId = item.actionId || item.id || ''
    return {
      actionId,
      actionName: item.actionName || item.name || actionId,
      actionDesc: item.actionDesc || item.description || '',
      paramTypes: Array.isArray(item.paramTypes) ? item.paramTypes
        : (Array.isArray(item.params) ? item.params : [])
    }
  }).filter(item => item.actionId)
}

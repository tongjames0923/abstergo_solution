import request from '@/utils/request'

/**
 * 查询已注册的触发器动作
 */
export function getActionMetas() {
  return request({
    url: '/private/event-action/actions',
    method: 'get'
  })
}

/**
 * 分页查询动作绑定
 * @param {Object} data 查询条件
 * @param {number} pageNum 页码
 * @param {number} pageSize 每页大小
 */
export function queryEventActions(data, pageNum = 1, pageSize = 20) {
  return request({
    url: '/private/event-action/query',
    method: 'post',
    params: { pageNum, pageSize },
    data
  })
}

/**
 * 按事件查询动作绑定（含停用）
 * @param {string} reporterId 事件上报器ID
 * @param {string} eventCode 事件编码
 */
export function getActionsByEvent(reporterId, eventCode) {
  return request({
    url: '/private/event-action/by-event',
    method: 'get',
    params: { reporterId, eventCode }
  })
}

/**
 * 整体保存指定事件下的动作绑定
 * @param {Object} data {reporterId, eventCode, actions}
 */
export function saveEventActions(data) {
  return request({
    url: '/private/event-action/save-event',
    method: 'post',
    data
  })
}

/**
 * 测试执行单个动作
 * @param {Object} data {actionId, paramValues}
 */
export function testEventAction(data) {
  return request({
    url: '/private/event-action/test',
    method: 'post',
    data
  })
}

/**
 * 启用或停用动作绑定
 * @param {number} id 绑定主键
 * @param {boolean} enabled 是否启用
 */
export function setEventActionEnabled(id, enabled) {
  return request({
    url: `/private/event-action/${enabled ? 'enable' : 'disable'}/${id}`,
    method: 'post'
  })
}

/**
 * 删除动作绑定
 * @param {number} id 绑定主键
 */
export function deleteEventAction(id) {
  return request({
    url: `/private/event-action/delete/${id}`,
    method: 'post'
  })
}

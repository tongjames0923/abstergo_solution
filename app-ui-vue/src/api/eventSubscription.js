import request from '@/utils/request'

/**
 * 查询已注册的事件上报器
 */
export function getEventReporters() {
  return request({
    url: '/private/event-subscription/reporters',
    method: 'get'
  })
}

/**
 * 按上报器查询事件定义
 * @param {string} reporterId 事件上报器ID
 */
export function getReporterEvents(reporterId) {
  return request({
    url: '/private/event-subscription/events',
    method: 'get',
    params: { reporterId }
  })
}

/**
 * 查询已注册的触发器
 */
export function getTriggerMetas() {
  return request({
    url: '/private/event-subscription/triggers',
    method: 'get'
  })
}

/**
 * 分页查询事件订阅
 * @param {Object} data 查询条件
 * @param {number} pageNum 页码
 * @param {number} pageSize 每页大小
 */
export function queryEventSubscriptions(data, pageNum = 1, pageSize = 20) {
  return request({
    url: '/private/event-subscription/query',
    method: 'post',
    params: { pageNum, pageSize },
    data
  })
}

/**
 * 按事件查询订阅（含停用）
 * @param {string} reporterId 事件上报器ID
 * @param {string} eventCode 事件编码
 */
export function getSubscriptionsByEvent(reporterId, eventCode) {
  return request({
    url: '/private/event-subscription/by-event',
    method: 'get',
    params: { reporterId, eventCode }
  })
}

/**
 * 整体保存指定事件下的订阅
 * @param {Object} data {reporterId, eventCode, subscriptions}
 */
export function saveEventSubscriptions(data) {
  return request({
    url: '/private/event-subscription/save-event',
    method: 'post',
    data
  })
}

/**
 * 模拟上报事件并返回分发明细
 * @param {Object} data 事件信封
 */
export function reportEvent(data) {
  return request({
    url: '/private/event-subscription/report',
    method: 'post',
    data
  })
}

/**
 * 启用或停用订阅
 * @param {number} id 订阅主键
 * @param {boolean} enabled 是否启用
 */
export function setEventSubscriptionEnabled(id, enabled) {
  return request({
    url: `/private/event-subscription/${enabled ? 'enable' : 'disable'}/${id}`,
    method: 'post'
  })
}

/**
 * 删除订阅
 * @param {number} id 订阅主键
 */
export function deleteEventSubscription(id) {
  return request({
    url: `/private/event-subscription/delete/${id}`,
    method: 'post'
  })
}

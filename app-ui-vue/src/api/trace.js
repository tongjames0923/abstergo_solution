import request from '@/utils/request'

/**
 * 获取所有已记录的rootTraceId列表
 * @returns {Promise} traceId列表
 */
export function listTraceIds() {
  return request({
    url: '/private/trace/list',
    method: 'get'
  })
}

/**
 * 根据rootTraceId查询完整的分布式调用树
 * @param {string} rootTraceId 根traceId
 * @returns {Promise} 完整的分布式调用树JSON
 */
export function getTraceTree(rootTraceId) {
  return request({
    url: '/private/trace/tree',
    method: 'get',
    params: { rootTraceId }
  })
}

/**
 * 根据rootTraceId查询格式化的树形调用链文本
 * @param {string} rootTraceId 根traceId
 * @returns {Promise} 包含text和tree的调用链数据
 */
export function getTraceTreeText(rootTraceId) {
  return request({
    url: '/private/trace/tree/text',
    method: 'get',
    params: { rootTraceId }
  })
}

/**
 * 根据rootTraceId查询调用链的摘要信息
 * @param {string} rootTraceId 根traceId
 * @returns {Promise} 调用链摘要（含耗时、节点数等指标）
 */
export function getTraceSummary(rootTraceId) {
  return request({
    url: '/private/trace/tree/summary',
    method: 'get',
    params: { rootTraceId }
  })
}

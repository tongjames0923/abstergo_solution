import request from '@/utils/request'

/**
 * 获取系统操作类型报表数据
 * @returns {Promise} 报表数据
 */
export function getSystemLogOperateTypeList() {
  return request({
    url: '/private/log/listWithOpType',
    method: 'get'
  })
}

/**
 * 获取日志记录列表
 * @param {Object} form 查询参数
 * @param {Array} form.operateTypes 操作类型列表
 * @param {Date} form.operateTimeBegin 开始时间
 * @param {Date} form.operateTimeEnd 结束时间
 * @param {number} form.pageNum 页码
 * @param {number} form.pageSize 每页数量
 * @returns {Promise} 日志记录列表
 */
export function getSystemLogRecordList(form) {
  return request({
    url: '/private/log/listRecord',
    method: 'post',
    data: form
  })
}

/**
 * 获取所有操作类型列表(用于下拉框搜索)
 * @param {string} keyword 关键词(可选),支持模糊搜索
 * @returns {Promise} 操作类型列表
 */
export function getOperateTypeList(keyword) {
  return request({
    url: '/private/log/operateTypes',
    method: 'get',
    params: keyword ? { keyword } : {}
  })
}


/**
 * 强制刷新日志记录
 * @returns {Promise}
 */
export function flushLog() {
  return request({
    url: '/private/log/forceFlush',
    method: 'post'
  })
}

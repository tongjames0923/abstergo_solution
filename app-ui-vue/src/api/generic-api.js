import request from '@/utils/request'

/**
 * 分页查询泛化API配置列表
 * @param {Object} data - 查询条件
 * @param {string} [data.apiNameLike] - API名称（模糊匹配）
 * @param {string} [data.appNameLike] - 应用名称（模糊匹配）
 * @param {string} [data.interfaceNameLike] - 接口名称（模糊匹配）
 * @param {number[]} [data.flagIn] - 状态过滤
 * @param {number} pageNum - 页码
 * @param {number} pageSize - 每页大小
 * @returns {Promise} 分页结果
 */
export function queryGenericApis(data, pageNum = 1, pageSize = 10) {
  return request({
    url: '/private/admin/generic-api/query',
    method: 'post',
    params: { pageNum, pageSize },
    data
  })
}

/**
 * 根据ID获取泛化API配置
 * @param {number} id - API配置ID
 * @returns {Promise} API配置详情
 */
export function getGenericApi(id) {
  return request({
    url: `/private/admin/generic-api/get/${id}`,
    method: 'get'
  })
}

/**
 * 保存泛化API配置（新增或更新）
 * @param {Object} data - API配置信息
 * @returns {Promise} 保存后的API配置
 */
export function saveGenericApi(data) {
  return request({
    url: '/private/admin/generic-api/save',
    method: 'post',
    data
  })
}

/**
 * 删除泛化API配置
 * @param {number} id - API配置ID
 * @returns {Promise} 操作结果
 */
export function deleteGenericApi(id) {
  return request({
    url: `/private/admin/generic-api/delete/${id}`,
    method: 'post'
  })
}

/**
 * 启用泛化API
 * @param {string} apiId - API ID
 * @returns {Promise} 操作结果
 */
export function enableGenericApi(apiId) {
  return request({
    url: `/private/admin/generic-api/enable/${apiId}`,
    method: 'post'
  })
}

/**
 * 禁用泛化API
 * @param {string} apiId - API ID
 * @returns {Promise} 操作结果
 */
export function disableGenericApi(apiId) {
  return request({
    url: `/private/admin/generic-api/disable/${apiId}`,
    method: 'post'
  })
}

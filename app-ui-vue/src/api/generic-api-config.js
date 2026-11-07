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
 * 查询指定泛化API已登记的入参定义
 * @param {string} apiId - API唯一标识
 * @returns {Promise} 入参定义列表（按 paramOrder 升序）
 */
export function getGenericApiParams(apiId) {
  return request({
    url: `/private/admin/generic-api/params/${apiId}`,
    method: 'get'
  })
}

/**
 * 保存泛化API配置并整组替换其入参定义
 * @param {Object} data - 整组保存表单
 * @param {Object} data.apiConfig - API声明（无 id 则新增，有 id 则更新）
 * @param {Array} data.params - 入参定义列表，空数组表示目标方法无入参
 * @returns {Promise} 保存后的API配置
 */
export function saveGenericApiWithParams(data) {
  return request({
    url: '/private/admin/generic-api/saveWithParams',
    method: 'post',
    data
  })
}

/**
 * 删除泛化API配置（其入参定义由服务端联动清除）
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

/**
 * 获取所有可用的预认证实现列表
 * @returns {Promise} 预认证标识列表
 */
export function getAvailablePreAuths() {
  return request({
    url: '/private/admin/generic-api/allPreAuth',
    method: 'get'
  })
}

/**
 * 查询泛化API的入参元数据（新版调用链路的自描述入口）
 * @param {string} apiId - API唯一标识
 * @param {number} [version] - 版本，空表示不约束
 * @returns {Promise} 入参元数据列表
 */
export function getGenericApiMeta(apiId, version) {
  return request({
    url: `/private/genric/meta/${apiId}`,
    method: 'get',
    params: version === '' || version === null || version === undefined ? {} : { version }
  })
}

/**
 * 按新版链路调用泛化API
 * @param {string} apiId - API唯一标识
 * @param {Object|Array} body - 命名式对象或位置式数组
 * @param {number} [version] - 版本，空表示不约束
 * @returns {Promise} 统一响应，data 为调用结果视图
 */
export function invokeGenericApi(apiId, body, version) {
  return request({
    url: `/private/genric/call/${apiId}`,
    method: 'post',
    params: version === '' || version === null || version === undefined ? {} : { version },
    data: body
  })
}

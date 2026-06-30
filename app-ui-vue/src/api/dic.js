import request from '@/utils/request'

/**
 * 查询数据字典列表（模糊查询）
 * @param {Object} data - 查询条件
 * @param {string} [data.dicCode] - 字典编码（模糊匹配）
 * @param {string} [data.dicName] - 字典名称（模糊匹配）
 * @returns {Promise} 字典模型列表
 */
export function queryDics(data) {
  return request({
    url: '/private/admin/dic/query',
    method: 'post',
    data
  })
}

/**
 * 保存数据字典（新增或更新）
 * @param {Object} data - 字典信息
 * @param {string} data.dicCode - 字典编码
 * @param {string} data.dicName - 字典名称
 * @returns {Promise} 保存后的字典实体
 */
export function saveDic(data) {
  return request({
    url: '/private/admin/dic/saveDic',
    method: 'post',
    data
  })
}

/**
 * 启用数据字典
 * @param {string} dicCode - 字典编码
 * @returns {Promise} 操作结果
 */
export function enableDic(dicCode) {
  return request({
    url: '/private/admin/dic/enableDic',
    method: 'post',
    data: { dicCode }
  })
}

/**
 * 禁用数据字典
 * @param {string} dicCode - 字典编码
 * @returns {Promise} 操作结果
 */
export function disableDic(dicCode) {
  return request({
    url: '/private/admin/dic/disableDic',
    method: 'post',
    data: { dicCode }
  })
}

/**
 * 删除数据字典
 * @param {string} dicCode - 字典编码
 * @returns {Promise} 操作结果
 */
export function deleteDic(dicCode) {
  return request({
    url: '/private/admin/dic/deleteDic',
    method: 'post',
    data: { dicCode }
  })
}

/**
 * 保存数据字典项（新增或更新）
 * @param {Object} data - 字典项信息
 * @param {string} data.dicCode - 字典编码
 * @param {string} data.dicItemLabel - 字典项标签
 * @param {string} data.dicItemValue - 字典项值
 * @returns {Promise} 操作结果
 */
export function saveDicItem(data) {
  return request({
    url: '/private/admin/dic/saveDicItem',
    method: 'post',
    data
  })
}

/**
 * 启用数据字典项
 * @param {string} dicCode - 字典编码
 * @param {string} label - 字典项标签
 * @returns {Promise} 操作结果
 */
export function enableDicItem(dicCode, label) {
  return request({
    url: '/private/admin/dic/enableDicItem',
    method: 'post',
    data: { dicCode, label }
  })
}

/**
 * 禁用数据字典项
 * @param {string} dicCode - 字典编码
 * @param {string} label - 字典项标签
 * @returns {Promise} 操作结果
 */
export function disableDicItem(dicCode, label) {
  return request({
    url: '/private/admin/dic/disableDicItem',
    method: 'post',
    data: { dicCode, label }
  })
}

/**
 * 删除数据字典项
 * @param {string} dicCode - 字典编码
 * @param {string} label - 字典项标签
 * @returns {Promise} 操作结果
 */
export function deleteDicItem(dicCode, label) {
  return request({
    url: '/private/admin/dic/deleteDicItem',
    method: 'post',
    data: { dicCode, label }
  })
}

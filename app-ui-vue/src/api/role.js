import request from '@/utils/request'

/**
 * 查询角色列表
 * @param {Object} data - 查询条件
 * @param {number} [data.idEquals] - 根据ID精确查询
 * @param {string} [data.roleEquals] - 根据角色名精确查询
 * @param {number} [data.flagEquals] - 根据标志位精确查询
 * @param {Object} [data.createTimeOrder] - 创建时间排序
 * @param {Object} [data.updateTimeOrder] - 更新时间排序
 * @param {number} [pageNum] - 页码
 * @param {number} [pageSize] - 每页数量
 * @returns {Promise} 查询结果
 */
export function queryRoles(data, pageNum, pageSize) {
  return request({
    url: '/private/admin/role/query',
    method: 'post',
    data,
    params: {
      pageNum,
      pageSize
    }
  })
}

/**
 * 根据ID获取角色信息
 * @param {number} id - 角色ID
 * @returns {Promise} 角色信息
 */
export function getRoleById(id) {
  return request({
    url: `/private/admin/role/get/${id}`,
    method: 'get'
  })
}

/**
 * 保存角色信息（新增或更新）
 * @param {Object} data - 角色信息
 * @param {number} [data.id] - 角色ID，有ID为更新，无ID为新增
 * @param {string} data.role - 角色名
 * @param {string} data.description - 描述
 * @param {number} data.flag - 状态
 * @returns {Promise} 保存结果
 */
export function saveRole(data) {
  return request({
    url: '/private/admin/role/save',
    method: 'post',
    data
  })
}

/**
 * 删除角色
 * @param {number} id - 角色ID
 * @returns {Promise} 删除结果
 */
export function deleteRole(id) {
  return request({
    url: `/private/admin/role/delete/${id}`,
    method: 'post'
  })
}

/**
 * 激活角色
 * @param {number} id - 角色ID
 * @returns {Promise} 操作结果
 */
export function activateRole(id) {
  return request({
    url: `/private/admin/role/activite/${id}`,
    method: 'get'
  })
}

/**
 * 禁用角色
 * @param {number} id - 角色ID
 * @returns {Promise} 操作结果
 */
export function disableRole(id) {
  return request({
    url: `/private/admin/role/disable/${id}`,
    method: 'get'
  })
}

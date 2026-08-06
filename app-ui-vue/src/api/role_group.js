import request from '@/utils/request'

/**
 * 查询角色组列表
 * @param {Object} data - 查询条件
 * @param {string} [data.nameLike] - 根据角色组名称模糊查询
 * @param {string} [data.descriptionLike] - 根据描述模糊查询
 * @param {number[]} [data.flagIn] - 根据状态列表查询
 * @param {Object} [data.createTimeOrder] - 创建时间排序
 * @param {Object} [data.updateTimeOrder] - 更新时间排序
 * @param {number} [pageNum] - 页码
 * @param {number} [pageSize] - 每页数量
 * @returns {Promise} 查询结果
 */
export function queryRoleGroups(data, pageNum, pageSize) {
  return request({
    url: '/private/role/group/query',
    method: 'post',
    data,
    params: {
      pageNum,
      pageSize
    }
  })
}

/**
 * 根据ID获取角色组信息
 * @param {number} id - 角色组ID
 * @returns {Promise} 角色组信息
 */
export function getRoleGroupById(id) {
  return request({
    url: `/private/role/group/get/${id}`,
    method: 'get'
  })
}

/**
 * 保存角色组信息（新增或更新）
 * @param {Object} data - 角色组信息
 * @param {number} [data.id] - 角色组ID，有ID为更新，无ID为新增
 * @param {string} data.roleGroupName - 角色组名称
 * @param {string} data.description - 描述
 * @param {number} data.flag - 状态
 * @returns {Promise} 保存结果
 */
export function saveRoleGroup(data) {
  return request({
    url: '/private/role/group/save',
    method: 'post',
    data
  })
}

/**
 * 删除角色组
 * @param {number} id - 角色组ID
 * @returns {Promise} 删除结果
 */
export function deleteRoleGroup(id) {
  return request({
    url: `/private/role/group/delete/${id}`,
    method: 'post'
  })
}

/**
 * 获取角色组下的权限列表
 * @param {number} groupId - 角色组ID
 * @returns {Promise} 权限列表
 */
export function getRoleListByGroupId(groupId) {
  return request({
    url: `/private/role/group/roles/${groupId}`,
    method: 'get'
  })
}

/**
 * 添加角色到角色组
 * @param {number} groupId - 角色组ID
 * @param {number} roleId - 角色ID
 * @returns {Promise} 操作结果
 */
export function appendRoleToGroup(groupId, roleId) {
  return request({
    url: `/private/role/group/roles/${groupId}/${roleId}`,
    method: 'post'
  })
}

/**
 * 移除角色组下的角色
 * @param {number} groupId - 角色组ID
 * @param {number} roleId - 角色ID
 * @returns {Promise} 操作结果
 */
export function removeRoleFromGroup(groupId, roleId) {
  return request({
    url: `/private/role/group/roles/${groupId}/${roleId}`,
    method: 'delete'
  })
}

/**
 * 分配用户到角色组
 * @param {number} groupId - 角色组ID
 * @param {number} userId - 用户ID
 * @returns {Promise} 操作结果
 */
export function appendUserToGroup(groupId, userId) {
  return request({
    url: `/private/role/group/users/${groupId}/${userId}`,
    method: 'post'
  })
}

/**
 * 移除用户从角色组
 * @param {number} groupId - 角色组ID
 * @param {number} userId - 用户ID
 * @returns {Promise} 操作结果
 */
export function removeUserFromGroup(groupId, userId) {
  return request({
    url: `/private/role/group/users/${groupId}/${userId}`,
    method: 'delete'
  })
}

/**
 * 获取角色组下的用户列表
 * @param {number} groupId - 角色组ID
 * @returns {Promise} 用户列表
 */
export function getUserListByGroupId(groupId) {
  return request({
    url: `/private/role/group/users/${groupId}`,
    method: 'get'
  })
}

/**
 * 获取用户的角色组列表
 * @param {number} userId - 用户ID
 * @returns {Promise} 用户角色组列表
 */
export function getUserRoleGroupVOList(userId) {
  return request({
    url: `/private/role/group/user/${userId}`,
    method: 'get'
  })
}

/**
 * 检查权限是否存在于任意角色组
 * @param {number} roleId - 权限ID
 * @returns {Promise} 检查结果
 */
export function isRoleInAnyGroup(roleId) {
  return request({
    url: `/private/role/group/check/role/${roleId}`,
    method: 'get'
  })
}

/**
 * 检查用户是否在任意角色组
 * @param {number} userId - 用户ID
 * @returns {Promise} 检查结果
 */
export function isUserInAnyGroup(userId) {
  return request({
    url: `/private/role/group/check/user/${userId}`,
    method: 'get'
  })
}

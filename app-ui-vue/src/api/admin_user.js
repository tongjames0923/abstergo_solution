import request from '@/utils/request'

/**
 * 管理员查询用户
 * @param {Object} data - 查询条件
 * @param {string} [data.idEquals] - 根据ID精确查询
 * @param {string} [data.userNameEquals] - 根据用户名精确查询
 * @param {string} [data.nickNameLike] - 根据昵称模糊查询
 * @param {number} [data.flagEquals] - 根据标志位精确查询
 * @param {string} [data.createTimeGreaterThan] - 创建时间大于该值
 * @param {string} [data.createTimeLessThan] - 创建时间小于该值
 * @param {string} [data.updateTimeGreaterThan] - 更新时间大于该值
 * @param {string} [data.updateTimeLessThan] - 更新时间小于该值
 * @param {Object} [data.createTimeOrder] - 创建时间排序
 * @param {Object} [data.updateTimeOrder] - 更新时间排序
 * @param {number} [pageNum] - 页码
 * @param {number} [pageSize] - 每页数量
 * @returns {Promise} 查询结果
 */
export function adminQueryUser(data) {
  return request({
    url: '/private/admin/user/query',
    method: 'post',
    data
  })
}

// ... existing code ...
/**
 * 激活用户账户
 * @param {number} userId - 用户ID
 * @returns {Promise} 操作结果
 */
export function activateUser(userId) {
  return request({
    url: `/private/admin/account/activite/${userId}`,
    method: 'get'
  })
}

/**
 * 禁用用户账户
 * @param {number} userId - 用户ID
 * @returns {Promise} 操作结果
 */
export function disableUser(userId) {
  return request({
    url: `/private/admin/account/disable/${userId}`,
    method: 'get'
  })
}

/**
 * 保存用户信息（新增或更新）
 * @param {Object} data - 用户信息
 * @param {number} [data.id] - 用户ID，有ID为更新，无ID为新增
 * @param {string} data.userName - 用户名
 * @param {string} [data.password] - 密码
 * @param {string} data.nickName - 昵称
 * @param {string} data.remark - 备注
 * @param {string} data.avatar - 头像URL
 * @returns {Promise} 保存结果
 */
export function saveUser(data) {
  return request({
    url: '/private/admin/user/save',
    method: 'post',
    data
  })
}

export function getRolesByUserIdAdmin(userId) {
  return request({
    url: `/private/admin/roles/${userId}`,
    method: 'get'
  })
}

/**
 * 为用户启用角色
 * @param {number} userId - 用户ID
 * @param {string} role - 角色名称
 * @returns {Promise} 操作结果
 */
export function enableRoleForUser(userId, role) {
  return request({
    url: `/private/admin/role/enable/${userId}`,
    method: 'get',
    params: { role }
  })
}

export function disableRoleForUser(userId, role) {
  return request({
    url: `/private/admin/role/disable/${userId}`,
    method: 'get',
    params: { role }
  })
}

/**
 * 查询所有角色
 * @param {Object} data - 查询条件
 * @returns {Promise} 查询结果
 */
export function queryRoles(data) {
  return request({
    url: '/private/admin/roles/query',
    method: 'post',
    data
  })
}

/**
 * 删除用户
 * @param userId
 * @returns {AxiosPromise}
 */
export function deleteUser(userId) {
  return request({
    url: `/private/admin/user/delete/${userId}`,
    method: 'get'
  })
}

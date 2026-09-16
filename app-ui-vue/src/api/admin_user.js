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
export function adminQueryUser(data, pageNum, pageSize) {
  return request({
    url: '/private/admin/user/query',
    params: {
      pageNum,
      pageSize
    },
    method: 'post',
    data
  })
}

/**
 * 根据ID获取用户详情
 * @param {number} userId - 用户ID
 * @returns {Promise} 用户详情（AjaxResult.data 为 SystemUserEntity，用户不存在时后端返回业务异常）
 */
export function getUserDetail(userId) {
  return request({
    url: `/private/admin/user/get/${userId}`,
    method: 'get'
  })
}

/**
 * 激活用户账户
 * @param {number} userId - 用户ID
 * @returns {Promise} 操作结果
 */
export function activateUser(userId) {
  return request({
    url: `/private/admin/roles/account/activite/${userId}`,
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
    url: `/private/admin/roles/account/disable/${userId}`,
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

/**
 * 删除用户
 * @param userId
 * @returns {AxiosPromise}
 */
export function deleteUser(userId) {
  return request({
    url: `/private/admin/user/delete/${userId}`,
    method: 'post'
  })
}

/**
 * 获取用户登录的token
 * @param userId
 * @returns {*}
 */
export function getUserSessions(userId) {
  return request({
    url: 'private/admin/user/sessions/' + userId,
    method: 'get'
  })
}

/**
 * 获取用户登录的token
 * @param sessionId
 * @returns {*}
 */
export function getTokensBySessionId(sessionId) {
  return request({
    url: 'private/admin/user/tokens/' + sessionId,
    method: 'get'
  })
}

/**
 * 禁用用户的Token
 * @param token - 要禁用的Token
 * @param userId - 用户ID
 * @returns {*}
 */
export function disableUserToken(token, userId) {
  return request({
    url: 'private/admin/user/token/disable/',
    method: 'post',
    data: {
      token: token,
      userId: userId
    }
  })
}

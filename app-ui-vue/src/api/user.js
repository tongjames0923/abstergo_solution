import request from '@/utils/request'

export function login(data) {
  return request({
    url: '/public/user/login',
    method: 'post',
    data
  })
}

export function getInfo() {
  return request({
    url: '/private/user/info',
    method: 'get'
  })
}

export function logout() {
  return request({
    url: '/private/user/logout',
    method: 'get'
  })
}

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

import request from '@/utils/request'

export function login(data) {
  return request({
    url: '/public/user/login',
    method: 'post',
    data
  })
}

/**
 * 统一登录授权：按授权类型换取访问令牌
 * @param {string} grantType - 授权类型标识，与后端 ILoginGrantProvider 对齐（如 'wecom'）
 * @param {Object} credential - 凭证参数，字段由各授权方式表单契约约定（如 { code }）
 * @returns {Promise} 令牌信息（AjaxResult.data 为 ITokenModel）
 */
export function loginByGrant(grantType, credential) {
  return request({
    url: `/public/user/login/${grantType}`,
    method: 'post',
    data: credential
  })
}

export function getInfo() {
  return request({
    url: '/private/user/info',
    method: 'get'
  })
}

export function logout() {
  return request('/private/user/logout')
}

export function userStatusEnums() {
  return request({
    url: '/private/user/statusOptions',
    method: 'get'
  })
}

export function getRefreshToken() {
  return request({
    url: '/private/user/getRefreshToken', method: 'get'
  })
}

/**
 * 设置当前选中的部门
 * @param {number} departmentId - 部门ID
 * @returns {Promise}
 */
export function setCurrentDepartment(departmentId) {
  return request({
    url: `/private/user/setCurrentDepartment/${departmentId}`,
    method: 'post'
  })
}

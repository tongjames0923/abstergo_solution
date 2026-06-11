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

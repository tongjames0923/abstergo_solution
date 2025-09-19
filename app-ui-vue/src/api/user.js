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

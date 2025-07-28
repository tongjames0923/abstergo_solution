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

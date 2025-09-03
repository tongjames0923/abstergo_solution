import Cookies from 'js-cookie'

import defaultSettings from '@/settings'

const TokenKey = defaultSettings.tokenCookieKey

// 获取Token - 首先从localStorage获取，如果不存在则从Cookie获取
export function getToken() {
  // 检查是否保存了"记住我"的Token
  const rememberMeToken = localStorage.getItem(TokenKey)
  if (rememberMeToken) {
    return rememberMeToken
  }
  // 否则从Cookie获取
  return Cookies.get(TokenKey)
}

// 设置Token - 根据是否记住我决定存储方式
export function setToken(token, rememberMe = false) {
  if (rememberMe) {
    // "记住我"时保存到localStorage，长期有效
    localStorage.setItem(TokenKey, token)
    // 同时也保存到Cookie，保持兼容性
    Cookies.set(TokenKey, token, { expires: 7 }) // 7天过期
  } else {
    // 不记住我时只保存到Cookie，浏览器关闭后失效
    Cookies.set(TokenKey, token)
  }
}

// 移除Token - 同时清理localStorage和Cookie
export function removeToken() {
  localStorage.removeItem(TokenKey)
  Cookies.remove(TokenKey)
}

// 设置记住我的状态
export function setRememberMe(rememberMe) {
  if (rememberMe) {
    localStorage.setItem('REMEMBER_ME', 'true')
  } else {
    localStorage.removeItem('REMEMBER_ME')
  }
}

// 获取记住我的状态
export function getRememberMe() {
  return localStorage.getItem('REMEMBER_ME') === 'true'
}

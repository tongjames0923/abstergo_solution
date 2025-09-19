import defaultSettings from '@/settings'
import { getRefreshToken } from '@/api/user'

const TokenKey = defaultSettings.tokenCookieKey

// 获取Token - 首先从localStorage获取，如果不存在则从Cookie获取
export function getToken() {
  // 检查是否保存了"记住我"的Token
  const rememberMeToken = localStorage.getItem(TokenKey)
  return rememberMeToken
}

// 设置Token - 根据是否记住我决定存储方式
export function setToken(token, rememberMe = false) {
  localStorage.setItem(TokenKey, token)
  if (rememberMe) {
    getRefreshToken().then((res) => {
      console.log('无感刷新Token设置成功')
    })
  }
}

// 移除Token - 同时清理localStorage和Cookie
export function removeToken() {
  localStorage.removeItem(TokenKey)
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

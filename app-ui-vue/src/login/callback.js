/**
 * 登录回调分发器
 *
 * 约定：SSO 授权回调以 query 参数回到登录路由，state 携带发起跳转的 Provider id
 * （OAuth 通用约定，亦兼容 query.loginMethod 显式指定）
 */
import { getProviderById } from './registry'

/**
 * 按回调参数路由到对应 Provider 的 handleCallback
 * @param {Object} query - 回调路由查询参数（含 code/state 等）
 * @param {Object} context - 宿主登录上下文（LoginContext）
 * @returns {Promise<boolean>} 是否已被某个 Provider 消费并发起登录
 */
export async function dispatchLoginCallback(query, context) {
  if (!query || !query.code) {
    return false
  }
  const providerId = query.state || query.loginMethod
  if (!providerId) {
    return false
  }
  const provider = getProviderById(providerId)
  if (!provider || typeof provider.handleCallback !== 'function') {
    return false
  }
  return provider.handleCallback(query, context)
}

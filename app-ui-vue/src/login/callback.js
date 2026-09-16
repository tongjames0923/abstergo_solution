/**
 * 登录回调分发器
 *
 * 约定：SSO 授权回调以 query 参数回到登录路由，state 携带发起跳转的 Provider id
 * （OAuth 通用约定，亦兼容 query.loginMethod 显式指定）
 */
import { getProviderById } from './registry'

/**
 * 模块加载期捕获地址栏 search 查询参数
 * <p>
 * hash 路由模式下，OAuth 授权回调的 code/state 附加在 {@code #} 之前的 search 部分，
 * 而 {@code $route.query} 只读取 hash 内的查询串，且路由守卫改写地址时会丢弃 search 部分；
 * 故在模块加载（早于首次路由解析）时先行捕获留存。
 * </p>
 */
const BOOT_SEARCH_QUERY = (typeof window !== 'undefined' && window.location && window.location.search)
  ? Object.fromEntries(new URLSearchParams(window.location.search))
  : {}

/**
 * 合并路由查询与启动期 search 参数
 * @param {Object} routeQuery - 当前路由查询对象（可为空）
 * @returns {Object} 合并后的查询对象（路由值优先，缺失项由 search 补齐）
 */
function mergeQuery(routeQuery) {
  const merged = { ...(routeQuery || {}) }
  Object.keys(BOOT_SEARCH_QUERY).forEach(key => {
    const current = merged[key]
    if (current === undefined || current === null || current === '') {
      merged[key] = BOOT_SEARCH_QUERY[key]
    }
  })
  return merged
}

/**
 * 清理地址栏 search 中的回调查询参数
 * <p>
 * 授权码一次性，分发完成后立即移除 search 部分（保留 hash 路由），
 * 避免刷新页面触发对已失效 code 的重放。
 * </p>
 */
function cleanLocationSearch() {
  if (typeof window === 'undefined' || !window.history || !window.history.replaceState) {
    return
  }
  const { origin, pathname, hash } = window.location
  window.history.replaceState({}, '', `${origin}${pathname}${hash || ''}`)
}

/**
 * 按回调参数路由到对应 Provider 的 handleCallback
 * <p>
 * 查询来源合并了路由查询与启动期 search 参数，兼容 history 与 hash 两种路由模式的回调落点。
 * </p>
 * @param {Object} query - 回调路由查询参数（含 code/state 等）
 * @param {Object} context - 宿主登录上下文（LoginContext）
 * @returns {Promise<boolean>} 是否已被某个 Provider 消费并发起登录
 */
export async function dispatchLoginCallback(query, context) {
  const merged = mergeQuery(query)
  if (!merged.code) {
    return false
  }
  const providerId = merged.state || merged.loginMethod
  if (!providerId) {
    return false
  }
  const provider = getProviderById(providerId)
  if (!provider || typeof provider.handleCallback !== 'function') {
    return false
  }
  try {
    return await provider.handleCallback(merged, context)
  } finally {
    cleanLocationSearch()
  }
}

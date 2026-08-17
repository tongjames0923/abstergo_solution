/**
 * 路由缓存服务
 * 用于缓存从后端获取的路由数据，减少重复请求
 */

const ROUTE_CACHE_KEY = 'dynamic_routes_cache'
const ROUTE_CACHE_TIMESTAMP_KEY = 'dynamic_routes_timestamp'
const ROUTE_VERSION_KEY = 'dynamic_routes_version'
const DEFAULT_CACHE_DURATION = 15 * 60 * 1000 // 默认缓存15分钟

class RouteCacheService {
  constructor() {
    this.cacheDuration = DEFAULT_CACHE_DURATION
  }

  /**
   * 设置缓存过期时间
   * @param {number} duration - 缓存时长（毫秒）
   */
  setCacheDuration(duration) {
    this.cacheDuration = duration
  }

  /**
   * 获取缓存的路由数据
   * @returns {Object|null} 缓存的路由数据或null
   */
  getCachedRoutes() {
    try {
      const cachedData = localStorage.getItem(ROUTE_CACHE_KEY)
      const timestamp = localStorage.getItem(ROUTE_CACHE_TIMESTAMP_KEY)

      if (!cachedData || !timestamp) {
        return null
      }

      // 检查缓存是否过期
      const now = Date.now()
      const cacheTime = parseInt(timestamp, 10)

      if (now - cacheTime > this.cacheDuration) {
        // 缓存已过期，清除缓存
        this.clearCache()
        return null
      }

      return JSON.parse(cachedData)
    } catch (error) {
      console.error('获取路由缓存失败:', error)
      return null
    }
  }

  /**
   * 缓存路由数据
   * @param {Array} routes - 要缓存的路由数据
   */
  cacheRoutes(routes) {
    try {
      localStorage.setItem(ROUTE_CACHE_KEY, JSON.stringify(routes))
      localStorage.setItem(ROUTE_CACHE_TIMESTAMP_KEY, Date.now().toString())
      console.log('路由数据已缓存')
    } catch (error) {
      console.error('缓存路由数据失败:', error)
    }
  }

  /**
   * 清除缓存（不清除本地版本号，版本号仅在实际拉取路由后更新）
   */
  clearCache() {
    try {
      localStorage.removeItem(ROUTE_CACHE_KEY)
      localStorage.removeItem(ROUTE_CACHE_TIMESTAMP_KEY)
      console.log('路由缓存已清除')
    } catch (error) {
      console.error('清除路由缓存失败:', error)
    }
  }

  /**
   * 获取本地记录的视图版本号
   * @returns {string|null} 本地版本号或null
   */
  getLocalVersion() {
    try {
      return localStorage.getItem(ROUTE_VERSION_KEY)
    } catch (error) {
      console.error('获取本地视图版本失败:', error)
      return null
    }
  }

  /**
   * 记录本地视图版本号（实际从后端拉取路由后调用）
   * @param {string|number} version - 视图版本号
   */
  setLocalVersion(version) {
    try {
      if (version !== null && version !== undefined && String(version) !== '0') {
        localStorage.setItem(ROUTE_VERSION_KEY, String(version))
      }
    } catch (error) {
      console.error('记录本地视图版本失败:', error)
    }
  }

  /**
   * 检查缓存是否存在且有效
   * @returns {boolean} 缓存是否有效
   */
  hasValidCache() {
    return this.getCachedRoutes() !== null
  }

  /**
   * 强制刷新缓存（先清除再获取新数据）
   * @param {Function} fetchFunction - 获取路由数据的函数
   * @returns {Promise<Array>} 新的路由数据
   */
  async refreshCache(fetchFunction) {
    this.clearCache()
    try {
      const routes = await fetchFunction()
      this.cacheRoutes(routes)
      return routes
    } catch (error) {
      console.error('刷新路由缓存失败:', error)
      throw error
    }
  }
}

// 创建单例实例
const routeCacheService = new RouteCacheService()

export default routeCacheService

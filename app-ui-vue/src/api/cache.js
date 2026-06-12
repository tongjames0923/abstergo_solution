import request from '@/utils/request'

/**
 * 获取所有缓存名称
 * @returns {Promise} 缓存名称列表
 */
export function listCacheNames() {
  return request({
    url: '/private/cache/names',
    method: 'get'
  })
}

/**
 * 获取指定缓存的键列表
 * @param {string} cacheName - 缓存名称
 * @returns {Promise} 缓存键列表
 */
export function getCacheKeys(cacheName) {
  return request({
    url: '/private/cache/keys',
    method: 'post',
    data: { cacheName }
  })
}

/**
 * 根据缓存名称和键获取缓存值
 * @param {string} cacheName - 缓存名称
 * @param {string} key - 缓存键
 * @returns {Promise} 缓存值
 */
export function getCache(cacheName, key) {
  return request({
    url: '/private/cache/get',
    method: 'post',
    data: { cacheName, key }
  })
}

/**
 * 清除指定缓存（如果未指定key则清除整个缓存）
 * @param {string} cacheName - 缓存名称
 * @param {string} [key] - 缓存键，不传则清除整个缓存
 * @returns {Promise} 操作结果
 */
export function evictCache(cacheName, key) {
  return request({
    url: '/private/cache/evict',
    method: 'post',
    data: { cacheName, key }
  })
}

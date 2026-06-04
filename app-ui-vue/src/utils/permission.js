/**
 * 权限工具函数
 * 统一管理前端权限判断逻辑
 */

/**
 * 检查用户是否拥有所需角色（或关系）
 * 用户拥有 requiredRoles 中的任意一个角色即返回 true
 *
 * @param {Array} userRoles - 用户拥有的角色数组
 * @param {Array} requiredRoles - 需要的角色数组
 * @returns {boolean}
 */
export function hasRole(userRoles = [], requiredRoles = []) {
  // 如果没有设置角色要求，则对所有用户放行
  if (!requiredRoles || requiredRoles.length === 0) {
    return true
  }
  // 或关系：用户拥有任一必需角色即可
  return requiredRoles.some(role => userRoles.includes(role))
}

/**
 * 检查用户是否拥有所有角色（与关系）
 * 用户必须拥有 requiredRoles 中的所有角色才返回 true
 *
 * @param {Array} userRoles - 用户拥有的角色数组
 * @param {Array} requiredRoles - 需要的角色数组
 * @returns {boolean}
 */
export function hasAllRoles(userRoles = [], requiredRoles = []) {
  // 如果没有设置角色要求，则对所有用户放行
  if (!requiredRoles || requiredRoles.length === 0) {
    return true
  }
  // 与关系：用户必须拥有所有必需角色
  return requiredRoles.every(role => userRoles.includes(role))
}

/**
 * 检查路由是否对当前用户可见
 *
 * @param {Object} route - 路由对象
 * @param {Array} userRoles - 用户拥有的角色数组
 * @returns {boolean}
 */
export function isRouteVisible(route, userRoles = []) {
  // 如果路由没有设置角色要求，则对所有用户可见
  if (!route.meta || !route.meta.roles || route.meta.roles.length === 0) {
    return true
  }
  // 使用或关系判断
  return hasRole(userRoles, route.meta.roles)
}

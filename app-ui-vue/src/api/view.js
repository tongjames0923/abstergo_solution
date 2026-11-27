import request from '@/utils/request'

export function getFormConfig(configName) {
  return request({
    url: '/private/view/form/' + configName,
    method: 'get'
  })
}

export function saveFormConfig(config) {
  return request({
    url: 'private/view/saveForm',
    method: 'post',
    data: config
  })
}

export function updateFormConfig(id, config) {
  return request({
    url: `private/view/updateForm/${id}`,
    method: 'put',
    data: config
  })
}

export function deleteFormConfig(id) {
  return request({
    url: `private/view/form/${id}`,
    method: 'delete'
  })
}

export function listForms(pageNum, pageSize) {
  return request({
    url: '/private/view/listForms',
    method: 'get',
    params: { pageNum, pageSize }
  })
}

export function getFormById(id) {
  return request({
    url: `/private/view/form/${id}`,
    method: 'get'
  })
}

/**
 * 列出可生成默认表单配置的 CRUD 控制器
 * @returns {Promise} 控制器选项列表响应
 */
export function listCrudControllers() {
  return request({
    url: '/private/view/form/controllers',
    method: 'get'
  })
}

/**
 * 根据 CRUD 控制器生成默认表单配置
 * @param {string} controller - 控制器类名或类级路径
 * @param {string} [scene=search] - 生成场景：search / save / edit
 * @returns {Promise} 表单配置草稿响应
 */
export function generateFormConfigFromController(controller, scene = 'search') {
  return request({
    url: '/private/view/form/generate',
    method: 'get',
    params: { controller, scene }
  })
}

export function getRoutes() {
  return request({
    url: '/private/view/routes',
    method: 'get'
  })
}

/**
 * 保存路由配置（产生新版本记录）
 * @param {string} group 路由分组名称
 * @param {Array} routeConfigs 路由配置列表
 * @param {string} description 版本说明，可为空
 * @returns {Promise}
 */
export function saveRouteConfigs(group, routeConfigs, description) {
  return request({
    url: `/private/view/routes/${group}`,
    method: 'post',
    params: description ? { description } : {},
    data: routeConfigs
  })
}

/**
 * 删除路由配置
 * @param {string} group 路由分组名称
 * @returns {Promise}
 */
export function deleteRouteConfigs(group) {
  return request({
    url: `/private/view/routes/${group}`,
    method: 'delete'
  })
}

/**
 * 获取所有路由分组
 * @returns {Promise}
 */
export function getRouteGroups() {
  return request({
    url: '/private/view/route-groups',
    method: 'get'
  })
}

/**
 * 获取指定分组的路由配置
 * @param {string} group 路由分组名称
 * @returns {Promise}
 */
export function getRouteConfigsByGroup(group) {
  return request({
    url: `/private/view/routes/${group}`,
    method: 'get'
  })
}

/**
 * 刷新路由缓存
 * @returns {Promise}
 */
export function refreshRouteCache() {
  return request({
    url: '/private/view/routes/refresh',
    method: 'post'
  })
}

/**
 * 获取指定分组的版本历史（最新版本在前）
 * @param {string} group 路由分组名称
 * @returns {Promise}
 */
export function getRouteVersions(group) {
  return request({
    url: `/private/view/routes/${group}/versions`,
    method: 'get'
  })
}

/**
 * 回滚到指定版本（复制目标版本数据并产生新版本）
 * @param {string} group 路由分组名称
 * @param {number} versionId 目标版本ID
 * @returns {Promise}
 */
export function rollbackRouteVersion(group, versionId) {
  return request({
    url: `/private/view/routes/${group}/rollback/${versionId}`,
    method: 'post'
  })
}

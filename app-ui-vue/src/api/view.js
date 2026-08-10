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

export function getRoutes() {
  return request({
    url: '/private/view/routes',
    method: 'get'
  })
}

/**
 * 保存路由配置
 * @param {string} group 路由分组名称
 * @param {Array} routeConfigs 路由配置列表
 * @returns {Promise}
 */
export function saveRouteConfigs(group, routeConfigs) {
  return request({
    url: `/private/view/routes/${group}`,
    method: 'post',
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

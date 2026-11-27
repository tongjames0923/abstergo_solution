import request from '@/utils/request'

// ==================== 版本管理 ====================

/**
 * 分页查询版本列表
 * @param {Object} data - 查询条件
 * @param {number} pageNum - 页码
 * @param {number} pageSize - 每页大小
 */
export function queryVersions(data, pageNum = 1, pageSize = 10) {
  return request({
    url: '/private/admin/app-version/query',
    method: 'post',
    params: { pageNum, pageSize },
    data
  })
}

/** 根据ID获取版本 */
export function getVersion(id) {
  return request({ url: `/private/admin/app-version/get/${id}`, method: 'get' })
}

/** 保存版本（新建或编辑） */
export function saveVersion(data) {
  return request({ url: '/private/admin/app-version/save', method: 'post', data })
}

/** 基于已有版本复制新建，服务端复制安装包文件到新 key */
export function cloneVersion(sourceId, data, copyPackages = true) {
  return request({
    url: `/private/admin/app-version/clone/${sourceId}`,
    method: 'post',
    params: { copyPackages },
    data
  })
}

/** 删除版本 */
export function deleteVersion(id) {
  return request({ url: `/private/admin/app-version/delete/${id}`, method: 'post' })
}

/** 提交审核 */
export function submitVersion(id) {
  return request({ url: `/private/admin/app-version/submit/${id}`, method: 'post' })
}

/** 发布版本 */
export function releaseVersion(id) {
  return request({ url: `/private/admin/app-version/release/${id}`, method: 'post' })
}

/** 下线版本 */
export function offlineVersion(id) {
  return request({ url: `/private/admin/app-version/offline/${id}`, method: 'post' })
}

/** 回滚版本 */
export function rollbackVersion(id) {
  return request({ url: `/private/admin/app-version/rollback/${id}`, method: 'post' })
}

/** 归档版本 */
export function archiveVersion(id) {
  return request({ url: `/private/admin/app-version/archive/${id}`, method: 'post' })
}

/** 获取版本聚合详情（版本 + 安装包列表） */
export function getVersionDetail(id) {
  return request({ url: `/private/admin/app-version/detail/${id}`, method: 'get' })
}

/** 查询最新已发布版本 */
export function getLatestReleased(appKey, platform) {
  return request({
    url: '/private/admin/app-version/latest',
    method: 'get',
    params: { appKey, platform }
  })
}

/** 查询已登记版本的应用标识清单（去重） */
export function listAppKeys() {
  return request({ url: '/private/admin/app-version/appKeys', method: 'get' })
}

// ==================== 安装包管理 ====================

/** 分页查询安装包列表 */
export function queryPackages(data, pageNum = 1, pageSize = 10) {
  return request({
    url: '/private/admin/app-package/query',
    method: 'post',
    params: { pageNum, pageSize },
    data
  })
}

/** 保存安装包 */
export function savePackage(data) {
  return request({ url: '/private/admin/app-package/save', method: 'post', data })
}

/** 删除安装包 */
export function deletePackage(id) {
  return request({ url: `/private/admin/app-package/delete/${id}`, method: 'post' })
}

/** 按版本ID查询安装包列表 */
export function listPackagesByVersion(versionId) {
  return request({ url: `/private/admin/app-package/listByVersion/${versionId}`, method: 'get' })
}

// ==================== 兼容策略管理 ====================

/** 分页查询兼容策略列表 */
export function queryPolicies(data, pageNum = 1, pageSize = 10) {
  return request({
    url: '/private/admin/app-version-policy/query',
    method: 'post',
    params: { pageNum, pageSize },
    data
  })
}

/** 保存兼容策略 */
export function savePolicy(data) {
  return request({ url: '/private/admin/app-version-policy/save', method: 'post', data })
}

/** 删除兼容策略 */
export function deletePolicy(id) {
  return request({ url: `/private/admin/app-version-policy/delete/${id}`, method: 'post' })
}

/** 启用兼容策略 */
export function enablePolicy(id) {
  return request({ url: `/private/admin/app-version-policy/enable/${id}`, method: 'post' })
}

/** 禁用兼容策略 */
export function disablePolicy(id) {
  return request({ url: `/private/admin/app-version-policy/disable/${id}`, method: 'post' })
}

/** 客户端升级检查 */
export function checkUpgrade(data) {
  return request({ url: '/private/admin/app-version-policy/check', method: 'post', data })
}

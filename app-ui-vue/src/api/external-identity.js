import request from '@/utils/request'

/**
 * 查询当前登录用户的全部外部身份绑定
 * @returns {Promise} 绑定列表（AjaxResult.data 为绑定实体数组：identityType/externalId/userId/...）
 */
export function myExternalIdentities() {
  return request({
    url: '/private/external-identity/my',
    method: 'get'
  })
}

/**
 * 自助绑定当前登录用户
 * @param {string} identityType - 外部身份类型（如 'wecom'）
 * @param {Object} credential - 平台凭证键值（如 { code: 企微授权码 }）
 * @returns {Promise} 操作结果
 */
export function selfBindExternalIdentity(identityType, credential) {
  return request({
    url: `/private/external-identity/self-bind/${identityType}`,
    method: 'post',
    data: credential
  })
}

/**
 * 分页查询绑定关系（管理端）
 * @param {Object} [params] - 查询参数
 * @param {string} [params.identityType] - 外部身份类型过滤
 * @param {string} [params.userName] - 系统登录用户名过滤
 * @param {number} [params.pageNum=1] - 页码，从 1 起
 * @param {number} [params.pageSize=10] - 每页数量
 * @returns {Promise} 分页结果（AjaxResult.data 为 { total, data }）
 */
export function pageExternalIdentities(params) {
  return request({
    url: '/private/external-identity/page',
    method: 'get',
    params
  })
}

/**
 * 手工绑定（管理端）
 * @param {Object} data - 绑定表单
 * @param {string} data.identityType - 外部身份类型（如 'wecom'）
 * @param {string} data.externalId - 外部身份标识（如企微 userid）
 * @param {string} data.userName - 系统登录用户名
 * @returns {Promise} 操作结果
 */
export function bindExternalIdentity(data) {
  return request({
    url: '/private/external-identity/bind',
    method: 'post',
    data
  })
}

/**
 * 解除绑定
 * @param {string} identityType - 外部身份类型
 * @param {number} userId - 系统用户 ID
 * @returns {Promise} 操作结果
 */
export function unbindExternalIdentity(identityType, userId) {
  return request({
    url: `/private/external-identity/unbind/${identityType}/${userId}`,
    method: 'delete'
  })
}

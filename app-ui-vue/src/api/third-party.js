import request from '@/utils/request'

/**
 * 分页查询第三方应用列表
 * @param {Object} data - 查询条件（对应 CommonThirdPartyAppCriteria）
 * @param {string}  [data.EqAppId] - 按应用标识精确匹配
 * @param {number[]}[data.proxyUserIdList] - 按代理用户ID集合匹配（IN）
 * @param {number} pageNum - 页码
 * @param {number} pageSize - 每页大小
 * @returns {Promise} 分页结果（res.data = { data, total, ... }）
 */
export function queryThirdPartyApps(data, pageNum = 1, pageSize = 10) {
  return request({
    url: '/private/admin/third-party/query',
    method: 'post',
    params: { pageNum, pageSize },
    data
  })
}

/**
 * 根据ID获取第三方应用详情
 * @param {number} id - 第三方应用ID
 * @returns {Promise} 应用详情（res.data 为 ThirdPartyAppEntity）
 */
export function getThirdPartyApp(id) {
  return request({
    url: `/private/admin/third-party/get/${id}`,
    method: 'get'
  })
}

/**
 * 新增第三方应用（后端不支持更新，带ID保存会被拒绝）
 * @param {Object} data - 应用信息
 * @param {string}  [data.appId]  - 应用标识，留空由系统生成UUID
 * @param {string} [data.appSecret] - 应用密钥（可选，留空由系统生成并哈希存储）
 * @param {string}  data.grantType - 授权类型（对应 ILoginGrantProvider 的 grantType）
 * @param {number}  data.proxyId  - 代理用户ID（执行人账号）
 * @returns {Promise} 保存后的应用信息
 */
export function saveThirdPartyApp(data) {
  return request({
    url: '/private/admin/third-party/save',
    method: 'post',
    data
  })
}

/**
 * 逻辑删除第三方应用
 * @param {number} id - 第三方应用ID
 * @returns {Promise} 操作结果
 */
export function deleteThirdPartyApp(id) {
  return request({
    url: `/private/admin/third-party/delete/${id}`,
    method: 'post'
  })
}

/**
 * 获取当前环境启用的登录授权方式清单（用于授权类型下拉）
 * @returns {Promise} LoginGrantDescriptor[]，每项含 grantType / labelKey / publicOptions
 */
export function getLoginGrants() {
  return request({
    url: '/public/user/login-grants',
    method: 'get'
  })
}

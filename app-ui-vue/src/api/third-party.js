import request from '@/utils/request'
import settings from '@/settings'
import { sha256 } from '@/utils/sha256'

/**
 * 第三方应用授权类型标识（对应后端 DefaultThirdPartyGrantProvider.GRANT_TYPE）
 */
export const THIRD_PARTY_GRANT_TYPE = 'third_party'

/**
 * 授权码生成盐值：与后端 DefaultThirdPartyGrantProvider.SALT 一致，
 * 属于与第三方约定的公开协议常量（密钥本身不下发），用于连通性测试时复现授权码算法
 */
const CODE_GENERATE_SALT = 'cad39065478d1e81fc528f9a6be211a5'

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

/**
 * 按后端约定算法生成第三方授权码
 * 原料拼接顺序：SALT:appSecret:timestamp:appId，SHA-256 哈希后转小写
 * @param {string} appId     - 应用标识
 * @param {string} appSecret - 应用密钥（创建时一次性展示的明文）
 * @param {number} timestamp - 毫秒时间戳（需落在服务端 ±3 分钟窗口内）
 * @returns {string} 64 位小写十六进制授权码
 */
export function buildThirdPartyCode(appId, appSecret, timestamp) {
  const base = `${CODE_GENERATE_SALT}:${appSecret}:${timestamp}:${appId}`
  return sha256(base).toLowerCase()
}

/**
 * 统一登录端点（免认证）：按授权类型换取访问令牌
 * 隔离型请求：不携带当前登录令牌、不走登录态统一处理，由调用方自行判定结果
 * @param {string} grantType - 授权类型标识（如 third_party）
 * @param {Object} data      - 凭证请求体（third_party 为 { appId, code, timestamp }）
 * @returns {Promise} 原始响应体（res.data 为令牌模型，含 token 字段）
 */
export function loginByGrantType(grantType, data) {
  return request({
    url: `/public/user/login/${grantType}`,
    method: 'post',
    data,
    _skipAuthHandling: true
  })
}

/**
 * 使用指定令牌获取用户信息（验证令牌可用性）
 * 隔离型请求：显式携带待测令牌，不被当前登录令牌覆盖，也不触发无感刷新/重登
 * @param {string} token - 待验证的访问令牌
 * @returns {Promise} 原始响应体（成功时 res.data 为用户信息，含 userName/nickName）
 */
export function getUserInfoByToken(token) {
  return request({
    url: '/private/user/info',
    method: 'get',
    headers: { [settings.tokenHeader]: token },
    _skipAuthHandling: true
  })
}

import { buildAuthorizeUrl } from '@/identity/authorize'
import { loginByGrant } from '@/api/user'

/**
 * 后端授权类型标识（重定向与扫码两种前端交互共用同一凭证类型）
 */
const GRANT_TYPE = 'wecom'

/**
 * 授权跳转构造器名称（见 src/identity/authorize.js）
 */
const AUTHORIZE_BUILDER = 'wecom-web-login'

/**
 * 企业微信单点登录 Provider（OAuth 重定向模式）
 *
 * - authenticate：跳转企业微信 Web 登录（扫码授权），state 携带本 Provider id
 * - handleCallback：回调 code 经统一授权端点换票，经 loginWithToken 收口
 * 默认未在 login.config.js 中启用；启用前须配置后端 tbs.wecom.* 参数与可信回调域名
 */
export default {
  id: 'wecom-sso',
  labelKey: 'login.method.wecom',
  icon: 'el-icon-chat-line-round',
  interactionMode: 'redirect',

  /**
   * 认证入口：发起企业微信 Web 登录（PC 浏览器扫码授权），state 携带本 Provider id
   * @param {Object} context - 宿主登录上下文（LoginContext）
   * @returns {Promise} 跳转发起后即 resolve（页面随后被授权页替换）
   */
  async authenticate(context) {
    const url = buildAuthorizeUrl(AUTHORIZE_BUILDER, this.id)
    if (!url) {
      context.reportError(context.t('login.wecomNotConfigured'))
      return
    }
    window.location.href = url
  },

  /**
   * 授权回调处理：state 携带本 Provider id，code 为企业微信授权码
   * 经统一授权端点（POST /public/user/login/wecom）换票后由宿主收口，
   * 完成 getInfo/updateRouter/跳转 后置链路；换票失败由 request.js 统一提示并 reject
   *
   * @param {Object} query - 回调路由查询参数（含 code/state）
   * @param {Object} context - 宿主登录上下文（LoginContext）
   * @returns {Promise<boolean>} 是否已消费回调并发起登录
   */
  async handleCallback(query, context) {
    if (query.state !== this.id || !query.code) {
      return false
    }
    const res = await loginByGrant(GRANT_TYPE, { code: query.code })
    const token = res && res.data && res.data.token
    if (!token) {
      return false
    }
    await context.loginWithToken({ token })
    return true
  }
}

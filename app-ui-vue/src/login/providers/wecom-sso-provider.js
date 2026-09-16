import loginConfig from '@/config/login.config'
import { loginByGrant } from '@/api/user'

const WECOM_WEB_LOGIN_URL = 'https://login.work.weixin.qq.com/wwlogin/sso/login'

/**
 * 后端授权类型标识（重定向与扫码两种前端交互共用同一凭证类型）
 */
const GRANT_TYPE = 'wecom'

/**
 * 构造企业微信 Web 登录（扫码授权）跳转地址
 * <p>
 * 使用企业微信 wwlogin 扫码登录地址，授权完成后携带 code/state 回跳 redirectUri；
 * 若需企微客户端内静默授权，可另行切换 open.weixin.qq.com/connect/oauth2/authorize 网页授权地址。
 * </p>
 * @param {string} state - 回传状态标识（区分登录 / 绑定等场景）
 * @param {string} [redirectUri] - 授权回调地址，缺省回退站点根路径
 * @returns {string} 授权跳转地址；未配置 corpId 时返回空字符串
 */
export function buildWecomAuthorizeUrl(state, redirectUri) {
  const options = (loginConfig.methods && loginConfig.methods['wecom-sso']) || {}
  if (!options.corpId) {
    return ''
  }
  const callbackUri = redirectUri || `${window.location.origin}/`
  const params = [
    'login_type=CorpApp',
    `appid=${encodeURIComponent(options.corpId)}`,
    `agentid=${encodeURIComponent(options.agentId || '')}`,
    `redirect_uri=${encodeURIComponent(callbackUri)}`,
    `state=${state}`
  ]
  return `${WECOM_WEB_LOGIN_URL}?${params.join('&')}`
}

/**
 * 企业微信单点登录 Provider（OAuth 重定向模式）
 *
 * - authenticate：携带 corpId/redirectUri/state 跳转企业微信授权页
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
    const url = buildWecomAuthorizeUrl(this.id)
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

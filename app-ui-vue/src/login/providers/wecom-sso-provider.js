import loginConfig from '@/config/login.config'

const WECOM_AUTHORIZE_URL = 'https://open.weixin.qq.com/connect/oauth2/authorize'

/**
 * 企业微信单点登录 Provider（接入骨架）
 *
 * 交互模式为 OAuth 重定向回调：
 * - authenticate：携带 corpId/redirectUri/state 跳转企业微信授权页
 * - handleCallback：接收回调 code，待后端换票接口就绪后补全换票逻辑
 * 默认未在 login.config.js 中启用；启用前须在后端登记换票接口并配置可信回调域名
 */
export default {
  id: 'wecom-sso',
  labelKey: 'login.method.wecom',
  icon: 'el-icon-chat-line-round',
  interactionMode: 'redirect',

  /**
   * 认证入口：发起企业微信 OAuth 授权跳转
   * @param {Object} context - 宿主登录上下文（LoginContext）
   * @returns {Promise} 跳转发起后即 resolve（页面随后被授权页替换）
   */
  async authenticate(context) {
    const options = (loginConfig.methods && loginConfig.methods[this.id]) || {}
    if (!options.corpId) {
      context.reportError(context.t('login.wecomNotConfigured'))
      return
    }
    const redirectUri = options.redirectUri || `${window.location.origin}/login`
    const params = [
      `appid=${encodeURIComponent(options.corpId)}`,
      `redirect_uri=${encodeURIComponent(redirectUri)}`,
      'response_type=code',
      'scope=snsapi_base',
      `state=${this.id}`
    ]
    window.location.href = `${WECOM_AUTHORIZE_URL}?${params.join('&')}#wechat_redirect`
  },

  /**
   * 授权回调处理：state 携带本 Provider id，code 为企业微信授权码
   *
   * TODO 后端换票接口（建议 /public/user/login/wecom，入参 code）就绪后补全：
   *   经 request.js 调用换票接口取得 token 后，调用 context.loginWithToken({ token })
   *   并返回 true，由宿主统一完成 getInfo/updateRouter/跳转 后置链路
   * 当前骨架仅识别回调但不发起登录，返回 false 交还宿主正常渲染登录表单
   *
   * @param {Object} query - 回调路由查询参数（含 code/state）
   * @param {Object} context - 宿主登录上下文（LoginContext）
   * @returns {Promise<boolean>} 是否已消费回调并发起登录
   */
  async handleCallback(query, context) {
    if (query.state !== this.id || !query.code) {
      return false
    }
    return false
  }
}

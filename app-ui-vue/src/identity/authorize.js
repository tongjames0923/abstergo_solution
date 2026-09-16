/**
 * 外部身份授权跳转地址构造器注册表
 * <p>
 * 平台授权跳转的统一收敛点：各平台注册具名构造器（如 wecom-web-login），
 * 登录 Provider 与绑定控件均按名称取用，新增平台只需注册新构造器。
 * </p>
 */
import loginConfig from '@/config/login.config'

const WECOM_WEB_LOGIN_URL = 'https://login.work.weixin.qq.com/wwlogin/sso/login'

const builders = new Map()

/**
 * 注册授权跳转地址构造器
 * @param {string} name - 构造器名称（供配置引用）
 * @param {Function} builder - (state, redirectUri) => 授权跳转地址
 */
export function registerAuthorizeBuilder(name, builder) {
  builders.set(name, builder)
}

/**
 * 按构造器名称生成授权跳转地址
 * @param {string} builderName - 构造器名称
 * @param {string} state - 回传状态标识（区分登录 / 绑定等场景）
 * @param {string} [redirectUri] - 授权回调地址
 * @returns {string} 授权跳转地址；未注册构造器或平台未配置时返回空字符串
 */
export function buildAuthorizeUrl(builderName, state, redirectUri) {
  const builder = builders.get(builderName)
  if (!builder) {
    return ''
  }
  return builder(state, redirectUri)
}

registerAuthorizeBuilder('wecom-web-login', (state, redirectUri) => {
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
})

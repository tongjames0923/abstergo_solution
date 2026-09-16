import loginConfig from '@/config/login.config'

/**
 * 登录方式 Provider 注册表
 *
 * Provider 契约（LoginMethodProvider）：
 * - id: string                              唯一标识，配置与回调分发均以此为准
 * - labelKey: string                        展示文案 i18n key（禁止硬编码文案）
 * - icon?: string                           Element UI icon class，用于其他登录方式入口
 * - interactionMode: string                 交互模式：'form-fields' | 'redirect' | 'popup' | 'embedded-qr'
 * - formSchema?: Array<Field>               form-fields 模式的字段声明
 *     Field: { key, type, prefixIcon?, placeholderKey, tabindex?, toggleable?, submitOnEnter? }
 * - formOptions?: Object                    form-fields 模式附加项：
 *     { rememberMe?: boolean, submitLabelKey?: string, submittingLabelKey?: string }
 * - component?: Object                      popup / embedded-qr 模式的自渲染 Vue 组件（可选）
 * - authenticate(context, values?): Promise 认证入口：表单提交 / 入口按钮点击统一调用
 * - handleCallback?(query, context): Promise<boolean> 授权回调处理，返回 true 表示已消费并发起登录
 * - onMounted?(context): void               生命周期钩子（预留）
 *
 * 宿主注入的 LoginContext：
 * - t(key): string                          i18n 翻译
 * - setSubmitting(boolean): void            控制宿主 loading 态
 * - reportError(message): void              统一错误提示
 * - loginWithCredentials(credentials): Promise  账号密码收口（复用 user/login 既有链路）
 * - loginWithToken({token, rememberMe}): Promise SSO Token 收口（复用 loginByToken 既有链路）
 */
const providers = new Map()

/**
 * 注册登录方式 Provider
 * @param {Object} provider - 实现 LoginMethodProvider 契约的登录方式
 */
export function registerProvider(provider) {
  if (!provider || !provider.id) {
    return
  }
  if (!provider.interactionMode || typeof provider.authenticate !== 'function') {
    return
  }
  providers.set(provider.id, provider)
}

/**
 * 按 id 获取 Provider
 * @param {string} id - 登录方式唯一标识
 * @returns {Object|null} 对应 Provider，未注册时返回 null
 */
export function getProviderById(id) {
  return providers.get(id) || null
}

/**
 * 获取配置启用的 Provider 列表
 * 按配置顺序返回；未注册或契约不完整的方式静默跳过（降级不阻塞登录页）
 * @returns {Array<Object>} 启用的 Provider 列表
 */
export function getEnabledProviders() {
  return (loginConfig.enabledMethods || [])
    .map(id => providers.get(id))
    .filter(Boolean)
}

/**
 * 获取主登录方式：第一个 form-fields 模式的 Provider
 * @returns {Object|null} 主 Provider，配置缺失时返回 null
 */
export function getPrimaryProvider() {
  return getEnabledProviders().find(p => p.interactionMode === 'form-fields') || null
}

/**
 * 获取其余登录方式：渲染为「其他登录方式」入口
 * @returns {Array<Object>} 除主方式外的启用 Provider 列表
 */
export function getSecondaryProviders() {
  const primary = getPrimaryProvider()
  return getEnabledProviders().filter(p => p !== primary)
}

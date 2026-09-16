/**
 * 账号密码登录 Provider（默认登录方式）
 *
 * 由 views/login/index.vue 的既有表单逻辑迁移而来，行为与现状保持一致：
 * - 字段级校验规则在现有登录页中处于停用状态，此处同样不启用（loginRules 为空）
 * - 认证统一经 LoginContext.loginWithCredentials 收口，复用 user/login 既有链路
 */
export default {
  id: 'password',
  labelKey: 'login.method.password',
  icon: 'el-icon-user',
  interactionMode: 'form-fields',
  formSchema: [
    {
      key: 'username',
      type: 'text',
      prefixIcon: 'el-icon-user',
      placeholderKey: 'login.username',
      tabindex: 1
    },
    {
      key: 'password',
      type: 'password',
      prefixIcon: 'el-icon-lock',
      placeholderKey: 'login.password',
      tabindex: 2,
      toggleable: true,
      submitOnEnter: true
    }
  ],
  formOptions: {
    rememberMe: true,
    submitLabelKey: 'login.login',
    submittingLabelKey: 'login.loggingIn'
  },

  /**
   * 认证入口：提交账号密码凭据
   * @param {Object} context - 宿主登录上下文（LoginContext）
   * @param {Object} values - 宿主收集的表单值（含 username/password/rememberMe）
   * @returns {Promise} 登录完成（含后置链路）后 resolve
   */
  async authenticate(context, values) {
    return context.loginWithCredentials({
      username: values.username,
      password: values.password,
      rememberMe: values.rememberMe
    })
  }
}

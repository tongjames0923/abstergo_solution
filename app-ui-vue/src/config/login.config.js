/**
 * 登录方式静态配置
 *
 * enabledMethods：启用的登录方式 id 列表（按渲染顺序声明）
 * 第一个 form-fields 模式的方式作为主登录区，其余方式渲染为「其他登录方式」入口
 * 新增登录方式：在 src/login/providers/ 实现 Provider 契约，于 src/login/index.js 注册，并在此启用
 */
export default {
  enabledMethods: [
    'password',
    'wecom-sso',
    'wecom-qr'
  ],
  methods: {
    /**
     * 企业微信开放平台参数（OAuth 重定向模式）
     * corpId/agentId 为公开参数；secret 一律由后端持有，禁止放前端
     * redirectUri 须与企业微信管理后台配置的可信回调域名一致
     */
    'wecom-sso': {
      corpId: '',
      agentId: '',
      redirectUri: ''
    },
    /**
     * 企业微信内嵌扫码登录参数（embedded-qr 模式）
     */
    'wecom-qr': {
      corpId: '',
      agentId: ''
    }
  }
}

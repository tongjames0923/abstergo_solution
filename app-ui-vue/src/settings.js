module.exports = {
  title: '小童快速开发模板',

  userAddFormKey: '2',
  userSearchFormKey: '1',
  userEditFormKey: '3',
  companySearchFormKey: '4',
  companyAddFormKey: '5',
  companyEditFormKey: '5',

  /**
   * @type {boolean} true | false
   * @description Whether fix the header
   */
  fixedHeader: false,

  /**
   * @type {boolean} true | false
   * @description Whether show the logo in sidebar
   */
  sidebarLogo: false,
  /**
   * @type {string}
   * 后台接口地址
   */
  backend: 'http://localhost:8080',
  /**
   * @type {string}
   * token header
   */
  tokenHeader: 'Authorization',

  /**
   * @type {array}
   * 未登录的错误码
   */
  noLoginCode: [405],

  /**
   * @type {array}
   * 刷新token的错误码
   */
  refreshTokenCode: [468],

  /**
   * @type {number}
   * 成功的code
   */
  successCode: 200,

  /**
   * @type {array}
   * 调试信息
   */
  debugCodes: [601],

  /**
   * @type {array}
   * 通知信息
   */
  noticeCodes: [701],

  /**
   * @type {string}
   * token存在cookie中的 key
   */
  tokenCookieKey: 'SYSTEM_TOKEN'
}

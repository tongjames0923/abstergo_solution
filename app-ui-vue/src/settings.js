module.exports = {
  title: 'Vue Admin Template',

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
   * @type {number}
   * 成功的code
   */
  successCode: 200
}

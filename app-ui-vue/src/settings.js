module.exports = {
  title: '小童快速开发模板',

  userAddFormKey: '2',
  userSearchFormKey: '1',
  userEditFormKey: '3',
  roleSearchFormKey: '6',
  roleAddFormKey: '12',
  roleEditFormKey: '13',
  roleGroupSearchFormKey: '14',
  roleGroupAddFormKey: '15',
  roleGroupEditFormKey: '15',

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
  backend: 'http://localhost:8083',
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
  tokenCookieKey: 'SYSTEM_TOKEN',

  /**
   * 推送 SSE 连接与重连配置（覆盖 push 模块 PUSH_DEFAULTS 缺省值，按环境自行调整）
   * 重连间隔 = min(reconnectBaseDelay * 2^已重连次数, reconnectMaxDelay) + [0, reconnectJitter) 随机抖动
   * 连续自动重连达到 reconnectMaxAttempts 次仍未成功，则停止自动重连并醒目提示，由用户手动重连
   */
  push: {
    /**
     * 最大自动重连次数（非正数表示不限次）
     */
    reconnectMaxAttempts: 3,
    /**
     * 重连退避基数（毫秒），首次重连等待时长，适当拉大避免频繁无效重试
     */
    reconnectBaseDelay: 30000,
    /**
     * 重连退避上限（毫秒），多次失败后单次重连间隔封顶
     */
    reconnectMaxDelay: 60000,
    /**
     * 重连退避随机抖动上限（毫秒），防多端同时重连惊群
     */
    reconnectJitter: 1000,
    /**
     * 心跳上行周期兜底默认值（毫秒）：仅用于 hello 握手到达前的引导；
     * 稳态心跳周期与最迟期限由后端经 IHeartbeatRecommender 生成随 hello 下发并覆盖本值
     */
    heartbeatInterval: 30000,
    /**
     * 心跳连续失败阈值：达到即判定会话已死，主动关流换票重连
     */
    heartbeatMaxFailures: 3
  }
}

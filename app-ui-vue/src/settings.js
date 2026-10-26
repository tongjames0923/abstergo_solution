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
     * 心跳上行周期（毫秒）：服务端对每个 PING 会在 SSE 下行流回一个 PONG，
     * 故本值同时决定下行流的字节间隔——须足够小以撑过 dev 代理与 nginx 的空闲读超时
     * （过长会被中间层掐断静默流导致周期性重连），同时远小于后端 heartbeat-timeout-millis(600s)
     */
    heartbeatInterval: 30000,
    /**
     * 心跳连续失败阈值：达到即判定会话已死，主动关流换票重连
     */
    heartbeatMaxFailures: 3
  }
}

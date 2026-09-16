import WecomQrPanel from './wecom-qr-panel'

/**
 * 企业微信扫码登录 Provider（接入骨架）
 *
 * 交互模式为内嵌二维码（embedded-qr）：
 * - 宿主点击入口后，将卡片区域切换渲染本 Provider 的 component（WecomQrPanel）
 * - 面板内接入企微 wwLogin SDK 展示二维码，扫码确认回调获得 code
 * - 换票后经 context.loginWithToken 收口，复用统一后置链路
 * 默认未在 login.config.js 中启用；启用前须后端换票接口就绪并配置企微登录参数
 */
export default {
  id: 'wecom-qr',
  labelKey: 'login.method.qr',
  icon: 'el-icon-mobile-phone',
  interactionMode: 'embedded-qr',
  component: WecomQrPanel,

  /**
   * 认证入口：内嵌模式由宿主渲染 component 承载交互，此入口仅为契约完整性兜底
   * @param {Object} context - 宿主登录上下文（LoginContext）
   * @returns {Promise} 兜底提示后即 resolve
   */
  async authenticate(context) {
    context.reportError(context.t('login.qr.placeholder'))
  }
}

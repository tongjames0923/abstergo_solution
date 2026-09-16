import { registerProvider } from './registry'
import passwordProvider from './providers/password-provider'
import wecomSsoProvider from './providers/wecom-sso-provider'
import wecomQrProvider from './providers/wecom-qr-provider'

/**
 * 登录能力模块入口：注册内置登录方式
 * 新增登录方式：新增 provider 文件后在此补一行注册，并在 login.config.js 中启用
 */
registerProvider(passwordProvider)
registerProvider(wecomSsoProvider)
registerProvider(wecomQrProvider)

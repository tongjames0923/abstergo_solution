<template>
  <div class="login-container">
    <!-- 背景氛围层：Apple 风格流动极光 -->
    <div class="bg-aurora" aria-hidden="true">
      <div class="aurora-blob aurora-blob--blue" />
      <div class="aurora-blob aurora-blob--indigo" />
      <div class="aurora-blob aurora-blob--cyan" />
    </div>

    <!-- 语言切换：固定在页面右上角 -->
    <div class="page-lang-switch">
      <lang-select />
    </div>

    <div class="login-card" :class="{ 'is-visible': isLoaded }">
      <!-- macOS 风格应用图标 -->
      <div class="app-icon anim-stagger" style="--stagger-delay: 0.05s">
        <i class="el-icon-office-building" />
      </div>

      <div class="login-header">
        <h2 class="login-title anim-stagger" style="--stagger-delay: 0.12s">{{ systemTitle }}</h2>
        <p class="login-subtitle anim-stagger" style="--stagger-delay: 0.18s">{{ $t('login.title') }}</p>
      </div>

      <!-- SSO 授权处理态：回调换票期间替换表单区，避免表单闪现造成割裂 -->
      <div v-if="ssoProcessing" class="sso-processing anim-stagger" style="--stagger-delay: 0.24s">
        <span class="sso-spinner" />
        <p class="sso-text">{{ ssoProcessingText }}</p>
        <p class="sso-sub">{{ ssoProcessingSubText }}</p>
      </div>

      <!-- 内嵌登录面板：embedded-qr 等自渲染模式由 Provider component 承载 -->
      <div v-else-if="panelComponent && loginContext" class="login-embed-area anim-stagger" style="--stagger-delay: 0.24s">
        <component :is="panelComponent" :context="loginContext" />
        <el-button type="text" class="embed-back" @click="closeProviderPanel">{{ $t('login.backToLogin') }}</el-button>
      </div>

      <el-form
        v-else-if="primaryProvider"
        ref="loginForm"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        auto-complete="on"
        label-position="left"
      >
        <!-- 主登录方式字段区：由 Provider formSchema 驱动渲染 -->
        <el-form-item
          v-for="(field, index) in primarySchema"
          :key="field.key"
          :prop="field.key"
          class="anim-stagger"
          :style="{ '--stagger-delay': (0.24 + index * 0.06) + 's' }"
        >
          <el-input
            :ref="field.key"
            :key="field.toggleable ? passwordType : field.key"
            v-model="loginForm[field.key]"
            :type="field.toggleable ? passwordType : field.type"
            :placeholder="$t(field.placeholderKey)"
            :name="field.key"
            :tabindex="field.tabindex"
            auto-complete="on"
            :prefix-icon="field.prefixIcon"
            size="medium"
            class="login-input"
            @keyup.enter.native="handleFieldEnter(field)"
            @focus="handleInputFocus"
            @blur="handleInputBlur"
          />
          <span v-if="field.toggleable" class="show-pwd" @click="showPwd">
            <i :class="passwordType === 'password' ? 'el-icon-view' : 'el-icon-hide'" />
          </span>
        </el-form-item>

        <div v-if="primaryOptions.rememberMe" class="login-options anim-stagger" style="--stagger-delay: 0.36s">
          <el-checkbox v-model="loginForm.rememberMe">{{ $t('login.rememberMe') || '记住我' }}</el-checkbox>
        </div>

        <el-button
          :loading="submitting"
          type="primary"
          class="login-button anim-stagger"
          style="--stagger-delay: 0.42s"
          @click.native.prevent="handleLogin"
        >
          {{
            submitting ? $t(primaryOptions.submittingLabelKey || 'login.loggingIn') : $t(primaryOptions.submitLabelKey || 'login.login')
          }}
        </el-button>

        <!-- 其他登录方式入口：由注册表按配置渲染 -->
        <div v-if="secondaryProviders.length" class="login-alt-methods anim-stagger" style="--stagger-delay: 0.46s">
          <div class="alt-divider">
            <span>{{ $t('login.otherMethods') }}</span>
          </div>
          <div class="alt-buttons">
            <button
              v-for="method in secondaryProviders"
              :key="method.id"
              type="button"
              class="alt-button"
              :title="$t(method.labelKey)"
              @click="startProvider(method)"
            >
              <i :class="method.icon"/>
              <span>{{ $t(method.labelKey) }}</span>
            </button>
          </div>
        </div>

        <div class="secure-hint anim-stagger" style="--stagger-delay: 0.48s">
          <i class="el-icon-lock" />
          <span>{{ $t('login.secureHint') }}</span>
        </div>

        <div class="login-footer anim-stagger" style="--stagger-delay: 0.54s">
          <p>© 2025 {{ systemTitle }}. {{ $t('login.copyright') || '保留所有权利.' }}</p>
          <p v-if="gatewayVersion" class="footer-version">{{ $t('login.version') }} {{ gatewayVersion }}</p>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script>
import {getRememberMe} from '@/utils/auth'
import settings from '@/settings'
import {updateRouter} from '@/router'
import LangSelect from '@/components/LangSelect'
import {getGatewayVersion} from '@/api/system'
import {getPrimaryProvider, getSecondaryProviders, getProviderById} from '@/login/registry'
import {dispatchLoginCallback, readCallbackQuery} from '@/login/callback'
// 引入登录能力模块入口，确保内置 Provider 注册副作用执行
import '@/login'

export default {
  name: 'Login',
  components: {
    LangSelect
  },
  data() {
    return {
      isLoaded: false,
      loginForm: {
        rememberMe: false
      },
      loginRules: {},
      submitting: false,
      // SSO 授权回调处理中：替换表单区的过渡态
      ssoProcessing: false,
      ssoProcessingText: '',
      ssoProcessingSubText: '',
      passwordType: 'password',
      // 当前内嵌面板 Provider（embedded-qr 等自渲染模式）
      panelProvider: null,
      redirect: undefined,
      // 后端网关版本号（接口无需认证，登录前即可展示）
      gatewayVersion: ''
    }
  },
  computed: {
    systemTitle() {
      return settings.title || '企业管理平台'
    },
    // 主登录方式：注册表中第一个 form-fields 模式的 Provider
    primaryProvider() {
      return getPrimaryProvider()
    },
    // 其他登录方式：渲染为补充入口
    secondaryProviders() {
      return getSecondaryProviders()
    },
    primarySchema() {
      return (this.primaryProvider && this.primaryProvider.formSchema) || []
    },
    primaryOptions() {
      return (this.primaryProvider && this.primaryProvider.formOptions) || {}
    },
    // 内嵌面板组件：声明了 component 的 Provider（embedded-qr 模式）
    panelComponent() {
      return (this.panelProvider && this.panelProvider.component) || null
    }
  },
  watch: {
    $route: {
      handler: function(route) {
        this.redirect = route.query && route.query.redirect
      },
      immediate: true
    }
  },
  created() {
    this.initLoginForm()
    // 首帧前判定 SSO 回调，命中则直接进入授权处理态，表单不闪现
    this.ssoProcessing = this.detectSsoCallback()
  },
  mounted() {
    this.loginForm.rememberMe = getRememberMe()
    this.loadGatewayVersion()
    this.loginContext = this.createLoginContext()
    this.handleLoginCallback()
    // 添加淡入动画
    this.$nextTick(() => {
      setTimeout(() => {
        this.isLoaded = true
      }, 100)
    })
  },
  methods: {
    // 按主登录方式的 formSchema 初始化表单值
    initLoginForm() {
      const form = {}
      this.primarySchema.forEach(field => {
        form[field.key] = ''
      })
      form.rememberMe = false
      this.loginForm = form
    },
    // 构造注入给 Provider 的宿主上下文（LoginContext）
    createLoginContext() {
      const self = this
      return {
        t: key => self.$t(key),
        setSubmitting: value => {
          self.submitting = value
        },
        reportError: message => {
          // 授权跳转前的前置失败（如未配置 corpId）需退出处理态，交还表单
          self.ssoProcessing = false
          self.$message.error(message)
        },
        loginWithCredentials: credentials => self.doLoginWithCredentials(credentials),
        loginWithToken: tokenInfo => self.doLoginWithToken(tokenInfo)
      }
    },
    // SSO 授权回调分发入口：state 携带 Provider id，code 为授权码
    async handleLoginCallback() {
      if (!this.ssoProcessing) {
        return
      }
      try {
        const handled = await dispatchLoginCallback(this.$route.query, this.loginContext)
        // 成功时保持处理态直至路由跳转完成；未消费或失败回落表单
        if (!handled) {
          this.ssoProcessing = false
        }
      } catch (e) {
        // 授权失败时回落登录表单，错误提示由 request.js 统一展示
        this.ssoProcessing = false
      }
    },
    // 判定当前是否为 SSO 授权回调（code + 已注册 Provider）
    detectSsoCallback() {
      const query = readCallbackQuery(this.$route.query)
      if (!query.code) {
        return false
      }
      const providerId = query.state || query.loginMethod
      const provider = providerId ? getProviderById(providerId) : null
      const hit = !!(provider && typeof provider.handleCallback === 'function')
      if (hit) {
        this.ssoProcessingText = this.$t('login.ssoProcessing')
        this.ssoProcessingSubText = this.$t('login.ssoProcessingSub')
      }
      return hit
    },
    // 拉取后端网关版本信息，失败时静默降级不影响登录页
    async loadGatewayVersion() {
      try {
        const res = await getGatewayVersion()
        if (res && res.data && res.data.version && res.data.version !== 'unknown') {
          this.gatewayVersion = res.data.version
        }
      } catch (e) {
        // 忽略：后端不可用时不展示版本信息
      }
    },
    handleInputFocus(event) {
      event.target.closest('.el-form-item').classList.add('input-focused')
    },
    handleInputBlur(event) {
      event.target.closest('.el-form-item').classList.remove('input-focused')
    },
    handleFieldEnter(field) {
      if (field.submitOnEnter) {
        this.handleLogin()
      }
    },
    showPwd() {
      if (this.passwordType === 'password') {
        this.passwordType = 'text'
      } else {
        this.passwordType = 'password'
      }
      this.$nextTick(() => {
        const ref = this.$refs.password
        const input = Array.isArray(ref) ? ref[0] : ref
        if (input) {
          input.focus()
        }
      })
    },
    handleLogin() {
      if (!this.primaryProvider) {
        return
      }
      this.$refs.loginForm.validate(valid => {
        if (valid) {
          this.primaryProvider.authenticate(this.loginContext, this.loginForm).catch(() => {
          })
        }
      })
    },
    // 发起其他登录方式：声明 component 的走内嵌面板，redirect 模式先进入跳转处理态
    startProvider(provider) {
      if (provider.component) {
        this.panelProvider = provider
        return
      }
      if (provider.interactionMode === 'redirect') {
        this.ssoProcessingText = this.$t('login.ssoRedirecting')
        this.ssoProcessingSubText = this.$t('login.ssoRedirectingSub')
        this.ssoProcessing = true
      }
      provider.authenticate(this.loginContext).catch(() => {
        this.ssoProcessing = false
      })
    },
    // 关闭内嵌面板，交还账号密码表单
    closeProviderPanel() {
      this.panelProvider = null
    },
    // 账号密码登录收口：复用既有 user/login 链路
    async doLoginWithCredentials(credentials) {
      this.submitting = true
      try {
        await this.$store.dispatch('user/login', credentials)
        await this.finalizeAfterLogin()
      } finally {
        this.submitting = false
      }
    },
    // SSO Token 收口：复用 loginByToken 链路，与账号密码登录共用后置逻辑
    async doLoginWithToken(tokenInfo) {
      this.submitting = true
      try {
        await this.$store.dispatch('user/loginByToken', tokenInfo)
        await this.finalizeAfterLogin()
      } finally {
        this.submitting = false
      }
    },
    // 登录成功后的统一后置链路：获取用户信息 → 更新路由 → 跳转
    async finalizeAfterLogin() {
      // 等待一小段时间确保token已经设置到请求拦截器中
      await new Promise(resolve => setTimeout(resolve, 100))

      try {
        // 先获取用户信息，再更新路由
        await this.$store.dispatch('user/getInfo')
        // 登录成功后更新路由数据
        await updateRouter()
        // 确保路由更新完成后才跳转
        await this.$nextTick()
        // 登录后始终跳转到dashboard
        this.$router.push({path: '/Welcome'})
      } catch (error) {
        console.error('路由更新失败:', error)
        // 即使路由更新失败，也允许跳转到dashboard
        this.$router.push({path: '/Welcome'})
      }
    }
  }
}
</script>

<style lang="scss" scoped>
/* ===== Apple 风格登录页：浅灰底 + 流动极光 + 白色卡片 + 蓝色强调 ===== */
.login-container {
  min-height: 100vh;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  box-sizing: border-box;
  overflow: hidden;
  position: relative;
  background-color: #f5f5f7;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Text', 'PingFang SC', 'Helvetica Neue', sans-serif;
}

/* 流动极光背景：大尺寸模糊光斑缓慢漂移，营造 macOS 壁纸氛围 */
.bg-aurora {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
  z-index: 0;

  /* 顶部提亮，避免光斑让整体发灰 */
  &::after {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(180deg, rgba(255, 255, 255, 0.5) 0%, rgba(255, 255, 255, 0) 38%);
  }
}

.aurora-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(90px);
  opacity: 0.55;
  will-change: transform;
}

.aurora-blob--blue {
  width: 52vw;
  height: 52vw;
  min-width: 480px;
  min-height: 480px;
  left: -12vw;
  top: -18vh;
  background: radial-gradient(circle at 30% 30%, rgba(0, 113, 227, 0.5), rgba(0, 113, 227, 0) 65%);
  animation: aurora-drift-a 18s ease-in-out infinite;
}

.aurora-blob--indigo {
  width: 46vw;
  height: 46vw;
  min-width: 420px;
  min-height: 420px;
  right: -14vw;
  bottom: -20vh;
  background: radial-gradient(circle at 60% 40%, rgba(94, 92, 230, 0.42), rgba(94, 92, 230, 0) 65%);
  animation: aurora-drift-b 23s ease-in-out infinite;
}

.aurora-blob--cyan {
  width: 34vw;
  height: 34vw;
  min-width: 320px;
  min-height: 320px;
  right: 12vw;
  top: -12vh;
  background: radial-gradient(circle at 50% 50%, rgba(100, 210, 255, 0.35), rgba(100, 210, 255, 0) 65%);
  animation: aurora-drift-c 15s ease-in-out infinite;
}

/* 多关键帧环绕流动：位移 + 旋转（让偏心光斑的亮核绕行）+ 呼吸缩放 + 透明度脉动 */
@keyframes aurora-drift-a {
  0% {
    transform: translate(0, 0) rotate(0deg) scale(1);
    opacity: 0.55;
  }
  33% {
    transform: translate(12vw, 9vh) rotate(50deg) scale(1.18);
    opacity: 0.7;
  }
  66% {
    transform: translate(5vw, 16vh) rotate(110deg) scale(0.96);
    opacity: 0.5;
  }
  100% {
    transform: translate(0, 0) rotate(0deg) scale(1);
    opacity: 0.55;
  }
}

@keyframes aurora-drift-b {
  0% {
    transform: translate(0, 0) rotate(0deg) scale(1.05);
    opacity: 0.55;
  }
  40% {
    transform: translate(-10vw, -9vh) rotate(-60deg) scale(0.9);
    opacity: 0.68;
  }
  70% {
    transform: translate(-16vw, -3vh) rotate(-130deg) scale(1.15);
    opacity: 0.48;
  }
  100% {
    transform: translate(0, 0) rotate(0deg) scale(1.05);
    opacity: 0.55;
  }
}

@keyframes aurora-drift-c {
  0% {
    transform: translate(0, 0) rotate(0deg) scale(0.95);
    opacity: 0.55;
  }
  50% {
    transform: translate(-14vw, 10vh) rotate(90deg) scale(1.2);
    opacity: 0.72;
  }
  100% {
    transform: translate(0, 0) rotate(0deg) scale(0.95);
    opacity: 0.55;
  }
}

/* 语言切换：右上角胶囊按钮 */
.page-lang-switch {
  position: fixed;
  top: 20px;
  right: 24px;
  z-index: 10;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border: 1px solid rgba(0, 0, 0, 0.06);
  transition: all 0.25s ease;

  &:hover {
    background: #fff;
    box-shadow: 0 2px 10px rgba(0, 0, 0, 0.08);
  }

  ::v-deep i {
    color: #6e6e73;
    font-size: 16px !important;
  }
}

/* 登录卡片 */
.login-card {
  position: relative;
  z-index: 1;
  width: 396px;
  max-width: 100%;
  /* 半透明白 + 毛玻璃，透出背景极光色彩 */
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: blur(24px) saturate(180%);
  -webkit-backdrop-filter: blur(24px) saturate(180%);
  border-radius: 20px;
  border: 1px solid rgba(0, 0, 0, 0.06);
  box-shadow: 0 12px 40px rgba(0, 0, 0, 0.08), 0 2px 8px rgba(0, 0, 0, 0.04);
  padding: 36px 36px 20px;
  box-sizing: border-box;
  opacity: 0;
  transform: translateY(12px) scale(0.985);
  transition: opacity 0.5s ease-out, transform 0.5s ease-out;

  &.is-visible {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

/* 应用图标：macOS squircle 风格 */
.app-icon {
  width: 60px;
  height: 60px;
  margin: 0 auto 16px;
  border-radius: 15px;
  background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6px 16px rgba(29, 78, 216, 0.3), inset 0 1px 0 rgba(255, 255, 255, 0.3);

  i {
    font-size: 30px;
    color: #fff;
  }
}

.login-header {
  margin-bottom: 24px;
  text-align: center;

  .login-title {
    font-size: 22px;
    font-weight: 700;
    letter-spacing: -0.02em;
    color: #1d1d1f;
    margin: 0 0 4px;
  }

  .login-subtitle {
    font-size: 13px;
    color: #86868b;
    margin: 0;
  }
}

::v-deep .el-form-item {
  margin-bottom: 18px;
  position: relative;

  .el-form-item__error {
    padding-top: 2px;
    font-size: 12px;
  }
}

/* 填充式输入框：灰底无边框，聚焦时白底 + 蓝色描边光环 */
.login-input {
  ::v-deep .el-input__inner {
    height: 46px;
    line-height: 46px;
    padding-left: 42px;
    border-radius: 12px;
    border: 1px solid transparent;
    background-color: #f5f5f7;
    font-size: 14px;
    color: #1d1d1f;
    transition: background-color 0.25s ease, border-color 0.25s ease, box-shadow 0.25s ease;

    &::placeholder {
      color: #9a9aa0;
    }

    &:hover {
      background-color: #f0f0f2;
    }

    &:focus {
      background-color: #fff;
      border-color: #0071e3;
      box-shadow: 0 0 0 4px rgba(0, 113, 227, 0.12);
    }
  }

  ::v-deep .el-input__prefix {
    left: 14px;
    color: #86868b;
    transition: color 0.25s ease;
  }

  ::v-deep .el-input__icon {
    line-height: 46px;
    font-size: 16px;
  }
}

::v-deep .el-form-item.is-focus .login-input .el-input__prefix,
::v-deep .el-form-item.input-focused .login-input .el-input__prefix {
  color: #0071e3;
}

.show-pwd {
  position: absolute;
  right: 14px;
  top: 23px;
  transform: translateY(-50%);
  cursor: pointer;
  color: #9a9aa0;
  font-size: 16px;
  line-height: 1;
  transition: color 0.2s ease;

  &:hover {
    color: #0071e3;
  }
}

.login-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 0 0 18px;

  ::v-deep .el-checkbox__label {
    color: #515154;
    font-size: 13px;
  }

  ::v-deep .el-checkbox__inner {
    border-radius: 5px;
    border-color: #c7c7cc;
  }

  ::v-deep .el-checkbox__input.is-checked .el-checkbox__inner {
    background-color: #0071e3;
    border-color: #0071e3;
  }

  ::v-deep .el-checkbox__input.is-focus .el-checkbox__inner {
    border-color: #0071e3;
  }
}

/* 主按钮：Apple 蓝 */
.login-button {
  width: 100%;
  height: 46px;
  font-size: 15px;
  font-weight: 500;
  letter-spacing: 0.02em;
  border-radius: 12px;
  background-color: #0071e3;
  border: none;
  transition: background-color 0.25s ease, box-shadow 0.25s ease, transform 0.15s ease;

  &:hover,
  &:focus {
    background-color: #0077ed;
    border: none;
    box-shadow: 0 4px 14px rgba(0, 113, 227, 0.32);
  }

  &:active {
    transform: scale(0.985);
    background-color: #006edb;
  }

  &.is-loading {
    background-color: #0071e3;
    opacity: 0.85;
  }
}

/* 其他登录方式入口：分割线 + 胶囊按钮，延续卡片细节质感 */
.login-alt-methods {
  margin-top: 14px;

  .alt-divider {
    display: flex;
    align-items: center;
    gap: 12px;
    color: #9a9aa0;
    font-size: 11px;

    &::before,
    &::after {
      content: '';
      flex: 1;
      height: 1px;
      background: #ececf0;
    }
  }

  .alt-buttons {
    display: flex;
    justify-content: center;
    flex-wrap: wrap;
    gap: 10px;
    margin-top: 12px;
  }

  .alt-button {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    height: 34px;
    padding: 0 16px;
    border-radius: 17px;
    border: 1px solid rgba(0, 0, 0, 0.08);
    background: rgba(255, 255, 255, 0.85);
    color: #515154;
    font-size: 12px;
    cursor: pointer;
    transition: all 0.25s ease;

    i {
      font-size: 14px;
      color: #86868b;
      transition: color 0.25s ease;
    }

    &:hover {
      border-color: rgba(0, 113, 227, 0.4);
      color: #0071e3;
      box-shadow: 0 2px 10px rgba(0, 113, 227, 0.12);

      i {
        color: #0071e3;
      }
    }

    &:active {
      transform: scale(0.97);
    }
  }
}

/* SSO 授权处理态：环形进度 + 说明文案，与卡片入场动画衔接 */
.sso-processing {
  padding: 42px 0 52px;
  text-align: center;

  .sso-spinner {
    display: inline-block;
    width: 34px;
    height: 34px;
    border-radius: 50%;
    border: 3px solid rgba(0, 113, 227, 0.15);
    border-top-color: #0071e3;
    animation: sso-spin 0.9s linear infinite;
  }

  .sso-text {
    margin: 18px 0 0;
    font-size: 14px;
    font-weight: 500;
    color: #1d1d1f;
  }

  .sso-sub {
    margin: 6px 0 0;
    font-size: 12px;
    color: #86868b;
  }
}

@keyframes sso-spin {
  to {
    transform: rotate(360deg);
  }
}

/* 内嵌登录面板：承载 embedded-qr 等自渲染组件 */
.login-embed-area {
  text-align: center;

  .embed-back {
    margin-top: 4px;
    padding: 0;
    font-size: 12px;
    color: #86868b;

    &:hover {
      color: #0071e3;
    }
  }
}

/* 安全提示：充实按钮下方空间，传递信任感 */
.secure-hint {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 5px;
  margin-top: 16px;
  font-size: 11px;
  color: #9a9aa0;

  i {
    font-size: 11px;
    color: #34c759;
  }
}

.login-footer {
  text-align: center;
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid #f2f2f4;

  p {
    font-size: 11px;
    color: #9a9aa0;
    margin: 0;
  }

  .footer-version {
    margin-top: 4px;
    font-size: 10px;
    color: #b8b8be;
    letter-spacing: 0.3px;
  }
}

/* 分层入场动画 */
.anim-stagger {
  opacity: 0;
  animation: rise-in 0.55s cubic-bezier(0.25, 0.1, 0.25, 1) forwards;
  animation-delay: var(--stagger-delay, 0s);
}

@keyframes rise-in {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@media (max-width: 480px) {
  .login-container {
    padding: 16px;
  }

  .login-card {
    padding: 36px 24px 24px;
  }

  .login-title {
    font-size: 20px;
  }
}

/* 横屏手机：压缩卡片与间距，避免纵向溢出 */
@media (max-height: 480px) and (orientation: landscape) {
  .login-container {
    align-items: flex-start;
    overflow-y: auto;
    padding: 12px;
  }

  .login-card {
    padding: 16px 24px;
  }

  .app-icon {
    width: 40px;
    height: 40px;
  }

  .login-header {
    margin-bottom: 12px;
  }

  .login-button {
    height: 38px;
  }
}
</style>

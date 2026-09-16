<template>
  <div class="oauth-callback">
    <div class="callback-card">
      <transition name="state-fade" mode="out-in">
        <span v-if="status === 'processing'" key="processing" class="state-spinner" />
        <i v-else-if="status === 'success'" key="success" class="el-icon-success state-icon success pop-in" />
        <i v-else key="error" class="el-icon-error state-icon error pop-in" />
      </transition>
      <transition name="state-fade" mode="out-in">
        <p :key="status" class="state-text">{{ stateText }}</p>
      </transition>
    </div>
  </div>
</template>

<script>
import { readCallbackQuery, cleanCallbackSearch } from '@/login/callback'
import { selfBindExternalIdentity } from '@/api/external-identity'

/**
 * OAuth 授权回调处理页（静态路由 /oauth/callback）
 * <p>
 * 自助绑定场景（约定 state = 「{identityType}-bind」）：以授权码调用自助绑定接口后回到绑定来源页；
 * 其他 state 交还登录链路处理（登录页回调分发器基于同一启动期参数捕获）。
 * 状态切换与登录页授权处理态共用同一套动画语言（环形进度 / 淡入过渡）。
 * </p>
 */
const SELF_BIND_STATE_SUFFIX = '-bind'

export default {
  name: 'OauthCallback',
  data() {
    return {
      status: 'processing'
    }
  },
  computed: {
    stateText() {
      if (this.status === 'success') {
        return this.$t('identity.bindSuccess')
      }
      if (this.status === 'error') {
        return this.$t('identity.bindFailed')
      }
      return this.$t('identity.processing')
    }
  },
  mounted() {
    this.handleCallback()
  },
  methods: {
    async handleCallback() {
      const query = readCallbackQuery(this.$route.query)
      cleanCallbackSearch()
      if (!query.code) {
        this.goHome()
        return
      }
      const state = query.state || ''
      if (state.endsWith(SELF_BIND_STATE_SUFFIX)) {
        const identityType = state.slice(0, -SELF_BIND_STATE_SUFFIX.length)
        await this.handleSelfBind(identityType, query.code)
        return
      }
      this.$router.replace({ path: '/login', query: { code: query.code, state: query.state } })
    },
    async handleSelfBind(identityType, code) {
      try {
        await selfBindExternalIdentity(identityType, { code })
        this.status = 'success'
        this.goToBindingPage(1200)
      } catch (e) {
        this.status = 'error'
        this.goToBindingPage(2000)
      }
    },
    goToBindingPage(delay) {
      setTimeout(() => {
        this.$router.replace('/')
      }, delay)
    },
    goHome() {
      this.$router.replace('/')
    }
  }
}
</script>

<style lang="scss" scoped>
/* 与登录页同源的动画语言：灰底 + 白卡 + Apple 蓝环形进度 */
.oauth-callback {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  box-sizing: border-box;
  background-color: #f5f5f7;
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Text', 'PingFang SC', 'Helvetica Neue', sans-serif;

  .callback-card {
    width: 320px;
    padding: 40px 24px;
    border-radius: 20px;
    background: #fff;
    border: 1px solid rgba(0, 0, 0, 0.06);
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.08), 0 2px 8px rgba(0, 0, 0, 0.04);
    text-align: center;

    .state-spinner {
      display: inline-block;
      width: 40px;
      height: 40px;
      border-radius: 50%;
      border: 3px solid rgba(0, 113, 227, 0.15);
      border-top-color: #0071e3;
      animation: sso-spin 0.9s linear infinite;
    }

    .state-icon {
      font-size: 44px;

      &.success {
        color: #34c759;
      }

      &.error {
        color: #f56c6c;
      }
    }

    .pop-in {
      animation: pop-in 0.4s cubic-bezier(0.34, 1.56, 0.64, 1) both;
    }

    .state-text {
      margin: 16px 0 0;
      font-size: 14px;
      color: #1d1d1f;
    }
  }
}

@keyframes sso-spin {
  to {
    transform: rotate(360deg);
  }
}

@keyframes pop-in {
  from {
    opacity: 0;
    transform: scale(0.6);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.state-fade-enter-active,
.state-fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.state-fade-enter {
  opacity: 0;
  transform: translateY(4px);
}

.state-fade-leave-to {
  opacity: 0;
  transform: translateY(-4px);
}
</style>

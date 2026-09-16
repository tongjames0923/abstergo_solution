<template>
  <div class="oauth-callback">
    <div class="callback-card">
      <i :class="stateIcon" />
      <p class="state-text">{{ stateText }}</p>
    </div>
  </div>
</template>

<script>
import { readCallbackQuery, cleanCallbackSearch } from '@/login/callback'
import { selfBindExternalIdentity } from '@/api/external-identity'

/**
 * OAuth 授权回调处理页（静态路由 /oauth/callback）
 * <p>
 * 当前处理自助绑定场景（state=wecom-bind）：以授权码调用自助绑定接口后回到绑定页；
 * 其他 state 交还登录链路处理（登录页回调分发器基于同一启动期参数捕获）。
 * </p>
 */
const SELF_BIND_STATE_PREFIX = 'wecom-bind'

export default {
  name: 'OauthCallback',
  data() {
    return {
      status: 'processing'
    }
  },
  computed: {
    stateIcon() {
      if (this.status === 'success') {
        return 'el-icon-success success'
      }
      if (this.status === 'error') {
        return 'el-icon-error error'
      }
      return 'el-icon-loading loading'
    },
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
      if (query.state === SELF_BIND_STATE_PREFIX) {
        await this.handleSelfBind(query.code)
        return
      }
      this.$router.replace({ path: '/login', query: { code: query.code, state: query.state } })
    },
    async handleSelfBind(code) {
      try {
        await selfBindExternalIdentity('wecom', { code })
        this.status = 'success'
        this.goToBindingPage(1200)
      } catch (e) {
        this.status = 'error'
        this.goToBindingPage(2000)
      }
    },
    goToBindingPage(delay) {
      setTimeout(() => {
        this.$router.replace('/account/external-identity')
      }, delay)
    },
    goHome() {
      this.$router.replace('/')
    }
  }
}
</script>

<style lang="scss" scoped>
.oauth-callback {
  min-height: 60vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f5f7;

  .callback-card {
    width: 320px;
    padding: 36px 24px;
    border-radius: 16px;
    background: #fff;
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.08);
    text-align: center;

    i {
      font-size: 44px;

      &.loading {
        color: #0071e3;
      }

      &.success {
        color: #34c759;
      }

      &.error {
        color: #f56c6c;
      }
    }

    .state-text {
      margin: 14px 0 0;
      font-size: 14px;
      color: #1d1d1f;
    }
  }
}
</style>

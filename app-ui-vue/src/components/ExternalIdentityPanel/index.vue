<template>
  <div class="pane ext-identity-pane">
    <h3 class="pane-hd">
      <span class="pane-ic">
        <i :class="icon" />
      </span>
      {{ $t(title) }}
      <span v-if="boundCount" class="pane-badge">{{ boundCount }}</span>
    </h3>

    <ul v-if="providers.length" class="pane-ul">
      <li v-for="provider in providers" :key="provider.identityType" class="pane-li">
        <span
          class="pane-dot"
          :style="{ background: boundOf(provider) ? '#30B879' : '#C6CBD4' }"
        />
        <div class="row-info">
          <span class="row-name">
            <i v-if="provider.icon" :class="provider.icon" class="row-ic" />
            {{ $t(provider.labelKey) }}
          </span>
          <small>{{ boundOf(provider) ? boundOf(provider).externalId : $t('identity.unbound') }}</small>
        </div>
        <el-button
          v-if="!boundOf(provider)"
          type="text"
          size="small"
          class="bind-btn"
          @click="startBind(provider)"
        >{{ bindLabel(provider) }}</el-button>
        <el-button
          v-else
          type="text"
          size="small"
          class="unbind-btn"
          @click="handleUnbind(boundOf(provider))"
        >{{ $t('identity.unbind') }}</el-button>
      </li>
    </ul>

    <p v-else class="pane-emp">
      <i class="el-icon-link" />
      {{ $t('identity.noData') }}
    </p>

    <p v-if="providers.length" class="pane-hint">
      <i class="el-icon-info" />
      {{ $t('identity.autoBindHint') }}
    </p>
  </div>
</template>

<script>
import externalIdentityConfig from '@/config/external-identity.config'
import { myExternalIdentities, unbindExternalIdentity } from '@/api/external-identity'
import { buildAuthorizeUrl } from '@/identity/authorize'

/**
 * 外部身份绑定面板（可复用控件）
 * <p>
 * 平台清单由 external-identity.config.js 声明式配置（身份类型/文案/图标/授权构造器/按钮文案均可自定义），
 * 控件本身不感知具体平台；视觉与主页 pane 卡片同源（根节点带 .pane 类以继承宿主页栅格样式）。
 * 放置于任意页面栅格内即可使用。
 * </p>
 */
export default {
  name: 'ExternalIdentityPanel',
  props: {
    /**
     * 面板标题 i18n key
     */
    title: {
      type: String,
      default: 'identity.title'
    },
    /**
     * 标题图标
     */
    icon: {
      type: String,
      default: 'el-icon-link'
    }
  },
  data() {
    return {
      loading: false,
      bindings: []
    }
  },
  computed: {
    providers() {
      return externalIdentityConfig.providers || []
    },
    boundCount() {
      return this.providers.filter(provider => this.boundOf(provider)).length
    }
  },
  created() {
    this.loadBindings()
  },
  methods: {
    loadBindings() {
      this.loading = true
      myExternalIdentities().then(res => {
        this.bindings = (res && res.data) || []
      }).finally(() => {
        this.loading = false
      })
    },
    boundOf(provider) {
      return this.bindings.find(item => item.identityType === provider.identityType) || null
    },
    bindLabel(provider) {
      if (provider.bindLabelKey) {
        return this.$t(provider.bindLabelKey)
      }
      return this.$t('identity.bindPlatform', { platform: this.$t(provider.labelKey) })
    },
    startBind(provider) {
      const authorize = provider.authorize || {}
      const callbackUri = `${window.location.origin}/#/oauth/callback`
      const url = buildAuthorizeUrl(authorize.builder, authorize.state, callbackUri)
      if (!url) {
        this.$message.error(this.$t('identity.authorizeNotConfigured'))
        return
      }
      window.location.href = url
    },
    handleUnbind(binding) {
      this.$confirm(this.$t('identity.confirmUnbind'), this.$t('identity.title'), {
        type: 'warning'
      }).then(() => {
        unbindExternalIdentity(binding.identityType, binding.userId).then(() => {
          this.$message.success(this.$t('identity.unbindSuccess'))
          this.loadBindings()
        })
      }).catch(() => {})
    }
  }
}
</script>

<style lang="scss" scoped>
/* ── 与主页 pane 同源的调色板 ── */
$surface: rgba(255, 255, 255, 0.72);
$border: rgba(0, 0, 0, 0.06);
$primary: #0a84ff;
$primary-bg: rgba(10, 132, 255, 0.06);
$green: #30b879;
$green-bg: rgba(48, 184, 121, 0.07);
$green-bd: rgba(48, 184, 121, 0.18);
$text-1: #1a1d23;
$text-2: #5a6072;
$text-3: #8e95a9;
$radius-sm: 12px;
$ease: cubic-bezier(0.22, 1, 0.36, 1);
$font-display: 'Outfit', -apple-system, BlinkMacSystemFont, sans-serif;

.ext-identity-pane {
  .pane-hd {
    display: flex;
    align-items: center;
    gap: 12px;
    margin: 0 0 12px 0;
    font-family: $font-display;
    font-size: 15px;
    font-weight: 700;
    color: $text-1;
    letter-spacing: -0.2px;
  }

  .pane-ic {
    width: 34px;
    height: 34px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 16px;
    flex-shrink: 0;
    background: $primary-bg;
    color: $primary;
    border: 1px solid rgba(10, 132, 255, 0.12);
  }

  .pane-badge {
    margin-left: auto;
    font-size: 10px;
    font-weight: 600;
    color: $green;
    background: $green-bg;
    border: 1px solid $green-bd;
    padding: 2px 8px;
    border-radius: 100px;
    letter-spacing: 0.3px;
  }

  .pane-ul {
    list-style: none;
    margin: 0;
    padding: 0;
    display: flex;
    flex-direction: column;
    gap: 8px;
  }

  .pane-li {
    display: flex;
    align-items: center;
    gap: 12px;
    font-size: 14px;
    color: $text-2;
    line-height: 1.5;
    padding: 6px 8px;
    margin: -2px 0;
    border-radius: $radius-sm;
    border: 1px solid transparent;
    transition: all 0.25s $ease;

    &:hover {
      background: rgba(10, 132, 255, 0.04);
      border-color: rgba(10, 132, 255, 0.08);
    }
  }

  .pane-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    flex-shrink: 0;
    box-shadow: 0 0 0 3px rgba(0, 0, 0, 0.02);
    transition: background 0.3s ease;
  }

  .row-info {
    display: flex;
    flex-direction: column;
    gap: 2px;
    min-width: 0;
    flex: 1;

    .row-name {
      display: flex;
      align-items: center;
      gap: 6px;
      font-weight: 500;
      color: $text-1;

      .row-ic {
        color: $text-3;
        font-size: 14px;
      }
    }

    small {
      font-size: 12px;
      color: $text-3;
      letter-spacing: 0.2px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .bind-btn {
    flex-shrink: 0;
    padding: 6px 14px;
    border-radius: 100px;
    color: $primary;
    background: $primary-bg;
    font-weight: 600;
    transition: all 0.25s $ease;

    &:hover {
      color: #fff;
      background: $primary;
    }
  }

  .unbind-btn {
    flex-shrink: 0;
    color: $text-3;

    &:hover {
      color: #f56c6c;
    }
  }

  .pane-emp {
    display: flex;
    align-items: center;
    gap: 10px;
    color: $text-3;
    font-size: 13px;
    padding: 10px 0;

    i {
      font-size: 18px;
      opacity: 0.5;
    }
  }

  .pane-hint {
    display: flex;
    align-items: center;
    gap: 6px;
    margin: 12px 0 0;
    padding-top: 10px;
    border-top: 1px dashed rgba(0, 0, 0, 0.05);
    font-size: 11px;
    color: $text-3;

    i {
      color: $primary;
      opacity: 0.6;
    }
  }
}
</style>

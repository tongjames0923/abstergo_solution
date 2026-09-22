<template>
  <div class="pv-wrap">
    <span class="pv-badge">{{ $t('notifyAdmin.preview.badge') }}</span>

    <!-- 消息中心视角：复用消息中心共享 token，与 MessageCard 严格同构 -->
    <div
      v-if="mode === 'center'"
      :key="'center'"
      class="ink-card is-unread"
      :class="{ 'is-urgent': form.priority === 'urgent' }"
      :style="centerVars"
    >
      <span class="ink-card__rail" />
      <div class="ink-card__body">
        <div class="ink-card__head">
          <span class="ink-chip">
            <i class="ink-chip__dot" />
            {{ $t('push.type.' + form.type) }}
          </span>
          <span
            v-if="isHighPriority"
            class="ink-priority"
            :style="priorityStyle"
          >
            {{ $t('push.priority.' + form.priority) }}
          </span>
        </div>
        <p class="ink-card__title">{{ form.title || $t('notifyAdmin.form.titlePlaceholder') }}</p>
        <p class="ink-card__content">{{ form.content || $t('notifyAdmin.preview.empty') }}</p>
        <div v-if="form.url" class="pv-url">
          <i class="el-icon-link" />
          <span>{{ form.url }}</span>
        </div>
        <div class="ink-card__meta">
          <span class="ink-time">{{ timeText }}</span>
          <i class="pv-unread" />
        </div>
      </div>
    </div>

    <!-- 在线弹窗视角：与站内实时弹窗（Element Notification）同构 -->
    <div
      v-else
      :key="'toast'"
      class="pv-toast"
      :style="toastVars"
    >
      <span class="pv-toast-icon">
        <i :class="toastIcon" />
      </span>
      <div class="pv-toast-body">
        <p class="pv-toast-title">{{ form.title || $t('push.defaultTitle') }}</p>
        <p class="pv-toast-content">{{ form.content || $t('notifyAdmin.preview.empty') }}</p>
        <span class="pv-toast-time">{{ timeText }}</span>
      </div>
    </div>
  </div>
</template>

<script>
import { typeAccent, priorityBadge } from '@/push/message-display'

/**
 * 通知实时预览卡（双视角）
 * <p>
 * center 模式与消息中心（PushNotificationCenter/MessageCard）严格同构：直接复用
 * components/PushNotificationCenter/styles/_message-card.scss 共享 token 与
 * push/message-display.js 语义色（Liquid Glass 玻璃卡 + 语义色导轨），与全站
 * macOS 浅色布局同色系；toast 模式与在线实时弹窗（Element Notification）
 * 同构：类型图标、标题、正文、时间。随表单输入实时重渲染，呈现"所见即用户所得"。
 * </p>
 */

/**
 * toast 视角类型图标映射（未知类型回退铃铛）
 */
const TOAST_ICONS = {
  notify: 'el-icon-bell',
  warning: 'el-icon-warning',
  reminder: 'el-icon-alarm-clock'
}

/**
 * 将 hex 色值转为带透明度的 rgba 字符串（非法输入回退灰色）
 *
 * @param {string} hex 十六进制色值（#rgb 或 #rrggbb）
 * @param {number} alpha 透明度 0-1
 * @returns {string} rgba 色值字符串
 */
function hexToRgba(hex, alpha) {
  const normalized = /^#([0-9a-f]{3}|[0-9a-f]{6})$/i.test(hex) ? hex : '#8e8e93'
  let value = normalized.slice(1)
  if (value.length === 3) {
    value = value.split('').map(ch => ch + ch).join('')
  }
  const num = parseInt(value, 16)
  const r = (num >> 16) & 255
  const g = (num >> 8) & 255
  const b = num & 255
  return 'rgba(' + r + ', ' + g + ', ' + b + ', ' + alpha + ')'
}

export default {
  name: 'NotifyPreviewCard',
  props: {
    form: {
      type: Object,
      required: true
    },
    /**
     * 预览视角：center = 消息中心卡片，toast = 在线实时弹窗
     */
    mode: {
      type: String,
      default: 'center'
    }
  },
  data() {
    return {
      now: Date.now()
    }
  },
  computed: {
    /**
     * 类型语义强调色（与消息中心共享 message-display 展示语义层）
     */
    accent() {
      return typeAccent(this.form.type)
    },
    /**
     * 优先级徽章参数（仅 high/urgent 展示）
     */
    badge() {
      return priorityBadge(this.form.priority)
    },
    isHighPriority() {
      return this.badge.visible
    },
    toastIcon() {
      return TOAST_ICONS[this.form.type] || 'el-icon-bell'
    },
    /**
     * 消息中心视角的语义色变量（极光导轨/类型圆点/未读辉光）
     */
    centerVars() {
      return {
        '--accent': this.accent
      }
    },
    /**
     * 优先级徽章语义色联动（文字/底色/描边，与 MessageCard 一致）
     */
    priorityStyle() {
      if (!this.badge.visible) {
        return {}
      }
      const accent = this.badge.accent
      return {
        color: accent,
        background: accent + '22',
        borderColor: accent + '3d'
      }
    },
    /**
     * 在线弹窗视角的语义色变量（类型图标底色/描边随类型联动）
     */
    toastVars() {
      return {
        '--accent': this.accent,
        '--accent-bg': hexToRgba(this.accent, 0.1),
        '--accent-border': hexToRgba(this.accent, 0.2)
      }
    },
    timeText() {
      return new Date(this.now).toLocaleTimeString()
    }
  },
  mounted() {
    this.timer = setInterval(() => {
      this.now = Date.now()
    }, 30000)
  },
  beforeDestroy() {
    clearInterval(this.timer)
  }
}
</script>

<style lang="scss" scoped>
/* 消息中心视角：复用共享 token，与 MessageCard 严格同构 */
@import '~@/components/PushNotificationCenter/styles/message-card';

.pv-wrap {
  position: relative;
  padding-top: 10px;
}

.pv-badge {
  position: absolute;
  top: 0;
  right: 0;
  z-index: 1;
  font-size: 10px;
  letter-spacing: 0.14em;
  color: #0a84ff;
  background: rgba(10, 132, 255, 0.1);
  border: 1px solid rgba(10, 132, 255, 0.22);
  border-radius: 999px;
  padding: 2px 10px;
}

/* 预览卡非交互件：覆盖共享 token 的指针形态 */
.ink-card {
  cursor: default;
}

/* 跳转路由胶囊（系统蓝淡染） */
.pv-url {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  max-width: 100%;
  margin-top: 8px;
  font-family: 'SF Mono', Menlo, monospace;
  font-size: 10.5px;
  color: #0a84ff;
  background: rgba(10, 132, 255, 0.07);
  border: 1px solid rgba(10, 132, 255, 0.16);
  border-radius: 6px;
  padding: 2px 8px;

  span {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

/* 未读辉光圆点 */
.pv-unread {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--accent);
  box-shadow: 0 0 8px rgba(0, 0, 0, 0.18);
}

/* ---------- 在线弹窗视角 ---------- */
.pv-toast {
  position: relative;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-top: 6px;
  padding: 14px 16px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.94);
  border: 1px solid rgba(0, 0, 0, 0.07);
  box-shadow:
    0 18px 44px rgba(12, 18, 38, 0.16),
    0 2px 8px rgba(12, 18, 38, 0.06);
  overflow: hidden;
  animation: pv-toast-in 0.4s cubic-bezier(0.22, 1, 0.36, 1) both;
}

@keyframes pv-toast-in {
  from {
    opacity: 0;
    transform: translateX(26px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.pv-toast-icon {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 10px;
  font-size: 17px;
  color: var(--accent);
  background: var(--accent-bg);
  border: 1px solid var(--accent-border);
}

.pv-toast-body {
  flex: 1;
  min-width: 0;
}

.pv-toast-title {
  margin: 0 0 4px;
  font-size: 13.5px;
  font-weight: 600;
  color: #1d1d1f;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pv-toast-content {
  margin: 0;
  font-size: 12px;
  line-height: 1.55;
  color: #6e6e73;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.pv-toast-time {
  display: block;
  margin-top: 7px;
  font-family: 'DIN Alternate', 'Avenir Next', 'Helvetica Neue', sans-serif;
  font-size: 10.5px;
  letter-spacing: 0.04em;
  color: #a1a1a6;
  font-variant-numeric: tabular-nums;
}
</style>

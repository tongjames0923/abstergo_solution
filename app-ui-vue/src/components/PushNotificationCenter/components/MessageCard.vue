<template>
  <li
    class="ink-card"
    :class="{ 'is-unread': !message.read, 'is-urgent': message.priority === 'urgent' }"
    :style="cardVars"
    @click="$emit('click', message)"
  >
    <span class="ink-card__rail" />
    <div class="ink-card__body">
      <div class="ink-card__head">
        <span class="ink-chip">
          <i class="ink-chip__dot" />
          {{ typeLabel }}
        </span>
        <span
          v-if="badge.visible"
          class="ink-priority"
          :style="badgeStyle"
        >{{ $t('push.priority.' + message.priority) }}</span>
      </div>
      <p class="ink-card__title">{{ message.title || $t('push.defaultTitle') }}</p>
      <p class="ink-card__content">{{ message.content || $t('push.center.emptyContent') }}</p>
      <div class="ink-card__meta">
        <span class="ink-time">
          <i class="el-icon-time" />
          {{ $t('push.center.sentAt', { time: timeText }) }}
        </span>
        <span v-if="message.sender" class="ink-sender">{{ message.sender }}</span>
      </div>
      <div class="ink-card__actions">
        <button
          v-if="!message.read"
          type="button"
          class="ink-btn ink-btn--read"
          @click.stop="$emit('mark-read', message)"
        >
          <i class="el-icon-check" />{{ $t('push.center.markRead') }}
        </button>
        <span v-else class="ink-read-flag">
          <i class="el-icon-circle-check" />{{ $t('push.center.read') }}
        </span>
        <button
          v-if="message.url"
          type="button"
          class="ink-btn ink-btn--open"
          @click.stop="$emit('click', message)"
        >
          <i class="el-icon-top-right" />{{ $t('push.center.open') }}
        </button>
      </div>
    </div>
  </li>
</template>

<script>
import { resolveTypeKey, typeAccent, priorityBadge, relativeTime } from '@/push/message-display'

/**
 * 单条消息卡（Liquid Glass 玻璃卡）
 * <p>
 * 纯展示组件：接收规范化消息对象，类型归组、优先级徽章、相对时间均委托
 * push/message-display.js 展示语义层；视觉结构复用共享 partial
 * styles/_message-card.scss（与 NotifyPreviewCard center 模式同构）。
 * 类型/优先级语义色经 CSS 变量 --accent 注入；入场动效由父级 MessageList 的
 * transition-group 统一编排（错峰延迟取 --delay），本组件不自带动画以免冲突。
 * 交互仅上抛语义事件（click / mark-read），业务动作由编排层处理。
 * </p>
 */
export default {
  name: 'PushMessageCard',
  props: {
    /**
     * 规范化消息对象（push Vuex 模块 messages 数组元素）
     */
    message: {
      type: Object,
      required: true
    },
    /**
     * 所在分组内的序号（错峰入场延迟封顶用）
     */
    index: {
      type: Number,
      default: 0
    }
  },
  computed: {
    /**
     * 归组后的展示类型键（未知 msgType 归 other，避免 i18n key 直出）
     */
    typeKey() {
      return resolveTypeKey(this.message.type)
    },
    typeLabel() {
      return this.$t('push.type.' + this.typeKey)
    },
    /**
     * 优先级徽章参数（仅 high/urgent 展示）
     */
    badge() {
      return priorityBadge(this.message.priority)
    },
    /**
     * 优先级徽章语义色联动（文字/底色/描边）
     */
    badgeStyle() {
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
     * 相对时间文案（服务端 createTime 优先，超 24 小时回退本地时间串）
     */
    timeText() {
      return relativeTime(this.message.timestamp || this.message.receivedAt, key => this.$t(key))
    },
    /**
     * 卡片样式变量：类型语义色 + 错峰入场延迟（列表 transition-group 消费）
     */
    cardVars() {
      return {
        '--accent': typeAccent(this.message.type),
        '--delay': Math.min(this.index, 10) * 45 + 'ms'
      }
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../styles/message-card';
</style>

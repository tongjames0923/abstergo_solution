<template>
  <div class="pn-filter">
    <div class="pn-seg">
      <button
        type="button"
        class="pn-seg__item"
        :class="{ 'is-active': !unreadOnly }"
        @click="$emit('toggle-unread', false)"
      >
        {{ $t('push.center.filter.all') }}
        <span class="pn-seg__count">{{ total }}</span>
      </button>
      <button
        type="button"
        class="pn-seg__item"
        :class="{ 'is-active': unreadOnly }"
        @click="$emit('toggle-unread', true)"
      >
        {{ $t('push.center.filter.unread') }}
        <span
          v-if="unreadCount"
          class="pn-seg__count"
        >{{ unreadCount }}</span>
      </button>
    </div>
    <div class="pn-tabs">
      <button
        v-for="tab in tabs"
        :key="tab.key"
        type="button"
        class="pn-tab"
        :class="{ 'is-active': activeType === tab.key }"
        :style="tab.key === 'all' ? {} : { '--accent': tab.accent }"
        @click="$emit('change-type', tab.key)"
      >
        <i
          v-if="tab.key !== 'all'"
          class="pn-tab__dot"
        />
        <span>{{ tab.label }}</span>
        <transition name="pn-badge">
          <span
            v-if="tab.unread"
            class="pn-tab__badge"
          >{{ tab.unread }}</span>
        </transition>
      </button>
    </div>
  </div>
</template>

<script>
/**
 * 消息中心筛选栏
 * <p>
 * 两行结构：上行“全部 / 只看未读”iOS 分段切换（未读侧带计数），下行类型 Tab 胶囊
 * （全部 / 通知 / 告警 / 提醒 / 其他-动态出现，各 Tab 带未读角标与语义色圆点）。
 * 纯受控组件：筛选状态由编排层持有，本组件仅上抛 change-type / toggle-unread。
 * 动效：选中态背景/描边/阴影过渡（0.24s），角标以缩放淡入淡出呈现增减；
 * 指针悬停与键盘焦点均有独立反馈，触摸设备走按压反馈避免粘滞 hover。
 * 响应式：手机端两行收纳为可横向滚动的紧凑条（触摸目标 >= 44px，右侧渐隐提示可滚动），
 * 手机横屏合并为单行；窄屏手机进一步收紧间距。
 * </p>
 */
export default {
  name: 'PushFilterBar',
  props: {
    /**
     * 类型 Tab 列表：[{ key, label, accent, unread }]（编排层派生）
     */
    tabs: {
      type: Array,
      default: () => []
    },
    /**
     * 当前选中类型键（'all' 或归组后的展示类型键）
     */
    activeType: {
      type: String,
      default: 'all'
    },
    /**
     * 是否只看未读
     */
    unreadOnly: {
      type: Boolean,
      default: false
    },
    /**
     * 未读总数
     */
    unreadCount: {
      type: Number,
      default: 0
    },
    /**
     * 消息总数
     */
    total: {
      type: Number,
      default: 0
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../styles/motion';

.pn-filter {
  padding: 12px var(--pn-gutter, 16px) 11px;
  display: flex;
  flex-direction: column;
  gap: 9px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  background: rgba(255, 255, 255, 0.28);
}

/* 分段切换（iOS 风格）：全部 / 只看未读 */
.pn-seg {
  display: inline-flex;
  align-self: flex-start;
  padding: 3px;
  border-radius: 999px;
  background: rgba(118, 118, 128, 0.12);
}

.pn-seg__item {
  appearance: none;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-family: inherit;
  font-size: 11.5px;
  letter-spacing: 0.04em;
  color: #86868b;
  padding: 5px 14px;
  border: none;
  border-radius: 999px;
  background: transparent;
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
  transition:
    background $mo-dur-base $mo-ease-out,
    color $mo-dur-fast ease,
    box-shadow $mo-dur-base $mo-ease-out,
    transform $mo-dur-fast $mo-ease-out;

  @include focus-ring;
  @include press;

  &.is-active {
    background: #fff;
    color: #0a84ff;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);

    @include hover-only {
      color: #0a84ff;
    }
  }

  @include hover-only {
    color: #1d1d1f;
  }
}

.pn-seg__count {
  font-family: 'DIN Alternate', 'Avenir Next', 'Helvetica Neue', sans-serif;
  font-size: 10.5px;
  color: inherit;
  opacity: 0.75;
  font-variant-numeric: tabular-nums;
  transition: opacity $mo-dur-fast ease;
}

/* 类型 Tab 胶囊：语义色圆点 + 未读角标，选中态语义色淡染 */
.pn-tabs {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;
  scroll-snap-type: x proximity;

  &::-webkit-scrollbar {
    display: none;
  }
}

.pn-tab {
  appearance: none;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
  scroll-snap-align: start;
  font-family: inherit;
  font-size: 11.5px;
  letter-spacing: 0.04em;
  color: #86868b;
  padding: 5px 13px;
  border-radius: 999px;
  border: 1px solid rgba(0, 0, 0, 0.08);
  background: rgba(255, 255, 255, 0.6);
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
  transition:
    background $mo-dur-base $mo-ease-out,
    border-color $mo-dur-base $mo-ease-out,
    color $mo-dur-fast ease,
    box-shadow $mo-dur-base $mo-ease-out,
    transform $mo-dur-fast $mo-ease-out;

  @include focus-ring;
  @include press;

  .pn-tab__dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: var(--accent, #8e8e93);
    transition: transform $mo-dur-base $mo-ease-spring;
  }

  @include hover-only {
    color: #1d1d1f;
    border-color: rgba(0, 0, 0, 0.14);
  }

  &.is-active {
    color: #1d1d1f;
    border-color: rgba(0, 0, 0, 0.12);
    border-color: color-mix(in srgb, var(--accent, #0a84ff) 30%, rgba(0, 0, 0, 0.06));
    background: #fff;
    background: linear-gradient(135deg, color-mix(in srgb, var(--accent, #0a84ff) 10%, transparent), rgba(255, 255, 255, 0.9));
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);

    .pn-tab__dot {
      transform: scale(1.15);
    }
  }

  .pn-tab__badge {
    min-width: 16px;
    height: 16px;
    display: inline-grid;
    place-items: center;
    padding: 0 4px;
    border-radius: 999px;
    font-family: 'DIN Alternate', 'Avenir Next', 'Helvetica Neue', sans-serif;
    font-size: 10px;
    color: #fff;
    background: var(--accent, #0a84ff);
    font-variant-numeric: tabular-nums;
  }
}

/* 角标增减：缩放淡入淡出 */
.pn-badge-enter-active,
.pn-badge-leave-active {
  transition: opacity $mo-dur-fast ease, transform $mo-dur-base $mo-ease-spring;
}

.pn-badge-enter,
.pn-badge-leave-to {
  opacity: 0;
  transform: scale(0.6);
}

/* 手机：紧凑条 + 触摸目标 >= 44px + 右侧渐隐提示可横向滚动 */
@include respond-to(phone) {
  .pn-filter {
    padding: 10px var(--pn-gutter, 14px) 9px;
    gap: 8px;
  }

  .pn-seg__item {
    @include touch-target;
    padding: 10px 18px;
    font-size: 12.5px;
  }

  .pn-tab {
    @include touch-target;
    padding: 10px 16px;
    font-size: 12.5px;
  }

  .pn-tabs {
    mask-image: linear-gradient(90deg, #000 calc(100% - 20px), transparent);
    -webkit-mask-image: linear-gradient(90deg, #000 calc(100% - 20px), transparent);
  }
}

/* 手机横屏：两行合并为单行（分段切换 + 可滚动 Tab） */
@include respond-to(phone-landscape) {
  .pn-filter {
    flex-direction: row;
    align-items: center;
    gap: 10px;
    padding-top: 8px;
    padding-bottom: 8px;
  }

  .pn-seg {
    align-self: center;
  }

  .pn-tabs {
    flex: 1;
    min-width: 0;
  }
}

/* 窄屏手机（<=360px）：收紧间距与字号 */
@media only screen and (max-width: 360px) {
  .pn-filter {
    padding-left: 12px;
    padding-right: 12px;
    gap: 7px;
  }

  .pn-seg__item {
    padding: 10px 14px;
    font-size: 12px;
  }

  .pn-tab {
    padding: 10px 13px;
    font-size: 12px;
  }
}

/* 弱动效偏好 */
@media (prefers-reduced-motion: reduce) {
  .pn-badge-enter-active,
  .pn-badge-leave-active {
    transition-duration: 0.01ms;
  }
}
</style>

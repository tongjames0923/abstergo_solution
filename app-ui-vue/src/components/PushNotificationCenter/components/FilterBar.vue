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
        <span
          v-if="tab.unread"
          class="pn-tab__badge"
        >{{ tab.unread }}</span>
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
 * 手机端两行均可横向滚动收纳为紧凑条，触摸目标 >= 44px。
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
.pn-filter {
  padding: 12px 16px 10px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  background: rgba(255, 255, 255, 0.25);
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
  transition: all 0.18s ease;

  &.is-active {
    background: #fff;
    color: #0a84ff;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
  }
}

.pn-seg__count {
  font-family: 'DIN Alternate', 'Avenir Next', 'Helvetica Neue', sans-serif;
  font-size: 10.5px;
  color: inherit;
  opacity: 0.75;
  font-variant-numeric: tabular-nums;
}

/* 类型 Tab 胶囊：语义色圆点 + 未读角标，选中态语义色淡染 */
.pn-tabs {
  display: flex;
  gap: 8px;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
  scrollbar-width: none;

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
  font-family: inherit;
  font-size: 11.5px;
  letter-spacing: 0.04em;
  color: #86868b;
  padding: 5px 13px;
  border-radius: 999px;
  border: 1px solid rgba(0, 0, 0, 0.08);
  background: rgba(255, 255, 255, 0.6);
  cursor: pointer;
  transition: all 0.18s ease;

  .pn-tab__dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: var(--accent, #8e8e93);
  }

  &.is-active {
    color: #1d1d1f;
    border-color: rgba(0, 0, 0, 0.12);
    border-color: color-mix(in srgb, var(--accent, #0a84ff) 30%, rgba(0, 0, 0, 0.06));
    background: #fff;
    background: linear-gradient(135deg, color-mix(in srgb, var(--accent, #0a84ff) 10%, transparent), rgba(255, 255, 255, 0.9));
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
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

/* 移动端：紧凑条 + 触摸目标 >= 44px */
@media (max-width: 768px) {
  .pn-filter {
    padding: 10px 12px 8px;
    gap: 8px;
  }

  .pn-seg__item {
    min-height: 44px;
    padding: 10px 18px;
    font-size: 12.5px;
  }

  .pn-tab {
    min-height: 44px;
    padding: 10px 16px;
    font-size: 12.5px;
  }
}
</style>

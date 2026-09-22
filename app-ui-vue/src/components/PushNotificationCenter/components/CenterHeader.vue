<template>
  <header class="pn-header">
    <div class="pn-heading">
      <span class="pn-beacon">
        <i class="el-icon-message-solid" />
      </span>
      <div class="pn-titles">
        <h3 class="pn-title">{{ $t('push.center.title') }}</h3>
        <p class="pn-sub">
          <span class="pn-sub-num">{{ unreadCount }}</span>
          <span class="pn-sub-label">{{ $t('push.center.unread') }}</span>
          <span
            v-if="total"
            class="pn-sub-total"
          >· {{ $t('push.center.total', { n: total }) }}</span>
        </p>
      </div>
    </div>
    <div class="pn-actions">
      <button
        type="button"
        class="pn-act"
        :disabled="!unreadCount"
        @click="$emit('mark-all-read')"
      >
        {{ $t('push.center.markAllRead') }}
      </button>
      <span class="pn-act-divider" />
      <button
        type="button"
        class="pn-act pn-act--danger"
        :disabled="!total"
        @click="$emit('clear')"
      >
        {{ $t('push.center.clear') }}
      </button>
    </div>
  </header>
</template>

<script>
/**
 * 消息中心头部
 * <p>
 * 信标徽标（呼吸辉光圆环）+ 标题与未读大数（DIN 数字、tabular-nums）+ “全部已读 /
 * 清空”胶囊操作。桌面端操作随头部右置；手机端经媒体查询收纳为底部固定操作栏
 * （毛玻璃 + 安全区留白，触摸目标 >= 44px），由编排层 pn-root 预留底部空间。
 * 事件仅上抛 mark-all-read / clear，业务动作由编排层处理。
 * </p>
 */
export default {
  name: 'PushCenterHeader',
  props: {
    /**
     * 未读消息数
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
.pn-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 22px 20px 14px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.pn-heading {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

/* 信标徽标：系统蓝玻璃 + 呼吸辉光圆环（与全站 macOS 浅色布局同色系） */
.pn-beacon {
  position: relative;
  width: 40px;
  height: 40px;
  display: grid;
  place-items: center;
  font-size: 18px;
  color: #0a84ff;
  background: linear-gradient(145deg, rgba(10, 132, 255, 0.14), rgba(10, 132, 255, 0.05));
  border: 1px solid rgba(10, 132, 255, 0.18);
  border-radius: 12px;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.7);
  flex-shrink: 0;

  &::before {
    content: '';
    position: absolute;
    inset: -4px;
    border-radius: 16px;
    border: 1px solid rgba(10, 132, 255, 0.28);
    animation: pn-beacon-ring 3.2s ease-in-out infinite;
  }
}

@keyframes pn-beacon-ring {
  0%, 100% {
    opacity: 0.2;
    transform: scale(1);
  }
  50% {
    opacity: 0.7;
    transform: scale(1.05);
  }
}

.pn-title {
  margin: 0;
  font-size: 17px;
  font-weight: 600;
  letter-spacing: 0.02em;
  line-height: 1.2;
  color: #1d1d1f;
}

.pn-sub {
  margin: 3px 0 0;
  display: flex;
  align-items: baseline;
  gap: 4px;
  color: #86868b;

  .pn-sub-num {
    font-family: 'DIN Alternate', 'Avenir Next', 'Helvetica Neue', sans-serif;
    font-size: 15px;
    font-weight: 700;
    color: #0a84ff;
    font-variant-numeric: tabular-nums;
    line-height: 1;
  }

  .pn-sub-label {
    font-size: 11px;
    letter-spacing: 0.06em;
  }

  .pn-sub-total {
    font-family: 'DIN Alternate', 'Avenir Next', 'Helvetica Neue', sans-serif;
    font-size: 11px;
    color: #a1a1a6;
  }
}

.pn-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.pn-act {
  appearance: none;
  border: 1px solid rgba(0, 0, 0, 0.08);
  background: rgba(255, 255, 255, 0.6);
  color: #0a84ff;
  font-family: inherit;
  font-size: 11px;
  letter-spacing: 0.04em;
  padding: 5px 12px;
  border-radius: 999px;
  cursor: pointer;
  transition: all 0.18s ease;

  &:hover:not(:disabled) {
    background: #0a84ff;
    border-color: #0a84ff;
    color: #fff;
    box-shadow: 0 4px 14px rgba(10, 132, 255, 0.32);
  }

  &:disabled {
    color: #c7c7cc;
    cursor: default;
  }

  &--danger {
    color: #ff3b30;

    &:hover:not(:disabled) {
      background: #ff3b30;
      border-color: #ff3b30;
      box-shadow: 0 4px 14px rgba(255, 59, 48, 0.3);
    }
  }
}

.pn-act-divider {
  width: 1px;
  height: 12px;
  background: rgba(0, 0, 0, 0.1);
}

/* 手机端：操作收纳为底部固定操作栏（毛玻璃 + 安全区留白），触摸目标 >= 44px */
@media (max-width: 768px) {
  .pn-actions {
    position: fixed;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 10;
    justify-content: center;
    gap: 12px;
    padding: 10px 16px calc(10px + env(safe-area-inset-bottom, 0px));
    background: rgba(255, 255, 255, 0.9);
    backdrop-filter: blur(20px) saturate(1.8);
    -webkit-backdrop-filter: blur(20px) saturate(1.8);
    border-top: 1px solid rgba(0, 0, 0, 0.06);
  }

  .pn-act {
    min-height: 44px;
    padding: 10px 22px;
    font-size: 12.5px;
  }

  .pn-beacon::before {
    animation: none;
    opacity: 0.4;
  }
}

/* 手机横屏：高度受限时压缩头部 */
@media (orientation: landscape) and (max-width: 896px) {
  .pn-header {
    padding-top: 12px;
    padding-bottom: 10px;
  }

  .pn-beacon {
    width: 34px;
    height: 34px;
    font-size: 15px;
  }
}
</style>

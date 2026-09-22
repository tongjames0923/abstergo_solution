<template>
  <header class="pn-header">
    <div class="pn-top">
      <div class="pn-heading">
        <span class="pn-beacon">
          <i class="el-icon-message-solid" />
        </span>
        <div class="pn-titles">
          <h3 class="pn-title">{{ $t('push.center.title') }}</h3>
          <p class="pn-sub">
            <span class="pn-sub-num-slot">
              <transition name="pn-tick" mode="out-in">
                <span
                  :key="unreadCount"
                  class="pn-sub-num"
                >{{ unreadCount }}</span>
              </transition>
            </span>
            <span class="pn-sub-label">{{ $t('push.center.unread') }}</span>
            <span
              v-if="total"
              class="pn-sub-total"
            >· {{ $t('push.center.total', { n: total }) }}</span>
          </p>
        </div>
      </div>
      <!-- 关闭按钮常驻：手机端抽屉全屏无遮罩可点，须保留显式关闭入口 -->
      <button
        type="button"
        class="pn-close"
        :title="$t('push.center.close')"
        :aria-label="$t('push.center.close')"
        @click="$emit('close')"
      >
        <i class="el-icon-close" />
      </button>
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
 * 两行结构，避免操作与标题争抢横向空间（窄抽屉 + 长文案下不会互相遮挡）：
 * 上行「信标徽标（呼吸辉光圆环）+ 标题/未读大数 + 常驻关闭按钮」，
 * 下行「全部已读 / 清空」操作组。
 * 关闭按钮必须常驻——手机端抽屉全屏占满视口、无遮罩可点，若省略则无法退出。
 * 动效：未读数变化以纵向位移+淡入淡出切换（key 驱动），信标圆环低频呼吸；
 * 动作按钮悬停填充、键盘焦点高亮、按压轻微缩放。
 * 响应式：手机端下行操作组改为绝对定位于抽屉底部的毛玻璃浮层（安全区留白，
 * 触摸目标 >= 44px；用 absolute 而非 fixed，避免祖先 transform/backdrop-filter
 * 改变包含块导致错位），由编排层 .pn-root 预留底部空间；顶部预留刘海安全区；
 * 手机横屏压缩头部与浮层高度；窄屏手机缩小信标与字号。
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
@import '../styles/motion';

.pn-header {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 20px var(--pn-gutter, 18px) 12px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.pn-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.pn-heading {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
}

/* 标题区可压缩：英文等长文案场景以省略号收尾 */
.pn-titles {
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
  transition: box-shadow $mo-dur-base ease;

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
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pn-sub {
  margin: 3px 0 0;
  display: flex;
  align-items: baseline;
  gap: 4px;
  color: #86868b;
  overflow: hidden;
  white-space: nowrap;

  /* 数字切换槽：为纵向位移留出稳定占位 */
  .pn-sub-num-slot {
    position: relative;
    display: inline-block;
    min-width: 1.1em;
    text-align: center;
  }

  .pn-sub-num {
    display: inline-block;
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

/* 未读数变化：新值自下浮现、旧值向上淡出 */
.pn-tick-enter-active {
  transition: opacity $mo-dur-fast ease, transform $mo-dur-base $mo-ease-spring;
}

.pn-tick-leave-active {
  transition: opacity $mo-dur-fast ease, transform $mo-dur-fast ease;
  position: absolute;
  left: 0;
  right: 0;
}

.pn-tick-enter {
  opacity: 0;
  transform: translateY(7px);
}

.pn-tick-leave-to {
  opacity: 0;
  transform: translateY(-7px);
}

/* 关闭按钮：常驻右上，手机端为 44px 触摸目标 */
.pn-close {
  appearance: none;
  width: 30px;
  height: 30px;
  display: grid;
  place-items: center;
  font-size: 15px;
  color: #86868b;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 8px;
  cursor: pointer;
  flex-shrink: 0;
  -webkit-tap-highlight-color: transparent;
  transition:
    background $mo-dur-fast ease,
    border-color $mo-dur-fast ease,
    color $mo-dur-fast ease,
    box-shadow $mo-dur-base ease,
    transform $mo-dur-fast $mo-ease-out;

  @include focus-ring;
  @include press;

  @include hover-only {
    color: #1d1d1f;
    background: #fff;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  }
}

/* 操作组：桌面/平板位于标题下方独立一行，右对齐与关闭按钮形成同一条视觉轴 */
.pn-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 8px;
}

.pn-act {
  appearance: none;
  border: 1px solid rgba(0, 0, 0, 0.08);
  background: rgba(255, 255, 255, 0.6);
  color: #0a84ff;
  font-family: inherit;
  font-size: 11.5px;
  letter-spacing: 0.04em;
  padding: 6px 14px;
  border-radius: 999px;
  cursor: pointer;
  white-space: nowrap;
  -webkit-tap-highlight-color: transparent;
  transition:
    background $mo-dur-fast ease,
    border-color $mo-dur-fast ease,
    color $mo-dur-fast ease,
    box-shadow $mo-dur-base ease,
    transform $mo-dur-fast $mo-ease-out;

  @include focus-ring;
  @include press;

  @media (hover: hover) and (pointer: fine) {
    &:hover:not(:disabled) {
      background: #0a84ff;
      border-color: #0a84ff;
      color: #fff;
      box-shadow: 0 4px 14px rgba(10, 132, 255, 0.32);
    }
  }

  &:disabled {
    color: #c7c7cc;
    cursor: default;
    transform: none;
  }

  &--danger {
    color: #ff3b30;

    @media (hover: hover) and (pointer: fine) {
      &:hover:not(:disabled) {
        background: #ff3b30;
        border-color: #ff3b30;
        color: #fff;
        box-shadow: 0 4px 14px rgba(255, 59, 48, 0.3);
      }
    }
  }
}

.pn-act-divider {
  width: 1px;
  height: 12px;
  background: rgba(0, 0, 0, 0.1);
}

/* 手机：操作组转为抽屉底部浮层（相对抽屉根定位），顶部预留刘海安全区 */
@include respond-to(phone) {
  .pn-header {
    padding: calc(18px + env(safe-area-inset-top, 0px)) var(--pn-gutter, 14px) 12px;
  }

  .pn-actions {
    position: absolute;
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
    @include touch-target;
    padding: 10px 22px;
    font-size: 12.5px;
  }

  .pn-close {
    @include touch-target;
    width: auto;
    min-width: $touch-target-size;
    font-size: 17px;
    border-radius: 10px;
  }

  .pn-beacon::before {
    animation: none;
    opacity: 0.4;
  }
}

/* 手机横屏：压缩头部与底部浮层高度 */
@include respond-to(phone-landscape) {
  .pn-header {
    padding-top: calc(10px + env(safe-area-inset-top, 0px));
    padding-bottom: 8px;
  }

  .pn-actions {
    padding: 7px 16px calc(7px + env(safe-area-inset-bottom, 0px));
  }

  .pn-beacon {
    width: 34px;
    height: 34px;
    font-size: 15px;
  }

  .pn-title {
    font-size: 15px;
  }
}

/* 窄屏手机（<=360px）：缩小信标与字号 */
@media only screen and (max-width: 360px) {
  .pn-header {
    gap: 8px;
  }

  .pn-beacon {
    width: 36px;
    height: 36px;
    font-size: 16px;
  }

  .pn-title {
    font-size: 16px;
  }
}

/* 弱动效偏好 */
@media (prefers-reduced-motion: reduce) {
  .pn-beacon::before {
    animation: none;
    opacity: 0.4;
  }

  .pn-tick-enter-active,
  .pn-tick-leave-active {
    transition-duration: 0.01ms;
  }
}
</style>

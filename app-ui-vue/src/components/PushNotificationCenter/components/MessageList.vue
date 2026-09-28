<template>
  <div
    class="pn-list"
    @scroll.passive="handleScroll"
  >
    <transition-group
      name="pn-group"
      tag="div"
      class="pn-groups"
    >
      <section
        v-for="group in groups"
        :key="group.key"
        class="pn-group"
      >
        <header class="pn-group__head">{{ $t('push.center.group.' + group.key) }}</header>
        <transition-group
          name="pn-list"
          tag="ul"
          class="pn-group__list"
          appear
        >
          <message-card
            v-for="(message, index) in group.items"
            :key="message.id"
            :message="message"
            :index="index"
            @click="$emit('item-click', $event)"
            @mark-read="$emit('item-mark-read', $event)"
          />
        </transition-group>
      </section>
    </transition-group>
    <transition name="pn-more">
      <div
        v-if="hasMore"
        class="pn-load-more"
      >
        <button
          type="button"
          class="pn-more-btn"
          :disabled="loadingMore"
          @click="$emit('load-more')"
        >
          <i v-if="loadingMore" class="el-icon-loading" />
          {{ loadingMore ? $t('push.center.loading') : $t('push.center.loadMore') }}
        </button>
      </div>
    </transition>
  </div>
</template>

<script>
import MessageCard from './MessageCard'

/**
 * 消息中心滚动列表
 * <p>
 * 渲染按今天/昨天/更早分组的消息流，动效由两层 transition-group 编排：
 * 外层负责分组（section）的进出与位移，内层负责消息卡（appear 首屏错峰浮现、
 * 筛选切换淡出、新消息插入时的位移动画）。分组标题 sticky 吸顶，实色底 +
 * 发丝分隔线，卡片滚动时从其下方穿行而不穿透。
 * 滚动通道：近底部（阈值 80px、节流 200ms）触发 load-more，与底部按钮互为兜底；
 * store 层 loadingMore 防重入保证幂等。子项事件原样上抛，不掺业务逻辑。
 * </p>
 */

/**
 * 滚动近底部判定阈值（px）
 */
const LOAD_MORE_THRESHOLD = 80

/**
 * 滚动节流间隔（毫秒）
 */
const SCROLL_THROTTLE = 200

export default {
  name: 'PushMessageList',
  components: { MessageCard },
  props: {
    /**
     * 时间分组结果：[{ key: 'today'|'yesterday'|'earlier', items: [...] }]
     */
    groups: {
      type: Array,
      default: () => []
    },
    /**
     * 是否还有更早的历史消息（游标分页）
     */
    hasMore: {
      type: Boolean,
      default: false
    },
    /**
     * 加载更多进行中
     */
    loadingMore: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      /**
       * 滚动节流标记（timeout 挂起期间丢弃新事件）
       */
      scrollTicking: false
    }
  },
  methods: {
    /**
     * 滚动节流：近底部且仍有更多时触发 load-more
     * <p>
     * 事件对象在 timeout 闭包内复用前先取 target 引用，避免 0 延迟下
     * 复用事件带来的取值歧义。
     * </p>
     *
     * @param {Event} evt 原生滚动事件
     */
    handleScroll(evt) {
      if (this.scrollTicking || !this.hasMore || this.loadingMore) {
        return
      }
      const el = evt.target
      this.scrollTicking = true
      window.setTimeout(() => {
        this.scrollTicking = false
        if (el.scrollHeight - el.scrollTop - el.clientHeight < LOAD_MORE_THRESHOLD) {
          this.$emit('load-more')
        }
      }, SCROLL_THROTTLE)
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../styles/motion';

.pn-list {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  overscroll-behavior: contain;
  -webkit-overflow-scrolling: touch;
  padding: 6px var(--pn-gutter, 16px) 22px;
  /* sticky 分组标题的高度补偿：锚点滚动不被标题遮挡 */
  scroll-padding-top: 34px;

  &::-webkit-scrollbar {
    width: 4px;
  }
  &::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.14);
    border-radius: 4px;
  }
  &::-webkit-scrollbar-track {
    background: transparent;
  }
}

.pn-groups {
  position: relative;
}

/* 时间分组吸顶标题：与抽屉底色一致的实色横条 + 底部发丝线。
   不用半透明/毛玻璃底——它们与渐变背景叠加会产生矩形色差；
   实色 + 发丝线最简单稳定，卡片从下方穿行被完全遮住。 */
.pn-group__head {
  position: sticky;
  top: 0;
  z-index: 5;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px 2px;
  font-size: 10.5px;
  font-weight: 600;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: #a1a1a6;
  background: #f6f7fa;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);

  &::after {
    content: '';
    flex: 1;
    height: 1px;
    background: linear-gradient(90deg, rgba(0, 0, 0, 0.1), transparent);
  }
}

.pn-group__list {
  position: relative;
  list-style: none;
  margin: 0 0 10px;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

/* ── 分组进出与位移 ─────────────────────────────── */
.pn-group-enter-active,
.pn-group-leave-active {
  transition: opacity $mo-dur-base $mo-ease-out, transform $mo-dur-base $mo-ease-out;
}

.pn-group-enter,
.pn-group-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

.pn-group-leave-active {
  position: absolute;
  left: 0;
  right: 0;
}

.pn-group-move {
  transition: transform $mo-dur-base $mo-ease-out;
}

/* ── 消息卡浮现 / 离场 / 位移（--delay 提供错峰） ──── */
.pn-list-enter-active {
  transition:
    opacity $mo-dur-base $mo-ease-out,
    transform $mo-dur-base $mo-ease-spring;
  transition-delay: var(--delay, 0ms);
}

.pn-list-enter,
.pn-list-leave-to {
  opacity: 0;
  transform: translateX(16px) scale(0.98);
}

.pn-list-leave-active {
  transition:
    opacity $mo-dur-fast ease,
    transform $mo-dur-fast ease;
  position: absolute;
  left: 0;
  right: 0;
}

.pn-list-move {
  transition: transform $mo-dur-base $mo-ease-out;
}

/* ── 加载更多 ─────────────────────────────── */
.pn-load-more {
  display: flex;
  justify-content: center;
  padding: 8px 0 2px;
}

.pn-more-enter-active,
.pn-more-leave-active {
  transition: opacity $mo-dur-base ease, transform $mo-dur-base $mo-ease-out;
}

.pn-more-enter,
.pn-more-leave-to {
  opacity: 0;
  transform: translateY(6px);
}

.pn-more-btn {
  appearance: none;
  border: 1px solid rgba(0, 0, 0, 0.08);
  background: rgba(255, 255, 255, 0.6);
  color: #0a84ff;
  font-family: inherit;
  font-size: 11.5px;
  letter-spacing: 0.04em;
  padding: 7px 22px;
  border-radius: 999px;
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
  transition:
    background $mo-dur-fast ease,
    border-color $mo-dur-fast ease,
    color $mo-dur-fast ease,
    box-shadow $mo-dur-base ease,
    transform $mo-dur-fast $mo-ease-out;

  @include focus-ring;
  @include press;

  i {
    margin-right: 4px;
  }

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
  }
}

/* ── 手机：触摸目标 >= 44px，毛玻璃吸顶降级为实色 ── */
@include respond-to(phone) {
  .pn-list {
    padding: 4px var(--pn-gutter, 14px) 18px;
  }

  .pn-group__head {
    padding: 7px 2px;
  }

  .pn-more-btn {
    @include touch-target;
    padding: 10px 26px;
  }
}

/* ── 手机横屏：压缩纵向留白 ── */
@include respond-to(phone-landscape) {
  .pn-list {
    padding-top: 2px;
    padding-bottom: 12px;
  }

  .pn-group__head {
    padding: 4px 2px;
  }

  .pn-group__list {
    gap: 8px;
    margin-bottom: 6px;
  }
}

/* ── 弱动效偏好：交由系统设置关闭位移类过渡 ── */
@media (prefers-reduced-motion: reduce) {
  .pn-group-enter-active,
  .pn-group-leave-active,
  .pn-group-move,
  .pn-list-enter-active,
  .pn-list-leave-active,
  .pn-list-move,
  .pn-more-enter-active,
  .pn-more-leave-active {
    transition-duration: 0.01ms;
    transition-delay: 0ms;
  }
}
</style>

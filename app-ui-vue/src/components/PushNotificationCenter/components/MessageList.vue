<template>
  <div
    class="pn-list"
    @scroll.passive="handleScroll"
  >
    <section
      v-for="group in groups"
      :key="group.key"
      class="pn-group"
    >
      <header class="pn-group__head">{{ $t('push.center.group.' + group.key) }}</header>
      <ul class="pn-group__list">
        <message-card
          v-for="(message, index) in group.items"
          :key="message.id"
          :message="message"
          :index="index"
          @click="$emit('item-click', $event)"
          @mark-read="$emit('item-mark-read', $event)"
        />
      </ul>
    </section>
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
  </div>
</template>

<script>
import MessageCard from './MessageCard'

/**
 * 消息中心滚动列表
 * <p>
 * 渲染按今天/昨天/更早分组的消息流（分组标题吸顶），承载“加载更多”双通道中的
 * 滚动通道：滚动近底部（阈值 80px、节流 200ms）触发 load-more，与底部按钮互为
 * 兜底；store 层 loadingMore 防重入保证幂等。子项事件原样上抛，不掺业务逻辑。
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
.pn-list {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  padding: 14px 14px 20px;

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

/* 时间分组吸顶标题：小号大写字距标签 + 细线延伸 */
.pn-group__head {
  position: sticky;
  top: 0;
  z-index: 5;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 6px 4px 8px;
  font-size: 10.5px;
  font-weight: 600;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  color: #a1a1a6;
  background: rgba(246, 247, 250, 0.92);

  &::after {
    content: '';
    flex: 1;
    height: 1px;
    background: linear-gradient(90deg, rgba(0, 0, 0, 0.1), transparent);
  }
}

.pn-group__list {
  list-style: none;
  margin: 0 0 10px;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.pn-load-more {
  display: flex;
  justify-content: center;
  padding: 6px 0 2px;
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
  transition: all 0.18s ease;

  i {
    margin-right: 4px;
  }

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
}

/* 移动端：加载更多按钮触摸目标 >= 44px */
@media (max-width: 768px) {
  .pn-more-btn {
    min-height: 44px;
    padding: 10px 26px;
  }
}
</style>

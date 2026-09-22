<template>
  <el-drawer
    :visible="visible"
    :size="drawerSize"
    direction="rtl"
    custom-class="push-center-drawer"
    append-to-body
    :show-close="false"
    @close="handleClose"
  >
    <div class="pn-root">
      <!-- 头部：标题 + 未读大数 + 胶囊操作 -->
      <header class="pn-header">
        <div class="pn-heading">
          <span class="pn-glyph">
            <i class="el-icon-message-solid" />
          </span>
          <div class="pn-titles">
            <h3 class="pn-title">{{ $t('push.center.title') }}</h3>
            <p class="pn-sub">
              <span class="pn-sub-num">{{ unreadCount }}</span>
              <span class="pn-sub-label">{{ $t('push.center.unread') }}</span>
              <span v-if="!unreadCount" class="pn-sub-calm">·  ∞</span>
            </p>
          </div>
        </div>
        <div class="pn-actions">
          <button
            class="pn-act"
            :disabled="!unreadCount"
            @click="handleAllRead"
          >
            {{ $t('push.center.markAllRead') }}
          </button>
          <span class="pn-act-divider" />
          <button
            class="pn-act pn-act--danger"
            :disabled="!messages.length"
            @click="handleClear"
          >
            {{ $t('push.center.clear') }}
          </button>
        </div>
      </header>

      <!-- 空态：光晕中的铃铛 -->
      <div v-if="!messages.length" class="pn-empty">
        <div class="pn-empty-halo">
          <i class="el-icon-bell" />
        </div>
        <p class="pn-empty-text">{{ $t('push.center.empty') }}</p>
      </div>

      <!-- 消息流：错峰浮现的玻璃卡片 -->
      <ul v-else class="pn-list">
        <li
          v-for="(item, index) in messages"
          :key="item.id"
          class="pn-card"
          :class="{ 'is-unread': !item.read }"
          :style="{
            '--accent': accentOf(item.type),
            '--delay': Math.min(index, 10) * 45 + 'ms'
          }"
          @click="handleItemClick(item)"
        >
          <span class="pn-card-rail" />
          <div class="pn-card-body">
            <div class="pn-card-head">
              <span class="pn-chip">
                <i class="pn-chip-dot" />
                {{ $t('push.type.' + item.type) }}
              </span>
              <span
                v-if="item.priority === 'urgent' || item.priority === 'high'"
                class="pn-priority"
              >{{ $t('push.priority.' + item.priority) }}</span>
            </div>
            <p class="pn-card-title">{{ item.title || $t('push.defaultTitle') }}</p>
            <p class="pn-card-content">{{ item.content }}</p>
            <div class="pn-card-foot">
              <span class="pn-time">{{ relativeTime(item.receivedAt || item.timestamp) }}</span>
              <i v-if="!item.read" class="pn-unread-dot" />
            </div>
          </div>
        </li>
      </ul>
    </div>
  </el-drawer>
</template>

<script>
import { mapGetters, mapActions } from 'vuex'

/**
 * 消息中心抽屉（Liquid Glass 视觉）
 * <p>
 * 展示当前用户接收到的推送消息历史，区分已读/未读，支持单条查看（标记已读并可跳转 url）、
 * 全部已读与清空。数据源自 push Vuex 模块，文案全部走 i18n。
 * 外壳样式因 append-to-body 传送至 body 而置于非 scoped 样式块（.push-center-drawer 命名空间下）。
 * </p>
 */
export default {
  name: 'PushNotificationCenter',
  props: {
    visible: {
      type: Boolean,
      default: false
    }
  },
  computed: {
    ...mapGetters('push', ['pushMessages', 'pushUnreadCount']),
    messages() {
      return this.pushMessages
    },
    unreadCount() {
      return this.pushUnreadCount
    },
    drawerSize() {
      return this.$store.getters.device === 'mobile' ? '100%' : '384px'
    }
  },
  watch: {
    visible(open) {
      // 打开时主动拉取最新一页，兜底内存态未更新的情况
      if (open) {
        this.pullLatest().catch(e => console.warn('[push-center] 拉取失败', e))
      }
    }
  },
  methods: {
    ...mapActions('push', ['markRead', 'markAllRead', 'clearAll', 'pullLatest']),
    handleClose() {
      this.$emit('update:visible', false)
    },
    handleItemClick(item) {
      if (!item.read) {
        this.markRead(item.id)
      }
      if (item.url) {
        this.$router.push(item.url)
        this.handleClose()
      }
    },
    handleAllRead() {
      this.markAllRead()
    },
    handleClear() {
      this.clearAll()
    },
    /**
     * 消息类型 → 语义强调色（Apple 系统色板）
     *
     * @param {string} type 消息类型
     * @returns {string} 颜色值
     */
    accentOf(type) {
      const map = {
        notify: '#34c759',
        warning: '#ff9f0a',
        reminder: '#0a84ff'
      }
      return map[type] || '#8e8e93'
    },
    /**
     * 相对时间文案（超过一小时回退本地时间串）
     *
     * @param {number} ts 毫秒时间戳
     * @returns {string} 展示文案
     */
    relativeTime(ts) {
      if (!ts) {
        return ''
      }
      const diff = Date.now() - ts
      if (diff < 60_000) {
        return this.$t('push.time.justNow')
      }
      if (diff < 3_600_000) {
        return Math.floor(diff / 60_000) + this.$t('push.time.minutesAgo')
      }
      return new Date(ts).toLocaleString()
    }
  }
}
</script>

<!-- 面板外壳：append-to-body 后需在全局命名空间下覆盖 -->
<style lang="scss">
.push-center-drawer {
  background:
    radial-gradient(120% 90% at 100% 0%, rgba(10, 132, 255, 0.05), transparent 55%),
    radial-gradient(90% 70% at 0% 100%, rgba(52, 199, 89, 0.04), transparent 50%),
    rgba(246, 247, 250, 0.88);
  backdrop-filter: blur(40px) saturate(1.8);
  -webkit-backdrop-filter: blur(40px) saturate(1.8);
  border-left: 1px solid rgba(255, 255, 255, 0.65);
  box-shadow:
    -28px 0 80px rgba(12, 18, 38, 0.16),
    inset 1px 0 0 rgba(255, 255, 255, 0.55);

  /* 内建头与内边距让位给自定义头部 */
  .el-drawer__header {
    display: none;
  }
  .el-drawer__body {
    padding: 0;
    height: 100%;
  }
}
</style>

<style lang="scss" scoped>
$font-cjk: -apple-system, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
$font-num: 'DIN Alternate', 'Avenir Next', 'Helvetica Neue', sans-serif;

.pn-root {
  height: 100%;
  display: flex;
  flex-direction: column;
  font-family: $font-cjk;
  color: #1d1d1f;
}

/* ── 头部 ─────────────────────────────── */
.pn-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 22px 20px 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
}

.pn-heading {
  display: flex;
  align-items: center;
  gap: 12px;
}

.pn-glyph {
  width: 40px;
  height: 40px;
  display: grid;
  place-items: center;
  font-size: 19px;
  color: #0a84ff;
  background: linear-gradient(145deg, rgba(10, 132, 255, 0.14), rgba(10, 132, 255, 0.05));
  border: 1px solid rgba(10, 132, 255, 0.18);
  border-radius: 12px;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.7);
}

.pn-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 0.02em;
  line-height: 1.2;
}

.pn-sub {
  margin: 3px 0 0;
  display: flex;
  align-items: baseline;
  gap: 4px;
  color: #86868b;

  .pn-sub-num {
    font-family: $font-num;
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
  .pn-sub-calm {
    font-family: $font-num;
    font-size: 11px;
  }
}

.pn-actions {
  display: flex;
  align-items: center;
  gap: 8px;
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

/* ── 空态 ─────────────────────────────── */
.pn-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 18px;
}

.pn-empty-halo {
  position: relative;
  width: 92px;
  height: 92px;
  display: grid;
  place-items: center;

  i {
    font-size: 36px;
    color: #0a84ff;
    opacity: 0.75;
    z-index: 1;
  }

  /* 双层光晕呼吸 */
  &::before,
  &::after {
    content: '';
    position: absolute;
    inset: 0;
    border-radius: 50%;
    background: radial-gradient(circle, rgba(10, 132, 255, 0.16), transparent 68%);
    animation: pn-breathe 3.2s ease-in-out infinite;
  }
  &::after {
    inset: -18px;
    background: radial-gradient(circle, rgba(10, 132, 255, 0.07), transparent 70%);
    animation-delay: 1.4s;
  }
}

@keyframes pn-breathe {
  0%, 100% { transform: scale(0.92); opacity: 0.55; }
  50% { transform: scale(1.06); opacity: 1; }
}

.pn-empty-text {
  margin: 0;
  font-size: 13px;
  letter-spacing: 0.14em;
  color: #a1a1a6;
}

/* ── 消息流 ───────────────────────────── */
.pn-list {
  list-style: none;
  margin: 0;
  padding: 14px 14px 20px;
  overflow-y: auto;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;

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

.pn-card {
  position: relative;
  display: flex;
  align-items: stretch;
  background: rgba(255, 255, 255, 0.66);
  border: 1px solid rgba(0, 0, 0, 0.055);
  border-radius: 14px;
  padding: 12px 14px 12px 16px;
  cursor: pointer;
  overflow: hidden;
  transition:
    transform 0.2s cubic-bezier(0.2, 0.8, 0.3, 1),
    box-shadow 0.2s ease,
    background 0.2s ease;
  /* 错峰浮现 */
  animation: pn-rise 0.42s cubic-bezier(0.2, 0.8, 0.3, 1) both;
  animation-delay: var(--delay);

  &:hover {
    background: rgba(255, 255, 255, 0.92);
    transform: translateY(-2px);
    box-shadow: 0 10px 28px rgba(12, 18, 38, 0.1);
  }

  /* 类型语义色导轨：未读时点亮 */
  .pn-card-rail {
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 3px;
    background: var(--accent);
    opacity: 0.25;
    transition: opacity 0.2s ease;
  }
  &.is-unread .pn-card-rail {
    opacity: 1;
  }

  /* 未读：语义色薄雾洗底（color-mix 不可用时回退纯玻璃底） */
  &.is-unread {
    background: rgba(255, 255, 255, 0.78);
    background:
      linear-gradient(100deg, color-mix(in srgb, var(--accent) 7%, transparent), transparent 42%),
      rgba(255, 255, 255, 0.72);
    border-color: rgba(0, 0, 0, 0.08);
    border-color: color-mix(in srgb, var(--accent) 22%, rgba(0, 0, 0, 0.05));
  }
}

@keyframes pn-rise {
  from {
    opacity: 0;
    transform: translateX(18px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.pn-card-body {
  flex: 1;
  min-width: 0;
}

.pn-card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.pn-chip {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 10px;
  letter-spacing: 0.1em;
  color: #86868b;

  .pn-chip-dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: var(--accent);
    box-shadow: 0 0 0 3px rgba(0, 0, 0, 0.06);
    box-shadow: 0 0 0 3px color-mix(in srgb, var(--accent) 18%, transparent);
  }
}

.pn-priority {
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 0.08em;
  color: #ff3b30;
  padding: 2px 8px;
  border-radius: 999px;
  background: rgba(255, 59, 48, 0.09);
  border: 1px solid rgba(255, 59, 48, 0.18);
}

.pn-card-title {
  margin: 8px 0 4px;
  font-size: 14px;
  font-weight: 500;
  color: #1d1d1f;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pn-card.is-unread .pn-card-title {
  font-weight: 600;
}

.pn-card-content {
  margin: 0;
  font-size: 12.5px;
  color: #6e6e73;
  line-height: 1.55;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.pn-card-foot {
  margin-top: 9px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.pn-time {
  font-family: $font-num;
  font-size: 11px;
  letter-spacing: 0.04em;
  color: #a1a1a6;
  font-variant-numeric: tabular-nums;
}

.pn-unread-dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--accent);
  box-shadow: 0 0 8px rgba(0, 0, 0, 0.18);
  box-shadow: 0 0 8px color-mix(in srgb, var(--accent) 60%, transparent);
}
</style>

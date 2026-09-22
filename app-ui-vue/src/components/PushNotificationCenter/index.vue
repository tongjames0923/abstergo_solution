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
      <center-header
        :unread-count="unreadCount"
        :total="messages.length"
        @mark-all-read="handleAllRead"
        @clear="handleClear"
      />
      <filter-bar
        :tabs="typeTabs"
        :active-type="activeType"
        :unreadonly="unreadOnly"
        :unread-count="unreadCount"
        :total="messages.length"
        @change-type="handleChangeType"
        @toggle-unread="handleToggleUnread"
      />
      <list-state
        v-if="stateMode"
        :mode="stateMode"
        @retry="handleRetry"
        @clear-filter="handleClearFilter"
      />
      <message-list
        v-else
        :groups="groupedMessages"
        :has-more="hasMore"
        :loading-more="loadingMore"
        @load-more="handleLoadMore"
        @item-click="handleItemClick"
        @item-mark-read="handleMarkRead"
      />
    </div>
  </el-drawer>
</template>

<script>
import {mapActions, mapGetters} from 'vuex'
import {operateMessage} from '@/api/push-message'
import {groupByDay, resolveTypeKey, typeAccent} from '@/push/message-display'
import CenterHeader from './components/CenterHeader'
import FilterBar from './components/FilterBar'
import MessageList from './components/MessageList'
import ListState from './components/ListState'

/**
 * 消息中心抽屉（Liquid Glass 玻璃拟态）· 编排层
 * <p>
 * 职责边界：本组件只负责抽屉外壳、筛选状态（activeType / unreadOnly 属视图关注点，
 * 持有在组件 data，不进 Vuex 以免污染单一数据源）与子组件编排；数据仍以 push Vuex
 * 模块为单一来源。类型 Tab / 只看未读 / 今天-昨天-更早时间分组均为客户端派生
 * （filteredMessages → groupByDay，computed 缓存，O(n)），与 seenIds 去重、cursor
 * 游标分页的单一数据流互不干扰。
 * 交互沿旧链路：卡片点击标记已读并按需回传 CLICK 后路由跳转（vue-router 3 回调式
 * push，规避 Promise 链式调用的 TypeError）、全部已读批量回传 READ、底部按钮与
 * 滚动近底部双通道加载更多、打开时主动拉取最新一页兜底。
 * 三态：首屏骨架（loading 且无数据）、错误重试（loadError 且无数据）、空态（无消息
 * 与筛选无结果区分，后者提供一键清除筛选）。响应式三档：手机 100% 全屏 + 底部
 * 操作栏 / 平板 480px / 桌面 400px，经 matchMedia 驱动，与 variables.scss 断点一致。
 * </p>
 */

/**
 * 已知类型 Tab 常驻顺序（后端 msgType 字典扩展出的未知类型归 other，动态出现）
 */
const KNOWN_TYPE_KEYS = ['notify', 'warning', 'reminder']

export default {
  name: 'PushNotificationCenter',
  components: {
    CenterHeader,
    FilterBar,
    MessageList,
    ListState
  },
  props: {
    /**
     * 抽屉可见性（.sync 双向）
     */
    visible: {
      type: Boolean,
      default: false
    }
  },
  data() {
    return {
      /**
       * 当前选中的类型 Tab 键（'all' 或归组后的展示类型键）
       */
      activeType: 'all',
      /**
       * 是否只看未读（客户端派生过滤）
       */
      unreadOnly: false,
      /**
       * 视口档位（matchMedia 驱动）：phone / tablet / desktop
       */
      viewport: 'desktop'
    }
  },
  computed: {
    ...mapGetters('push', [
      'pushMessages',
      'pushUnreadCount',
      'pushHasMore',
      'pushLoadingMore',
      'pushLoading',
      'pushLoadError'
    ]),
    messages() {
      return this.pushMessages
    },
    unreadCount() {
      return this.pushUnreadCount
    },
    hasMore() {
      return this.pushHasMore
    },
    loadingMore() {
      return this.pushLoadingMore
    },
    loading() {
      return this.pushLoading
    },
    loadError() {
      return this.pushLoadError
    },
    /**
     * 抽屉宽度三档：手机全屏 / 平板 480px / 桌面 400px
     */
    drawerSize() {
      if (this.viewport === 'phone') {
        return '100%'
      }
      if (this.viewport === 'tablet') {
        return '480px'
      }
      return '400px'
    },
    /**
     * 是否存在未知类型消息（决定“其他”Tab 是否出现）
     */
    hasOtherType() {
      return this.messages.some(item => resolveTypeKey(item.type) === 'other')
    },
    /**
     * 类型 Tab 列表：全部 + 三个已知类型常驻，“其他”仅实际存在时动态出现；
     * 各 Tab 附未读角标与语义色
     */
    typeTabs() {
      const keys = this.hasOtherType ? KNOWN_TYPE_KEYS.concat(['other']) : KNOWN_TYPE_KEYS
      const tabs = [{
        key: 'all',
        label: this.$t('push.center.filter.all'),
        accent: typeAccent('notify'),
        unread: this.unreadCount
      }]
      keys.forEach(key => {
        tabs.push({
          key,
          label: this.$t('push.type.' + key),
          accent: typeAccent(key),
          unread: this.messages.filter(item => !item.read && resolveTypeKey(item.type) === key).length
        })
      })
      return tabs
    },
    /**
     * 当前筛选下的消息子集（类型 + 未读双条件客户端派生）
     */
    filteredMessages() {
      return this.messages.filter(message => {
        if (this.activeType !== 'all' && resolveTypeKey(message.type) !== this.activeType) {
          return false
        }
        if (this.unreadOnly && message.read) {
          return false
        }
        return true
      })
    },
    /**
     * 时间分组结果（今天 / 昨天 / 更早，到达序分桶）
     */
    groupedMessages() {
      return groupByDay(this.filteredMessages)
    },
    /**
     * 列表区三态模式：非空串时以 ListState 替换消息流
     */
    stateMode() {
      if (this.loading && !this.messages.length) {
        return 'loading'
      }
      if (this.loadError && !this.messages.length) {
        return 'error'
      }
      if (!this.messages.length) {
        return 'empty'
      }
      if (!this.filteredMessages.length) {
        return 'filtered-empty'
      }
      return ''
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
  mounted() {
    this.mqPhone = window.matchMedia('(max-width: 768px)')
    this.mqTablet = window.matchMedia('(max-width: 992px)')
    this.updateViewport()
    if (typeof this.mqPhone.addEventListener === 'function') {
      this.mqPhone.addEventListener('change', this.updateViewport)
      this.mqTablet.addEventListener('change', this.updateViewport)
    } else {
      this.mqPhone.addListener(this.updateViewport)
      this.mqTablet.addListener(this.updateViewport)
    }
  },
  beforeDestroy() {
    if (typeof this.mqPhone.removeEventListener === 'function') {
      this.mqPhone.removeEventListener('change', this.updateViewport)
      this.mqTablet.removeEventListener('change', this.updateViewport)
    } else {
      this.mqPhone.removeListener(this.updateViewport)
      this.mqTablet.removeListener(this.updateViewport)
    }
  },
  methods: {
    ...mapActions('push', ['markRead', 'markAllRead', 'clearAll', 'pullLatest', 'loadMore', 'retry']),
    /**
     * 视口档位刷新（matchMedia change 驱动，与 $bp-phone/$bp-tablet 断点一致）
     */
    updateViewport() {
      if (this.mqPhone.matches) {
        this.viewport = 'phone'
      } else if (this.mqTablet.matches) {
        this.viewport = 'tablet'
      } else {
        this.viewport = 'desktop'
      }
    },
    handleClose() {
      this.$emit('update:visible', false)
    },
    /**
     * 单条点击：未读即时标记（READ 回传由 markRead 处理）；
     * 有跳转路由时回传 CLICK 操作日志（后端自动补记 READ）并跳转。
     * 路由跳转用 onComplete/onAbort 回调承接——vue-router 3.0.6 的 push 不返回 Promise，
     * Promise 链式 .catch 会导致 TypeError，回跳重复路由由 onAbort 静默吞掉
     */
    handleItemClick(item) {
      if (!item.read) {
        this.markRead(item.id)
      }
      if (item.url) {
        this.reportClick(item)
        this.$router.push(item.url, () => {}, () => {})
        this.handleClose()
      }
    },
    /**
     * 回传 CLICK 操作日志（仅新链路数字 id；异常隔离不影响跳转）
     */
    reportClick(item) {
      if (item.id == null || !/^\d+$/.test(String(item.id))) {
        return
      }
      operateMessage({
        messageId: Number(item.id),
        opType: 'CLICK',
        extInfo: 'message-center'
      }).catch(e => console.warn('[push-center] 点击回传失败', e))
    },
    /**
     * 标记单条已读（按钮显式触发，异常隔离不影响其余交互）
     */
    handleMarkRead(item) {
      if (item.read) {
        return
      }
      this.markRead(item.id)
    },
    handleAllRead() {
      this.markAllRead()
    },
    handleClear() {
      this.clearAll()
    },
    handleLoadMore() {
      this.loadMore()
    },
    handleRetry() {
      this.retry()
    },
    handleClearFilter() {
      this.activeType = 'all'
      this.unreadOnly = false
    },
    handleChangeType(key) {
      this.activeType = key
    },
    handleToggleUnread(unreadOnly) {
      this.unreadOnly = unreadOnly
    }
  }
}
</script>

<!-- 面板外壳：append-to-body 后需在全局命名空间下覆盖（Liquid Glass 毛玻璃底，与全站一致） -->
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
  color: #1d1d1f;

  /* 内建头与内边距让位给自定义头部；body 走 flex 纵向布局并锁死溢出，滚动交给 .pn-list */
  .el-drawer__header {
    display: none;
  }
  .el-drawer__body {
    padding: 0;
    flex: 1;
    min-height: 0;
    height: auto;
    overflow: hidden;
  }
}
</style>

<style lang="scss" scoped>
.pn-root {
  height: 100%;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  font-family: 'PingFang SC', -apple-system, 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif;
  color: #1d1d1f;
}

/* 手机端：为 CenterHeader 内固定的底部操作栏预留空间（含安全区） */
@media (max-width: 768px) {
  .pn-root {
    padding-bottom: calc(64px + env(safe-area-inset-bottom, 0px));
  }
}
</style>

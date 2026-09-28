import { initPush, closePush, CONNECTION_STATUS, notifyMessage, dispatchMessage } from '@/push'
import { getToken } from '@/utils/auth'
import { TOPIC_PREFIX, normalizeFetchedMessage } from '@/push/push-message'
import { fetchMessages, markRead } from '@/api/push-message'

/**
 * 推送消息 Vuex 模块
 * <p>
 * 单一数据源：消息列表、已读状态、连接状态均收敛于此；去重用模块级 seenIds（非响应式，
 * 仅作幂等判断）避免重连/重发导致的重复弹窗与重复入列。未读计数由 messages 派生。
 * 新链路（信令+拉取）：SSE 仅触发 SIGNAL 拉取，消息内容由 pullLatest 拉取后去重入列，
 * 再经门面渲染器（默认弹窗）与业务处理器分发；本模块不直接依赖 SSE 数据帧。
 * </p>
 */

/**
 * 内存保留的最大消息条数（防止长期在线内存膨胀）
 */
const MAX_MESSAGES = 200

/**
 * 已见消息 id 集合（非响应式，仅用于去重）
 */
const seenIds = new Set()

/**
 * SIGNAL 拉取防抖定时器（合并信令风暴与 hello 兜底）
 */
let pullTimer = null

/**
 * 是否已完成首次拉取（hello 兜底）：首拉仅入列不弹窗，避免登录即弹出一批历史通知
 */
let firstPullDone = false

/**
 * 拉取防抖间隔（毫秒），对齐设计文档 2.3 触发条件 A
 */
const PULL_DEBOUNCE = 300

/**
 * 已读状态持久化扩展点（本轮为空实现，维持内存态）
 * <p>
 * 后续如需已读状态跨会话保留（刷新/重登不丢），可在此接入 localStorage
 * 或后端已读同步接口；当前刷新后已读状态随内存重置。
 * </p>
 *
 * @param {Array<Object>} messages 当前全部消息（含最新已读标记）
 */
function persistRead(messages) {
  // 扩展点：按需持久化已读状态，本轮不实现
}

const state = () => ({
  connectionStatus: CONNECTION_STATUS.IDLE,
  messages: [],
  cursor: null,
  hasMore: false,
  loadingMore: false,
  /**
   * 首屏/刷新拉取进行中（消息中心骨架屏依据）
   */
  loading: false,
  /**
   * 最近一次拉取失败标记（消息中心错误态与重试依据）
   */
  loadError: false
})

const mutations = {
  SET_STATUS(state, status) {
    state.connectionStatus = status
  },
  ADD_MESSAGE(state, message) {
    state.messages.unshift(message)
    if (state.messages.length > MAX_MESSAGES) {
      const overflow = state.messages.splice(MAX_MESSAGES)
      overflow.forEach(m => seenIds.delete(m.id))
    }
  },
  MARK_READ(state, id) {
    const target = state.messages.find(item => item.id === id)
    if (target) {
      target.read = true
      persistRead(state.messages)
    }
  },
  MARK_ALL_READ(state) {
    state.messages.forEach(item => { item.read = true })
    persistRead(state.messages)
  },
  CLEAR_ALL(state) {
    state.messages = []
  },
  RESET(state) {
    state.messages = []
    state.connectionStatus = CONNECTION_STATUS.IDLE
    state.cursor = null
    state.hasMore = false
    state.loading = false
    state.loadError = false
    seenIds.clear()
  },
  /**
   * 追加更早的历史消息（游标分页加载，到达序追加到列表尾部）
   */
  APPEND_MESSAGES(state, messages) {
    messages.forEach(message => state.messages.push(message))
    if (state.messages.length > MAX_MESSAGES) {
      const overflow = state.messages.splice(MAX_MESSAGES)
      overflow.forEach(m => seenIds.delete(m.id))
    }
  },
  SET_PAGE(state, { cursor, hasMore }) {
    state.cursor = cursor != null ? cursor : null
    state.hasMore = Boolean(hasMore)
  },
  SET_LOADING_MORE(state, loading) {
    state.loadingMore = Boolean(loading)
  },
  SET_LOADING(state, loading) {
    state.loading = Boolean(loading)
  },
  SET_LOAD_ERROR(state, error) {
    state.loadError = Boolean(error)
  }
}

const getters = {
  pushConnectionStatus: state => state.connectionStatus,
  pushMessages: state => state.messages,
  pushUnreadCount: state => state.messages.filter(item => !item.read).length,
  pushHasMore: state => state.hasMore,
  pushCursor: state => state.cursor,
  pushLoadingMore: state => state.loadingMore,
  pushLoading: state => state.loading,
  pushLoadError: state => state.loadError
}

const actions = {
  /**
   * 启动推送连接：取登录态 token 与数字 id 委托门面建连；
   * 仅订阅 SIGNAL 信令频道（tbs.push.channel.{userId}），业务消息由信令触发拉取获得
   */
  startConnection({ commit, dispatch, rootState }) {
    const token = getToken()
    const userId = rootState.user.userId
    if (!token || !userId) {
      return
    }
    // SIGNAL 信令/hello 兜底统一经防抖触发拉取（合并信令风暴）
    const triggerPull = () => {
      if (pullTimer) {
        clearTimeout(pullTimer)
      }
      pullTimer = setTimeout(() => {
        dispatch('pullLatest').catch(e => console.warn('[push] 拉取消息失败', e))
      }, PULL_DEBOUNCE)
    }
    initPush({
      token,
      userId,
      topics: [TOPIC_PREFIX + userId].filter(Boolean),
      onStatus: status => commit('SET_STATUS', status),
      onSignal: triggerPull
    })
    // 登录即先拉一遍：不等待 SSE 握手（hello 慢/解析失败时不至于空窗），
    // 与 hello 兜底/SIGNAL 触发的后续拉取经 seenIds 去重，幂等无害
    dispatch('pullLatest').catch(e => console.warn('[push] 登录首轮拉取失败', e))
  },
  /**
   * 停止推送连接（登出 / 页面卸载）
   * <p>
   * 关闭连接同时复位模块态：清空消息与 seenIds、复位 firstPullDone、取消挂起的拉取防抖，
   * 避免同会话内换账号重登后，旧账号消息残留或首轮拉取被误判为非首拉而批量弹窗。
   * </p>
   */
  stopConnection({ commit }) {
    closePush()
    if (pullTimer) {
      clearTimeout(pullTimer)
      pullTimer = null
    }
    firstPullDone = false
    commit('RESET')
    commit('SET_STATUS', CONNECTION_STATUS.CLOSED)
  },
  /**
   * 拉取当前用户消息第一页并合并入列（SIGNAL 信令/hello 兜底触发）
   * <p>
   * 按 messageId 去重（seenIds 幂等）；拉取回来的 readFlag 直接决定本地已读态；
   * 游标与 hasMore 写入 state 供消息中心分页加载。
   * 新增消息依次触发渲染器（默认弹窗，弹窗查看/关闭回传已读）与业务处理器；
   * 首次拉取（hello 兜底）仅入列不弹窗，避免登录即弹出一批历史通知。
   * 拉取期间置 loading、失败置 loadError，供消息中心骨架屏与错误重试呈现。
   * </p>
   */
  async pullLatest({ commit, dispatch }) {
    commit('SET_LOADING', true)
    try {
      const data = await fetchMessages({ limit: 20, readFlag: 'all' })
      const list = (data && data.list) || []
      const isFirst = !firstPullDone
      firstPullDone = true
      let added = 0
      list.forEach(vo => {
        const message = normalizeFetchedMessage(vo)
        if (!message || seenIds.has(message.id)) {
          return
        }
        seenIds.add(message.id)
        commit('ADD_MESSAGE', { ...message, receivedAt: Date.now() })
        added++
        if (!isFirst) {
          notifyMessage(message, { onRead: () => dispatch('markRead', message.id) })
          dispatchMessage(message)
        }
      })
      commit('SET_PAGE', { cursor: data.cursor, hasMore: data.hasMore })
      commit('SET_LOAD_ERROR', false)
      if (added > 0) {
        console.info(`[push] 拉取新增 ${added} 条消息`)
      }
    } catch (e) {
      commit('SET_LOAD_ERROR', true)
      console.warn('[push] 拉取消息失败', e)
    } finally {
      commit('SET_LOADING', false)
    }
  },
  /**
   * 首屏拉取失败后的用户触发重试：复位错误态后重发 pullLatest
   */
  retry({ commit, dispatch }) {
    commit('SET_LOAD_ERROR', false)
    return dispatch('pullLatest')
  },
  /**
   * 游标加载更早的历史消息（消息中心分页）
   * <p>
   * 以 state.cursor（上一页响应返回的接收行ID）请求下一页，规范化并按 seenIds 去重后
   * 追加到列表尾部；游标与 hasMore 以本页响应为准。无更多或加载中时幂等返回。
   * </p>
   */
  async loadMore({ state, commit }) {
    if (!state.hasMore || state.loadingMore) {
      return
    }
    commit('SET_LOADING_MORE', true)
    try {
      const data = await fetchMessages({ cursor: state.cursor, limit: 20, readFlag: 'all' })
      const list = (data && data.list) || []
      const normalized = []
      list.forEach(vo => {
        const message = normalizeFetchedMessage(vo)
        if (!message || seenIds.has(message.id)) {
          return
        }
        seenIds.add(message.id)
        normalized.push(message)
      })
      commit('APPEND_MESSAGES', normalized)
      commit('SET_PAGE', { cursor: data.cursor, hasMore: data.hasMore })
    } catch (e) {
      console.warn('[push] 加载更多消息失败', e)
    } finally {
      commit('SET_LOADING_MORE', false)
    }
  },
  /**
   * 记录单条已读：本地即时反馈，并异步回传后端写 READ 日志
   */
  async markRead({ commit }, id) {
    commit('MARK_READ', id)
    if (id != null && /^\d+$/.test(String(id))) {
      try {
        await markRead([Number(id)])
      } catch (e) {
        console.warn('[push] 标记已读回传失败', id, e)
      }
    }
  },
  /**
   * 全部已读：本地全标，并异步回传后端（仅数字 id 视为新链路消息）
   */
  async markAllRead({ commit, state }) {
    const ids = state.messages.filter(item => !item.read && /^\d+$/.test(String(item.id))).map(item => Number(item.id))
    commit('MARK_ALL_READ')
    if (ids.length) {
      try {
        await markRead(ids)
      } catch (e) {
        console.warn('[push] 全部已读回传失败', e)
      }
    }
  },
  clearAll({ commit }) {
    commit('CLEAR_ALL')
  },
  reset({ commit }) {
    firstPullDone = false
    commit('RESET')
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  getters,
  actions
}

import { initPush, closePush, CONNECTION_STATUS } from '@/push'
import { getToken } from '@/utils/auth'

/**
 * 推送消息 Vuex 模块
 * <p>
 * 单一数据源：消息列表、已读状态、连接状态均收敛于此；去重用模块级 seenIds（非响应式，
 * 仅作幂等判断）避免重连/重发导致的重复弹窗与重复入列。未读计数由 messages 派生。
 * 消息呈现与业务处理不在本模块内：连接与分发统一走门面 {@link module:@/push}（initPush），
 * 弹窗由默认渲染器负责，本模块仅在 onMessage 中完成去重与入列。
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
  messages: []
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
    seenIds.clear()
  }
}

const getters = {
  pushConnectionStatus: state => state.connectionStatus,
  pushMessages: state => state.messages,
  pushUnreadCount: state => state.messages.filter(item => !item.read).length
}

const actions = {
  /**
   * 启动推送连接：取登录态 token 与 userName 委托门面建连；
   * onMessage 仅做去重与入列（重复消息返回 false 交由门面终止后续渲染与处理器分发）
   */
  startConnection({ commit, rootState }) {
    const token = getToken()
    const userName = rootState.user.userName
    if (!token || !userName) {
      return
    }
    initPush({
      token,
      userName,
      onStatus: status => commit('SET_STATUS', status),
      onMessage: message => {
        if (seenIds.has(message.id)) {
          return false
        }
        seenIds.add(message.id)
        const full = { ...message, read: false, receivedAt: Date.now() }
        commit('ADD_MESSAGE', full)
        return true
      },
      onRead: message => commit('MARK_READ', message.id)
    })
  },
  /**
   * 停止推送连接（登出 / 页面卸载）
   */
  stopConnection({ commit }) {
    closePush()
    commit('SET_STATUS', CONNECTION_STATUS.CLOSED)
  },
  markRead({ commit }, id) {
    commit('MARK_READ', id)
  },
  markAllRead({ commit }) {
    commit('MARK_ALL_READ')
  },
  clearAll({ commit }) {
    commit('CLEAR_ALL')
  },
  reset({ commit }) {
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

import Vue from 'vue'

/**
 * 多标签页（页签栏）状态模块
 *
 * 记录已打开的页签，供顶部页签栏渲染与关闭操作。页签以 $route.fullPath 为唯一键：
 * 同一地址（含 query）只保留一个页签，带路径参数的详情页天然各占一个页签。
 *
 * refreshSeq 记录每个地址的刷新序号，AppMain 把它拼进页面组件的 key，
 * 页签右键「刷新页面」时递增序号即可强制重建组件实例（无需 /redirect 中转路由）。
 *
 * ponytail: 页面实例由 AppMain 的 keep-alive 按 max 上限回收，关闭页签不会立刻销毁其缓存实例；
 * 升级路径是给页面组件补 name 并改用 keep-alive 的 include 精确回收。
 */

const state = {
  // 已打开页签：[{ title, fullPath }]
  visitedViews: [],
  // 地址刷新序号，用于强制重建页面实例
  refreshSeq: {}
}

const mutations = {
  /**
   * 登记当前路由为页签
   * @param {Object} state 模块状态
   * @param {Object} view 当前路由对象，无 meta.title 的路由（404/403/登录回调等）不登记
   */
  ADD_VIEW: (state, view) => {
    if (!view.meta || !view.meta.title) {
      return
    }
    if (state.visitedViews.some(v => v.fullPath === view.fullPath)) {
      return
    }
    state.visitedViews.push({ title: view.meta.title, fullPath: view.fullPath })
  },

  /**
   * 关闭指定页签
   * @param {Object} state 模块状态
   * @param {Object} view 页签对象（含 fullPath）
   */
  DEL_VIEW: (state, view) => {
    state.visitedViews = state.visitedViews.filter(v => v.fullPath !== view.fullPath)
  },

  /**
   * 只保留指定页签
   * @param {Object} state 模块状态
   * @param {Object} view 保留的页签对象
   */
  DEL_OTHERS_VIEWS: (state, view) => {
    state.visitedViews = state.visitedViews.filter(v => v.fullPath === view.fullPath)
  },

  /**
   * 关闭指定页签左侧的全部页签
   * @param {Object} state 模块状态
   * @param {Object} view 基准页签对象
   */
  DEL_LEFT_VIEWS: (state, view) => {
    const index = state.visitedViews.findIndex(v => v.fullPath === view.fullPath)
    if (index > 0) {
      state.visitedViews.splice(0, index)
    }
  },

  /**
   * 关闭指定页签右侧的全部页签
   * @param {Object} state 模块状态
   * @param {Object} view 基准页签对象
   */
  DEL_RIGHT_VIEWS: (state, view) => {
    const index = state.visitedViews.findIndex(v => v.fullPath === view.fullPath)
    if (index >= 0) {
      state.visitedViews.splice(index + 1)
    }
  },

  /**
   * 清空全部页签（登出、全部关闭时调用）
   * @param {Object} state 模块状态
   */
  DEL_ALL_VIEWS: (state) => {
    state.visitedViews = []
    state.refreshSeq = {}
  },

  /**
   * 递增页签地址的刷新序号
   *
   * 首次刷新会给对象新增键，Vue 2 的普通属性新增不是响应式的，必须走 Vue.set，
   * 否则 AppMain 的缓存键不会重算，页面实例不会重建。
   * @param {Object} state 模块状态
   * @param {Object} view 页签对象
   */
  BUMP_REFRESH: (state, view) => {
    Vue.set(state.refreshSeq, view.fullPath, (state.refreshSeq[view.fullPath] || 0) + 1)
  }
}

export default {
  namespaced: true,
  state,
  mutations
}

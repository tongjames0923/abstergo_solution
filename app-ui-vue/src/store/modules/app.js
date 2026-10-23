import Cookies from 'js-cookie'
import { resolveDevice } from '@/utils/device'

// 首屏即按实际视口给出正确档位，不让设备态从 desktop 开始再被 mounted 纠正
const initialDevice = resolveDevice()

const state = {
  sidebar: {
    // 非桌面档侧边栏是抽屉，首屏应为收起态，否则手机会先闪一下桌面双栏布局
    opened: initialDevice === 'desktop'
      ? (Cookies.get('sidebarStatus') ? !!+Cookies.get('sidebarStatus') : true)
      : false,
    withoutAnimation: false
  },
  device: initialDevice,
  // 当前横竖屏方向：portrait（竖屏）/ landscape（横屏）
  orientation: 'portrait',
  language: Cookies.get('language') || 'zh'
}

const mutations = {
  TOGGLE_SIDEBAR: state => {
    state.sidebar.opened = !state.sidebar.opened
    state.sidebar.withoutAnimation = false
    if (state.sidebar.opened) {
      Cookies.set('sidebarStatus', 1)
    } else {
      Cookies.set('sidebarStatus', 0)
    }
  },
  CLOSE_SIDEBAR: (state, withoutAnimation) => {
    Cookies.set('sidebarStatus', 0)
    state.sidebar.opened = false
    state.sidebar.withoutAnimation = withoutAnimation
  },
  TOGGLE_DEVICE: (state, device) => {
    state.device = device
  },
  SET_ORIENTATION: (state, orientation) => {
    state.orientation = orientation
  },
  SET_LANGUAGE: (state, language) => {
    state.language = language
    Cookies.set('language', language)
  }
}

const actions = {
  toggleSideBar({ commit }) {
    commit('TOGGLE_SIDEBAR')
  },
  closeSideBar({ commit }, { withoutAnimation }) {
    commit('CLOSE_SIDEBAR', withoutAnimation)
  },
  toggleDevice({ commit }, device) {
    commit('TOGGLE_DEVICE', device)
  },
  setOrientation({ commit }, orientation) {
    commit('SET_ORIENTATION', orientation)
  },
  setLanguage({ commit }, language) {
    commit('SET_LANGUAGE', language)
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}

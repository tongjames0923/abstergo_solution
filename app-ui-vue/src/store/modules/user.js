import {getInfo, login, logout} from '@/api/user'
import {getRememberMe, getToken, removeToken, setRememberMe, setToken} from '@/utils/auth'
import {resetRouter} from '@/router'
import request from '@/utils/request'

const getDefaultState = () => {
  return {
    token: getToken(),
    refreshToken: null,
    name: '',
    avatar: '',
    roles: [],
    routes: [] // 存储动态路由
  }
}

const state = getDefaultState()

const mutations = {
  RESET_STATE: (state) => {
    Object.assign(state, getDefaultState())
  },
  SET_TOKEN: (state, token) => {
    state.token = token
  },
  SET_NAME: (state, name) => {
    state.name = name
  },
  SET_AVATAR: (state, avatar) => {
    state.avatar = avatar
  },
  SET_ROLES: (state, roles) => {
    state.roles = roles
  },
  SET_ROUTES: (state, routes) => {
    state.routes = routes
  }
}

const actions = {
  // user login
  async login({ commit }, userInfo) {
    try {
      const { username, password, rememberMe } = userInfo
      const { data } = await login({ username: username.trim(), password: password })
      commit('SET_TOKEN', data.token)
      setRememberMe(rememberMe)
      // 根据rememberMe选项设置Token存储方式
      setToken(data.token, rememberMe)
      return data
    } catch (error) {
      throw error
    }
  },

  // get user info
  async getInfo({ commit, state }) {
    try {
      const response = await getInfo()
      const { data } = response

      if (!data) {
        throw new Error('Verification failed, please Login again.')
      }

      const { nickName, avatar, avaliableRoles } = data

      commit('SET_NAME', nickName)
      commit('SET_AVATAR', avatar)
      commit('SET_ROLES', avaliableRoles)
      return data
    } catch (error) {
      throw error
    }
  },

  // user logout
  async logout({ commit, state }) {
    try {
      await logout()
      removeToken() // must remove  token  first
      resetRouter()
      commit('RESET_STATE')
    } catch (error) {
      throw error
    }
  },

  // remove token
  async resetToken({ commit }) {
    removeToken() // must remove  token  first
    commit('RESET_STATE')
  },

  /**
   * 刷新Token
   * @param commit
   * @returns {Promise<any>}
   */
  async refreshToken({ commit }) {
    try {
      // 添加超时控制，避免长时间等待
      const controller = new AbortController()
      const timeoutId = setTimeout(() => controller.abort(), 10000) // 10秒超时

      const response = await request({
        url: 'public/user/refresh',
        method: 'get',
        signal: controller.signal
      })

      clearTimeout(timeoutId)

      const { code, data } = response
      setToken(data.token)
      commit('SET_TOKEN', data.token)
      setToken(data.token, getRememberMe())
      return data
    } catch (error) {
      // 检查是否是超时错误
      if (error.name === 'AbortError') {
        throw new Error('Token刷新超时，请检查网络连接')
      }
      throw error
    }
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}

import {getInfo, login, logout, setCurrentDepartment} from '@/api/user'
import {getRememberMe, getToken, removeToken, setRememberMe, setToken} from '@/utils/auth'
import {resetRouter} from '@/router'
import request from '@/utils/request'
import routeCacheService from '@/utils/route-cache'

const getDefaultState = () => {
  return {
    token: getToken(),
    refreshToken: null,
    name: '',
    userName: '',
    avatar: '',
    roles: [],
    departments: [],
    currentDepartment: null,
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
  SET_USER_NAME: (state, userName) => {
    state.userName = userName
  },
  SET_AVATAR: (state, avatar) => {
    state.avatar = avatar
  },
  SET_ROLES: (state, roles) => {
    state.roles = roles
  },
  SET_DEPARTMENTS: (state, departments) => {
    state.departments = departments
  },
  SET_CURRENT_DEPARTMENT: (state, department) => {
    state.currentDepartment = department
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

      const { nickName, userName, avatar, avaliableRoles, avaliableDepartments } = data

      commit('SET_NAME', nickName)
      commit('SET_USER_NAME', userName)
      commit('SET_AVATAR', avatar)
      commit('SET_ROLES', avaliableRoles)
      commit('SET_DEPARTMENTS', avaliableDepartments || [])
      commit('SET_CURRENT_DEPARTMENT', data.currentDepartment || null)
      return data
    } catch (error) {
      throw error
    }
  },

  // 切换当前部门
  async switchDepartment({ commit }, departmentId) {
    try {
      await setCurrentDepartment(departmentId)
      // 刷新用户信息以获取最新的 currentDepartment
      const response = await getInfo()
      const { data } = response
      if (data) {
        commit('SET_CURRENT_DEPARTMENT', data.currentDepartment || null)
        commit('SET_DEPARTMENTS', data.avaliableDepartments || [])
      }
    } catch (error) {
      throw error
    }
  },

  // user logout
  async logout({ commit, state }) {
    try {
      await logout()
    } catch (error) {
      console.error('Logout API request failed:', error)
    } finally {
      removeToken() // must remove  token  first
      resetRouter()
      
      // 清除路由缓存
      routeCacheService.clearCache()
      
      commit('RESET_STATE')
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

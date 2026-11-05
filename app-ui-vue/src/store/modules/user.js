import {getInfo, login, logout, setCurrentDepartment} from '@/api/user'
import {getRememberMe, getToken, removeToken, setRememberMe, setToken} from '@/utils/auth'
import {resetRouter} from '@/router'
import {resetDynamicRoutesLoaded} from '@/permission'
import request from '@/utils/request'
import routeCacheService from '@/utils/route-cache'

const getDefaultState = () => {
  return {
    token: getToken(),
    refreshToken: null,
    name: '',
    userName: '',
    userId: null,
    avatar: '',
    roles: [],
    roleGroups: [],
    departments: [],
    currentDepartment: null,
    routes: [],
    accountStatus: null,
    accountStatusStr: ''
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
  SET_USER_ID: (state, userId) => {
    state.userId = userId
  },
  SET_AVATAR: (state, avatar) => {
    state.avatar = avatar
  },
  SET_ROLES: (state, roles) => {
    state.roles = roles
  },
  SET_ROLE_GROUPS: (state, roleGroups) => {
    state.roleGroups = roleGroups
  },
  SET_DEPARTMENTS: (state, departments) => {
    state.departments = departments
  },
  SET_CURRENT_DEPARTMENT: (state, department) => {
    state.currentDepartment = department
  },
  SET_ROUTES: (state, routes) => {
    state.routes = routes
  },
  SET_ACCOUNT_STATUS: (state, status) => {
    state.accountStatus = status
  },
  SET_ACCOUNT_STATUS_STR: (state, statusStr) => {
    state.accountStatusStr = statusStr
  }
}

const actions = {
  // user login
  async login({ commit }, userInfo) {
    const { username, password, rememberMe } = userInfo
    const { data } = await login({ username: username.trim(), password: password })
    commit('SET_TOKEN', data.token)
    setRememberMe(rememberMe)
    // 根据rememberMe选项设置Token存储方式
    setToken(data.token, rememberMe)
    return data
  },

  /**
   * 第三方登录（SSO）Token 收口：复用与账号密码登录相同的后置链路
   * 仅新增动作，不改变既有 login 动作的签名与语义
   * @param {Object} tokenInfo - SSO 换票结果
   * @param {string} tokenInfo.token - 后端签发的访问令牌
   * @param {boolean} [tokenInfo.rememberMe] - 是否记住登录态
   * @returns {Promise<Object>} 收口后的 token 信息
   */
  async loginByToken({ commit }, tokenInfo) {
    const { token, rememberMe } = tokenInfo
    if (rememberMe) {
      setRememberMe(rememberMe)
    }
    setToken(token, rememberMe)
    commit('SET_TOKEN', token)
    return tokenInfo
  },

  // get user info
  async getInfo({ commit, dispatch, state }) {
    const response = await getInfo()
    const { data } = response

    if (!data) {
      throw new Error('Verification failed, please Login again.')
    }

    const { id, nickName, userName, avatar, avaliableRoles, avaliableDepartments, accountStatus, accountStatusStr } = data

    // 后端返回 List<SystemRoleGroupVO>，需拆分为权限组列表和展平的权限项列表
    const roleGroups = avaliableRoles || []
    const flatPermissions = []
    roleGroups.forEach(vo => {
      if (vo.permissions && Array.isArray(vo.permissions)) {
        vo.permissions.forEach(perm => flatPermissions.push(perm))
      }
    })

    commit('SET_NAME', nickName)
    commit('SET_USER_NAME', userName)
    commit('SET_USER_ID', id)
    commit('SET_AVATAR', avatar)
    commit('SET_ROLES', flatPermissions)
    commit('SET_ROLE_GROUPS', roleGroups)
    commit('SET_DEPARTMENTS', avaliableDepartments || [])
    commit('SET_CURRENT_DEPARTMENT', data.currentDepartment || null)
    commit('SET_ACCOUNT_STATUS', accountStatus)
    commit('SET_ACCOUNT_STATUS_STR', accountStatusStr || '')

    // 用户态就绪后启动推送连接（登录成功自动建连并订阅 per-user 主题）
    // user 模块为 namespaced，跨模块分发必须显式 root 作用域，否则被解析为 user/push/... 而失效
    dispatch('push/startConnection', null, { root: true })
    return data
  },

  // 切换当前部门
  async switchDepartment({ commit }, departmentId) {
    await setCurrentDepartment(departmentId)
    // 刷新用户信息以获取最新的 currentDepartment
    const response = await getInfo()
    const { data } = response
    if (data) {
      const roleGroups = data.avaliableRoles || []
      const flatPermissions = []
      roleGroups.forEach(vo => {
        if (vo.permissions && Array.isArray(vo.permissions)) {
          vo.permissions.forEach(perm => flatPermissions.push(perm))
        }
      })
      commit('SET_CURRENT_DEPARTMENT', data.currentDepartment || null)
      commit('SET_DEPARTMENTS', data.avaliableDepartments || [])
      commit('SET_ROLES', flatPermissions)
      commit('SET_ROLE_GROUPS', roleGroups)
    }
  },

  // user logout
  async logout({ commit, dispatch, state }) {
    try {
      await logout()
    } catch (error) {
      console.error('Logout API request failed:', error)
    } finally {
      // 先关闭推送长连接，避免登出后继续占用会话（跨模块分发须显式 root 作用域）
      dispatch('push/stopConnection', null, { root: true })
      removeToken() // must remove  token  first
      resetRouter()
      resetDynamicRoutesLoaded()

      // 清除路由缓存
      routeCacheService.clearCache()

      // 清空页签，避免换账号后仍保留上一个用户的页签
      commit('tagsView/DEL_ALL_VIEWS', null, { root: true })

      commit('RESET_STATE')
    }
  },

  // remove token
  async resetToken({ commit }) {
    removeToken() // must remove  token  first
    commit('tagsView/DEL_ALL_VIEWS', null, { root: true })
    commit('RESET_STATE')
  },

  /**
   * 刷新Token
   * @param commit
   * @returns {Promise<any>}
   */
  async refreshToken({ commit }) {
    try {
      // 使用 axios 的 timeout 实现超时控制（当前 axios 0.18.1 不支持 AbortController.signal，
      // 用 signal 做的超时不会生效，刷新请求一旦无响应会永久挂起）
      const response = await request({
        url: 'public/user/login/refresh',
        method: 'post',
        data: {},
        timeout: 10000,
        // 标记刷新请求自身：其未登录响应必须直接失败，避免在拦截器中排队死锁
        _isRefreshRequest: true
      })

      const { data } = response
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

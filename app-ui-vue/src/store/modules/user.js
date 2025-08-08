import {getInfo, login, logout} from '@/api/user'
import {getToken, removeToken, setToken} from '@/utils/auth'
import {resetRouter} from '@/router'

const getDefaultState = () => {
  return {
    token: getToken(),
    name: '',
    avatar: '',
    roles: []
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
  }
}

const actions = {
  // user login
  async login({commit}, userInfo) {
    try {
      const {username, password} = userInfo
      const {data} = await login({username: username.trim(), password: password})
      commit('SET_TOKEN', data.token)
      setToken(data.token)
      return data;
    } catch (error) {
      throw error;
    }

  },

  // get user info
  async getInfo({commit, state}) {
    try {
      const response = await getInfo();
      const {data} = response;

      if (!data) {
        throw new Error('Verification failed, please Login again.');
      }

      const {nickName, avatar, avaliableRoles} = data;

      commit('SET_NAME', nickName);
      commit('SET_AVATAR', avatar);
      commit('SET_ROLES', avaliableRoles);
      return data;
    } catch (error) {
      throw error;
    }
  },


  // user logout
  async logout({commit, state}) {
    try {
      await logout(state.token);
      removeToken() // must remove  token  first
      resetRouter()
      commit('RESET_STATE')
    } catch (error) {
      throw error;
    }
  },

  // remove token
  async resetToken({commit}) {
    removeToken() // must remove  token  first
    commit('RESET_STATE')
    resolve()
  }
}

export default {
  namespaced: true,
  state,
  mutations,
  actions
}


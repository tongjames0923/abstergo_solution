import {Dictionary} from '@/utils/Dictionary'
import {createDicMeta} from '@/utils/Dictionary/DicMeta'
import Vue from 'vue'

// 状态
const state = {
  // 存储所有字典实例
  dictionaries: {},
  // 存储字典的加载状态
  loadingStates: {},
  // 存储字典的错误信息
  errorStates: {},
  // 存储字典配置
  dictionaryConfigs: {}
}

// getters
const getters = {
  // 获取指定字典的数据
  dictionaryItems: (state) => (dictKey) => {
    const dict = state.dictionaries[dictKey]
    return dict ? dict.items : []
  },

  // 获取指定字典的加载状态
  dictionaryLoading: (state) => (dictKey) => {
    return state.loadingStates[dictKey] || false
  },

  // 获取指定字典是否已加载
  dictionaryLoaded: (state) => (dictKey) => {
    const dict = state.dictionaries[dictKey]
    return dict ? dict.loaded : false
  },

  // 获取指定字典的错误信息
  dictionaryError: (state) => (dictKey) => {
    return state.errorStates[dictKey] || null
  },

  // 根据值获取标签
  dictLabel: (state) => (dictKey, value) => {
    const dict = state.dictionaries[dictKey]
    return dict ? dict.getLabel(value) : null
  },

  // 根据标签获取值
  dictValue: (state) => (dictKey, label) => {
    const dict = state.dictionaries[dictKey]
    return dict ? dict.getValue(label) : null
  },

  // 获取字典实例
  dictionary: (state) => (dictKey) => {
    return state.dictionaries[dictKey] || null
  }
}

// mutations
const mutations = {
  // 注册字典
  REGISTER_DICTIONARY(state, {key, dictionary}) {
    Vue.set(state.dictionaries, key, dictionary)
  },

  // 设置加载状态
  SET_LOADING_STATE(state, {key, loading}) {
    Vue.set(state.loadingStates, key, loading)
  },

  // 设置错误信息
  SET_ERROR_STATE(state, {key, error}) {
    Vue.set(state.errorStates, key, error)
  },

  // 移除字典
  REMOVE_DICTIONARY(state, key) {
    Vue.delete(state.dictionaries, key)
    Vue.delete(state.loadingStates, key)
    Vue.delete(state.errorStates, key)
    Vue.delete(state.dictionaryConfigs, key)
  },

  // 保存字典配置
  SET_DICTIONARY_CONFIG(state, {key, config}) {
    Vue.set(state.dictionaryConfigs, key, config)
  }
}

// actions
const actions = {
  // 创建并注册字典
  createDictionary({commit, state, dispatch}, {key, apiUrl, transform, labelField, valueField, lazy = true}) {
    // 如果字典已存在，直接返回
    if (state.dictionaries[key]) {
      return state.dictionaries[key]
    }

    // 保存配置信息
    const config = {key, apiUrl, transform, labelField, valueField, lazy}
    commit('SET_DICTIONARY_CONFIG', {key, config})

    // 创建DicMeta
    const meta = createDicMeta(apiUrl, transform, labelField, valueField)

    // 创建Dictionary实例
    const dictionary = new Dictionary(meta)

    // 注册到store
    commit('REGISTER_DICTIONARY', {key, dictionary})

    // 如果不是懒加载，则立即加载
    if (!lazy) {
      dispatch('loadDictionary', key)
    }

    return dictionary
  },

  // 加载字典数据
  async loadDictionary({state, commit}, key) {
    const dictionary = state.dictionaries[key]
    if (!dictionary) {
      throw new Error(`Dictionary with key '${key}' not found`)
    }

    // 设置加载状态
    commit('SET_LOADING_STATE', {key, loading: true})
    commit('SET_ERROR_STATE', {key, error: null})

    try {
      // 加载数据
      await dictionary.load()
      return dictionary.items
    } catch (error) {
      // 记录错误信息
      commit('SET_ERROR_STATE', {key, error: error.message || '加载失败'})
      throw error
    } finally {
      // 恢复加载状态
      commit('SET_LOADING_STATE', {key, loading: false})
    }
  },

  // 重新加载字典数据
  async reloadDictionary({state, commit}, key) {
    const dictionary = state.dictionaries[key]
    if (!dictionary) {
      throw new Error(`Dictionary with key '${key}' not found`)
    }

    // 设置加载状态
    commit('SET_LOADING_STATE', {key, loading: true})
    commit('SET_ERROR_STATE', {key, error: null})

    try {
      // 重新加载数据
      await dictionary.reload()
      return dictionary.items
    } catch (error) {
      // 记录错误信息
      commit('SET_ERROR_STATE', {key, error: error.message || '重新加载失败'})
      throw error
    } finally {
      // 恢复加载状态
      commit('SET_LOADING_STATE', {key, loading: false})
    }
  },

  // 懒加载字典数据（如果尚未加载）
  async ensureDictionaryLoaded({state, dispatch}, key) {
    const dictionary = state.dictionaries[key]
    if (!dictionary) {
      throw new Error(`Dictionary with key '${key}' not found`)
    }

    if (!dictionary.loaded && !state.loadingStates[key]) {
      return await dispatch('loadDictionary', key)
    }

    return dictionary.items
  },

  // 移除字典
  removeDictionary({commit}, key) {
    commit('REMOVE_DICTIONARY', key)
  },

  // 清空字典数据但保留实例
  clearDictionary({state}, key) {
    const dictionary = state.dictionaries[key]
    if (dictionary) {
      dictionary.clear()
    }
  },

  // 批量加载字典
  async loadDictionaries({dispatch}, keys) {
    const results = await Promise.allSettled(
      keys.map(key => dispatch('loadDictionary', key))
    )

    const errors = results
      .map((result, index) => ({result, key: keys[index]}))
      .filter(({result}) => result.status === 'rejected')
      .map(({key, result}) => ({key, error: result.reason}))

    if (errors.length > 0) {
      console.warn('Some dictionaries failed to load:', errors)
    }

    return results
  },

  // 预加载字典
  async preloadDictionaries({state, dispatch}) {
    const lazyDictionaries = Object.keys(state.dictionaryConfigs)
      .filter(key => state.dictionaryConfigs[key].lazy !== false)

    return await dispatch('loadDictionaries', lazyDictionaries)
  }
}

export default {
  namespaced: true,
  state,
  getters,
  mutations,
  actions
}

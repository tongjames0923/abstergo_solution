import Vue from 'vue'
import Vuex from 'vuex'
import getters from './getters'
import app from './modules/app'
import settings from './modules/settings'
import user from './modules/user'
import formConfigs from './modules/FormConfig'
import dictionaries from './modules/dictionaries'

Vue.use(Vuex)

const store = new Vuex.Store({
  modules: {
    app,
    settings,
    user,
    formConfigs,
    dictionaries
  },
  getters
})

export default store


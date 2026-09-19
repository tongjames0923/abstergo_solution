import Vue from 'vue'
import Vuex from 'vuex'
import getters from './getters'
import app from './modules/app'
import settings from './modules/settings'
import user from './modules/user'
import formConfigs from './modules/FormConfig'
import routes from './modules/routes'
import push from './modules/push'

Vue.use(Vuex)

const store = new Vuex.Store({
  modules: {
    app,
    settings,
    user,
    formConfigs,
    routes,
    push
  },
  getters
})

export default store

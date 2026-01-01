import Vue from 'vue'

import 'normalize.css/normalize.css' // A modern alternative to CSS resets
import '@fortawesome/fontawesome-free/css/all.css'
import '@fortawesome/fontawesome-free/js/all.js'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import '@/styles/index.scss' // global css
import App from './App'
import store from './store'

import '@/icons' // icon
import '@/permission'
import getServiceRouter from '@/router' // permission control
/**
 * If you don't want to use mock-server
 * you want to use MockJs for mock api
 * you can execute: mockXHR()
 *
 * Currently MockJs will be used in the production environment,
 * please remove it before going online ! ! !
 */
if (process.env.NODE_ENV === 'production') {
  const { mockXHR } = require('../mock')
  mockXHR()
}

// set ElementUI lang to EN
// Vue.use(ElementUI, { locale })
// 如果想要中文版 element-ui，按如下方式声明
Vue.use(ElementUI)

Vue.config.productionTip = false

var router = null

async function bootstrap() {
  router = await getServiceRouter()
  const vueInstance = new Vue({
    el: '#app',
    router: router,
    store,
    render: (h) => h(App)
  })

  // 将Vue实例保存到window对象中，以便updateRouter函数能够访问
  window.vueInstance = vueInstance

  return vueInstance
}

bootstrap()

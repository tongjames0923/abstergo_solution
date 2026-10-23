import Vue from 'vue'

import 'normalize.css/normalize.css' // A modern alternative to CSS resets
import '@fortawesome/fontawesome-free/css/all.css'
import '@fortawesome/fontawesome-free/js/all.js'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import '@/styles/index.scss' // global css
import App from './App'
import store from './store'
import i18n from './i18n'

import '@/icons' // icon
import '@/permission'
import getServiceRouter from '@/router' // permission control
import installResponsiveGuard from '@/utils/responsive-guard'

// set ElementUI lang to EN
// Vue.use(ElementUI, { locale })
// 如果想要中文版 element-ui，按如下方式声明
Vue.use(ElementUI, {
  i18n: (key, value) => i18n.t(key, value)
})

Vue.config.productionTip = false

var router = null

async function bootstrap() {
  router = await getServiceRouter()
  // 开发环境溢出守卫：检测未适配窄屏的横向溢出元素
  installResponsiveGuard(router)
  const vueInstance = new Vue({
    el: '#app',
    router: router,
    store,
    i18n,
    render: (h) => h(App)
  })

  // 将Vue实例保存到window对象中，以便updateRouter函数能够访问
  window.vueInstance = vueInstance

  return vueInstance
}

bootstrap()

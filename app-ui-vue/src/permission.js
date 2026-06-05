import router from './router'
import store from './store'
import { Message } from 'element-ui'
import NProgress from 'nprogress' // progress bar
import 'nprogress/nprogress.css' // progress bar style
import { getToken } from '@/utils/auth' // get token from cookie
import getPageTitle from '@/utils/get-page-title'
import { hasRole } from '@/utils/permission'
import { isRouteUpdating, waitForRouteUpdate } from '@/router'

NProgress.configure({ showSpinner: false }) // NProgress Configuration

const whiteList = ['/login'] // no redirect whitelist

// 标记是否已经在登录时加载过动态路由
let dynamicRoutesLoaded = false

export function setUpRouterPermissions(router) {
  router.beforeEach(async(to, from, next) => {
    // start progress bar
    NProgress.start()

    // set page title
    document.title = getPageTitle(to.meta.title)

    // determine whether the user has logged in
    const hasToken = getToken()

    if (hasToken) {
      if (to.path === '/login') {
        // if is logged in, redirect to the home page
        next({ path: '/' })
        NProgress.done()
      } else {
        // 如果路由正在更新中，等待更新完成
        if (isRouteUpdating()) {
          console.log('路由正在更新中，等待更新完成...')
          await waitForRouteUpdate()
        }

        const hasGetUserInfo = store.getters.name
        if (!hasGetUserInfo) {
          try {
            // get user info
            await store.dispatch('user/getInfo')

            // 获取用户信息后，更新路由数据
            // 如果是从登录页跳转过来，且已经加载过路由，则跳过
            if (from.path !== '/login' || !dynamicRoutesLoaded) {
              const { updateRouter } = await import('@/router')
              await updateRouter()
              dynamicRoutesLoaded = true
            }
          } catch (error) {
            // remove token and go to login page to re-login
            await store.dispatch('user/resetToken')
            Message.error(error || '错误')
            next(`/login?redirect=${to.path}`)
            NProgress.done()
            return
          }
        }

        // 检查路由是否需要特定角色权限
        if (to.meta && to.meta.roles && to.meta.roles.length > 0) {
          const userRoles = store.getters.roles || []

          if (!hasRole(userRoles, to.meta.roles)) {
            next(`/403`)
            NProgress.done()
            return
          }
        }

        next()
        NProgress.done()
      }
    } else {
      /* has no token*/
      if (whiteList.indexOf(to.path) !== -1) {
        // in the free login whitelist, go directly
        next()
        NProgress.done()
      } else {
        // other pages that do not have permission to access are redirected to the login page.
        next(`/login?redirect=${to.path}`)
        NProgress.done()
      }
    }
  })

  router.afterEach(() => {
    // finish progress bar
    NProgress.done()
  })
}

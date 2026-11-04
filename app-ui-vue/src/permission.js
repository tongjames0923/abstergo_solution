import store from './store'
import {Message} from 'element-ui'
import NProgress from 'nprogress' // progress bar
import 'nprogress/nprogress.css' // progress bar style
import {getToken} from '@/utils/auth' // get token from cookie
import getPageTitle from '@/utils/get-page-title'
import {hasRole} from '@/utils/permission'
import {isRouteUpdating, waitForRouteUpdate} from '@/router'

NProgress.configure({ showSpinner: false }) // NProgress Configuration

const whiteList = ['/login'] // no redirect whitelist

// 标记是否已经在登录时加载过动态路由
let dynamicRoutesLoaded = false

/**
 * 取被拦截时的原始访问地址，用于拼登录后的回跳目标
 * 未匹配到任何路由（如未登录时动态路由尚未注册）会被通配路由 redirect 到 /404，
 * 而该重定向在 matcher 阶段完成，守卫里的 to 已是 /404，真实地址保留在 to.redirectedFrom 上
 * @param {Object} to 路由守卫解析后的目标路由
 * @return {string} 原始访问地址（含 query 与 hash）
 */
function resolveOriginFullPath(to) {
  return to.redirectedFrom || to.fullPath
}

/**
 * 重置动态路由加载状态
 * 用于用户退出登录时调用，确保重新登录时能正确加载路由
 */
export function resetDynamicRoutesLoaded() {
  dynamicRoutesLoaded = false
}

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
            // 获取用户信息与动态路由加载并行执行，缩短首次进入系统的等待时间
            const tasks = [store.dispatch('user/getInfo')]

            // 如果是从登录页跳转过来，且已经加载过路由，则跳过
            if (from.path !== '/login' || !dynamicRoutesLoaded) {
              const { updateRouter } = await import('@/router')
              tasks.push(updateRouter().then(() => {
                dynamicRoutesLoaded = true
              }))
            }

            await Promise.all(tasks)
          } catch (error) {
            // remove token and go to login page to re-login
            await store.dispatch('user/resetToken')
            Message.error(error || '错误')
            next(`/login?redirect=${encodeURIComponent(resolveOriginFullPath(to))}`)
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
        next(`/login?redirect=${encodeURIComponent(resolveOriginFullPath(to))}`)
        NProgress.done()
      }
    }
  })

  router.afterEach(() => {
    // finish progress bar
    NProgress.done()
  })
}

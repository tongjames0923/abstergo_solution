import Vue from 'vue'
import Router from 'vue-router'
import store from '@/store'
/* Layout */
import Layout from '@/layout'
import { getRoutes } from '@/api/view'
import { getGatewayVersion } from '@/api/system'
import { setUpRouterPermissions } from '@/permission'
import { getToken } from '@/utils/auth'
import routeCacheService from '@/utils/route-cache'

Vue.use(Router)

/**
 * Note: sub-menu only appear when route children.length >= 1
 * Detail see: https://panjiachen.github.io/vue-element-admin-site/guide/essentials/router-and-nav.html
 *
 * hidden: true                   if set true, item will not show in the sidebar(default is false)
 * alwaysShow: true               if set true, will always show the root menu
 *                                if not set alwaysShow, when item has more than one children route,
 *                                it will becomes nested mode, otherwise not show the root menu
 * redirect: noRedirect           if set noRedirect will no redirect in the breadcrumb
 * name:'router-name'             the name is used by <keep-alive> (must set!!!)
 * meta : {
 roles: ['admin','editor']    control the page roles (you can set multiple roles)
 title: 'title'               the name show in sidebar and breadcrumb (recommend set)
 icon: 'svg-name'/'el-icon-x' the icon show in the sidebar
 breadcrumb: false            if set false, the item will hidden in breadcrumb(default is true)
 activeMenu: '/example/list'  if set path, the sidebar will highlight the path you set
 }
 */

/**
 * constantRoutes
 * a base page that does not have permission requirements
 * all roles can be accessed
 */
export const constantRoutes = [
  {
    path: '/login',
    component: () => import('@/views/login/index'),
    hidden: true
  },

  {
    path: '/404',
    component: () => import('@/views/404'),
    hidden: true
  },
  {
    path: '/403',
    component: () => import('@/views/403'),
    hidden: true
  },

  {
    path: '/oauth/callback',
    component: () => import('@/views/oauth/callback/index'),
    hidden: true
  },

  {
    path: '/account',
    component: Layout,
    redirect: '/account/external-identity',
    meta: { title: '账号与安全', icon: 'el-icon-lock' },
    children: [{
      path: 'external-identity',
      name: 'ExternalIdentityBind',
      component: () => import('@/views/account/external-identity/index'),
      meta: { title: '外部身份绑定', icon: 'el-icon-key' }
    }]
  },

  {
    path: '/',
    component: Layout,
    redirect: '/Welcome',
    meta: { title: '首页', icon: 'el-icon-s-home' },
    children: [{
      path: 'Welcome',
      name: '欢迎',
      component: () => import('@/views/dashboard/index'),
      meta: { title: '欢迎页', icon: 'el-icon-star-on' }
    }
    ]
  }
]

export async function resetRouter() {
  const newRouter = await getServiceRouter()
  currentRouter.matcher = newRouter.matcher // reset router
}

// 预加载所有 views 目录下的 .vue 文件
const allViews = require.context('@/views', true, /\.vue$/)

/**
 * 将后端返回的路由数据转换为组件路径
 * 例如：'LoginView' => () => import('@/views/login/index')
 */
function mapComponent(route) {
  console.log('映射组件:', route.path, '组件路径:', route.component) // 调试日志

  if (route.component === 'Layout') {
    route.component = Layout
    console.log('使用Layout组件') // 调试日志
  } else {
    // 构造相对路径 key，例如：'dashboard/index.vue'
    const componentKey = `.${route.component}`
    console.log('尝试加载组件:', componentKey) // 调试日志

    try {
      // 使用 require.context 加载组件
      // require.context 已经预加载了 @/views 目录下所有 .vue 文件
      const component = allViews(componentKey).default
      route.component = component
      console.log('组件加载成功') // 调试日志
    } catch (e) {
      // require.context 未找到组件，使用 404 页面作为 fallback
      console.error(`找不到组件：${componentKey}，将使用404页面`, e)
      route.component = () => import('@/views/404')
    }
  }

  // 递归处理子路由
  if (route.children && Array.isArray(route.children)) {
    console.log('处理子路由:', route.children.length) // 调试日志
    route.children.forEach(mapComponent)
  }
}

var currentRouter = null

// 添加一个标志位，防止重复加载动态路由
let hasLoadedDynamicRoutes = false

// 路由更新锁，防止并发更新
let isUpdatingRoutes = false

// 路由更新等待队列
let updateWaitQueue = []

async function generateRoutesFromServer() {
  try {
    const hasToken = getToken()
    var routes = []

    // 合并静态路由
    constantRoutes.forEach((r) => {
      routes.push(r)
    })

    // 如果有token，则从线上获取动态路由
    if (hasToken) {
      try {
        // 先尝试从缓存中获取路由数据
        let res = routeCacheService.getCachedRoutes()

        if (res) {
          console.log('使用缓存的动态路由数据') // 调试日志
        } else {
          // 缓存中没有数据，从后端获取
          const response = await getRoutes()
          res = response.data
          console.log('从后端获取的动态路由数据:', res) // 调试日志

          // 将获取到的路由数据缓存起来
          routeCacheService.cacheRoutes(res)

          // 实际拉取路由后，记录服务端视图版本号供后续比对（失败不阻断路由加载）
          try {
            const versionRes = await getGatewayVersion()
            if (versionRes && versionRes.data && versionRes.data.viewVersion) {
              routeCacheService.setLocalVersion(versionRes.data.viewVersion)
            }
          } catch (e) {
            console.warn('获取视图版本号失败:', e)
          }
        }

        // 映射 component 字段为实际组件路径
        for (let i = 0; i < res.length; i++) {
          mapComponent(res[i])
          routes.push(res[i])
        }
        console.log('映射后的动态路由:', routes) // 调试日志

        // 标记动态路由已加载
        hasLoadedDynamicRoutes = true
      } catch (error) {
        console.error('获取动态路由失败:', error)
        // 如果获取动态路由失败，只返回静态路由
        console.log('使用静态路由作为备选方案')
      }
    } else {
      console.log('未检测到token，仅使用静态路由')
    }

    // 添加404路由
    routes.push({ path: '*', redirect: '/404', hidden: true })
    console.log('合并后的所有路由:', routes) // 调试日志
    return routes
  } catch (error) {
    console.error('生成路由失败:', error)
    // 如果生成路由失败，返回基础路由（静态路由 + 404）
    return [...constantRoutes, { path: '*', redirect: '/404', hidden: true }]
  }
}

async function getServiceRouter() {
  const routes = await generateRoutesFromServer()
  currentRouter = new Router({
    scrollBehavior: () => ({ y: 0 }),
    routes
  })
  setUpRouterPermissions(currentRouter)
  return currentRouter
}

export async function updateRouter() {
  // 如果正在更新路由，返回当前路由器
  if (isUpdatingRoutes) {
    console.log('路由正在更新中，等待更新完成...')
    return new Promise((resolve) => {
      updateWaitQueue.push(resolve)
    })
  }

  isUpdatingRoutes = true

  try {
    // 1. 获取新路由（使用缓存或重新获取）
    const newRoutes = await generateRoutesFromServer()

    // 2. 创建新 router 实例
    const newRouter = new Router({
      scrollBehavior: () => ({ y: 0 }),
      routes: newRoutes
    })

    // 3. 设置权限守卫
    setUpRouterPermissions(newRouter)

    // 4. 更新store中的路由数据（只存储动态路由部分）
    // 过滤掉静态路由，只保留动态路由
    const dynamicRoutes = newRoutes.filter(route =>
      !constantRoutes.some(constantRoute => constantRoute.path === route.path)
    )
    store.commit('user/SET_ROUTES', dynamicRoutes)

    // 5. 关键修复：先更新 matcher，再替换 router 引用
    // 这样可以确保所有引用都指向新的路由配置
    if (currentRouter) {
      // 更新现有 router 的 matcher
      currentRouter.matcher = newRouter.matcher
    }

    // 6. 更新全局 router 引用
    currentRouter = newRouter

    // 7. 如果有全局 Vue 实例，也更新它的路由
    if (window.vueInstance && window.vueInstance.$router) {
      window.vueInstance.$router.matcher = newRouter.matcher
    }

    console.log('路由更新成功，动态路由数量:', dynamicRoutes.length)

    // 8. 处理等待队列中的回调
    updateWaitQueue.forEach(resolve => resolve(currentRouter))
    updateWaitQueue = []

    // 9. 返回 Promise，确保调用者可以等待更新完成
    return new Promise((resolve) => {
      // 使用 Vue.nextTick 确保 DOM 更新完成
      Vue.nextTick(() => {
        resolve(currentRouter)
      })
    })
  } catch (error) {
    console.error('路由更新失败:', error)
    throw error
  } finally {
    isUpdatingRoutes = false
  }
}

/**
 * 强制刷新路由缓存并更新路由
 * @returns {Promise<void>}
 */
export async function refreshRoutes() {
  try {
    // 清除缓存
    routeCacheService.clearCache()

    // 重新获取路由数据并更新路由
    await updateRouter()

    console.log('路由缓存已刷新')
  } catch (error) {
    console.error('刷新路由失败:', error)
    throw error
  }
}

// 导出路由更新状态检查函数
export function isRouteUpdating() {
  return isUpdatingRoutes
}

// 导出等待路由更新完成的函数
export function waitForRouteUpdate() {
  if (!isUpdatingRoutes) {
    return Promise.resolve(currentRouter)
  }
  return new Promise((resolve) => {
    updateWaitQueue.push(resolve)
  })
}

export default getServiceRouter

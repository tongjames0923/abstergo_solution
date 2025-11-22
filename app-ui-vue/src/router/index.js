import Vue from 'vue'
import Router from 'vue-router'
/* Layout */
import Layout from '@/layout'
import {getRoutes} from "@/api/view";
import {setUpRouterPermissions} from "@/permission";

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
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    meta: {title: '首页', icon: 'el-icon-s-home'},
    children: [{
      path: 'dashboard',
      name: 'Dashboard',
      component: () => import('@/views/dashboard/index'),
      meta: {title: 'Dashboard', icon: 'el-icon-star-on', roles: ['2']}
    },
      {
        path: 'table',
        name: 'Table',
        component: () => import('@/views/table/index'),
        meta: {title: 'Table', icon: 'el-icon-star-on', roles: ['2']}
      }
    ]
  },
  //
  // {
  //   path: '/admin',
  //   component: Layout,
  //   name: 'admin',
  //   redirect: '/admin/user',
  //   meta: {title: '管理员操作', icon: 'el-icon-s-help', roles: ['1']},
  //   children: [
  //     {
  //       path: 'user',
  //       name: '用户管理',
  //       component: () => import('@/views/admin/user/index.vue'),
  //       meta: {title: '用户管理', icon: 'table', roles: ['1']}
  //     },
  //     {
  //       path: 'role',
  //       name: '角色管理',
  //       component: () => import('@/views/admin/role/index.vue'),
  //       meta: {title: '角色管理', icon: 'eye', roles: ['1']}
  //     },
  //     {
  //       path: 'company',
  //       name: '公司管理',
  //       component: () => import('@/views/admin/company/index.vue'),
  //       meta: {title: '公司管理', icon: 'el-icon-s-custom', roles: ['1']}
  //     },
  //     {
  //       path: 'form',
  //       name: '表单创建',
  //       component: () => import('@/views/form/FormCreator.vue'),
  //       meta: {title: '表单创建', icon: 'el-icon-s-custom', roles: ['1']}
  //     },
  //     {
  //       path: 'form-manager',
  //       name: '表单管理',
  //       component: () => import('@/views/form/FormManager.vue'),
  //       meta: {title: '表单管理', icon: 'el-icon-s-order', roles: ['1']}
  //     },
  //     {
  //       path: 'systemlog',
  //       name: '系统运行报表',
  //       component: () => import('@/views/admin/systemlog/index.vue'),
  //       meta: {title: '系统运行报表', icon: 'el-icon-s-data', roles: ['1']}
  //     }
  //   ]
  // },
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
      const component = allViews(componentKey).default
      route.component = component
      console.log('组件加载成功') // 调试日志
    } catch (e) {
      console.error(`找不到组件：${componentKey}`, e)
      // 尝试使用动态导入
      try {
        route.component = () => import(`@/views${route.component}`)
        console.log('使用动态导入成功') // 调试日志
      } catch (importError) {
        console.error('动态导入也失败:', importError)
        route.component = () => import('@/views/404') // fallback 组件
      }
    }
  }

  if (route.children) {
    console.log('处理子路由:', route.children.length) // 调试日志
    route.children.forEach(mapComponent)
  }
}

var currentRouter = null

async function generateRoutesFromServer() {
  try {
    var res = await getRoutes()
    res = res.data
    console.log("从后端获取的动态路由数据:", res) // 调试日志
    var routes = []
    // 合并静态路由
    constantRoutes.forEach((r) => {
      routes.push(r)
    })

    // 映射 component 字段为实际组件路径
    for (let i = 0; i < res.length; i++) {
      mapComponent(res[i])
      routes.push(res[i])
    }
    console.log("映射后的动态路由:", routes) // 调试日志


    constantRoutes.push(  // 404 page must be placed at the end !!!
      {path: '*', redirect: '/404', hidden: true})
    console.log("合并后的所有路由:", routes) // 调试日志
    return routes
  } catch (error) {
    console.error("获取动态路由失败:", error)
    // 如果获取动态路由失败，只返回静态路由
    return [...constantRoutes,  // 404 page must be placed at the end !!!
      {path: '*', redirect: '/404', hidden: true}]
  }
}

async function getServiceRouter() {
  const routes = await generateRoutesFromServer()
  currentRouter = new Router({
    scrollBehavior: () => ({y: 0}),
    routes
  })
  setUpRouterPermissions(currentRouter)
  return currentRouter
}

export async function updateRouter() {
  // 1. 重置旧路由
  if (currentRouter) {
    await resetRouter()
  }

  // 2. 获取新路由
  const newRoutes = await generateRoutesFromServer()

  // 3. 创建新 router 实例
  currentRouter = new Router({
    scrollBehavior: () => ({y: 0}),
    routes: newRoutes
  })

  // 4. 设置权限守卫
  setUpRouterPermissions(currentRouter)

  return currentRouter
}

export default getServiceRouter

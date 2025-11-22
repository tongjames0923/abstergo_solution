import { getViewRoutes } from '@/api/view'
import Router from 'vue-router'

/**
 * 动态路由工具类
 * 用于处理动态路由的加载、转换和合并
 */
export class DynamicRouter {

  /**
   * 将后端路由模型转换为前端路由配置
   * @param {Object} routeModel 后端路由模型
   * @returns {Object} 前端路由配置
   */
  static convertRouteModel(routeModel) {
    const route = {
      path: routeModel.path,
      name: routeModel.name,
      hidden: routeModel.hidden || false,
      alwaysShow: routeModel.alwaysShow || false,
      redirect: routeModel.redirect || 'noRedirect'
    }

    // 处理组件路径转换
    if (routeModel.component) {
      // 使用动态导入，确保路径格式正确
      if (routeModel.component.startsWith('@/')) {
        route.component = () => import(routeModel.component)
      } else if (routeModel.component.startsWith('@/views/')) {
        route.component = () => import(routeModel.component)
      } else if (routeModel.component.includes('.vue')) {
        // 如果包含.vue但路径不完整，添加@/views/前缀
        route.component = () => import(`@/views/${routeModel.component}`)
      } else {
        // 对于Layout组件特殊处理
        if (routeModel.component === 'Layout') {
          route.component = () => import('@/layout')
        } else {
          // 默认使用views目录
          route.component = () => import(`@/views/${routeModel.component}`)
        }
      }
    }

    // 处理元数据
    if (routeModel.meta) {
      route.meta = {
        title: routeModel.meta.title || '',
        icon: routeModel.meta.icon || '',
        roles: routeModel.meta.roles || [],
        breadcrumb: routeModel.meta.breadcrumb !== false,
        activeMenu: routeModel.meta.activeMenu || ''
      }
    }

    // 递归处理子路由
    if (routeModel.children && routeModel.children.length > 0) {
      route.children = routeModel.children.map(child => this.convertRouteModel(child))
    }

    return route
  }

  /**
   * 将后端路由分组转换为前端路由结构
   * @param {Object} routesData 后端返回的路由数据
   * @returns {Array} 前端路由配置数组
   */
  static convertRoutesData(routesData) {
    const dynamicRoutes = []

    if (routesData.routes) {
      Object.keys(routesData.routes).forEach(group => {
        const groupRoutes = routesData.routes[group]

        // 创建分组路由
        const groupRoute = {
          path: `/${group}`,
          component: () => import('@/layout'),
          redirect: 'noRedirect',
          name: group,
          meta: {
            title: group,
            icon: 'el-icon-s-help',
            roles: ['1', '2'] // 默认权限
          },
          children: []
        }

        // 转换该分组下的所有路由
        if (Array.isArray(groupRoutes)) {
          groupRoutes.forEach(routeModel => {
            const convertedRoute = this.convertRouteModel(routeModel)
            // 确保子路由的路径是相对于分组的
            if (convertedRoute.path && convertedRoute.path.startsWith('/')) {
              convertedRoute.path = convertedRoute.path.substring(1)
            }
            groupRoute.children.push(convertedRoute)
          })
        }

        // 如果分组有子路由，则添加到动态路由中
        if (groupRoute.children.length > 0) {
          dynamicRoutes.push(groupRoute)
        }
      })
    }

    return dynamicRoutes
  }

  /**
   * 加载动态路由
   * @returns {Promise<Array>} 动态路由配置数组
   */
  static async loadDynamicRoutes() {
    try {
      const response = await getViewRoutes()
      if (response && response.data && response.data.routes) {
        return this.convertRoutesData(response.data)
      }
      return []
    } catch (error) {
      console.error('加载动态路由失败:', error)
      return []
    }
  }

  /**
   * 合并静态路由和动态路由
   * @param {Array} staticRoutes 静态路由配置
   * @param {Array} dynamicRoutes 动态路由配置
   * @returns {Array} 合并后的路由配置
   */
  static mergeRoutes(staticRoutes, dynamicRoutes) {
    // 深拷贝静态路由，避免修改原数组
    const mergedRoutes = JSON.parse(JSON.stringify(staticRoutes))

    // 将动态路由直接添加到路由配置中（在404路由之前）
    if (dynamicRoutes.length > 0) {
      // 找到404路由的位置
      const notFoundIndex = mergedRoutes.findIndex(route => route.path === '*')

      if (notFoundIndex !== -1) {
        // 在404路由之前插入动态路由
        mergedRoutes.splice(notFoundIndex, 0, ...dynamicRoutes)
      } else {
        // 如果没有找到404路由，直接添加到末尾
        mergedRoutes.push(...dynamicRoutes)
      }
    }

    return mergedRoutes
  }

  /**
   * 初始化动态路由
   * @param {Router} router Vue Router实例
   * @param {Array} staticRoutes 静态路由配置
   * @returns {Promise<boolean>} 是否成功初始化
   */
  static async initDynamicRoutes(router, staticRoutes) {
    try {
      // 检查是否已经初始化过动态路由
      if (router._dynamicRoutesInitialized) {
        console.log('动态路由已经初始化过，跳过重复初始化')
        return true
      }

      // 加载动态路由
      const dynamicRoutes = await this.loadDynamicRoutes()

      if (dynamicRoutes.length > 0) {
        // 使用router.addRoutes方法添加动态路由（Vue Router 3.x兼容）
        // 注意：Vue Router 4.x使用addRoute，但这里我们使用3.x的addRoutes
        if (router.addRoutes) {
          router.addRoutes(dynamicRoutes)
        } else {
          // 如果addRoutes不存在，使用addRoute逐个添加
          dynamicRoutes.forEach(route => {
            if (router.addRoute) {
              router.addRoute(route)
            }
          })
        }

        // 标记动态路由已初始化
        router._dynamicRoutesInitialized = true

        console.log('动态路由初始化成功，共加载', dynamicRoutes.length, '个动态路由')
        return true
      }

      console.log('未找到动态路由配置')
      return false
    } catch (error) {
      console.error('初始化动态路由失败:', error)
      return false
    }
  }
}

export default DynamicRouter

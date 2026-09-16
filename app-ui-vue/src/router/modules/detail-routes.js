import Layout from '@/layout'
import i18n from '@/i18n'

/**
 * 详情页路由集中注册表
 *
 * 基于 URL 动态参数的详情页统一在此登记，新增详情页只需追加一条路由配置，
 * 无需修改路由守卫与动态路由加载逻辑。约定：
 * 1. 路由路径使用动态参数（如 :userId），参数名与页面 detailConfig.paramName 一致
 * 2. name 唯一且以业务名 + Details 命名（如 UserDetails），列表页通过 name + params 跳转
 * 3. meta.activeMenu 指回所属列表页菜单路径，保证侧边栏高亮正确
 * 4. 详情页路由挂在 constantRoutes（静态路由），保证刷新与外部链接直达不被 404 兜底吞掉；
 *    访问权限仍由 permission.js 守卫（登录态/角色）与后端接口权限双重保障
 */
export const detailRoutes = [
  {
    path: '/user/details/:userId',
    component: Layout,
    hidden: true,
    children: [
      {
        path: '',
        name: 'UserDetails',
        component: () => import('@/views/user/details/index.vue'),
        meta: {
          title: i18n.t('userDetail.title'),
          icon: 'el-icon-user',
          activeMenu: '/common/user',
          breadcrumb: false
        }
      }
    ]
  }
]

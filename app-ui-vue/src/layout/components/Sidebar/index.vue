<template>
  <div :class="{'has-logo':showLogo}" class="sidebar-container-macos">
    <logo v-if="showLogo" :collapse="isCollapse" />
    <el-scrollbar wrap-class="scrollbar-wrapper">
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :background-color="variables.menuBg"
        :text-color="variables.menuText"
        :unique-opened="false"
        :active-text-color="variables.menuActiveText"
        :collapse-transition="false"
        mode="vertical"
        class="sidebar-menu-macos"
      >
        <sidebar-item v-for="route in allRoutes" :key="route.path" :item="route" :base-path="route.path" />
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import Logo from './Logo'
import SidebarItem from './SidebarItem'
import variables from '@/styles/variables.scss'
import { constantRoutes } from '@/router'
import { isRouteVisible } from '@/utils/permission'

export default {
  components: { SidebarItem, Logo },
  computed: {
    ...mapGetters([
      'sidebar',
      'routes', // 从 store 中获取动态路由
      'roles' // 获取用户角色
    ]),
    // 合并静态路由和动态路由
    allRoutes() {
      // 将 store 中的动态路由与静态路由合并，并进行权限过滤
      const allRoutes = [...constantRoutes, ...this.routes]
      return this.filterRoutes(allRoutes)
    },
    activeMenu() {
      const route = this.$route
      const { meta, path } = route
      // if set path, the sidebar will highlight the path you set
      if (meta.activeMenu) {
        return meta.activeMenu
      }
      return path
    },
    showLogo() {
      return this.$store.state.settings.sidebarLogo
    },
    variables() {
      return variables
    },
    isCollapse() {
      return !this.sidebar.opened
    }
  },
  methods: {
    // 过滤路由，只保留用户有权限访问的路由
    filterRoutes(routes) {
      return routes.filter(route => {
        // 检查当前路由是否对用户可见
        const isVisible = isRouteVisible(route, this.roles)
        
        // 如果有子路由，递归过滤子路由
        if (route.children && route.children.length > 0) {
          const filteredChildren = this.filterRoutes(route.children)
          // 更新子路由
          route.children = filteredChildren
          // 如果过滤后没有子路由，则父路由也不显示
          return isVisible && filteredChildren.length > 0
        }
        
        return isVisible
      })
    }
  }
}
</script>

<style lang="scss" scoped>
@import "~@/styles/variables.scss";
.sidebar-container-macos {
  background-color: #ffffff;
  border-radius: 0;
  box-shadow: none;
  margin: 0;
  padding: 0;
  height: 100vh;
  overflow: hidden;
  border-right: 1px solid #eaeaea;
}

.sidebar-menu-macos {
  background-color: transparent !important;
  border-right: none !important;
  border-radius: 0;
  padding: 4px 0;

  .el-menu-item,
  .el-submenu__title {
    height: 32px;
    line-height: 32px;
    margin: 1px 8px;
    border-radius: 6px;
    transition: background-color 0.15s ease, color 0.15s ease;
    font-size: 13px;
    backdrop-filter: none;
    -webkit-backdrop-filter: none;
    background: transparent;
    box-shadow: none;
    border: none;
    color: #666;

    &:hover {
      background: #f5f5f7 !important;
      color: #333 !important;
      backdrop-filter: none;
      -webkit-backdrop-filter: none;
      box-shadow: none;
      border: none;
      transform: none;
    }

    // 选中态：蓝色调浅色背景，与系统主色 #0071e3 呼应（macOS App Store 风格）
    &.is-active {
      background-color: rgba(0, 113, 227, 0.1) !important;
      color: #0071e3 !important;
      font-weight: 500;
    }
  }

  .el-submenu {
    .el-submenu__title {
      &:hover {
        background-color: #f5f5f7 !important;
      }
    }

    // 展开中的父级菜单保持轻量强调
    &.is-opened > .el-submenu__title {
      color: #333;
    }

    .el-submenu__icon-arrow {
      font-size: 12px;
      color: #999;
    }

    .el-menu {
      background-color: transparent !important;
      border-radius: 0;
      margin: 0;
      padding: 2px 0;

      .el-menu-item {
        padding-left: 44px !important;
        margin: 1px 8px;
        border-radius: 6px;
        position: relative;
        /* 覆盖全局 sidebar.scss 中子菜单 $subMenuBg 灰底，保持与一级菜单一致的透明底 */
        background-color: transparent !important;

        // 子菜单项左侧层级指示线
        &::before {
          content: '';
          position: absolute;
          left: 30px;
          top: 50%;
          transform: translateY(-50%);
          width: 4px;
          height: 4px;
          border-radius: 50%;
          background: #d1d1d6;
          transition: background-color 0.15s ease;
        }

        &.is-active::before {
          background: #0071e3;
        }
      }
    }
  }

  .el-scrollbar {
    height: calc(100% - 44px);
  }
}

/* 覆盖全局 sidebar.scss 中子菜单灰底规则（.app-wrapper .sidebar-container .el-submenu .el-menu-item），
   提升选择器优先级，保持子菜单项与一级菜单一致的透明底 */
.sidebar-container-macos .sidebar-menu-macos .el-submenu .el-menu .el-menu-item {
  background-color: transparent !important;
  min-width: 0 !important;
}

/* macOS 风格细滚动条 */
.sidebar-container-macos {
  ::v-deep .el-scrollbar__bar.is-vertical {
    width: 4px;
  }

  ::v-deep .el-scrollbar__thumb {
    background-color: rgba(0, 0, 0, 0.15);
    border-radius: 4px;
    opacity: 0.6;

    &:hover {
      opacity: 1;
    }
  }
}
</style>

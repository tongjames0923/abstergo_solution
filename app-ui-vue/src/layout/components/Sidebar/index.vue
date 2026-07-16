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
    transition: all 0.15s ease;
    font-size: 13px;
    backdrop-filter: none;
    -webkit-backdrop-filter: none;
    background: transparent;
    box-shadow: none;
    border: none;
    color: #666;

    &:hover {
      background: #f5f5f5 !important;
      color: #333 !important;
      backdrop-filter: none;
      -webkit-backdrop-filter: none;
      box-shadow: none;
      border: none;
      transform: none;
    }

    &.is-active {
      background-color: #f0f0f0 !important;
      color: #000 !important;
      font-weight: 500;
    }
  }

  .el-submenu {
    .el-submenu__title {
      &:hover {
        background-color: #f5f5f5 !important;
      }
    }

    .el-menu {
      background-color: transparent !important;
      border-radius: 0;
      margin: 0;
      padding: 4px 0;

      .el-menu-item {
        padding-left: 48px !important;
        margin: 1px 8px;
        border-radius: 6px;
      }
    }
  }

  .el-scrollbar {
    height: calc(100% - 44px);
  }
}
</style>

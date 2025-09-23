<template>
  <div :class="{'has-logo':showLogo}" class="glass-sidebar">
    <logo v-if="showLogo" :collapse="isCollapse" />
    <div class="sidebar-menu-wrapper">
      <el-scrollbar wrap-class="scrollbar-wrapper" class="sidebar-scrollbar">
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          :background-color="variables.menuBg"
          :text-color="variables.menuText"
          :unique-opened="false"
          :active-text-color="variables.menuActiveText"
          :collapse-transition="false"
          mode="vertical"
          class="sidebar-menu"
        >
          <sidebar-item v-for="route in routes" :key="route.path" :item="route" :base-path="route.path" />
        </el-menu>
      </el-scrollbar>
    </div>
  </div>
</template>

<script>
import {mapGetters} from 'vuex'
import Logo from './Logo'
import SidebarItem from './SidebarItem'
import variables from '@/styles/variables.scss'

export default {
  components: { SidebarItem, Logo },
  computed: {
    ...mapGetters([
      'sidebar'
    ]),
    routes() {
      return this.filterRoutesByRole(this.$router.options.routes, this.$store.getters.roles)
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
    filterRoutesByRole(routes, roles) {
      const res = []
      routes.forEach(route => {
        const tmp = { ...route }
        if (this.hasRolePermission(roles, tmp)) {
          if (tmp.children) {
            tmp.children = this.filterRoutesByRole(tmp.children, roles)
          }
          res.push(tmp)
        }
      })
      return res
    },
    hasRolePermission(roles, route) {
      if (route.meta && route.meta.roles) {
        return roles.some(role => route.meta.roles.includes(role))
      } else {
        // 如果路由没有设置角色，则默认可见
        return true
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.glass-sidebar {
  display: flex;
  flex-direction: column;
  height: 100%;
  border-radius: 10px; // 根据规范调整为10px
}

.sidebar-menu-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  border-bottom-left-radius: 10px; // 根据规范调整为10px
  border-bottom-right-radius: 10px; // 根据规范调整为10px
  overflow: hidden;
  min-height: 200px; // 确保即使在项目很少时也有最小高度
}

.sidebar-scrollbar {
  flex: 1;
  border-bottom-left-radius: 10px; // 根据规范调整为10px
  border-bottom-right-radius: 10px; // 根据规范调整为10px

  // 当内容较少时，滚动条区域自适应内容高度
  // 当内容较多时，保持滚动功能
  max-height: calc(100vh - 100px); // 为顶部logo和边距留出空间

  ::v-deep .el-scrollbar__wrap {
    overflow-x: hidden;
  }
}

.sidebar-menu {
  min-height: 100%;
  display: flex;
  flex-direction: column;

  // 调整菜单项间距，减少底部留白
  ::v-deep {
    .el-menu-item,
    .el-submenu {
      margin-bottom: 2px;
    }

    // 最后一个菜单项不需要底部边距
    .el-menu-item:last-child,
    .el-submenu:last-child {
      margin-bottom: 0;
    }

    // 在菜单项较少时，适当添加一些顶部和底部内边距
    .el-menu-item,
    .el-submenu__title {
      margin-top: 2px;
    }

    // 第一个菜单项不需要顶部边距
    .el-menu-item:first-child,
    .el-submenu:first-child .el-submenu__title {
      margin-top: 0;
    }

    // 侧边栏收缩状态下的菜单项样式
    .hideSidebar & {
      .el-menu-item,
      .el-submenu__title {
        height: 46px;
        line-height: 46px;
      }
    }
  }
}
</style>

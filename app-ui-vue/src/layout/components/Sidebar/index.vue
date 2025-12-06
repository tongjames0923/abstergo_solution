<template>
  <div :class="{'has-logo':showLogo}" class="sidebar-container-macos">
    <logo v-if="showLogo" :collapse="isCollapse"/>
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
        <sidebar-item v-for="route in routes" :key="route.path" :item="route" :base-path="route.path"/>
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script>
import {mapGetters} from 'vuex'
import Logo from './Logo'
import SidebarItem from './SidebarItem'
import variables from '@/styles/variables.scss'

export default {
  components: {SidebarItem, Logo},
  computed: {
    ...mapGetters([
      'sidebar'
    ]),
    routes() {
      return this.$router.options.routes
    },
    activeMenu() {
      const route = this.$route
      const {meta, path} = route
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
  }
}
</script>

<style lang="scss" scoped>
@import "~@/styles/variables.scss";
.sidebar-container-macos {
  background-color: $menuBg;
  border-radius: $sidebarBorderRadius;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
  margin: 8px;
  padding: $sidebarPadding;
  height: calc(100vh - 16px);
  overflow: hidden;
}

.sidebar-menu-macos {
  background-color: transparent !important;
  border-right: none !important;
  border-radius: $sidebarBorderRadius;
  padding: 4px 0;

  .el-menu-item,
  .el-submenu__title {
    height: 36px;
    line-height: 36px;
    margin: 2px 8px;
    border-radius: 12px;
    transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);

    // 默认状态下轻微的玻璃效果
    backdrop-filter: blur(5px);
    -webkit-backdrop-filter: blur(5px);
    background: rgba(255, 255, 255, 0.4);
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.03);
    border: 1px solid rgba(255, 255, 255, 0.2);

    &:hover {
      background: rgba(255, 255, 255, 0.7) !important;
      color: $menuText !important;

      // 增强的玻璃效果和光影
      backdrop-filter: blur(10px);
      -webkit-backdrop-filter: blur(10px);
      box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
      border: 1px solid rgba(255, 255, 255, 0.4);

      // 添加发光效果
      transform: translateY(-1px);
    }

    &.is-active {
      background-color: $menuHover !important;
      color: $menuActiveText !important;
      font-weight: 500;
    }
  }

  .el-submenu {
    .el-submenu__title {
      &:hover {
        background-color: $menuHover !important;
      }
    }

    .el-menu {
      background-color: transparent !important;
      border-radius: 8px;
      margin: 0 8px;
      padding: 4px 0;

      .el-menu-item {
        padding-left: 48px !important;
        margin: 2px 0;
        border-radius: 10px;
      }
    }
  }

  .el-scrollbar {
    height: calc(100% - 50px);
  }
}
</style>

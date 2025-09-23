<template>
  <div :class="classObj" class="app-wrapper">
    <div v-if="device==='mobile'&&sidebar.opened" class="drawer-bg" @click="handleClickOutside" />
    <div
      class="sidebar-container-wrapper"
      ref="sidebarContainerWrapper"
      @click="handleSidebarClick"
    >
      <div class="user-avatar-outside">
        <user-avatar />
      </div>
      <div class="sidebar-container">
        <sidebar />
      </div>
    </div>

    <!-- 左侧面包屑组件 -->
    <div
      v-show="!sidebar.opened"
      class="left-breadcrumb-container"
      :class="{ 'sidebar-hidden': !sidebar.opened }"
    >
      <navbar />
    </div>

    <div
      class="main-container"
      ref="mainContainer"
      @click="handleMainContainerClick"
    >
      <!-- 原来的 navbar-container 已移除 -->
      <app-main />
    </div>
  </div>
</template>

<script>
import {AppMain, Sidebar, Navbar} from './components'
import UserAvatar from '@/components/UserAvatar'
import ResizeMixin from './mixin/ResizeHandler'

export default {
  name: 'Layout',
  components: {
    Sidebar,
    AppMain,
    UserAvatar,
    Navbar
  },
  mixins: [ResizeMixin],
  computed: {
    sidebar() {
      return this.$store.state.app.sidebar
    },
    device() {
      return this.$store.state.app.device
    },
    fixedHeader() {
      return this.$store.state.settings.fixedHeader
    },
    classObj() {
      return {
        hideSidebar: !this.sidebar.opened,
        openSidebar: this.sidebar.opened,
        withoutAnimation: this.sidebar.withoutAnimation,
        mobile: this.device === 'mobile'
      }
    }
  },
  methods: {
    handleClickOutside() {
      this.$store.dispatch('app/closeSideBar', { withoutAnimation: false })
    },
    handleSidebarClick(event) {
      // 只有在侧边栏收起时，点击侧边栏区域才展开
      if (!this.sidebar.opened) {
        this.$store.dispatch('app/toggleSideBar')
      }
    },
    handleMainContainerClick(event) {
      // 只有在侧边栏展开且不是移动端时，点击主容器才收起侧边栏
      if (this.sidebar.opened && this.device !== 'mobile') {
        this.$store.dispatch('app/closeSideBar', { withoutAnimation: false })
      }
    }
  }
}
</script>

<style lang="scss" scoped>
@import "~@/styles/mixin.scss";
@import "~@/styles/variables.scss";

.app-wrapper {
  @include clearfix;
  position: relative;
  height: 100%;
  width: 100%;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4edf9 100%);
  overflow-x: hidden;

  &.mobile.openSidebar {
    position: fixed;
    top: 0;
  }

  // 添加侧边栏动画增强
  &.hideSidebar {
    .sidebar-container-wrapper {
      transition: all 0.4s cubic-bezier(0.64, 0.57, 0.67, 1.53) !important;
    }
  }

  .navbar-container {
    position: absolute;
    top: 0;
    left: calc(#{$sideBarWidth} + 20px);
    z-index: 1000;
    transition: left 0.3s ease;
  }

  .hideSidebar .navbar-container {
    left: calc(#{$sideBarCollapseWidth} + 20px);
  }
}

.drawer-bg {
  background: #000;
  opacity: 0.3;
  width: 100%;
  top: 0;
  height: 100%;
  position: absolute;
  z-index: 999;
  transition: opacity 0.3s cubic-bezier(0.25, 0.1, 0.25, 1);
}

.sidebar-container-wrapper {
  position: fixed;
  width: $sideBarWidth !important;
  height: calc(100% - 20px);
  top: 10px;
  left: 10px;
  z-index: 1001;
  transition: all 0.3s cubic-bezier(0.25, 0.1, 0.25, 1);
  display: flex;
  flex-direction: column;
  will-change: transform;
  transform: translateZ(0);
  backface-visibility: hidden;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.12);
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);

  &.hideSidebar {
    width: $sideBarCollapseWidth !important;
    height: $sideBarMinHeight !important;
    top: 50%;
    transform: translateY(-50%);
    transition: all 0.4s cubic-bezier(0.64, 0.57, 0.67, 1.53);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  }

  &.openSidebar {
    transition: all 0.4s cubic-bezier(0.25, 0.1, 0.25, 1);
  }

  .user-avatar-outside {
    display: flex;
    justify-content: center;
    margin-bottom: 12px;
    flex-shrink: 0;
    will-change: transform;
    transform: translateZ(0);
    backface-visibility: hidden;
  }

  .sidebar-container {
    flex: 1;
    height: calc(100% - 60px);
    width: 100%;
    will-change: transform;
    transform: translateZ(0);
    backface-visibility: hidden;
    overflow: hidden;
  }
}

.main-container {
    height: 100%;
    width: 100%;
    transition: none !important;
    margin-left: 0 !important;
    padding-left: calc(#{$sideBarCollapseWidth} + 50px); // 增加左侧内边距，避免面包屑组件侵占主页面
    box-sizing: border-box;
}
</style>

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
    <div 
      class="main-container" 
      ref="mainContainer"
      @click="handleMainContainerClick"
    >
      <app-main />
    </div>
  </div>
</template>

<script>
import {AppMain, Sidebar} from './components'
import UserAvatar from '@/components/UserAvatar'
import ResizeMixin from './mixin/ResizeHandler'

export default {
  name: 'Layout',
  components: {
    Sidebar,
    AppMain,
    UserAvatar
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
  
  &.openSidebar {
    .sidebar-container-wrapper {
      transition: all 0.4s cubic-bezier(0.25, 0.1, 0.25, 1) !important;
    }
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
  
  &.hideSidebar {
    width: $sideBarCollapseWidth !important;
    height: $sideBarMinHeight !important;
    top: 50%;
    transform: translateY(-50%);
    transition: all 0.4s cubic-bezier(0.64, 0.57, 0.67, 1.53);
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
  }
}
</style>
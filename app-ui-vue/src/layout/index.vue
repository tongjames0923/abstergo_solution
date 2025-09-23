<template>
  <div :class="classObj" class="app-wrapper">
    <div v-if="device==='mobile'&&sidebar.opened" class="drawer-bg" @click="handleClickOutside" />
    <div 
      class="sidebar-container" 
      ref="sidebarContainer"
      @click="handleSidebarClick"
    >
      <sidebar />
    </div>
    <div 
      class="main-container" 
      ref="mainContainer"
      @click="handleMainContainerClick"
    >
      <div :class="{'fixed-header':fixedHeader}">
        <navbar />
      </div>
      <app-main />
    </div>
  </div>
</template>

<script>
import {AppMain, Navbar, Sidebar} from './components'
import ResizeMixin from './mixin/ResizeHandler'

export default {
  name: 'Layout',
  components: {
    Navbar,
    Sidebar,
    AppMain
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
  background-color: #f7f7f7;

  &.mobile.openSidebar {
    position: fixed;
    top: 0;
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
}

.sidebar-container {
  border-radius: 15px;
  margin: 10px;
  height: calc(100% - 20px);
  cursor: pointer;
}

.fixed-header {
  position: fixed;
  top: 0;
  right: 0;
  z-index: 9;
  width: calc(100% - #{$sideBarWidth});
  transition: width 0.28s;
  border-top-left-radius: 15px;
}

.hideSidebar .fixed-header {
  width: calc(100% - 54px)
}

.mobile .fixed-header {
  width: 100%;
}

.main-container {
  cursor: pointer;
  height: 100%;
}
</style>
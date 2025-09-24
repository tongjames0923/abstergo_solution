<template>
  <div :class="classObj" class="app-wrapper">
    <div v-if="device==='mobile'&&sidebar.opened" class="drawer-bg" @click="handleClickOutside"/>
    <div
      class="sidebar-container-wrapper"
      ref="sidebarContainerWrapper"
      @click="handleSidebarClick"
    >
      <div class="user-avatar-outside">
        <user-avatar/>
      </div>
      <!-- 侧边栏收起时显示主页按钮和当前页面名称 -->
      <div v-if="!sidebar.opened" class="collapsed-nav">
        <div class="home-button" @click="goToHome" title="回到首页">
          <i class="el-icon-house"></i>
        </div>
        <div class="current-page-name">
          {{ currentPageName }}
        </div>
      </div>
      <!-- 侧边栏展开时显示完整的面包屑 -->
      <div v-else class="expanded-breadcrumb">
        <capsule-breadcrumb />
      </div>
      <div class="sidebar-container">
        <sidebar/>
      </div>
    </div>

    <div
      class="main-container"
      ref="mainContainer"
      @click="handleMainContainerClick"
    >
      <app-main/>
    </div>
  </div>
</template>

<script>
import {AppMain, Sidebar} from './components'
import UserAvatar from '@/components/UserAvatar'
import CapsuleBreadcrumb from '@/components/Breadcrumb/CapsuleBreadcrumb'
import ResizeMixin from './mixin/ResizeHandler'

export default {
  name: 'Layout',
  components: {
    Sidebar,
    AppMain,
    UserAvatar,
    CapsuleBreadcrumb
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
    },
    currentPageName() {
      const matched = this.$route.matched.filter(item => item.meta && item.meta.title)
      const lastItem = matched[matched.length - 1]
      return lastItem ? lastItem.meta.title : ''
    }
  },
  methods: {
    handleClickOutside() {
      this.$store.dispatch('app/closeSideBar', {withoutAnimation: false})
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
        this.$store.dispatch('app/closeSideBar', {withoutAnimation: false})
      }
    },
    goToHome() {
      this.$router.push('/')
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
  height: 100vh;
  width: 100%;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4edf9 100%);
  overflow: hidden; // 禁止整个页面滚动

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
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);

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

  .collapsed-nav {
    display: flex;
    flex-direction: column;
    align-items: center;
    margin-bottom: 12px;
    transition: all 0.3s cubic-bezier(0.25, 0.1, 0.25, 1);

    .home-button {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 32px;
      height: 32px;
      border-radius: 50%;
      background: rgba(255, 255, 255, 0.3);
      backdrop-filter: blur(6px);
      -webkit-backdrop-filter: blur(6px);
      cursor: pointer;
      transition: all 0.3s ease;
      transform: scale(1);
      margin-bottom: 8px;

      &:hover {
        background: rgba(64, 158, 255, 0.4);
        transform: scale(1.1);
      }

      .el-icon-house {
        font-size: 16px;
        color: $menuText;
      }
    }

    .current-page-name {
      font-size: 12px;
      color: $menuText;
      text-align: center;
      padding: 3px 6px;
      border-radius: 6px;
      background: rgba(255, 255, 255, 0.2);
      backdrop-filter: blur(6px);
      max-width: 70px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }
  }

  .expanded-breadcrumb {
    margin: 0 10px 12px;
    border-radius: 16px;
    overflow: hidden;
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
  padding-left: calc(#{$sideBarCollapseWidth} + 20px); // 减少左侧内边距
  padding-right: 20px; // 添加右侧内边距
  box-sizing: border-box;
  overflow: hidden; // 禁止主容器滚动，让滚动发生在app-main内部
  background: transparent;
}
</style>
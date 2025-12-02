<template>
  <div :class="classObj" class="app-wrapper">
    <div v-if="device==='mobile'&&sidebar.opened" class="drawer-bg" @click="handleClickOutside" />
    <div
      ref="sidebarContainerWrapper"
      :class="{ 'hideSidebar': !sidebar.opened }"
      class="sidebar-container-wrapper"
      @click="handleSidebarClick"
    >
      <div class="user-avatar-outside">
        <user-avatar />
      </div>
      <!-- 侧边栏收起时显示主页按钮和当前页面名称 -->
      <div v-if="!sidebar.opened" class="collapsed-nav">
        <div class="home-button" title="回到首页" @click="goToHome">
          <i class="el-icon-house" />
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
        <sidebar />
      </div>
    </div>

    <div
      class="main-container"
      :style="mainContainerStyle"
    >
      <app-main />
    </div>
  </div>
</template>

<script>
import { AppMain, Sidebar } from './components'
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
      // 优化路由匹配逻辑，减少不必要的计算
      const matched = this.$route.matched
      for (let i = matched.length - 1; i >= 0; i--) {
        if (matched[i].meta && matched[i].meta.title) {
          return matched[i].meta.title
        }
      }
      return ''
    },
    mainContainerStyle() {
      // 动态计算主容器的内边距，根据侧边栏的状态调整
      const paddingLeft = this.sidebar.opened 
        ? 'calc(300px + 24px)' // 侧边栏展开时
        : 'calc(100px + 24px)' // 侧边栏收起时
      
      return {
        paddingLeft: paddingLeft,
        transition: 'padding-left 0.45s cubic-bezier(0.23, 1, 0.32, 1)'
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
      // 阻止事件冒泡，避免触发其他事件
      event.stopPropagation()
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

// 苹果风格主题变量 - MacOS 26设计规范
$appleGlassBg: rgba(255, 255, 255, 0.7);
$appleGlassHover: rgba(255, 255, 255, 0.85);
$appleBorder: rgba(255, 255, 255, 0.3);
$appleShadow: 0 10px 38px rgba(0, 0, 0, 0.1), 0 0 0 1px rgba(0, 0, 0, 0.05);
$appleShadowHover: 0 16px 48px rgba(0, 0, 0, 0.15), 0 0 0 1px rgba(0, 0, 0, 0.08);
$appleInnerShadow: inset 0 1px 0 rgba(255, 255, 255, 0.5), inset 0 0 30px rgba(255, 255, 255, 0.1);
$appleTransition: all 0.45s cubic-bezier(0.23, 1, 0.32, 1);
$appleAccentColor: rgba(64, 158, 255, 0.2);
$appleAccentBorder: rgba(64, 158, 255, 0.3);
$appleAccentShadow: 0 4px 16px rgba(64, 158, 255, 0.15);

.app-wrapper {
  @include clearfix;
  position: relative;
  height: 100vh;
  width: 100%;
  // MacOS 26风格渐变背景
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 25%, #cbd5e1 50%, #94a3b8 100%);
  overflow: hidden; // 禁止整个页面滚动
  // 添加精致的噪点纹理，增强玻璃质感
  &::before {
    content: '';
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 200 200' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='noiseFilter'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.65' numOctaves='3' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23noiseFilter)' opacity='0.03'/%3E%3C/svg%3E");
    pointer-events: none;
    z-index: 0;
  }

  &.mobile.openSidebar {
    position: fixed;
    top: 0;
  }

  // 侧边栏动画增强
  &.hideSidebar {
    .sidebar-container-wrapper {
      transition: $appleTransition !important;
    }
  }

  .navbar-container {
    position: absolute;
    top: 0;
    left: calc(#{$sideBarWidth} + 20px);
    z-index: 1000;
    transition: left $appleTransition;
  }

  .hideSidebar .navbar-container {
    left: calc(#{$sideBarCollapseWidth} + 20px);
  }
}

.drawer-bg {
  // 苹果风格半透明背景
  background: rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(4px);
  -webkit-backdrop-filter: blur(4px);
  width: 100%;
  top: 0;
  height: 100%;
  position: absolute;
  z-index: 999;
  transition: opacity 0.3s cubic-bezier(0.25, 0.1, 0.25, 1);
}

.sidebar-container-wrapper {
    position: fixed;
    width: $sideBarWidth;
    height: calc(100% - 24px);
    top: 12px;
    left: 12px;
    z-index: 1001;
    transition: $appleTransition;
    display: flex;
    flex-direction: column;
    will-change: transform, box-shadow, background, width, height, top;
    transform: translateZ(0);
    backface-visibility: hidden;
    // MacOS 26风格圆角
    border-radius: 24px;
    // 增强的玻璃态效果
    background: $appleGlassBg;
    backdrop-filter: blur(24px) saturate(130%);
    -webkit-backdrop-filter: blur(24px) saturate(130%);
    // 苹果风格阴影
    box-shadow: $appleShadow, $appleInnerShadow;
    // 苹果风格边框
    border: 1px solid $appleBorder;
    // 添加内部发光效果
    &::before {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      border-radius: 24px;
      box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.6), inset 0 0 40px rgba(255, 255, 255, 0.15);
      pointer-events: none;
      z-index: -1;
      transition: box-shadow 0.45s cubic-bezier(0.23, 1, 0.32, 1);
    }

    // 悬停效果增强 - MacOS 26风格
    &:hover {
      box-shadow: $appleShadowHover, $appleInnerShadow;
      background: $appleGlassHover;
      // 添加微妙的上浮效果
      transform: translateY(-2px);
    }

    &.hideSidebar {
      width: $sideBarCollapseWidth;
      height: $sideBarMinHeight;
      top: 50%;
      transform: translateY(-50%);
      // 收起状态下保持精致阴影
      box-shadow: $appleShadow, $appleInnerShadow;
      // 收起状态下简化内部效果
      &::before {
        box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.6);
      }
      
      // 收起状态下移除悬停效果，避免不必要的动画
      &:hover {
        transform: translateY(-50%);
      }
    }

    &.openSidebar {
      transition: $appleTransition;
    }

  .user-avatar-outside {
    display: flex;
    justify-content: center;
    margin: 16px 0 12px;
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
    transition: $appleTransition;

    .home-button {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 40px;
      height: 40px;
      border-radius: 14px;
      background: rgba(255, 255, 255, 0.5);
      backdrop-filter: blur(12px) saturate(120%);
      -webkit-backdrop-filter: blur(12px) saturate(120%);
      cursor: pointer;
      transition: $appleTransition;
      transform: scale(1);
      margin-bottom: 10px;
      box-shadow: 0 3px 12px rgba(0, 0, 0, 0.08);
      border: 1px solid rgba(255, 255, 255, 0.4);

      &:hover {
        background: rgba(64, 158, 255, 0.2);
        transform: scale(1.08);
        box-shadow: 0 5px 20px rgba(64, 158, 255, 0.25);
        border-color: $appleAccentBorder;
      }

      .el-icon-house {
        font-size: 20px;
        color: $menuText;
      }
    }

    .current-page-name {
      font-size: 13px;
      color: $menuText;
      text-align: center;
      padding: 6px 12px;
      border-radius: 14px;
      background: rgba(255, 255, 255, 0.6);
      backdrop-filter: blur(12px) saturate(120%);
      -webkit-backdrop-filter: blur(12px) saturate(120%);
      max-width: 90px;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
      box-shadow: 0 3px 12px rgba(0, 0, 0, 0.06);
      border: 1px solid rgba(255, 255, 255, 0.3);
    }
  }

  .expanded-breadcrumb {
    margin: 0 12px 12px;
    border-radius: 18px;
    overflow: hidden;
    // 为面包屑添加玻璃效果
    background: rgba(255, 255, 255, 0.5);
    backdrop-filter: blur(12px) saturate(120%);
    -webkit-backdrop-filter: blur(12px) saturate(120%);
    box-shadow: 0 3px 12px rgba(0, 0, 0, 0.06);
    border: 1px solid rgba(255, 255, 255, 0.3);
  }

  .sidebar-container {
    flex: 1;
    height: calc(100% - 60px);
    width: 100%;
    will-change: transform;
    transform: translateZ(0);
    backface-visibility: hidden;
    overflow: hidden;
    // 侧边栏内容区域添加精致边框
    border-top: 1px solid rgba(255, 255, 255, 0.4);
    border-bottom: 1px solid rgba(255, 255, 255, 0.4);
    transition: height 0.45s cubic-bezier(0.23, 1, 0.32, 1);
    
    // 侧边栏收起时调整高度
    .hideSidebar & {
      height: calc(100% - 80px);
    }
  }
}

// 侧边栏内部样式增强
:deep(.glass-sidebar) {
  background: transparent;
  border-radius: 0 0 24px 24px;
  overflow: hidden;
}

// 菜单样式增强 - MacOS 26风格
:deep(.el-menu) {
  background: transparent !important;
  border-right: none !important;
  // 增强菜单容器的玻璃效果
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
}

:deep(.el-menu-item),
:deep(.el-submenu__title) {
  border-radius: 14px !important;
  margin: 6px 8px !important;
  transition: $appleTransition !important;
  border: 1px solid transparent;
  // 增强文字对比度
  color: rgba(30, 41, 59, 0.85) !important;

  &:hover {
    background: rgba(255, 255, 255, 0.7) !important;
    box-shadow: 0 6px 20px rgba(0, 0, 0, 0.08) !important;
    border-color: rgba(255, 255, 255, 0.5) !important;
    // 增强文字颜色
    color: rgba(30, 41, 59, 0.95) !important;
    // 添加微妙的上浮效果
    transform: translateY(-1px);
  }

  &.is-active {
    background: $appleAccentColor !important;
    box-shadow: $appleAccentShadow !important;
    border-color: $appleAccentBorder !important;
    // 增强激活状态的文字颜色
    color: rgba(30, 41, 59, 0.95) !important;
    // 添加激活状态的上浮效果
    transform: translateY(-1px);
  }
}

:deep(.el-submenu .el-menu) {
  background: transparent !important;
  padding: 6px 0 !important;
}

:deep(.el-submenu .el-menu-item) {
  margin: 3px 16px !important;
  border-radius: 10px !important;
  // 子菜单的特殊样式
  color: rgba(30, 41, 59, 0.85) !important; // 确保文字颜色正确
}

.main-container {
    height: 100%;
    width: 100%;
    margin-left: 0 !important;
    padding-right: 12px;
    padding-top: 12px;
    padding-bottom: 12px;
    box-sizing: border-box;
    overflow: hidden; // 禁止主容器滚动，让滚动发生在app-main内部
    background: transparent;
  }
</style>

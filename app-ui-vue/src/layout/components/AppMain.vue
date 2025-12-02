<template>
  <section ref="appMain" class="app-main">
    <transition name="fade-transform" mode="out-in">
      <router-view :key="key" />
    </transition>
  </section>
</template>

<script>
export default {
  name: 'AppMain',
  computed: {
    key() {
      return this.$route.path
    }
  },
  mounted() {
    const appMain = this.$refs.appMain
    if (appMain) {
      // 监听滚动事件，添加视觉效果
      appMain.addEventListener('scroll', this.handleScroll)

      // 初始化时检查滚动位置
      this.handleScroll.call({ $el: appMain })
    }
  },
  beforeDestroy() {
    const appMain = this.$refs.appMain
    if (appMain) {
      appMain.removeEventListener('scroll', this.handleScroll)
    }
  },
  methods: {
    handleScroll(event) {
      const element = event ? event.target : this.$el
      const scrollTop = element.scrollTop
      const scrollHeight = element.scrollHeight
      const clientHeight = element.clientHeight

      // 移除所有滚动相关的类
      element.classList.remove('scroll-shadow-top', 'scroll-shadow-bottom', 'scroll-shadow-both')

      // 根据滚动位置添加相应的类
      if (scrollTop > 0 && scrollTop + clientHeight < scrollHeight) {
        // 既不是顶部也不是底部
        element.classList.add('scroll-shadow-both')
      } else if (scrollTop > 0) {
        // 不在顶部
        element.classList.add('scroll-shadow-top')
      } else if (scrollTop + clientHeight < scrollHeight) {
        // 不在底部
        element.classList.add('scroll-shadow-bottom')
      }
    }
  }
}
</script>

<style lang="scss" scoped>
// MacOS 26风格主题变量
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

.app-main {
  /*50 = navbar  */
  min-height: calc(100vh - 50px);
  width: 100%;
  position: relative;
  overflow: auto; /* 允许app-main内部滚动 */
  // MacOS 26风格玻璃态背景
  background: $appleGlassBg;
  // MacOS 26风格圆角
  border-radius: 24px;
  margin: 0;
  margin-left: 0;
  // 增强的玻璃态效果
  backdrop-filter: blur(24px) saturate(130%);
  -webkit-backdrop-filter: blur(24px) saturate(130%);
  // MacOS 26风格阴影
  box-shadow: $appleShadow, $appleInnerShadow;
  // 确保高度正确
  height: calc(100vh - 24px);
  // MacOS 26风格边框
  border: 1px solid $appleBorder;
  // 增强的过渡效果
  transition: $appleTransition;
  will-change: transform, box-shadow, background;
  transform: translateZ(0);
  backface-visibility: hidden;

  // 悬停效果增强 - MacOS 26风格
  &:hover {
    box-shadow: $appleShadowHover, $appleInnerShadow;
    background: $appleGlassHover;
    // 添加微妙的上浮效果
    transform: translateY(-2px);
  }

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    border-radius: 24px;
    // 更精致的内阴影和发光效果
    box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.6), inset 0 0 40px rgba(255, 255, 255, 0.15);
    pointer-events: none;
    z-index: -1;
  }

  // 优化滚动条样式
  &::-webkit-scrollbar {
    width: 10px;
    height: 10px;
  }

  &::-webkit-scrollbar-thumb {
    // MacOS 26风格滚动条拇指
    background: rgba(148, 163, 184, 0.4);
    border-radius: 12px;
    transition: all 0.3s ease;
    border: 3px solid transparent;
    background-clip: content-box;
  }

  &::-webkit-scrollbar-thumb:hover {
    // 悬停时的滚动条拇指
    background: rgba(148, 163, 184, 0.6);
    border: 2px solid transparent;
    background-clip: content-box;
  }

  &::-webkit-scrollbar-track {
    // MacOS 26风格滚动条轨道
    background: transparent;
    border-radius: 12px;
    margin: 8px 0;
  }

  // 滚动条角落样式
  &::-webkit-scrollbar-corner {
    background: transparent;
  }
}

.fixed-header+.app-main {
  padding-top: 50px;
}
</style>

<style lang="scss">
// 苹果风格主题变量（用于全局样式）
$appleShadowHover: 0 12px 40px rgba(0, 0, 0, 0.12), 0 0 0 1px rgba(0, 0, 0, 0.06);

// 自定义苹果风格滚动条
.app-main::-webkit-scrollbar {
  width: 8px;
}

.app-main::-webkit-scrollbar-thumb {
  // 苹果风格滚动条拇指
  background: rgba(142, 142, 147, 0.4);
  border-radius: 10px;
  transition: all 0.3s ease;
  border: 2px solid transparent;
  background-clip: content-box;
}

.app-main::-webkit-scrollbar-thumb:hover {
  // 悬停时的滚动条拇指
  background: rgba(142, 142, 147, 0.6);
  border: 1px solid transparent;
  background-clip: content-box;
}

.app-main::-webkit-scrollbar-track {
  // 苹果风格滚动条轨道
  background: transparent;
  border-radius: 10px;
}

// 页面滚动时的视觉效果
.app-main {
  scroll-behavior: smooth;

  // 滚动时顶部阴影效果
  &.scroll-shadow-top {
    box-shadow: $appleShadowHover,
    inset 0 0 30px rgba(255, 255, 255, 0.15),
    inset 0 5px 10px -5px rgba(0, 0, 0, 0.08);
  }

  // 滚动时底部阴影效果
  &.scroll-shadow-bottom {
    box-shadow: $appleShadowHover,
    inset 0 0 30px rgba(255, 255, 255, 0.15),
    inset 0 -5px 10px -5px rgba(0, 0, 0, 0.08);
  }

  // 同时有顶部和底部滚动阴影
  &.scroll-shadow-both {
    box-shadow: $appleShadowHover,
    inset 0 0 30px rgba(255, 255, 255, 0.15),
    inset 0 5px 10px -5px rgba(0, 0, 0, 0.08),
    inset 0 -5px 10px -5px rgba(0, 0, 0, 0.08);
  }
}

// 优化过渡动画
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.4s cubic-bezier(0.23, 1, 0.32, 1);
}

.fade-transform-enter,
.fade-transform-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style>

<template>
  <section class="app-main" ref="appMain">
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
    const appMain = this.$refs.appMain;
    if (appMain) {
      // 监听滚动事件，添加视觉效果
      appMain.addEventListener('scroll', this.handleScroll);

      // 初始化时检查滚动位置
      this.handleScroll.call({$el: appMain});
    }
  },
  methods: {
    handleScroll(event) {
      const element = event ? event.target : this.$el;
      const scrollTop = element.scrollTop;
      const scrollHeight = element.scrollHeight;
      const clientHeight = element.clientHeight;

      // 移除所有滚动相关的类
      element.classList.remove('scroll-shadow-top', 'scroll-shadow-bottom', 'scroll-shadow-both');

      // 根据滚动位置添加相应的类
      if (scrollTop > 0 && scrollTop + clientHeight < scrollHeight) {
        // 既不是顶部也不是底部
        element.classList.add('scroll-shadow-both');
      } else if (scrollTop > 0) {
        // 不在顶部
        element.classList.add('scroll-shadow-top');
      } else if (scrollTop + clientHeight < scrollHeight) {
        // 不在底部
        element.classList.add('scroll-shadow-bottom');
      }
    }
  },
  beforeDestroy() {
    const appMain = this.$refs.appMain;
    if (appMain) {
      appMain.removeEventListener('scroll', this.handleScroll);
    }
  }
}
</script>

<style scoped>
.app-main {
  /*50 = navbar  */
  min-height: calc(100vh - 50px);
  width: 100%;
  position: relative;
  overflow: auto; /* 允许app-main内部滚动 */
  background: rgba(230, 240, 255, 0.8); /* 更明显的淡蓝色背景 */
  border-radius: 15px; /* 增大圆角 */
  margin: 10px;
  margin-left: 0;
  backdrop-filter: blur(15px); /* 增强模糊效果 */
  -webkit-backdrop-filter: blur(15px);
  box-shadow: 0 12px 40px rgba(64, 158, 255, 0.15); /* 增强阴影效果 */
  height: calc(100vh - 20px); /* 确保高度正确 */
  border: 1px solid rgba(255, 255, 255, 0.5);

  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    border-radius: 15px;
    box-shadow: inset 0 0 20px rgba(64, 158, 255, 0.1);
    pointer-events: none;
    z-index: -1;
  }
}

.fixed-header+.app-main {
  padding-top: 50px;
}
</style>

<style lang="scss">
// 自定义滚动条样式
.app-main::-webkit-scrollbar {
  width: 6px;
}

.app-main::-webkit-scrollbar-thumb {
  background: linear-gradient(180deg, rgba(64, 158, 255, 0.6), rgba(64, 158, 255, 0.3));
  border-radius: 4px;
  transition: all 0.3s ease;
  border: 2px solid transparent;
  background-clip: content-box;
}

.app-main::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(180deg, rgba(64, 158, 255, 0.8), rgba(64, 158, 255, 0.4));
  border: 1px solid transparent;
  background-clip: content-box;
}

.app-main::-webkit-scrollbar-track {
  background: transparent;
  border-radius: 4px;
}

// 页面滚动时的视觉效果
.app-main {
  scroll-behavior: smooth;

  // 滚动时顶部阴影效果
  &.scroll-shadow-top {
    box-shadow: 0 12px 40px rgba(64, 158, 255, 0.15),
    inset 0 0 20px rgba(64, 158, 255, 0.1),
    inset 0 5px 10px -5px rgba(0, 0, 0, 0.1);
  }

  // 滚动时底部阴影效果
  &.scroll-shadow-bottom {
    box-shadow: 0 12px 40px rgba(64, 158, 255, 0.15),
    inset 0 0 20px rgba(64, 158, 255, 0.1),
    inset 0 -5px 10px -5px rgba(0, 0, 0, 0.1);
  }

  // 同时有顶部和底部滚动阴影
  &.scroll-shadow-both {
    box-shadow: 0 12px 40px rgba(64, 158, 255, 0.15),
    inset 0 0 20px rgba(64, 158, 255, 0.1),
    inset 0 5px 10px -5px rgba(0, 0, 0, 0.1),
    inset 0 -5px 10px -5px rgba(0, 0, 0, 0.1);
  }
}
</style>

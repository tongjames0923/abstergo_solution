<template>
  <section class="app-main">
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
  }
}
</script>

<style scoped>
.app-main {
  /*44 = navbar height */
  min-height: calc(100vh - 44px);
  width: 100%;
  position: relative;
  overflow: hidden;
  padding: 0;
  /* 浅灰底色使白色卡片具备层次感，与侧边栏 #f0f0f0 同系 */
  background: #f5f5f7;
  /*
   * 组件级响应式锚点：后代可用 @container 按内容区宽度适配，
   * 不依赖视口。声明等价于 contain:layout，页面内 position:fixed 浮层
   * 会改为相对内容区定位（背景装饰层符合预期，弹层请用 append-to-body）
   */
  container-type: inline-size;
}

.fixed-header + .app-main {
  padding-top: 44px; /* navbar height */
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-main {
    padding: 0;
    min-height: calc(100vh - 44px);
  }

  .fixed-header + .app-main {
    padding-top: 44px;
  }
}

/* 手机横屏：导航栏压缩为 38px，内容区同步偏移 */
@media only screen and (orientation: landscape) and (max-width: 896px) {
  .app-main {
    min-height: calc(100vh - 38px);
  }

  .fixed-header + .app-main {
    padding-top: 38px;
  }
}
</style>

<style lang="scss">
// fix css style bug in open el-dialog
.el-popup-parent--hidden {
  .fixed-header {
    padding-right: 15px;
  }
}
</style>

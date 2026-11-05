<template>
  <section class="app-main">
    <transition name="fade-transform" mode="out-in">
      <keep-alive :max="10">
        <router-view :key="viewKey" />
      </keep-alive>
    </transition>
  </section>
</template>

<script>
export default {
  name: 'AppMain',
  computed: {
    /**
     * 页面实例缓存键：以 fullPath 为标识，与页签栏的页签一一对应，
     * 切换页签时保留各自状态；拼上 tagsView 的刷新序号，
     * 页签右键「刷新页面」递增序号即可强制重建组件实例。
     * ponytail: 只按 max 上限回收缓存实例，关闭页签不即时销毁（升级路径：页面组件补 name 后改用 include）
     */
    viewKey() {
      const seq = this.$store.state.tagsView.refreshSeq[this.$route.fullPath]
      return `${this.$route.fullPath}@${seq || 0}`
    }
  }
}
</script>

<style scoped>
.app-main {
  /*78 = navbar 44 + tags-view 34 */
  min-height: calc(100vh - 78px);
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
  padding-top: 78px; /* navbar height + tags-view height */
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-main {
    padding: 0;
    min-height: calc(100vh - 78px);
  }

  .fixed-header + .app-main {
    padding-top: 78px;
  }
}

/* 手机横屏：导航栏压缩为 38px，内容区同步偏移 */
@media only screen and (orientation: landscape) and (max-width: 896px) {
  .app-main {
    min-height: calc(100vh - 72px);
  }

  .fixed-header + .app-main {
    padding-top: 72px;
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

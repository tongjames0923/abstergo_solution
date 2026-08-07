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
</style>

<style lang="scss">
// fix css style bug in open el-dialog
.el-popup-parent--hidden {
  .fixed-header {
    padding-right: 15px;
  }
}
</style>

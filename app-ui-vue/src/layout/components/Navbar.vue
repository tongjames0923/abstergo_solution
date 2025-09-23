<template>
  <div class="navbar">
    <capsule-breadcrumb class="breadcrumb-container" />
  </div>
</template>

<script>
import {mapGetters} from 'vuex'
import CapsuleBreadcrumb from '@/components/Breadcrumb/CapsuleBreadcrumb'

export default {
  components: {
    CapsuleBreadcrumb
  },
  computed: {
    ...mapGetters([
      'sidebar'
    ])
  }
}
</script>

<style lang="scss" scoped>
@import "~@/styles/variables.scss";

.navbar {
  height: 40px; // 进一步减小Navbar高度
  overflow: visible;
  position: relative;
  background: rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 14px; // 调整圆角以适应更小的高度
  margin: 6px 0; // 进一步减小上下边距
  z-index: 1000;
  display: inline-flex;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
  transition: all 0.3s cubic-bezier(0.25, 0.1, 0.25, 1);
  max-width: 100%;

  .breadcrumb-container {
    height: 100%;
    display: flex;
    align-items: center;
    padding: 0 10px; // 进一步减小内边距
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}
</style>
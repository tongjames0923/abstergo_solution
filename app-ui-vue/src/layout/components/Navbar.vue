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
  height: 50px;
  overflow: visible;
  position: relative;
  background: rgba(255, 255, 255, 0.3);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 18px;
  margin: 10px 0;
  z-index: 1000;
  display: inline-flex;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s cubic-bezier(0.25, 0.1, 0.25, 1);

  .breadcrumb-container {
    height: 100%;
    display: flex;
    align-items: center;
    padding: 0 16px;
  }
}
</style>
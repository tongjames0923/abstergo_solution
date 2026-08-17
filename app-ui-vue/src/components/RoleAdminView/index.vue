<template>
  <permission-control-panel />
</template>

<script>
import PermissionControlPanel from '@/components/PermissionControlPanel/index.vue'

export default {
  name: 'RoleAdminView',
  components: {
    PermissionControlPanel
  }
}
</script>

<style lang="scss" scoped>
/* Original styles are now handled by PermissionControlPanel component */
</style>

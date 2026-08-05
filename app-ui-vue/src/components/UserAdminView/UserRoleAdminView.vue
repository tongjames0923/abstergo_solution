<template>
  <user-permission-panel :user-id="userId" />
</template>

<script>
import UserPermissionPanel from './UserPermissionPanel.vue'

export default {
  name: 'UserRoleAdminView',
  components: {
    UserPermissionPanel
  },
  props: {
    userId: {
      type: [Number, String],
      required: true
    }
  }
}
</script>

<style scoped lang="scss">
/* Original styles are now handled by UserPermissionPanel component */
</style>

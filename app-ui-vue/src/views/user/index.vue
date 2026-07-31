<template>
  <div class="app-container">
    <user-management-view />
  </div>
</template>
<script lang="ts">
import Vue from 'vue'
import UserManagementView from '@/components/UserManagementView/index.vue'

export default Vue.extend({
  components: { UserManagementView }
})
</script>

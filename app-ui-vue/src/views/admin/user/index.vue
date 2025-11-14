<template>
  <div class="app-container">
    <user-admin-view/>
  </div>
</template>
<script lang="ts">
import Vue from 'vue'
import UserAdminView from '@/components/UserAdminView/index.vue'

export default Vue.extend({
  components: {UserAdminView}
})
</script>

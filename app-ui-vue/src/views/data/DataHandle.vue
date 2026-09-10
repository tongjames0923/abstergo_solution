<template>
  <div class="data-handle-container">
    <data-handle-panel />
  </div>
</template>

<script>
import { DataHandlePanel } from '@/components/DataHandleUpload'

export default {
  name: 'DataHandle',
  components: {
    DataHandlePanel
  }
}
</script>

<style lang="scss" scoped>
.data-handle-container {
  padding: 16px;
}
</style>

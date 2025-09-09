<template>
  <div class="dashboard-container">
    <el-input v-model="val" type="number" />
    <el-button @click="click()">test</el-button>
    <el-button @click="simpleTest()">自我测试</el-button>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import QuickUseManagementView from '@/components/QuickUseManagementView/index.vue'
import request from '@/utils/request'
import { getInfo } from '@/api/user'

export default {
  name: 'Dashboard',
  components: { QuickUseManagementView },
  computed: {
    ...mapGetters([
      'name'
    ])
  },
  data() {
    return {
      val: 0
    }
  },
  methods: {
    simpleTest() {
      getInfo().then(res => {
        this.$message.success(res.data)
      })
    },
    click() {
      request('private/test', {
        method: 'get',
        params: {
          value: this.val
        }
      }).then(res => {
        this.$message.success(res)
      }).catch(err => {
        debugger
        console.error(err)
        this.$message.error(err.message)
      })
    }
  }
}
</script>

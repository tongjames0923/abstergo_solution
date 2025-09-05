<template>
  <div class="dashboard-container">
    <el-input type="number" v-model="val"></el-input>
    <el-button @click="click()">test</el-button>
  </div>
</template>

<script>
import {mapGetters} from 'vuex'
import QuickUseManagementView from "@/components/QuickUseManagementView/index.vue"
import request from "@/utils/request";

export default {
  name: 'Dashboard',
  components: {QuickUseManagementView},
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
    click() {
      request("private/test", {
        method: "get",
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
    },
  }
}
</script>

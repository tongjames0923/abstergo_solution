<template>
  <el-dropdown trigger="click" class="international" @command="handleSetLanguage">
    <div>
      <i class="fa fa-globe" style="font-size: 20px; cursor: pointer;" />
    </div>
    <el-dropdown-menu slot="dropdown">
      <el-dropdown-item :disabled="language === 'zh'" command="zh">
        中文
      </el-dropdown-item>
      <el-dropdown-item :disabled="language === 'en'" command="en">
        English
      </el-dropdown-item>
    </el-dropdown-menu>
  </el-dropdown>
</template>

<script>
export default {
  computed: {
    language() {
      return this.$store.getters.language || this.$i18n.locale
    }
  },
  methods: {
    handleSetLanguage(lang) {
      this.$i18n.locale = lang
      this.$store.dispatch('app/setLanguage', lang)
      this.$message({
        message: lang === 'zh' ? '切换语言成功' : 'Switch Language Success',
        type: 'success'
      })
    }
  }
}
</script>

<style scoped>
.international {
  display: inline-block;
  cursor: pointer;
  fill: #5a5e66;
  vertical-align: 10px;
}

.international .el-icon-globe {
  font-size: 20px;
}
</style>

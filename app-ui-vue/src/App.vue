<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script>
/**
 * 应用根组件：负责推送连接的卸载兜底
 * <p>
 * 登录成功后由 user/getInfo 动作触发推送建连；此处仅注册页面卸载事件，
 * 确保关闭/刷新页面时主动断开 SSE 长连接，避免孤儿连接。
 * </p>
 */
export default {
  name: 'App',
  mounted() {
    // 已登录且用户态就绪时的幂等兜底（权限守卫已触发过则此处不重复建连）
    const { token, userName } = this.$store.state.user
    if (token && userName) {
      this.$store.dispatch('push/startConnection')
    }
    window.addEventListener('beforeunload', this.handleBeforeUnload)
  },
  beforeDestroy() {
    window.removeEventListener('beforeunload', this.handleBeforeUnload)
  },
  methods: {
    handleBeforeUnload() {
      this.$store.dispatch('push/stopConnection')
    }
  }
}
</script>

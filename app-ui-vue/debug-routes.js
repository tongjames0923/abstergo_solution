// 路由调试脚本
// 在浏览器控制台中运行此脚本来检查路由状态

function debugRoutes() {
  console.log('=== 路由调试信息 ===');

  // 检查路由实例
  console.log('路由实例:', window.$router);
  console.log('路由配置:', window.$router?.options?.routes);

  // 检查用户信息
  console.log('Vuex store:', window.$store);
  console.log('用户角色:', window.$store?.getters?.roles);
  console.log('用户信息:', window.$store?.getters);

  // 检查当前路由
  console.log('当前路由:', window.$route);

  // 检查所有可用视图组件
  const viewsContext = require.context('@/views', true, /\.vue$/);
  console.log('可用视图组件:', viewsContext.keys());
}

// 自动运行调试
if (typeof window !== 'undefined' && window.$router) {
  debugRoutes();
} else {
  console.log('请在浏览器控制台中运行此脚本');
}

// 导出函数供控制台使用
if (typeof window !== 'undefined') {
  window.debugRoutes = debugRoutes;
}

module.exports = { debugRoutes };

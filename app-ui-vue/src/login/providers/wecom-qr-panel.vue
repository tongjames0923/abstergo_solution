<template>
  <div class="wecom-qr-panel">
    <h3 class="qr-title">{{ $t('login.qr.title') }}</h3>
    <p class="qr-hint">{{ $t('login.qr.hint') }}</p>
    <div ref="qrContainer" class="qr-container">
      <div class="qr-placeholder">
        <i class="el-icon-mobile-phone" />
        <span>{{ $t('login.qr.placeholder') }}</span>
      </div>
    </div>
    <p class="qr-tip">{{ $t('login.qr.tip') }}</p>
  </div>
</template>

<script>
export default {
  name: 'WecomQrPanel',
  props: {
    // 宿主注入的 LoginContext：扫码换取 token 后经 loginWithToken 收口
    context: {
      type: Object,
      required: true
    }
  },
  mounted() {
    // TODO 接入企业微信内嵌二维码登录 SDK（wwLogin）：
    // 1. 动态加载 https://wwcdn.weixin.qq.com/node/open/js/wecom-login-{version}.js
    // 2. 读取 login.config.js 中 wecom-qr 的 corpId/agentId，在 this.$refs.qrContainer 内创建登录实例
    // 3. SDK 回调获得 code 后，经 request.js 调后端换票接口取得 token，
    //    调用 this.context.loginWithToken({ token })，由宿主统一完成后置链路
    // 4. 二维码过期时重建登录实例实现刷新
  }
}
</script>

<style lang="scss" scoped>
/* 扫码面板：延续登录卡片 Apple 风格细节 */
.wecom-qr-panel {
  text-align: center;

  .qr-title {
    margin: 0 0 6px;
    font-size: 16px;
    font-weight: 600;
    letter-spacing: -0.01em;
    color: #1d1d1f;
  }

  .qr-hint {
    margin: 0 0 14px;
    font-size: 12px;
    color: #86868b;
  }

  .qr-container {
    width: 200px;
    height: 200px;
    margin: 0 auto;
    border-radius: 14px;
    border: 1px solid rgba(0, 0, 0, 0.06);
    background-color: #f5f5f7;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  .qr-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
    color: #9a9aa0;
    font-size: 11px;

    i {
      font-size: 28px;
      color: #c7c7cc;
    }
  }

  .qr-tip {
    margin: 12px 0 0;
    font-size: 11px;
    color: #9a9aa0;
  }
}
</style>

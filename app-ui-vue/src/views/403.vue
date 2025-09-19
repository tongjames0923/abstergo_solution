<template>
  <div class="forbidden-container">
    <div class="forbidden-box" :class="{ 'fade-in': isLoaded }">
      <div class="forbidden-content">
        <div class="forbidden-header">
          <div class="forbidden-icon">
            <i class="el-icon-warning-outline" />
          </div>
          <h1 class="forbidden-title">403</h1>
          <p class="forbidden-subtitle">访问被拒绝</p>
        </div>

        <div class="forbidden-description">
          <p>抱歉，您没有权限访问该页面</p>
          <p v-if="fromPage" class="from-page-info">来自页面: {{ fromPage }}</p>
        </div>

        <div class="forbidden-actions">
          <el-button
            type="primary"
            round
            class="action-button home-button"
            @click="goHome"
          >
            <i class="el-icon-house" />
            返回首页
          </el-button>
          <el-button
            round
            class="action-button back-button"
            @click="goBack"
          >
            <i class="el-icon-back" />
            返回上页
          </el-button>
        </div>

        <div class="forbidden-footer">
          <p>© 2025 {{ systemTitle }}. 保留所有权利.</p>
        </div>
      </div>

      <div class="forbidden-decoration">
        <div class="decoration-content">
          <div class="decoration-icon">
            <i class="el-icon-lock float-animation" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import settings from '@/settings'

export default {
  name: 'Page403',
  data() {
    return {
      isLoaded: false,
      systemTitle: settings.title || '企业管理平台'
    }
  },
  computed: {
    fromPage() {
      return this.$route.query.from || ''
    }
  },
  mounted() {
    // 添加淡入动画
    this.$nextTick(() => {
      setTimeout(() => {
        this.isLoaded = true
      }, 100)
    })
  },
  methods: {
    goHome() {
      this.$router.push('/')
    },
    goBack() {
      if (window.history.length > 1) {
        this.$router.go(-1)
      } else {
        this.$router.push('/')
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.forbidden-container {
  min-height: 100vh;
  width: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  box-sizing: border-box;
}

.forbidden-box {
  display: flex;
  width: 900px;
  height: 500px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2);
  overflow: hidden;
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.5s ease-out;

  &.fade-in {
    opacity: 1;
    transform: translateY(0);
  }
}

.forbidden-content {
  flex: 1;
  padding: 50px 40px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.forbidden-header {
  text-align: center;
  margin-bottom: 30px;
}

.forbidden-icon {
  font-size: 64px;
  color: #f56c6c;
  margin-bottom: 20px;
  animation: float 3s ease-in-out infinite;
}

.forbidden-title {
  font-size: 48px;
  font-weight: 700;
  color: #f56c6c;
  margin: 0 0 10px 0;
}

.forbidden-subtitle {
  font-size: 20px;
  color: #606266;
  margin: 0 0 20px 0;
}

.forbidden-description {
  text-align: center;
  margin-bottom: 40px;

  p {
    font-size: 16px;
    color: #909399;
    margin: 10px 0;
  }

  .from-page-info {
    font-size: 14px;
    background: #f5f7fa;
    padding: 8px 15px;
    border-radius: 4px;
    color: #606266;
  }
}

.forbidden-actions {
  display: flex;
  gap: 20px;
  margin-bottom: 40px;

  .action-button {
    height: 46px;
    padding: 0 30px;
    font-size: 16px;
    border-radius: 6px;
    display: flex;
    align-items: center;
    gap: 8px;

    &.home-button {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border: none;
      color: white;

      &:hover {
        opacity: 0.9;
        box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
        transform: translateY(-2px);
      }
    }

    &.back-button {
      border: 1px solid #dcdfe6;

      &:hover {
        border-color: #409eff;
        color: #409eff;
      }
    }
  }
}

.forbidden-footer {
  text-align: center;
  margin-top: auto;

  p {
    font-size: 12px;
    color: #909399;
    margin: 0;
  }
}

.forbidden-decoration {
  width: 40%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  padding: 30px;
  box-sizing: border-box;
}

.decoration-content {
  text-align: center;
}

.decoration-icon {
  font-size: 64px;
  margin-bottom: 20px;
}

.float-animation {
  animation: float 3s ease-in-out infinite;
  display: inline-block;
}

@media (max-width: 768px) {
  .forbidden-box {
    flex-direction: column;
    height: auto;
    width: 90%;
  }

  .forbidden-decoration {
    width: 100%;
    padding: 30px 20px;
  }

  .forbidden-content {
    padding: 30px 20px;
  }

  .forbidden-actions {
    flex-direction: column;
    gap: 15px;
    width: 100%;

    .action-button {
      width: 100%;
      justify-content: center;
    }
  }
}

@keyframes float {
  0% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-15px);
  }
  100% {
    transform: translateY(0px);
  }
}
</style>

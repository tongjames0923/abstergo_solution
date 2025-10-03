<template>
  <div class="user-avatar-container" @click.stop>
    <el-dropdown trigger="click" placement="bottom-start" @command="handleCommand">
      <div class="avatar-wrapper">
        <div class="avatar-inner">
          <img :src="avatar+'?imageView2/1/w/80/h/80'" class="user-avatar">
        </div>
        <div class="avatar-ring" />
        <div class="avatar-ping">
          <div class="ping-dot" />
        </div>
      </div>
      <el-dropdown-menu slot="dropdown" class="user-dropdown-menu">
        <el-dropdown-item command="logout">
          <i class="el-icon-switch-button" />
          <span>退出登录</span>
        </el-dropdown-item>
      </el-dropdown-menu>
    </el-dropdown>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'UserAvatar',
  computed: {
    ...mapGetters([
      'avatar'
    ])
  },
  methods: {
    async handleCommand(command) {
      if (command === 'logout') {
        try {
          await this.$confirm('确定要退出登录吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          })
          await this.$store.dispatch('user/logout')
          this.$router.push(`/login?redirect=${this.$route.fullPath}`)
        } catch (error) {
          // 用户取消操作
          console.log('用户取消退出操作')
        }
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.user-avatar-container {
  .avatar-wrapper {
    cursor: pointer;
    position: relative;
    width: 46px;
    height: 46px;
    display: flex;
    align-items: center;
    justify-content: center;

    .avatar-inner {
      width: 42px;
      height: 42px;
      border-radius: 50%;
      overflow: hidden;
      z-index: 2;
      position: relative;
      transition: all 0.3s cubic-bezier(0.25, 0.1, 0.25, 1);
      transform: translateZ(0);
      backface-visibility: hidden;

      .user-avatar {
        width: 100%;
        height: 100%;
        object-fit: cover;
        transition: all 0.3s cubic-bezier(0.25, 0.1, 0.25, 1);
      }
    }

    .avatar-ring {
      position: absolute;
      width: 46px;
      height: 46px;
      border-radius: 50%;
      background: linear-gradient(135deg, rgba(102, 126, 234, 0.6), rgba(118, 75, 162, 0.6));
      z-index: 1;
      animation: rotate 3s linear infinite;
      transition: all 0.3s cubic-bezier(0.25, 0.1, 0.25, 1);
      transform: translateZ(0);
      backface-visibility: hidden;
    }

    .avatar-ping {
      position: absolute;
      top: 0;
      right: 0;
      width: 14px;
      height: 14px;
      z-index: 3;
      display: flex;
      align-items: center;
      justify-content: center;

      .ping-dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        background: #42b983;
        box-shadow: 0 0 0 0 rgba(66, 185, 131, 0.7);
        animation: pulse 1.5s infinite ease-in-out;
      }
    }

    &:hover {
      .avatar-inner {
        transform: scale(1.05);
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
      }

      .avatar-ring {
        transform: scale(1.1) rotate(90deg);
      }
    }

    &:active {
      .avatar-inner {
        transform: scale(0.95);
      }
    }
  }
}

.user-dropdown-menu {
  border-radius: 12px;
  overflow: hidden;
  border: none;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.15);
  backdrop-filter: blur(15px);
  -webkit-backdrop-filter: blur(15px);
  background: rgba(255, 255, 255, 0.9);
  animation: dropdownFadeIn 0.3s cubic-bezier(0.25, 0.1, 0.25, 1);

  ::v-deep .el-dropdown-menu__item {
    padding: 12px 16px;
    transition: all 0.2s ease;

    &:hover {
      background: rgba(245, 108, 108, 0.1);
    }

    i {
      margin-right: 8px;
      font-size: 16px;
      width: 16px;
      text-align: center;
    }
  }
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(66, 185, 131, 0.7);
  }
  70% {
    box-shadow: 0 0 0 6px rgba(66, 185, 131, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(66, 185, 131, 0);
  }
}

@keyframes dropdownFadeIn {
  from {
    opacity: 0;
    transform: translateY(-10px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}
</style>

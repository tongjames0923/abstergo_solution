<template>
  <div class="sidebar-logo-container" :class="{'collapse':collapse}">
    <router-link class="sidebar-logo-link" to="/">
      <img v-if="logo" :src="logo" class="sidebar-logo">
      <span v-else class="sidebar-monogram">{{ monogram }}</span>
      <transition name="sidebarLogoFade">
        <h1 v-if="!collapse" class="sidebar-title">{{ title }}</h1>
      </transition>
    </router-link>
  </div>
</template>

<script>
import settings from '@/settings'

export default {
  name: 'SidebarLogo',
  props: {
    collapse: {
      type: Boolean,
      required: true
    }
  },
  data() {
    return {
      // 标题统一取自项目配置，避免模板残留文案
      title: settings.title,
      logo: ''
    }
  },
  computed: {
    // 无 logo 图片时以标题首字符作为字标
    monogram() {
      return (this.title || 'A').charAt(0)
    }
  }
}
</script>

<style lang="scss" scoped>
.sidebarLogoFade-enter-active {
  transition: opacity 0.3s cubic-bezier(0.25, 0.1, 0.25, 1);
}

.sidebarLogoFade-leave-active {
  transition: opacity 0.3s cubic-bezier(0.25, 0.1, 0.25, 1);
}

.sidebarLogoFade-enter,
.sidebarLogoFade-leave-to {
  opacity: 0;
}

.sidebar-logo-container {
  position: relative;
  width: 100%;
  height: 44px;
  line-height: 44px;
  background-color: #ffffff;
  text-align: center;
  overflow: hidden;
  border-bottom: 1px solid #eaeaea;

  & .sidebar-logo-link {
    height: 100%;
    width: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;

    & .sidebar-logo {
      width: 24px;
      height: 24px;
      flex: 0 0 auto;
      vertical-align: middle;
      transition: all 0.2s ease;
    }

    /* 无图片时的蓝色字标，与系统主色呼应 */
    & .sidebar-monogram {
      width: 24px;
      height: 24px;
      flex: 0 0 auto;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      border-radius: 6px;
      background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
      color: #fff;
      font-size: 13px;
      font-weight: 600;
      line-height: 1;
    }

    & .sidebar-title {
      display: inline-block;
      margin: 0;
      color: #000;
      font-weight: 600;
      line-height: 44px;
      font-size: 13px;
      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, sans-serif;
      vertical-align: middle;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      transition: all 0.2s ease;
    }
  }

  &.collapse {
    .sidebar-logo-link {
      gap: 0;
    }
  }
}
</style>

<template>
  <div class="capsule-breadcrumb">
    <transition-group name="breadcrumb" tag="div" class="capsule-container">
      <div
        v-for="(item, index) in displayedLevels"
        :key="`${item.path}-${index}`"
        class="capsule-item"
        :class="{ 'is-active': index === displayedLevels.length - 1 }"
      >
        <span
          v-if="item.redirect === 'noRedirect' || index === displayedLevels.length - 1"
          class="capsule-link no-redirect"
        >
          {{ item.meta.title }}
        </span>
        <a v-else class="capsule-link" @click.prevent="handleLink(item)">
          {{ item.meta.title }}
        </a>
        
        <i v-if="index < displayedLevels.length - 1" class="capsule-separator el-icon-arrow-right"></i>
      </div>
    </transition-group>
    
    <!-- 关闭按钮 -->
    <div class="close-button" @click="toggleBreadcrumb">
      <i class="el-icon-close"></i>
    </div>
  </div>
</template>

<script>
import pathToRegexp from 'path-to-regexp'

export default {
  data() {
    return {
      levelList: [],
      isCollapsed: false // 面包屑是否收缩
    }
  },
  computed: {
    displayedLevels() {
      // 如果是收缩状态，只显示最后一项（当前页面）
      if (this.isCollapsed) {
        const lastItem = this.levelList[this.levelList.length - 1]
        return lastItem ? [lastItem] : []
      }
      return this.levelList
    }
  },
  watch: {
    $route() {
      this.getBreadcrumb()
    }
  },
  created() {
    this.getBreadcrumb()
  },
  methods: {
    getBreadcrumb() {
      // only show routes with meta.title
      let matched = this.$route.matched.filter(item => item.meta && item.meta.title)
      const first = matched[0]

      if (!this.isDashboard(first)) {
        matched = [{ path: '/dashboard', meta: { title: 'Dashboard' }}].concat(matched)
      }

      this.levelList = matched.filter(item => item.meta && item.meta.title && item.meta.breadcrumb !== false)
    },
    isDashboard(route) {
      const name = route && route.name
      if (!name) {
        return false
      }
      return name.trim().toLocaleLowerCase() === 'Dashboard'.toLocaleLowerCase()
    },
    pathCompile(path) {
      // To solve this problem https://github.com/PanJiaChen/vue-element-admin/issues/561
      const { params } = this.$route
      var toPath = pathToRegexp.compile(path)
      return toPath(params)
    },
    handleLink(item) {
      const { redirect, path } = item
      if (redirect) {
        this.$router.push(redirect)
        return
      }
      this.$router.push(this.pathCompile(path))
    },
    toggleBreadcrumb() {
      this.isCollapsed = !this.isCollapsed
    }
  }
}
</script>

<style lang="scss" scoped>
@import "~@/styles/variables.scss";

.capsule-breadcrumb {
  display: inline-flex;
  align-items: center;
  height: 100%;
  max-width: 100%;
  overflow: hidden;
  position: relative;
  
  .capsule-container {
    display: flex;
    align-items: center;
    flex-wrap: nowrap;
    
    /* 面包屑项出现/消失的动画 */
    .breadcrumb-enter-active {
      transition: all 0.5s cubic-bezier(0.165, 0.84, 0.44, 1);
      transition-delay: 0.1s;
    }
    
    .breadcrumb-leave-active {
      transition: all 0.4s cubic-bezier(0.165, 0.84, 0.44, 1);
      position: absolute;
    }
    
    .breadcrumb-enter {
      opacity: 0;
      transform: translateX(20px) scale(0.9);
      filter: blur(2px);
    }
    
    .breadcrumb-leave-to {
      opacity: 0;
      transform: translateX(-20px) scale(0.9);
      filter: blur(2px);
    }
    
    .breadcrumb-move {
      transition: all 0.4s cubic-bezier(0.165, 0.84, 0.44, 1);
    }
    
    .capsule-item {
      display: flex;
      align-items: center;
      height: 28px;
      background: rgba(255, 255, 255, 0.25);
      backdrop-filter: blur(6px);
      -webkit-backdrop-filter: blur(6px);
      border: 1px solid rgba(255, 255, 255, 0.25);
      border-radius: 14px;
      padding: 0 8px; // 进一步减小内边距
      margin-right: 4px; // 进一步减小右边距
      transition: all 0.3s cubic-bezier(0.25, 0.1, 0.25, 1);
      position: relative;
      white-space: nowrap;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
      max-width: 150px;
      
      &:last-child {
        margin-right: 0;
      }
      
      &:hover {
        background: rgba(255, 255, 255, 0.35);
        backdrop-filter: blur(8px);
        -webkit-backdrop-filter: blur(8px);
        box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
        transform: translateY(-1px);
      }
      
      &.is-active {
        background: linear-gradient(120deg, rgba(64, 158, 255, 0.3), rgba(64, 158, 255, 0.2)) !important;
        backdrop-filter: blur(10px);
        -webkit-backdrop-filter: blur(10px);
        box-shadow: 0 1px 4px rgba(64, 158, 255, 0.2);
        
        .capsule-link {
          color: #205cd8;
          font-weight: 500;
        }
      }
      
      .capsule-link {
        color: $menuText;
        text-decoration: none;
        font-size: 12px;
        cursor: pointer;
        transition: all 0.2s ease-in-out;
        overflow: hidden;
        text-overflow: ellipsis;
        
        &.no-redirect {
          cursor: default;
          color: $subMenuActiveText;
        }
      }
      
      .capsule-separator {
        margin-left: 5px;
        margin-right: 0px;
        color: $subMenuActiveText;
        font-size: 10px;
      }
    }
  }
  
  .close-button {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 20px;
    height: 20px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.3);
    backdrop-filter: blur(6px);
    -webkit-backdrop-filter: blur(6px);
    margin-left: 6px;
    cursor: pointer;
    transition: all 0.3s ease;
    
    &:hover {
      background: rgba(255, 255, 255, 0.5);
      transform: scale(1.1);
    }
    
    .el-icon-close {
      font-size: 12px;
      color: $menuText;
    }
  }
}
</style>
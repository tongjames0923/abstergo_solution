<template>
  <div class="capsule-breadcrumb">
    <transition-group name="breadcrumb" tag="div" class="capsule-container">
      <div
        v-for="(item, index) in levelList"
        :key="item.path"
        class="capsule-item"
        :class="{ 'is-active': index === levelList.length - 1 }"
      >
        <span
          v-if="item.redirect === 'noRedirect' || index === levelList.length - 1"
          class="capsule-link no-redirect"
        >
          {{ item.meta.title }}
        </span>
        <a v-else class="capsule-link" @click.prevent="handleLink(item)">
          {{ item.meta.title }}
        </a>
        
        <i v-if="index < levelList.length - 1" class="capsule-separator el-icon-arrow-right"></i>
      </div>
    </transition-group>
  </div>
</template>

<script>
import pathToRegexp from 'path-to-regexp'

export default {
  data() {
    return {
      levelList: []
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
  
  .capsule-container {
    display: flex;
    align-items: center;
    flex-wrap: nowrap;
    
    .capsule-item {
      display: flex;
      align-items: center;
      height: 32px;
      background: rgba(255, 255, 255, 0.25);
      backdrop-filter: blur(6px);
      -webkit-backdrop-filter: blur(6px);
      border: 1px solid rgba(255, 255, 255, 0.25);
      border-radius: 16px;
      padding: 0 16px;
      margin-right: 8px;
      transition: all 0.3s cubic-bezier(0.25, 0.1, 0.25, 1);
      position: relative;
      white-space: nowrap;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
      
      &:last-child {
        margin-right: 0;
      }
      
      &:hover {
        background: rgba(255, 255, 255, 0.35);
        backdrop-filter: blur(8px);
        -webkit-backdrop-filter: blur(8px);
        box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
        transform: translateY(-1px);
      }
      
      &.is-active {
        background: linear-gradient(120deg, rgba(64, 158, 255, 0.3), rgba(64, 158, 255, 0.2)) !important;
        backdrop-filter: blur(10px);
        -webkit-backdrop-filter: blur(10px);
        box-shadow: 0 2px 6px rgba(64, 158, 255, 0.2);
        
        .capsule-link {
          color: #205cd8;
          font-weight: 500;
        }
      }
      
      .capsule-link {
        color: $menuText;
        text-decoration: none;
        font-size: 14px;
        cursor: pointer;
        transition: all 0.2s ease-in-out;
        
        &.no-redirect {
          cursor: default;
          color: $subMenuActiveText;
        }
      }
      
      .capsule-separator {
        margin-left: 12px;
        margin-right: -4px;
        color: $subMenuActiveText;
        font-size: 12px;
      }
    }
  }
}
</style>
<template>
  <div class="capsule-breadcrumb">
    <!-- 首页按钮 -->
    <div class="home-button" @click="goToHome" title="回到首页">
      <i class="el-icon-house"></i>
    </div>
    
    <transition-group name="breadcrumb" tag="div" class="capsule-container">
      <div
        v-for="(item, index) in displayedLevels"
        :key="`${item.path}-${index}`"
        class="capsule-item"
        :class="{ 'is-active': index === displayedLevels.length - 1 }"
      >
        <el-tooltip
          v-if="shouldShowTooltip(item.meta.title)"
          :content="item.meta.title"
          placement="top"
        >
          <a
            v-if="item.redirect !== 'noRedirect' && index !== displayedLevels.length - 1"
            class="capsule-link"
            @click.prevent="handleLink(item)"
          >
            {{ formatTitle(item.meta.title) }}
          </a>
          <span
            v-else
            class="capsule-link no-redirect"
          >
            {{ formatTitle(item.meta.title) }}
          </span>
        </el-tooltip>
        <template v-else>
          <a
            v-if="item.redirect !== 'noRedirect' && index !== displayedLevels.length - 1"
            class="capsule-link"
            @click.prevent="handleLink(item)"
          >
            {{ formatTitle(item.meta.title) }}
          </a>
          <span
            v-else
            class="capsule-link no-redirect"
          >
            {{ formatTitle(item.meta.title) }}
          </span>
        </template>

        <i v-if="index < displayedLevels.length - 1" class="capsule-separator el-icon-arrow-right"></i>
      </div>
    </transition-group>

    <!-- 移除了关闭按钮 -->
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

      // 过滤掉不需要在面包屑中显示的路由
      this.levelList = matched.filter(item => item.meta && item.meta.title && item.meta.breadcrumb !== false)
    },
    pathCompile(path) {
      // To solve this problem https://github.com/PanJiaChen/vue-element-admin/issues/561
      const {params} = this.$route
      var toPath = pathToRegexp.compile(path)
      return toPath(params)
    },
    handleLink(item) {
      const {redirect, path} = item
      if (redirect) {
        this.$router.push(redirect)
        return
      }
      this.$router.push(this.pathCompile(path))
    },
    toggleBreadcrumb() {
      this.isCollapsed = !this.isCollapsed
    },
    goToHome() {
      this.$router.push('/')
    },
    formatTitle(title) {
      // 如果文本过长则截断
      if (title && title.length > 8) {
        return title.substring(0, 8) + '...'
      }
      return title
    },
    shouldShowTooltip(title) {
      // 判断是否需要显示tooltip
      return title && title.length > 8
    }
  }
}
</script>

<style lang="scss" scoped>
@import "~@/styles/variables.scss";

.capsule-breadcrumb {
  display: flex;
  align-items: center;
  width: 100%;
  max-width: 100%;
  overflow: hidden;
  position: relative;

  .home-button {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 30px;
    height: 30px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.3);
    backdrop-filter: blur(6px);
    -webkit-backdrop-filter: blur(6px);
    margin-right: 10px;
    cursor: pointer;
    transition: all 0.3s ease;
    transform: scale(1);
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
    flex-shrink: 0;

    &:hover {
      background: rgba(64, 158, 255, 0.4);
      transform: scale(1.1);
      box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
    }

    .el-icon-house {
      font-size: 16px;
      color: $menuText;
    }
  }

  .capsule-container {
    display: flex;
    align-items: center;
    flex-wrap: nowrap;
    width: calc(100% - 40px);

    /* 面包屑项出现/消失的动画 */
    .breadcrumb-enter-active {
      transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
      transition-delay: 0.1s;
    }

    .breadcrumb-leave-active {
      transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
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
      transition: all 0.3s cubic-bezier(0.175, 0.885, 0.32, 1.275);
    }

    .capsule-item {
      display: flex;
      align-items: center;
      height: 32px;
      background: rgba(255, 255, 255, 0.25);
      backdrop-filter: blur(6px);
      -webkit-backdrop-filter: blur(6px);
      border: 1px solid rgba(255, 255, 255, 0.25);
      border-radius: 16px;
      padding: 0 14px;
      margin-right: 10px;
      transition: all 0.3s cubic-bezier(0.25, 0.1, 0.25, 1);
      position: relative;
      white-space: nowrap;
      box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
      max-width: 180px;
      transform: scale(1);
      transform-origin: center;

      &:last-child {
        margin-right: 0;
      }

      &:hover {
        background: rgba(255, 255, 255, 0.35);
        backdrop-filter: blur(8px);
        -webkit-backdrop-filter: blur(8px);
        box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
        transform: translateY(-1px) scale(1.02);
      }

      &.is-active {
        background: linear-gradient(120deg, rgba(64, 158, 255, 0.3), rgba(64, 158, 255, 0.2)) !important;
        backdrop-filter: blur(10px);
        -webkit-backdrop-filter: blur(10px);
        box-shadow: 0 1px 4px rgba(64, 158, 255, 0.2);
        transform: scale(1.03);

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
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
        max-width: 140px;
        display: inline-block;

        &.no-redirect {
          cursor: default;
          color: $subMenuActiveText;
        }
      }

      .capsule-separator {
        margin-left: 10px;
        margin-right: 0px;
        color: $subMenuActiveText;
        font-size: 12px;
      }
    }
  }
}
</style>
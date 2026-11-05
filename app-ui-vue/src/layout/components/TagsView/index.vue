<template>
  <div class="tags-view-container">
    <div ref="scroll" class="tags-view-scroll">
      <router-link
        v-for="tag in visitedViews"
        :key="tag.fullPath"
        :to="tag.fullPath"
        class="tags-view-item"
        :class="{ active: isActive(tag) }"
        :title="tag.title"
        @contextmenu.native.prevent="openMenu(tag, $event)"
      >
        <span class="tag-title">{{ tag.title }}</span>
        <i class="el-icon-close tag-close" @click.prevent.stop="closeTag(tag)" />
      </router-link>
    </div>

    <ul
      v-show="menuVisible"
      class="tags-context-menu"
      :style="{ left: menuLeft + 'px', top: menuTop + 'px' }"
    >
      <li @click="refreshSelected">{{ $t('tagsView.refresh') }}</li>
      <li @click="closeSelected">{{ $t('tagsView.close') }}</li>
      <li @click="closeOthers">{{ $t('tagsView.closeOthers') }}</li>
      <li @click="closeLeft">{{ $t('tagsView.closeLeft') }}</li>
      <li @click="closeRight">{{ $t('tagsView.closeRight') }}</li>
      <li @click="closeAll">{{ $t('tagsView.closeAll') }}</li>
    </ul>
  </div>
</template>

<script>
import {mapGetters} from 'vuex'

// 右键菜单近似宽度，用于把菜单夹在页签栏内，避免窄屏右侧溢出被裁切
const MENU_WIDTH = 112

/**
 * 多标签页签栏
 *
 * 点击侧边栏菜单不再独占整页，而是以页签形式累积在导航栏下方，可来回切换与关闭。
 * 页签状态存于 tagsView 模块（以 fullPath 为键），页面实例缓存由 AppMain 的 keep-alive 承载。
 * 触摸设备无右键，逐个关闭依赖常驻可见的关闭按钮，批量操作（右键菜单）为桌面端便利项。
 */
export default {
  name: 'TagsView',
  data() {
    return {
      menuVisible: false,
      menuLeft: 0,
      menuTop: 0,
      selectedTag: null
    }
  },
  computed: {
    ...mapGetters(['visitedViews'])
  },
  watch: {
    $route() {
      this.addCurrentTag()
      this.scrollToActive()
    }
  },
  mounted() {
    this.addCurrentTag()
    this.scrollToActive()
    // 桌面端滚轮横向滚动页签栏；passive:false 才能调用 preventDefault
    this.$refs.scroll.addEventListener('wheel', this.handleWheel, { passive: false })
    document.addEventListener('click', this.closeMenu)
  },
  beforeDestroy() {
    this.$refs.scroll.removeEventListener('wheel', this.handleWheel)
    document.removeEventListener('click', this.closeMenu)
  },
  methods: {
    isActive(tag) {
      return tag.fullPath === this.$route.fullPath
    },
    // 登记当前路由为页签（无 meta.title 的路由由 store 侧过滤）
    addCurrentTag() {
      this.$store.commit('tagsView/ADD_VIEW', this.$route)
    },
    scrollToActive() {
      this.$nextTick(() => {
        const el = this.$el.querySelector('.tags-view-item.active')
        if (el) {
          el.scrollIntoView({ behavior: 'smooth', block: 'nearest', inline: 'nearest' })
        }
      })
    },
    handleWheel(e) {
      const el = this.$refs.scroll
      // 没有横向溢出时不拦截，保留页面自身的纵向滚动
      if (el.scrollWidth <= el.clientWidth) {
        return
      }
      e.preventDefault()
      el.scrollLeft += (e.deltaX || e.deltaY)
    },
    openMenu(tag, e) {
      const rect = this.$el.getBoundingClientRect()
      this.selectedTag = tag
      this.menuLeft = Math.max(0, Math.min(e.clientX - rect.left, rect.width - MENU_WIDTH))
      this.menuTop = e.clientY - rect.top + 4
      this.menuVisible = true
    },
    closeMenu() {
      this.menuVisible = false
    },
    // 批量关闭后把视图切回被保留的页签，避免当前页面已无对应页签
    gotoSelected() {
      if (this.selectedTag && !this.isActive(this.selectedTag)) {
        this.$router.push(this.selectedTag.fullPath)
      }
    },
    closeTag(tag) {
      const isCurrent = this.isActive(tag)
      const index = this.visitedViews.findIndex(v => v.fullPath === tag.fullPath)
      this.$store.commit('tagsView/DEL_VIEW', tag)
      if (isCurrent) {
        // 优先切到右侧相邻页签，最右侧的页签被关闭时退回左侧
        const next = this.visitedViews[index] || this.visitedViews[index - 1]
        this.$router.push(next ? next.fullPath : '/')
      }
    },
    refreshSelected() {
      this.$store.commit('tagsView/BUMP_REFRESH', this.selectedTag)
      this.gotoSelected()
    },
    closeSelected() {
      this.closeTag(this.selectedTag)
    },
    closeOthers() {
      this.$store.commit('tagsView/DEL_OTHERS_VIEWS', this.selectedTag)
      this.gotoSelected()
    },
    closeLeft() {
      this.$store.commit('tagsView/DEL_LEFT_VIEWS', this.selectedTag)
      this.gotoSelected()
    },
    closeRight() {
      this.$store.commit('tagsView/DEL_RIGHT_VIEWS', this.selectedTag)
      this.gotoSelected()
    },
    closeAll() {
      this.$store.commit('tagsView/DEL_ALL_VIEWS')
      // 全部关闭后回到首页，首页路径会重定向到欢迎页并由路由监听补回页签
      if (this.$route.path !== '/') {
        this.$router.push('/')
      }
    }
  }
}
</script>

<style lang="scss" scoped>
@import "~@/styles/variables.scss";
@import "~@/styles/mixin.scss";

.tags-view-container {
  position: relative;
  height: 34px;
  background: rgba(255, 255, 255, 0.72);
  backdrop-filter: saturate(180%) blur(12px);
  -webkit-backdrop-filter: saturate(180%) blur(12px);
  border-bottom: 1px solid #eaeaea;
}

.tags-view-scroll {
  display: flex;
  align-items: center;
  gap: 6px;
  height: 100%;
  padding: 0 8px;
  overflow-x: auto;
  overflow-y: hidden;
  white-space: nowrap;
}

.tags-view-item {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  height: 24px;
  padding: 0 6px 0 10px;
  font-size: 12px;
  color: $menuText;
  background: #f5f5f7;
  border: 1px solid #eaeaea;
  border-radius: 6px;
  transition: color 0.2s, background 0.2s, border-color 0.2s;

  &:hover {
    color: $menuActiveText;
  }

  &.active {
    color: $menuActiveText;
    background: rgba(0, 113, 227, 0.08);
    border-color: rgba(0, 113, 227, 0.24);
  }

  .tag-title {
    max-width: 120px;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  /* 关闭按钮常驻可见：触摸设备没有 hover，不能做成悬停才出现 */
  .tag-close {
    margin-left: 6px;
    width: 16px;
    height: 16px;
    line-height: 16px;
    text-align: center;
    border-radius: 50%;
    font-size: 12px;
    flex-shrink: 0;

    &:hover {
      color: #fff;
      background: $menuActiveText;
    }
  }
}

.tags-context-menu {
  position: absolute;
  z-index: 1002;
  margin: 0;
  padding: 4px 0;
  list-style: none;
  background: #fff;
  border: 1px solid #eaeaea;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);

  li {
    padding: 6px 14px;
    font-size: 12px;
    color: #333;
    white-space: nowrap;
    cursor: pointer;

    &:hover {
      color: $menuActiveText;
      background: #f5f5f7;
    }
  }
}

/* 平板/手机：加大页签与关闭按钮的点击区，保证触摸可点 */
@include respond-to(mobile) {
  .tags-view-item {
    height: 28px;
    padding-right: 4px;

    .tag-title {
      max-width: 96px;
    }

    .tag-close {
      width: 22px;
      height: 22px;
      line-height: 22px;
    }
  }
}
</style>

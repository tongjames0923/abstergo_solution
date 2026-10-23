import store from '@/store'
import { resolveDevice } from '@/utils/device'

// resize 防抖延迟，避免横竖屏切换/拖拽窗口时高频触发
const RESIZE_DEBOUNCE = 150

/**
 * 防抖包装
 * @param {Function} fn - 待防抖函数
 * @param {number} wait - 等待毫秒数
 * @returns {Function} 防抖后的函数
 */
function debounce(fn, wait) {
  let timer = null
  return function() {
    if (timer) {
      clearTimeout(timer)
    }
    timer = setTimeout(fn, wait)
  }
}

export default {
  watch: {
    $route() {
      // 移动端路由切换后自动收起抽屉侧边栏
      if (this.device !== 'desktop' && this.sidebar.opened) {
        store.dispatch('app/closeSideBar', { withoutAnimation: false })
      }
    }
  },
  beforeMount() {
    this.$_handleResize = debounce(this.$_resizeHandler, RESIZE_DEBOUNCE)
    window.addEventListener('resize', this.$_handleResize)
    this.$_initOrientationListener()
  },
  beforeDestroy() {
    window.removeEventListener('resize', this.$_handleResize)
    this.$_destroyOrientationListener()
  },
  mounted() {
    const device = resolveDevice()
    store.dispatch('app/toggleDevice', device)
    if (device !== 'desktop') {
      store.dispatch('app/closeSideBar', { withoutAnimation: true })
    }
  },
  computed: {
    device() {
      return this.$store.state.app.device
    },
    sidebar() {
      return this.$store.state.app.sidebar
    }
  },
  methods: {
    /**
     * 初始化横竖屏变化监听（matchMedia orientation 查询）
     */
    $_initOrientationListener() {
      if (!window.matchMedia) {
        return
      }
      this.$_orientationMq = window.matchMedia('(orientation: portrait)')
      this.$_orientationHandler = mq => {
        store.dispatch('app/setOrientation', mq.matches ? 'portrait' : 'landscape')
      }
      this.$_orientationHandler(this.$_orientationMq)
      if (this.$_orientationMq.addEventListener) {
        this.$_orientationMq.addEventListener('change', this.$_orientationHandler)
      } else if (this.$_orientationMq.addListener) {
        // 兼容旧版 Safari 的 addListener API
        this.$_orientationMq.addListener(this.$_orientationHandler)
      }
    },
    /**
     * 销毁横竖屏监听，防止内存泄漏
     */
    $_destroyOrientationListener() {
      if (!this.$_orientationMq || !this.$_orientationHandler) {
        return
      }
      if (this.$_orientationMq.removeEventListener) {
        this.$_orientationMq.removeEventListener('change', this.$_orientationHandler)
      } else if (this.$_orientationMq.removeListener) {
        this.$_orientationMq.removeListener(this.$_orientationHandler)
      }
    },
    /**
     * resize 回调：重新判定设备档位，非桌面档收起侧边栏
     */
    $_resizeHandler() {
      if (document.hidden) {
        return
      }
      const device = resolveDevice()
      store.dispatch('app/toggleDevice', device)
      if (device !== 'desktop') {
        store.dispatch('app/closeSideBar', { withoutAnimation: true })
      }
    }
  }
}

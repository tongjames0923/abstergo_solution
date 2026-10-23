/**
 * 响应式设备契约 mixin
 *
 * 统一页面组件获取设备档位的出口，避免各组件直连 store 造成判定标准漂移。
 * 设备档位由 layout/mixin/ResizeHandler.js 写入 store，全站唯一真源。
 *
 * 用法：mixins: [ResponsiveMixin]
 * 模板/脚本中可直接使用 isPhone / isTablet / isTouch 做条件渲染；
 * 样式层优先使用 @include container-down()，仅在需要切换 DOM 结构时才用这些计算属性。
 */
export default {
  computed: {
    // 当前设备档位：phone / tablet / desktop
    device() {
      return this.$store.state.app.device
    },
    // 当前横竖屏方向：portrait / landscape
    orientation() {
      return this.$store.state.app.orientation
    },
    isPhone() {
      return this.device === 'phone'
    },
    isTablet() {
      return this.device === 'tablet'
    },
    isDesktop() {
      return this.device === 'desktop'
    },
    // 平板与手机均视为触摸设备，用于触摸目标放大与按压态判定
    isTouch() {
      return this.device !== 'desktop'
    }
  }
}

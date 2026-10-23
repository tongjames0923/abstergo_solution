/**
 * 设备档位判定（唯一真源）
 *
 * 断点与 styles/variables.scss 的 $bp-phone / $bp-tablet 以及
 * element-ui.scss 的媒体查询保持一致：
 * phone <= 768px，tablet 769~992px，desktop > 992px
 *
 * 供 store 初始值与 layout/mixin/ResizeHandler.js 共用，
 * 避免多处各写一份断点导致判定漂移
 */

const PHONE_WIDTH = 768
const TABLET_WIDTH = 992

/**
 * 根据宽度解析设备档位
 * @param {number} [width] - 视口宽度，缺省时取 body 内容宽度（已排除滚动条）
 * @returns {string} phone / tablet / desktop
 */
export function resolveDevice(width) {
  const w = width !== undefined
    ? width
    : document.body.getBoundingClientRect().width
  if (w <= PHONE_WIDTH) {
    return 'phone'
  }
  if (w <= TABLET_WIDTH) {
    return 'tablet'
  }
  return 'desktop'
}

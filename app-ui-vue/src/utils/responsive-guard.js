/**
 * 响应式溢出守卫（仅开发环境）
 *
 * 页面新增或修改后若未做窄屏适配，横向溢出既不会报错也不会进 CI，
 * 只能在真机上肉眼发现。这里在内容区横向溢出时告警并定位到具体 DOM，
 * 把\"忘记适配\"从视觉问题变成开发期可感知的显式问题。
 *
 * 挂载：main.js 中 installResponsiveGuard(router)
 */
const GUARD_ROOT = '.app-main'
const DEBOUNCE_DELAY = 300

/**
 * 防抖包装
 * @param {Function} fn - 待防抖函数
 * @param {number} wait - 等待毫秒数
 * @returns {Function} 防抖后的函数
 */
function debounce(fn, wait) {
  let timer = null
  return function() {
    clearTimeout(timer)
    timer = setTimeout(fn, wait)
  }
}

/**
 * 判断元素是否横向溢出
 * 自身或祖先链上声明了 overflow-x: auto/scroll 的元素是设计上的滚动容器
 * （如表格 body-wrapper、el-scrollbar），其溢出是可滚动的，不算适配缺陷
 * @param {Element} el - 待判定元素
 * @param {Element} root - 检测根节点
 * @returns {boolean} 是否需告警的溢出元素
 */
function isOverflowing(el, root) {
  if (el.clientWidth <= 0 || el.scrollWidth - el.clientWidth <= 1) {
    return false
  }
  let node = el
  while (node && node !== root) {
    const overflowX = window.getComputedStyle(node).overflowX
    if (overflowX === 'auto' || overflowX === 'scroll') {
      return false
    }
    node = node.parentElement
  }
  return true
}

/**
 * 定位内容区内所有横向溢出的元素
 * 先判断内容区自身是否溢出，未溢出时不深扫，避免常态全量遍历
 * @returns {Array<Element>} 溢出元素列表
 */
function findOffenders() {
  const root = document.querySelector(GUARD_ROOT)
  if (!root || root.scrollWidth - root.clientWidth <= 1) {
    return []
  }
  return Array.prototype.filter.call(
    root.querySelectorAll('*'),
    el => isOverflowing(el, root)
  )
}

/**
 * 安装溢出守卫
 * @param {Object} router - vue-router 实例，用于路由切换后重新检测
 */
export default function installResponsiveGuard(router) {
  if (process.env.NODE_ENV === 'production' || typeof window === 'undefined') {
    return
  }

  const check = () => {
    const offenders = findOffenders()
    if (offenders.length) {
      console.warn(
        `[responsive] 内容区横向溢出，共 ${offenders.length} 个元素未适配窄屏：`,
        offenders
      )
    }
  }
  const debouncedCheck = debounce(check, DEBOUNCE_DELAY)

  window.addEventListener('resize', debouncedCheck)
  if (router && router.afterEach) {
    // 路由切换后等待视图渲染完成再检测
    router.afterEach(() => setTimeout(debouncedCheck, 150))
  }
  // 首次进入时布局尚未就绪，延迟检测一次
  setTimeout(debouncedCheck, 800)
}

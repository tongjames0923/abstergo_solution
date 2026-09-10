/**
 * 数据处理上传组件公共常量与工具
 */

/**
 * CSV 文件扩展名校验正则
 */
export const CSV_EXT_REGEXP = /\.csv$/i

/**
 * 处理器 ID 防抖校验时长（毫秒）
 */
export const VALIDATE_DEBOUNCE_MS = 500

/**
 * 全局搜索输入防抖时长（毫秒）
 */
export const SEARCH_DEBOUNCE_MS = 300

/**
 * 列筛选下拉选项数量上限，超过后该列不提供筛选
 */
export const FILTER_OPTIONS_LIMIT = 100

/**
 * 分页页大小可选项
 */
export const PAGE_SIZES = [50, 100, 200, 500]

/**
 * 默认页大小
 */
export const DEFAULT_PAGE_SIZE = 50

/**
 * 判断文件名是否为 CSV 文件
 * @param {string} name - 文件名
 * @returns {boolean} 是否为 CSV 文件
 */
export function isCsvFile(name) {
  return CSV_EXT_REGEXP.test(name || '')
}

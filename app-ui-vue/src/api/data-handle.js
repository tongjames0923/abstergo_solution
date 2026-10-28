import request from '@/utils/request'

/**
 * 获取数据处理器选项列表
 * @returns {Promise} 处理器选项列表，每项含 id 与 name
 */
export function listHandlers() {
  return request({
    url: '/private/data/handler/list',
    method: 'get'
  })
}

/**
 * 校验数据处理器ID有效性
 * @param {string} handlerId - 处理器ID
 * @returns {Promise} 校验结果：available（是否可用）、source（命中来源：local 本地 / remote 远程）
 */
export function validateHandler(handlerId) {
  return request({
    url: `/private/data/handler/validate/${encodeURIComponent(handlerId)}`,
    method: 'get'
  })
}

/**
 * 上传并解析 CSV 文件
 * @param {string} handlerId - 数据处理器ID
 * @param {File} file - CSV 文件
 * @param {Function} [onProgress] - 上传进度回调，入参为 0~100 的整数
 * @returns {Promise} 解析后的表格数据
 */
export function handleCsv(handlerId, file, onProgress) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: `/private/data/handle/csv/${encodeURIComponent(handlerId)}`,
    method: 'post',
    data: formData,
    onUploadProgress: e => {
      if (onProgress && e.total) {
        onProgress(Math.round((e.loaded * 100) / e.total))
      }
    }
  })
}

import axios from 'axios'
import request from '@/utils/request'
import { getToken } from '@/utils/auth'
import settings from '@/settings'
import i18n from '@/i18n'

/**
 * 上传文件
 * @param {File} file - 待上传的文件
 * @param {string} [objectKey] - 存储对象键，缺省由后端自动生成
 * @param {string} [bucket] - 存储桶，缺省使用默认桶
 * @param {Function} [onProgress] - 浏览器传输进度回调，入参为 0~100 的整数
 * @param {string} [taskId] - 上传任务ID，指定后可通过 getUploadProgress 轮询服务端写入进度
 * @returns {Promise} 上传结果（含对象键与访问地址）
 */
export function uploadObject(file, objectKey, bucket, onProgress, taskId) {
  const formData = new FormData()
  formData.append('file', file)
  if (objectKey) {
    formData.append('objectKey', objectKey)
  }
  if (bucket) {
    formData.append('bucket', bucket)
  }
  if (taskId) {
    formData.append('taskId', taskId)
  }
  return request({
    url: '/private/oss/upload',
    method: 'post',
    data: formData,
    onUploadProgress: e => {
      if (onProgress && e.total) {
        onProgress(Math.round((e.loaded * 100) / e.total))
      }
    }
  })
}

/**
 * 查询上传任务进度（上传请求执行期间由前端轮询）
 * @param {string} taskId - 上传任务ID
 * @returns {Promise} 进度信息：status（RECEIVING 接收中 / STORING 写入中 / SUCCESS 成功 / FAILED 失败）、percentage（0~100）
 */
export function getUploadProgress(taskId) {
  return request({
    url: '/private/oss/upload/progress',
    method: 'get',
    params: { taskId }
  })
}

/**
 * 按前缀分页列举对象
 * @param {Object} params - { prefix, marker, bucket }
 * @returns {Promise} 对象列表（含 nextMarker 与 truncated）
 */
export function listObjects(params) {
  return request({
    url: '/private/oss/list',
    method: 'get',
    params
  })
}

/**
 * 获取对象元数据
 * @param {string} objectKey - 存储对象键
 * @param {string} [bucket] - 存储桶
 * @returns {Promise} 对象元数据
 */
export function getObjectMetadata(objectKey, bucket) {
  return request({
    url: '/private/oss/metadata',
    method: 'get',
    params: { objectKey, bucket }
  })
}

/**
 * 判断对象是否存在
 * @param {string} objectKey - 存储对象键
 * @param {string} [bucket] - 存储桶
 * @returns {Promise} 是否存在
 */
export function existObject(objectKey, bucket) {
  return request({
    url: '/private/oss/exist',
    method: 'get',
    params: { objectKey, bucket }
  })
}

/**
 * 生成预签名访问地址
 * @param {Object} params - { objectKey, type: DOWNLOAD|UPLOAD, expirationSeconds, bucket }
 * @returns {Promise} 预签名地址
 */
export function getPresignedUrl(params) {
  return request({
    url: '/private/oss/presigned-url',
    method: 'get',
    params
  })
}

/**
 * 删除单个对象
 * @param {string} objectKey - 存储对象键
 * @param {string} [bucket] - 存储桶
 * @returns {Promise} 操作结果
 */
export function deleteObject(objectKey, bucket) {
  return request({
    url: '/private/oss',
    method: 'delete',
    params: { objectKey, bucket }
  })
}

/**
 * 批量删除对象
 * @param {string[]} objectKeys - 存储对象键列表
 * @param {string} [bucket] - 存储桶
 * @returns {Promise} 操作结果
 */
export function deleteObjects(objectKeys, bucket) {
  return request({
    url: '/private/oss/batch',
    method: 'delete',
    params: { bucket },
    data: objectKeys
  })
}

/**
 * 下载对象
 * <p>
 * 优先通过预签名地址触发浏览器原生下载（由浏览器下载器接管进度展示），
 * 预签名获取失败时回退为携带鉴权头的 Blob 下载
 * </p>
 * @param {string} objectKey - 存储对象键
 * @param {string} [bucket] - 存储桶
 * @returns {Promise} 下载结果
 */
export function downloadObject(objectKey, bucket) {
  const params = { objectKey, type: 'DOWNLOAD', expirationSeconds: 300 }
  if (bucket) {
    params.bucket = bucket
  }
  return getPresignedUrl(params).then(res => {
    const presignedUrl = res.data
    if (!presignedUrl) {
      return Promise.reject(new Error('presigned url empty'))
    }
    // 隐藏 iframe 加载预签名地址，浏览器识别为附件后直接接管下载；
    // 相比 window.open 不会弹出空白新页，也不影响当前页面
    const iframe = document.createElement('iframe')
    iframe.style.display = 'none'
    iframe.src = presignedUrl
    document.body.appendChild(iframe)
    setTimeout(() => {
      document.body.removeChild(iframe)
    }, 60000)
  }).catch(() => downloadObjectViaBlob(objectKey, bucket))
}

/**
 * 构造公开下载码的免认证下载地址
 * @param {string} code - 下载唯一码
 * @returns {string} 完整公开下载 URL
 */
export function buildPublicDownloadUrl(code) {
  return process.env.VUE_APP_BASE_API + '/public/oss/download-code/download?code=' + encodeURIComponent(code)
}

/**
 * 创建公开下载码（code 缺省时由后端自动生成）
 * @param {Object} form - { objectKey, code?, bucket?, expireSeconds?, remark? }
 * @returns {Promise} 创建成功的下载码信息
 */
export function createDownloadCode(form) {
  return request({
    url: '/private/oss/download-code',
    method: 'post',
    data: form
  })
}

/**
 * 按码查询下载码信息
 * @param {string} code - 下载唯一码
 * @returns {Promise} 下载码信息
 */
export function getDownloadCode(code) {
  return request({
    url: '/private/oss/download-code',
    method: 'get',
    params: { code }
  })
}

/**
 * 更新下载码（续期/改备注）
 * @param {string} code - 下载唯一码
 * @param {Object} form - { expireSeconds?, remark? }
 * @returns {Promise} 操作结果
 */
export function updateDownloadCode(code, form) {
  return request({
    url: '/private/oss/download-code',
    method: 'put',
    params: { code },
    data: form
  })
}

/**
 * 删除下载码
 * @param {string} code - 下载唯一码
 * @returns {Promise} 操作结果
 */
export function deleteDownloadCode(code) {
  return request({
    url: '/private/oss/download-code',
    method: 'delete',
    params: { code }
  })
}

/**
 * 携带鉴权头以 Blob 方式下载对象（回退通道）
 * @param {string} objectKey - 存储对象键
 * @param {string} [bucket] - 存储桶
 * @returns {Promise} 下载结果
 */
function downloadObjectViaBlob(objectKey, bucket) {
  return axios({
    url: process.env.VUE_APP_BASE_API + '/private/oss/download',
    method: 'get',
    params: { objectKey, bucket },
    responseType: 'blob',
    headers: {
      [settings.tokenHeader]: getToken(),
      'Accept-Language': i18n.locale === 'en' ? 'en_US' : 'zh_CN'
    }
  }).then(response => {
    const key = objectKey.includes('/') ? objectKey.substring(objectKey.lastIndexOf('/') + 1) : objectKey
    const url = window.URL.createObjectURL(new Blob([response.data]))
    const link = document.createElement('a')
    link.href = url
    link.setAttribute('download', key)
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  })
}

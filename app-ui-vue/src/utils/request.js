import axios from 'axios'
import {Loading, Message, MessageBox, Notification} from 'element-ui'
import store from '@/store'
import {getToken} from '@/utils/auth'
import settings from '@/settings'

// create an axios instance
const service = axios.create({
  baseURL: process.env.VUE_APP_BASE_API, // url = base url + request url
  withCredentials: true // send cookies when cross-domain requests
  // timeout: 3000 // request timeout
})

// Token刷新相关状态
let isRefreshing = false
let failedQueue = []
let refreshLoadingInstance = null

const processQueue = (error, token = null) => {
  failedQueue.forEach(prom => {
    if (error) {
      prom.reject(error)
    } else {
      prom.resolve(token)
    }
  })

  failedQueue = []
}

// request interceptor
service.interceptors.request.use(
  config => {
    // do something before request is sent
    if (store.getters.token) {
      // let each request carry token
      // ['X-Token'] is a custom headers key
      // please modify it according to the actual situation
      config.headers[settings.tokenHeader] = getToken()
    }
    return config
  },
  error => {
    // do something with request error
    console.log(error) // for debug
    return Promise.reject(error)
  }
)

// 响应拦截器中处理token刷新的逻辑
const refreshTokenHandler = async(response) => {
  const config = response.config

  // 如果正在刷新token，则将请求加入队列等待
  if (isRefreshing) {
    return new Promise((resolve, reject) => {
      failedQueue.push({ resolve, reject })
    }).then(token => {
      config.headers[settings.tokenHeader] = token
      // 修复：确保不会重复添加baseURL
      if (config.baseURL && config.url.startsWith(config.baseURL)) {
        // 如果URL已经包含了baseURL，则创建一个新的配置对象，避免重复
        const newConfig = {...config}
        newConfig.url = config.url.substring(config.baseURL.length)
        return service(newConfig)
      }
      return service(config)
    }).catch(err => {
      return Promise.reject(err)
    })
  }

  // 开始刷新token流程
  isRefreshing = true
  config._retry = true

  // 显示刷新Token的加载提示，提升用户体验
  refreshLoadingInstance = Loading.service({
    lock: true,
    text: '登录状态即将过期，正在自动续期...',
    spinner: 'el-icon-loading',
    background: 'rgba(0, 0, 0, 0.7)'
  })

  try {
    await store.dispatch('user/refreshToken')
    // 处理队列中的请求
    processQueue(null, getToken())
    // 修复：确保不会重复添加baseURL
    if (config.baseURL && config.url.startsWith(config.baseURL)) {
      // 如果URL已经包含了baseURL，则创建一个新的配置对象，避免重复
      const newConfig = {...config}
      newConfig.url = config.url.substring(config.baseURL.length)
      return await service(newConfig)
    }
    // 重新发送当前请求并返回结果
    return await service(config)
  } catch (err) {
    // 处理刷新token失败的情况
    processQueue(err, null)
    throw err
  } finally {
    isRefreshing = false
    // 关闭加载提示
    if (refreshLoadingInstance) {
      refreshLoadingInstance.close()
      refreshLoadingInstance = null
    }
  }
}

async function noLoginFunction(res) {
  // 刷新token失败，要求重新登录
  await MessageBox.confirm('登录已过期，请重新登录。详情：' + res.message, '提示', {
    confirmButtonText: '重新登录',
    showCancelButton: false,
    type: 'warning'
  })
  await store.dispatch('user/resetToken')
  location.reload()
  return Promise.reject(new Error(res.message || '登录已过期，请重新登录'))
}

// response interceptor
service.interceptors.response.use(
  /**
   * If you want to get http information such as headers or status
   * Please return  response => response
   */

  /**
   * Determine the request status by custom code
   * Here is just an example
   * You can also judge the status by HTTP Status Code
   */
  async response => {
    if (!response) {
      return Promise.reject(new Error('网络连接失败，请检查网络设置'))
    }
    const res = response.data
    if (!res.code) {
      return Promise.reject(new Error('返回配置错误，请检查网络设置'))
    }
    if (res.code === settings.successCode) {
      return res
    } else {
      if (settings.refreshTokenCode.includes(res.code)) {
        // 当遇到需要重新登录的错误码(405)时，直接要求重新登录
        return await noLoginFunction(res)
      } else if (settings.noLoginCode.includes(res.code)) {
        // 当遇到刷新token失败的错误码(468)时，先尝试刷新token
        try {
          return await refreshTokenHandler(response)
        } catch (err) {
          return await noLoginFunction(res)
        }
      } else {
        if (settings.debugCodes.includes(res.code)) {
          console.log(res.message)
          return Promise.reject(new Error(res.message || 'Error'))
        } else if (settings.noticeCodes.includes(res.code)) {
          Notification({
            title: res.title || '提示',
            type: res.type || 'info',
            message: res.message || '提示',
            duration: res.duration || 3 * 1000,
            showClose: true
          })
        } else {
          Message({
            message: res.message || '错误',
            type: res.type || 'error',
            duration: res.duration || 3 * 1000
          })
        }
        return Promise.reject(new Error(res.message || 'Error'))
      }
    }
  }, error => {
    console.error(error)
    // 处理HTTP错误（如网络错误、超时等）
    let message = '网络连接失败'

    if (error.response) {
      switch (error.response.status) {
        case 401:
          message = '未授权，请重新登录'
          break
        case 403:
          message = '拒绝访问'
          break
        case 404:
          message = '请求地址不存在'
          break
        case 500:
          message = '服务器内部错误'
          break
        case 504:
          message = '网关超时'
          break
        default:
          message = error.response.data?.message || '请求失败'
      }
    } else if (error.code === 'ECONNABORTED') {
      message = '请求超时'
    } else if (error.message) {
      message = error.message
    }

    Message({
      message,
      type: 'error',
      duration: 5 * 1000
    })

    return Promise.reject(error)
  }
)

export default service

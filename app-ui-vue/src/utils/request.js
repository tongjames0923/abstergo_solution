import axios from 'axios'
import {Message, MessageBox} from 'element-ui'
import store from '@/store'
import {getToken} from '@/utils/auth'
import settings from '@/settings'

// create an axios instance
const service = axios.create({
  baseURL: process.env.VUE_APP_BASE_API, // url = base url + request url
  withCredentials: true, // send cookies when cross-domain requests
  // timeout: 3000 // request timeout
})

// Token刷新相关状态
let isRefreshing = false
let failedQueue = []

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
const refreshTokenHandler = async (response) => {
  const config = response.config

  // 如果正在刷新token，则将请求加入队列等待
  if (isRefreshing) {
    return new Promise((resolve, reject) => {
      failedQueue.push({resolve, reject})
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

  try {
    await store.dispatch('user/refreshToken')
    // 处理队列中的请求
    processQueue(null, getToken())
    // 重新发送当前请求并返回结果
    // 修复：确保不会重复添加baseURL
    if (config.baseURL && config.url.startsWith(config.baseURL)) {
      // 如果URL已经包含了baseURL，则创建一个新的配置对象，避免重复
      const newConfig = {...config}
      newConfig.url = config.url.substring(config.baseURL.length)
      return await service(newConfig)
    }
    return await service(config)
  } catch (err) {
    // 处理刷新token失败的情况
    processQueue(err, null)
    throw err
  } finally {
    isRefreshing = false
  }
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
    const res = response.data
    if (res.code === settings.successCode) {
      return res;
    } else {
      if (settings.refreshTokenCode.includes(res.code)) {
        // 处理token过期的情况 (code 468)
        try {
          return await refreshTokenHandler(response)
        } catch (err) {
          return Promise.reject(err)
        }
      } else if (settings.noLoginCode.includes(res.code)) {
        Message({
          message: '登录已过期，请重新登录',
          type: 'error',
          duration: 3 * 1000
        })
        await store.dispatch('user/resetToken')
        location.reload()
        return Promise.reject(new Error(res.message | '登录已过期，请重新登录'))
      } else {
        Message({
          message: res.message || '错误',
          type: 'error',
          duration: 3 * 1000
        })

        return Promise.reject(new Error(res.message || '错误'))
      }
    }

    // 处理其他错误


  },
  error => {
    Message({
      message: error.message,
      type: 'error',
      duration: 5 * 1000
    })
    return Promise.reject(error)
  }
)

export default service

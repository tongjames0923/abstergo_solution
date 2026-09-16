import axios from 'axios'
import { Loading, Message, MessageBox, Notification } from 'element-ui'
import store from '@/store'
import { getToken } from '@/utils/auth'
import settings from '@/settings'
import i18n from '@/i18n'

// create an axios instance
const service = axios.create({
  baseURL: process.env.VUE_APP_BASE_API, // url = base url + request url
  withCredentials: true, // send cookies when cross-domain requests
  // 全局超时：后端/网关无响应时请求必须能失败返回，
  // 否则启动期 bootstrap 会永久挂起，导致“正在进入系统…”闪屏卡死
  timeout: 15000
})

// 在请求头中添加语言设置
service.interceptors.request.use(
  config => {
    // do something before request is sent
    if (store.getters.token) {
      // let each request carry token
      // ['X-Token'] is a custom headers key
      // please modify it according to the actual situation
      config.headers[settings.tokenHeader] = getToken()
    }
    // 添加语言头
    config.headers['Accept-Language'] = i18n.locale === 'en' ? 'en_US' : 'zh_CN'
    return config
  },
  error => {
    // do something with request error
    console.log(error) // for debug
    return Promise.reject(error)
  }
)

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
        const newConfig = { ...config }
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
    text: i18n.t('login.tokenRefreshing'),
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
      const newConfig = { ...config }
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
  await MessageBox.confirm(
    i18n.t('login.sessionExpiredDetail') + res.message,
    i18n.t('common.tip'),
    {
      confirmButtonText: i18n.t('login.relogin'),
      showCancelButton: false,
      type: 'warning'
    }
  )
  await store.dispatch('user/resetToken')
  location.reload()
  return Promise.reject(new Error(res.message || i18n.t('login.sessionExpired')))
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
      return Promise.reject(new Error(i18n.t('request.networkError')))
    }
    const res = response.data
    if (!res.code) {
      return Promise.reject(new Error(i18n.t('request.configError')))
    }

    // 成功响应处理
    if (res.code === settings.successCode) {
      if (res.message && res.type) {
        Message({
          message: res.message,
          type: res.type || 'success',
          duration: res.duration || 3 * 1000
        })
      }

      return res
    }

    // 错误响应处理
    // 当遇到需要重新登录的错误码(405)时，直接要求重新登录
    if (settings.refreshTokenCode.includes(res.code)) {
      return await noLoginFunction(res)
    }

    // 当遇到刷新token失败的错误码(468)时，先尝试刷新token
    if (settings.noLoginCode.includes(res.code)) {
      // 已重试过仍返回未登录码，说明刷新后依然无效，
      // 必须终止，否则会陷入“刷新→重试→再刷新”的死循环
      if (config._retry) {
        return await noLoginFunction(res)
      }
      try {
        return await refreshTokenHandler(response)
      } catch (err) {
        return await noLoginFunction(res)
      }
    }

    // 其他错误码处理
    if (settings.debugCodes.includes(res.code)) {
      console.log(res.message)
    } else if (settings.noticeCodes.includes(res.code)) {
      Notification({
        title: res.title || i18n.t('common.tip'),
        type: res.type || 'info',
        message: res.message || i18n.t('common.tip'),
        duration: res.duration || 3 * 1000,
        showClose: true
      })
    } else {
      Message({
        message: res.message || i18n.t('common.error'),
        type: res.type || 'error',
        duration: res.duration || 3 * 1000
      })
    }

    // 如果返回体中包含redirect字段，自动跳转到指定路径
    if (res.redirect && window.vueInstance && window.vueInstance.$router) {
      // 显示跳转提示信息
      const redirectMsg = i18n.t('request.redirectMessage', { path: res.redirect })
      Notification({
        title: i18n.t('request.redirectTip'),
        message: redirectMsg,
        type: 'info',
        duration: 2000,
        showClose: false
      })
      // 等待提示显示后再跳转
      await new Promise(resolve => setTimeout(resolve, 2000))
      // 执行路由跳转
      window.vueInstance.$router.push(res.redirect)
    }

    return Promise.reject(new Error(res.message || 'Error'))
  }, error => {
    console.error(error)
    return Promise.reject(error)
  }
)

export default service

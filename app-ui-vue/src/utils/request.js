import axios from 'axios'
import {Loading, Message, MessageBox, Notification} from 'element-ui'
import store from '@/store'
import {getToken} from '@/utils/auth'
import settings from '@/settings'
import i18n from '@/i18n'

// create an axios instance
const service = axios.create({
  baseURL: process.env.VUE_APP_BASE_API, // url = base url + request url
  withCredentials: true // send cookies when cross-domain requests
  // 全局超时：后端/网关无响应时请求必须能失败返回，
})

// 在请求头中添加语言设置
service.interceptors.request.use(
  config => {
    // do something before request is sent
    // 允许调用方显式指定令牌（如第三方连通性测试）：请求头已携带令牌时不再用当前登录令牌覆盖
    if (store.getters.token && !config.headers[settings.tokenHeader]) {
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
    const newToken = getToken()
    // 触发刷新的请求自身也须换上新令牌重试：请求拦截器对已携带 Authorization 头的配置
    // 不覆盖，留着旧令牌重试必再回未登录码（_retry 已置位），会被误判为刷新无效而强制重登
    config.headers[settings.tokenHeader] = newToken
    // 处理队列中的请求
    processQueue(null, newToken)
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
    // 单飞锁标志置回字面量：并发请求已在入口 isRefreshing 分支转入等待队列，不存在读改写竞态
    // eslint-disable-next-line require-atomic-updates
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
  const error = new Error(res.message || i18n.t('login.sessionExpired'))
  // 登录态失效标记：push 等长连接模块据此区分“凭证失效”与“网络失败”，
  // 停止无效重连循环，等待重登
  error.authExpired = true
  return Promise.reject(error)
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

    // 隔离型请求（如第三方连通性测试）：不走统一的登录态处理流程（刷新/重登/全局通知），
    // 直接返回原始响应体，由调用方自行判定 code 并展示结果
    if (response.config._skipAuthHandling) {
      return res
    }

    if (!res.code) {
      return Promise.reject(new Error(i18n.t('request.configError')))
    }

    // 刷新token请求自身的未登录/刷新失败响应：说明刷新会话已失效，
    // 必须直接失败返回。若进入下方刷新逻辑，会排队等待自己触发的刷新流程，造成死锁
    if (response.config._isRefreshRequest &&
        settings.noLoginCode.concat(settings.refreshTokenCode).includes(res.code)) {
      return Promise.reject(new Error(res.message || i18n.t('login.sessionExpired')))
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
      if (response.config._retry) {
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

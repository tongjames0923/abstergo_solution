import request from '@/utils/request'

/**
 * 推送相关接口封装
 * <p>
 * 协议层统一连接流程：先向认证端点换取一次性建连凭证（ticket），
 * 再用 ticket 建立 SSE 下行流；上行帧（订阅/退订/心跳）走 HTTP POST 端点。
 * 上行帧 type 必须为后端 FrameTypeEnum 的枚举大写名（SUBSCRIBE/UNSUBSCRIBE/PING），
 * 与下行 SSE 事件名（帧类型小写值 hello/data/pong）是两套表达，勿混用。
 * 所有请求复用项目统一 axios 实例（自动携带 token 头与语言头）。
 * </p>
 */

/**
 * 连接认证并签发一次性凭证
 * <p>
 * 凭证为一次性、TTL 默认 60s，须尽快用于 SSE 建连；重连时必须重新签发。
 * </p>
 *
 * @param {string} token 当前登录令牌（取自 utils/auth.getToken）
 * @param {string} [userId] 业务用户标识（可选，供后端审计）
 * @returns {Promise<Object>} 凭证对象 { ticket, sessionId, expireAt, attributes }
 */
export function pushAuth(token, userId) {
  return request({
    url: '/private/push/auth',
    method: 'post',
    data: { token, userId }
  }).then(res => res.data)
}

/**
 * 上行订阅帧：声明对指定 topic 的接收意愿
 *
 * @param {string} sessionId 握手确认帧下发的会话标识
 * @param {string} topic 订阅主题
 * @returns {Promise<Object>} 统一响应
 */
export function pushSubscribe(sessionId, topic) {
  return request({
    url: '/private/push/sse/upload',
    method: 'post',
    data: {
      sessionId,
      frame: { version: 1, type: 'SUBSCRIBE', topic }
    }
  })
}

/**
 * 发布一条业务消息到指定 topic
 * <p>
 * 走私有接口 /private/push/send（受登录网关鉴权，须携带 Authorization 令牌），body 为 {topic, payload}；
 * 返回 data 含 messageId（消息唯一标识）、topic（实际投递主题）、onlineCount（投递时在线会话数）。
 * 供管理侧发布封装（@/push/admin）复用。
 * </p>
 *
 * @param {string} topic 投递主题（支持 * 单级 / # 多级通配符）
 * @param {Object} payload 业务载荷（须符合 push-message.js 消息契约，否则接收端容错丢弃）
 * @returns {Promise<Object>} data: { messageId, topic, onlineCount }
 */
export function pushSend(topic, payload) {
  return request({
    url: '/private/push/send',
    method: 'post',
    data: { topic, payload }
  }).then(res => res.data)
}

/**
 * 上行退订帧：取消对指定 topic 的接收意愿
 *
 * @param {string} sessionId 握手确认帧下发的会话标识
 * @param {string} topic 退订主题
 * @returns {Promise<Object>} 统一响应
 */
export function pushUnsubscribe(sessionId, topic) {
  return request({
    url: '/private/push/sse/upload',
    method: 'post',
    data: {
      sessionId,
      frame: { version: 1, type: 'UNSUBSCRIBE', topic }
    }
  })
}

/**
 * 上行心跳帧（PING）：保活会话，避免被服务端心跳空闲监控关闭
 *
 * @param {string} sessionId 会话标识
 * @returns {Promise<Object>} 统一响应
 */
export function pushPing(sessionId) {
  return request({
    url: '/private/push/sse/upload',
    method: 'post',
    data: {
      sessionId,
      frame: { version: 1, type: 'PING' }
    }
  })
}

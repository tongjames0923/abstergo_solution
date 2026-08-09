import request from '@/utils/request'

/**
 * 查询网关版本信息（无需认证）
 * @returns {Promise} 返回 version / artifact / buildTime / instancePort
 */
export function getGatewayVersion() {
  return request({
    url: '/public/gateway/version',
    method: 'get'
  })
}

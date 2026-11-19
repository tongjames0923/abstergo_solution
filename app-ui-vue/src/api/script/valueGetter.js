import request from '@/utils/request'

/**
 * 查询值获取器列表
 * @param {{ nameLike?: string, idLike?: string }} params
 */
export function getValueGetters(params) {
  return request({
    url: '/private/value-getter/list',
    method: 'get',
    params
  })
}

/**
 * 获取值获取器详情
 * @param {{ getterId: string }} data
 */
export function getValueGetterDetail(data) {
  return request({
    url: '/private/value-getter/detail',
    method: 'post',
    data
  })
}

/**
 * 调用值获取器
 * @param {{ getterId: string, paramValues: Array }} data
 */
export function invokeValueGetter(data) {
  return request({
    url: '/private/value-getter/invoke',
    method: 'post',
    data
  })
}

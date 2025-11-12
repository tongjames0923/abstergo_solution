import request from '@/utils/request'

/**
 * 获取系统操作类型报表数据
 * @returns {Promise} 报表数据
 */
export function getSystemLogOperateTypeList() {
  return request({
    url: '/private/log/listWithOpType',
    method: 'get'
  })
}
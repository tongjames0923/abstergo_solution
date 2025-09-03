import request from '@/utils/request'

export function getFormConfig(configName) {
  return request({
    url: '/private/view/form',
    method: 'get',
    params: { key: configName }
  })
}

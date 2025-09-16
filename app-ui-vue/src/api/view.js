import request from '@/utils/request'

export function getFormConfig(configName) {
  return request({
    url: '/private/view/form/' + configName,
    method: 'get'
  })
}

export function saveFormConfig(config) {
  return request({
    url: 'private/view/saveForm',
    method: 'post',
    data: config
  })
}

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

export function updateFormConfig(id, config) {
  return request({
    url: `private/view/updateForm/${id}`,
    method: 'put',
    data: config
  })
}

export function deleteFormConfig(id) {
  return request({
    url: `private/view/form/${id}`,
    method: 'delete'
  })
}

export function listForms(pageNum, pageSize) {
  return request({
    url: '/private/view/listForms',
    method: 'get',
    params: { pageNum, pageSize }
  })
}

export function getFormById(id) {
  return request({
    url: `/private/view/form/${id}`,
    method: 'get'
  })
}

export function getRoutes() {
  return request({
    url: '/private/view/routes',
    method: 'get'
  })
}

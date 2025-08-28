import request from "@/utils/request";


export function getSearchFormConfig(configName) {
  return request({
    url: '/private/view/getSearchForm',
    method: 'get',
    params: {key: configName}
  })
}

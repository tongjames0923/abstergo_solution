import request from "@/utils/request";


/**
 * 部门列表
 * @param data
 * @returns {*}
 */
export function fetchDepartmentList(data) {
  return request({
    url: 'private/admin/department/query'
    , method: 'post'
    , data
  })
}

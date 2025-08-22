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

/**
 * 保存部门（新增/修改）
 * @param data 部门数据
 * @returns {*}
 */
export function saveDepartment(data) {
  return request({
    url: 'private/admin/department/save'
    , method: 'post'
    , data
  })
}

/**
 * 删除部门
 * @param id 部门ID
 * @returns {*}
 */
export function deleteDepartment(id) {
  return request({
    url: `private/admin/department/delete/${id}`
    , method: 'get'
  })
}

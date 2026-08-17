import request from '@/utils/request'

/**
 * 管理员查询部门
 * @param {Object} data - 查询条件
 * @param {number} [data.equalsId] - 根据ID精确查询
 * @param {string} [data.likeName] - 根据名称模糊查询
 * @param {string} [data.equalsDepartmentCode] - 根据部门编码精确查询
 * @param {number} [data.equalsCompanyId] - 根据公司ID精确查询
 * @param {Array} [data.flagEquals] - 根据状态IN查询
 * @param {Object} [data.createTimeOrder] - 创建时间排序
 * @param {Object} [data.updateTimeOrder] - 更新时间排序
 * @param {number} [pageNum] - 页码
 * @param {number} [pageSize] - 每页数量
 * @returns {Promise} 查询结果
 */
export function adminQueryDepartment(data, pageNum, pageSize) {
  return request({
    url: '/private/admin/department/query',
    method: 'post',
    params: {
      pageNum,
      pageSize
    },
    data
  })
}

/**
 * 保存部门信息（新增或更新）
 * @param {Object} data - 部门信息
 * @param {number} [data.id] - 部门ID，有ID为更新，无ID为新增
 * @param {string} data.departmentName - 部门名称
 * @param {string} data.departmentCode - 部门编码
 * @param {number} data.companyId - 所属公司ID
 * @param {string} data.manager - 部门负责人
 * @param {string} data.description - 部门描述
 * @param {number} data.flag - 状态
 * @param {number} [data.parentId] - 父部门ID
 * @returns {Promise} 保存结果
 */
export function saveDepartment(data) {
  return request({
    url: '/private/admin/department/save',
    method: 'post',
    data
  })
}

/**
 * 根据ID获取部门信息
 * @param {number} id - 部门ID
 * @returns {Promise} 部门信息
 */
export function getDepartmentById(id) {
  return request({
    url: `/private/admin/department/get/${id}`,
    method: 'get'
  })
}

/**
 * 删除部门
 * @param {number} id - 部门ID
 * @returns {Promise} 删除结果
 */
export function deleteDepartment(id) {
  return request({
    url: `/private/admin/department/delete/${id}`,
    method: 'post'
  })
}

/**
 * 移动部门到指定父级部门下
 * @param {number} parent - 父级部门ID
 * @param {number} child - 子级部门ID
 * @returns {Promise} 操作结果
 */
export function moveDepartmentTo(parent, child) {
  return request({
    url: '/private/admin/department/moveDepartmentTo',
    method: 'post',
    params: {
      parent,
      child
    }
  })
}

/**
 * 获取指定部门的所有子级部门列表
 * @param {number} parentId - 父级部门ID
 * @returns {Promise} 子级部门列表
 */
export function listChildren(parentId) {
  return request({
    url: `/private/admin/department/listChildren/${parentId}`,
    method: 'get'
  })
}

/**
 * 申请用户加入部门
 * @param userId
 * @param departmentId
 * @returns {*}
 */
export function applyUserToDepartment(userId, departmentId) {
  return request({
    url: 'private/department/addEmp/' + departmentId + '/' + userId,
    method: 'post'
  })
}

/**
 * 移除用户从部门
 * @param userId
 * @param departmentId
 * @returns {*}
 */
export function removeUserFromDepartment(userId, departmentId) {
  return request({
    url: 'private/department/delEmp/' + departmentId + '/' + userId,
    method: 'post'
  })
}

/**
 * 获取指定部门的员工列表
 * @param {number} departmentId - 部门ID
 * @returns {Promise} 员工列表
 */
export function getDepartmentEmployees(departmentId) {
  return request({
    url: `/private/department/employee/${departmentId}`,
    method: 'get'
  })
}

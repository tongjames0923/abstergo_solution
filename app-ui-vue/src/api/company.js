import request from '@/utils/request'

/**
 * 管理员查询公司
 * @param {Object} data - 查询条件
 * @param {string} [data.idEquals] - 根据ID精确查询
 * @param {string} [data.companyNameLike] - 根据名称模糊查询
 * @param {string} [data.taxNumberLike] - 根据税号模糊查询
 * @param {number} [data.flagEquals] - 根据标志位精确查询
 * @param {Object} [data.createTimeOrder] - 创建时间排序
 * @param {Object} [data.updateTimeOrder] - 更新时间排序
 * @param {number} [pageNum] - 页码
 * @param {number} [pageSize] - 每页数量
 * @returns {Promise} 查询结果
 */
export function adminQueryCompany(data, pageNum, pageSize) {
  return request({
    url: '/private/admin/company/query',
    method: 'post',
    params: {
      pageNum,
      pageSize
    },
    data
  })
}

/**
 * 激活公司
 * @param {number} companyId - 公司ID
 * @returns {Promise} 操作结果
 */
export function activateCompany(companyId) {
  return request({
    url: `/private/admin/company/activite/${companyId}`,
    method: 'get'
  })
}

/**
 * 禁用公司
 * @param {number} companyId - 公司ID
 * @returns {Promise} 操作结果
 */
export function disableCompany(companyId) {
  return request({
    url: `/private/admin/company/disable/${companyId}`,
    method: 'get'
  })
}

/**
 * 保存公司信息（新增或更新）
 * @param {Object} data - 公司信息
 * @param {number} [data.id] - 公司ID，有ID为更新，无ID为新增
 * @param {string} data.companyName - 公司名称
 * @param {string} data.taxNumber - 公司税号
 * @param {string} data.description - 备注
 * @param {number} data.flag - 状态
 * @returns {Promise} 保存结果
 */
export function saveCompany(data) {
  return request({
    url: '/private/admin/company/save',
    method: 'post',
    data
  })
}

/**
 * 根据ID获取公司信息
 * @param {number} id - 公司ID
 * @returns {Promise} 公司信息
 */
export function getCompanyById(id) {
  return request({
    url: `/private/admin/company/get/${id}`,
    method: 'get'
  })
}

/**
 * 删除公司
 * @param {number} id - 公司ID
 * @returns {Promise} 删除结果
 */
export function deleteCompany(id) {
  return request({
    url: `/private/admin/company/delete/${id}`,
    method: 'post'
  })
}

/**
 * 移动公司到指定父级公司下
 * @param {number} parent - 父级公司ID
 * @param {number} child - 子级公司ID
 * @returns {Promise} 操作结果
 */
export function moveCompanyTo(parent, child) {
  return request({
    url: '/private/admin/company/moveCompanyTo',
    method: 'post',
    params: {
      parent,
      child
    }
  })
}

/**
 * 获取指定公司的所有子级公司列表
 * @param {number} parentId - 父级公司ID
 * @returns {Promise} 子级公司列表
 */
export function listChildren(parentId) {
  return request({
    url: `/private/admin/company/listChildren/${parentId}`,
    method: 'get'
  })
}

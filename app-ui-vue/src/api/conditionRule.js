import request from '@/utils/request'

/**
 * 分页查询条件规则
 * @param {Object} data 查询条件
 * @param {number} pageNum 页码
 * @param {number} pageSize 每页大小
 */
export function queryConditionRules(data, pageNum = 1, pageSize = 20) {
  return request({
    url: '/private/condition-rule/query',
    method: 'post',
    params: { pageNum, pageSize },
    data
  })
}

/**
 * 按主键获取规则详情
 * @param {number} id 规则主键
 */
export function getConditionRule(id) {
  return request({
    url: `/private/condition-rule/get/${id}`,
    method: 'get'
  })
}

/**
 * 按编码获取规则详情
 * @param {string} ruleCode 规则编码
 */
export function getConditionRuleByCode(ruleCode) {
  return request({
    url: `/private/condition-rule/get-by-code/${ruleCode}`,
    method: 'get'
  })
}

/**
 * 保存规则（新建或编辑）
 * @param {Object} data 规则实体
 */
export function saveConditionRule(data) {
  return request({
    url: '/private/condition-rule/save',
    method: 'post',
    data
  })
}

/**
 * 删除规则
 * @param {number} id 规则主键
 */
export function deleteConditionRule(id) {
  return request({
    url: `/private/condition-rule/delete/${id}`,
    method: 'post'
  })
}

/**
 * 启用或停用规则
 * @param {number} id 规则主键
 * @param {boolean} enabled 是否启用
 */
export function setConditionRuleEnabled(id, enabled) {
  return request({
    url: `/private/condition-rule/${enabled ? 'enable' : 'disable'}/${id}`,
    method: 'post'
  })
}

/**
 * 按编码执行规则
 * @param {string} ruleCode 规则编码
 */
export function evaluateConditionRule(ruleCode) {
  return request({
    url: `/private/condition-rule/evaluate/${ruleCode}`,
    method: 'post'
  })
}

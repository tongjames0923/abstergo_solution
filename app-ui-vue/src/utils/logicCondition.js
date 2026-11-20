/**
 * 逻辑条件表达式树工具。
 * <p>
 * 分组节点在提交时编译为 LogicConditionGetter 调用 JSON，因此后端无需感知树结构即可支持
 * 「且」「或」混合嵌套；反向解析时把嵌套调用还原为分组节点，保证编辑态可往返。
 * </p>
 */

/**
 * 逻辑条件 getter ID
 */
const LOGIC_GETTER_ID = 'LogicConditionGetter'

/**
 * 单个分组最多承载的条件位，与后端声明数量一致
 */
const MAX_CHILDREN = 8

/**
 * 节点 ID 序列
 */
let sequence = 0

/**
 * 生成稳定的节点 ID
 *
 * @returns {string} 节点 ID
 */
export function nextNodeId() {
  sequence += 1
  return 'lc-' + sequence
}

/**
 * 创建条件叶子节点
 *
 * @param {string} value 条件原始字符串
 * @returns {Object} 条件节点
 */
export function createCondition(value = '') {
  return { id: nextNodeId(), type: 'condition', value }
}

/**
 * 创建分组节点
 *
 * @param {string} op 逻辑运算符
 * @param {Array} children 子节点列表
 * @returns {Object} 分组节点
 */
export function createGroup(op = 'and', children = null) {
  return {
    id: nextNodeId(),
    type: 'group',
    op,
    children: children || [createCondition()]
  }
}

/**
 * 判断节点是否为空
 *
 * @param {Object} node 表达式节点
 * @returns {boolean} 是否为空
 */
export function isEmptyNode(node) {
  if (!node) {
    return true
  }
  if (node.type === 'condition') {
    return !node.value || !String(node.value).trim()
  }
  return !hasCondition(node)
}

/**
 * 判断表达式树中是否至少有一个有效条件
 *
 * @param {Object} node 表达式节点
 * @returns {boolean} 是否存在有效条件
 */
export function hasCondition(node) {
  if (!node) {
    return false
  }
  if (node.type === 'condition') {
    return !isEmptyNode(node)
  }
  return (node.children || []).some(hasCondition)
}

/**
 * 统计表达式树中的有效条件数量
 *
 * @param {Object} node 表达式节点
 * @returns {number} 有效条件数
 */
export function countConditions(node) {
  if (!node) {
    return 0
  }
  if (node.type === 'condition') {
    return isEmptyNode(node) ? 0 : 1
  }
  return (node.children || []).reduce((sum, child) => sum + countConditions(child), 0)
}

/**
 * 编译节点为请求参数值
 *
 * @param {Object} node 表达式节点
 * @param {Array} meta 后端参数定义，用于取参数名
 * @returns {string} 条件原始字符串
 */
function compileNode(node, meta) {
  if (node.type === 'condition') {
    return node.value || ''
  }
  return JSON.stringify({
    getterId: LOGIC_GETTER_ID,
    paramValues: compileGroup(node, meta)
  })
}

/**
 * 把分组编译为 LogicConditionGetter 的参数列表
 *
 * @param {Object} group 分组节点
 * @param {Array} meta 后端参数定义
 * @returns {Array} ParamValue 列表
 */
export function compileGroup(group, meta) {
  const paramTypes = Array.isArray(meta) ? meta : []
  const opField = paramTypes.find(f => f.isEnum)
  const operandFields = paramTypes.filter(f => f.type === 'operand')
  const params = [{
    type: 'string',
    name: opField ? opField.name : '',
    sort: 1,
    value: group.op || 'and'
  }]
  const children = (group.children || []).filter(child => !isEmptyNode(child)).slice(0, MAX_CHILDREN)
  children.forEach((child, index) => {
    params.push({
      type: 'operand',
      name: operandFields[index] ? operandFields[index].name : '',
      sort: index + 2,
      value: compileNode(child, meta)
    })
  })
  return params
}

/**
 * 从参数字符串还原节点，嵌套的 LogicConditionGetter 调用会还原为分组
 *
 * @param {string} raw 条件原始字符串
 * @returns {Object} 表达式节点
 */
function restoreNode(raw) {
  if (!raw || typeof raw !== 'string' || !raw.trim().startsWith('{')) {
    return createCondition(raw == null ? '' : String(raw))
  }
  let parsed = null
  try {
    const obj = JSON.parse(raw)
    if (obj && typeof obj.getterId === 'string' && Array.isArray(obj.paramValues)) {
      parsed = obj
    }
  } catch (e) {
    return createCondition(raw)
  }
  if (!parsed || parsed.getterId !== LOGIC_GETTER_ID) {
    return createCondition(raw)
  }
  const values = parsed.paramValues
  const opParam = values.find(p => p && Number(p.sort) === 1)
  const conditionParams = values.filter(p => p && Number(p.sort) > 1 && p.value)
    .sort((a, b) => Number(a.sort) - Number(b.sort))
  const children = conditionParams.map(p => restoreNode(p.value))
  return createGroup(opParam && opParam.value ? opParam.value : 'and', children.length ? children : [createCondition()])
}

/**
 * 从运算符与扁平条件位还原整棵表达式树
 *
 * @param {string} op 逻辑运算符
 * @param {Array} conditionValues 条件原始字符串列表
 * @returns {Object} 分组节点
 */
export function restoreLogicTree(op, conditionValues) {
  const values = Array.isArray(conditionValues) ? conditionValues : []
  const children = values.filter(v => v && String(v).trim()).map(v => restoreNode(v))
  return createGroup(op || 'and', children.length ? children : [createCondition()])
}

/**
 * 按节点 ID 更新树中的节点
 *
 * @param {Object} node 当前节点
 * @param {string} id 目标节点 ID
 * @param {Object} patch 更新内容
 * @returns {Object} 更新后的节点
 */
export function updateNode(node, id, patch) {
  if (!node) {
    return node
  }
  if (node.id === id) {
    return Object.assign({}, node, patch)
  }
  if (node.type !== 'group') {
    return node
  }
  return Object.assign({}, node, {
    children: node.children.map(child => updateNode(child, id, patch))
  })
}

/**
 * 按节点 ID 删除树中的节点，根节点不可删除
 *
 * @param {Object} node 当前节点
 * @param {string} id 目标节点 ID
 * @returns {Object} 删除后的节点
 */
export function removeNode(node, id) {
  if (!node || node.type !== 'group') {
    return node
  }
  return Object.assign({}, node, {
    children: node.children.filter(child => child.id !== id).map(child => removeNode(child, id))
  })
}

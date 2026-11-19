/**
 * 值获取器调试页共用工具。
 * <p>
 * 统一 ParamType 到表单控件的映射，以及 getter 调用操作数的序列化/反序列化。
 * 序列化格式与后端 ConditionGetter.Operand 保持一致：{getterId, paramValues}。
 * </p>
 */

/**
 * 按参数类型推断控件类型
 *
 * @param {string} rawType 后端声明的参数类型
 * @returns {{uiType: string, dateFormat: string|null}} 控件类型与日期格式
 */
export function resolveUiType(rawType) {
  const t = (rawType || 'string').toLowerCase()
  if (t === 'operand') {
    return { uiType: 'operand', dateFormat: null }
  }
  const numberPrefixes = ['number', 'int', 'integer', 'long', 'float', 'double', 'decimal']
  if (numberPrefixes.some(p => t.startsWith(p))) {
    return { uiType: 'number', dateFormat: null }
  }
  if (t.startsWith('date') || t.startsWith('time') || t.startsWith('datetime')) {
    const idx = t.indexOf(':')
    const fmt = idx >= 0 ? rawType.substring(idx + 1) : 'yyyy-MM-dd HH:mm:ss'
    return { uiType: 'date', dateFormat: fmt }
  }
  return { uiType: 'string', dateFormat: null }
}

/**
 * 把 ParamType 列表转换为按 sort 升序排列的字段描述。
 * <p>
 * 对 ConditionGetter 的左右操作数做兜底识别：即使后端仍返回 string 类型，
 * 也按 operand 渲染嵌套编辑器，避免用户面对原始 JSON 输入框。
 * </p>
 *
 * @param {Array} paramTypes 后端参数定义
 * @param {string} getterId getter ID，用于兜底识别
 * @returns {Array} 字段描述列表
 */
export function toParamFields(paramTypes, getterId) {
  if (!Array.isArray(paramTypes)) {
    return []
  }
  return [...paramTypes].sort((a, b) => {
    const sa = a.sort == null ? Number.MAX_SAFE_INTEGER : a.sort
    const sb = b.sort == null ? Number.MAX_SAFE_INTEGER : b.sort
    return sa - sb
  }).map(pt => {
    const resolved = resolveUiType(pt.type)
    const dateFormat = resolved.dateFormat
    let uiType = resolved.uiType
    if (getterId === 'ConditionGetter' && !pt.isEnum) {
      uiType = 'operand'
    }
    return {
      ...pt,
      isEnum: !!pt.isEnum,
      required: !!pt.required,
      enumValues: pt.enumValues || [],
      uiType,
      dateFormat
    }
  })
}

/**
 * 根据字段描述初始化表单数据
 *
 * @param {Array} fields 字段描述列表
 * @returns {Object} 表单数据
 */
export function buildFormData(fields) {
  const data = {}
  fields.forEach(f => {
    if (f.uiType === 'operand') {
      data[f.name] = ''
    } else if (f.isEnum || f.uiType === 'number') {
      data[f.name] = undefined
    } else if (f.uiType === 'date') {
      data[f.name] = null
    } else {
      data[f.name] = ''
    }
  })
  return data
}

/**
 * 把表单数据转换为 ParamValue 列表
 *
 * @param {Array} fields 字段描述列表
 * @param {Object} formData 表单数据
 * @returns {Array} ParamValue 列表
 */
export function toParamValues(fields, formData) {
  return fields.map(f => {
    const raw = formData[f.name]
    let value = null
    if (raw !== undefined && raw !== null && raw !== '') {
      value = typeof raw === 'string' ? raw : String(raw)
    }
    return {
      type: f.type,
      name: f.name,
      sort: f.sort,
      value
    }
  })
}

/**
 * 判断字符串是否为 getter 调用操作数
 *
 * @param {string} raw 操作数原始字符串
 * @returns {boolean} 是否为 getter 调用
 */
export function isGetterOperand(raw) {
  if (!raw || typeof raw !== 'string' || !raw.trim().startsWith('{')) {
    return false
  }
  try {
    const obj = JSON.parse(raw)
    return !!(obj && typeof obj.getterId === 'string' && obj.getterId.trim())
  } catch (e) {
    return false
  }
}

/**
 * 解析 getter 调用操作数
 *
 * @param {string} raw 操作数原始字符串
 * @returns {{getterId: string, paramValues: Array}|null} 解析结果，字面量返回 null
 */
export function parseGetterOperand(raw) {
  if (!isGetterOperand(raw)) {
    return null
  }
  try {
    const obj = JSON.parse(raw)
    return {
      getterId: obj.getterId,
      paramValues: Array.isArray(obj.paramValues) ? obj.paramValues : []
    }
  } catch (e) {
    return null
  }
}

/**
 * 序列化 getter 调用操作数
 *
 * @param {string} getterId getter ID
 * @param {Array} paramValues 参数值列表
 * @returns {string} JSON 字符串
 */
export function stringifyGetterOperand(getterId, paramValues) {
  return JSON.stringify({
    getterId,
    paramValues: Array.isArray(paramValues) ? paramValues : []
  })
}

/**
 * 值获取器调试页共用工具。
 * <p>
 * 统一 ParamType 到输入控件的映射、表单默认值与校验规则，以及 getter 调用操作数的序列化/反序列化。
 * 序列化格式与后端 ConditionGetter.Operand 保持一致：{getterId, paramValues}。
 * </p>
 */

/**
 * 默认日期时间格式
 */
const DEFAULT_DATE_FORMAT = 'yyyy-MM-dd HH:mm:ss'

/**
 * 数值类型前缀
 */
const NUMBER_PREFIXES = ['number', 'int', 'integer', 'long', 'float', 'double', 'decimal', 'bigdecimal', 'short']

/**
 * 显式输入控件类型别名表
 */
const UI_TYPE_ALIASES = {
  text: 'text',
  string: 'text',
  input: 'text',
  textarea: 'textarea',
  json: 'json',
  number: 'number',
  integer: 'number',
  int: 'number',
  long: 'number',
  decimal: 'number',
  boolean: 'boolean',
  bool: 'boolean',
  switch: 'boolean',
  date: 'date',
  datetime: 'datetime',
  time: 'time',
  select: 'select',
  enum: 'select',
  operand: 'operand'
}

/**
 * 把后端声明的类型归一化为前端控件类型
 *
 * @param {string} type 后端声明的类型
 * @returns {string} 控件类型
 */
function normalizeUiType(type) {
  const lower = (type || '').toLowerCase()
  if (UI_TYPE_ALIASES[lower]) {
    return UI_TYPE_ALIASES[lower]
  }
  if (NUMBER_PREFIXES.some(p => lower.startsWith(p))) {
    return 'number'
  }
  if (lower.startsWith('bool')) {
    return 'boolean'
  }
  if (lower.startsWith('date') || lower.startsWith('time') || lower.startsWith('datetime')) {
    return inferDateUiType(lower)
  }
  return 'text'
}

/**
 * 按日期类型字符串推断日期粒度
 *
 * @param {string} lower 小写类型字符串
 * @returns {string} date / datetime / time
 */
function inferDateUiType(lower) {
  if (lower.startsWith('time') && !lower.startsWith('timestamp')) {
    return 'time'
  }
  if (lower.startsWith('date') && lower.indexOf('hh') < 0) {
    return 'date'
  }
  return 'datetime'
}

/**
 * 解析日期控件格式
 *
 * @param {string} rawType 后端声明的原始类型
 * @param {string} uiType 控件类型
 * @returns {string|null} 日期格式
 */
function resolveDateFormat(rawType, uiType) {
  if (uiType !== 'date' && uiType !== 'datetime' && uiType !== 'time') {
    return null
  }
  const raw = rawType || ''
  const idx = raw.indexOf(':')
  if (idx >= 0) {
    return raw.substring(idx + 1)
  }
  if (uiType === 'date') {
    return 'yyyy-MM-dd'
  }
  if (uiType === 'time') {
    return 'HH:mm:ss'
  }
  return DEFAULT_DATE_FORMAT
}

/**
 * 解析参数对应的输入控件类型
 *
 * @param {string} rawType 后端声明的参数类型
 * @param {string} inputType 后端声明的控件类型，优先使用
 * @returns {{uiType: string, dateFormat: string|null}} 控件类型与日期格式
 */
export function resolveUiType(rawType, inputType) {
  const uiType = normalizeUiType(inputType || rawType)
  return { uiType, dateFormat: resolveDateFormat(rawType, uiType) }
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
    const ui = pt.ui || {}
    const resolved = resolveUiType(pt.type, ui.inputType)
    let uiType = resolved.uiType
    if (getterId === 'ConditionGetter' && !pt.isEnum) {
      uiType = 'operand'
    } else if (pt.isEnum) {
      uiType = 'select'
    }
    return {
      ...pt,
      label: pt.label || pt.name,
      isEnum: !!pt.isEnum,
      required: !!pt.required,
      options: pt.enumValues || [],
      uiType,
      dateFormat: resolved.dateFormat,
      placeholder: ui.placeholder || pt.description || '',
      defaultValue: ui.defaultValue == null ? null : ui.defaultValue,
      filterable: !!ui.filterable,
      allowCreate: !!ui.allowCreate,
      maxLength: ui.maxLength == null ? undefined : ui.maxLength,
      pattern: ui.pattern || '',
      min: ui.min == null ? undefined : Number(ui.min),
      max: ui.max == null ? undefined : Number(ui.max),
      step: ui.step == null ? undefined : Number(ui.step)
    }
  })
}

/**
 * 判断值是否为空（未填写）
 *
 * @param {*} value 表单值
 * @returns {boolean} 是否为空
 */
export function isBlank(value) {
  return value === undefined || value === null || value === '' ||
    (Array.isArray(value) && value.length === 0)
}

/**
 * 把默认值字符串解析为控件值
 *
 * @param {Object} field 字段描述
 * @returns {*} 控件初始值
 */
export function parseDefaultValue(field) {
  const raw = field.defaultValue
  switch (field.uiType) {
    case 'number': {
      if (isBlank(raw)) {
        return undefined
      }
      const num = Number(raw)
      return Number.isNaN(num) ? undefined : num
    }
    case 'boolean':
      return raw === 'true' || raw === '1'
    case 'date':
    case 'datetime':
    case 'time':
      return isBlank(raw) ? null : raw
    case 'select':
      return isBlank(raw) ? undefined : raw
    case 'operand':
      return ''
    default:
      return raw == null ? '' : String(raw)
  }
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
    data[f.name] = parseDefaultValue(f)
  })
  return data
}

/**
 * 校验单个字段，返回校验失败的规则标识
 *
 * @param {Object} field 字段描述
 * @param {*} value 表单值
 * @returns {string|null} 规则标识：required / pattern / min / max / maxLength / json，通过返回 null
 */
export function validateField(field, value) {
  if (field.uiType === 'operand') {
    return field.required && isBlank(value) ? 'required' : null
  }
  if (isBlank(value)) {
    return field.required ? 'required' : null
  }
  if (field.uiType === 'number' && Number.isNaN(Number(value))) {
    return 'number'
  }
  if (field.uiType === 'json' && !isJsonString(value)) {
    return 'json'
  }
  const text = String(value)
  if (field.maxLength && text.length > field.maxLength) {
    return 'maxLength'
  }
  if (field.min !== undefined && Number(value) < field.min) {
    return 'min'
  }
  if (field.max !== undefined && Number(value) > field.max) {
    return 'max'
  }
  if (field.pattern && !matchPattern(field.pattern, text)) {
    return 'pattern'
  }
  return null
}

/**
 * 校验整张参数表单
 *
 * @param {Array} fields 字段描述列表
 * @param {Object} formData 表单数据
 * @returns {Object} 参数名到失败规则的映射，空对象表示全部通过
 */
export function validateFields(fields, formData) {
  const errors = {}
  fields.forEach(field => {
    const rule = validateField(field, formData[field.name])
    if (rule) {
      errors[field.name] = rule
    }
  })
  return errors
}

/**
 * 判断字符串是否为合法 JSON
 *
 * @param {*} value 待校验值
 * @returns {boolean} 是否合法
 */
export function isJsonString(value) {
  if (typeof value !== 'string' || !value.trim()) {
    return false
  }
  try {
    JSON.parse(value)
    return true
  } catch (e) {
    return false
  }
}

/**
 * 执行正则校验，非法正则视为通过
 *
 * @param {string} pattern 正则表达式
 * @param {string} text 待校验文本
 * @returns {boolean} 是否匹配
 */
function matchPattern(pattern, text) {
  try {
    return new RegExp(pattern).test(text)
  } catch (e) {
    return true
  }
}

/**
 * 把单个表单值序列化为 ParamValue.value
 *
 * @param {Object} field 字段描述
 * @param {*} raw 表单值
 * @returns {string|null} 序列化结果
 */
export function toParamValue(field, raw) {
  if (isBlank(raw)) {
    return null
  }
  if (field.uiType === 'json') {
    try {
      return JSON.stringify(JSON.parse(raw))
    } catch (e) {
      return String(raw)
    }
  }
  if (field.uiType === 'boolean') {
    return raw ? 'true' : 'false'
  }
  if (typeof raw === 'string') {
    return raw
  }
  return String(raw)
}

/**
 * 把表单数据转换为 ParamValue 列表
 *
 * @param {Array} fields 字段描述列表
 * @param {Object} formData 表单数据
 * @returns {Array} ParamValue 列表
 */
export function toParamValues(fields, formData) {
  return fields.map(f => ({
    type: f.type,
    name: f.name,
    sort: f.sort,
    value: toParamValue(f, formData[f.name])
  }))
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

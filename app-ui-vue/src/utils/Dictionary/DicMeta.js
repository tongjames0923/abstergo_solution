import request from "@/utils/request";

/**
 * 字典元数据配置类
 * 用于配置字典的数据获取和转换
 */
export class DicMeta {
  /**
   * 构造函数
   * @param {Object} options 配置选项
   * @param {Function} options.request 获取数据的Promise函数
   * @param {Function} options.transform 数据转换函数
   * @param {string} options.labelField 标签字段名，默认为'label'
   * @param {string} options.valueField 值字段名，默认为'value'
   */
  constructor(options = {}) {
    this.request = options.request
    this.transform = options.transform
    this.labelField = options.labelField || 'label'
    this.valueField = options.valueField || 'value'
  }

  /**
   * 设置数据请求函数
   * @param {Function} request 返回Promise的函数
   * @returns {DicMeta} 返回当前实例，支持链式调用
   */
  setRequest(request) {
    this.request = request
    return this
  }

  /**
   * 设置数据转换函数
   * @param {Function} transform 数据转换函数
   * @returns {DicMeta} 返回当前实例，支持链式调用
   */
  setTransform(transform) {
    this.transform = transform
    return this
  }

  /**
   * 设置标签字段名
   * @param {string} field 标签字段名
   * @returns {DicMeta} 返回当前实例，支持链式调用
   */
  setLabelField(field) {
    this.labelField = field
    return this
  }

  /**
   * 设置值字段名
   * @param {string} field 值字段名
   * @returns {DicMeta} 返回当前实例，支持链式调用
   */
  setValueField(field) {
    this.valueField = field
    return this
  }
}

export function createDicMeta(apiUrl, transform, labelField, valueField) {
  // 创建请求函数
  const req = () => {
    // 如果 apiUrl 是函数，则调用它获取数据
    if (typeof apiUrl === 'function') {
      // 将函数返回的结果包装成 axios promise
      return new Promise((resolve, reject) => {
        try {
          const result = apiUrl();
          // 如果函数返回的是 promise，则直接返回
          if (result instanceof Promise) {
            result.then(resolve).catch(reject);
          } else {
            // 如果是普通值，则包装成 resolved promise
            resolve({data: result});
          }
        } catch (error) {
          reject(error);
        }
      });
    } else {
      // 否则使用常规请求方式
      return () => request({
        url: apiUrl,
        method: 'get'
      });
    }
  };
  return new DicMeta({
    request: req,
    transform: transform || null,
    labelField: labelField || 'label',
    valueField: valueField || 'value'
  });
}

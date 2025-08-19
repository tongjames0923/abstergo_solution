import {DicMeta} from "@/utils/Dictionary/DicMeta";
import Vue from "vue";

/**
 * 字典类
 * 基于DicMeta配置从Promise获取并管理字典数据
 */
export class Dictionary {
  /**
   * 构造函数
   * @param {DicMeta} meta 字典元数据配置
   */
  constructor(meta) {
    if (!(meta instanceof DicMeta)) {
      throw new Error('Parameter meta must be an instance of DicMeta')
    }

    this.meta = meta
    // 使用Vue.observable使数据具有响应性
    this._data = Vue.observable({
      items: [],
      loading: false,
      loaded: false
    })
  }

  /**
   * 获取字典数据
   * @returns {Array} 字典数据列表
   */
  get items() {
    return this._data.items
  }

  /**
   * 获取加载状态
   * @returns {boolean} 是否正在加载
   */
  get loading() {
    return this._data.loading
  }

  /**
   * 获取是否已加载
   * @returns {boolean} 是否已加载
   */
  get loaded() {
    return this._data.loaded
  }

  /**
   * 根据值获取标签
   * @param {*} value 值
   * @returns {string|null} 对应的标签
   */
  getLabel(value) {
    const item = this.items.find(item => item[this.meta.valueField] === value)
    return item ? item[this.meta.labelField] : null
  }

  /**
   * 根据标签获取值
   * @param {string} label 标签
   * @returns {*|null} 对应的值
   */
  getValue(label) {
    const item = this.items.find(item => item[this.meta.labelField] === label)
    return item ? item[this.meta.valueField] : null
  }

  /**
   * 从Promise加载字典数据
   * @returns {Promise<Array>} 返回加载的字典数据
   */
  async load() {
    if (typeof this.meta.request !== 'function') {
      throw new Error('Request function is required for loading dictionary data')
    }

    this._data.loading = true

    try {
      let data = await this.meta.request()

      // 如果配置了转换函数，则使用转换函数处理数据
      if (typeof this.meta.transform === 'function') {
        data = this.meta.transform(data, this.meta)
      }

      // 确保数据是数组格式
      if (!Array.isArray(data)) {
        data = [data]
      }

      this._data.items = data
      this._data.loaded = true
      return data
    } catch (error) {
      console.error('Failed to load dictionary data:', error)
      throw error
    } finally {
      this._data.loading = false
    }
  }

  /**
   * 重新加载字典数据
   * @returns {Promise<Array>} 返回加载的字典数据
   */
  reload() {
    return this.load()
  }

  /**
   * 清空字典数据
   */
  clear() {
    this._data.items = []
    this._data.loaded = false
  }

  /**
   * 查找符合条件的字典项
   * @param {Function} predicate 查找条件函数
   * @returns {Object|undefined} 符合条件的字典项
   */
  find(predicate) {
    return this.items.find(predicate)
  }

  /**
   * 过滤字典项
   * @param {Function} predicate 过滤条件函数
   * @returns {Array} 过滤后的字典项列表
   */
  filter(predicate) {
    return this.items.filter(predicate)
  }
}

import {mapActions, mapGetters} from 'vuex'

export default {
  computed: {
    ...mapGetters('dictionaries', [
      'dictionaryItems',
      'dictionaryRawData',
      'dictionaryLoading',
      'dictionaryLoaded',
      'dictionaryError',
      'dictLabel',
      'dictValue',
      'dictionary'
    ])
  },
  methods: {
    ...mapActions('dictionaries', [
      'createDictionary',
      'loadDictionary',
      'ensureDictionaryLoaded',
      'reloadDictionary',
      'clearCache'
    ]),

    /**
     * 初始化字典
     * @param {String} dictKey 字典键名
     * @param {Object} options 字典配置选项
     * @param {String} options.apiUrl API地址
     * @param {Function} options.transform 数据转换函数
     * @param {String} options.labelField 标签字段名
     * @param {String} options.valueField 值字段名
     * @param {Boolean} options.lazy 是否懒加载
     */
    async initDictionary(dictKey, options = {}) {
      if (!dictKey) {
        console.warn('字典键名不能为空')
        return
      }

      try {
        // 先检查字典是否已存在
        const existingDict = this.dictionary(dictKey)
        if (existingDict) {
          // 字典已存在，无需重新创建
          console.log(`字典 ${dictKey} 已存在，跳过创建`)
          return existingDict
        }

        // 创建字典实例
        const dictionary = await this.createDictionary({
          key: dictKey,
          apiUrl: options.apiUrl,
          transform: options.transform,
          labelField: options.labelField,
          valueField: options.valueField,
          lazy: options.lazy !== false // 默认为懒加载
        })

        // 如果不是懒加载，则立即加载
        if (options.lazy === false) {
          await this.loadDictionary(dictKey)
        }

        return dictionary
      } catch (error) {
        console.error(`初始化字典 ${dictKey} 失败:`, error)
      }
    },

    /**
     * 确保字典已加载
     * @param {String} dictKey 字典键名
     */
    async ensureDictLoaded(dictKey) {
      if (!dictKey) {
        console.warn('字典键名不能为空')
        return
      }

      try {
        await this.ensureDictionaryLoaded(dictKey)
      } catch (error) {
        console.error(`确保字典 ${dictKey} 加载失败:`, error)
      }
    },

    /**
     * 重新加载字典（强制刷新缓存）
     * @param {String} dictKey 字典键名
     */
    async reloadDict(dictKey) {
      if (!dictKey) {
        console.warn('字典键名不能为空')
        return
      }

      try {
        await this.reloadDictionary(dictKey)
      } catch (error) {
        console.error(`重新加载字典 ${dictKey} 失败:`, error)
      }
    },

    /**
     * 清除字典缓存
     * @param {String} dictKey 字典键名，如果不提供则清除所有缓存
     */
    async clearDictCache(dictKey) {
      try {
        await this.clearCache(dictKey)
      } catch (error) {
        console.error(`清除字典缓存失败:`, error)
      }
    },

    /**
     * 获取字典选项列表
     * @param {String} dictKey 字典键名
     * @returns {Array} 字典选项列表
     */
    getDictItems(dictKey) {
      return this.dictionaryItems(dictKey) || []
    },

    /**
     * 获取字典原始数据列表
     * @param {String} dictKey 字典键名
     * @returns {Array} 字典原始数据列表
     */
    getDictRawData(dictKey) {
      return this.dictionaryRawData(dictKey) || []
    },

    /**
     * 根据值获取标签
     * @param {String} dictKey 字典键名
     * @param {*} value 值
     * @returns {String|null} 标签
     */
    getDictLabel(dictKey, value) {
      return this.dictLabel(dictKey, value)
    },

    /**
     * 根据标签获取值
     * @param {String} dictKey 字典键名
     * @param {String} label 标签
     * @returns {*|null} 值
     */
    getDictValue(dictKey, label) {
      return this.dictValue(dictKey, label)
    },

    /**
     * 获取字典加载状态
     * @param {String} dictKey 字典键名
     * @returns {Boolean} 是否正在加载
     */
    isDictLoading(dictKey) {
      return this.dictionaryLoading(dictKey) || false
    },

    /**
     * 获取字典是否已加载
     * @param {String} dictKey 字典键名
     * @returns {Boolean} 是否已加载
     */
    isDictLoaded(dictKey) {
      return this.dictionaryLoaded(dictKey) || false
    },

    /**
     * 获取字典错误信息
     * @param {String} dictKey 字典键名
     * @returns {String|null} 错误信息
     */
    getDictError(dictKey) {
      return this.dictionaryError(dictKey) || null
    }
  }
}

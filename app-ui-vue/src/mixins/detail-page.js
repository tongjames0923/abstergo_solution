/**
 * 详情页通用 mixin
 *
 * 为基于 URL 动态参数的详情页（如 /user/details/:userId）提供统一能力：
 * 参数校验、路由参数变化监听、加载状态机与错误重试。
 *
 * 接入方式：页面组件在 detailConfig 中声明配置，并按需实现 afterDetailLoaded 钩子：
 * {
 *   detailConfig: {
 *     paramName: 'userId',                  // 路由动态参数名，与路由定义保持一致
 *     idPattern: /^\d+$/,                    // 参数合法性校验正则
 *     fetcher: (id) => getUserDetail(id),    // 数据获取函数，返回 request 封装的 Promise
 *     fallbackRoute: '/common/user'          // 参数非法时的兜底跳转目标
 *   }
 * }
 */
export default {
  data() {
    return {
      detailData: null,
      detailLoading: false,
      detailError: null
    }
  },

  computed: {
    /**
     * 当前路由中的详情标识参数
     */
    detailId() {
      return this.$route.params[this.detailConfig.paramName]
    }
  },

  watch: {
    /**
     * 同一组件被不同参数复用时（如 userId 1 -> 2），重新拉取数据
     */
    detailId(newVal, oldVal) {
      if (newVal !== oldVal) {
        this.loadDetail()
      }
    }
  },

  created() {
    this.loadDetail()
  },

  methods: {
    /**
     * 加载详情数据：先校验参数，再走状态机请求
     */
    async loadDetail() {
      const config = this.detailConfig
      const rawId = this.$route.params[config.paramName]

      if (!config.idPattern.test(String(rawId))) {
        this.$message.error(this.$t('detail.invalidId'))
        this.$router.replace(config.fallbackRoute)
        return
      }

      this.detailLoading = true
      this.detailError = null
      try {
        const res = await config.fetcher(rawId)
        this.detailData = res.data
        if (typeof this.afterDetailLoaded === 'function') {
          this.afterDetailLoaded(res.data)
        }
      } catch (e) {
        this.detailError = e
      } finally {
        this.detailLoading = false
      }
    },

    /**
     * 错误态重试入口
     */
    retryFetch() {
      this.loadDetail()
    }
  }
}

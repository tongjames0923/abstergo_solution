<template>
  <div class="app-container">
    <user-detail-view
      :user="detailData"
      :role-groups="roleGroups"
      :sessions="sessions"
      :tokens-map="tokensMap"
      :tokens-loading="tokensLoading"
      :loading="detailLoading"
      :error="detailError"
      @back="goBack"
      @refresh="loadDetail"
      @load-tokens="handleLoadTokens"
      @disable-token="handleDisableToken"
    />
  </div>
</template>

<script>
import UserDetailView from '@/components/UserDetailView/index.vue'
import detailPageMixin from '@/mixins/detail-page'
import { getUserDetail, getUserSessions, getTokensBySessionId, disableUserToken } from '@/api/admin_user'
import { getUserRoleGroupVOList } from '@/api/role_group'

/**
 * 用户详情页容器组件
 *
 * 职责：接入通用详情页 mixin 完成参数校验与基本信息加载，
 * 在此基础上聚合角色组与登录会话数据（并行请求），
 * 令牌明细按会话展开时惰性加载；渲染全部委托给 UserDetailView。
 */
export default {
  name: 'UserDetails',
  components: { UserDetailView },
  mixins: [detailPageMixin],
  data() {
    return {
      roleGroups: [],
      sessions: [],
      tokensMap: {},
      tokensLoading: {}
    }
  },
  computed: {
    /**
     * 详情页通用配置：参数名与路由 /user/details/:userId 一致
     */
    detailConfig() {
      return {
        paramName: 'userId',
        idPattern: /^\d+$/,
        fetcher: (id) => getUserDetail(id),
        fallbackRoute: '/common/user'
      }
    }
  },
  methods: {
    /**
     * 基本信息加载成功后，并行拉取角色组与登录会话
     */
    afterDetailLoaded(user) {
      this.roleGroups = []
      this.sessions = []
      this.tokensMap = {}
      this.tokensLoading = {}
      Promise.all([
        getUserRoleGroupVOList(user.id).catch(() => null),
        getUserSessions(user.id).catch(() => null)
      ]).then(([roleGroupRes, sessionRes]) => {
        this.roleGroups = (roleGroupRes && roleGroupRes.data) || []
        this.sessions = (sessionRes && sessionRes.data) || []
      })
    },

    /**
     * 返回用户列表页
     */
    goBack() {
      this.$router.push(this.detailConfig.fallbackRoute)
    },

    /**
     * 惰性加载指定会话的令牌明细
     */
    async handleLoadTokens(session) {
      this.$set(this.tokensLoading, session.sessionId, true)
      try {
        const res = await getTokensBySessionId(session.sessionId)
        this.$set(this.tokensMap, session.sessionId, res.data || [])
      } catch (e) {
        this.$set(this.tokensMap, session.sessionId, [])
      } finally {
        this.$set(this.tokensLoading, session.sessionId, false)
      }
    },

    /**
     * 二次确认后禁用令牌，成功后刷新该会话令牌与在线状态
     */
    handleDisableToken(session, tokenModel) {
      this.$confirm(this.$t('userDetail.confirmDisableToken'), this.$t('common.tip'), {
        confirmButtonText: this.$t('common.confirm'),
        cancelButtonText: this.$t('common.cancel'),
        type: 'warning'
      }).then(async() => {
        await disableUserToken(tokenModel.token, this.detailData.id)
        await this.handleLoadTokens(session)
        const sessionRes = await getUserSessions(this.detailData.id)
        this.sessions = sessionRes.data || []
      }).catch(() => {})
    }
  }
}
</script>

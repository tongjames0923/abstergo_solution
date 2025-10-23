<template>
  <div class="user-login-info">
    <!-- 第一层：会话列表 -->
    <div v-if="!showTokens">
      <div style="margin-bottom: 10px;">
        <el-button v-if="showTokens" size="mini" @click="backToSessions">返回会话列表</el-button>
      </div>
      <el-skeleton v-if="loadingSessions" :rows="5" animated />
      <DynamicTable
        v-else
        :data="paginatedSessions"
        :columns="sessionColumns"
        :pagination="true"
        :total="loginSessions.length"
        :page-size="sessionPageSize"
        :current-page="sessionCurrentPage"
        :page-sizes="[10, 20, 50]"
        border
        stripe
        @current-page-change="handleSessionPageChange"
        @page-size-change="handleSessionSizeChange"
        @row-click="handleSessionRowClick"
      >
        <template #actions="{ row }">
          <el-button size="mini" type="primary" @click.stop="showSessionTokens(row.sessionId)">
            查看Token
          </el-button>
        </template>
      </DynamicTable>
    </div>

    <!-- 第二层：Token列表 -->
    <div v-else>
      <div style="margin-bottom: 10px;">
        <el-button size="mini" @click="backToSessions">返回会话列表</el-button>
        <span style="margin-left: 10px;">会话ID:
          <el-tooltip :content="currentSessionId" placement="top">
            <span>{{ formatToken(currentSessionId) }}</span>
          </el-tooltip>
        </span>
      </div>
      <el-skeleton v-if="loadingTokens" :rows="5" animated />
      <DynamicTable
        v-else
        :data="paginatedTokens"
        :columns="tokenColumns"
        :pagination="true"
        :total="sessionTokens.length"
        :page-size="tokenPageSize"
        :current-page="tokenCurrentPage"
        @current-page-change="handleTokenPageChange"
        @page-size-change="handleTokenSizeChange"
        :page-sizes="[10, 20, 50]"
        border
        stripe
      >
        <template #tokenType="{ row }">
          <el-tag
            size="mini"
            :type="row.tokenType === 'ACCESS_TOKEN' ? 'success' : row.tokenType === 'REFRESH_TOKEN' ? 'warning' : 'info'"
          >
            {{
              row.tokenType === 'ACCESS_TOKEN' ? '访问令牌' : row.tokenType === 'REFRESH_TOKEN' ? '刷新令牌' : row.tokenType || '未知类型'
            }}
          </el-tag>
        </template>

        <template #status="{ row }">
          <el-tag
            size="mini"
            :type="getTokenStatusType(row.status)"
          >
            {{ getTokenStatusText(row.status) }}
          </el-tag>
        </template>

        <template #createType="{ row }">
          <el-tag
            size="mini"
            :type="row.createType === 'LOGIN' ? 'primary' : row.createType === 'REFRESH' ? 'success' : 'info'"
          >
            {{
              row.createType === 'LOGIN' ? '登录' : row.createType === 'REFRESH' ? '刷新' : row.createType || '未知'
            }}
          </el-tag>
        </template>
      </DynamicTable>
    </div>
  </div>
</template>

<script>
import DynamicTable from '@/components/DynamicTable/index.vue'
import {getTokensBySessionId, getUserSessions} from '@/api/admin_user'

export default {
  name: 'UserLoginInfoView',
  components: {
    DynamicTable
  },
  props: {
    userId: {
      type: [Number, String],
      required: true
    }
  },
  data() {
    return {
      loginSessions: [], // 会话列表
      sessionTokens: [], // Token列表
      showTokens: false, // 是否显示Token列表
      currentSessionId: '', // 当前查看的会话ID
      loadingSessions: false, // 会话数据加载状态
      loadingTokens: false, // Token数据加载状态
      // 会话分页相关
      sessionCurrentPage: 1,
      sessionPageSize: 10,
      // Token分页相关
      tokenCurrentPage: 1,
      tokenPageSize: 10
    }
  },
  computed: {
    // 会话列表分页数据
    paginatedSessions() {
      const start = (this.sessionCurrentPage - 1) * this.sessionPageSize
      const end = start + this.sessionPageSize
      return this.loginSessions.slice(start, end)
    },
    // Token列表分页数据
    paginatedTokens() {
      const start = (this.tokenCurrentPage - 1) * this.tokenPageSize
      const end = start + this.tokenPageSize
      return this.sessionTokens.slice(start, end)
    },
    // 会话列表列配置
    sessionColumns() {
      return [
        {
          prop: 'sessionId',
          label: '会话ID',
          minWidth: 180,
          showOverflowTooltip: true,
          formatter: (row) => this.formatToken(row.sessionId)
        },
        {
          prop: 'createTime',
          label: '登录时间',
          width: 160
        },
        {
          prop: 'tokenCount',
          label: '令牌数',
          width: 80
        },
        {
          prop: 'activeRefreshTokenCount',
          label: '有效刷新令牌',
          width: 120
        },
        {
          prop: 'online',
          label: '状态',
          width: 100,
          formatter: (row) => row.online ? '在线' : '离线'
        },
        {
          prop: 'lastActiveTime',
          label: '最后活动时间',
          width: 160
        }
      ]
    },
    // Token列表列配置
    tokenColumns() {
      return [
        {
          prop: 'token',
          label: 'Token',
          minWidth: 180,
          showOverflowTooltip: true,
          formatter: (row) => this.formatToken(row.token)
        },
        {
          prop: 'userName',
          label: '用户名',
          width: 120
        },
        {
          prop: 'tokenType',
          label: 'Token类型',
          width: 120,
          slot: 'tokenType'
        },
        {
          prop: 'status',
          label: '状态',
          width: 100,
          slot: 'status'
        },
        {
          prop: 'createTime',
          label: '创建时间',
          width: 160,
          formatter: (row) => row.createTime ? row.createTime.replace(' ', '\n') : ''
        },
        {
          prop: 'expireTime',
          label: '过期时间',
          width: 160,
          formatter: (row) => row.expireTime ? row.expireTime.replace(' ', '\n') : ''
        },
        {
          prop: 'ipAddr',
          label: 'IP地址',
          width: 140,
          formatter: (row) => row.ipAddr || '无'
        },
        {
          prop: 'createType',
          label: '创建类型',
          width: 100,
          slot: 'createType'
        }
      ]
    }
  },
  watch: {
    userId: {
      handler() {
        this.fetchUserSessions()
      },
      immediate: true
    },
    // 当会话数据变化时重置分页
    loginSessions() {
      this.sessionCurrentPage = 1
    },
    // 当Token数据变化时重置分页
    sessionTokens() {
      this.tokenCurrentPage = 1
    }
  },
  methods: {
    /**
     * 获取用户会话信息
     */
    async fetchUserSessions() {
      if (this.userId === undefined) return

      this.loadingSessions = true
      try {
        const { data } = await getUserSessions(this.userId)
        // 使用新的会话数据结构
        this.loginSessions = data || []
        this.showTokens = false
      } catch (error) {
        console.error('获取登录信息失败:', error)
        this.$message.error('获取登录信息失败')
      } finally {
        this.loadingSessions = false
      }
    },

    /**
     * 处理会话行点击事件
     */
    handleSessionRowClick(row) {
      // 可以在这里添加行点击事件的处理逻辑
      console.log('点击了会话行:', row)
    },

    /**
     * 处理会话分页变化
     */
    handleSessionPageChange(page) {
      this.sessionCurrentPage = page
    },

    /**
     * 处理会话分页大小变化
     */
    handleSessionSizeChange(size) {
      this.sessionPageSize = size
      this.sessionCurrentPage = 1
    },

    /**
     * 处理Token分页变化
     */
    handleTokenPageChange(page) {
      this.tokenCurrentPage = page
    },

    /**
     * 处理Token分页大小变化
     */
    handleTokenSizeChange(size) {
      this.tokenPageSize = size
      this.tokenCurrentPage = 1
    },

    /**
     * 格式化Token显示，前3后4，中间用*代替
     * @param token 完整的Token字符串
     * @returns 格式化后的Token显示字符串
     */
    formatToken(token) {
      if (!token) return ''

      // 如果Token长度小于等于7，则直接显示，不进行隐藏处理
      if (token.length <= 7) {
        return token
      }

      // 取前3位和后4位，中间用****代替
      const prefix = token.substring(0, 3)
      const suffix = token.substring(token.length - 4)
      return `${prefix}****${suffix}`
    },

    /**
     * 获取Token状态对应的标签类型
     * @param {string} status - Token状态
     * @returns {string} Element UI标签类型
     */
    getTokenStatusType(status) {
      const statusTypeMap = {
        'ACTIVE': 'success', // 有效
        'INACTIVE': 'info', // 无效
        'REVOKE': 'danger', // 已撤销
        'EXPIRED': 'warning', // 已过期
        'DELETED': 'danger' // 已删除
      }
      return statusTypeMap[status] || 'info'
    },

    /**
     * 获取Token状态对应的文本
     * @param {string} status - Token状态
     * @returns {string} 状态文本
     */
    getTokenStatusText(status) {
      const statusTextMap = {
        'ACTIVE': '有效',
        'INACTIVE': '无效',
        'REVOKE': '已撤销',
        'EXPIRED': '已过期',
        'DELETED': '已删除'
      }
      return statusTextMap[status] || '未知状态'
    },

    // 显示会话下的Token列表
    async showSessionTokens(sessionId) {
      this.loadingTokens = true
      try {
        const { data } = await getTokensBySessionId(sessionId)
        this.sessionTokens = data || []
        this.currentSessionId = sessionId
        this.showTokens = true
      } catch (error) {
        console.error('获取Token信息失败:', error)
        this.$message.error('获取Token信息失败')
      } finally {
        this.loadingTokens = false
      }
    },

    // 返回会话列表
    backToSessions() {
      this.showTokens = false
      this.sessionTokens = []
      this.currentSessionId = ''
    }
  }
}
</script>

<style scoped lang="scss">
.user-login-info {
  height: 100%;

  ::v-deep .el-table-column--selection .cell {
    padding-left: 14px;
    padding-right: 14px;
  }
}
</style>

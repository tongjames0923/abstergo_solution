<template>
  <div class="user-detail-view">
    <!-- 页头：返回 + 面包屑 + 刷新 -->
    <div class="detail-header">
      <el-button icon="el-icon-back" size="small" @click="$emit('back')">
        {{ $t('userDetail.backToList') }}
      </el-button>
      <el-breadcrumb separator="/" class="detail-breadcrumb">
        <el-breadcrumb-item>{{ $t('user.title') }}</el-breadcrumb-item>
        <el-breadcrumb-item>{{ $t('userDetail.title') }}</el-breadcrumb-item>
      </el-breadcrumb>
      <el-button
        icon="el-icon-refresh"
        size="small"
        circle
        class="detail-refresh"
        :title="$t('common.refreshData')"
        @click="$emit('refresh')"
      />
    </div>

    <!-- 加载骨架 -->
    <div v-if="loading" class="detail-skeleton">
      <el-card v-for="n in 3" :key="n" shadow="never" class="detail-card">
        <el-skeleton :rows="3" animated />
      </el-card>
    </div>

    <!-- 错误态 -->
    <el-card v-else-if="error" shadow="never" class="detail-card">
      <el-result icon="error" :title="$t('detail.loadFailed')" :sub-title="error.message || ''">
        <template slot="extra">
          <el-button type="primary" @click="$emit('refresh')">{{ $t('detail.retry') }}</el-button>
        </template>
      </el-result>
    </el-card>

    <template v-else-if="user">
      <!-- 基本信息 -->
      <el-card shadow="never" class="detail-card">
        <div slot="header" class="card-header">
          <span>{{ $t('userDetail.basicInfo') }}</span>
        </div>
        <div class="basic-info">
          <el-avatar :size="88" fit="fill" :src="user.avatar" class="basic-avatar">
            <i class="el-icon-user-solid" />
          </el-avatar>
          <el-descriptions :column="isPhone ? 1 : 3" border size="medium" class="basic-descriptions">
            <el-descriptions-item :label="$t('user.username')">{{ user.userName }}</el-descriptions-item>
            <el-descriptions-item :label="$t('user.nickname')">{{ user.nickName || '-' }}</el-descriptions-item>
            <el-descriptions-item :label="$t('user.status')">
              <el-tag :type="statusTagType(user.flag)">{{ statusText(user.flag) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item :label="$t('user.remark')" :span="2">{{ user.remark || '-' }}</el-descriptions-item>
            <el-descriptions-item :label="$t('userDetail.userId')">
              {{ user.id }}
            </el-descriptions-item>
            <el-descriptions-item :label="$t('user.createTime')">{{ user.createTime || '-' }}</el-descriptions-item>
            <el-descriptions-item :label="$t('user.updateTime')">{{ user.updateTime || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>
      </el-card>

      <!-- 角色组 -->
      <el-card shadow="never" class="detail-card">
        <div slot="header" class="card-header">
          <span>{{ $t('userDetail.roleGroups') }}</span>
        </div>
        <template v-if="roleGroups && roleGroups.length">
          <div class="role-group-list">
            <el-tag
              v-for="item in roleGroups"
              :key="item.group && item.group.id"
              effect="plain"
              class="role-group-tag"
            >
              {{ (item.group && item.group.roleGroupName) || '-' }}
            </el-tag>
          </div>
        </template>
        <el-empty v-else :description="$t('userDetail.noRoleGroups')" :image-size="80" />
      </el-card>

      <!-- 登录会话 -->
      <el-card shadow="never" class="detail-card">
        <div slot="header" class="card-header">
          <span>{{ $t('userDetail.sessions') }}</span>
        </div>
        <el-table
          v-if="sessions && sessions.length"
          :data="sessions"
          size="small"
          border
          stripe
        >
          <el-table-column type="expand">
            <template slot-scope="scope">
              <div class="token-panel">
                <template v-if="tokensMap[scope.row.sessionId]">
                  <el-table :data="tokensMap[scope.row.sessionId]" size="mini" border>
                    <el-table-column :label="$t('userDetail.token')" min-width="220" show-overflow-tooltip>
                      <template slot-scope="tokenScope">{{ tokenScope.row.token }}</template>
                    </el-table-column>
                    <el-table-column :label="$t('userDetail.tokenType')" width="120">
                      <template slot-scope="tokenScope">{{ tokenScope.row.tokenType }}</template>
                    </el-table-column>
                    <el-table-column :label="$t('userDetail.tokenStatus')" width="120">
                      <template slot-scope="tokenScope">{{ tokenScope.row.status }}</template>
                    </el-table-column>
                    <el-table-column :label="$t('user.createTime')" width="170">
                      <template slot-scope="tokenScope">{{ tokenScope.row.createTime }}</template>
                    </el-table-column>
                    <el-table-column :label="$t('userDetail.expireTime')" width="170">
                      <template slot-scope="tokenScope">{{ tokenScope.row.expireTime || '-' }}</template>
                    </el-table-column>
                    <el-table-column :label="$t('common.operation')" width="120" align="center">
                      <template slot-scope="tokenScope">
                        <el-button
                          size="mini"
                          type="danger"
                          plain
                          @click="$emit('disable-token', scope.row, tokenScope.row)"
                        >
                          {{ $t('userDetail.disableToken') }}
                        </el-button>
                      </template>
                    </el-table-column>
                  </el-table>
                </template>
                <el-button
                  v-else
                  size="mini"
                  type="primary"
                  plain
                  :loading="tokensLoading[scope.row.sessionId]"
                  icon="el-icon-key"
                  @click="$emit('load-tokens', scope.row)"
                >
                  {{ $t('userDetail.loadTokens') }}
                </el-button>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="sessionId" :label="$t('userDetail.sessionId')" min-width="220" show-overflow-tooltip />
          <el-table-column :label="$t('user.status')" width="100" align="center">
            <template slot-scope="scope">
              <el-tag :type="scope.row.online ? 'success' : 'info'" size="small">
                {{ scope.row.online ? $t('userDetail.online') : $t('userDetail.offline') }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="tokenCount" :label="$t('userDetail.tokenCount')" width="100" align="center" />
          <el-table-column prop="createTime" :label="$t('userDetail.loginTime')" width="170" />
          <el-table-column prop="lastActiveTime" :label="$t('userDetail.lastActiveTime')" width="170" />
        </el-table>
        <el-empty v-else :description="$t('userDetail.noSessions')" :image-size="80" />
      </el-card>
    </template>
  </div>
</template>

<script>
import ResponsiveMixin from '@/mixins/responsive'

/**
 * 用户详情纯展示组件
 *
 * 只负责渲染（基本信息卡片、角色组标签、会话/令牌表格），
 * 数据获取与事件处理全部上抛给容器组件（views/user/details/index.vue）。
 */
export default {
  name: 'UserDetailView',
  // 设备档位由响应式契约 mixin 提供：手机上描述列表改为单列，避免三列挤压
  mixins: [ResponsiveMixin],
  props: {
    /* 用户基本信息（SystemUserEntity） */
    user: {
      type: Object,
      default: null
    },
    /* 角色组列表（SystemRoleGroupVO[]） */
    roleGroups: {
      type: Array,
      default: () => []
    },
    /* 登录会话列表（LoginSessionVO[]） */
    sessions: {
      type: Array,
      default: () => []
    },
    /* 会话令牌映射表 { sessionId: ITokenModel[] } */
    tokensMap: {
      type: Object,
      default: () => ({})
    },
    /* 会话令牌加载中状态 { sessionId: boolean } */
    tokensLoading: {
      type: Object,
      default: () => ({})
    },
    /* 基本信息加载中 */
    loading: {
      type: Boolean,
      default: false
    },
    /* 基本信息加载错误 */
    error: {
      type: Error,
      default: null
    }
  },
  methods: {
    /**
     * 用户状态文案（与列表页 flag 语义一致）
     */
    statusText(flag) {
      if (flag === 1) return this.$t('common.normal')
      if (flag === 0) return this.$t('user.disable')
      if (flag === 2) return this.$t('user.lock')
      return this.$t('common.info')
    },
    /**
     * 用户状态标签颜色（与列表页一致的三色语义）
     */
    statusTagType(flag) {
      if (flag === 1) return 'success'
      if (flag === 0) return 'danger'
      if (flag === 2) return 'warning'
      return 'info'
    }
  }
}
</script>

<style lang="scss" scoped>
.user-detail-view {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);
}

.detail-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;

  .detail-breadcrumb {
    margin-left: 16px;
  }

  .detail-refresh {
    margin-left: auto;
  }
}

.detail-card {
  margin-bottom: 16px;

  .card-header {
    font-weight: 600;
    color: #303133;
  }
}

.detail-skeleton {
  .detail-card {
    margin-bottom: 16px;
  }
}

.basic-info {
  display: flex;
  align-items: flex-start;

  .basic-avatar {
    flex-shrink: 0;
    margin-right: 24px;
    background-color: #409eff;
    font-size: 40px;
    color: #ffffff;
  }

  .basic-descriptions {
    flex: 1;
  }

  /* 手机：头像与描述列表上下堆叠，避免头像挤占描述区宽度 */
  @media (max-width: 768px) {
    flex-direction: column;
    align-items: center;

    .basic-avatar {
      margin-right: 0;
      margin-bottom: 16px;
    }

    .basic-descriptions {
      width: 100%;
    }
  }
}

.role-group-list {
  .role-group-tag {
    margin-right: 12px;
    margin-bottom: 8px;
  }
}

.token-panel {
  padding: 8px 16px;
}
</style>

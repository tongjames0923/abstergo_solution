<template>
  <div class="user-role-admin">
    <div class="role-content">
      <div class="assigned-roles-section">
        <div class="section-title">
          <h4>已分配角色</h4>
          <span class="role-count">({{ userRoles.length }})</span>
        </div>
        <div class="role-tags">
          <el-tag
            v-for="role in userRoles"
            :key="role.id"
            :type="getRoleType(role.role)"
            class="role-tag"
            closable
            @close="disableRole(role.id)"
          >
            {{ role.role }}
          </el-tag>
          <div v-if="userRoles.length === 0" class="no-roles">
            <i class="el-icon-info" />
            该用户暂无角色
          </div>
        </div>
      </div>

      <div class="available-roles-section">
        <div class="section-header">
          <div class="section-title">
            <h4>可分配角色</h4>
            <span class="role-count">({{ totalRoles }})</span>
          </div>
          <div class="search-box">
            <el-input
              v-model="searchKeyword"
              clearable
              placeholder="搜索角色名称"
              prefix-icon="el-icon-search"
              @clear="handleSearch"
              @keyup.enter.native="handleSearch"
            />
          </div>
        </div>

        <!-- 使用CardList组件展示角色 -->
        <CardList
          :columns="2"
          :current-page="currentPage"
          :data="paginatedRoles"
          :gap="15"
          :loading="loading"
          :max-height="300"
          :page-size="pageSize"
          :pagination="true"
          :scroll-y="true"
          :scrollbar-visible="false"
          :selectable="false"
          :total="totalRoles"
          class="role-card-list"
          @current-change="handlePageChange"
        >
          <!-- 自定义卡片内容 -->
          <template #card="{ item: role }">
            <el-card
              :class="{ 'role-card-selected': hasRole(role.id) }"
              class="role-card-item"
              shadow="hover"
            >
              <div class="role-content">
                <div class="role-info">
                  <div class="role-name">{{ role.role }}</div>
                  <div class="role-description">{{ role.description || '暂无描述' }}</div>
                </div>
                <div class="role-action">
                  <el-button
                    v-if="!hasRole(role.id)"
                    icon="el-icon-circle-plus"
                    size="small"
                    type="primary"
                    @click.stop="enableRole(role.id)"
                  >
                    分配
                  </el-button>
                  <el-button
                    v-else
                    icon="el-icon-remove"
                    size="small"
                    type="danger"
                    @click.stop="disableRole(role.id)"
                  >
                    取消
                  </el-button>
                </div>
              </div>
            </el-card>
          </template>

          <!-- 自定义空状态 -->
          <template #empty>
            <div class="empty-role-state">
              <el-empty :image-size="80" description="暂无角色数据">
                <el-button
                  v-if="!searchKeyword"
                  icon="el-icon-refresh"
                  type="primary"
                  @click="fetchAllRoles"
                >
                  重新加载
                </el-button>
              </el-empty>
            </div>
          </template>
        </CardList>
      </div>
    </div>
  </div>
</template>

<script>
import CardList from '@/components/CardList'
import { disableRoleForUser, enableRoleForUser, getRolesByUserIdAdmin, queryRoles } from '@/api/admin_user'

export default {
  name: 'UserRoleAdminView',
  components: {
    CardList
  },
  props: {
    userId: {
      type: [Number, String],
      required: true
    }
  },
  data() {
    return {
      userRoles: [],
      availableRoles: [],
      loading: false,
      searchKeyword: '',
      currentPage: 1,
      pageSize: 6,
      totalRoles: 0
    }
  },
  computed: {
    filteredRoles() {
      // 如果有搜索关键词，则返回匹配的角色
      if (this.searchKeyword) {
        return this.availableRoles.filter(role =>
          role.role.toLowerCase().includes(this.searchKeyword.toLowerCase())
        )
      }
      return this.availableRoles
    },
    paginatedRoles() {
      // 实现分页逻辑
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return this.filteredRoles.slice(start, end)
    },
    loadingCount() {
      // 根据当前页数据量来确定加载占位符数量
      return Math.min(this.paginatedRoles.length || this.pageSize, this.pageSize)
    }
  },
  watch: {
    userId: {
      handler() {
        this.fetchUserRoles()
        this.fetchAllRoles()
      },
      immediate: true
    }
  },
  methods: {
    // 获取用户角色
    async fetchUserRoles() {
      if (this.userId === undefined) return

      this.loading = true
      try {
        const response = await getRolesByUserIdAdmin(this.userId)
        this.userRoles = response.data || []
      } catch (error) {
        console.error('获取用户角色失败:', error)
        // 消息提示由后端统一控制
      } finally {
        this.loading = false
      }
    },

    // 获取所有可用角色
    async fetchAllRoles() {
      this.loading = true
      try {
        const response = await queryRoles({
          roleLike: this.searchKeyword || undefined,
          flagEquals: [1, 2], // 只获取启用的角色
          pageNum: this.currentPage,
          pageSize: this.pageSize
        })
        this.availableRoles = response.data?.data || []
        this.totalRoles = response.data?.total || 0
      } catch (error) {
        console.error('获取角色列表失败:', error)
        // 消息提示由后端统一控制
      } finally {
        this.loading = false
      }
    },

    // 处理搜索
    handleSearch() {
      this.currentPage = 1
      this.fetchAllRoles()
    },

    // 处理分页变化
    handlePageChange(page) {
      this.currentPage = page
      this.fetchAllRoles()
    },

    // 启用角色
    async enableRole(roleId) {
      try {
        await enableRoleForUser(this.userId, roleId)
        // 更新本地数据
        const role = this.availableRoles.find(r => r.id === roleId)
        if (role) {
          this.userRoles.push({ ...role })
        }
        // 消息提示由后端统一控制
      } catch (error) {
        console.error('启用角色失败:', error)
        // 消息提示由后端统一控制
      }
    },

    // 禁用角色
    async disableRole(roleId) {
      try {
        await disableRoleForUser(this.userId, roleId)
        // 更新本地数据
        this.userRoles = this.userRoles.filter(role => role.id !== roleId)
        // 消息提示由后端统一控制
      } catch (error) {
        console.error('禁用角色失败:', error)
        // 消息提示由后端统一控制
      }
    },

    // 判断角色是否已分配给用户
    hasRole(roleId) {
      return this.userRoles.some(role => role.id === roleId)
    },

    // 获取角色标签类型
    getRoleType(roleName) {
      const typeMap = {
        '管理员': 'danger',
        '超级管理员': 'danger',
        '管理员操作': 'warning',
        '常规用户': 'success'
      }
      return typeMap[roleName] || 'info'
    }
  }
}
</script>

<style scoped lang="scss">
.user-role-admin {
  height: 100%;
  display: flex;
  flex-direction: column;

  .role-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 15px 20px;
    border-bottom: 1px solid #ebeef5;

    h3 {
      margin: 0;
      font-size: 18px;
      font-weight: 600;
      color: #303133;
    }

    .close-btn {
      font-size: 20px;
      color: #909399;

      &:hover {
        color: #409eff;
      }
    }
  }

  .role-content {
    flex: 1;
    padding: 20px;
    overflow: auto;

    .assigned-roles-section {
      margin-bottom: 25px;

      .section-title {
        display: flex;
        align-items: center;
        margin-bottom: 15px;

        h4 {
          margin: 0;
          font-size: 16px;
          font-weight: 600;
          color: #333;
        }

        .role-count {
          margin-left: 8px;
          font-size: 14px;
          color: #999;
        }
      }

      .role-tags {
        display: flex;
        flex-wrap: wrap;
        gap: 10px;

        .role-tag {
          margin: 0;
          font-size: 13px;
          padding: 5px 10px;
        }

        .no-roles {
          color: #999;
          font-style: italic;
          display: flex;
          align-items: center;
          gap: 5px;
        }
      }
    }

    .available-roles-section {
      .section-header {
        display: flex;
        justify-content: space-between;
        align-items: center;
        margin-bottom: 15px;

        .section-title {
          display: flex;
          align-items: center;

          h4 {
            margin: 0;
            font-size: 16px;
            font-weight: 600;
            color: #333;
          }

          .role-count {
            margin-left: 8px;
            font-size: 14px;
            color: #999;
          }
        }

        .search-box {
          width: 250px;
        }
      }

      .role-card-list {
        border: 1px solid #ebeef5;
        border-radius: 6px;
        overflow: hidden;

        ::v-deep .card-list-scroll-container {
          &::-webkit-scrollbar {
            width: 8px;
          }

          &::-webkit-scrollbar-thumb {
            background-color: rgba(144, 147, 153, 0.3);
            border-radius: 4px;

            &:hover {
              background-color: rgba(144, 147, 153, 0.5);
            }
          }
        }

        ::v-deep .card-list {
          padding: 15px;
        }

        ::v-deep .pagination-container {
          margin: 0;
          padding: 15px;
          border-top: 1px solid #ebeef5;
        }
      }

      .role-card-item {
        border: 1px solid #ebeef5;
        transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
        height: 100%;

        &:hover {
          transform: translateY(-2px);
          box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
        }

        &.role-card-selected {
          border-color: #409eff;
          box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
        }

        .role-content {
          display: flex;
          flex-direction: column;
          height: 100%;

          .role-info {
            flex: 1;

            .role-name {
              font-weight: bold;
              margin-bottom: 8px;
              font-size: 15px;
              color: #333;
            }

            .role-description {
              font-size: 13px;
              color: #999;
              line-height: 1.4;
              display: -webkit-box;
              -webkit-line-clamp: 2;
              -webkit-box-orient: vertical;
              overflow: hidden;
            }
          }

          .role-action {
            margin-top: 15px;
            padding-top: 15px;
            border-top: 1px solid #eee;

            ::v-deep .el-button {
              width: 100%;
            }
          }
        }
      }

      .empty-role-state {
        padding: 30px 0;
      }
    }
  }
}
</style>

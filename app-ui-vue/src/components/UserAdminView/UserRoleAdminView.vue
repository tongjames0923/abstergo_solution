<template>
  <div class="user-role-admin">
    <el-card class="role-card">
      <div slot="header" class="clearfix">
        <span>用户角色管理</span>
      </div>

      <el-skeleton :loading="loading" animated>
        <el-row :gutter="20">
          <el-col :span="24">
            <div class="role-section">
              <h4>已分配角色</h4>
              <div class="role-tags">
                <el-tag
                  v-for="role in userRoles"
                  :key="role.id"
                  closable
                  type="success"
                  @close="disableRole(role.role)"
                  class="role-tag"
                >
                  {{ role.role }}
                </el-tag>
                <div v-if="userRoles.length === 0" class="no-roles">
                  该用户暂无角色
                </div>
              </div>
            </div>
          </el-col>

          <el-col :span="24">
            <div class="role-section">
              <div class="section-header">
                <h4>可分配角色</h4>
                <div class="search-box">
                  <el-input
                    v-model="searchKeyword"
                    placeholder="搜索角色名称"
                    clearable
                    @clear="handleSearch"
                    @keyup.enter.native="handleSearch"
                  >
                    <el-button slot="append" icon="el-icon-search" @click="handleSearch"></el-button>
                  </el-input>
                </div>
              </div>

              <div class="available-roles">
                <el-row :gutter="10">
                  <el-col
                    v-for="role in filteredRoles"
                    :key="role.id"
                    :span="12"
                    class="role-item"
                  >
                    <el-card
                      :class="{ 'role-card-selected': hasRole(role.role) }"
                      shadow="hover">
                      <div class="role-content">
                        <div class="role-info">
                          <div class="role-name">{{ role.role }}</div>
                          <div class="role-description">{{ role.description || '暂无描述' }}</div>
                        </div>
                        <div class="role-action">
                          <el-button
                            v-if="!hasRole(role.role)"
                            size="small"
                            type="primary"
                            @click="enableRole(role.role)"
                          >
                            分配
                          </el-button>
                          <el-button
                            v-else
                            size="small"
                            type="danger"
                            @click="disableRole(role.role)"
                          >
                            取消
                          </el-button>
                        </div>
                      </div>
                    </el-card>
                  </el-col>
                </el-row>

                <!-- 分页 -->
                <div class="pagination-container" v-if="totalRoles > pageSize">
                  <el-pagination
                    @current-change="handlePageChange"
                    :current-page="currentPage"
                    :page-size="pageSize"
                    :total="totalRoles"
                    layout="prev, pager, next"
                    background
                  >
                  </el-pagination>
                </div>
              </div>
            </div>
          </el-col>
        </el-row>
      </el-skeleton>
    </el-card>
  </div>
</template>

<script>
import {disableRoleForUser, enableRoleForUser, getRolesByUserIdAdmin, queryRoles} from '@/api/admin_user'

export default {
  name: 'UserRoleAdminView',
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
      pageSize: 10,
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
      // 否则返回当前页的角色
      const start = (this.currentPage - 1) * this.pageSize
      const end = start + this.pageSize
      return this.availableRoles.slice(start, end)
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
        this.$message.error('获取用户角色失败')
      } finally {
        this.loading = false
      }
    },

    // 获取所有可用角色
    async fetchAllRoles() {
      try {
        const response = await queryRoles({
          roleEquals: this.searchKeyword || undefined,
          flagEquals: [1, 2] // 只获取启用的角色
        })
        this.availableRoles = response.data?.data || []
        this.totalRoles = response.data?.total || 0
      } catch (error) {
        console.error('获取角色列表失败:', error)
        this.$message.error('获取角色列表失败')
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
      // 如果没有搜索关键词，我们需要重新获取数据以支持分页
      if (!this.searchKeyword) {
        this.fetchAllRoles()
      }
    },

    // 启用角色
    async enableRole(roleName) {
      try {
        await enableRoleForUser(this.userId, roleName)
        // 更新本地数据
        const role = this.availableRoles.find(r => r.role === roleName)
        if (role) {
          this.userRoles.push(role)
        }
        this.$message.success('角色分配成功')
      } catch (error) {
        console.error('启用角色失败:', error)
        this.$message.error('角色分配失败')
      }
    },

    // 禁用角色
    async disableRole(roleName) {
      try {
        await disableRoleForUser(this.userId, roleName)
        // 更新本地数据
        this.userRoles = this.userRoles.filter(role => role.role !== roleName)
        this.$message.success('角色取消成功')
      } catch (error) {
        console.error('禁用角色失败:', error)
        this.$message.error('角色取消失败')
      }
    },

    // 判断角色是否已分配给用户
    hasRole(roleName) {
      return this.userRoles.some(role => role.role === roleName)
    }
  }
}
</script>

<style scoped lang="scss">
.user-role-admin {
  .role-card {
    min-height: 300px;
  }

  .role-section {
    margin-bottom: 20px;

    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 15px;

      h4 {
        margin: 0;
        color: #333;
      }

      .search-box {
        width: 300px;
      }
    }
  }

  .role-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
  }

  .role-tag {
    margin-bottom: 5px;
  }

  .no-roles {
    color: #999;
    font-style: italic;
  }

  .available-roles {
    .role-item {
      margin-bottom: 15px;

      .el-card {
        border: 1px solid #ebeef5;
        transition: all 0.3s;

        &.role-card-selected {
          border-color: #409eff;
          box-shadow: 0 2px 12px 0 rgba(64, 158, 255, 0.2);
        }

        .role-content {
          display: flex;
          justify-content: space-between;
          align-items: center;

          .role-info {
            .role-name {
              font-weight: bold;
              margin-bottom: 5px;
            }

            .role-description {
              font-size: 12px;
              color: #999;
            }
          }
        }
      }
    }

    .pagination-container {
      display: flex;
      justify-content: center;
      margin-top: 20px;
    }
  }
}
</style>

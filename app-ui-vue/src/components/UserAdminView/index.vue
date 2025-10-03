<template>
  <div class="user-management">
    <quick-use-management-view
      ref="userManagement"
      main-title="用户管理"
      :config-name="userSearchFormKey()"
      :search-handler="searchHandler"
      :columns="columns"
      :add-form-config-name="userAddFormKey()"
      :add-handler="(formData) => saveUser(formData)"
      :edit-form-config-name="userEditFormKey()"
      :edit-handler="(formData) => saveUser(formData)"
      :query-transformer="rangeQueryTransform"
      @add-success="(data)=>$message.success(' 新增用户成功！ ')"
      @edit-success="(data)=>$message.success(' 修改用户成功！ ')"
    >
      <template #actions-dropdown="{ row, index }">
        <el-dropdown-item command="toggleStatus" @click.native="handleToggleStatus(row)">
          {{ row.flag === 1 ? '禁用' : '启用' }}
        </el-dropdown-item>
        <el-dropdown-item command="showLoginInfo" @click.native="handleShowLoginInfo(row)">
          登录信息
        </el-dropdown-item>
        <el-dropdown-item command="resetPassword" @click.native="handleResetPassword(row)">
          重置密码
        </el-dropdown-item>
        <el-dropdown-item command="manageRoles" @click.native="handleManageRoles(row)">
          权限管理
        </el-dropdown-item>
      </template>
    </quick-use-management-view>

    <!-- 重置密码弹窗 -->
    <el-dialog
      title="重置密码"
      :visible.sync="resetPasswordDialogVisible"
      width="450px"
      :modal-append-to-body="false"
      :close-on-click-modal="false"
      class="password-dialog"
      @close="handleResetPasswordDialogClose"
    >
      <el-form ref="passwordForm" :model="passwordForm" :rules="passwordRules" label-width="80px">
        <el-form-item label="用户名" prop="userName">
          <el-input
            v-model="passwordForm.userName"
            disabled
            prefix-icon="el-icon-user"
          />
        </el-form-item>
        <el-form-item label="新密码" prop="password">
          <el-input
            v-model="passwordForm.password"
            type="password"
            show-password
            prefix-icon="el-icon-lock"
            placeholder="请输入新密码"
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            show-password
            prefix-icon="el-icon-lock"
            placeholder="请再次输入密码"
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="resetPasswordDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handlePasswordSubmit">确 定</el-button>
      </div>
    </el-dialog>

    <!-- 登录信息弹窗 -->
    <el-dialog
      title="用户登录信息"
      :visible.sync="loginInfoDialogVisible"
      width="80%"
      :modal-append-to-body="false"
      :close-on-click-modal="false"
      class="login-info-dialog"
      @close="handleLoginInfoDialogClose"
    >
      <!-- 第一层：会话列表 -->
      <div v-if="!showTokens">
        <div style="margin-bottom: 10px;">
          <el-button v-if="showTokens" size="mini" @click="backToSessions">返回会话列表</el-button>
        </div>
        <DynamicTable
          :data="loginSessions"
          :columns="sessionColumns"
          :pagination="false"
          :border="true"
          :stripe="true"
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
        <DynamicTable
          :data="sessionTokens"
          :columns="tokenColumns"
          :pagination="false"
          :border="true"
          :stripe="true"
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
              :type="row.status === 'ACTIVE' ? 'success' : row.status === 'INACTIVE' ? 'info' : row.status === 'REVOKE' ? 'danger' : row.status === 'EXPIRED' ? 'warning' : 'info'"
            >
              {{
                row.status === 'ACTIVE' ? '活跃' : row.status === 'INACTIVE' ? '无效' : row.status === 'REVOKE' ? '已撤销' : row.status === 'EXPIRED' ? '已过期' : row.status || '未知状态'
              }}
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
      <div slot="footer" class="dialog-footer">
        <el-button @click="loginInfoDialogVisible = false">关 闭</el-button>
      </div>
    </el-dialog>

    <!-- 权限管理弹窗 -->
    <el-dialog
      title="权限管理"
      :visible.sync="roleManagementDialogVisible"
      width="800px"
      :modal-append-to-body="false"
      :close-on-click-modal="false"
      class="role-dialog"
      @close="handleRoleManagementDialogClose"
    >
      <UserRoleAdminView
        v-if="roleManagementDialogVisible"
        :user-id="currentUserId"
      />
    </el-dialog>
  </div>
</template>

<script>
import QuickUseManagementView from '@/components/QuickUseManagementView/index.vue'
import UserRoleAdminView from '@/components/UserAdminView/UserRoleAdminView.vue'
import DynamicTable from '@/components/DynamicTable/index.vue'
import {
  activateUser,
  adminQueryUser,
  deleteUser,
  disableUser,
  getTokensBySessionId,
  getUserSessions,
  saveUser
} from '@/api/admin_user'
import { rangeQueryTransform } from '@/utils/query-help'
import { userAddFormKey, userEditFormKey, userSearchFormKey } from '@/settings'

export default {
  name: 'UserAdminView',
  components: {
    QuickUseManagementView,
    UserRoleAdminView,
    DynamicTable
  },
  data() {
    // 确认密码验证规则
    const validateConfirmPassword = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请再次输入密码'))
      } else if (value !== this.passwordForm.password) {
        callback(new Error('两次输入密码不一致!'))
      } else {
        callback()
      }
    }

    return {
      resetPasswordDialogVisible: false,
      roleManagementDialogVisible: false,
      currentUserId: null,
      passwordForm: {
        id: undefined,
        userName: '',
        password: '',
        confirmPassword: ''
      },
      loginInfoDialogVisible: false,
      loginSessions: [], // 会话列表
      sessionTokens: [], // Token列表
      showTokens: false, // 是否显示Token列表
      currentSessionId: '', // 当前查看的会话ID
      passwordRules: {
        password: [
          { required: true, message: '请输入新密码', trigger: 'blur' },
          { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
        ],
        confirmPassword: [
          { required: true, validator: validateConfirmPassword, trigger: 'blur' }
        ]
      },
      // 会话列表列配置
      sessionColumns: [
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
      ],
      // Token列表列配置
      tokenColumns: [
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
      ],
      columns: [
        { prop: 'id', label: 'ID', width: 80 },
        {
          prop: 'avatar',
          label: '头像',
          width: 120,
          render: (h, scope) => {
            return <el-avatar src={scope.row.avatar}></el-avatar>
          }
        },
        { prop: 'userName', label: '用户名', width: 180 },
        { prop: 'nickName', label: '昵称', width: 180 },
        { prop: 'remark', label: '备注', minWidth: 200 },
        {
          prop: 'flag',
          label: '状态',
          width: 100,
          render: (h, scope) => {
            if (scope.row.flag === 1) {
              return <el-tag type='success'>正常</el-tag>
            } else if (scope.row.flag === 0) {
              return <el-tag type='danger'>禁用</el-tag>
            } else if (scope.row.flag === 2) {
              return <el-tag type='warning'>锁定</el-tag>
            } else {
              return <el-tag>未知</el-tag>
            }
          }
        },
        { prop: 'createTime', label: '创建时间', width: 180, sortable: true },
        { prop: 'updateTime', label: '更新时间', width: 180, sortable: true }
      ]
    }
  },
  methods: {
    /**
     * 处理会话行点击事件
     */
    handleSessionRowClick(row) {
      // 可以在这里添加行点击事件的处理逻辑
      console.log('点击了会话行:', row)
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

    userEditFormKey() {
      return userEditFormKey
    },
    userAddFormKey() {
      return userAddFormKey
    },
    userSearchFormKey() {
      return userSearchFormKey
    },

    async handleShowLoginInfo(row) {
      try {
        const { data } = await getUserSessions(row.id)
        // 使用新的会话数据结构
        this.loginSessions = data || []
        this.showTokens = false
        this.loginInfoDialogVisible = true
      } catch (error) {
        console.error('获取登录信息失败:', error)
        this.$message.error('获取登录信息失败')
      }
    },

    handleLoginInfoDialogClose() {
      this.loginSessions = []
      this.sessionTokens = []
      this.showTokens = false
      this.currentSessionId = ''
      this.loginInfoDialogVisible = false
    },

    // 显示会话下的Token列表
    async showSessionTokens(sessionId) {
      try {
        const { data } = await getTokensBySessionId(sessionId)
        this.sessionTokens = data || []
        this.currentSessionId = sessionId
        this.showTokens = true
      } catch (error) {
        console.error('获取Token信息失败:', error)
        this.$message.error('获取Token信息失败')
      }
    },

    // 返回会话列表
    backToSessions() {
      this.showTokens = false
      this.sessionTokens = []
      this.currentSessionId = ''
    },

    saveUser,
    deleteUser,
    activateUser,
    disableUser,
    rangeQueryTransform,
    // 简化后的搜索处理函数，适配AbstractCrudController的数据格式
    async searchHandler(bodyParams, requestParams) {
      // 返回符合组件要求的数据格式
      return await adminQueryUser(
        bodyParams, // 查询条件和排序作为body
        requestParams.pageNum, // 分页参数作为查询参数
        requestParams.pageSize
      )
    },
    /**
     * 切换用户状态
     */
    async handleToggleStatus(row) {
      try {
        let response
        if (row.flag === 1) {
          // 当前是正常状态，点击后应该禁用
          await this.$confirm('确认要禁用该用户吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          })
          response = await disableUser(row.id)
        } else {
          // 当前是禁用或锁定状态，点击后应该激活
          await this.$confirm('确认要激活该用户吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          })
          response = await activateUser(row.id)
        }

        if (response.code === 200) {
          this.$message.success(response.message || '操作成功')
          // 刷新数据
          this.$refs.userManagement.refresh()
        } else {
          this.$message.error(response.message || '操作失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('操作失败:', error)
          this.$message.error('操作失败')
        }
      }
    },

    /**
     * 重置密码按钮点击事件
     */
    handleResetPassword(row) {
      this.passwordForm = {
        id: row.id,
        userName: row.userName,
        password: '',
        confirmPassword: ''
      }
      this.resetPasswordDialogVisible = true
    },

    /**
     * 处理密码重置提交事件
     */
    async handlePasswordSubmit() {
      this.$refs.passwordForm.validate(async(valid) => {
        if (valid) {
          try {
            const response = await saveUser(this.passwordForm)
            if (response.code === 200) {
              this.$message.success('密码重置成功')
              this.resetPasswordDialogVisible = false
            } else {
              this.$message.error(response.message || '密码重置失败')
            }
          } catch (error) {
            console.error('密码重置失败:', error)
            this.$message.error('密码重置失败')
          }
        }
      })
    },

    /**
     * 处理重置密码弹窗关闭事件
     */
    handleResetPasswordDialogClose() {
      this.$refs.passwordForm.resetFields()
    },

    /**
     * 处理权限管理按钮点击事件
     */
    handleManageRoles(row) {
      this.currentUserId = row.id
      this.roleManagementDialogVisible = true
    },

    /**
     * 处理权限管理弹窗关闭事件
     */
    handleRoleManagementDialogClose() {
      this.currentUserId = null
      this.roleManagementDialogVisible = false
    }
  }
}
</script>

<style lang="scss" scoped>
.login-info-dialog {
  ::v-deep .el-dialog__header {
    background-color: #f8f9fa;
    border-bottom: 1px solid #ebeef5;
    padding: 15px 20px;
    border-radius: 8px 8px 0 0 !important;
  }

  ::v-deep .el-dialog__title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }

  ::v-deep .el-table-column--selection .cell {
    padding-left: 14px;
    padding-right: 14px;
  }
}

.user-management {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);

  .password-dialog,
  .role-dialog {
    ::v-deep .el-dialog__header {
      background-color: #f8f9fa;
      border-bottom: 1px solid #ebeef5;
      padding: 15px 20px;
      border-radius: 8px 8px 0 0 !important;
    }

    ::v-deep .el-dialog__title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }
  }

  ::v-deep .el-dialog__footer {
    border-top: 1px solid #ebeef5;
    padding: 15px 20px;
  }
}
</style>

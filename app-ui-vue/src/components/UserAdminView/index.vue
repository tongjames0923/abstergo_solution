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
      @add-success="(data)=>$message.success(' 新增用户成功！ ')"
      edit-form-config-name="userEditForm"
      :edit-handler="(formData) => saveUser(formData)"
      :query-transformer="rangeQueryTransform"
    >
      <template #actions-dropdown="{ row, index }">
        <el-dropdown-item command="toggleStatus" @click.native="handleToggleStatus(row)">
          {{ row.flag === 1 ? '禁用' : '启用' }}
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

    <!-- 权限管理弹窗 -->
    <el-dialog
      title="权限管理"
      :visible.sync="roleManagementDialogVisible"
      width="800px"
      :modal-append-to-body="false"
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
import {activateUser, adminQueryUser, deleteUser, disableUser, saveUser} from '@/api/admin_user'
import {rangeQueryTransform} from "@/utils/query-help";
import {userAddFormKey, userSearchFormKey} from "@/settings";

export default {
  name: 'UserAdminView',
  components: {
    QuickUseManagementView,
    UserRoleAdminView
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
      passwordRules: {
        password: [
          {required: true, message: '请输入新密码', trigger: 'blur'},
          {min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur'}
        ],
        confirmPassword: [
          {required: true, validator: validateConfirmPassword, trigger: 'blur'}
        ]
      },
      columns: [
        {prop: 'id', label: 'ID', width: 80},
        {
          prop: 'avatar',
          label: '头像',
          width: 120,
          render: (h, scope) => {
            return <el-avatar src={scope.row.avatar}></el-avatar>
          }
        },
        {prop: 'userName', label: '用户名', width: 180},
        {prop: 'nickName', label: '昵称', width: 180},
        {prop: 'remark', label: '备注', minWidth: 200},
        {
          prop: 'flag',
          label: '状态',
          width: 100,
          render: (h, scope) => {
            if (scope.row.flag === 1)
              return <el-tag type="success">正常</el-tag>
            else if (scope.row.flag === 0)
              return <el-tag type="danger">禁用</el-tag>
            else if (scope.row.flag === 2)
              return <el-tag type="warning">锁定</el-tag>
            else
              return <el-tag>未知</el-tag>
          }
        },
        {prop: 'createTime', label: '创建时间', width: 180, sortable: true},
        {prop: 'updateTime', label: '更新时间', width: 180, sortable: true}
      ]
    }
  },
  methods: {
    userAddFormKey() {
      return userAddFormKey
    },
    userSearchFormKey() {
      return userSearchFormKey
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
    handlePasswordSubmit() {
      this.$refs.passwordForm.validate(async (valid) => {
        if (valid) {
          try {
            // 构造用户数据，只更新密码
            const userData = {
              id: this.passwordForm.id,
              userName: this.passwordForm.userName,
              password: this.passwordForm.password
            }

            // 调用后端保存接口
            const response = await saveUser(userData)

            if (response.code === 200) {
              this.$message.success('密码重置成功')
              this.resetPasswordDialogVisible = false

              // 刷新数据
              this.$refs.userManagement.refresh()
            } else {
              this.$message.error(response.message || '密码重置失败')
            }
          } catch (error) {
            console.error('密码重置失败:', error)
            this.$message.error('密码重置失败')
          }
        } else {
          console.log('表单验证失败')
          this.$message.warning('请填写正确的表单信息')
          return false
        }
      })
    },

    /**
     * 处理重置密码弹窗关闭事件
     */
    handleResetPasswordDialogClose() {
      this.$refs.passwordForm.resetFields()
      this.passwordForm = {
        id: undefined,
        userName: '',
        password: '',
        confirmPassword: ''
      }
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
      this.currentUserId = undefined
    }
  }
}
</script>

<style lang="scss" scoped>
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

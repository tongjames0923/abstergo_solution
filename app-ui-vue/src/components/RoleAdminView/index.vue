<template>
  <div class="role-management">
    <quick-use-management-view
      ref="roleManagement"
      main-title="权限管理"
      :config-name="roleSearchFormKey()"
      :search-handler="searchHandler"
      :columns="columns"
      :add-form-config-name="roleAddFormKey()"
      :add-handler="handleSaveRole"
      :delete-handler="handleDeleteRole"
      :delete-confirm-text="deleteConfirmText"
      :editable="(row) => row.flag !== 2"
      :deleteable="(row) => row.flag !== 2"
      @add-success="() => {}"
      @edit-success="() => {}"
      @delete-success="() => {}"
    >
      <template #actions-dropdown="{ row, index }">
        <el-dropdown-item
          v-if="row.flag !== 2"
          command="toggleStatus"
          @click.native="handleToggleStatus(row)"
        >
          {{ row.flag === 1 ? '禁用' : '启用' }}
        </el-dropdown-item>
      </template>
    </quick-use-management-view>
  </div>
</template>

<script>
import QuickUseManagementView from '@/components/QuickUseManagementView/index.vue'
import {activateRole, deleteRole, disableRole, queryRoles, saveRole} from '@/api/role'
import {roleAddFormKey, roleSearchFormKey} from '@/settings'

export default {
  name: 'RoleAdminView',
  components: { QuickUseManagementView },
  data() {
    return {
      columns: [
        { prop: 'id', label: 'ID', width: 80 },
        { prop: 'role', label: '权限名称', width: 150 },
        { prop: 'description', label: '权限描述', minWidth: 200 },
        {
          prop: 'flag',
          label: '状态',
          width: 100,
          render: (h, scope) => {
            if (scope.row.flag === 1) {
              return <el-tag type='success'>激活</el-tag>
            } else if (scope.row.flag === 0) {
              return <el-tag type='danger'>失效</el-tag>
            } else if (scope.row.flag === 2) {
              return <el-tag type='warning'>系统</el-tag>
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
    roleSearchFormKey() {
      return roleSearchFormKey
    },
    roleAddFormKey() {
      return roleAddFormKey
    },
    saveRole,
    activateRole,
    disableRole,
    deleteRole,

    // 搜索处理器，适配AbstractCrudController的数据格式
    async searchHandler(bodyParams, requestParams) {
      return await queryRoles(
        bodyParams,
        requestParams.pageNum,
        requestParams.pageSize
      )
    },

    // 保存权限（新增/编辑）
    async handleSaveRole(formData) {
      return await saveRole(formData)
    },

    // 删除权限
    async handleDeleteRole(row) {
      return await deleteRole(row.id)
    },

    // 删除确认文本
    deleteConfirmText(row, index) {
      return `确认要删除权限"${row.role}"吗？`
    },

    /**
     * 切换权限状态
     */
    async handleToggleStatus(row) {
      try {
        let response
        if (row.flag === 1) {
          // 当前是激活状态，点击后应该禁用
          await this.$confirm('确认要禁用该权限吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          })
          response = await disableRole(row.id)
        } else if (row.flag === 0) {
          // 当前是失效状态，点击后应该激活
          await this.$confirm('确认要激活该权限吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          })
          response = await activateRole(row.id)
        }

        if (response.code === 200) {
          // 消息提示由后端统一控制
          // 刷新数据
          this.$refs.roleManagement.refresh()
        } else {
          // 消息提示由后端统一控制
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('操作失败:', error)
          // 消息提示由后端统一控制
        }
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.role-management {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);
}
</style>

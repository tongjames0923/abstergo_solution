<template>
  <div class="user-management">
    <div class="header-section">
      <h2>用户管理</h2>
      <div class="header-actions">
        <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增用户</el-button>
      </div>
    </div>

    <!-- 查询条件 -->
    <DynamicForm
      ref="searchForm"
      v-model="searchForm"
      :config="searchConfig"
      title="查询条件"
      @reset="handleReset"
      @submit="handleSearch"
    />

    <!-- 用户列表 -->
    <el-card class="table-card" shadow="never">
      <div slot="header" class="clearfix card-header">
        <span class="card-title">用户列表</span>
        <div class="table-header-actions">
          <el-button
            icon="el-icon-refresh"
            circle
            size="mini"
            @click="handleSearch"
            title="刷新数据"
          ></el-button>
        </div>
      </div>

      <DynamicTable
        ref="userTable"
        :data-fetcher="fetchUserData"
        :columns="columns"
        :actions="actions"
        :auto-load="true"
        actions-fixed="right"
        actions-width="380px"
        border
        stripe
        class="user-table"
        @sort-change="handleTableSortChange"
      >

        <template #status="{ row }">
          <el-tag v-if="row.flag === 0" type="danger" effect="dark">禁用</el-tag>
          <el-tag v-else-if="row.flag===1" type="success" effect="dark">正常</el-tag>
          <el-tag v-else type="warning" effect="dark">锁定</el-tag>
        </template>

        <template #avatar="{row}">
          <el-avatar
            :src="row.avatar || '/default-avatar.png'"
            fit="cover"
            shape="circle"
            size="small"
            class="user-avatar"
          >
            <i v-if="!row.avatar || row.avatar === ''" class="el-icon-user-solid"></i>
          </el-avatar>
        </template>

        <template #actions="{ row, $index }">
          <div class="action-buttons">
            <el-button
              size="mini"
              type="primary"
              plain
              icon="el-icon-edit"
              @click="handleEdit(row)"
            >编辑
            </el-button>

            <el-button
              size="mini"
              :type="row.flag === 1 ? 'danger' : 'success'"
              plain
              :icon="row.flag === 1 ? 'el-icon-lock' : 'el-icon-unlock'"
              @click="handleToggleStatus(row)"
            >
              {{ row.flag === 1 ? '禁用' : '启用' }}
            </el-button>

            <el-button
              size="mini"
              type="warning"
              plain
              icon="el-icon-refresh-left"
              @click="handleResetPassword(row)"
            >重置密码
            </el-button>
          </div>
        </template>
      </DynamicTable>
    </el-card>

    <!-- 新增用户弹窗 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="userDialogVisible"
      width="600px"
      @close="handleUserDialogClose"
      :modal-append-to-body="false"
      class="user-dialog"
    >
      <UserAddView
        v-if="dialogMode === 'add'"
        ref="userAddView"
        @success="handleUserOperationSuccess"
        @cancel="handleUserOperationCancel"
      />
      <UserEditView
        v-else-if="dialogMode === 'edit'"
        ref="userEditView"
        :user-data="currentUser"
        @success="handleUserOperationSuccess"
        @cancel="handleUserOperationCancel"
      />
    </el-dialog>

    <!-- 重置密码弹窗 -->
    <el-dialog
      title="重置密码"
      :visible.sync="resetPasswordDialogVisible"
      width="450px"
      @close="handleResetPasswordDialogClose"
      :modal-append-to-body="false"
      class="password-dialog"
    >
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordForm" label-width="80px">
        <el-form-item label="用户名" prop="userName">
          <el-input
            v-model="passwordForm.userName"
            disabled
            prefix-icon="el-icon-user"
          ></el-input>
        </el-form-item>
        <el-form-item label="新密码" prop="password">
          <el-input
            v-model="passwordForm.password"
            type="password"
            show-password
            prefix-icon="el-icon-lock"
            placeholder="请输入新密码"
          ></el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            show-password
            prefix-icon="el-icon-lock"
            placeholder="请再次输入密码"
          ></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="resetPasswordDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handlePasswordSubmit">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import DynamicTable from "@/components/DynamicTable/index.vue"
import {activateUser, adminQueryUser, disableUser, saveUser} from '@/api/user'
import DynamicOptions from "@/components/DynamicOptions/index.vue";
import UserAddView from "@/components/UserAdminView/UserAddView.vue";
import UserEditView from "@/components/UserAdminView/UserEditView.vue";
import DynamicForm from "@/components/DynamicForm/index.vue";

export default {
  name: 'UserAdminView',
  components: {DynamicOptions, DynamicTable, UserAddView, UserEditView, DynamicForm},
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
      userDialogVisible: false,
      resetPasswordDialogVisible: false,
      dialogMode: 'add', // 'add' 或 'edit'
      currentUser: {},
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
      searchForm: {
        idEquals: '',
        userNameEquals: '',
        nickNameLike: '',
        flagEquals: undefined,
        createTimeRange: [], // 确保这是数组
        updateTimeRange: [], // 确保这是数组
        createTimeGreaterThan: '',
        createTimeLessThan: '',
        updateTimeGreaterThan: '',
        updateTimeLessThan: '',
        createTimeOrder: '',
        updateTimeOrder: ''
      },
      // 查询表单配置
      searchConfig: {
        expandable: true,
        expanded: true,
        gutter: 20,
        labelWidth: '100px',
        fields: [
          {
            prop: 'idEquals',
            label: '用户ID',
            type: 'input',
            placeholder: '请输入用户ID',
            prefixIcon: 'el-icon-search'
          },
          {
            prop: 'userNameEquals',
            label: '用户名',
            type: 'input',
            placeholder: '请输入用户名',
            prefixIcon: 'el-icon-user'
          },
          {
            prop: 'nickNameLike',
            label: '昵称',
            type: 'input',
            placeholder: '请输入昵称',
            prefixIcon: 'el-icon-star-off'
          },
          {
            prop: 'flagEquals',
            label: '用户状态',
            type: 'dynamic-options',
            apiUrl: '/private/user/statusOptions',
            placeholder: '请选择用户状态'
          },
          {
            prop: 'createTimeRange',
            label: '创建时间',
            type: 'date-range',
            valueFormat: 'yyyy-MM-dd HH:mm:ss',
            format: 'yyyy-MM-dd',
            startPlaceholder: '开始日期',
            endPlaceholder: '结束日期'
          },
          {
            prop: 'updateTimeRange',
            label: '更新时间',
            type: 'date-range',
            valueFormat: 'yyyy-MM-dd HH:mm:ss',
            format: 'yyyy-MM-dd',
            startPlaceholder: '开始日期',
            endPlaceholder: '结束日期'
          }
        ]
      },
      columns: [
        {prop: 'id', label: 'ID', width: 80},
        {prop: 'avatar', label: '头像', width: 120},
        {prop: 'userName', label: '用户名', width: 180},
        {prop: 'nickName', label: '昵称', width: 180},
        {prop: 'remark', label: '备注', width: 250},
        {prop: 'flag', label: '状态', width: 150, slot: 'status',},
        {prop: 'createTime', label: '创建时间', width: 180, sortable: 'custom'},
        {prop: 'updateTime', label: '更新时间', width: 180, sortable: 'custom'}
      ],
      actions: true,
      // 当前排序参数
      sortParams: {}
    }
  },
  computed: {
    dialogTitle() {
      return this.dialogMode === 'add' ? '新增用户' : '编辑用户';
    }
  },
  methods: {
    /**
     * 获取用户数据
     */
    async fetchUserData(params) {
      try {
        // 构造查询条件
        const criteria = {
          idEquals: this.searchForm.idEquals || undefined,
          userNameEquals: this.searchForm.userNameEquals || undefined,
          nickNameLike: this.searchForm.nickNameLike || undefined,
          flagEquals: this.searchForm.flagEquals,
          createTimeGreaterThan: this.searchForm.createTimeRange && this.searchForm.createTimeRange[0] || this.searchForm.createTimeGreaterThan || undefined,
          createTimeLessThan: this.searchForm.createTimeRange && this.searchForm.createTimeRange[1] || this.searchForm.createTimeLessThan || undefined,
          updateTimeGreaterThan: this.searchForm.updateTimeRange && this.searchForm.updateTimeRange[0] || this.searchForm.updateTimeGreaterThan || undefined,
          updateTimeLessThan: this.searchForm.updateTimeRange && this.searchForm.updateTimeRange[1] || this.searchForm.updateTimeLessThan || undefined
        }

        // 添加排序参数
        if (this.sortParams.field && this.sortParams.order) {
          // 将Element UI的排序格式转换为后端需要的格式
          const order = this.sortParams.order === 'ascending' ? 'asc' : 'desc';
          criteria[`${this.sortParams.field}Order`] = order;
        }

        // 移除值为undefined的属性
        Object.keys(criteria).forEach(key => {
          if (criteria[key] === undefined) {
            delete criteria[key]
          }
        })

        // 调用后端接口
        const response = await adminQueryUser({
          ...criteria,
          pageNum: params.pagination.page,
          pageSize: params.pagination.size
        })

        // 返回数据给DynamicTable组件
        return {
          data: response.data.data || [],
          total: response.data.total || 0
        }
      } catch (error) {
        console.error('获取用户数据失败:', error)
        this.$message.error('获取用户数据失败')
        return {
          data: [],
          total: 0
        }
      }
    },

    /**
     * 查询按钮点击事件
     */
    handleSearch() {
      this.$refs.userTable.refresh()
    },

    /**
     * 重置按钮点击事件
     */
    handleReset() {
      this.searchForm = {
        idEquals: '',
        userNameEquals: '',
        nickNameLike: '',
        flagEquals: undefined,
        createTimeRange: [],
        updateTimeRange: [],
        createTimeGreaterThan: '',
        createTimeLessThan: '',
        updateTimeGreaterThan: '',
        updateTimeLessThan: '',
        createTimeOrder: '',
        updateTimeOrder: ''
      }
      this.sortParams = {}
      this.$refs.userTable.refresh()
    },

    /**
     * 处理表格排序变化
     */
    handleTableSortChange(sortInfo) {
      const {prop, order} = sortInfo;
      if (prop && order) {
        this.sortParams = {
          field: prop,
          order: order
        };
      } else {
        this.sortParams = {};
      }
      // 表格会自动调用 loadData 方法，无需手动刷新
    },

    /**
     * 新增用户
     */
    handleAdd() {
      this.dialogMode = 'add';
      this.userDialogVisible = true;
    },

    /**
     * 编辑用户
     */
    handleEdit(row) {
      this.dialogMode = 'edit';
      this.currentUser = {...row};
      this.userDialogVisible = true;
    },

    /**
     * 切换用户状态
     */
    async handleToggleStatus(row) {
      try {
        let response;
        if (row.flag === 1) {
          // 当前是正常状态，点击后应该禁用
          await this.$confirm('确认要禁用该用户吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          });
          response = await disableUser(row.id);
        } else {
          // 当前是禁用或锁定状态，点击后应该激活
          await this.$confirm('确认要激活该用户吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          });
          response = await activateUser(row.id);
        }

        if (response.code === 200) {
          this.$message.success(response.message || '操作成功');
          // 刷新表格数据
          this.$refs.userTable.refresh();
        } else {
          this.$message.error(response.message || '操作失败');
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('操作失败:', error);
          this.$message.error('操作失败');
        }
      }
    },

    /**
     * 处理用户操作成功事件
     */
    handleUserOperationSuccess() {
      this.$message.success(this.dialogMode === 'add' ? '用户创建成功' : '用户更新成功');
      this.userDialogVisible = false;
      this.$refs.userTable.refresh();
    },

    /**
     * 处理用户操作取消事件
     */
    handleUserOperationCancel() {
      this.userDialogVisible = false;
    },

    /**
     * 处理用户弹窗关闭事件
     */
    handleUserDialogClose() {
      if (this.dialogMode === 'add' && this.$refs.userAddView) {
        // 重置表单
        this.$refs.userAddView.$refs.userForm.resetFields();
        // 重置表单数据
        this.$refs.userAddView.userForm = {
          userName: '',
          password: '',
          confirmPassword: '',
          nickName: '',
          remark: '',
          avatar: ''
        };
      }
      // 编辑模式不需要特别处理，因为下次打开时会传入新的数据
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
      };
      this.resetPasswordDialogVisible = true;
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
            };

            // 调用后端保存接口
            const response = await saveUser(userData);

            if (response.code === 200) {
              this.$message.success('密码重置成功');
              this.resetPasswordDialogVisible = false;
              this.$refs.userTable.refresh();
            } else {
              this.$message.error(response.message || '密码重置失败');
            }
          } catch (error) {
            console.error('密码重置失败:', error);
            this.$message.error('密码重置失败');
          }
        } else {
          console.log('表单验证失败');
          this.$message.warning('请填写正确的表单信息');
          return false;
        }
      });
    },

    /**
     * 处理重置密码弹窗关闭事件
     */
    handleResetPasswordDialogClose() {
      this.$refs.passwordForm.resetFields();
      this.passwordForm = {
        id: undefined,
        userName: '',
        password: '',
        confirmPassword: ''
      };
    }
  }
}
</script>

<style lang="scss" scoped>
.user-management {
  margin: 20px;
  padding: 0;

  .header-section {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;

    h2 {
      margin: 0;
      font-size: 24px;
      font-weight: 600;
      color: #303133;
    }

    .header-actions {
      display: flex;
      gap: 10px;
    }
  }

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .card-title {
      font-size: 16px;
      font-weight: 600;
      color: #303133;
    }
  }

  .search-card,
  .table-card {
    margin-bottom: 20px;
    border-radius: 8px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);

    ::v-deep .el-card__header {
      padding: 15px 20px;
      background-color: #f8f9fa;
      border-bottom: 1px solid #ebeef5;
      border-radius: 8px 8px 0 0 !important;
    }
  }

  .search-form {
    padding: 10px 0;

    .el-form-item {
      margin-bottom: 18px;
    }

    .date-range-picker,
    .sort-select {
      width: 100%;
    }

    .search-actions {
      display: flex;
      justify-content: flex-end;
      gap: 10px;
      padding-top: 10px;
      border-top: 1px solid #f0f0f0;
      margin-top: 10px;
    }
  }

  .table-header-actions {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .user-table {
    ::v-deep .el-table th {
      background-color: #f5f7fa;
    }
  }

  .user-avatar {
    background-color: #e1f3d8;
  }

  .action-buttons {
    display: flex;
    gap: 8px;
    justify-content: center;
    flex-wrap: wrap;
  }

  .user-dialog,
  .password-dialog {
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

.dashboard {
  &-container {
    margin: 30px;
  }

  &-text {
    font-size: 30px;
    line-height: 46px;
  }
}
</style>


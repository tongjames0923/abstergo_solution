<template>
  <div class="user-management">
    <div class="header-section">
      <h2>用户管理</h2>
      <div class="header-actions">
        <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增用户</el-button>
      </div>
    </div>

    <!-- 查询条件 -->
    <DynamicFormWithApi
      ref="searchForm"
      v-model="searchForm"
      config-name="userSearchForm"
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
            :icon="viewMode === 'card' ? 'el-icon-tickets' : 'el-icon-menu'"
            :title="viewMode === 'card' ? '切换到表格视图' : '切换到卡片视图'"
            circle
            size="mini"
            @click="toggleViewMode"
          ></el-button>
          <el-button
            icon="el-icon-refresh"
            circle
            size="mini"
            @click="handleSearch"
            title="刷新数据"
          ></el-button>
        </div>
      </div>

      <!-- 卡片视图 -->
      <CardList
        v-if="viewMode === 'card'"
        ref="userCardList"
        :columns="columns"
        :current-page="currentPage"
        :data="userList"
        :gap="20"
        :loading="loading"
        :loading-count="loadingCount"
        :padding="20"
        :page-size="pageSize"
        :pagination="true"
        :total="total"
        @current-change="handlePageChange"
        @size-change="handleSizeChange"
      >
        <!-- 工具栏插槽 -->
        <template #toolbar>
          <div class="card-list-toolbar-content">
            <div class="toolbar-info">
              共 {{ total }} 个用户
            </div>
          </div>
        </template>

        <!-- 自定义卡片内容 -->
        <template #card="{ item: user }">
          <el-card class="user-card" shadow="hover">
            <div class="user-card-header">
              <el-avatar
                :src="user.avatar || '/default-avatar.png'"
                class="user-avatar"
                fit="cover"
                size="large"
              >
                <i v-if="!user.avatar || user.avatar === ''" class="el-icon-user-solid"></i>
              </el-avatar>
              <div class="user-basic-info">
                <div class="user-name">{{ user.userName }}</div>
                <div class="user-nickname">{{ user.nickName }}</div>
              </div>
              <el-tag
                :type="getUserStatusType(user.flag)"
                class="user-status-tag"
                size="mini"
              >
                {{ getUserStatusText(user.flag) }}
              </el-tag>
            </div>

            <div class="user-card-body">
              <el-descriptions :column="1" border size="mini">
                <el-descriptions-item label="ID">{{ user.id }}</el-descriptions-item>
                <el-descriptions-item label="备注">{{ user.remark || '无' }}</el-descriptions-item>
                <el-descriptions-item label="创建时间">{{ formatDate(user.createTime) }}</el-descriptions-item>
                <el-descriptions-item label="更新时间">{{ formatDate(user.updateTime) }}</el-descriptions-item>
              </el-descriptions>
            </div>

            <div class="user-card-footer">
              <el-button
                icon="el-icon-edit"
                plain
                size="mini"
                type="primary"
                @click.stop="handleEdit(user)"
              >编辑
              </el-button>

              <el-button
                :icon="user.flag === 1 ? 'el-icon-lock' : 'el-icon-unlock'"
                :type="user.flag === 1 ? 'danger' : 'success'"
                plain
                size="mini"
                @click.stop="handleToggleStatus(user)"
              >
                {{ user.flag === 1 ? '禁用' : '启用' }}
              </el-button>

              <el-button
                icon="el-icon-refresh-left"
                plain
                size="mini"
                type="warning"
                @click.stop="handleResetPassword(user)"
              >重置密码
              </el-button>

              <el-button
                icon="el-icon-setting"
                plain
                size="mini"
                type="info"
                @click.stop="handleManageRoles(user)"
              >权限管理
              </el-button>
            </div>
          </el-card>
        </template>

        <!-- 自定义空状态 -->
        <template #empty>
          <el-empty description="暂无用户数据">
            <el-button icon="el-icon-plus" type="primary" @click="handleAdd">新增用户</el-button>
          </el-empty>
        </template>
      </CardList>

      <!-- 表格视图 -->
      <DynamicTable
        v-else
        ref="userTable"
        :columns="tableColumns"
        :data-fetcher="fetchUserDataForTable"
        :actions="actions"
        :auto-load="true"
        actions-fixed="right"
        actions-width="450px"
        border
        stripe
        class="user-table"
        @sort-change="handleTableSortChange"
      >
        <template #status="{ row }">
          <el-tag v-if="row.flag===1" type="success">正常</el-tag>
          <el-tag v-else-if="row.flag===2" type="danger">锁定</el-tag>
          <el-tag v-else type="warning">禁用</el-tag>
        </template>

        <template #avatar="{row}">
          <i v-if="!row.avatar || row.avatar === ''" class="el-icon-user-solid">未设置图片</i>
          <el-avatar v-else
                     :src="row.avatar || '/default-avatar.png'"
                     fit="fill"
                     shape="circle"
                     size="large"
                     class="user-avatar"
          >
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

            <el-button
              size="mini"
              type="info"
              plain
              icon="el-icon-setting"
              @click="handleManageRoles(row)"
            >权限管理
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

    <!-- 权限管理弹窗 -->
    <el-dialog
      title="权限管理"
      :visible.sync="roleManagementDialogVisible"
      width="800px"
      @close="handleRoleManagementDialogClose"
      :modal-append-to-body="false"
      class="role-dialog"
    >
      <UserRoleAdminView
        v-if="roleManagementDialogVisible"
        :user-id="currentUserId"
      />
    </el-dialog>
  </div>
</template>

<script>
import CardList from "@/components/CardList"
import DynamicTable from "@/components/DynamicTable/index.vue"
import DynamicOptions from "@/components/DynamicOptions/index.vue";
import UserAddView from "@/components/UserAdminView/UserAddView.vue";
import UserEditView from "@/components/UserAdminView/UserEditView.vue";
import DynamicFormWithApi from "@/components/DynamicForm/DynamicFormWithApi.vue";
import UserRoleAdminView from "@/components/UserAdminView/UserRoleAdminView.vue";
import {activateUser, adminQueryUser, disableUser, saveUser} from "@/api/admin_user";

export default {
  name: 'UserAdminView',
  components: {
    CardList,
    DynamicTable,
    DynamicOptions,
    UserAddView,
    UserEditView,
    DynamicFormWithApi,
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
      viewMode: 'card', // 'card' 或 'table'
      loading: false,
      userList: [],
      searchConfig: null,
      total: 0,
      currentPage: 1,
      pageSize: 10,
      userDialogVisible: false,
      resetPasswordDialogVisible: false,
      roleManagementDialogVisible: false,
      dialogMode: 'add', // 'add' 或 'edit'
      currentUserId: null,
      currentUser: undefined,
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
      columns: 4,
      tableColumns: [
        {prop: 'id', label: 'ID', width: 80},
        {prop: 'avatar', label: '头像', width: 120, slot: 'avatar'},
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
    },

    loadingCount() {
      // 根据当前页数据量来确定加载占位符数量，避免创建多余元素
      // 如果当前页数据量小于每页大小，使用实际数据量；否则使用每页大小
      return 0
    }


  },
  mounted() {
    this.fetchUserData()
  },
  methods: {
    /**
     * 切换视图模式
     */
    toggleViewMode() {
      this.viewMode = this.viewMode === 'card' ? 'table' : 'card';
      this.$nextTick(() => {
        if (this.viewMode === 'table' && this.$refs.userTable) {
          this.$refs.userTable.refresh();
        }
      });
    },

    /**
     * 获取用户数据（卡片视图）
     */
    async fetchUserData() {
      this.loading = true
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
          pageNum: this.currentPage,
          pageSize: this.pageSize
        })

        // 返回数据给CardList组件
        this.userList = response.data.data || []
        this.total = response.data.total || 0
      } catch (error) {
        console.error('获取用户数据失败:', error)
        this.$message.error('获取用户数据失败')
        this.userList = []
        this.total = 0
      } finally {
        this.loading = false
      }
    },

    /**
     * 获取用户数据（表格视图）
     */
    async fetchUserDataForTable(params) {
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
      if (this.viewMode === 'card') {
        this.currentPage = 1
        this.fetchUserData()
      } else {
        this.$refs.userTable.refresh()
      }
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

      if (this.viewMode === 'card') {
        this.currentPage = 1
        this.fetchUserData()
      } else {
        this.$refs.userTable.refresh()
      }
    },

    /**
     * 处理分页变化（卡片视图）
     */
    handlePageChange(page) {
      this.currentPage = page
      this.fetchUserData()
    },

    /**
     * 处理页面大小变化（卡片视图）
     */
    handleSizeChange(size) {
      this.pageSize = size
      this.currentPage = 1
      this.fetchUserData()
    },

    /**
     * 处理表格排序变化
     */
    handleTableSortChange(sortInfo) {
      this.sortParams = sortInfo;
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
          // 刷新数据
          if (this.viewMode === 'card') {
            this.fetchUserData();
          } else {
            this.$refs.userTable.refresh();
          }
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

      // 刷新数据
      if (this.viewMode === 'card') {
        this.fetchUserData();
      } else {
        this.$refs.userTable.refresh();
      }
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

              // 刷新数据
              if (this.viewMode === 'card') {
                this.fetchUserData();
              } else {
                this.$refs.userTable.refresh();
              }
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
    },

    /**
     * 处理权限管理按钮点击事件
     */
    handleManageRoles(row) {
      this.currentUserId = row.id;
      this.roleManagementDialogVisible = true;
    },

    /**
     * 处理权限管理弹窗关闭事件
     */
    handleRoleManagementDialogClose() {
      this.currentUserId = undefined
    },

    /**
     * 获取用户状态文本
     */
    getUserStatusText(flag) {
      const statusMap = {
        1: '正常',
        2: '锁定',
        0: '禁用'
      }
      return statusMap[flag] || '未知'
    },

    /**
     * 获取用户状态标签类型
     */
    getUserStatusType(flag) {
      const typeMap = {
        1: 'success',
        2: 'danger',
        0: 'warning'
      }
      return typeMap[flag] || 'info'
    },

    /**
     * 格式化日期
     */
    formatDate(date) {
      if (!date) return '无'
      return date
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

  .card-list-toolbar-content {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .toolbar-info {
      color: #666;
      font-size: 14px;
    }
  }

  .user-card {
    height: 100%;
    display: flex;
    flex-direction: column;

    .user-card-header {
      display: flex;
      align-items: center;
      padding: 10px 0;
      border-bottom: 1px solid #eee;

      .user-avatar {
        margin-right: 15px;
      }

      .user-basic-info {
        flex: 1;

        .user-name {
          font-size: 16px;
          font-weight: 600;
          color: #333;
          margin-bottom: 3px;
        }

        .user-nickname {
          font-size: 13px;
          color: #999;
        }
      }

      .user-status-tag {
        flex-shrink: 0;
      }
    }

    .user-card-body {
      flex: 1;
      padding: 15px 0;

      ::v-deep .el-descriptions {
        .el-descriptions__body {
          background-color: #fafafa;
        }

        .el-descriptions__label {
          width: 80px;
          background-color: #f5f7fa;
        }
      }
    }

    .user-card-footer {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
      padding-top: 15px;
      border-top: 1px solid #eee;

      ::v-deep .el-button {
        flex: 1;
        min-width: 70px;
      }
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

  ::v-deep .card-list-container {
    .card-list {
      padding: 20px;
    }

    .pagination-container {
      padding: 20px;
      border-top: 1px solid #ebeef5;
    }
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

// 响应式设计
@media (max-width: 1200px) {
  .user-management {
    ::v-deep .card-list-container .card-list {
      grid-template-columns: repeat(3, minmax(0, 1fr)) !important;
    }
  }
}

@media (max-width: 992px) {
  .user-management {
    ::v-deep .card-list-container .card-list {
      grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
    }

    .user-card {
      .user-card-footer {
        ::v-deep .el-button {
          min-width: 60px;
          font-size: 12px;
          padding: 7px 5px;
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .user-management {
    margin: 10px;

    ::v-deep .card-list-container .card-list {
      grid-template-columns: repeat(1, minmax(0, 1fr)) !important;
      gap: 15px !important;
      padding: 15px !important;
    }

    .header-section {
      h2 {
        font-size: 20px;
      }
    }

    .user-card {
      .user-card-header {
        .user-name {
          font-size: 15px;
        }

        .user-nickname {
          font-size: 12px;
        }
      }

      .user-card-footer {
        ::v-deep .el-button {
          min-width: 50px;
          font-size: 12px;
          padding: 7px 3px;
        }
      }
    }
  }
}
</style>

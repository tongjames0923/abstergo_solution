<template>
  <div class="user-management">
    <h2>用户管理</h2>

    <!-- 查询条件 -->
    <el-card class="search-card">
      <div slot="header" class="clearfix">
        <span>查询条件</span>
      </div>
      <el-form :model="searchForm" ref="searchForm" label-width="120px" class="search-form">
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="用户ID">
              <el-input v-model="searchForm.idEquals" placeholder="请输入用户ID"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="用户名">
              <el-input v-model="searchForm.userNameEquals" placeholder="请输入用户名"></el-input>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="昵称">
              <el-input v-model="searchForm.nickNameLike" placeholder="请输入昵称"></el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="用户状态">
              <DynamicOptions api-url="/private/user/statusOptions" :value="searchForm.flagEquals"
                              @change="(val)=>this.searchForm.flagEquals=val"
              ></DynamicOptions>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="创建时间(起)">
              <el-date-picker
                v-model="searchForm.createTimeGreaterThan"
                type="datetime"
                placeholder="选择开始时间"
                value-format="yyyy-MM-dd HH:mm:ss"
                format="yyyy-MM-dd HH:mm:ss">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="创建时间(止)">
              <el-date-picker
                v-model="searchForm.createTimeLessThan"
                type="datetime"
                placeholder="选择结束时间"
                value-format="yyyy-MM-dd HH:mm:ss"
                format="yyyy-MM-dd HH:mm:ss">
              </el-date-picker>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="更新时间(起)">
              <el-date-picker
                v-model="searchForm.updateTimeGreaterThan"
                type="datetime"
                placeholder="选择开始时间"
                value-format="yyyy-MM-dd HH:mm:ss"
                format="yyyy-MM-dd HH:mm:ss">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="更新时间(止)">
              <el-date-picker
                v-model="searchForm.updateTimeLessThan"
                type="datetime"
                placeholder="选择结束时间"
                value-format="yyyy-MM-dd HH:mm:ss"
                format="yyyy-MM-dd HH:mm:ss">
              </el-date-picker>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="创建时间排序">
              <el-select v-model="searchForm.createTimeOrder" placeholder="请选择排序方式" clearable>
                <el-option label="升序" value="asc"></el-option>
                <el-option label="降序" value="desc"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="更新时间排序">
              <el-select v-model="searchForm.updateTimeOrder" placeholder="请选择排序方式" clearable>
                <el-option label="升序" value="asc"></el-option>
                <el-option label="降序" value="desc"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="24" style="text-align: right">
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 用户列表 -->
    <el-card class="table-card">
      <div slot="header" class="clearfix">
        <span>用户列表</span>
      </div>
      <DynamicTable
        ref="userTable"
        :data-fetcher="fetchUserData"
        :columns="columns"
        :actions="actions"
        :auto-load="true"
      >
        <template #toolbar>
          <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增用户</el-button>
        </template>

        <template #status="{ row }">
          <el-tag v-if="row.flag === 0" type="danger">禁用</el-tag>
          <el-tag v-else-if="row.flag===1" type="success">正常</el-tag>
          <el-tag v-else type="warning">锁定</el-tag>
        </template>

        <template #avatar="{row}">
          <el-avatar :src="row.avatar || '/default-avatar.png'" fit="cover">
            <i v-if="!row.avatar || row.avatar === ''" class="el-icon-user-solid"></i>
          </el-avatar>
        </template>

        <template #actions="{ row, $index }">
          <el-button size="mini" @click="handleEdit(row)">编辑</el-button>
          <el-button
            size="mini"
            :type="row.flag === 1 ? 'danger' : 'success'"
            @click="handleToggleStatus(row)"
          >
            {{ row.flag === 1 ? '禁用' : '启用' }}
          </el-button>
        </template>
      </DynamicTable>
    </el-card>

    <!-- 新增用户弹窗 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="userDialogVisible"
      width="85%"
      @close="handleUserDialogClose"
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
  </div>
</template>

<script>
import DynamicTable from "@/components/DynamicTable/index.vue"
import {activateUser, adminQueryUser, disableUser} from '@/api/user'
import DynamicOptions from "@/components/DynamicOptions/index.vue";
import UserAddView from "@/components/UserAdminView/UserAddView.vue";
import UserEditView from "@/components/UserAdminView/UserEditView.vue";

export default {
  name: 'UserAdminView',
  components: {DynamicOptions, DynamicTable, UserAddView, UserEditView},
  data() {
    return {
      userDialogVisible: false,
      dialogMode: 'add', // 'add' 或 'edit'
      currentUser: {},
      searchForm: {
        idEquals: '',
        userNameEquals: '',
        nickNameLike: '',
        flagEquals: undefined,
        createTimeGreaterThan: '',
        createTimeLessThan: '',
        updateTimeGreaterThan: '',
        updateTimeLessThan: '',
        createTimeOrder: '',
        updateTimeOrder: ''
      },
      columns: [
        {prop: 'id', label: 'ID', width: 80},
        {prop: 'avatar', label: '头像', width: 80, slot: 'avatar'},
        {prop: 'userName', label: '用户名', width: 150},
        {prop: 'nickName', label: '昵称', width: 150},
        {prop: 'remark', label: '备注', width: 150},
        {prop: 'flag', label: '状态', width: 100, slot: 'status'},
        {prop: 'createTime', label: '创建时间', width: 180},
        {prop: 'updateTime', label: '更新时间', width: 180}
      ],
      actions: true
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
          createTimeGreaterThan: this.searchForm.createTimeGreaterThan || undefined,
          createTimeLessThan: this.searchForm.createTimeLessThan || undefined,
          updateTimeGreaterThan: this.searchForm.updateTimeGreaterThan || undefined,
          updateTimeLessThan: this.searchForm.updateTimeLessThan || undefined
        }

        // 处理排序字段
        if (this.searchForm.createTimeOrder) {
          criteria.createTimeOrder = this.searchForm.createTimeOrder
        }

        if (this.searchForm.updateTimeOrder) {
          criteria.updateTimeOrder = this.searchForm.updateTimeOrder
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
        createTimeGreaterThan: '',
        createTimeLessThan: '',
        updateTimeGreaterThan: '',
        updateTimeLessThan: '',
        createTimeOrder: '',
        updateTimeOrder: ''
      }
      this.$refs.userTable.refresh()
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
  }
}
</script>

<style lang="scss" scoped>
.dashboard {
  &-container {
    margin: 30px;
  }

  &-text {
    font-size: 30px;
    line-height: 46px;
  }
}

.user-management {
  margin-top: 30px;

  h2 {
    margin-bottom: 20px;
  }

  .search-card,
  .table-card {
    margin-bottom: 20px;
  }

  .search-form {
    .el-form-item {
      margin-bottom: 15px;
    }
  }

  ::v-deep .el-card__header {
    padding: 10px 20px;
    background-color: #f5f7fa;
    border-bottom: 1px solid #ebeef5;
  }
}
</style>


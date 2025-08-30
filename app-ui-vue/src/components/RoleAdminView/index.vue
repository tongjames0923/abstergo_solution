<template>
  <div class="role-management">
    <div class="header-section">
      <h2>角色管理</h2>
      <div class="header-actions">
        <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增角色</el-button>
      </div>
    </div>

    <!-- 查询条件 -->
    <el-card class="search-card" shadow="never">
      <div slot="header" class="clearfix">
        <span>查询条件</span>
      </div>
      <el-form
        :model="searchForm"
        ref="searchForm"
        label-width="80px"
        class="search-form"
      >
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="角色ID">
              <el-input
                v-model="searchForm.idEquals"
                placeholder="请输入角色ID"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="角色名称">
              <el-input
                v-model="searchForm.roleEquals"
                placeholder="请输入角色名称"
                clearable
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="状态">
              <el-select
                v-model="searchForm.flagEquals"
                placeholder="请选择状态"
                clearable
                style="width: 100%"
              >
                <el-option label="失效" :value="0"></el-option>
                <el-option label="激活" :value="1"></el-option>
                <el-option label="系统" :value="2"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <div class="search-actions">
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">查询</el-button>
          <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
        </div>
      </el-form>
    </el-card>

    <!-- 角色列表 -->
    <el-card class="table-card" shadow="never">
      <div slot="header" class="clearfix card-header">
        <span class="card-title">角色列表</span>
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

      <CardList
        ref="roleCardList"
        :data="roleData"
        :loading="loading"
        :pagination="true"
        :total="total"
        :current-page="currentPage"
        :page-size="pageSize"
        :columns="columns"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      >
        <template #card="{ item: row }">
          <el-card class="role-card" shadow="hover">
            <div class="role-card-header">
              <div class="role-name">{{ row.role }}</div>
              <div class="role-flag">
                <el-tag v-if="row.flag === 0" type="danger" effect="dark">失效</el-tag>
                <el-tag v-else-if="row.flag===1" type="success" effect="dark">激活</el-tag>
                <el-tag v-else-if="row.flag===2" type="warning" effect="dark">系统</el-tag>
              </div>
            </div>
            <div class="role-card-body">
              <div class="role-description">{{ row.description }}</div>
              <div class="role-meta">
                <div class="role-id">ID: {{ row.id }}</div>
                <div class="role-time">
                  <div>创建: {{ row.createTime }}</div>
                  <div>更新: {{ row.updateTime }}</div>
                </div>
              </div>
            </div>
            <div class="role-card-footer">
              <div class="action-buttons">
                <el-button
                  size="mini"
                  type="primary"
                  plain
                  icon="el-icon-edit"
                  @click.stop="handleEdit(row)"
                  :disabled="row.flag === 2"
                >编辑
                </el-button>

                <el-button
                  v-if="row.flag !== 2"
                  size="mini"
                  :type="row.flag === 1 ? 'danger' : 'success'"
                  plain
                  :icon="row.flag === 1 ? 'el-icon-lock' : 'el-icon-unlock'"
                  @click.stop="handleToggleStatus(row)"
                >
                  {{ row.flag === 1 ? '禁用' : '启用' }}
                </el-button>

                <el-button
                  v-if="row.flag === 2"
                  size="mini"
                  type="info"
                  plain
                  disabled
                >系统角色
                </el-button>

                <el-button
                  size="mini"
                  type="danger"
                  plain
                  icon="el-icon-delete"
                  @click.stop="handleDelete(row)"
                  :disabled="row.flag === 2"
                >删除
                </el-button>
              </div>
            </div>
          </el-card>
        </template>
      </CardList>
    </el-card>

    <!-- 新增/编辑角色弹窗 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="roleDialogVisible"
      width="600px"
      @close="handleRoleDialogClose"
      :modal-append-to-body="false"
      class="role-dialog"
    >
      <el-form
        :model="roleForm"
        :rules="roleRules"
        ref="roleForm"
        label-width="80px"
        class="role-form"
      >
        <el-form-item label="角色名称" prop="role">
          <el-input
            v-model="roleForm.role"
            placeholder="请输入角色名称"
            size="medium"
            prefix-icon="el-icon-user"
            :disabled="isEditMode"
          ></el-input>
        </el-form-item>

        <el-form-item label="角色描述" prop="description">
          <el-input
            v-model="roleForm.description"
            placeholder="请输入角色描述"
            type="textarea"
            :rows="3"
            size="medium"
          ></el-input>
        </el-form-item>

        <el-form-item label="状态" prop="flag">
          <el-switch
            v-model="roleForm.flag"
            :active-value="1"
            :inactive-value="0"
            active-text="启用"
            inactive-text="禁用"
          >
          </el-switch>
        </el-form-item>
      </el-form>

      <div slot="footer" class="dialog-footer">
        <el-button @click="roleDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleRoleSubmit">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import CardList from "@/components/CardList/index.vue"
import {activateRole, deleteRole, disableRole, queryRoles, saveRole} from "@/api/role"

export default {
  name: 'RoleAdminView',
  components: {CardList},
  data() {
    return {
      roleDialogVisible: false,
      dialogMode: 'add', // 'add' 或 'edit'
      roleForm: {
        id: undefined,
        role: '',
        description: '',
        flag: 1
      },
      roleRules: {
        role: [
          {required: true, message: '请输入角色名称', trigger: 'blur'},
          {min: 2, max: 20, message: '长度在 2 到 20 个字符', trigger: 'blur'}
        ],
        description: [
          {required: true, message: '请输入角色描述', trigger: 'blur'}
        ]
      },
      searchForm: {
        idEquals: '',
        roleEquals: '',
        flagEquals: undefined
      },
      roleData: [],
      loading: false,
      total: 0,
      currentPage: 1,
      pageSize: 10,
      columns: 3
    }
  },
  computed: {
    dialogTitle() {
      return this.dialogMode === 'add' ? '新增角色' : '编辑角色'
    },
    // 是否为编辑模式
    isEditMode() {
      return this.roleForm.id !== undefined
    }
  },
  mounted() {
    this.fetchRoleData()
  },
  methods: {
    /**
     * 获取角色数据
     */
    async fetchRoleData() {
      try {
        this.loading = true

        // 构造查询条件
        const criteria = {
          idEquals: this.searchForm.idEquals || undefined,
          roleEquals: this.searchForm.roleEquals || undefined,
          flagEquals: this.searchForm.flagEquals !== '' ? this.searchForm.flagEquals : undefined
        }

        // 移除值为undefined的属性
        Object.keys(criteria).forEach(key => {
          if (criteria[key] === undefined) {
            delete criteria[key]
          }
        })

        // 调用后端接口
        const response = await queryRoles(criteria, this.currentPage, this.pageSize)

        // 设置数据
        this.roleData = response.data.data || []
        this.total = response.data.total || 0
      } catch (error) {
        console.error('获取角色数据失败:', error)
        this.$message.error('获取角色数据失败')
        this.roleData = []
        this.total = 0
      } finally {
        this.loading = false
      }
    },

    /**
     * 查询按钮点击事件
     */
    handleSearch() {
      this.currentPage = 1
      this.fetchRoleData()
    },

    /**
     * 重置按钮点击事件
     */
    handleReset() {
      this.searchForm = {
        idEquals: '',
        roleEquals: '',
        flagEquals: undefined
      }
      this.currentPage = 1
      this.fetchRoleData()
    },

    /**
     * 处理分页大小变化
     */
    handleSizeChange(val) {
      this.pageSize = val
      this.currentPage = 1
      this.fetchRoleData()
    },

    /**
     * 处理当前页变化
     */
    handleCurrentChange(val) {
      this.currentPage = val
      this.fetchRoleData()
    },

    /**
     * 新增角色
     */
    handleAdd() {
      this.dialogMode = 'add'
      this.roleForm = {
        id: undefined,
        role: '',
        description: '',
        flag: 1
      }
      this.roleDialogVisible = true
    },

    /**
     * 编辑角色
     */
    handleEdit(row) {
      this.dialogMode = 'edit'
      this.roleForm = {...row}
      this.roleDialogVisible = true
    },

    /**
     * 切换角色状态
     */
    async handleToggleStatus(row) {
      try {
        let response
        if (row.flag === 2) {
          // 系统角色不能操作
          this.$message.warning('系统角色不能更改状态')
          return
        }

        if (row.flag === 1) {
          // 当前是激活状态，点击后应该禁用
          await this.$confirm('确认要禁用该角色吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          })

          response = await disableRole(row.id)
        } else if (row.flag === 0) {
          // 当前是失效状态，点击后应该激活
          await this.$confirm('确认要激活该角色吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          })

          response = await activateRole(row.id)
        }

        if (response.code === 200) {
          this.$message.success(response.message || '操作成功')
          // 刷新数据
          this.fetchRoleData()
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
     * 删除角色
     */
    async handleDelete(row) {
      try {
        if (row.flag === 2) {
          // 系统角色不能删除
          this.$message.warning('系统角色不能删除')
          return
        }

        await this.$confirm(`确认要删除角色"${row.role}"吗？`, '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        })

        const response = await deleteRole(row.id)
        if (response.code === 200) {
          this.$message.success('删除成功')
          this.fetchRoleData()
        } else {
          this.$message.error(response.message || '删除失败')
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除失败:', error)
          this.$message.error('删除失败')
        }
      }
    },

    /**
     * 提交角色表单
     */
    handleRoleSubmit() {
      this.$refs.roleForm.validate(async (valid) => {
        if (valid) {
          try {
            const roleData = {
              id: this.roleForm.id,
              role: this.roleForm.role,
              description: this.roleForm.description,
              flag: this.roleForm.flag
            }

            const response = await saveRole(roleData)

            if (response.code === 200) {
              this.$message.success(this.dialogMode === 'add' ? '角色创建成功' : '角色更新成功')
              this.roleDialogVisible = false
              this.fetchRoleData()
            } else {
              this.$message.error(response.message || '操作失败')
            }
          } catch (error) {
            console.error('保存失败:', error)
            this.$message.error('保存失败')
          }
        } else {
          console.log('表单验证失败')
          this.$message.warning('请填写正确的表单信息')
          return false
        }
      })
    },

    /**
     * 处理角色弹窗关闭事件
     */
    handleRoleDialogClose() {
      this.$refs.roleForm.resetFields()
    }
  }
}
</script>

<style lang="scss" scoped>
.role-management {
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

  .role-card {
    height: 100%;
    display: flex;
    flex-direction: column;

    .role-card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 10px;
      padding-bottom: 10px;
      border-bottom: 1px solid #eee;

      .role-name {
        font-size: 18px;
        font-weight: bold;
        color: #333;
      }
    }

    .role-card-body {
      flex: 1;
      margin-bottom: 15px;

      .role-description {
        margin-bottom: 15px;
        color: #666;
        line-height: 1.5;
      }

      .role-meta {
        font-size: 12px;
        color: #999;

        .role-id {
          margin-bottom: 5px;
        }

        .role-time {
          display: flex;
          justify-content: space-between;
        }
      }
    }

    .role-card-footer {
      border-top: 1px solid #eee;
      padding-top: 10px;

      .action-buttons {
        display: flex;
        flex-wrap: wrap;
        gap: 5px;
        justify-content: flex-end;

        ::v-deep .el-button {
          margin-left: 0;
        }
      }
    }
  }

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

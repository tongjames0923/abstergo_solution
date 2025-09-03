<template>
  <div class="company-management">
    <div class="header-section">
      <h2>公司管理</h2>
      <div class="header-actions">
        <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增公司</el-button>
      </div>
    </div>

    <!-- 查询条件 -->
    <DynamicFormWithApi
      ref="searchForm"
      config-name="companySearchForm"
      title="查询条件"
      @reset="handleReset"
      @submit="handleSearch"
    />

    <!-- 公司列表 -->
    <el-card class="table-card" shadow="never">
      <div slot="header" class="clearfix card-header">
        <span class="card-title">公司列表</span>
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

      <!-- 表格视图 -->
      <DynamicTable
        ref="companyTable"
        :columns="columns"
        :data-fetcher="fetchCompanyData"
        :auto-load="true"
        actions-fixed="right"
        actions-width="200px"
        border
        stripe
        class="company-table"
        @sort-change="handleTableSortChange"
      >
        <template #flag="{ row }">
          <el-tag v-if="row.flag===1" type="success">已激活</el-tag>
          <el-tag v-else-if="row.flag===2" type="danger">禁用</el-tag>
          <el-tag v-else-if="row.flag===0" type="info">已删除</el-tag>
          <el-tag v-else-if="row.flag===5" type="warning">系统</el-tag>
          <el-tag v-else type="info">未知</el-tag>
        </template>

        <template #actions="{ row }">
          <div class="action-buttons">
            <el-button
              size="mini"
              type="primary"
              plain
              icon="el-icon-edit"
              @click="handleEdit(row)"
              :disabled="row.flag === 0 || row.flag === 5"
            >编辑
            </el-button>
          </div>
        </template>
      </DynamicTable>
    </el-card>

    <!-- 新增/编辑公司弹窗 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="companyDialogVisible"
      width="600px"
      @close="handleCompanyDialogClose"
      :modal-append-to-body="false"
      class="company-dialog"
    >
      <el-form :model="companyForm" :rules="companyRules" ref="companyForm" label-width="100px">
        <el-form-item label="公司名称" prop="companyName">
          <el-input
            v-model="companyForm.companyName"
            placeholder="请输入公司名称"
          ></el-input>
        </el-form-item>
        <el-form-item label="公司税号" prop="taxNumber">
          <el-input
            v-model="companyForm.taxNumber"
            placeholder="请输入公司税号"
          ></el-input>
        </el-form-item>
        <el-form-item label="备注" prop="description">
          <el-input
            v-model="companyForm.description"
            type="textarea"
            placeholder="请输入备注"
          ></el-input>
        </el-form-item>
        <el-form-item label="状态" prop="flag">
          <el-select v-model="companyForm.flag" placeholder="请选择状态">
            <el-option label="已激活" :value="1"></el-option>
            <el-option label="禁用" :value="2"></el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="companyDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="handleCompanySubmit">确 定</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import DynamicTable from "@/components/DynamicTable/index.vue"
import DynamicFormWithApi from "@/components/DynamicForm/DynamicFormWithApi.vue"
import {activateCompany, adminQueryCompany, disableCompany, saveCompany} from "@/api/company"

export default {
  name: 'CompanyAdminView',
  components: {
    DynamicTable,
    DynamicFormWithApi
  },
  data() {
    return {
      companyDialogVisible: false,
      dialogMode: 'add', // 'add' 或 'edit'
      companyForm: {
        id: undefined,
        companyName: '',
        taxNumber: '',
        description: '',
        flag: 1
      },
      companyRules: {
        companyName: [
          {required: true, message: '请输入公司名称', trigger: 'blur'}
        ],
        taxNumber: [
          {required: true, message: '请输入公司税号', trigger: 'blur'}
        ],
        flag: [
          {required: true, message: '请选择状态', trigger: 'change'}
        ]
      },
      searchForm: {
        equalsId: '',
        likeName: '',
        equalsTaxNumber: '',
        flagEquals: undefined,
        createTimeRange: [],
        updateTimeRange: []
      },
      columns: [
        {prop: 'id', label: 'ID', width: 80},
        {prop: 'companyName', label: '公司名称', width: 200},
        {prop: 'taxNumber', label: '公司税号', width: 150},
        {prop: 'description', label: '备注', width: 250},
        {prop: 'flag', label: '状态', width: 100, slot: 'flag'},
        {prop: 'createTime', label: '创建时间', width: 180, sortable: 'custom'},
        {prop: 'updateTime', label: '更新时间', width: 180, sortable: 'custom'}
      ],
      // 当前排序参数
      sortParams: {}
    }
  },
  computed: {
    dialogTitle() {
      return this.dialogMode === 'add' ? '新增公司' : '编辑公司';
    }
  },
  methods: {
    /**
     * 获取公司数据（表格视图）
     */
    async fetchCompanyData(params) {
      try {
        // 构造查询条件，字段名称需要与后端CommonSystemCompanyQueryCriteria类匹配
        const criteria = {
          equalsId: this.searchForm.equalsId || undefined,
          likeName: this.searchForm.likeName || undefined,
          equalsTaxNumber: this.searchForm.equalsTaxNumber || undefined,
          flagEquals: this.searchForm.flagEquals
        }

        // 处理创建时间范围
        if (this.searchForm.createTimeRange && this.searchForm.createTimeRange.length === 2) {
          criteria.createTimeStart = this.searchForm.createTimeRange[0];
          criteria.createTimeEnd = this.searchForm.createTimeRange[1];
        }

        // 处理更新时间范围
        if (this.searchForm.updateTimeRange && this.searchForm.updateTimeRange.length === 2) {
          criteria.updateTimeStart = this.searchForm.updateTimeRange[0];
          criteria.updateTimeEnd = this.searchForm.updateTimeRange[1];
        }

        // 处理排序参数
        if (this.sortParams.prop && this.sortParams.order) {
          // 将Element UI的排序格式转换为后端需要的格式
          const order = this.sortParams.order === 'ascending' ? 'asc' : 'desc';
          // 根据排序字段映射到后端对应的排序字段
          if (this.sortParams.prop === 'createTime') {
            criteria.createTimeOrder = order;
          } else if (this.sortParams.prop === 'updateTime') {
            criteria.updateTimeOrder = order;
          }
        }

        // 移除值为undefined的属性
        Object.keys(criteria).forEach(key => {
          if (criteria[key] === undefined) {
            delete criteria[key]
          }
        })

        // 调用后端接口
        const response = await adminQueryCompany({
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
        console.error('获取公司数据失败:', error)
        this.$message.error('获取公司数据失败')
        return {
          data: [],
          total: 0
        }
      }
    },

    /**
     * 查询按钮点击事件
     */
    handleSearch(data) {
      this.sortParams = {}
      this.searchForm = data;
      console.log('公司查询条件:', data)
      this.$refs.companyTable.refresh()
    },

    /**
     * 重置按钮点击事件
     */
    handleReset() {
      this.searchForm = {
        equalsId: '',
        likeName: '',
        equalsTaxNumber: '',
        flagEquals: undefined,
        createTimeRange: [],
        updateTimeRange: []
      }
      this.sortParams = {}
      this.$refs.companyTable.refresh()
    },
    /**
     * 处理表格排序变化
     */
    handleTableSortChange(sortInfo) {
      console.log('sortInfo:', sortInfo)
      this.sortParams = sortInfo;
    },

    /**
     * 新增公司
     */
    handleAdd() {
      this.dialogMode = 'add';
      this.companyForm = {
        id: undefined,
        companyName: '',
        taxNumber: '',
        description: '',
        flag: 1
      };
      this.companyDialogVisible = true;
    },

    /**
     * 编辑公司
     */
    handleEdit(row) {
      this.dialogMode = 'edit';
      this.companyForm = {...row};
      this.companyDialogVisible = true;
    },

    /**
     * 切换公司状态
     */
    async handleToggleStatus(row) {
      // 系统公司和已删除公司不能操作
      if (row.flag === 0 || row.flag === 5) {
        this.$message.warning('该状态下的公司不能进行此操作');
        return;
      }

      try {
        let response;
        if (row.flag === 1) {
          // 当前是已激活状态，点击后应该禁用
          await this.$confirm('确认要禁用该公司吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          });
          response = await disableCompany(row.id);
        } else if (row.flag === 2) {
          // 当前是禁用状态，点击后应该激活
          await this.$confirm('确认要激活该公司吗？', '提示', {
            confirmButtonText: '确定',
            cancelButtonText: '取消',
            type: 'warning'
          });
          response = await activateCompany(row.id);
        }

        if (response.code === 200) {
          this.$message.success(response.message || '操作成功');
          // 刷新数据
          this.$refs.companyTable.refresh();
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
     * 处理公司弹窗关闭事件
     */
    handleCompanyDialogClose() {
      this.$refs.companyForm.resetFields();
    },

    /**
     * 处理公司提交事件
     */
    handleCompanySubmit() {
      this.$refs.companyForm.validate(async (valid) => {
        if (valid) {
          try {
            const response = await saveCompany(this.companyForm);

            if (response.code === 200) {
              this.$message.success(this.dialogMode === 'add' ? '公司创建成功' : '公司更新成功');
              this.companyDialogVisible = false;
              // 刷新数据
              this.$refs.companyTable.refresh();
            } else {
              this.$message.error(response.message || (this.dialogMode === 'add' ? '公司创建失败' : '公司更新失败'));
            }
          } catch (error) {
            console.error('保存公司失败:', error);
            this.$message.error('保存公司失败');
          }
        } else {
          console.log('表单验证失败');
          this.$message.warning('请填写正确的表单信息');
          return false;
        }
      });
    }
  }
}
</script>

<style lang="scss" scoped>
.company-management {
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

  .table-header-actions {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .company-table {
    ::v-deep .el-table th {
      background-color: #f5f7fa;
    }
  }

  .action-buttons {
    display: flex;
    gap: 8px;
    justify-content: center;
    flex-wrap: wrap;
  }

  .company-dialog {
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

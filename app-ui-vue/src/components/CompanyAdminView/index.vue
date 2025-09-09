<template>
  <div class="company-management">
    <quick-use-management-view
      ref="companyManagement"
      main-title="公司管理"
      config-name="companySearchForm"
      :search-handler="searchHandler"
      :columns="columns"
      add-form-config-name="companyAddForm"
      :add-handler="(formData) => saveCompany(formData)"
      edit-form-config-name="companyEditForm"
      :edit-handler="(formData) => saveCompany(formData)"
      :delete-handler="(row, index) => deleteCompany(row.id)"
      :delete-confirm-text="(row) => `确定要删除公司 ${row.companyName} 吗？`"
    />
  </div>
</template>

<script>
import QuickUseManagementView from '@/components/QuickUseManagementView/index.vue'
import { adminQueryCompany, deleteCompany, saveCompany } from '@/api/company'

export default {
  name: 'CompanyAdminView',
  components: {
    QuickUseManagementView
  },
  data() {
    return {
      columns: [
        {
          prop: 'id',
          label: '公司ID',
          width: 100
        },
        {
          prop: 'companyName',
          label: '公司名称',
          minWidth: 150
        },
        {
          prop: 'taxNumber',
          label: '税号',
          minWidth: 150
        },
        {
          prop: 'description',
          label: '描述',
          minWidth: 200
        },
        {
          prop: 'flag',
          label: '状态',
          width: 100,
          formatter: (row) => {
            const statusMap = {
              5: '系统',
              1: '激活',
              2: '禁用'
            }
            return statusMap[row.flag] || '未知'
          }
        },
        {
          prop: 'createTime',
          label: '创建时间',
          width: 180,
          sortable: true
        },
        {
          prop: 'updateTime',
          label: '更新时间',
          width: 180,
          sortable: true
        }
      ]
    }
  },
  methods: {
    saveCompany,
    deleteCompany,
    // 简化后的搜索处理函数，适配AbstractCrudController的数据格式
    async searchHandler(bodyParams, requestParams) {
      // 返回符合组件要求的数据格式
      return await adminQueryCompany(
        bodyParams, // 查询条件和排序作为body
        requestParams.pageNum, // 分页参数作为查询参数
        requestParams.pageSize
      )
    }
  }
}
</script>

<style lang="scss" scoped>
.company-management {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);
}
</style>

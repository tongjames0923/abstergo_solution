<template>
  <div class="dashboard-container">
    <quick-use-management-view
      ref="companyManagement"
      config-name="companySearchForm"
      :search-handler="searchHandler"
      :columns="columns"
      add-form-config-name="companyAddForm"
      :add-handler="(formData) => saveCompany(formData) "
      edit-form-config-name="companyEditForm"
      :edit-handler="(formData) => saveCompany(formData)"
      :delete-handler="(row, index) => deleteCompany(row.id)"
    >
    </quick-use-management-view>
  </div>
</template>

<script>
import {mapGetters} from 'vuex'
import QuickUseManagementView from "@/components/QuickUseManagementView/index.vue"
import {adminQueryCompany, deleteCompany, saveCompany} from "@/api/company"

export default {
  name: 'Dashboard',
  components: {QuickUseManagementView},
  computed: {
    ...mapGetters([
      'name'
    ])
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
    deleteCompany,
    saveCompany,
    // 简化后的搜索处理函数，适配AbstractCrudController的数据格式
    async searchHandler(bodyParams, requestParams) {


      // 返回符合组件要求的数据格式
      return await adminQueryCompany(
        bodyParams,  // 查询条件和排序作为body
        requestParams.pageNum,   // 分页参数作为查询参数
        requestParams.pageSize
      )
    },
  }
}
</script>

<template>
  <div class="dashboard-container">
    <quick-use-management-view
      ref="companyManagement"
      config-name="companySearchForm"
      :search-handler="searchHandler"
      :columns="columns"
      :actions="actions"
    >
    </quick-use-management-view>
  </div>
</template>

<script>
import {mapGetters} from 'vuex'
import QuickUseManagementView from "@/components/QuickUseManagementView/index.vue"
import {adminQueryCompany, deleteCompany} from "@/api/company"

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
              0: '禁用',
              1: '激活',
              2: '系统'
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
      ],
      actions: [
        {
          label: '编辑',
          type: 'primary',
          size: 'mini',
          handler: this.handleEdit
        },
        {
          label: '删除',
          type: 'danger',
          size: 'mini',
          handler: this.handleDelete
        }
      ]
    }
  },
  methods: {
    // 简化后的搜索处理函数，适配AbstractCrudController的数据格式
    async searchHandler(bodyParams, requestParams) {


      // 返回符合组件要求的数据格式
      return await adminQueryCompany(
        bodyParams,  // 查询条件和排序作为body
        requestParams.pageNum,   // 分页参数作为查询参数
        requestParams.pageSize
      )
    },

    // 编辑公司
    handleEdit(row) {
      this.$message.info(`编辑公司: ${row.companyName}`)
    },

    // 删除公司
    handleDelete(row) {
      this.$confirm(`确定要删除公司 "${row.companyName}" 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await deleteCompany(row.id)
          this.$message.success('删除成功')
          // 刷新表格数据
          this.$refs.companyManagement.refresh()
        } catch (error) {
          this.$message.error('删除失败: ' + (error.message || '未知错误'))
        }
      }).catch(() => {
        // 用户取消删除
      })
    }
  }
}
</script>

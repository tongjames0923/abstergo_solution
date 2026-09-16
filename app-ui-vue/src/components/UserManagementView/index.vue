<template>
  <div class="user-management">
    <quick-use-management-view
      ref="userManagement"
      main-title="用户管理"
      :config-name="userSearchFormKey()"
      :search-handler="searchHandler"
      :columns="columns"
      title-field="userName"
      :card-fields="['avatar', 'nickName', 'flag', 'createTime']"
      :add-form-config-name="userAddFormKey()"
      :add-handler="(formData) => saveUser(formData)"
      :edit-form-config-name="userEditFormKey()"
      :edit-handler="(formData) => saveUser(formData)"
      :query-transformer="rangeQueryTransform"
      @add-success="(data)=>{}"
      @edit-success="(data)=>{}"
    >
      <template v-slot:actions="{ row }">
        <el-button size="mini" type="default" plain @click="goUserDetail(row)">
          {{ $t('user.viewDetail') }}
        </el-button>
      </template>
    </quick-use-management-view>
  </div>
</template>

<script>
import QuickUseManagementView from '@/components/QuickUseManagementView/index.vue'
import { adminQueryUser, saveUser } from '@/api/admin_user'
import { rangeQueryTransform } from '@/utils/query-help'
import { userAddFormKey, userEditFormKey, userSearchFormKey } from '@/settings'

export default {
  name: 'UserManagementView',
  components: {
    QuickUseManagementView
  },
  data() {
    return {
      columns: [
        {
          prop: 'avatar',
          label: '头像',
          width: 120,
          render: (h, scope) => {
            return <el-avatar fit='fill' src={scope.row.avatar}>
              <i class='el-icon-warning-outline'></i>
            </el-avatar>
          }
        },
        { prop: 'userName', label: '用户名', width: 180 },
        { prop: 'nickName', label: '昵称', width: 180 },
        { prop: 'remark', label: '备注', minWidth: 200 },
        {
          prop: 'flag',
          label: '状态',
          width: 100,
          render: (h, scope) => {
            if (scope.row.flag === 1) {
              return <el-tag type='success'>正常</el-tag>
            } else if (scope.row.flag === 0) {
              return <el-tag type='danger'>禁用</el-tag>
            } else if (scope.row.flag === 2) {
              return <el-tag type='warning'>锁定</el-tag>
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
    userEditFormKey() {
      return userEditFormKey
    },
    userAddFormKey() {
      return userAddFormKey
    },
    userSearchFormKey() {
      return userSearchFormKey
    },

    saveUser,
    rangeQueryTransform,
    /**
     * 跳转用户详情页：按路由 name + params 跳转，与 URL 形态解耦
     */
    goUserDetail(row) {
      this.$router.push({ name: 'UserDetails', params: { userId: row.id }})
    },
    // 简化后的搜索处理函数，适配AbstractCrudController的数据格式
    async searchHandler(bodyParams, requestParams) {
      // 返回符合组件要求的数据格式
      return await adminQueryUser(
        bodyParams, // 查询条件和排序作为body
        requestParams.pageNum, // 分页参数作为查询参数
        requestParams.pageSize
      )
    }
  }
}
</script>

<style lang="scss" scoped>
.user-management {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);
}
</style>

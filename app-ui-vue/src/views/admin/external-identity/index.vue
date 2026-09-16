<template>
  <div class="app-container">
    <el-card shadow="never" class="identity-card">
      <el-form :inline="true" :model="queryForm" class="query-form" @submit.native.prevent>
        <el-form-item :label="$t('identity.identityType')">
          <el-select v-model="queryForm.identityType" clearable :placeholder="$t('identity.all')" style="width: 160px">
            <el-option :label="$t('identity.type.wecom')" value="wecom" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('identity.userName')">
          <el-input
            v-model.trim="queryForm.userName"
            clearable
            :placeholder="$t('identity.userNamePlaceholder')"
            style="width: 200px"
            @keyup.enter.native="handleSearch"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="el-icon-search" @click="handleSearch">{{ $t('identity.search') }}</el-button>
          <el-button icon="el-icon-refresh" @click="handleReset">{{ $t('identity.reset') }}</el-button>
        </el-form-item>
      </el-form>

      <div class="table-toolbar">
        <el-button type="primary" size="small" icon="el-icon-plus" @click="openBindDialog">
          {{ $t('identity.admin.addBind') }}
        </el-button>
      </div>

      <el-table v-loading="loading" :data="pageData" border>
        <el-table-column :label="$t('identity.identityType')" width="160" align="center">
          <template slot-scope="{ row }">{{ identityTypeLabel(row.identityType) }}</template>
        </el-table-column>
        <el-table-column prop="externalId" :label="$t('identity.externalId')" />
        <el-table-column prop="userId" :label="$t('identity.userId')" width="200" align="center" />
        <el-table-column :label="$t('identity.bindTime')" width="220" align="center">
          <template slot-scope="{ row }">{{ row.updateTime || row.createTime || '-' }}</template>
        </el-table-column>
        <el-table-column :label="$t('identity.actions')" width="120" align="center">
          <template slot-scope="{ row }">
            <el-button type="text" size="small" class="danger-text" @click="handleUnbind(row)">
              {{ $t('identity.unbind') }}
            </el-button>
          </template>
        </el-table-column>
        <template slot="empty">{{ $t('identity.noData') }}</template>
      </el-table>

      <el-pagination
        class="pager"
        layout="total, prev, pager, next, sizes"
        :total="total"
        :current-page.sync="queryForm.pageNum"
        :page-size.sync="queryForm.pageSize"
        :page-sizes="[10, 20, 50]"
        @current-change="loadPage"
        @size-change="handleSearch"
      />
    </el-card>

    <el-dialog :title="$t('identity.admin.addBind')" :visible.sync="bindDialogVisible" width="460px">
      <el-form ref="bindForm" :model="bindForm" :rules="bindRules" label-width="110px">
        <el-form-item :label="$t('identity.identityType')" prop="identityType">
          <el-select v-model="bindForm.identityType" style="width: 100%">
            <el-option :label="$t('identity.type.wecom')" value="wecom" />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('identity.externalId')" prop="externalId">
          <el-input v-model.trim="bindForm.externalId" />
        </el-form-item>
        <el-form-item :label="$t('identity.userName')" prop="userName">
          <el-input v-model.trim="bindForm.userName" />
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="bindDialogVisible = false">{{ $t('identity.cancel') }}</el-button>
        <el-button type="primary" :loading="submitting" @click="submitBind">{{ $t('identity.confirm') }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  pageExternalIdentities,
  bindExternalIdentity,
  unbindExternalIdentity
} from '@/api/external-identity'

const IDENTITY_TYPE_LABELS = {
  wecom: 'identity.type.wecom'
}

export default {
  name: 'ExternalIdentityManage',
  data() {
    return {
      loading: false,
      submitting: false,
      pageData: [],
      total: 0,
      queryForm: {
        identityType: '',
        userName: '',
        pageNum: 1,
        pageSize: 10
      },
      bindDialogVisible: false,
      bindForm: {
        identityType: 'wecom',
        externalId: '',
        userName: ''
      },
      bindRules: {
        identityType: [{ required: true, message: this.$t('identity.rules.identityType'), trigger: 'change' }],
        externalId: [{ required: true, message: this.$t('identity.rules.externalId'), trigger: 'blur' }],
        userName: [{ required: true, message: this.$t('identity.rules.userName'), trigger: 'blur' }]
      }
    }
  },
  created() {
    this.loadPage()
  },
  methods: {
    loadPage() {
      this.loading = true
      pageExternalIdentities({
        identityType: this.queryForm.identityType || undefined,
        userName: this.queryForm.userName || undefined,
        pageNum: this.queryForm.pageNum,
        pageSize: this.queryForm.pageSize
      }).then(res => {
        const page = (res && res.data) || {}
        this.pageData = page.data || []
        this.total = page.total || 0
      }).finally(() => {
        this.loading = false
      })
    },
    handleSearch() {
      this.queryForm.pageNum = 1
      this.loadPage()
    },
    handleReset() {
      this.queryForm.identityType = ''
      this.queryForm.userName = ''
      this.handleSearch()
    },
    identityTypeLabel(identityType) {
      const labelKey = IDENTITY_TYPE_LABELS[identityType]
      return labelKey ? this.$t(labelKey) : identityType
    },
    openBindDialog() {
      this.bindForm = { identityType: 'wecom', externalId: '', userName: '' }
      this.bindDialogVisible = true
      this.$nextTick(() => {
        if (this.$refs.bindForm) {
          this.$refs.bindForm.clearValidate()
        }
      })
    },
    submitBind() {
      this.$refs.bindForm.validate(valid => {
        if (!valid) {
          return
        }
        this.submitting = true
        bindExternalIdentity(this.bindForm).then(() => {
          this.$message.success(this.$t('identity.bindSuccess'))
          this.bindDialogVisible = false
          this.loadPage()
        }).finally(() => {
          this.submitting = false
        })
      })
    },
    handleUnbind(row) {
      this.$confirm(this.$t('identity.confirmUnbind'), this.$t('identity.title'), {
        type: 'warning'
      }).then(() => {
        unbindExternalIdentity(row.identityType, row.userId).then(() => {
          this.$message.success(this.$t('identity.unbindSuccess'))
          this.loadPage()
        })
      }).catch(() => {})
    }
  }
}
</script>

<style lang="scss" scoped>
.identity-card {
  border-radius: 12px;

  .query-form {
    margin-bottom: 4px;
  }

  .table-toolbar {
    display: flex;
    justify-content: flex-end;
    margin-bottom: 12px;
  }

  .danger-text {
    color: #f56c6c;
  }

  .pager {
    margin-top: 14px;
    text-align: right;
  }
}
</style>

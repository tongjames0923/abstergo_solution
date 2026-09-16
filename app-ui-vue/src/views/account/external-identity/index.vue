<template>
  <div class="app-container">
    <el-card shadow="never" class="identity-card">
      <div slot="header" class="card-header">
        <span class="card-title">{{ $t('identity.title') }}</span>
        <el-button
          v-if="!hasWecomBinding"
          type="primary"
          size="small"
          icon="el-icon-connection"
          :loading="binding"
          @click="startWecomBind"
        >{{ $t('identity.bindWecom') }}</el-button>
      </div>

      <el-table v-loading="loading" :data="bindings" border>
        <el-table-column :label="$t('identity.identityType')" width="160" align="center">
          <template slot-scope="{ row }">
            <el-tag size="small">{{ identityTypeLabel(row.identityType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="externalId" :label="$t('identity.externalId')" />
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

      <div class="bind-hint">
        <i class="el-icon-info" />
        <span>{{ $t('identity.autoBindHint') }}</span>
      </div>
    </el-card>
  </div>
</template>

<script>
import { myExternalIdentities, unbindExternalIdentity } from '@/api/external-identity'
import { buildWecomAuthorizeUrl } from '@/login/providers/wecom-sso-provider'

const IDENTITY_TYPE_LABELS = {
  wecom: 'identity.type.wecom'
}

export default {
  name: 'ExternalIdentityBind',
  data() {
    return {
      loading: false,
      binding: false,
      bindings: []
    }
  },
  computed: {
    hasWecomBinding() {
      return this.bindings.some(item => item.identityType === 'wecom')
    }
  },
  created() {
    this.loadBindings()
  },
  methods: {
    loadBindings() {
      this.loading = true
      myExternalIdentities().then(res => {
        this.bindings = (res && res.data) || []
      }).finally(() => {
        this.loading = false
      })
    },
    identityTypeLabel(identityType) {
      const labelKey = IDENTITY_TYPE_LABELS[identityType]
      return labelKey ? this.$t(labelKey) : identityType
    },
    startWecomBind() {
      const callbackUri = `${window.location.origin}/#/oauth/callback`
      const url = buildWecomAuthorizeUrl('wecom-bind', callbackUri)
      if (!url) {
        this.$message.error(this.$t('login.wecomNotConfigured'))
        return
      }
      this.binding = true
      window.location.href = url
    },
    handleUnbind(row) {
      this.$confirm(this.$t('identity.confirmUnbind'), this.$t('identity.title'), {
        type: 'warning'
      }).then(() => {
        unbindExternalIdentity(row.identityType, row.userId).then(() => {
          this.$message.success(this.$t('identity.unbindSuccess'))
          this.loadBindings()
        })
      }).catch(() => {})
    }
  }
}
</script>

<style lang="scss" scoped>
.identity-card {
  border-radius: 12px;

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;

    .card-title {
      font-size: 15px;
      font-weight: 600;
      color: #1d1d1f;
    }
  }

  .danger-text {
    color: #f56c6c;
  }

  .bind-hint {
    display: flex;
    align-items: center;
    gap: 6px;
    margin-top: 14px;
    font-size: 12px;
    color: #909399;

    i {
      color: #409eff;
    }
  }
}
</style>

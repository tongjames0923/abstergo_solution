<template>
  <div class="system-log-container">
    <!-- 页面标题 -->
    <el-card class="header-card">
      <div slot="header" class="card-header">
        <span>{{ $t('systemlog.title') }}</span>
        <el-radio-group v-model="currentView" size="mini">
          <el-radio-button label="report">{{ $t('systemlog.reportView') }}</el-radio-button>
          <el-radio-button label="record">{{ $t('systemlog.recordView') }}</el-radio-button>
        </el-radio-group>
      </div>
      <div class="header-content">
        <el-alert
          v-if="currentView === 'report'"
          :closable="false"
          :description="$t('systemlog.reportAlertDesc')"
          show-icon
          :title="$t('systemlog.reportAlertTitle')"
          type="info"
        />
        <el-alert
          v-else
          :closable="false"
          :description="$t('systemlog.recordAlertDesc')"
          show-icon
          :title="$t('systemlog.recordAlertTitle')"
          type="info"
        />
      </div>
    </el-card>

    <!-- 报表视图 -->
    <system-log-report v-if="currentView === 'report'" />

    <!-- 日志记录视图 -->
    <system-log-record v-else />
  </div>
</template>

<script>
import SystemLogReport from './SystemLogReport.vue'
import SystemLogRecord from './SystemLogRecord.vue'

export default {
  name: 'SystemLog',
  components: {
    SystemLogReport,
    SystemLogRecord
  },
  data() {
    return {
      // 视图切换: 'report' 或 'record'
      currentView: 'report'
    }
  }
}
</script>

<style lang="scss" scoped>
.system-log-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: calc(100vh - 84px);

  .header-card {
    margin-bottom: 20px;

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .header-content {
      margin-top: 15px;
    }
  }
}
</style>

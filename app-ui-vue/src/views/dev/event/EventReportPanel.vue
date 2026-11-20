<template>
  <div class="report-panel">
    <div class="panel-head">
      <span class="panel-title">{{ $t('eventCommon.debug') }}</span>
      <span class="panel-hint">{{ reporterId }} # {{ eventCode }}</span>
    </div>

    <event-param-form
      ref="paramForm"
      :param-types="paramTypes"
      label-width="96px"
      :empty-text="$t('eventCommon.noParams')"
    />

    <div class="report-bar">
      <el-button
        type="primary"
        size="small"
        icon="el-icon-s-promotion"
        :loading="reporting"
        :disabled="!reporterId || !eventCode"
        @click="handleReport"
      >
        {{ $t('eventCommon.report') }}
      </el-button>
      <el-button size="small" icon="el-icon-refresh-left" @click="handleReset">
        {{ $t('eventCommon.reset') }}
      </el-button>
      <span class="report-hint">{{ $t('eventCommon.reportHint') }}</span>
    </div>

    <div v-if="result" class="report-result">
      <div class="result-head">
        <el-tag size="mini" effect="dark" :type="result.success ? 'success' : 'danger'">
          {{ result.success ? $t('eventCommon.reportSuccess') : $t('eventCommon.reportFailed') }}
        </el-tag>
        <span class="result-meta">{{ $t('eventCommon.elapsed', { ms: result.elapsedMs }) }}</span>
        <span v-if="result.errorMsg" class="result-error">{{ result.errorMsg }}</span>
      </div>

      <div class="result-list">
        <div class="result-list-head">
          {{ $t('eventCommon.triggerResults') }}
          <span class="result-count">{{ triggerResults.length }}</span>
        </div>
        <div v-for="item in triggerResults" :key="item.triggerId" class="result-row">
          <el-tag size="mini" effect="plain" :type="statusType(item)">{{ statusText(item) }}</el-tag>
          <span class="result-trigger">{{ item.triggerName || item.triggerId }}</span>
          <span class="result-time">{{ item.elapsedMs }}ms</span>
          <span v-if="item.errorMsg" class="result-error">{{ item.errorMsg }}</span>
        </div>
        <div v-if="!triggerResults.length" class="result-empty">
          {{ $t('eventCommon.noTriggerResults') }}
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { reportEvent } from '@/api/eventSubscription'
import EventParamForm from './EventParamForm'

/**
 * 事件上报调试面板。
 * <p>
 * 按事件参数声明渲染模拟载荷，调用上报接口后以「结果条 + 触发器明细」呈现分发过程：
 * 命中为绿色、未命中为灰色、调用异常为红色，用于快速定位订阅与条件配置问题。
 * </p>
 */
export default {
  name: 'EventReportPanel',
  components: { EventParamForm },
  props: {
    reporterId: {
      type: String,
      default: ''
    },
    eventCode: {
      type: String,
      default: ''
    },
    paramTypes: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      reporting: false,
      result: null
    }
  },
  computed: {
    /**
     * 触发器执行明细
     *
     * @returns {Array} 触发器结果列表
     */
    triggerResults() {
      const dispatch = this.result && this.result.dispatch
      return dispatch && Array.isArray(dispatch.triggerResults) ? dispatch.triggerResults : []
    }
  },
  watch: {
    reporterId() {
      this.result = null
    },
    eventCode() {
      this.result = null
    }
  },
  methods: {
    /**
     * 上报模拟事件
     */
    async handleReport() {
      const form = this.$refs.paramForm
      if (form && !form.validate()) {
        return
      }
      this.reporting = true
      try {
        const res = await reportEvent({
          reporterId: this.reporterId,
          eventCode: this.eventCode,
          paramValues: form ? form.buildParamValues() : []
        })
        this.result = res.data || null
      } catch (e) {
        this.result = { success: false, elapsedMs: 0, errorMsg: e && e.message ? e.message : '' }
      } finally {
        this.reporting = false
      }
    },
    /**
     * 重置调试表单与结果
     */
    handleReset() {
      if (this.$refs.paramForm) {
        this.$refs.paramForm.reset()
      }
      this.result = null
    },
    /**
     * 触发器结果标签类型
     *
     * @param {Object} item 触发器结果
     * @returns {string} 标签类型
     */
    statusType(item) {
      if (!item.success) {
        return 'danger'
      }
      return item.triggered ? 'success' : 'info'
    },
    /**
     * 触发器结果文案
     *
     * @param {Object} item 触发器结果
     * @returns {string} 结果文案
     */
    statusText(item) {
      if (!item.success) {
        return this.$t('eventCommon.failed')
      }
      return item.triggered ? this.$t('eventCommon.triggered') : this.$t('eventCommon.notTriggered')
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~@/styles/variables.scss';
@import '~@/styles/mixin.scss';

.report-panel {
  @include responsive-container;

  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

.panel-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 8px;
  margin-bottom: 8px;
}

.panel-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.panel-hint {
  font-family: Menlo, Consolas, monospace;
  font-size: 11px;
  color: #909399;
}

.report-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.report-hint {
  font-size: 12px;
  color: #909399;
}

.report-result {
  margin-top: 10px;
  padding: 10px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  background: #fafafa;
}

.result-head {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.result-meta {
  font-size: 12px;
  color: #606266;
}

.result-error {
  font-size: 12px;
  color: #f56c6c;
}

.result-list {
  margin-top: 8px;
}

.result-list-head {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 600;
  color: #606266;
}

.result-count {
  padding: 0 6px;
  border-radius: 8px;
  background: #ebeef5;
  font-size: 11px;
  color: #909399;
}

.result-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  padding: 6px 0;
  border-bottom: 1px dashed #ebeef5;

  &:last-child {
    border-bottom: none;
  }
}

.result-trigger {
  font-size: 12px;
  color: #303133;
}

.result-time {
  font-family: Menlo, Consolas, monospace;
  font-size: 11px;
  color: #909399;
}

.result-empty {
  padding: 12px 0;
  font-size: 12px;
  color: #909399;
}
</style>

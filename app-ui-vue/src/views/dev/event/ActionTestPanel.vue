<template>
  <div class="action-test">
    <div class="panel-head">
      <span class="panel-title">{{ $t('eventCommon.test') }}</span>
      <el-select
        v-model="actionId"
        size="mini"
        filterable
        clearable
        class="test-select"
        :placeholder="$t('eventCommon.actionPlaceholder')"
        @change="handleActionChange"
      >
        <el-option
          v-for="item in actions"
          :key="item.actionId"
          :label="item.actionName || item.actionId"
          :value="item.actionId"
        />
      </el-select>
      <el-button
        type="primary"
        size="mini"
        icon="el-icon-caret-right"
        :loading="testing"
        :disabled="!actionId"
        @click="handleTest"
      >
        {{ $t('eventCommon.test') }}
      </el-button>
      <el-tag v-if="result" size="mini" effect="dark" :type="result.success ? 'success' : 'danger'">
        {{ result.success ? $t('eventCommon.testSuccess') : $t('eventCommon.testFailed') }}
        · {{ result.elapsedMs }}ms
      </el-tag>
      <span v-if="result && result.errorMsg" class="test-error">{{ result.errorMsg }}</span>
    </div>

    <event-param-form
      v-if="actionId"
      ref="paramForm"
      :param-types="paramTypes"
      label-width="104px"
      :empty-text="$t('eventCommon.paramEmpty')"
    />
    <div v-else class="test-empty">{{ $t('eventCommon.selectActionFirst') }}</div>
  </div>
</template>

<script>
import { testEventAction } from '@/api/eventAction'
import EventParamForm from './EventParamForm'

/**
 * 单动作测试面板。
 * <p>
 * 选择动作后按参数声明渲染表单并直接执行一次，用于在保存绑定前验证动作参数与外部依赖是否可用。
 * </p>
 */
export default {
  name: 'ActionTestPanel',
  components: { EventParamForm },
  props: {
    actions: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      actionId: '',
      testing: false,
      result: null
    }
  },
  computed: {
    /**
     * 当前动作参数声明
     *
     * @returns {Array} 参数定义列表
     */
    paramTypes() {
      const meta = this.actions.find(item => item.actionId === this.actionId)
      return meta && Array.isArray(meta.paramTypes) ? meta.paramTypes : []
    }
  },
  methods: {
    /**
     * 动作切换后清理结果
     */
    handleActionChange() {
      this.result = null
    },
    /**
     * 执行动作测试
     */
    async handleTest() {
      const form = this.$refs.paramForm
      if (form && !form.validate()) {
        return
      }
      this.testing = true
      try {
        const res = await testEventAction({
          actionId: this.actionId,
          paramValues: form ? form.buildParamValues() : []
        })
        this.result = res.data || null
      } catch (e) {
        this.result = { success: false, elapsedMs: 0, errorMsg: e && e.message ? e.message : '' }
      } finally {
        this.testing = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~@/styles/variables.scss';
@import '~@/styles/mixin.scss';

.action-test {
  @include responsive-container;

  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

.panel-head {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 8px;
}

.panel-title {
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.test-select {
  width: 220px;
}

.test-error {
  font-size: 12px;
  color: #f56c6c;
}

.test-empty {
  padding: 18px 0;
  text-align: center;
  font-size: 12px;
  color: #909399;
}
</style>

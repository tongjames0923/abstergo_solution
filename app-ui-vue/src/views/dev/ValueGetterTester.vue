<template>
  <div class="app-container value-getter-tester">
    <el-row :gutter="16">
      <el-col :xs="24" :sm="24" :md="7" :lg="6">
        <el-card class="getter-panel" shadow="never">
          <div slot="header" class="panel-header">
            <span>{{ $t('conditionLab.library') }}</span>
            <el-button type="text" icon="el-icon-refresh" :loading="loading" @click="fetchGetters">
              {{ $t('common.refresh') }}
            </el-button>
          </div>
          <el-input
            v-model="keyword"
            :placeholder="$t('conditionLab.searchPlaceholder')"
            clearable
            prefix-icon="el-icon-search"
            size="small"
            @input="debouncedFetch"
          />
          <div class="getter-list">
            <div v-if="loading" class="hint">{{ $t('common.loading') }}</div>
            <div v-else-if="!getterList.length" class="hint">{{ $t('conditionLab.noGetters') }}</div>
            <div
              v-for="g in getterList"
              :key="g.getterId"
              :class="['getter-item', { active: g.getterId === selectedGetterId }]"
              @click="selectGetter(g)"
            >
              <i :class="['getter-icon', isCondition(g) ? 'el-icon-s-operation' : 'el-icon-cpu']" />
              <div class="getter-body">
                <div class="getter-name">{{ g.getterName }}</div>
                <div class="getter-id">{{ g.getterId }}</div>
              </div>
              <el-tag v-if="isCondition(g)" size="mini" type="warning" effect="plain">
                {{ $t('conditionLab.conditionTag') }}
              </el-tag>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :xs="24" :sm="24" :md="17" :lg="18">
        <el-card shadow="never" class="work-card">
          <div slot="header" class="panel-header">
            <span v-if="currentGetter">
              {{ currentGetter.getterName }}
              <el-tag size="mini" type="info" effect="plain" class="getter-id-tag">{{ currentGetter.getterId }}</el-tag>
            </span>
            <span v-else>{{ $t('conditionLab.title') }}</span>
          </div>

          <div v-if="!currentGetter" class="empty-block">
            <i class="el-icon-menu empty-icon" />
            <p class="empty-title">{{ $t('conditionLab.emptyTitle') }}</p>
            <p class="empty-desc">{{ $t('conditionLab.emptyDesc') }}</p>
          </div>

          <template v-else>
            <p class="getter-desc">{{ currentGetter.description }}</p>
            <el-divider content-position="left">{{ $t('conditionLab.params') }}</el-divider>

            <condition-builder
              v-if="isCondition(currentGetter)"
              v-model="condition"
              :getters="getterList"
            />

            <el-form v-else label-width="140px" size="small" class="param-form">
              <el-form-item v-for="field in paramFields" :key="field.name" :label="field.name">
                <el-select
                  v-if="field.isEnum"
                  v-model="formData[field.name]"
                  :placeholder="field.description || $t('valueGetter.pleaseSelect')"
                  clearable
                  class="form-control"
                >
                  <el-option v-for="ev in field.enumValues" :key="ev.value" :label="ev.label || ev.value" :value="ev.value" />
                </el-select>
                <el-input-number
                  v-else-if="field.uiType === 'number'"
                  v-model="formData[field.name]"
                  controls-position="right"
                  class="form-control"
                />
                <el-date-picker
                  v-else-if="field.uiType === 'date'"
                  v-model="formData[field.name]"
                  type="datetime"
                  :value-format="field.dateFormat"
                  :placeholder="field.description || $t('valueGetter.pleaseSelect')"
                  class="form-control"
                />
                <el-input
                  v-else
                  v-model="formData[field.name]"
                  :placeholder="field.description || $t('valueGetter.pleaseInput')"
                  clearable
                />
                <div v-if="field.description" class="field-desc">{{ field.description }}</div>
              </el-form-item>
            </el-form>

            <div class="action-bar">
              <el-button type="primary" icon="el-icon-caret-right" :loading="submitting" @click="handleInvoke">
                {{ $t('conditionLab.execute') }}
              </el-button>
              <el-button icon="el-icon-refresh-left" @click="resetForm">{{ $t('common.reset') }}</el-button>
            </div>

            <el-divider content-position="left">{{ $t('conditionLab.output') }}</el-divider>
            <div class="result-block">
              <div v-if="resultError" class="result-error">
                <i class="el-icon-warning-outline" /> {{ resultError }}
              </div>
              <getter-result v-else :result="result" :elapsed-ms="elapsedMs" />
            </div>
          </template>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import {getValueGetters, invokeValueGetter} from '@/api/script/valueGetter'
import ConditionBuilder from '@/components/ConditionBuilder'
import GetterResult from '@/components/GetterResult'
import {buildFormData, toParamFields, toParamValues} from '@/utils/valueGetter'

const CONDITION_GETTER_ID = 'ConditionGetter'

/**
 * 值获取器调试页面。
 * <p>
 * 左侧展示可用的值获取器，右侧按 getter 类型渲染普通参数表单或条件构建器，
 * 并提供执行调用与结果展示。
 * </p>
 */
export default {
  name: 'ValueGetterTester',
  components: { ConditionBuilder, GetterResult },
  data() {
    return {
      keyword: '',
      loading: false,
      submitting: false,
      getterList: [],
      selectedGetterId: null,
      currentGetter: null,
      formData: {},
      condition: { left: '', op: 'eq', right: '' },
      result: null,
      resultError: '',
      elapsedMs: 0
    }
  },
  computed: {
    /**
     * 当前 getter 的参数字段描述
     *
     * @returns {Array} 字段描述列表
     */
    paramFields() {
      return this.currentGetter ? toParamFields(this.currentGetter.paramTypes, this.currentGetter.getterId) : []
    }
  },
  created() {
    this.debouncedFetch = this.debounce(() => this.fetchGetters(), 300)
    this.fetchGetters()
  },
  methods: {
    /**
     * 防抖包装
     *
     * @param {Function} fn 目标函数
     * @param {number} delay 延迟毫秒
     * @returns {Function} 防抖函数
     */
    debounce(fn, delay) {
      let timer = null
      return function(...args) {
        clearTimeout(timer)
        timer = setTimeout(() => fn.apply(this, args), delay)
      }
    },

    /**
     * 判断是否为布尔条件 getter
     *
     * @param {Object} getter getter 元信息
     * @returns {boolean} 是否为条件 getter
     */
    isCondition(getter) {
      return !!getter && getter.getterId === CONDITION_GETTER_ID
    },

    /**
     * 加载 getter 列表
     *
     * @returns {Promise} 请求 Promise
     */
    async fetchGetters() {
      this.loading = true
      try {
        const res = await getValueGetters({
          nameLike: this.keyword || undefined,
          idLike: this.keyword || undefined
        })
        this.getterList = (res && res.data) || []
        if (this.selectedGetterId && !this.getterList.some(g => g.getterId === this.selectedGetterId)) {
          this.selectedGetterId = null
          this.currentGetter = null
        }
      } catch (e) {
        this.getterList = []
      } finally {
        this.loading = false
      }
    },

    /**
     * 选中 getter
     *
     * @param {Object} getter getter 元信息
     */
    selectGetter(getter) {
      this.selectedGetterId = getter.getterId
      this.currentGetter = getter
      this.condition = { left: '', op: 'eq', right: '' }
      this.resetOutput()
      this.$nextTick(() => {
        this.formData = buildFormData(this.paramFields)
      })
    },

    /**
     * 清空输出区
     */
    resetOutput() {
      this.result = null
      this.resultError = ''
      this.elapsedMs = 0
    },

    /**
     * 重置参数
     */
    resetForm() {
      this.formData = buildFormData(this.paramFields)
      this.condition = { left: '', op: 'eq', right: '' }
      this.resetOutput()
    },

    /**
     * 构造调用参数
     *
     * @returns {Array} ParamValue 列表
     */
    buildParams() {
      if (this.isCondition(this.currentGetter)) {
        const operands = this.paramFields.filter(f => f.uiType === 'operand')
        const op = this.paramFields.find(f => f.isEnum)
        return [
          { type: 'operand', name: operands[0] ? operands[0].name : 'left', sort: 1, value: this.condition.left || null },
          { type: 'string', name: op ? op.name : 'op', sort: 2, value: this.condition.op || null },
          { type: 'operand', name: operands[1] ? operands[1].name : 'right', sort: 3, value: this.condition.right || null }
        ]
      }
      return toParamValues(this.paramFields, this.formData)
    },

    /**
     * 执行 getter 取值
     *
     * @returns {Promise} 请求 Promise
     */
    async handleInvoke() {
      if (!this.currentGetter || this.submitting) {
        return
      }
      this.submitting = true
      this.resultError = ''
      this.result = null
      const start = Date.now()
      try {
        const res = await invokeValueGetter({
          getterId: this.selectedGetterId,
          paramValues: this.buildParams()
        })
        this.result = (res && res.data) || null
        this.elapsedMs = Date.now() - start
      } catch (e) {
        this.resultError = (e && e.message) || this.$t('common.fail')
        this.elapsedMs = Date.now() - start
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~@/styles/variables.scss';
@import '~@/styles/mixin.scss';

.value-getter-tester {
  @include responsive-container;

  .panel-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .getter-id-tag {
    margin-left: 8px;
  }

  .getter-list {
    margin-top: 12px;
    max-height: calc(100vh - 260px);
    overflow-y: auto;

    @include respond-to(mobile) {
      max-height: 320px;
    }
  }

  .getter-item {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 12px;
    margin-bottom: 8px;
    border: 1px solid #ebeef5;
    border-radius: 4px;
    cursor: pointer;
    transition: all 0.15s ease;

    &:hover {
      border-color: #c0c4cc;
      background: #fafafa;
    }

    &.active {
      border-color: $menuActiveText;
      background: #ecf5ff;
    }
  }

  .getter-icon {
    font-size: 16px;
    color: #909399;

    .getter-item.active & {
      color: $menuActiveText;
    }
  }

  .getter-body {
    flex: 1;
    min-width: 0;
  }

  .getter-name {
    font-weight: 600;
    font-size: 14px;
    color: #303133;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .getter-id {
    font-size: 12px;
    color: #909399;
    margin-top: 2px;
    font-family: Menlo, Consolas, monospace;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .empty-block {
    padding: 48px 0;
    text-align: center;

    .empty-icon {
      font-size: 48px;
      color: #dcdfe6;
    }

    .empty-title {
      margin: 12px 0 4px;
      font-size: 15px;
      color: #606266;
    }

    .empty-desc {
      margin: 0;
      font-size: 13px;
      color: #909399;
    }
  }

  .getter-desc {
    margin: 0;
    font-size: 13px;
    color: #606266;
    line-height: 1.7;
  }

  .param-form {
    margin-top: 4px;

    .form-control {
      width: 100%;
    }

    .field-desc {
      font-size: 12px;
      color: #909399;
      line-height: 1.5;
    }
  }

  .action-bar {
    margin-top: 8px;
  }

  .result-error {
    padding: 10px 12px;
    color: #f56c6c;
    background: #fef0f0;
    border-radius: 4px;
    font-size: 13px;
  }

  .hint {
    color: #909399;
    font-size: 13px;
    padding: 8px 0;
  }
}
</style>

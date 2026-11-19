<template>
  <div class="value-getter-tester">
    <el-row :gutter="16">
      <!-- 左侧：值获取器列表 -->
      <el-col :span="7">
        <el-card class="getter-panel" shadow="never">
          <div slot="header" class="panel-header">
            <span>值获取器</span>
            <el-button type="text" icon="el-icon-refresh" @click="fetchGetters">刷新</el-button>
          </div>
          <el-input
            v-model="keyword"
            placeholder="按名称或 ID 搜索"
            clearable
            prefix-icon="el-icon-search"
            size="small"
            @input="debouncedFetch"
          />
          <div class="getter-list">
            <div v-if="loading" class="hint">加载中...</div>
            <div v-else-if="!getterList.length" class="hint">暂无可用的值获取器</div>
            <div
              v-for="g in getterList"
              :key="g.getterId"
              :class="['getter-item', { active: g.getterId === selectedGetterId }]"
              @click="selectGetter(g)"
            >
              <div class="getter-name">{{ g.getterName }}</div>
              <div class="getter-id">{{ g.getterId }}</div>
              <div class="getter-desc">{{ g.description }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧：参数 + 执行 -->
      <el-col :span="17">
        <el-card shadow="never">
          <div slot="header" class="panel-header">
            <span>
              {{ currentGetter ? currentGetter.getterName : '未选择值获取器' }}
              <el-tag v-if="currentGetter" size="mini" type="info" style="margin-left: 8px">
                {{ currentGetter.getterId }}
              </el-tag>
            </span>
          </div>

          <div v-if="!currentGetter" class="hint">请从左侧选择一个值获取器</div>

          <template v-else>
            <p class="getter-desc-large">{{ currentGetter.description }}</p>

            <el-form
              ref="paramForm"
              :model="formData"
              :rules="formRules"
              label-width="150px"
              size="small"
            >
              <el-form-item
                v-for="field in paramFields"
                :key="field.name"
                :prop="field.name"
              >
                <template slot="label">
                  {{ field.name }}
                  <span v-if="field.required" class="required-flag">*</span>
                </template>

                <!-- 枚举 -->
                <el-select
                  v-if="field.isEnum"
                  v-model="formData[field.name]"
                  :placeholder="field.description || '请选择'"
                  clearable
                  style="width: 100%"
                >
                  <el-option
                    v-for="ev in field.enumValues"
                    :key="ev.value"
                    :label="ev.label || ev.value"
                    :value="ev.value"
                  />
                </el-select>

                <!-- 数字 -->
                <el-input-number
                  v-else-if="field.uiType === 'number'"
                  v-model="formData[field.name]"
                  :placeholder="field.description || '请输入数字'"
                  style="width: 100%"
                />

                <!-- 日期 -->
                <el-date-picker
                  v-else-if="field.uiType === 'date'"
                  v-model="formData[field.name]"
                  type="datetime"
                  :value-format="field.dateFormat"
                  :placeholder="field.description || '请选择时间'"
                  style="width: 100%"
                />

                <!-- 嵌套 getter 操作数 -->
                <value-getter-operand
                  v-else-if="field.uiType === 'operand'"
                  v-model="formData[field.name]"
                  :getters="getterList"
                />

                <!-- 字符串（兜底） -->
                <el-input
                  v-else
                  v-model="formData[field.name]"
                  :placeholder="field.description || '请输入'"
                  clearable
                />
              </el-form-item>

              <el-form-item>
                <el-button type="primary" :loading="submitting" @click="handleInvoke">
                  执行
                </el-button>
                <el-button @click="resetForm">重置</el-button>
                <span class="type-hint">当前支持类型：string / number / date</span>
              </el-form-item>
            </el-form>

            <el-divider />

            <div class="result-header">
              <span>执行结果</span>
              <span v-if="result !== null && !resultError" class="result-meta">
                耗时 {{ elapsedMs }} ms
              </span>
            </div>
            <el-alert
              v-if="resultError"
              :title="resultError"
              type="error"
              :closable="false"
              show-icon
            />
            <pre v-else-if="result !== null" class="result-pre">{{ prettyResult }}</pre>
            <div v-else class="hint">点击「执行」后在此展示结果</div>
          </template>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>
import {getValueGetters, invokeValueGetter} from '@/api/script/valueGetter'
import ValueGetterOperand from '@/components/ValueGetterOperand'
import {buildFormData, toParamFields, toParamValues} from '@/utils/valueGetter'

export default {
  name: 'ValueGetterTester',
  components: { ValueGetterOperand },
  data() {
    return {
      keyword: '',
      loading: false,
      submitting: false,
      getterList: [],
      selectedGetterId: null,
      currentGetter: null,
      formData: {},
      result: null,
      resultError: '',
      elapsedMs: 0
    }
  },
  computed: {
    /**
     * 把 ParamType 转换成渲染描述：
     * - 按 sort 升序对齐，保证与后端位置顺序一致
     * - 推断 uiType / dateFormat
     */
    paramFields() {
      return this.currentGetter ? toParamFields(this.currentGetter.paramTypes, this.currentGetter.getterId) : []
    },
    formRules() {
      const rules = {}
      this.paramFields.forEach(f => {
        if (f.required) {
          rules[f.name] = [
            {
              required: true,
              message: `请输入${f.name}`,
              trigger: f.isEnum || f.uiType === 'date' ? 'change' : 'blur'
            }
          ]
        }
      })
      return rules
    },
    prettyResult() {
      if (this.result === null) {
        return ''
      }
      try {
        return JSON.stringify(this.result, null, 2)
      } catch (e) {
        return String(this.result)
      }
    }
  },
  created() {
    this.debouncedFetch = this.debounce(() => this.fetchGetters(), 300)
    this.fetchGetters()
  },
  methods: {
    debounce(fn, delay) {
      let timer = null
      return function(...args) {
        clearTimeout(timer)
        timer = setTimeout(() => fn.apply(this, args), delay)
      }
    },

    async fetchGetters() {
      this.loading = true
      try {
        const res = await getValueGetters({
          nameLike: this.keyword || undefined,
          idLike: this.keyword || undefined
        })
        this.getterList = (res && res.data) || []
        // 当前选中的 getter 不在列表中则清空
        if (
          this.selectedGetterId &&
          !this.getterList.some(g => g.getterId === this.selectedGetterId)
        ) {
          this.selectedGetterId = null
          this.currentGetter = null
        }
      } catch (e) {
        this.getterList = []
      } finally {
        this.loading = false
      }
    },

    selectGetter(getter) {
      this.selectedGetterId = getter.getterId
      this.currentGetter = getter
      this.result = null
      this.resultError = ''
      this.elapsedMs = 0
      this.$nextTick(() => {
        this.formData = this.buildFormData()
        if (this.$refs.paramForm) {
          this.$refs.paramForm.clearValidate()
        }
      })
    },

    /**
     * 根据字段类型初始化表单默认值，避免 el-input-number 绑定 undefined 报错
     */
    buildFormData() {
      return buildFormData(this.paramFields)
    },

    resetForm() {
      this.formData = this.buildFormData()
      this.result = null
      this.resultError = ''
      this.elapsedMs = 0
      this.$nextTick(() => {
        if (this.$refs.paramForm) {
          this.$refs.paramForm.clearValidate()
        }
      })
    },

    handleInvoke() {
      if (!this.$refs.paramForm) {
        return
      }
      this.$refs.paramForm.validate(async valid => {
        if (!valid) {
          return
        }
        this.submitting = true
        this.resultError = ''
        this.result = null
        const start = Date.now()
        try {
          const paramValues = toParamValues(this.paramFields, this.formData)
          const res = await invokeValueGetter({
            getterId: this.selectedGetterId,
            paramValues
          })
          this.result = (res && res.data) || null
          this.elapsedMs = Date.now() - start
        } catch (e) {
          // 业务错误已由 request 拦截器统一 Message 提示，这里只做局部展示
          this.resultError = (e && e.message) || '执行失败'
          this.elapsedMs = Date.now() - start
        } finally {
          this.submitting = false
        }
      })
    }
  }
}
</script>

<style scoped>
.value-getter-tester {
  padding: 16px;
}

.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.getter-list {
  margin-top: 12px;
  max-height: calc(100vh - 260px);
  overflow-y: auto;
}

.getter-item {
  padding: 10px 12px;
  margin-bottom: 8px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.15s ease;
}

.getter-item:hover {
  border-color: #c0c4cc;
  background: #fafafa;
}

.getter-item.active {
  border-color: #409eff;
  background: #ecf5ff;
}

.getter-name {
  font-weight: 600;
  font-size: 14px;
  color: #303133;
}

.getter-id {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
  font-family: Menlo, Consolas, monospace;
}

.getter-desc {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.getter-desc-large {
  font-size: 13px;
  color: #606266;
  margin: 0 0 12px 0;
  line-height: 1.6;
}

.required-flag {
  color: #f56c6c;
  margin-left: 2px;
}

.type-hint {
  margin-left: 12px;
  font-size: 12px;
  color: #909399;
}

.result-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.result-meta {
  font-size: 12px;
  color: #909399;
  font-weight: normal;
}

.result-pre {
  margin: 0;
  padding: 12px;
  background: #1e1e1e;
  color: #d4d4d4;
  border-radius: 4px;
  font-family: Menlo, Consolas, monospace;
  font-size: 13px;
  line-height: 1.6;
  max-height: 420px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-all;
}

.hint {
  color: #909399;
  font-size: 13px;
  padding: 8px 0;
}
</style>

<template>
  <div class="vg-operand">
    <div class="vg-operand-mode">
      <el-radio-group v-model="mode" size="mini" @change="handleModeChange">
        <el-radio-button label="literal">{{ $t('valueGetter.literal') }}</el-radio-button>
        <el-radio-button label="getter">{{ $t('valueGetter.getterCall') }}</el-radio-button>
      </el-radio-group>
      <span v-if="depth > 0" class="vg-operand-depth">{{ $t('valueGetter.nestedDepth', { depth }) }}</span>
    </div>

    <el-input
      v-if="mode === 'literal'"
      :value="literalValue"
      :placeholder="$t('valueGetter.literalPlaceholder')"
      size="small"
      clearable
      @input="handleLiteralInput"
    />

    <div v-else class="vg-operand-getter">
      <el-select
        v-model="selectedGetterId"
        :placeholder="$t('valueGetter.selectGetter')"
        size="small"
        filterable
        style="width: 100%"
        @change="handleGetterChange"
      >
        <el-option
          v-for="g in availableGetters"
          :key="g.getterId"
          :label="g.getterName + ' (' + g.getterId + ')'"
          :value="g.getterId"
        />
      </el-select>

      <div v-if="selectedGetter && paramFields.length" class="vg-operand-params">
        <div v-for="field in paramFields" :key="field.name" class="vg-operand-param">
          <div class="vg-operand-param-label">
            <span>{{ field.name }}</span>
            <span v-if="field.required" class="vg-operand-required">*</span>
          </div>
          <div v-if="field.description" class="vg-operand-param-desc">{{ field.description }}</div>

          <value-getter-operand
            v-if="field.uiType === 'operand'"
            :value="formData[field.name]"
            :getters="getters"
            :depth="depth + 1"
            @input="val => setFieldValue(field.name, val)"
          />

          <el-select
            v-else-if="field.isEnum"
            :value="formData[field.name]"
            :placeholder="field.description || $t('valueGetter.pleaseSelect')"
            size="small"
            clearable
            style="width: 100%"
            @change="val => setFieldValue(field.name, val)"
          >
            <el-option
              v-for="ev in field.enumValues"
              :key="ev.value"
              :label="ev.label || ev.value"
              :value="ev.value"
            />
          </el-select>

          <el-input-number
            v-else-if="field.uiType === 'number'"
            :value="formData[field.name]"
            :placeholder="field.description || $t('valueGetter.pleaseInputNumber')"
            size="small"
            controls-position="right"
            style="width: 100%"
            @change="val => setFieldValue(field.name, val)"
          />

          <el-date-picker
            v-else-if="field.uiType === 'date'"
            :value="formData[field.name]"
            :type="field.type === 'time' ? 'time' : 'datetime'"
            :value-format="field.dateFormat"
            :placeholder="field.description || $t('valueGetter.pleaseSelect')"
            size="small"
            style="width: 100%"
            @change="val => setFieldValue(field.name, val)"
          />

          <el-input
            v-else
            :value="formData[field.name]"
            :placeholder="field.description || $t('valueGetter.pleaseInput')"
            size="small"
            clearable
            @input="val => setFieldValue(field.name, val)"
          />
        </div>
      </div>
      <div v-else-if="selectedGetterId && !selectedGetter" class="vg-operand-missing">
        {{ $t('valueGetter.getterNotFound', { id: selectedGetterId }) }}
      </div>
    </div>
  </div>
</template>

<script>
import {
  buildFormData,
  parseGetterOperand,
  stringifyGetterOperand,
  toParamFields,
  toParamValues
} from '@/utils/valueGetter'

/**
 * 嵌套 getter 操作数编辑器。
 * <p>
 * 支持字面量与 getter 调用两种模式；选择 getter 后按元信息递归渲染其参数，
 * 因此 ConditionGetter 的左右操作数可以继续嵌套 ConditionGetter 构建复合条件。
 * </p>
 */
export default {
  name: 'ValueGetterOperand',
  props: {
    value: {
      type: String,
      default: ''
    },
    getters: {
      type: Array,
      default: () => []
    },
    depth: {
      type: Number,
      default: 0
    }
  },
  data() {
    return {
      mode: 'literal',
      literalValue: '',
      selectedGetterId: '',
      formData: {},
      pendingParams: null,
      emitting: false
    }
  },
  computed: {
    maxDepth() {
      return 6
    },
    availableGetters() {
      if (this.depth >= this.maxDepth) {
        return this.getters.filter(g => g.getterId !== 'ConditionGetter')
      }
      return this.getters
    },
    selectedGetter() {
      if (!this.selectedGetterId) {
        return null
      }
      return this.getters.find(g => g.getterId === this.selectedGetterId) || null
    },
    paramFields() {
      return this.selectedGetter ? toParamFields(this.selectedGetter.paramTypes, this.selectedGetter.getterId) : []
    },
    serialized() {
      return this.mode === 'getter'
        ? stringifyGetterOperand(this.selectedGetterId, toParamValues(this.paramFields, this.formData))
        : this.literalValue
    }
  },
  watch: {
    value: {
      immediate: true,
      handler(val) {
        this.applyValue(val)
      }
    },
    getters() {
      if (this.mode === 'getter' && this.selectedGetterId && this.pendingParams) {
        this.initFormData(this.pendingParams)
        this.pendingParams = null
      }
    },
    serialized(val) {
      if (this.emitting) {
        return
      }
      this.emitting = true
      this.$emit('input', val)
      this.$nextTick(() => {
        this.emitting = false
      })
    }
  },
  methods: {
    /**
     * 应用外部传入的操作数字符串
     *
     * @param {string} val 字面量或 getter 调用 JSON
     */
    applyValue(val) {
      const parsed = parseGetterOperand(val)
      if (parsed) {
        this.mode = 'getter'
        this.selectedGetterId = parsed.getterId
        if (this.selectedGetter) {
          this.initFormData(parsed.paramValues)
        } else {
          this.pendingParams = parsed.paramValues
        }
        return
      }
      this.mode = 'literal'
      this.literalValue = val == null ? '' : String(val)
    },

    /**
     * 按 getter 元信息初始化参数表单
     *
     * @param {Array} paramValues 已有参数值（按 sort 或下标对齐）
     */
    initFormData(paramValues) {
      const data = buildFormData(this.paramFields)
      const values = Array.isArray(paramValues) ? paramValues : []
      this.paramFields.forEach((field, index) => {
        const found = values.find(p => p && p.name === field.name) || values[index]
        if (found && found.value !== undefined && found.value !== null) {
          data[field.name] = found.value
        }
      })
      this.formData = data
    },

    /**
     * 切换模式时重置另一侧数据并对外发出结果
     *
     * @param {string} mode 新模式
     */
    handleModeChange(mode) {
      if (mode === 'literal') {
        this.selectedGetterId = ''
        this.formData = {}
      } else {
        this.literalValue = ''
      }
      this.emitValue()
    },

    /**
     * 选择 getter 后重置参数表单
     *
     * @param {string} getterId getter ID
     */
    handleGetterChange(getterId) {
      const getter = this.getters.find(g => g.getterId === getterId)
      this.formData = buildFormData(getter ? toParamFields(getter.paramTypes) : [])
      this.emitValue()
    },

    /**
     * 设置单个参数值
     *
     * @param {string} name 参数名
     * @param {*} val 参数值
     */
    setFieldValue(name, val) {
      this.$set(this.formData, name, val)
      this.emitValue()
    },

    /**
     * 输入字面量
     *
     * @param {string} val 字面量内容
     */
    handleLiteralInput(val) {
      this.literalValue = val
      this.emitValue()
    },

    /**
     * 对外发出序列化后的操作数
     */
    emitValue() {
      this.emitting = true
      this.$emit('input', this.serialized)
      this.$nextTick(() => {
        this.emitting = false
      })
    }
  }
}
</script>

<style scoped>
.vg-operand {
  width: 100%;
}

.vg-operand-mode {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.vg-operand-depth {
  font-size: 12px;
  color: #909399;
}

.vg-operand-getter {
  padding: 8px;
  border: 1px dashed #dcdfe6;
  border-radius: 4px;
  background: #fafafa;
}

.vg-operand-params {
  margin-top: 8px;
}

.vg-operand-param {
  padding: 6px 0;
  border-top: 1px solid #ebeef5;
}

.vg-operand-param:first-child {
  border-top: none;
}

.vg-operand-param-label {
  font-size: 13px;
  color: #606266;
  margin-bottom: 4px;
}

.vg-operand-required {
  color: #f56c6c;
  margin-left: 2px;
}

.vg-operand-param-desc {
  font-size: 12px;
  color: #909399;
  margin-bottom: 6px;
  line-height: 1.4;
}

.vg-operand-missing {
  font-size: 12px;
  color: #f56c6c;
  margin-top: 6px;
}
</style>

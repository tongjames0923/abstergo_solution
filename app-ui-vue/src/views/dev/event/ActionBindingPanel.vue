<template>
  <div class="action-binding">
    <div class="rows-head">
      <span class="rows-title">{{ $t('eventCommon.actions') }}</span>
      <el-button type="text" size="mini" icon="el-icon-plus" @click="addRow">
        {{ $t('eventCommon.addRow') }}
      </el-button>
    </div>

    <div v-if="!value.length" class="rows-empty">{{ $t('eventCommon.noActions') }}</div>

    <div v-for="(row, index) in value" :key="row.uid" class="action-card">
      <div class="action-head">
        <el-select
          v-model="row.actionId"
          size="mini"
          filterable
          class="action-select"
          :placeholder="$t('eventCommon.actionPlaceholder')"
          @change="handleActionChange(row)"
        >
          <el-option
            v-for="item in actions"
            :key="item.actionId"
            :label="item.actionName || item.actionId"
            :value="item.actionId"
          />
        </el-select>
        <el-input-number
          v-model="row.sortOrder"
          size="mini"
          :min="0"
          :controls="false"
          class="order-input"
          @change="emitChange"
        />
        <el-switch v-model="row.enabled" :active-value="1" :inactive-value="0" @change="emitChange" />
        <el-button type="text" size="mini" class="danger" icon="el-icon-delete" @click="removeRow(index)" />
      </div>

      <p v-if="metaOf(row.actionId)" class="action-desc">{{ metaOf(row.actionId).actionDesc }}</p>

      <event-param-form
        :ref="'form-' + index"
        :param-types="paramTypesOf(row.actionId)"
        label-width="104px"
        :empty-text="$t('eventCommon.paramEmpty')"
      />
    </div>
  </div>
</template>

<script>
import EventParamForm from './EventParamForm'

/**
 * 动作绑定编排面板。
 * <p>
 * 每个动作行独立渲染参数表单：动作切换时按新的参数声明重建表单，
 * 编辑已有绑定时用已保存的 paramJson 回填；提交前统一收集参数并序列化。
 * </p>
 */
export default {
  name: 'ActionBindingPanel',
  components: { EventParamForm },
  props: {
    value: {
      type: Array,
      default: () => []
    },
    actions: {
      type: Array,
      default: () => []
    }
  },
  data() {
    return {
      uidSeed: 0
    }
  },
  mounted() {
    this.$nextTick(this.syncForms)
  },
  methods: {
    /**
     * 新增动作行
     */
    addRow() {
      this.uidSeed += 1
      this.emitChange([...this.value, {
        uid: 'action-' + this.uidSeed,
        actionId: '',
        actionName: '',
        paramJson: '',
        sortOrder: this.value.length,
        enabled: 1
      }])
    },
    /**
     * 删除动作行
     *
     * @param {number} index 行下标
     */
    removeRow(index) {
      this.emitChange(this.value.filter((item, idx) => idx !== index))
    },
    /**
     * 动作切换后清空已填参数
     *
     * @param {Object} row 动作行
     */
    handleActionChange(row) {
      row.paramJson = ''
      const meta = this.metaOf(row.actionId)
      row.actionName = meta ? meta.actionName : ''
      this.emitChange()
      this.$nextTick(() => {
        const index = this.value.indexOf(row)
        const form = this.$refs['form-' + index]
        if (form && form[0]) {
          form[0].reset()
        }
      })
    },
    /**
     * 向外发出变更后的副本
     *
     * @param {Array} rows 新行列表
     */
    emitChange(rows) {
      this.$emit('input', (rows || this.value).map(item => ({ ...item })))
    },
    /**
     * 查询动作元信息
     *
     * @param {string} actionId 动作ID
     * @returns {Object|null} 动作元信息
     */
    metaOf(actionId) {
      return this.actions.find(item => item.actionId === actionId) || null
    },
    /**
     * 查询动作参数声明
     *
     * @param {string} actionId 动作ID
     * @returns {Array} 参数定义列表
     */
    paramTypesOf(actionId) {
      const meta = this.metaOf(actionId)
      return meta && Array.isArray(meta.paramTypes) ? meta.paramTypes : []
    },
    /**
     * 用已保存的参数回填各行表单
     */
    syncForms() {
      this.value.forEach((row, index) => {
        const form = this.$refs['form-' + index]
        if (!form || !form[0] || !row.paramJson) {
          return
        }
        try {
          form[0].setValues(JSON.parse(row.paramJson))
        } catch (e) {
          form[0].setValues([])
        }
      })
    },
    /**
     * 收集动作绑定，序列化参数
     *
     * @returns {Array} 动作绑定列表
     */
    collect() {
      return this.value.map((row, index) => {
        const form = this.$refs['form-' + index]
        const paramValues = form && form[0] ? form[0].buildParamValues() : []
        return {
          id: row.id || null,
          actionId: row.actionId,
          actionName: row.actionName,
          paramJson: JSON.stringify(paramValues),
          sortOrder: row.sortOrder,
          enabled: row.enabled
        }
      })
    },
    /**
     * 校验动作编排
     *
     * @returns {{valid: boolean, message: string}} 校验结果
     */
    validate() {
      if (!this.value.length) {
        return { valid: false, message: this.$t('eventCommon.actionRequired') }
      }
      const actionIds = this.value.map(row => row.actionId)
      if (actionIds.some(id => !id)) {
        return { valid: false, message: this.$t('eventCommon.actionRequired') }
      }
      if (new Set(actionIds).size !== actionIds.length) {
        return { valid: false, message: this.$t('eventCommon.actionDuplicate') }
      }
      for (let index = 0; index < this.value.length; index++) {
        const form = this.$refs['form-' + index]
        if (form && form[0] && !form[0].validate()) {
          return { valid: false, message: this.$t('eventCommon.actionParamInvalid') }
        }
      }
      return { valid: true, message: '' }
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~@/styles/variables.scss';
@import '~@/styles/mixin.scss';

.action-binding {
  @include responsive-container;
}

.rows-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}

.rows-title {
  font-size: 13px;
  font-weight: 600;
  color: #303133;
}

.action-card {
  margin-bottom: 8px;
  padding: 8px 10px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  transition: border-color 0.18s ease, box-shadow 0.18s ease;

  &:hover {
    border-color: #c0c4cc;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  }
}

.action-head {
  display: flex;
  align-items: center;
  gap: 8px;
}

.action-select {
  flex: 1;
  min-width: 180px;
}

.order-input {
  width: 72px;
}

.action-desc {
  margin: 4px 0 6px;
  font-size: 12px;
  line-height: 1.5;
  color: #909399;
}

.danger {
  color: #f56c6c;
}

.rows-empty {
  padding: 20px 0;
  text-align: center;
  font-size: 12px;
  color: #909399;
  border: 1px dashed #ebeef5;
  border-radius: 4px;
}
</style>

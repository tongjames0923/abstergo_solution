<template>
  <div class="binding-rows">
    <div class="rows-head">
      <span class="rows-title">{{ $t('eventCommon.bindings') }}</span>
      <el-button type="text" size="mini" icon="el-icon-plus" @click="addRow">
        {{ $t('eventCommon.addRow') }}
      </el-button>
    </div>

    <el-table v-if="value.length" :data="value" size="mini" border class="rows-table">
      <el-table-column :label="$t('eventCommon.trigger')" min-width="190">
        <template slot-scope="{row}">
          <el-select
            v-model="row.triggerId"
            size="mini"
            filterable
            class="row-control"
            :placeholder="$t('eventCommon.triggerPlaceholder')"
            @change="emitChange"
          >
            <el-option
              v-for="item in triggers"
              :key="item.triggerId"
              :label="item.triggerName || item.triggerId"
              :value="item.triggerId"
            />
          </el-select>
          <div v-if="itemOf(row.triggerId)" class="row-hint">{{ itemOf(row.triggerId).description }}</div>
        </template>
      </el-table-column>

      <el-table-column :label="$t('eventCommon.conditionRule')" min-width="230">
        <template slot-scope="{row}">
          <el-select
            v-model="row.ruleCode"
            size="mini"
            filterable
            clearable
            class="row-control"
            :placeholder="$t('eventCommon.noConditionRule')"
            @change="emitChange"
          >
            <el-option
              v-for="rule in rules"
              :key="rule.ruleCode"
              :label="rule.ruleCode + ' · ' + rule.ruleName"
              :value="rule.ruleCode"
            />
          </el-select>
        </template>
      </el-table-column>

      <el-table-column :label="$t('eventCommon.order')" width="96" align="center">
        <template slot-scope="{row}">
          <el-input-number
            v-model="row.sortOrder"
            size="mini"
            :min="0"
            :controls="false"
            class="order-input"
            @change="emitChange"
          />
        </template>
      </el-table-column>

      <el-table-column :label="$t('eventCommon.status')" width="70" align="center">
        <template slot-scope="{row}">
          <el-switch v-model="row.enabled" :active-value="1" :inactive-value="0" @change="emitChange" />
        </template>
      </el-table-column>

      <el-table-column width="52" align="center">
        <template slot-scope="{ $index }">
          <el-button type="text" size="mini" class="danger" icon="el-icon-delete" @click="removeRow($index)" />
        </template>
      </el-table-column>
    </el-table>

    <div v-else class="rows-empty">{{ $t('eventCommon.noBindings') }}</div>
  </div>
</template>

<script>
/**
 * 订阅绑定行编辑器。
 * <p>
 * 以行列表方式维护「触发器 + 条件规则 + 顺序 + 启停」，
 * 触发器与条件规则均来自后端元数据，避免手工输入编码带来的不可用绑定。
 * </p>
 */
export default {
  name: 'SubscriptionRows',
  props: {
    value: {
      type: Array,
      default: () => []
    },
    triggers: {
      type: Array,
      default: () => []
    },
    rules: {
      type: Array,
      default: () => []
    }
  },
  methods: {
    /**
     * 新增一行绑定
     */
    addRow() {
      const next = [...this.value, { triggerId: '', ruleCode: '', sortOrder: this.value.length, enabled: 1 }]
      this.$emit('input', next)
    },
    /**
     * 删除指定行
     *
     * @param {number} index 行下标
     */
    removeRow(index) {
      const next = this.value.filter((item, idx) => idx !== index)
      this.$emit('input', next)
    },
    /**
     * 向外发出变更后的副本
     */
    emitChange() {
      this.$emit('input', this.value.map(item => ({ ...item })))
    },
    /**
     * 查询触发器元信息
     *
     * @param {string} triggerId 触发器ID
     * @returns {Object|null} 触发器元信息
     */
    itemOf(triggerId) {
      return this.triggers.find(item => item.triggerId === triggerId) || null
    },
    /**
     * 校验绑定配置
     *
     * @returns {{valid: boolean, message: string}} 校验结果
     */
    validate() {
      if (!this.value.length) {
        return { valid: false, message: this.$t('eventCommon.bindingRequired') }
      }
      const triggerIds = this.value.map(item => item.triggerId)
      if (triggerIds.some(id => !id)) {
        return { valid: false, message: this.$t('eventCommon.triggerRequired') }
      }
      if (new Set(triggerIds).size !== triggerIds.length) {
        return { valid: false, message: this.$t('eventCommon.triggerDuplicate') }
      }
      return { valid: true, message: '' }
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~@/styles/variables.scss';
@import '~@/styles/mixin.scss';

.binding-rows {
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

.row-control {
  width: 100%;
}

.row-hint {
  margin-top: 2px;
  font-size: 11px;
  line-height: 1.4;
  color: #909399;
}

.order-input {
  width: 100%;
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

<template>
  <div :class="['logic-condition-builder', { 'is-nested': depth > 0 }]">
    <div class="lc-group-header">
      <span class="lc-prefix">{{ $t('logicCondition.groupPrefix') }}</span>
      <el-select
        :value="value.op"
        size="mini"
        class="lc-op-select"
        @change="setOp"
      >
        <el-option
          :label="$t('logicCondition.all')"
          value="and"
        />
        <el-option
          :label="$t('logicCondition.any')"
          value="or"
        />
      </el-select>
      <span class="lc-prefix">{{ $t('logicCondition.groupSuffix') }}</span>
      <el-button
        v-if="depth > 0"
        type="text"
        size="mini"
        class="lc-group-remove"
        @click="$emit('remove')"
      >
        {{ $t('logicCondition.removeGroup') }}
      </el-button>
    </div>

    <div class="lc-items">
      <div
        v-for="(child, index) in value.children"
        :key="child.id"
        class="lc-item"
      >
        <span class="lc-index">{{ index + 1 }}</span>
        <div class="lc-item-content">
          <component
            :is="operandComponent"
            v-if="child.type === 'condition' && operandComponent"
            :value="child.value"
            :getters="getters"
            :depth="depth + 1"
            @input="setChild(child.id, { value: $event })"
          />
          <el-input
            v-else-if="child.type === 'condition'"
            :value="child.value"
            size="small"
            :placeholder="$t('logicCondition.itemPlaceholder')"
            @input="setChild(child.id, { value: $event })"
          />
          <logic-condition-builder
            v-else
            :value="child"
            :getters="getters"
            :operand-component="operandComponent"
            :depth="depth + 1"
            @input="setChild(child.id, $event)"
            @remove="removeChild(child)"
          />
        </div>
        <el-button
          type="text"
          size="mini"
          class="lc-item-remove"
          icon="el-icon-delete"
          :title="child.type === 'group' ? $t('logicCondition.removeGroup') : $t('logicCondition.remove')"
          @click="removeChild(child)"
        />
      </div>
    </div>

    <div class="lc-actions">
      <el-button
        size="small"
        icon="el-icon-plus"
        :disabled="value.children.length >= maxChildren"
        @click="addCondition"
      >
        {{ $t('logicCondition.add') }}
      </el-button>
      <el-button
        size="small"
        icon="el-icon-folder-add"
        :disabled="value.children.length >= maxChildren"
        @click="addGroup"
      >
        {{ $t('logicCondition.addGroup') }}
      </el-button>
      <span class="lc-feedback">{{ $t('logicCondition.filled', { count: filledCount }) }}</span>
    </div>
  </div>
</template>

<script>
import {countConditions, createCondition, createGroup, removeNode, updateNode} from '@/utils/logicCondition'

/**
 * 逻辑条件构建器。
 * <p>
 * 以自然语言表达组合语义：「满足以下【全部 / 任一】条件」，下面逐行列出条件或子条件组。
 * 子组拥有自己的「全部 / 任一」选择，因此可以实现 (A 且 B) 或 (C 且 (D 或 E)) 的混合嵌套。
 * 操作动作只有「添加条件」「添加条件组」与「删除」，不需要用户理解节点类型切换。
 * </p>
 */
export default {
  name: 'LogicConditionBuilder',
  props: {
    value: {
      type: Object,
      required: true
    },
    getters: {
      type: Array,
      default: () => []
    },
    operandComponent: {
      type: [Object, Function],
      default: null
    },
    depth: {
      type: Number,
      default: 0
    }
  },
  computed: {
    /**
     * 单个分组允许的最大子节点数
     *
     * @returns {number} 最大数量
     */
    maxChildren() {
      return 8
    },
    /**
     * 当前分组下的有效条件总数，含子分组
     *
     * @returns {number} 有效条件数
     */
    filledCount() {
      return countConditions(this.value)
    }
  },
  methods: {
    /**
     * 切换当前分组的组合方式
     *
     * @param {string} op 目标运算符
     */
    setOp(op) {
      if (op !== this.value.op) {
        this.$emit('input', Object.assign({}, this.value, { op }))
      }
    },
    /**
     * 更新子节点
     *
     * @param {string} id 目标节点 ID
     * @param {Object} patch 更新内容
     */
    setChild(id, patch) {
      this.$emit('input', updateNode(this.value, id, patch))
    },
    /**
     * 删除子节点
     *
     * @param {Object} child 目标节点
     */
    removeChild(child) {
      const next = removeNode(this.value, child.id)
      if (!next.children.length) {
        next.children = [createCondition()]
      }
      this.$emit('input', next)
    },
    /**
     * 追加条件
     */
    addCondition() {
      this.$emit('input', Object.assign({}, this.value, {
        children: this.value.children.concat([createCondition()])
      }))
    },
    /**
     * 追加条件组
     */
    addGroup() {
      this.$emit('input', Object.assign({}, this.value, {
        children: this.value.children.concat([createGroup('and', [createCondition(), createCondition()])])
      }))
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~@/styles/variables.scss';
@import '~@/styles/mixin.scss';

@keyframes lc-in {
  from {
    opacity: 0;
    transform: translateY(-3px);
  }

  to {
    opacity: 1;
    transform: none;
  }
}

.logic-condition-builder {
  @include responsive-container;

  border: 1px solid #ebeef5;
  border-radius: 4px;
  background: #fff;
  overflow: hidden;

  &.is-nested {
    border-style: dashed;
    border-color: #c0c4cc;
    background: #fafafa;
  }
}

.lc-group-header {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
  padding: 8px 12px;
  border-bottom: 1px solid #ebeef5;
  background: #f5f7fa;
}

.lc-prefix {
  font-size: 13px;
  color: #606266;
}

.lc-op-select {
  width: 82px;
}

.lc-group-remove {
  margin-left: auto;
  color: #909399;
}

.lc-items {
  padding: 4px 12px;
}

.lc-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
  animation: lc-in 0.22s ease both;

  &:last-child {
    border-bottom: none;
  }
}

.lc-index {
  flex: none;
  width: 20px;
  height: 20px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin-top: 6px;
  border-radius: 50%;
  background: #f0f2f5;
  font-family: Menlo, Consolas, monospace;
  font-size: 11px;
  color: #909399;
}

.lc-item-content {
  flex: 1;
  min-width: 0;
}

.lc-item-remove {
  flex: none;
  padding: 4px;
  opacity: 0;
  color: #c0c4cc;
  transition: opacity 0.18s ease, color 0.18s ease;

  &:hover {
    color: #f56c6c;
  }

  .lc-item:hover & {
    opacity: 1;
  }

  @media (hover: none) {
    opacity: 1;
  }
}

.lc-actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  padding: 8px 12px;
  border-top: 1px solid #ebeef5;
  background: #fafafa;
}

.lc-feedback {
  margin-left: auto;
  font-size: 12px;
  color: #909399;
}

@include respond-to(phone) {
  .lc-op-select {
    width: 74px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .lc-item {
    animation: none;
  }
}
</style>

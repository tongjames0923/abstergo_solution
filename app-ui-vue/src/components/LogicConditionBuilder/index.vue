<template>
  <div :class="['logic-condition-builder', 'is-' + value.op, { 'is-nested': depth > 0 }]">
    <div class="lc-group-head">
      <span
        v-if="depth > 0"
        class="lc-group-tag"
      >{{ $t('logicCondition.group') }}</span>
      <div class="lc-mode">
        <button
          type="button"
          :class="['lc-mode-btn', { active: value.op === 'and' }]"
          @click="setOp('and')"
        >
          <span class="lc-symbol">∧</span>
          <span>{{ $t('logicCondition.and') }}</span>
        </button>
        <button
          type="button"
          :class="['lc-mode-btn', { active: value.op === 'or' }]"
          @click="setOp('or')"
        >
          <span class="lc-symbol">∨</span>
          <span>{{ $t('logicCondition.or') }}</span>
        </button>
      </div>
      <span class="lc-hint">{{ value.op === 'and' ? $t('logicCondition.hintAnd') : $t('logicCondition.hintOr') }}</span>
      <el-button
        v-if="depth > 0"
        type="text"
        size="mini"
        class="lc-remove-group"
        @click="$emit('remove')"
      >
        {{ $t('logicCondition.removeGroup') }}
      </el-button>
    </div>

    <div class="lc-branches">
      <div
        v-for="(child, index) in value.children"
        :key="child.id"
        class="lc-branch"
      >
        <span :class="['lc-node', { 'is-group': child.type === 'group' }]">
          <i
            v-if="child.type === 'group'"
            class="el-icon-s-operation"
          />
          <span v-else>{{ index + 1 }}</span>
        </span>

        <div class="lc-body">
          <div class="lc-node-head">
            <el-button
              type="text"
              size="mini"
              :class="{ active: child.type === 'condition' }"
              @click="toCondition(child)"
            >
              {{ $t('logicCondition.condition') }}
            </el-button>
            <el-button
              type="text"
              size="mini"
              :class="{ active: child.type === 'group' }"
              @click="toGroup(child)"
            >
              {{ $t('logicCondition.group') }}
            </el-button>
            <el-button
              type="text"
              size="mini"
              class="lc-remove"
              icon="el-icon-delete"
              :title="$t('logicCondition.remove')"
              @click="removeChild(child)"
            />
          </div>

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

        <div
          v-if="index < value.children.length - 1"
          class="lc-join"
        >
          <span class="lc-join-label">
            {{ value.op === 'and' ? $t('logicCondition.joinAnd') : $t('logicCondition.joinOr') }}
          </span>
        </div>
      </div>
    </div>

    <div class="lc-foot">
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
      <span class="lc-count">{{ $t('logicCondition.count', { current: value.children.length, max: maxChildren }) }}</span>
      <span class="lc-filled">{{ $t('logicCondition.filled', { count: filledCount }) }}</span>
    </div>

    <p
      v-if="depth === 0"
      class="lc-tip"
    >{{ $t('logicCondition.nestedHint') }}</p>
  </div>
</template>

<script>
import {
  compileGroup,
  countConditions,
  createCondition,
  createGroup,
  removeNode,
  updateNode
} from '@/utils/logicCondition'

/**
 * 逻辑条件表达式树构建器。
 * <p>
 * 每个分组拥有独立的「且 / 或」运算符，子节点可以是条件叶子，也可以是子分组；
 * 分组在提交时编译成 LogicConditionGetter 调用 JSON，因此可以实现
 * (A 且 B) 或 (C 且 (D 或 E)) 这类混合嵌套逻辑。操作数编辑器由调用方注入，
 * 子分组通过组件名自引用递归渲染，不形成模块循环依赖。
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
     * 切换当前分组的运算符
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
     * 追加条件叶子
     */
    addCondition() {
      this.$emit('input', Object.assign({}, this.value, {
        children: this.value.children.concat([createCondition()])
      }))
    },
    /**
     * 追加子分组
     */
    addGroup() {
      this.$emit('input', Object.assign({}, this.value, {
        children: this.value.children.concat([createGroup('and', [createCondition(), createCondition()])])
      }))
    },
    /**
     * 把子节点转为条件叶子，原分组内容编码为 getter 调用保留
     *
     * @param {Object} child 目标节点
     */
    toCondition(child) {
      if (child.type !== 'group') {
        return
      }
      const raw = JSON.stringify({
        getterId: 'LogicConditionGetter',
        paramValues: compileGroup(child)
      })
      this.setChild(child.id, { type: 'condition', value: raw })
    },
    /**
     * 把子节点转为分组，原条件作为分组的首个条件
     *
     * @param {Object} child 目标节点
     */
    toGroup(child) {
      if (child.type !== 'condition') {
        return
      }
      this.setChild(child.id, createGroup('and', [createCondition(child.value)]))
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
    transform: translateY(-4px);
  }

  to {
    opacity: 1;
    transform: none;
  }
}

.logic-condition-builder {
  @include responsive-container;

  --lc-accent: #0071e3;
  --lc-accent-soft: #ecf5ff;
  --lc-line: #ebeef5;

  display: flex;
  flex-direction: column;
  gap: 10px;

  &.is-or {
    --lc-accent: #b45309;
    --lc-accent-soft: #fdf3e7;
  }

  &.is-nested {
    padding: 10px 12px;
    border: 1px dashed var(--lc-accent);
    border-radius: 4px;
    background: #fafafa;
  }
}

.lc-group-head {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.lc-group-tag {
  padding: 1px 6px;
  border-radius: 2px;
  background: var(--lc-accent-soft);
  font-size: 11px;
  font-weight: 600;
  color: var(--lc-accent);
}

.lc-mode {
  display: inline-flex;
  border: 1px solid var(--lc-line);
  border-radius: 4px;
  overflow: hidden;
}

.lc-mode-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 13px;
  border: 0;
  border-right: 1px solid var(--lc-line);
  background: #fff;
  color: #606266;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.18s ease, color 0.18s ease;

  &:last-child {
    border-right: 0;
  }

  &:hover {
    background: #f5f7fa;
  }

  &.active {
    background: var(--lc-accent);
    color: #fff;
  }

  @media (hover: none) {
    &:active {
      background: var(--lc-accent-soft);
    }
  }
}

.lc-symbol {
  font-family: Menlo, Consolas, monospace;
  font-size: 15px;
  font-weight: 600;
  line-height: 1;
}

.lc-hint {
  font-size: 12px;
  line-height: 1.5;
  color: #909399;
}

.lc-remove-group {
  margin-left: auto;
  color: #909399;
}

.lc-branches {
  position: relative;
  padding-left: 32px;

  &::before {
    content: '';
    position: absolute;
    left: 11px;
    top: 14px;
    bottom: 26px;
    width: 2px;
    background: var(--lc-line);
  }
}

.lc-branch {
  position: relative;
  padding-bottom: 26px;
  animation: lc-in 0.24s ease both;

  &:last-child {
    padding-bottom: 0;
  }
}

.lc-node {
  position: absolute;
  left: -32px;
  top: 8px;
  width: 24px;
  height: 24px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 2px solid var(--lc-accent);
  border-radius: 50%;
  background: #fff;
  font-family: Menlo, Consolas, monospace;
  font-size: 11px;
  font-weight: 600;
  color: var(--lc-accent);

  &.is-group {
    border-radius: 4px;
    background: var(--lc-accent-soft);

    i {
      font-size: 12px;
    }
  }
}

.lc-body {
  position: relative;
  padding: 8px 10px 10px;
  border: 1px solid var(--lc-line);
  border-radius: 4px;
  background: #fff;
  transition: border-color 0.18s ease, box-shadow 0.18s ease;

  &:hover {
    border-color: #c0c4cc;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  }
}

.lc-node-head {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-bottom: 4px;

  .el-button {
    padding: 3px 5px;
    font-size: 11px;
    color: #c0c4cc;

    &.active {
      color: var(--lc-accent);
      font-weight: 600;
    }
  }
}

.lc-remove {
  margin-left: auto;
}

.lc-join {
  position: absolute;
  left: -39px;
  bottom: 4px;
  z-index: 1;
}

.lc-join-label {
  display: inline-block;
  padding: 1px 6px;
  border: 1px solid var(--lc-accent);
  border-radius: 10px;
  background: var(--lc-accent-soft);
  font-family: Menlo, Consolas, monospace;
  font-size: 10px;
  font-weight: 600;
  color: var(--lc-accent);
  white-space: nowrap;
}

.lc-foot {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  padding-left: 32px;
}

.lc-count {
  font-size: 12px;
  color: #909399;
}

.lc-filled {
  margin-left: auto;
  font-size: 12px;
  color: var(--lc-accent);
}

.lc-tip {
  margin: 0;
  padding-left: 32px;
  font-size: 12px;
  line-height: 1.6;
  color: #909399;
}

@include respond-to(phone) {
  .lc-branches {
    padding-left: 26px;
  }

  .lc-node {
    left: -26px;
    width: 20px;
    height: 20px;
    font-size: 10px;
  }

  .lc-join {
    left: -33px;
  }

  .lc-foot,
  .lc-tip {
    padding-left: 26px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .lc-branch {
    animation: none;
  }
}
</style>

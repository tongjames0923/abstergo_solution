<template>
  <div class="app-container condition-rule-manager">
    <div class="page-head">
      <div>
        <h2 class="page-title">{{ $t('conditionRule.title') }}</h2>
        <p class="page-desc">{{ $t('conditionRule.desc') }}</p>
      </div>
      <el-button type="primary" size="small" icon="el-icon-plus" @click="createRule">
        {{ $t('conditionRule.create') }}
      </el-button>
    </div>

    <div class="rule-layout">
      <el-card class="rule-list" shadow="hover">
        <div slot="header" class="card-head">
          <span>{{ $t('conditionRule.listTitle') }}</span>
          <el-tag size="mini" type="info" effect="plain">{{ total }}</el-tag>
        </div>

        <div class="list-toolbar">
          <el-input
            v-model="keyword"
            size="small"
            clearable
            prefix-icon="el-icon-search"
            :placeholder="$t('conditionRule.searchPlaceholder')"
            @keyup.enter.native="handleSearch"
            @clear="handleSearch"
          />
          <el-select v-model="enabledFilter" size="small" class="status-filter" @change="handleSearch">
            <el-option :label="$t('conditionRule.allStatus')" value="" />
            <el-option :label="$t('conditionRule.enabled')" :value="1" />
            <el-option :label="$t('conditionRule.disabled')" :value="0" />
          </el-select>
        </div>

        <div v-loading="loading" class="rule-items">
          <div
            v-for="rule in rules"
            :key="rule.id"
            :class="['rule-item', { active: form.id === rule.id }]"
            @click="selectRule(rule)"
          >
            <div class="rule-item-main">
              <span class="rule-code">{{ rule.ruleCode }}</span>
              <span class="rule-name">{{ rule.ruleName }}</span>
            </div>
            <div class="rule-item-meta">
              <el-tag size="mini" :type="rule.enabled === 1 ? 'success' : 'info'" effect="plain">
                {{ rule.enabled === 1 ? $t('conditionRule.enabled') : $t('conditionRule.disabled') }}
              </el-tag>
              <span class="rule-getter">{{ rule.getterId }}</span>
            </div>
            <div class="rule-item-actions">
              <el-button type="text" size="mini" @click.stop="handleEvaluate(rule)">
                {{ $t('conditionRule.runSelf') }}
              </el-button>
              <el-button type="text" size="mini" @click.stop="handleToggle(rule)">
                {{ rule.enabled === 1 ? $t('conditionRule.disabled') : $t('conditionRule.enabled') }}
              </el-button>
              <el-button type="text" size="mini" class="danger" @click.stop="handleDelete(rule)">
                {{ $t('common.delete') }}
              </el-button>
            </div>
          </div>
          <div v-if="!rules.length && !loading" class="list-empty">{{ $t('conditionRule.empty') }}</div>
        </div>

        <el-pagination
          v-if="total > pageSize"
          small
          layout="prev, pager, next"
          :current-page="pageNum"
          :page-size="pageSize"
          :total="total"
          @current-change="handlePageChange"
        />
      </el-card>

      <el-card class="rule-editor" shadow="hover">
        <div v-if="!editing" class="editor-empty">{{ $t('conditionRule.selectFirst') }}</div>
        <template v-else>
          <div slot="header" class="card-head">
            <span>{{ $t('conditionRule.baseTitle') }}</span>
            <el-switch
              v-model="form.enabled"
              :active-value="1"
              :inactive-value="0"
              :active-text="$t('conditionRule.enabledSwitch')"
            />
          </div>

          <el-form label-width="96px" size="small" class="base-form">
            <el-form-item :label="$t('conditionRule.code')" required>
              <el-input
                v-model="form.ruleCode"
                :disabled="!!form.id"
                :placeholder="$t('conditionRule.codePlaceholder')"
              />
            </el-form-item>
            <el-form-item :label="$t('conditionRule.name')" required>
              <el-input v-model="form.ruleName" :placeholder="$t('conditionRule.namePlaceholder')" />
            </el-form-item>
            <el-form-item :label="$t('conditionRule.description')">
              <el-input
                v-model="form.description"
                type="textarea"
                :rows="2"
                :placeholder="$t('conditionRule.descriptionPlaceholder')"
              />
            </el-form-item>
          </el-form>

          <div class="section-title">
            <span>{{ $t('conditionRule.editorTitle') }}</span>
            <el-radio-group v-model="editor" size="mini">
              <el-radio-button label="condition">{{ $t('conditionRule.editorCondition') }}</el-radio-button>
              <el-radio-button label="logic">{{ $t('conditionRule.editorLogic') }}</el-radio-button>
            </el-radio-group>
          </div>

          <condition-builder v-if="editor === 'condition'" v-model="condition" :getters="getterList" />
          <logic-condition-builder
            v-else
            v-model="logicTree"
            :getters="getterList"
            :operand-component="operandComponent"
          />

          <div class="test-bar">
            <el-button size="small" icon="el-icon-video-play" :loading="running" @click="handleTest">
              {{ $t('conditionRule.test') }}
            </el-button>
            <span class="test-hint">{{ $t('conditionRule.testHint') }}</span>
            <span v-if="result !== null" :class="['test-result', result ? 'is-true' : 'is-false']">
              {{ result ? $t('conditionRule.runTrue') : $t('conditionRule.runFalse') }}
            </span>
          </div>

          <div class="editor-footer">
            <el-button size="small" @click="cancelEdit">{{ $t('common.cancel') }}</el-button>
            <el-button type="primary" size="small" :loading="saving" @click="handleSave">
              {{ $t('conditionRule.save') }}
            </el-button>
          </div>
        </template>
      </el-card>
    </div>
  </div>
</template>

<script>
import {
  deleteConditionRule,
  evaluateConditionRule,
  getConditionRuleByCode,
  queryConditionRules,
  saveConditionRule,
  setConditionRuleEnabled
} from '@/api/conditionRule'
import {getValueGetters, invokeValueGetter} from '@/api/script/valueGetter'
import ConditionBuilder from '@/components/ConditionBuilder'
import LogicConditionBuilder from '@/components/LogicConditionBuilder'
import ValueGetterOperand from '@/components/ValueGetterOperand'
import ResponsiveMixin from '@/mixins/responsive'
import {compileGroup, createGroup, hasCondition, normalizeGroup} from '@/utils/logicCondition'

const CONDITION_GETTER_ID = 'ConditionGetter'
const LOGIC_GETTER_ID = 'LogicConditionGetter'

/**
 * 规则管理页。
 * <p>
 * 左侧检索与选择规则，右侧维护基础信息与判断逻辑；编辑器直接复用条件构建器与逻辑条件构建器，
 * 保存时把编排结果编译为调用载荷（invocationJson）与编辑器快照（editorJson）一并持久化。
 * </p>
 */
export default {
  name: 'ConditionRuleManager',
  components: { ConditionBuilder, LogicConditionBuilder },
  mixins: [ResponsiveMixin],
  data() {
    return {
      loading: false,
      saving: false,
      running: false,
      rules: [],
      total: 0,
      pageNum: 1,
      pageSize: 20,
      keyword: '',
      enabledFilter: '',
      getterList: [],
      editing: false,
      form: this.blankForm(),
      editor: 'condition',
      condition: { left: '', op: 'eq', right: '' },
      logicTree: createGroup(),
      result: null
    }
  },
  computed: {
    /**
     * 操作数编辑器组件，注入给逻辑条件构建器
     *
     * @returns {Object} 组件构造器
     */
    operandComponent() {
      return ValueGetterOperand
    }
  },
  created() {
    this.loadGetters()
    this.loadRules()
    this.openFromQuery()
  },
  methods: {
    /**
     * 按路由参数直接打开规则，供订阅页跳转定位
     */
    async openFromQuery() {
      const ruleCode = this.$route && this.$route.query ? this.$route.query.ruleCode : ''
      if (!ruleCode) {
        return
      }
      try {
        const res = await getConditionRuleByCode(ruleCode)
        if (res.data) {
          this.selectRule(res.data)
        }
      } catch (e) {
        this.$message.warning(this.$t('conditionRule.notExists'))
      }
    },
    /**
     * 空表单
     *
     * @returns {Object} 规则表单
     */
    blankForm() {
      return { id: null, ruleCode: '', ruleName: '', description: '', enabled: 1 }
    },
    /**
     * 加载取值器列表
     */
    async loadGetters() {
      try {
        const res = await getValueGetters()
        this.getterList = res.data || []
      } catch (e) {
        this.getterList = []
      }
    },
    /**
     * 加载规则列表
     */
    async loadRules() {
      this.loading = true
      try {
        const criteria = {}
        if (this.keyword) {
          criteria.ruleCodeLike = this.keyword
          criteria.ruleNameLike = this.keyword
        }
        if (this.enabledFilter !== '') {
          criteria.enabledEquals = this.enabledFilter
        }
        const res = await queryConditionRules(criteria, this.pageNum, this.pageSize)
        const page = res.data || {}
        this.rules = page.data || []
        this.total = page.total || 0
      } catch (e) {
        this.rules = []
        this.total = 0
      } finally {
        this.loading = false
      }
    },
    /**
     * 检索
     */
    handleSearch() {
      this.pageNum = 1
      this.loadRules()
    },
    /**
     * 翻页
     *
     * @param {number} page 页码
     */
    handlePageChange(page) {
      this.pageNum = page
      this.loadRules()
    },
    /**
     * 新建规则
     */
    createRule() {
      this.editing = true
      this.form = this.blankForm()
      this.editor = 'condition'
      this.condition = { left: '', op: 'eq', right: '' }
      this.logicTree = createGroup()
      this.result = null
    },
    /**
     * 选中规则并还原编辑器
     *
     * @param {Object} rule 规则实体
     */
    selectRule(rule) {
      this.editing = true
      this.form = {
        id: rule.id,
        ruleCode: rule.ruleCode,
        ruleName: rule.ruleName,
        description: rule.description || '',
        enabled: rule.enabled === 0 ? 0 : 1
      }
      this.result = null
      this.restoreEditor(rule)
    },
    /**
     * 从规则快照还原编辑器状态
     *
     * @param {Object} rule 规则实体
     */
    restoreEditor(rule) {
      let snapshot = null
      try {
        snapshot = rule.editorJson ? JSON.parse(rule.editorJson) : null
      } catch (e) {
        snapshot = null
      }
      if (snapshot && snapshot.editor === 'logic') {
        this.editor = 'logic'
        this.logicTree = normalizeGroup(snapshot.state)
        return
      }
      if (snapshot && snapshot.editor === 'condition') {
        this.editor = 'condition'
        this.condition = {
          left: snapshot.state && snapshot.state.left ? snapshot.state.left : '',
          op: (snapshot.state && snapshot.state.op) || 'eq',
          right: snapshot.state && snapshot.state.right ? snapshot.state.right : ''
        }
        return
      }
      this.editor = rule.getterId === LOGIC_GETTER_ID ? 'logic' : 'condition'
      this.condition = { left: '', op: 'eq', right: '' }
      this.logicTree = createGroup()
    },
    /**
     * 取消编辑
     */
    cancelEdit() {
      this.editing = false
      this.form = this.blankForm()
      this.result = null
    },
    /**
     * 构造调用载荷
     *
     * @returns {Object} {getterId, paramValues}
     */
    buildInvocation() {
      if (this.editor === 'condition') {
        return {
          getterId: CONDITION_GETTER_ID,
          paramValues: [
            { type: 'operand', name: '', sort: 1, value: this.condition.left || '' },
            { type: 'string', name: '', sort: 2, value: this.condition.op || 'eq' },
            { type: 'operand', name: '', sort: 3, value: this.condition.right || '' }
          ]
        }
      }
      return { getterId: LOGIC_GETTER_ID, paramValues: compileGroup(this.logicTree) }
    },
    /**
     * 校验当前编排
     *
     * @returns {boolean} 是否通过
     */
    validate() {
      if (!this.form.ruleCode || !this.form.ruleCode.trim()) {
        this.$message.warning(this.$t('conditionRule.ruleCodeRequired'))
        return false
      }
      if (!this.form.ruleName || !this.form.ruleName.trim()) {
        this.$message.warning(this.$t('conditionRule.ruleNameRequired'))
        return false
      }
      const valid = this.editor === 'condition'
        ? !!(this.condition.left && String(this.condition.left).trim())
        : hasCondition(this.logicTree)
      if (!valid) {
        this.$message.warning(this.$t('conditionRule.conditionRequired'))
        return false
      }
      return true
    },
    /**
     * 测试执行当前编排
     */
    async handleTest() {
      if (!this.validate()) {
        return
      }
      const invocation = this.buildInvocation()
      this.running = true
      try {
        const res = await invokeValueGetter({
          getterId: invocation.getterId,
          paramValues: invocation.paramValues
        })
        const value = res.data ? res.data.value : null
        this.result = String(value) === 'true'
      } catch (e) {
        this.result = false
      } finally {
        this.running = false
      }
    },
    /**
     * 保存规则
     */
    async handleSave() {
      if (!this.validate()) {
        return
      }
      const invocation = this.buildInvocation()
      const payload = {
        ruleCode: this.form.ruleCode.trim(),
        ruleName: this.form.ruleName.trim(),
        description: this.form.description,
        getterId: invocation.getterId,
        invocationJson: JSON.stringify(invocation.paramValues),
        editorJson: JSON.stringify({
          editor: this.editor,
          state: this.editor === 'condition' ? this.condition : this.logicTree
        }),
        enabled: this.form.enabled
      }
      if (this.form.id) {
        payload.id = this.form.id
      }
      this.saving = true
      try {
        const res = await saveConditionRule(payload)
        this.$message.success(this.$t('conditionRule.saved'))
        const saved = res.data
        if (saved) {
          this.form.id = saved.id
          this.form.ruleCode = saved.ruleCode
        }
        this.loadRules()
      } finally {
        this.saving = false
      }
    },
    /**
     * 执行已保存规则
     *
     * @param {Object} rule 规则
     */
    async handleEvaluate(rule) {
      try {
        const res = await evaluateConditionRule(rule.ruleCode)
        const value = res.data
        this.$message.success(this.$t('conditionRule.runSelf') + ': ' +
          (value === true ? this.$t('conditionRule.runTrue') : this.$t('conditionRule.runFalse')))
      } catch (e) {
        this.$message.error(this.$t('conditionRule.loadFailed'))
      }
    },
    /**
     * 启用或停用规则
     *
     * @param {Object} rule 规则
     */
    async handleToggle(rule) {
      const next = rule.enabled === 1 ? 0 : 1
      await setConditionRuleEnabled(rule.id, next === 1)
      this.loadRules()
    },
    /**
     * 删除规则
     *
     * @param {Object} rule 规则
     */
    async handleDelete(rule) {
      try {
        await this.$confirm(this.$t('conditionRule.deleteConfirm'), this.$t('common.tip'), { type: 'warning' })
      } catch (e) {
        return
      }
      await deleteConditionRule(rule.id)
      this.$message.success(this.$t('conditionRule.removed'))
      if (this.form.id === rule.id) {
        this.cancelEdit()
      }
      this.loadRules()
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~@/styles/variables.scss';
@import '~@/styles/mixin.scss';

.condition-rule-manager {
  @include responsive-container;

  padding: 12px;
  overflow-y: auto;
}

.page-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
}

.page-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.page-desc {
  margin: 4px 0 0;
  font-size: 12px;
  color: #909399;
}

.rule-layout {
  display: grid;
  grid-template-columns: 320px minmax(0, 1fr);
  gap: 12px;
  align-items: start;
}

.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.list-toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 8px;

  .el-input {
    flex: 1;
  }

  .status-filter {
    width: 104px;
  }
}

.rule-items {
  min-height: 120px;
}

.rule-item {
  padding: 10px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: border-color 0.18s ease, box-shadow 0.18s ease;

  &:hover {
    border-color: #c0c4cc;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  }

  &.active {
    border-color: #409eff;
    background: #f4f9ff;
  }
}

.rule-item-main {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.rule-code {
  font-family: Menlo, Consolas, monospace;
  font-size: 12px;
  color: #303133;
  word-break: break-all;
}

.rule-name {
  font-size: 12px;
  color: #606266;
}

.rule-item-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 6px;
}

.rule-getter {
  font-size: 11px;
  color: #909399;
  word-break: break-all;
}

.rule-item-actions {
  display: flex;
  align-items: center;
  gap: 2px;
  margin-top: 6px;

  .el-button {
    padding: 2px 4px;
    font-size: 11px;
  }

  .danger {
    color: #f56c6c;
  }
}

.list-empty,
.editor-empty {
  padding: 32px 0;
  text-align: center;
  font-size: 13px;
  color: #909399;
}

.base-form {
  margin-bottom: 4px;
}

.section-title {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin: 8px 0 10px;
  font-size: 14px;
  font-weight: 600;
  color: #303133;
}

.test-bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

.test-hint {
  font-size: 12px;
  color: #909399;
}

.test-result {
  padding: 2px 10px;
  border-radius: 10px;
  font-size: 12px;
  font-weight: 600;

  &.is-true {
    background: #f0f9eb;
    color: #67c23a;
  }

  &.is-false {
    background: #fef0f0;
    color: #f56c6c;
  }
}

.editor-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 12px;
}

@include respond-to(tablet) {
  .rule-layout {
    grid-template-columns: 260px minmax(0, 1fr);
  }
}

@include respond-to(phone) {
  .rule-layout {
    grid-template-columns: 1fr;
  }

  .page-head {
    flex-direction: column;
  }
}
</style>

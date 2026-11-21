<template>
  <div class="app-container event-binding-manager">
    <div class="page-head">
      <div>
        <h2 class="page-title">{{ $t('eventAction.title') }}</h2>
        <p class="page-desc">{{ $t('eventAction.desc') }}</p>
      </div>
      <div class="page-stats">
        <el-tag size="small" type="info" effect="plain">{{ $t('eventCommon.total', { total }) }}</el-tag>
        <el-button type="primary" size="small" icon="el-icon-plus" @click="createBinding">
          {{ $t('eventCommon.create') }}
        </el-button>
      </div>
    </div>

    <div class="manager-layout">
      <el-card class="list-card" shadow="hover">
        <div slot="header" class="card-head">
          <span>{{ $t('eventCommon.listTitle') }}</span>
          <el-tag size="mini" type="info" effect="plain">{{ total }}</el-tag>
        </div>

        <div class="list-toolbar">
          <el-input
            v-model="keyword"
            size="small"
            clearable
            prefix-icon="el-icon-search"
            :placeholder="$t('eventCommon.searchPlaceholder')"
            @keyup.enter.native="handleSearch"
            @clear="handleSearch"
          />
          <el-select v-model="enabledFilter" size="small" class="status-filter" @change="handleSearch">
            <el-option :label="$t('eventCommon.allStatus')" value="" />
            <el-option :label="$t('eventCommon.enabled')" :value="1" />
            <el-option :label="$t('eventCommon.disabled')" :value="0" />
          </el-select>
        </div>

        <div v-loading="loading" class="list-items">
          <div
            v-for="item in items"
            :key="item.id"
            :class="['list-item', { active: isActive(item) }]"
            @click="selectItem(item)"
          >
            <div class="item-main">
              <span class="item-code">{{ item.reporterId }} # {{ item.eventCode }}</span>
              <span class="item-name">{{ item.actionName || item.actionId }}</span>
            </div>
            <div class="item-meta">
              <el-tag size="mini" :type="item.enabled === 1 ? 'success' : 'info'" effect="plain">
                {{ item.enabled === 1 ? $t('eventCommon.enabled') : $t('eventCommon.disabled') }}
              </el-tag>
              <span class="item-rule">{{ item.actionId }}</span>
              <span class="item-order">#{{ item.sortOrder }}</span>
            </div>
            <div class="item-actions">
              <el-button type="text" size="mini" @click.stop="handleToggle(item)">
                {{ item.enabled === 1 ? $t('eventCommon.disabled') : $t('eventCommon.enabled') }}
              </el-button>
              <el-button type="text" size="mini" class="danger" @click.stop="handleDelete(item)">
                {{ $t('eventCommon.remove') }}
              </el-button>
            </div>
          </div>
          <div v-if="!items.length && !loading" class="list-empty">{{ $t('eventCommon.empty') }}</div>
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

      <el-card class="editor-card" shadow="hover">
        <div v-if="!editing" class="editor-empty">{{ $t('eventCommon.selectFirst') }}</div>
        <template v-else>
          <div slot="header" class="card-head">
            <span>{{ $t('eventCommon.editorTitle') }}</span>
            <el-switch
              v-model="enabled"
              :active-value="1"
              :inactive-value="0"
              :active-text="$t('eventCommon.enabledSwitch')"
            />
          </div>

          <event-scope-picker
            :reporters="reporters"
            :events="events"
            :reporter-id.sync="reporterId"
            :event-code.sync="eventCode"
            @change="handleScopeChange"
          />

          <p class="editor-hint">
            {{ $t('eventCommon.placeholderHint', { placeholder: '${paramName}', example: '${userName}' }) }}
          </p>

          <div class="editor-modes">
            <el-radio-group v-model="inputMode" size="mini">
              <el-radio-button label="graph">{{ $t('jsonMode.modeGraph') }}</el-radio-button>
              <el-radio-button label="json">{{ $t('jsonMode.modeJson') }}</el-radio-button>
            </el-radio-group>
          </div>

          <action-binding-panel
            v-if="inputMode === 'graph'"
            :key="editorKey"
            ref="bindingPanel"
            v-model="rows"
            :actions="actions"
          />
          <json-edit-panel
            v-else
            :value.sync="jsonText"
            :hints="jsonHints"
            :example="jsonExample"
            @apply="applyJson"
            @fill="fillJson"
          />

          <div v-if="inputMode === 'graph'" class="editor-footer">
            <el-button size="small" @click="cancelEdit">{{ $t('eventCommon.cancel') }}</el-button>
            <el-button type="primary" size="small" :loading="saving" @click="handleSave">
              {{ $t('eventCommon.save') }}
            </el-button>
          </div>

          <action-test-panel :actions="actions" />
        </template>
      </el-card>
    </div>
  </div>
</template>

<script>
import {
  deleteEventAction,
  getActionMetas,
  getActionsByEvent,
  queryEventActions,
  saveEventActions,
  setEventActionEnabled
} from '@/api/eventAction'
import {getEventReporters, getReporterEvents} from '@/api/eventSubscription'
import JsonEditPanel from '@/components/JsonEditPanel'
import ResponsiveMixin from '@/mixins/responsive'
import {toActionOptions, toEventOptions, toReporterOptions, unwrapList, unwrapPage} from '@/utils/eventMeta'
import ActionBindingPanel from './event/ActionBindingPanel'
import ActionTestPanel from './event/ActionTestPanel'
import EventScopePicker from './event/EventScopePicker'

/**
 * 事件动作管理页。
 * <p>
 * 左侧展示按事件登记的动作绑定；右侧按「事件 → 动作 → 参数」编排，动作参数表单由后端
 * ParamType 声明动态渲染，支持用 ${参数名} 引用本次事件载荷，底部提供单动作测试执行。
 * </p>
 */
export default {
  name: 'EventActionManager',
  components: { ActionBindingPanel, ActionTestPanel, EventScopePicker, JsonEditPanel },
  mixins: [ResponsiveMixin],
  data() {
    return {
      loading: false,
      saving: false,
      items: [],
      total: 0,
      pageNum: 1,
      pageSize: 20,
      keyword: '',
      enabledFilter: '',
      reporters: [],
      events: [],
      actions: [],
      editing: false,
      reporterId: '',
      eventCode: '',
      loadedReporterId: '',
      enabled: 1,
      rows: [],
      editorKey: 0,
      inputMode: 'graph',
      jsonText: ''
    }
  },
  computed: {
    /**
     * JSON 模式编写提示
     *
     * @returns {Array} 提示文案列表
     */
    jsonHints() {
      return [
        this.$t('jsonMode.hintRowsAction'),
        this.$t('jsonMode.hintParamJson'),
        this.$t('jsonMode.hintTip')
      ]
    },
    /**
     * JSON 模式示例
     *
     * @returns {string} 示例文本
     */
    jsonExample() {
      return JSON.stringify([{
        actionId: 'YOUR_ACTION_ID',
        actionName: '',
        paramJson: [{ type: 'string', name: 'userName', sort: 1, value: '${userName}' }],
        sortOrder: 0,
        enabled: 1
      }], null, 2)
    }
  },
  created() {
    this.loadReporters()
    this.loadActions()
    this.loadList()
  },
  methods: {
    /**
     * 加载事件上报器
     */
    async loadReporters() {
      try {
        const res = await getEventReporters()
        this.reporters = toReporterOptions(res)
      } catch (e) {
        console.error('加载事件上报器失败', e)
        this.reporters = []
      }
    },
    /**
     * 加载动作元信息
     */
    async loadActions() {
      try {
        const res = await getActionMetas()
        this.actions = toActionOptions(res)
      } catch (e) {
        console.error('加载动作元信息失败', e)
        this.actions = []
      }
    },
    /**
     * 加载动作绑定列表
     */
    async loadList() {
      this.loading = true
      try {
        const criteria = {}
        if (this.keyword) {
          criteria.reporterIdLike = this.keyword
          criteria.eventCodeLike = this.keyword
        }
        if (this.enabledFilter !== '') {
          criteria.enabledEquals = this.enabledFilter
        }
        const res = await queryEventActions(criteria, this.pageNum, this.pageSize)
        const page = unwrapPage(res)
        this.items = page.list
        this.total = page.total
      } catch (e) {
        console.error('加载动作绑定失败', e)
        this.items = []
        this.total = 0
      } finally {
        this.loading = false
      }
    },
    /**
     * 加载指定上报器的事件定义
     *
     * @param {string} reporterId 上报器ID
     */
    async loadEvents(reporterId) {
      if (!reporterId) {
        this.events = []
        return
      }
      try {
        const res = await getReporterEvents(reporterId)
        this.events = toEventOptions(res)
      } catch (e) {
        console.error('加载事件定义失败', e)
        this.events = []
      }
    },
    /**
     * 加载指定事件的动作绑定
     *
     * @param {string} reporterId 上报器ID
     * @param {string} eventCode 事件编码
     */
    async loadGroup(reporterId, eventCode) {
      this.inputMode = 'graph'
      this.jsonText = ''
      try {
        const res = await getActionsByEvent(reporterId, eventCode)
        const list = unwrapList(res)
        this.rows = list.map((item, index) => ({
          uid: 'action-' + (item.id || index),
          id: item.id,
          actionId: item.actionId,
          actionName: item.actionName || '',
          paramJson: item.paramJson || '',
          sortOrder: item.sortOrder == null ? 0 : item.sortOrder,
          enabled: item.enabled === 0 ? 0 : 1
        }))
        this.enabled = this.rows.some(row => row.enabled === 1) ? 1 : 0
      } catch (e) {
        this.rows = []
      }
      this.editorKey += 1
    },
    /**
     * 新建动作编排
     */
    createBinding() {
      this.editing = true
      this.reporterId = ''
      this.eventCode = ''
      this.loadedReporterId = ''
      this.enabled = 1
      this.rows = []
      this.events = []
      this.editorKey += 1
      this.inputMode = 'graph'
      this.jsonText = ''
    },
    /**
     * 选中左侧绑定行，加载其所属事件的全部动作
     *
     * @param {Object} item 绑定行
     */
    async selectItem(item) {
      this.editing = true
      this.reporterId = item.reporterId
      this.eventCode = item.eventCode
      this.loadedReporterId = item.reporterId
      await this.loadEvents(item.reporterId)
      await this.loadGroup(item.reporterId, item.eventCode)
    },
    /**
     * 事件范围变化后回显已有绑定
     *
     * @param {Object} scope {reporterId, eventCode}
     */
    async handleScopeChange(scope) {
      if (scope.reporterId && scope.reporterId !== this.loadedReporterId) {
        this.loadedReporterId = scope.reporterId
        this.reporterId = scope.reporterId
        this.eventCode = ''
        this.rows = []
        await this.loadEvents(scope.reporterId)
      }
      if (scope.reporterId && scope.eventCode) {
        this.eventCode = scope.eventCode
        await this.loadGroup(scope.reporterId, scope.eventCode)
      }
    },
    /**
     * 当前列表行是否为编辑中的事件
     *
     * @param {Object} item 绑定行
     * @returns {boolean} 是否选中
     */
    isActive(item) {
      return item.reporterId === this.reporterId && item.eventCode === this.eventCode
    },
    /**
     * 检索
     */
    handleSearch() {
      this.pageNum = 1
      this.loadList()
    },
    /**
     * 翻页
     *
     * @param {number} page 页码
     */
    handlePageChange(page) {
      this.pageNum = page
      this.loadList()
    },
    /**
     * 取消编辑
     */
    cancelEdit() {
      this.editing = false
      this.rows = []
      this.reporterId = ''
      this.eventCode = ''
      this.loadedReporterId = ''
      this.editorKey += 1
      this.inputMode = 'graph'
      this.jsonText = ''
    },
    /**
     * 用当前图形配置填充 JSON 文本
     */
    fillJson() {
      const panel = this.$refs.bindingPanel
      const source = panel ? panel.collect() : this.rows
      this.jsonText = JSON.stringify(source.map(row => ({
        actionId: row.actionId,
        actionName: row.actionName,
        paramJson: this.parseParamJson(row.paramJson),
        sortOrder: row.sortOrder,
        enabled: row.enabled
      })), null, 2)
    },
    /**
     * JSON 字符串还原为参数数组，供样例展示
     *
     * @param {string} raw 参数 JSON 字符串
     * @returns {*} 参数数组或原始字符串
     */
    parseParamJson(raw) {
      if (!raw) {
        return []
      }
      try {
        return JSON.parse(raw)
      } catch (e) {
        return raw
      }
    },
    /**
     * 把粘贴的 JSON 行回填到动作编排面板
     *
     * @param {Array} rows 动作行列表
     */
    applyJson(rows) {
      if (!Array.isArray(rows)) {
        this.$message.warning(this.$t('jsonMode.needArray'))
        return
      }
      const normalized = []
      for (let i = 0; i < rows.length; i++) {
        const item = rows[i]
        if (!item || typeof item !== 'object' || Array.isArray(item)) {
          this.$message.warning(this.$t('jsonMode.needObject'))
          return
        }
        let paramJson = ''
        const raw = item.paramJson
        if (typeof raw === 'string') {
          try {
            JSON.parse(raw)
            paramJson = raw
          } catch (e) {
            this.$message.warning(this.$t('jsonMode.invalidParamJson'))
            return
          }
        } else if (raw !== null && raw !== undefined) {
          paramJson = JSON.stringify(raw)
        }
        normalized.push({
          uid: 'action-' + this.editorKey + '-' + i,
          id: item.id || null,
          actionId: item.actionId || '',
          actionName: item.actionName || '',
          paramJson,
          sortOrder: item.sortOrder == null ? i : item.sortOrder,
          enabled: item.enabled === 0 ? 0 : 1
        })
      }
      this.rows = normalized
      this.editorKey += 1
      this.inputMode = 'graph'
      this.$message.success(this.$t('jsonMode.applied'))
    },
    /**
     * 保存动作编排
     */
    async handleSave() {
      if (!this.reporterId || !this.eventCode) {
        this.$message.warning(this.$t('eventCommon.eventRequired'))
        return
      }
      const panel = this.$refs.bindingPanel
      if (panel) {
        const validation = panel.validate()
        if (!validation.valid) {
          this.$message.warning(validation.message)
          return
        }
      }
      this.saving = true
      try {
        const actions = panel ? panel.collect() : []
        await saveEventActions({
          reporterId: this.reporterId,
          eventCode: this.eventCode,
          actions: actions.map(item => ({
            ...item,
            reporterId: this.reporterId,
            eventCode: this.eventCode,
            enabled: this.enabled === 1 ? item.enabled : 0
          }))
        })
        this.$message.success(this.$t('eventCommon.saved'))
        this.loadList()
        await this.loadGroup(this.reporterId, this.eventCode)
      } finally {
        this.saving = false
      }
    },
    /**
     * 启用或停用单条绑定
     *
     * @param {Object} item 绑定行
     */
    async handleToggle(item) {
      await setEventActionEnabled(item.id, item.enabled !== 1)
      this.loadList()
    },
    /**
     * 删除单条绑定
     *
     * @param {Object} item 绑定行
     */
    async handleDelete(item) {
      try {
        await this.$confirm(this.$t('eventCommon.removeConfirm'), this.$t('common.tip'), { type: 'warning' })
      } catch (e) {
        return
      }
      await deleteEventAction(item.id)
      this.$message.success(this.$t('eventCommon.removed'))
      if (this.isActive(item)) {
        this.cancelEdit()
      }
      this.loadList()
    }
  }
}
</script>

<style lang="scss" scoped>
@import '~@/styles/variables.scss';
@import '~@/styles/mixin.scss';

.event-binding-manager {
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

.page-stats {
  display: flex;
  align-items: center;
  gap: 8px;
}

.manager-layout {
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

.list-items {
  min-height: 120px;
}

.list-item {
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

.item-main {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.item-code {
  font-family: Menlo, Consolas, monospace;
  font-size: 12px;
  color: #303133;
  word-break: break-all;
}

.item-name {
  font-size: 12px;
  color: #606266;
  word-break: break-all;
}

.item-meta {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 6px;
  font-size: 11px;
  color: #909399;
}

.item-rule {
  word-break: break-all;
}

.item-order {
  font-family: Menlo, Consolas, monospace;
}

.item-actions {
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

.editor-hint {
  margin: 0 0 8px;
  font-size: 12px;
  color: #909399;
}

.editor-modes {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 8px;
}

.editor-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 12px;
}

@include respond-to(tablet) {
  .manager-layout {
    grid-template-columns: 260px minmax(0, 1fr);
  }
}

@include respond-to(phone) {
  .manager-layout {
    grid-template-columns: 1fr;
  }

  .page-head {
    flex-direction: column;
  }
}
</style>

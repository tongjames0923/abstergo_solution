<template>
  <div class="app-container event-binding-manager">
    <div class="page-head">
      <div>
        <h2 class="page-title">{{ $t('eventSubscription.title') }}</h2>
        <p class="page-desc">{{ $t('eventSubscription.desc') }}</p>
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
              <span class="item-name">{{ item.triggerId }}</span>
            </div>
            <div class="item-meta">
              <el-tag size="mini" :type="item.enabled === 1 ? 'success' : 'info'" effect="plain">
                {{ item.enabled === 1 ? $t('eventCommon.enabled') : $t('eventCommon.disabled') }}
              </el-tag>
              <span class="item-rule">{{ item.ruleCode || $t('eventCommon.noConditionRule') }}</span>
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

          <subscription-rows v-model="rows" :triggers="triggers" :rules="rules" />

          <div class="editor-footer">
            <el-button size="small" @click="cancelEdit">{{ $t('eventCommon.cancel') }}</el-button>
            <el-button size="small" icon="el-icon-setting" @click="openConditionRule">
              {{ $t('eventCommon.goConditionRule') }}
            </el-button>
            <el-button type="primary" size="small" :loading="saving" @click="handleSave">
              {{ $t('eventCommon.save') }}
            </el-button>
          </div>

          <event-report-panel
            v-if="reporterId && eventCode"
            :reporter-id="reporterId"
            :event-code="eventCode"
            :param-types="currentParamTypes"
          />
        </template>
      </el-card>
    </div>
  </div>
</template>

<script>
import { queryConditionRules } from '@/api/conditionRule'
import {
  deleteEventSubscription,
  getEventReporters,
  getReporterEvents,
  getSubscriptionsByEvent,
  getTriggerMetas,
  queryEventSubscriptions,
  saveEventSubscriptions,
  setEventSubscriptionEnabled
} from '@/api/eventSubscription'
import ResponsiveMixin from '@/mixins/responsive'
import { toEventOptions, toReporterOptions, toTriggerOptions, unwrapList, unwrapPage } from '@/utils/eventMeta'
import EventReportPanel from './event/EventReportPanel'
import EventScopePicker from './event/EventScopePicker'
import SubscriptionRows from './event/SubscriptionRows'

/**
 * 事件订阅管理页。
 * <p>
 * 左侧按订阅行展示配置并支持检索、启停与删除；右侧以「事件 → 触发器 → 条件规则」三段式编排，
 * 保存时按事件整体替换订阅，底部调试区可直接上报模拟事件并查看分发明细。
 * </p>
 */
export default {
  name: 'EventSubscriptionManager',
  components: { EventReportPanel, EventScopePicker, SubscriptionRows },
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
      triggers: [],
      rules: [],
      editing: false,
      reporterId: '',
      eventCode: '',
      loadedReporterId: '',
      enabled: 1,
      rows: []
    }
  },
  computed: {
    /**
     * 当前事件的参数声明
     *
     * @returns {Array} 参数定义列表
     */
    currentParamTypes() {
      const matched = this.events.find(item => item.code === this.eventCode)
      return matched && Array.isArray(matched.paramTypes) ? matched.paramTypes : []
    }
  },
  created() {
    this.loadReporters()
    this.loadTriggers()
    this.loadRules()
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
     * 加载触发器元信息
     */
    async loadTriggers() {
      try {
        const res = await getTriggerMetas()
        this.triggers = toTriggerOptions(res)
      } catch (e) {
        console.error('加载触发器失败', e)
        this.triggers = []
      }
    },
    /**
     * 加载条件规则候选
     */
    async loadRules() {
      try {
        const res = await queryConditionRules({}, 1, 200)
        this.rules = unwrapPage(res).list
      } catch (e) {
        console.error('加载条件规则失败', e)
        this.rules = []
      }
    },
    /**
     * 加载订阅列表
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
        const res = await queryEventSubscriptions(criteria, this.pageNum, this.pageSize)
        const page = unwrapPage(res)
        this.items = page.list
        this.total = page.total
      } catch (e) {
        console.error('加载事件订阅失败', e)
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
     * 加载指定事件的订阅组
     *
     * @param {string} reporterId 上报器ID
     * @param {string} eventCode 事件编码
     */
    async loadGroup(reporterId, eventCode) {
      try {
        const res = await getSubscriptionsByEvent(reporterId, eventCode)
        const list = unwrapList(res)
        this.rows = list.map(item => ({
          id: item.id,
          triggerId: item.triggerId,
          ruleCode: item.ruleCode || '',
          sortOrder: item.sortOrder == null ? 0 : item.sortOrder,
          enabled: item.enabled === 0 ? 0 : 1
        }))
        this.enabled = this.rows.some(row => row.enabled === 1) ? 1 : 0
      } catch (e) {
        this.rows = []
      }
    },
    /**
     * 新建订阅组
     */
    createBinding() {
      this.editing = true
      this.reporterId = ''
      this.eventCode = ''
      this.loadedReporterId = ''
      this.enabled = 1
      this.rows = []
      this.events = []
    },
    /**
     * 选中左侧订阅行，加载其所属事件的全部绑定
     *
     * @param {Object} item 订阅行
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
     * @param {Object} item 订阅行
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
    },
    /**
     * 跳转条件规则编排页，并携带当前绑定引用的规则编码
     */
    openConditionRule() {
      const bound = this.rows.find(row => row.ruleCode)
      const query = bound ? { ruleCode: bound.ruleCode } : {}
      this.$router.push({ path: '/condition-rule', query })
    },
    /**
     * 保存订阅组
     */
    async handleSave() {
      if (!this.reporterId || !this.eventCode) {
        this.$message.warning(this.$t('eventCommon.eventRequired'))
        return
      }
      if (!this.rows.length) {
        this.$message.warning(this.$t('eventCommon.bindingRequired'))
        return
      }
      const triggerIds = this.rows.map(row => row.triggerId)
      if (triggerIds.some(id => !id)) {
        this.$message.warning(this.$t('eventCommon.triggerRequired'))
        return
      }
      if (new Set(triggerIds).size !== triggerIds.length) {
        this.$message.warning(this.$t('eventCommon.triggerDuplicate'))
        return
      }
      this.saving = true
      try {
        const payload = {
          reporterId: this.reporterId,
          eventCode: this.eventCode,
          subscriptions: this.rows.map(row => ({
            id: row.id || null,
            reporterId: this.reporterId,
            eventCode: this.eventCode,
            triggerId: row.triggerId,
            ruleCode: row.ruleCode || null,
            sortOrder: row.sortOrder,
            enabled: this.enabled === 1 ? row.enabled : 0
          }))
        }
        await saveEventSubscriptions(payload)
        this.$message.success(this.$t('eventCommon.saved'))
        this.loadList()
        await this.loadGroup(this.reporterId, this.eventCode)
      } finally {
        this.saving = false
      }
    },
    /**
     * 启用或停用单条订阅
     *
     * @param {Object} item 订阅行
     */
    async handleToggle(item) {
      await setEventSubscriptionEnabled(item.id, item.enabled !== 1)
      this.loadList()
    },
    /**
     * 删除单条订阅
     *
     * @param {Object} item 订阅行
     */
    async handleDelete(item) {
      try {
        await this.$confirm(this.$t('eventCommon.removeConfirm'), this.$t('common.tip'), { type: 'warning' })
      } catch (e) {
        return
      }
      await deleteEventSubscription(item.id)
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

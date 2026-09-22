<template>
  <div class="notify-admin">
    <header class="na-header">
      <h2 class="na-title">{{ $t('notifyAdmin.title') }}</h2>
      <p class="na-subtitle">{{ $t('notifyAdmin.subtitle') }}</p>
    </header>

    <el-row :gutter="18" class="na-main">
      <!-- 左列：发布表单 -->
      <el-col :xs="24" :md="14">
        <section class="na-card">
          <el-form
            ref="notifyForm"
            :model="form"
            :rules="formRules"
            label-position="top"
            size="small"
            class="na-form"
            @submit.native.prevent
          >
            <el-form-item :label="$t('notifyAdmin.form.type')">
              <div class="na-seg">
                <button
                  v-for="item in typeOptions"
                  :key="item.value"
                  type="button"
                  class="na-seg-item"
                  :class="{ 'is-active': form.type === item.value }"
                  :style="{ '--accent': item.accent }"
                  @click="form.type = item.value"
                >
                  <i class="na-seg-dot" />{{ $t('push.type.' + item.value) }}
                </button>
              </div>
            </el-form-item>

            <el-form-item :label="$t('notifyAdmin.form.priority')">
              <div class="na-seg">
                <button
                  v-for="item in priorityOptions"
                  :key="item.value"
                  type="button"
                  class="na-seg-item"
                  :class="{ 'is-active': form.priority === item.value }"
                  :style="{ '--accent': item.accent }"
                  @click="form.priority = item.value"
                >
                  {{ $t('push.priority.' + item.value) }}
                </button>
              </div>
            </el-form-item>

            <el-form-item :label="$t('notifyAdmin.form.inputTitle')" prop="title">
              <el-input
                v-model="form.title"
                :placeholder="$t('notifyAdmin.form.titlePlaceholder')"
                :maxlength="limits.title"
                show-word-limit
              />
            </el-form-item>

            <el-form-item :label="$t('notifyAdmin.form.content')" prop="content">
              <el-input
                v-model="form.content"
                type="textarea"
                :rows="5"
                :placeholder="$t('notifyAdmin.form.contentPlaceholder')"
                :maxlength="limits.content"
                show-word-limit
              />
            </el-form-item>

            <el-form-item :label="$t('notifyAdmin.form.url')">
              <el-input v-model="form.url" :placeholder="$t('notifyAdmin.form.urlPlaceholder')" />
            </el-form-item>

            <el-form-item :label="$t('notifyAdmin.form.scope')">
              <div class="na-seg">
                <button
                  type="button"
                  class="na-seg-item"
                  :class="{ 'is-active': form.scope === 'all' }"
                  :style="{ '--accent': '#0a84ff' }"
                  @click="form.scope = 'all'"
                >
                  {{ $t('notifyAdmin.form.scopeAll') }}
                </button>
                <button
                  type="button"
                  class="na-seg-item"
                  :class="{ 'is-active': form.scope === 'users' }"
                  :style="{ '--accent': '#0a84ff' }"
                  @click="form.scope = 'users'"
                >
                  {{ $t('notifyAdmin.form.scopeUsers') }}
                </button>
              </div>
            </el-form-item>

            <el-form-item v-if="form.scope === 'users'" prop="users">
              <el-select
                v-model="form.users"
                multiple
                filterable
                remote
                reserve-keyword
                class="na-users"
                :remote-method="searchUsers"
                :loading="userSearching"
                :placeholder="$t('notifyAdmin.form.usersPlaceholder')"
              >
                <el-option
                  v-for="option in userOptions"
                  :key="option.value"
                  :label="option.label"
                  :value="option.value"
                />
              </el-select>
            </el-form-item>

            <button
              type="button"
              class="na-publish-btn"
              :disabled="publishing"
              @click="handlePublish"
            >
              <i v-if="publishing" class="el-icon-loading" />
              {{ publishing ? $t('notifyAdmin.form.publishing') : $t('notifyAdmin.form.publish') }}
            </button>
          </el-form>
        </section>
      </el-col>

      <!-- 右列：实时预览 -->
      <el-col :xs="24" :md="10">
        <section class="na-card na-preview">
          <h4 class="na-card-head">{{ $t('notifyAdmin.preview.title') }}</h4>
          <notify-preview-card :form="form" />
        </section>
      </el-col>
    </el-row>

    <!-- 发布历史 -->
    <section class="na-card na-history">
      <publish-history-panel :records="records" @remove="handleRemoveRecord" @clear="handleClearRecords" />
    </section>
  </div>
</template>

<script>
import { Notification } from 'element-ui'
import { adminQueryUser } from '@/api/admin_user'
import {
  publishNotify,
  validateNotifyForm,
  NOTIFY_FORM_LIMITS,
  getPublishHistory,
  removeHistoryRecord,
  clearHistory
} from '@/push/admin'
import { MESSAGE_TYPES, PRIORITIES } from '@/push/push-message'
import NotifyPreviewCard from './components/NotifyPreviewCard.vue'
import PublishHistoryPanel from './components/PublishHistoryPanel.vue'

export default {
  name: 'NotifyAdmin',
  components: {
    NotifyPreviewCard,
    PublishHistoryPanel
  },
  data() {
    return {
      form: {
        title: '',
        content: '',
        type: 'notify',
        priority: 'normal',
        url: '',
        scope: 'all',
        users: []
      },
      userOptions: [],
      userSearching: false,
      searchSeq: 0,
      publishing: false,
      records: []
    }
  },
  computed: {
    limits() {
      return NOTIFY_FORM_LIMITS
    },
    typeOptions() {
      return [
        { value: MESSAGE_TYPES.NOTIFY, accent: '#34c759' },
        { value: MESSAGE_TYPES.WARNING, accent: '#ff9f0a' },
        { value: MESSAGE_TYPES.REMINDER, accent: '#0a84ff' }
      ]
    },
    priorityOptions() {
      return [
        { value: PRIORITIES.LOW, accent: '#8e8e93' },
        { value: PRIORITIES.NORMAL, accent: '#0a84ff' },
        { value: PRIORITIES.HIGH, accent: '#ff9f0a' },
        { value: PRIORITIES.URGENT, accent: '#ff3b30' }
      ]
    },
    formRules() {
      return {
        title: [
          { required: true, message: this.$t('notifyAdmin.errors.titleRequired'), trigger: 'blur' },
          { max: this.limits.title, message: this.$t('notifyAdmin.errors.titleTooLong', { max: this.limits.title }), trigger: 'blur' }
        ],
        content: [
          { required: true, message: this.$t('notifyAdmin.errors.contentRequired'), trigger: 'blur' },
          { max: this.limits.content, message: this.$t('notifyAdmin.errors.contentTooLong', { max: this.limits.content }), trigger: 'blur' }
        ],
        users: [
          {
            validator: (rule, value, callback) => {
              if (this.form.scope === 'users' && (!this.form.users || !this.form.users.length)) {
                callback(new Error(this.$t('notifyAdmin.errors.usersEmpty')))
                return
              }
              callback()
            },
            trigger: 'change'
          }
        ]
      }
    }
  },
  mounted() {
    this.loadHistory()
  },
  methods: {
    /**
     * 远程搜索用户（adminQueryUser，按用户名过滤，分页取前 20 条）
     * 串行令牌防止乱序响应覆盖结果；已选项始终保留在选项中以维持回显标签
     */
    async searchUsers(keyword) {
      const seq = ++this.searchSeq
      this.userSearching = true
      try {
        const res = await adminQueryUser({ userName: keyword || undefined }, 1, 20)
        if (seq !== this.searchSeq) {
          return
        }
        const list = (res.data && res.data.data) || []
        const fetched = list
          .filter(item => item && item.userName)
          .map(item => ({
            value: item.userName,
            label: item.nickName ? item.nickName + ' (' + item.userName + ')' : item.userName
          }))
        const selected = this.userOptions.filter(option => this.form.users.includes(option.value))
        const merged = selected.slice()
        fetched.forEach(option => {
          if (!merged.some(existing => existing.value === option.value)) {
            merged.push(option)
          }
        })
        this.userOptions = merged
      } catch (e) {
        console.warn('[notify-admin] 用户搜索失败', e)
      } finally {
        if (seq === this.searchSeq) {
          this.userSearching = false
        }
      }
    },
    /**
     * 发布：页面 el-form 校验 + 封装层结构化校验双保险，
     * 成功/部分失败/全失败分别给出状态反馈，完成后刷新本地历史
     */
    handlePublish() {
      this.$refs.notifyForm.validate(async valid => {
        if (!valid) {
          return
        }
        const check = validateNotifyForm({ ...this.form })
        if (!check.valid) {
          this.$message.error(this.mapValidationError(check.errors))
          return
        }
        this.publishing = true
        try {
          const result = await publishNotify({ ...this.form })
          this.loadHistory()
          this.reportPublishResult(result)
        } finally {
          this.publishing = false
        }
      })
    },
    /**
     * 发布结果反馈：全成功 Notification.success（含送达数）、
     * 部分失败 Notification.warning（列失败目标）、全失败 Message.error
     */
    reportPublishResult(result) {
      const { summary, results } = result
      if (summary.delivered === 0) {
        this.$message.error(this.$t('notifyAdmin.message.publishFailed'))
        return
      }
      if (!result.ok) {
        const targets = results.filter(item => !item.ok).map(item => item.target).join(', ')
        Notification.warning({
          title: this.$t('notifyAdmin.title'),
          message: this.$t('notifyAdmin.message.publishPartial', {
            delivered: summary.delivered,
            failed: summary.failed,
            targets
          })
        })
        return
      }
      Notification.success({
        title: this.$t('notifyAdmin.title'),
        message: this.$t('notifyAdmin.message.publishSuccess', {
          delivered: summary.delivered,
          total: summary.total
        })
      })
    },
    /**
     * 将封装层的结构化校验错误映射为 i18n 文案（取首条）
     */
    mapValidationError(errors) {
      const first = errors && errors.length ? errors[0] : {}
      const keyMap = {
        title: { required: 'titleRequired', tooLong: 'titleTooLong' },
        content: { required: 'contentRequired', tooLong: 'contentTooLong' },
        users: { empty: 'usersEmpty' }
      }
      const fieldMap = keyMap[first.field]
      const key = fieldMap && fieldMap[first.code]
      if (!key) {
        return this.$t('notifyAdmin.errors.invalid')
      }
      if (key === 'titleTooLong' || key === 'contentTooLong') {
        return this.$t('notifyAdmin.errors.' + key, { max: NOTIFY_FORM_LIMITS[first.field] })
      }
      return this.$t('notifyAdmin.errors.' + key)
    },
    loadHistory() {
      this.records = getPublishHistory()
    },
    handleRemoveRecord(id) {
      removeHistoryRecord(id)
      this.loadHistory()
    },
    handleClearRecords() {
      this.$confirm(
        this.$t('notifyAdmin.history.clearConfirm'),
        this.$t('notifyAdmin.history.title'),
        { type: 'warning', confirmButtonText: this.$t('common.confirm') }
      ).then(() => {
        clearHistory()
        this.loadHistory()
      }).catch(() => {})
    }
  }
}
</script>

<style lang="scss" scoped>
.notify-admin {
  padding: 20px 22px 30px;
}

.na-header {
  margin-bottom: 18px;

  .na-title {
    margin: 0 0 6px;
    font-size: 20px;
    font-weight: 600;
    color: #1d1d1f;
    letter-spacing: 0.02em;
  }

  .na-subtitle {
    margin: 0;
    font-size: 13px;
    color: #6e6e73;
  }
}

.na-main {
  margin-bottom: 18px;
}

.na-card {
  background: rgba(255, 255, 255, 0.72);
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: 16px;
  padding: 20px 22px;
  box-shadow:
    0 12px 40px rgba(12, 18, 38, 0.08),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
}

.na-card-head {
  margin: 0 0 16px;
  font-size: 14px;
  font-weight: 600;
  color: #1d1d1f;
}

/* 分段选择器：类型/优先级/范围共用 */
.na-seg {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.na-seg-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 7px 14px;
  border-radius: 999px;
  border: 1px solid rgba(0, 0, 0, 0.09);
  background: rgba(255, 255, 255, 0.7);
  font-family: inherit;
  font-size: 12.5px;
  color: #3a3a3c;
  cursor: pointer;
  transition: all 0.18s ease;

  .na-seg-dot {
    width: 7px;
    height: 7px;
    border-radius: 50%;
    background: var(--accent);
  }

  &:hover {
    border-color: var(--accent);
  }

  &.is-active {
    border-color: var(--accent);
    color: #1d1d1f;
    font-weight: 600;
    background: rgba(255, 255, 255, 0.95);
    box-shadow: 0 0 0 3px rgba(0, 0, 0, 0.05);
  }
}

.na-users {
  width: 100%;
}

.na-publish-btn {
  width: 100%;
  height: 42px;
  margin-top: 4px;
  border: none;
  border-radius: 12px;
  color: #fff;
  font-family: inherit;
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 0.04em;
  cursor: pointer;
  background: linear-gradient(135deg, #0a84ff, #3ea0ff);
  box-shadow: 0 8px 24px rgba(10, 132, 255, 0.35);
  transition: all 0.2s ease;

  &:hover:not(:disabled) {
    transform: translateY(-1px);
    box-shadow: 0 12px 30px rgba(10, 132, 255, 0.45);
  }

  &:disabled {
    opacity: 0.7;
    cursor: not-allowed;
  }
}

.na-history {
  margin-top: 0;
}
</style>

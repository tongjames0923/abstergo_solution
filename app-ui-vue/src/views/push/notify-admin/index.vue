<template>
  <div class="notify-admin">
    <header class="na-hero">
      <div class="na-hero-text">
        <span class="na-eyebrow">{{ $t('notifyAdmin.hero.eyebrow') }}</span>
        <h2 class="na-title">{{ $t('notifyAdmin.title') }}</h2>
        <p class="na-subtitle">{{ $t('notifyAdmin.subtitle') }}</p>
      </div>
      <ul class="na-hero-chips">
        <li v-for="chip in heroChips" :key="chip.key" class="na-hero-chip">
          <i class="na-chip-dot" :style="{ background: chip.accent }" />{{ $t('notifyAdmin.hero.' + chip.key) }}
        </li>
      </ul>
    </header>

    <el-row :gutter="18" class="na-main">
      <!-- 左列：发布表单 -->
      <el-col :xs="24" :md="14">
        <section class="na-card na-card--form">
          <el-form
            ref="notifyForm"
            :model="form"
            :rules="formRules"
            label-position="top"
            size="small"
            class="na-form"
            @submit.native.prevent
          >
            <el-row :gutter="14">
              <el-col :span="12">
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
              </el-col>
              <el-col :span="12">
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
              </el-col>
            </el-row>

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
              <div class="na-scope">
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

                <el-select
                  v-if="form.scope === 'users'"
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

                <!-- 投递通道：与后端 TopicPrefixNotifyReceiverResolver 的 topic 解析规则同构 -->
                <div class="na-topic">
                  <span class="na-topic-label">{{ $t('notifyAdmin.form.effectiveTopic') }}</span>
                  <div class="na-topic-chips">
                    <code
                      v-for="chip in topicChips"
                      :key="chip.text"
                      class="na-topic-chip"
                      :class="'is-' + chip.tone"
                    >{{ chip.text }}</code>
                    <span v-if="!topicChips.length" class="na-topic-empty">—</span>
                  </div>
                </div>
              </div>
            </el-form-item>

            <button
              type="button"
              class="na-publish-btn"
              :class="{ 'is-busy': publishing }"
              :disabled="publishing"
              @click="handlePublish"
            >
              <i v-if="publishing" class="el-icon-loading" />
              {{ publishing ? $t('notifyAdmin.form.publishing') : $t('notifyAdmin.form.publish') }}
            </button>
          </el-form>
        </section>
      </el-col>

      <!-- 右列：实时预览（消息中心卡片 / 在线弹窗双视角） -->
      <el-col :xs="24" :md="10">
        <section class="na-card na-card--preview">
          <div class="na-card-head-row">
            <h4 class="na-card-head">{{ $t('notifyAdmin.preview.title') }}</h4>
            <div class="na-mode">
              <button
                type="button"
                class="na-mode-item"
                :class="{ 'is-active': previewMode === 'center' }"
                @click="previewMode = 'center'"
              >
                {{ $t('notifyAdmin.preview.modeCenter') }}
              </button>
              <button
                type="button"
                class="na-mode-item"
                :class="{ 'is-active': previewMode === 'toast' }"
                @click="previewMode = 'toast'"
              >
                {{ $t('notifyAdmin.preview.modeToast') }}
              </button>
            </div>
          </div>
          <notify-preview-card :form="form" :mode="previewMode" />
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
import { adminQueryUser } from '@/api/admin_user'
import {
  publishNotify,
  validateNotifyForm,
  NOTIFY_FORM_LIMITS,
  getPublishHistory,
  removeHistoryRecord,
  clearHistory
} from '@/push/admin'
import {
  MESSAGE_TYPES,
  PRIORITIES,
  TOPIC_BROADCAST,
  TOPIC_USER_NAME_PREFIX
} from '@/push/push-message'
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
      previewMode: 'center',
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
    /**
     * Hero 能力徽章：对应新链路「持久化存储 / 信令实时触达 / 离线补拉」三大特征
     */
    heroChips() {
      return [
        { key: 'persist', accent: '#0a84ff' },
        { key: 'realtime', accent: '#34c759' },
        { key: 'offline', accent: '#ff9f0a' }
      ]
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
    /**
     * 投递通道预览：全体广播走 global.broadcast 单 topic，指定用户逐人
     * user.name.{userName} 精确 topic（展示前 3 个，其余折叠计数）
     */
    topicChips() {
      if (this.form.scope === 'all') {
        return [{ text: TOPIC_BROADCAST, tone: 'all' }]
      }
      const users = this.form.users || []
      const chips = users.slice(0, 3).map(user => ({ text: TOPIC_USER_NAME_PREFIX + user, tone: 'user' }))
      if (users.length > 3) {
        chips.push({ text: '+' + (users.length - 3), tone: 'more' })
      }
      return chips
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
     * 发布结果反馈（对齐全库 $message 反馈约定）：
     * 送达事实以后端 receiverCount 为准——请求全挂 error 判网络失败，
     * receivers=0 判未解析到接收人（消息仅落库），部分目标未解析列目标，全成功报送达人数
     */
    reportPublishResult(result) {
      const { summary, results } = result
      if (summary.total > 0 && results.every(item => item.error)) {
        this.$message.error(this.$t('notifyAdmin.message.publishFailed'))
        return
      }
      if (summary.receivers === 0) {
        this.$message.warning(this.$t('notifyAdmin.message.publishZeroReceiver'))
        return
      }
      if (!result.ok) {
        const targets = results.filter(item => !item.ok).map(item => item.target).join(', ')
        this.$message.warning(this.$t('notifyAdmin.message.publishPartial', {
          receivers: summary.receivers,
          targets
        }))
        return
      }
      this.$message.success(this.$t('notifyAdmin.message.publishSuccess', {
        receivers: summary.receivers
      }))
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
  position: relative;
  padding: 24px 22px 34px;

  /* 环境光：三色语义光斑呼应 notify/warning/reminder */
  &::before {
    content: '';
    position: absolute;
    inset: 0;
    pointer-events: none;
    background:
      radial-gradient(560px 300px at 12% -4%, rgba(10, 132, 255, 0.09), transparent 70%),
      radial-gradient(480px 260px at 96% 8%, rgba(52, 199, 89, 0.07), transparent 70%),
      radial-gradient(420px 240px at 55% 110%, rgba(255, 159, 10, 0.06), transparent 70%);
  }

  /* 细颗粒噪点，压住渐变的塑料感 */
  &::after {
    content: '';
    position: absolute;
    inset: 0;
    pointer-events: none;
    opacity: 0.5;
    background-image: url('data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="120" height="120"><filter id="n"><feTurbulence type="fractalNoise" baseFrequency="0.9" numOctaves="2"/><feColorMatrix type="saturate" values="0"/></filter><rect width="120" height="120" filter="url(%23n)" opacity="0.04"/></svg>');
  }

  > * {
    position: relative;
    z-index: 1;
  }
}

/* ---------- Hero ---------- */
.na-hero {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 14px;
  padding-bottom: 18px;
  margin-bottom: 20px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.07);
}

.na-eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-family: 'DIN Alternate', 'Avenir Next', 'Helvetica Neue', sans-serif;
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 0.32em;
  text-transform: uppercase;
  color: #0a84ff;
  margin-bottom: 8px;

  &::before {
    content: '';
    width: 22px;
    height: 1px;
    background: linear-gradient(90deg, #0a84ff, transparent);
  }
}

.na-title {
  margin: 0 0 6px;
  font-size: 22px;
  font-weight: 700;
  color: #1d1d1f;
  letter-spacing: 0.02em;
}

.na-subtitle {
  margin: 0;
  max-width: 520px;
  font-size: 13px;
  line-height: 1.6;
  color: #6e6e73;
}

.na-hero-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.na-hero-chip {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 6px 13px;
  border-radius: 999px;
  border: 1px solid rgba(0, 0, 0, 0.07);
  background: rgba(255, 255, 255, 0.66);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  font-size: 11.5px;
  letter-spacing: 0.04em;
  color: #3a3a3c;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.na-chip-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  box-shadow: 0 0 6px rgba(0, 0, 0, 0.15);
}

/* ---------- 布局与卡片 ---------- */
.na-main {
  margin-bottom: 18px;
}

.na-card {
  background: rgba(255, 255, 255, 0.66);
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: 18px;
  padding: 22px 24px;
  box-shadow:
    0 14px 44px rgba(12, 18, 38, 0.09),
    inset 0 1px 0 rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(22px);
  -webkit-backdrop-filter: blur(22px);
}

.na-card-head {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #1d1d1f;
}

.na-card-head-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 18px;
}

.na-card--preview {
  position: sticky;
  top: 84px;
}

/* ---------- 预览模式切换 ---------- */
.na-mode {
  display: inline-flex;
  padding: 3px;
  border-radius: 999px;
  background: rgba(0, 0, 0, 0.05);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

.na-mode-item {
  appearance: none;
  border: none;
  background: transparent;
  font-family: inherit;
  font-size: 11px;
  color: #86868b;
  padding: 4px 12px;
  border-radius: 999px;
  cursor: pointer;
  transition: all 0.18s ease;

  &.is-active {
    background: #fff;
    color: #1d1d1f;
    font-weight: 600;
    box-shadow: 0 2px 8px rgba(12, 18, 38, 0.12);
  }
}

/* ---------- 分段选择器：类型/优先级/范围共用 ---------- */
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
    transform: translateY(-1px);
  }

  &.is-active {
    border-color: var(--accent);
    color: #1d1d1f;
    font-weight: 600;
    background: rgba(255, 255, 255, 0.95);
    box-shadow:
      0 0 0 3px rgba(0, 0, 0, 0.05),
      0 4px 14px rgba(12, 18, 38, 0.08);
  }
}

/* ---------- 投递通道 ---------- */
.na-scope {
  width: 100%;

  .na-users {
    width: 100%;
    margin-top: 10px;
  }
}

.na-topic {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-top: 12px;
  padding: 9px 12px;
  border-radius: 10px;
  background: rgba(10, 132, 255, 0.05);
  border: 1px dashed rgba(10, 132, 255, 0.2);
}

.na-topic-label {
  flex-shrink: 0;
  font-size: 11px;
  letter-spacing: 0.08em;
  color: #6e6e73;
}

.na-topic-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  min-width: 0;
}

.na-topic-chip {
  font-family: 'SF Mono', 'DIN Alternate', Menlo, monospace;
  font-size: 11px;
  letter-spacing: 0.02em;
  padding: 2px 9px;
  border-radius: 6px;
  white-space: nowrap;

  &.is-all {
    color: #0a84ff;
    background: rgba(10, 132, 255, 0.1);
    border: 1px solid rgba(10, 132, 255, 0.2);
  }

  &.is-user {
    color: #1d1d1f;
    background: rgba(255, 255, 255, 0.85);
    border: 1px solid rgba(0, 0, 0, 0.08);
  }

  &.is-more {
    color: #86868b;
    background: rgba(0, 0, 0, 0.04);
    border: 1px solid rgba(0, 0, 0, 0.06);
  }
}

.na-topic-empty {
  font-size: 11px;
  color: #a1a1a6;
}

/* ---------- 发布按钮 ---------- */
.na-publish-btn {
  position: relative;
  overflow: hidden;
  width: 100%;
  height: 44px;
  margin-top: 6px;
  border: none;
  border-radius: 13px;
  color: #fff;
  font-family: inherit;
  font-size: 14px;
  font-weight: 600;
  letter-spacing: 0.06em;
  cursor: pointer;
  background: linear-gradient(135deg, #0a84ff, #3ea0ff);
  box-shadow:
    0 10px 26px rgba(10, 132, 255, 0.35),
    inset 0 1px 0 rgba(255, 255, 255, 0.35);
  transition: all 0.2s ease;

  /* 掠光：发布通道激活时的信号扫过 */
  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: -80%;
    width: 60%;
    height: 100%;
    background: linear-gradient(105deg, transparent, rgba(255, 255, 255, 0.45), transparent);
    transform: skewX(-18deg);
    transition: none;
  }

  &:hover:not(:disabled)::after {
    left: 130%;
    transition: left 0.7s ease;
  }

  &:hover:not(:disabled) {
    transform: translateY(-1px);
    box-shadow: 0 14px 32px rgba(10, 132, 255, 0.45);
  }

  &.is-busy::after {
    animation: na-scan 1.1s linear infinite;
  }

  &:disabled {
    opacity: 0.72;
    cursor: not-allowed;
  }
}

@keyframes na-scan {
  0% {
    left: -80%;
  }
  100% {
    left: 130%;
  }
}

.na-history {
  margin-top: 0;
}

@media (max-width: 768px) {
  .na-hero {
    align-items: flex-start;
    flex-direction: column;
  }

  .na-card--preview {
    position: static;
  }
}
</style>

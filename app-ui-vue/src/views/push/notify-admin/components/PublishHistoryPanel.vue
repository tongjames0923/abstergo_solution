<template>
  <div class="ph-panel">
    <div class="ph-head">
      <h4 class="ph-title">{{ $t('notifyAdmin.history.title') }}</h4>
      <span class="ph-count">{{ $t('notifyAdmin.history.count', { n: records.length }) }}</span>
      <span class="ph-tip">{{ $t('notifyAdmin.history.localTip') }}</span>
      <button
        type="button"
        class="ph-clear"
        :disabled="!records.length"
        @click="$emit('clear')"
      >
        {{ $t('notifyAdmin.history.clear') }}
      </button>
    </div>

    <div v-if="!records.length" class="ph-empty">
      <i class="el-icon-document" />
      <p>{{ $t('notifyAdmin.history.empty') }}</p>
    </div>

    <div v-else class="ph-table">
      <div class="ph-row ph-row--head">
        <span>{{ $t('notifyAdmin.history.colTime') }}</span>
        <span>{{ $t('notifyAdmin.history.colTitle') }}</span>
        <span>{{ $t('notifyAdmin.history.colType') }}</span>
        <span>{{ $t('notifyAdmin.history.colPriority') }}</span>
        <span>{{ $t('notifyAdmin.history.colScope') }}</span>
        <span>{{ $t('notifyAdmin.history.colReceipt') }}</span>
        <span />
      </div>
      <div
        v-for="item in records"
        :key="item.id"
        class="ph-row"
        :style="{ '--accent': accentOf(item.type), '--pri': priorityOf(item.priority) }"
      >
        <span class="ph-time">{{ formatTime(item.publishedAt) }}</span>
        <span class="ph-cell-title" :title="item.title">{{ item.title }}</span>
        <span class="ph-type"><i class="ph-dot" />{{ $t('push.type.' + item.type) }}</span>
        <span class="ph-priority">{{ $t('push.priority.' + item.priority) }}</span>
        <span class="ph-scope" :title="scopeText(item)">{{ scopeText(item) }}</span>
        <span class="ph-receipt" :class="receiptClass(item)">
          <i class="ph-receipt-dot" />{{ receiptText(item) }}
        </span>
        <span class="ph-ops">
          <button type="button" class="ph-remove" @click="$emit('remove', item.id)">
            {{ $t('notifyAdmin.history.remove') }}
          </button>
        </span>
      </div>
    </div>
  </div>
</template>

<script>
/**
 * 发布历史面板
 * <p>
 * 以玻璃表格呈现本地发布记录（时间/标题/类型/优先级/范围/送达回执）。
 * 回执语义与后端 PushSendResultVO 对齐：delivered=后端 SPI 解析到的接收人总数，
 * failed=未解析到接收人的目标数（如用户名不存在），total=delivered+failed。
 * 回执按投递结果着色：全部送达（绿）/ 部分送达（橙）/ 无人送达（红）。
 * 支持单条删除与清空（仅作用于本浏览器 localStorage 记录，不代表全局发送状态）。
 * 数据由父组件注入，本组件只负责展示与动作上抛。
 * </p>
 */
const ACCENTS = {
  notify: '#34c759',
  warning: '#ff9f0a',
  reminder: '#0a84ff'
}

const PRIORITY_ACCENTS = {
  low: '#8e8e93',
  normal: '#0a84ff',
  high: '#ff9f0a',
  urgent: '#ff3b30'
}

export default {
  name: 'PublishHistoryPanel',
  props: {
    records: {
      type: Array,
      default: () => []
    }
  },
  methods: {
    accentOf(type) {
      return ACCENTS[type] || '#8e8e93'
    },
    priorityOf(priority) {
      return PRIORITY_ACCENTS[priority] || '#8e8e93'
    },
    formatTime(ts) {
      if (!ts) {
        return '-'
      }
      const date = new Date(ts)
      const pad = value => String(value).padStart(2, '0')
      return date.getFullYear() + '-' + pad(date.getMonth() + 1) + '-' + pad(date.getDate()) +
        ' ' + pad(date.getHours()) + ':' + pad(date.getMinutes())
    },
    scopeText(item) {
      if (item.scope === 'all') {
        return this.$t('notifyAdmin.history.scopeAll')
      }
      const targets = item.targets || []
      const text = targets.join(', ')
      return text.length > 24 ? text.slice(0, 24) + '…' : text
    },
    /**
     * 回执状态类名：按送达比例划分 ok（全部）/ partial（部分）/ fail（全失败）
     */
    receiptClass(item) {
      const total = item.delivered + item.failed
      if (item.delivered === 0) {
        return 'is-fail'
      }
      return item.delivered === total ? 'is-ok' : 'is-partial'
    },
    receiptText(item) {
      const total = item.delivered + item.failed
      if (item.delivered === 0) {
        return this.$t('notifyAdmin.history.receiptFail')
      }
      if (item.delivered === total) {
        return this.$t('notifyAdmin.history.receiptOk')
      }
      return this.$t('notifyAdmin.history.receiptText', {
        delivered: item.delivered,
        total
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.ph-panel {
  display: flex;
  flex-direction: column;
}

.ph-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
}

.ph-title {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
  color: #1d1d1f;
}

.ph-count {
  font-family: 'DIN Alternate', 'Avenir Next', 'Helvetica Neue', sans-serif;
  font-size: 10.5px;
  font-variant-numeric: tabular-nums;
  color: #0a84ff;
  background: rgba(10, 132, 255, 0.08);
  border: 1px solid rgba(10, 132, 255, 0.18);
  padding: 1px 9px;
  border-radius: 999px;
}

.ph-tip {
  flex: 1;
  font-size: 11px;
  color: #a1a1a6;
  letter-spacing: 0.02em;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ph-clear {
  appearance: none;
  border: 1px solid rgba(255, 59, 48, 0.25);
  background: rgba(255, 59, 48, 0.06);
  color: #ff3b30;
  font-family: inherit;
  font-size: 11px;
  padding: 4px 12px;
  border-radius: 999px;
  cursor: pointer;
  transition: all 0.18s ease;

  &:hover:not(:disabled) {
    background: #ff3b30;
    color: #fff;
    box-shadow: 0 4px 14px rgba(255, 59, 48, 0.3);
  }
  &:disabled {
    color: #c7c7cc;
    border-color: rgba(0, 0, 0, 0.08);
    background: rgba(255, 255, 255, 0.5);
    cursor: default;
  }
}

.ph-empty {
  padding: 36px 0;
  text-align: center;
  color: #a1a1a6;

  i {
    font-size: 26px;
    display: block;
    margin-bottom: 8px;
    opacity: 0.55;
  }

  p {
    margin: 0;
    font-size: 12.5px;
    letter-spacing: 0.12em;
  }
}

.ph-table {
  display: flex;
  flex-direction: column;
}

.ph-row {
  display: grid;
  grid-template-columns: 128px 1fr 64px 52px 140px 128px 64px;
  gap: 12px;
  align-items: center;
  padding: 10px 12px;
  border-radius: 10px;
  font-size: 12.5px;
  color: #3a3a3c;

  & + .ph-row {
    margin-top: 4px;
  }

  &:not(.ph-row--head):hover {
    background: rgba(255, 255, 255, 0.85);
    box-shadow: inset 0 0 0 1px rgba(0, 0, 0, 0.05);
  }

  &--head {
    font-size: 11px;
    letter-spacing: 0.08em;
    color: #a1a1a6;
    border-bottom: 1px solid rgba(0, 0, 0, 0.07);
    border-radius: 0;
    padding-bottom: 8px;
  }
}

.ph-time {
  font-family: 'DIN Alternate', 'Avenir Next', 'Helvetica Neue', sans-serif;
  font-variant-numeric: tabular-nums;
  color: #6e6e73;
}

.ph-cell-title {
  font-weight: 500;
  color: #1d1d1f;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ph-type {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  color: #86868b;
}

.ph-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--accent);
}

.ph-priority {
  color: var(--pri);
  font-weight: 500;
}

.ph-scope {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #6e6e73;
}

.ph-receipt {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-family: 'DIN Alternate', 'Avenir Next', 'Helvetica Neue', sans-serif;
  font-variant-numeric: tabular-nums;
  font-size: 11.5px;
  padding: 2px 9px;
  border-radius: 999px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;

  &.is-ok {
    color: #1d7a3d;
    background: rgba(52, 199, 89, 0.1);
    border: 1px solid rgba(52, 199, 89, 0.22);

    .ph-receipt-dot {
      background: #34c759;
    }
  }

  &.is-partial {
    color: #9a6200;
    background: rgba(255, 159, 10, 0.1);
    border: 1px solid rgba(255, 159, 10, 0.24);

    .ph-receipt-dot {
      background: #ff9f0a;
    }
  }

  &.is-fail {
    color: #c53030;
    background: rgba(255, 59, 48, 0.08);
    border: 1px solid rgba(255, 59, 48, 0.2);

    .ph-receipt-dot {
      background: #ff3b30;
    }
  }
}

.ph-receipt-dot {
  flex-shrink: 0;
  width: 5px;
  height: 5px;
  border-radius: 50%;
}

.ph-ops {
  text-align: right;
}

.ph-remove {
  appearance: none;
  border: none;
  background: transparent;
  color: #ff3b30;
  font-family: inherit;
  font-size: 11.5px;
  cursor: pointer;
  padding: 3px 6px;
  border-radius: 6px;
  transition: background 0.15s ease;

  &:hover {
    background: rgba(255, 59, 48, 0.08);
  }
}
</style>

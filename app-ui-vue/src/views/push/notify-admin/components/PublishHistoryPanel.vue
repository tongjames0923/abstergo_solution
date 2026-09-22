<template>
  <div class="ph-panel">
    <div class="ph-head">
      <h4 class="ph-title">{{ $t('notifyAdmin.history.title') }}</h4>
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
      {{ $t('notifyAdmin.history.empty') }}
    </div>

    <div v-else class="ph-table">
      <div class="ph-row ph-row--head">
        <span>{{ $t('notifyAdmin.history.colTime') }}</span>
        <span>{{ $t('notifyAdmin.history.colTitle') }}</span>
        <span>{{ $t('notifyAdmin.history.colType') }}</span>
        <span>{{ $t('notifyAdmin.history.colScope') }}</span>
        <span>{{ $t('notifyAdmin.history.colReceipt') }}</span>
        <span />
      </div>
      <div
        v-for="item in records"
        :key="item.id"
        class="ph-row"
        :style="{ '--accent': accentOf(item.type) }"
      >
        <span class="ph-time">{{ formatTime(item.publishedAt) }}</span>
        <span class="ph-cell-title" :title="item.title">{{ item.title }}</span>
        <span class="ph-type"><i class="ph-dot" />{{ $t('push.type.' + item.type) }}</span>
        <span class="ph-scope" :title="scopeText(item)">{{ scopeText(item) }}</span>
        <span class="ph-receipt">{{ receiptText(item) }}</span>
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
 * 以玻璃表格呈现本地发布记录（时间/标题/类型/范围/送达回执），
 * 支持单条删除与清空（仅作用于本浏览器 localStorage 记录，不代表全局发送状态）。
 * 数据由父组件注入，本组件只负责展示与动作上抛。
 * </p>
 */
const ACCENTS = {
  notify: '#34c759',
  warning: '#ff9f0a',
  reminder: '#0a84ff'
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
    receiptText(item) {
      return this.$t('notifyAdmin.history.receiptText', {
        delivered: item.delivered,
        total: item.delivered + item.failed
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

.ph-tip {
  flex: 1;
  font-size: 11px;
  color: #a1a1a6;
  letter-spacing: 0.02em;
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
  padding: 34px 0;
  text-align: center;
  font-size: 12.5px;
  letter-spacing: 0.12em;
  color: #a1a1a6;
}

.ph-table {
  display: flex;
  flex-direction: column;
}

.ph-row {
  display: grid;
  grid-template-columns: 132px 1fr 76px 150px 150px 76px;
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

.ph-scope,
.ph-receipt {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #6e6e73;
}

.ph-receipt {
  font-family: 'DIN Alternate', 'Avenir Next', 'Helvetica Neue', sans-serif;
  font-variant-numeric: tabular-nums;
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

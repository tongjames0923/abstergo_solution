<template>
  <div class="pv-wrap">
    <span class="pv-badge">{{ $t('notifyAdmin.preview.badge') }}</span>
    <div class="pv-card" :style="{ '--accent': accent }">
      <span class="pv-rail" />
      <div class="pv-body">
        <div class="pv-head">
          <span class="pv-chip"><i class="pv-dot" />{{ $t('push.type.' + form.type) }}</span>
          <span v-if="isHighPriority" class="pv-priority">
            {{ $t('push.priority.' + form.priority) }}
          </span>
        </div>
        <p class="pv-title">{{ form.title || $t('notifyAdmin.form.titlePlaceholder') }}</p>
        <p class="pv-content">{{ form.content || $t('notifyAdmin.preview.empty') }}</p>
        <div class="pv-foot">
          <span class="pv-time">{{ timeText }}</span>
          <i class="pv-unread" />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
/**
 * 通知实时预览卡
 * <p>
 * 与消息中心（PushNotificationCenter）消息卡片同构的视觉：语义色导轨、类型 chip、
 * 优先级徽章、未读辉光圆点，随表单输入实时重渲染，呈现"所见即用户所得"效果。
 * </p>
 */
const ACCENTS = {
  notify: '#34c759',
  warning: '#ff9f0a',
  reminder: '#0a84ff'
}

export default {
  name: 'NotifyPreviewCard',
  props: {
    form: {
      type: Object,
      required: true
    }
  },
  computed: {
    accent() {
      return ACCENTS[this.form.type] || '#8e8e93'
    },
    isHighPriority() {
      return this.form.priority === 'high' || this.form.priority === 'urgent'
    },
    timeText() {
      return new Date().toLocaleTimeString()
    }
  }
}
</script>

<style lang="scss" scoped>
.pv-wrap {
  position: relative;
  padding-top: 10px;
}

.pv-badge {
  position: absolute;
  top: 0;
  right: 0;
  z-index: 1;
  font-size: 10px;
  letter-spacing: 0.14em;
  color: #0a84ff;
  background: rgba(10, 132, 255, 0.1);
  border: 1px solid rgba(10, 132, 255, 0.22);
  border-radius: 999px;
  padding: 2px 10px;
}

.pv-card {
  position: relative;
  display: flex;
  align-items: stretch;
  background:
    linear-gradient(100deg, rgba(255, 255, 255, 0), rgba(255, 255, 255, 0) 42%),
    rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(0, 0, 0, 0.07);
  border-radius: 14px;
  padding: 14px 16px 14px 18px;
  overflow: hidden;
  box-shadow: 0 10px 30px rgba(12, 18, 38, 0.08);
  transition: transform 0.2s ease, box-shadow 0.2s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 16px 40px rgba(12, 18, 38, 0.12);
  }
}

.pv-rail {
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: var(--accent);
}

.pv-body {
  flex: 1;
  min-width: 0;
}

.pv-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.pv-chip {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 10px;
  letter-spacing: 0.1em;
  color: #86868b;
}

.pv-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--accent);
}

.pv-priority {
  font-size: 10px;
  font-weight: 600;
  letter-spacing: 0.08em;
  color: #ff3b30;
  padding: 2px 8px;
  border-radius: 999px;
  background: rgba(255, 59, 48, 0.09);
  border: 1px solid rgba(255, 59, 48, 0.18);
}

.pv-title {
  margin: 9px 0 5px;
  font-size: 14px;
  font-weight: 600;
  color: #1d1d1f;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.pv-content {
  margin: 0;
  font-size: 12.5px;
  color: #6e6e73;
  line-height: 1.55;
  min-height: 38px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.pv-foot {
  margin-top: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.pv-time {
  font-family: 'DIN Alternate', 'Avenir Next', 'Helvetica Neue', sans-serif;
  font-size: 11px;
  letter-spacing: 0.04em;
  color: #a1a1a6;
  font-variant-numeric: tabular-nums;
}

.pv-unread {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: var(--accent);
  box-shadow: 0 0 8px rgba(0, 0, 0, 0.18);
}
</style>

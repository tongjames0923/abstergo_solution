<template>
  <div class="pn-state">
    <!-- 加载骨架：玻璃占位 shimmer，逐张错峰浮现 -->
    <div
      v-if="mode === 'loading'"
      class="pn-skeleton"
      aria-live="polite"
    >
      <div
        v-for="n in 3"
        :key="n"
        class="pn-skel-card"
      >
        <div class="pn-skel-line pn-skel-line--chip" />
        <div class="pn-skel-line pn-skel-line--title" />
        <div class="pn-skel-line pn-skel-line--body" />
        <div class="pn-skel-line pn-skel-line--meta" />
      </div>
    </div>

    <!-- 空态：熄灭的信标 -->
    <div
      v-else-if="mode === 'empty'"
      class="pn-empty"
    >
      <div class="pn-empty-beacon">
        <i class="el-icon-bell" />
      </div>
      <p class="pn-empty-text">{{ $t('push.center.empty') }}</p>
    </div>

    <!-- 筛选无结果：漏斗 + 一键清除筛选 -->
    <div
      v-else-if="mode === 'filtered-empty'"
      class="pn-empty"
    >
      <div class="pn-empty-beacon pn-empty-beacon--filtered">
        <i class="el-icon-s-operation" />
      </div>
      <p class="pn-empty-text">{{ $t('push.center.emptyFiltered') }}</p>
      <button
        type="button"
        class="pn-state-btn"
        @click="$emit('clear-filter')"
      >
        {{ $t('push.center.clearFilter') }}
      </button>
    </div>

    <!-- 错误态：警示 + 重试 -->
    <div
      v-else-if="mode === 'error'"
      class="pn-error"
    >
      <div class="pn-error-icon">
        <i class="el-icon-warning-outline" />
      </div>
      <p class="pn-error-title">{{ $t('push.center.error.title') }}</p>
      <p class="pn-error-hint">{{ $t('push.center.error.hint') }}</p>
      <button
        type="button"
        class="pn-state-btn"
        @click="$emit('retry')"
      >
        <i class="el-icon-refresh" />{{ $t('push.center.error.retry') }}
      </button>
    </div>
  </div>
</template>

<script>
/**
 * 消息中心列表三态复用组件
 * <p>
 * 互斥呈现四种非消息流形态：loading（首屏骨架屏，逐张错峰浮现 + 行内 shimmer）、
 * empty（无消息，熄灭的信标 + 双层光晕呼吸）、filtered-empty（有消息但当前筛选
 * 无结果，提供一键清除筛选）、error（首屏拉取失败，警示 + 重试）。
 * 各形态内部元素带 0.32s 上浮淡入，与编排层的形态切换过渡叠加形成层次；
 * mode 由编排层依据 store loading/loadError/messages 派生；事件仅上抛
 * retry / clear-filter。移动端关闭装饰动画与 shimmer 以降 GPU 负载。
 * </p>
 */
export default {
  name: 'PushListState',
  props: {
    /**
     * 状态模式：'loading' | 'empty' | 'filtered-empty' | 'error'
     */
    mode: {
      type: String,
      default: ''
    }
  }
}
</script>

<style lang="scss" scoped>
@import '../styles/motion';

.pn-state {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

/* ── 骨架屏 ─────────────────────────────── */
.pn-skeleton {
  padding: 12px var(--pn-gutter, 16px);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.pn-skel-card {
  border: 1px solid rgba(0, 0, 0, 0.06);
  border-radius: 14px;
  padding: 14px 16px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  background: rgba(255, 255, 255, 0.55);
  animation: pn-state-in $mo-dur-slow $mo-ease-out both;

  &:nth-child(2) {
    animation-delay: 0.06s;
  }

  &:nth-child(3) {
    animation-delay: 0.12s;
  }
}

.pn-skel-line {
  height: 10px;
  border-radius: 5px;
  background: linear-gradient(90deg, rgba(0, 0, 0, 0.05) 25%, rgba(0, 0, 0, 0.1) 37%, rgba(0, 0, 0, 0.05) 63%);
  background-size: 400% 100%;
  animation: pn-shimmer 1.4s ease infinite;

  &--chip {
    width: 72px;
  }

  &--title {
    width: 62%;
    height: 13px;
  }

  &--body {
    width: 92%;
  }

  &--meta {
    width: 40%;
  }

  &:nth-child(2) {
    animation-delay: 0.12s;
  }

  &:nth-child(3) {
    animation-delay: 0.24s;
  }
}

@keyframes pn-shimmer {
  0% {
    background-position: 100% 0;
  }
  100% {
    background-position: 0 0;
  }
}

/* ── 空态 / 筛选无结果 ─────────────────────── */
.pn-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 24px var(--pn-gutter, 16px);
  animation: pn-state-in $mo-dur-slow $mo-ease-out both;
}

/* 熄灭的信标：双层光晕错峰呼吸（系统蓝） */
.pn-empty-beacon {
  position: relative;
  width: 92px;
  height: 92px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  border: 1px dashed rgba(0, 0, 0, 0.12);

  i {
    font-size: 34px;
    color: #0a84ff;
    opacity: 0.75;
    z-index: 1;
  }

  &::before,
  &::after {
    content: '';
    position: absolute;
    inset: 0;
    border-radius: 50%;
    background: radial-gradient(circle, rgba(10, 132, 255, 0.16), transparent 68%);
    animation: pn-breathe 3.2s ease-in-out infinite;
  }

  &::after {
    inset: -18px;
    background: radial-gradient(circle, rgba(10, 132, 255, 0.07), transparent 70%);
    animation-delay: 1.4s;
  }

  &--filtered i {
    opacity: 1;
  }
}

@keyframes pn-breathe {
  0%, 100% {
    transform: scale(0.92);
    opacity: 0.55;
  }
  50% {
    transform: scale(1.06);
    opacity: 1;
  }
}

/* 形态整体上浮淡入 */
@keyframes pn-state-in {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.pn-empty-text {
  margin: 0;
  font-size: 13px;
  letter-spacing: 0.12em;
  color: #a1a1a6;
  text-align: center;
}

/* ── 错误态（系统橙警示） ─────────────────── */
.pn-error {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 24px var(--pn-gutter, 16px);
  text-align: center;
  animation: pn-state-in $mo-dur-slow $mo-ease-out both;
}

.pn-error-icon {
  width: 64px;
  height: 64px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: rgba(255, 149, 0, 0.1);
  border: 1px solid rgba(255, 149, 0, 0.28);

  i {
    font-size: 28px;
    color: #ff9500;
  }
}

.pn-error-title {
  margin: 8px 0 0;
  font-size: 14px;
  font-weight: 600;
  color: #1d1d1f;
}

.pn-error-hint {
  margin: 0;
  font-size: 12px;
  color: #86868b;
}

.pn-state-btn {
  appearance: none;
  margin-top: 12px;
  display: inline-flex;
  align-items: center;
  gap: 6px;
  border: 1px solid rgba(10, 132, 255, 0.24);
  background: rgba(10, 132, 255, 0.08);
  color: #0a84ff;
  font-family: inherit;
  font-size: 12px;
  letter-spacing: 0.04em;
  padding: 8px 22px;
  border-radius: 999px;
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
  transition:
    background $mo-dur-fast ease,
    border-color $mo-dur-fast ease,
    color $mo-dur-fast ease,
    box-shadow $mo-dur-base ease,
    transform $mo-dur-fast $mo-ease-out;

  @include focus-ring;
  @include press;

  @include hover-only {
    background: #0a84ff;
    border-color: #0a84ff;
    color: #fff;
    box-shadow: 0 4px 14px rgba(10, 132, 255, 0.32);
  }
}

/* 手机：触摸目标 >= 44px，关闭装饰动画（光晕呼吸/骨架 shimmer/上浮） */
@include respond-to(phone) {
  .pn-state-btn {
    @include touch-target;
    padding: 10px 24px;
  }

  .pn-empty-beacon::before,
  .pn-empty-beacon::after {
    animation: none;
  }

  .pn-skel-line,
  .pn-skel-card {
    animation: none;
  }

  .pn-skeleton {
    padding: 10px var(--pn-gutter, 14px);
  }
}

/* 手机横屏：压缩留白与信标尺寸 */
@include respond-to(phone-landscape) {
  .pn-empty,
  .pn-error {
    gap: 10px;
    padding-top: 14px;
    padding-bottom: 14px;
  }

  .pn-empty-beacon {
    width: 70px;
    height: 70px;

    i {
      font-size: 26px;
    }

    &::after {
      inset: -12px;
    }
  }

  .pn-error-icon {
    width: 52px;
    height: 52px;

    i {
      font-size: 22px;
    }
  }
}

/* 弱动效偏好 */
@media (prefers-reduced-motion: reduce) {
  .pn-empty,
  .pn-error,
  .pn-skel-card,
  .pn-skel-line,
  .pn-empty-beacon::before,
  .pn-empty-beacon::after {
    animation: none;
  }
}
</style>

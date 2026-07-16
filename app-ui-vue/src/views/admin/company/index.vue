<template>
  <div class="company-page">
    <div class="company-page-inner">
      <company-admin-view />
    </div>
  </div>
</template>

<script>
import CompanyAdminView from '@/components/CompanyAdminView/index.vue'

export default {
  name: 'Company',
  components: { CompanyAdminView }
}
</script>

<style lang="scss" scoped>
@import '~@/styles/variables.scss';

.company-page {
  min-height: calc(100vh - 66px);
  background: linear-gradient(135deg, #f5f5f7 0%, #e8e8ed 50%, #f0f0f3 100%);
  padding: 20px;
  position: relative;
  overflow: hidden;

  // 添加微妙的几何纹理背景
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background:
      radial-gradient(circle at 20% 80%, rgba(0, 113, 227, 0.03) 0%, transparent 50%),
      radial-gradient(circle at 80% 20%, rgba(0, 113, 227, 0.02) 0%, transparent 50%);
    pointer-events: none;
    z-index: 0;
  }
}

.company-page-inner {
  position: relative;
  z-index: 1;
  background: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow:
    0 4px 16px rgba(0, 0, 0, 0.06),
    0 1px 4px rgba(0, 0, 0, 0.04),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  min-height: calc(100vh - 106px);
  transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);

  &:hover {
    box-shadow:
      0 8px 32px rgba(0, 0, 0, 0.08),
      0 2px 8px rgba(0, 0, 0, 0.06),
      inset 0 1px 0 rgba(255, 255, 255, 0.9);
  }

  // 覆盖子组件的背景色，使其融入玻璃容器
  ::v-deep .company-admin {
    background: transparent;
    min-height: auto;
    padding: 24px;

    // 更新设计令牌以匹配整体风格
    --ca-primary: #0071e3;
    --ca-primary-light: #0077ED;
    --ca-primary-bg: rgba(0, 113, 227, 0.08);
    --ca-primary-border: rgba(0, 113, 227, 0.2);
    --ca-bg: transparent;
    --ca-surface: rgba(255, 255, 255, 0.8);
    --ca-border: rgba(0, 0, 0, 0.08);
    --ca-border-light: rgba(0, 0, 0, 0.04);

    // 更新圆角以匹配设计系统
    --ca-radius: 12px;
    --ca-radius-sm: 10px;

    // 搜索区域
    .ca-search {
      background: rgba(255, 255, 255, 0.6);
      backdrop-filter: blur(10px);
      border: 1px solid rgba(255, 255, 255, 0.4);
      box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);

      .ca-input {
        background: rgba(255, 255, 255, 0.8);

        &:focus {
          box-shadow: 0 0 0 3px rgba(0, 113, 227, 0.15);
        }
      }
    }

    // 面板
    .ca-panel {
      background: rgba(255, 255, 255, 0.7);
      backdrop-filter: blur(10px);
      border: 1px solid rgba(255, 255, 255, 0.5);
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
    }

    // 按钮样式更新
    .ca-btn-primary {
      background: linear-gradient(180deg, #0077ED 0%, #006edb 100%);
      box-shadow:
        0 1px 2px rgba(0, 0, 0, 0.15),
        inset 0 1px 0 rgba(255, 255, 255, 0.15);

      &:hover:not(:disabled) {
        background: linear-gradient(180deg, #0077ED 0%, #0071e3 100%);
        box-shadow:
          0 4px 12px rgba(0, 113, 227, 0.35),
          inset 0 1px 0 rgba(255, 255, 255, 0.2);
      }
    }

    .ca-btn-ghost {
      background: rgba(255, 255, 255, 0.6);
      border: 1px solid rgba(0, 0, 0, 0.1);
      backdrop-filter: blur(5px);

      &:hover {
        background: rgba(255, 255, 255, 0.9);
        border-color: rgba(0, 0, 0, 0.15);
      }
    }

    // 树节点样式优化
    ::v-deep .el-tree-node__content {
      &:hover {
        background: rgba(0, 113, 227, 0.04);
      }
    }

    ::v-deep .el-tree-node.is-current > .el-tree-node__content {
      background: rgba(0, 113, 227, 0.08) !important;
      border-right: 3px solid #0071e3;
    }

    // 对话框样式优化
    ::v-deep .ca-dialog .el-dialog {
      background: rgba(255, 255, 255, 0.95);
      backdrop-filter: blur(20px);
      box-shadow:
        0 20px 60px rgba(0, 0, 0, 0.12),
        0 4px 16px rgba(0, 0, 0, 0.08);
    }

    // 状态标签颜色更新
    .ca-status-1 {
      color: #34c759;
      background: rgba(52, 199, 89, 0.1);
      border: 1px solid rgba(52, 199, 89, 0.2);
    }

    .ca-status-2 {
      color: #ff9500;
      background: rgba(255, 149, 0, 0.1);
      border: 1px solid rgba(255, 149, 0, 0.2);
    }

    .ca-status-5 {
      color: #5856d6;
      background: rgba(88, 86, 214, 0.1);
      border: 1px solid rgba(88, 86, 214, 0.2);
    }

    // 占位符样式
    .ca-placeholder {
      background: radial-gradient(circle at center, rgba(0, 113, 227, 0.02) 0%, transparent 70%);
    }

    .ca-placeholder-icon {
      opacity: 0.3;
      color: $menuText;
    }

    // 加载遮罩
    ::v-deep .el-loading-mask {
      background-color: rgba(255, 255, 255, 0.85);
      backdrop-filter: blur(8px);
    }

    // 分页器
    .ca-pagination {
      background: rgba(255, 255, 255, 0.4);
      border-top: 1px solid rgba(0, 0, 0, 0.05);
    }
  }
}

// 页面进入动画
@keyframes pageSlideIn {
  from {
    opacity: 0;
    transform: translateY(8px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.company-page-inner {
  animation: pageSlideIn 0.4s cubic-bezier(0.25, 0.8, 0.25, 1) forwards;
}
</style>

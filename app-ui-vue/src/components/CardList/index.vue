<template>
  <div class="card-list-container">
    <!-- 头部区域 -->
    <div v-if="$slots.header" class="card-list-header">
      <slot name="header"></slot>
    </div>

    <!-- 工具栏区域 -->
    <div v-if="$slots.toolbar" class="card-list-toolbar">
      <slot name="toolbar"></slot>
    </div>

    <!-- 选择操作栏 -->
    <div v-if="selectable && selectedItems.length > 0" class="card-list-selection-bar">
      <div class="selection-info">
        已选择 {{ selectedItems.length }} 项
      </div>
      <div class="selection-actions">
        <slot :selectedItems="selectedItems" name="selection-actions">
          <el-button type="text" @click="clearSelection">清空选择</el-button>
        </slot>
      </div>
    </div>

    <!-- 卡片列表容器 -->
    <div
      :style="{
        gridTemplateColumns: `repeat(${columns}, minmax(0, 1fr))`,
        gap: `${gap}px`,
        padding: `${padding}px`
      }"
      class="card-list"
    >
      <div
        v-for="(item, index) in data"
        :key="item[idField] || index"
        :class="[cardClass, {
          'card-item-selected': isItemSelected(item),
          'card-item-selectable': selectable
        }]"
        :style="cardStyle"
        class="card-item"
        @click="handleCardClick(item, $event)"
      >
        <!-- 选择控件 -->
        <div v-if="selectable" class="card-selection-control">
          <el-checkbox
            v-if="selectMode === 'multiple'"
            :value="isItemSelected(item)"
            @change="val => handleSelectChange(item, val)"
            @click.native.stop
          />
          <el-radio
            v-else-if="selectMode === 'single'"
            :label="true"
            :value="isItemSelected(item)"
            @change="val => handleSelectChange(item, val)"
            @click.native.stop
          />
        </div>

        <!-- 使用作用域插槽，允许父组件自定义卡片内容 -->
        <slot
          :index="index"
          :item="item"
          :loading="loading"
          :selected="isItemSelected(item)"
          name="card"
        >
          <!-- 默认卡片内容 -->
          <el-card class="default-card">
            <div class="default-card-content">
              <div class="default-card-title">{{ item.title || 'Card Title' }}</div>
              <div class="default-card-description">{{ item.description || 'Card description' }}</div>
            </div>
          </el-card>
        </slot>
      </div>

      <!-- 加载更多占位符 -->
      <div
        v-for="i in loadingCount"
        :key="`loading-${i}`"
        :style="cardStyle"
        class="card-item loading-card"
      >
        <slot name="loading">
          <el-card class="loading-placeholder">
            <div class="loading-content">
              <div class="loading-line"></div>
              <div class="loading-line short"></div>
              <div class="loading-line"></div>
            </div>
          </el-card>
        </slot>
      </div>
    </div>

    <!-- 空状态 -->
    <div v-if="!loading && data.length === 0" class="empty-state">
      <slot name="empty">
        <el-empty description="暂无数据"></el-empty>
      </slot>
    </div>

    <!-- 底部区域 -->
    <div v-if="$slots.footer" class="card-list-footer">
      <slot name="footer"></slot>
    </div>

    <!-- 分页 -->
    <div v-if="pagination && total > 0" class="pagination-container">
      <slot :current-page="currentPage" :page-size="pageSize" :total="total" name="pagination">
        <el-pagination
          :current-page="currentPage"
          :page-size="pageSize"
          :page-sizes="pageSizes"
          :total="total"
          background
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        >
        </el-pagination>
      </slot>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CardList',
  props: {
    // 数据源
    data: {
      type: Array,
      default: () => []
    },
    // 是否加载中
    loading: {
      type: Boolean,
      default: false
    },
    // 加载时显示的卡片数量
    loadingCount: {
      type: Number,
      default: 4
    },
    // 列数
    columns: {
      type: Number,
      default: 4
    },
    // 行数
    rows: {
      type: Number,
      default: 3
    },
    // 卡片间距
    gap: {
      type: Number,
      default: 20
    },
    // 容器内边距
    padding: {
      type: Number,
      default: 20
    },
    // 卡片样式
    cardStyle: {
      type: Object,
      default: () => ({})
    },
    // 卡片类名
    cardClass: {
      type: String,
      default: ''
    },
    // 数据项的唯一标识字段
    idField: {
      type: String,
      default: 'id'
    },
    // 是否显示分页
    pagination: {
      type: Boolean,
      default: false
    },
    // 总数据量（用于分页）
    total: {
      type: Number,
      default: 0
    },
    // 当前页码
    currentPage: {
      type: Number,
      default: 1
    },
    // 每页显示数量
    pageSize: {
      type: Number,
      default: 20
    },
    // 每页显示数量选项
    pageSizes: {
      type: Array,
      default: () => [10, 20, 50, 100]
    },
    // 是否可选择
    selectable: {
      type: Boolean,
      default: false
    },
    // 选择模式: single(单选) | multiple(多选)
    selectMode: {
      type: String,
      default: 'multiple',
      validator: value => ['single', 'multiple'].includes(value)
    },
    // 初始选中项
    value: {
      type: [Array, Object],
      default: () => null
    }
  },
  data() {
    return {
      // 内部选中项状态
      selectedItems: this.value || (this.selectMode === 'multiple' ? [] : null)
    }
  },
  watch: {
    value(newVal) {
      this.selectedItems = newVal;
    }
  },
  methods: {
    handleSizeChange(val) {
      this.$emit('size-change', val);
    },
    handleCurrentChange(val) {
      this.$emit('current-change', val);
    },
    // 处理卡片点击事件
    handleCardClick(item, event) {
      // 如果不可选择，直接触发card-click事件
      if (!this.selectable) {
        this.$emit('card-click', item, event);
        return;
      }

      // 可选择时，根据模式处理选择
      if (this.selectMode === 'multiple') {
        this.toggleSelection(item);
      } else if (this.selectMode === 'single') {
        this.selectItem(item);
      }

      // 触发card-click事件
      this.$emit('card-click', item, event);
    },
    // 判断项目是否被选中
    isItemSelected(item) {
      if (this.selectMode === 'multiple') {
        return this.selectedItems.some(selected => selected[this.idField] === item[this.idField]);
      } else {
        return this.selectedItems && this.selectedItems[this.idField] === item[this.idField];
      }
    },
    // 切换选择状态（多选模式）
    toggleSelection(item) {
      if (!this.selectable || this.selectMode !== 'multiple') return;

      const itemIndex = this.selectedItems.findIndex(selected => selected[this.idField] === item[this.idField]);

      if (itemIndex > -1) {
        // 已选中，移除
        const newSelected = [...this.selectedItems];
        newSelected.splice(itemIndex, 1);
        this.selectedItems = newSelected;
      } else {
        // 未选中，添加
        this.selectedItems = [...this.selectedItems, item];
      }

      this.$emit('input', this.selectedItems);
      this.$emit('selection-change', this.selectedItems);
    },
    // 选择项目（单选模式）
    selectItem(item) {
      if (!this.selectable || this.selectMode !== 'single') return;

      if (this.selectedItems && this.selectedItems[this.idField] === item[this.idField]) {
        // 已选中，取消选择
        this.selectedItems = null;
      } else {
        // 未选中，选择该项目
        this.selectedItems = item;
      }

      this.$emit('input', this.selectedItems);
      this.$emit('selection-change', this.selectedItems);
    },
    // 处理选择控件变化
    handleSelectChange(item, checked) {
      if (this.selectMode === 'multiple') {
        if (checked) {
          this.selectedItems = [...this.selectedItems, item];
        } else {
          const itemIndex = this.selectedItems.findIndex(selected => selected[this.idField] === item[this.idField]);
          if (itemIndex > -1) {
            const newSelected = [...this.selectedItems];
            newSelected.splice(itemIndex, 1);
            this.selectedItems = newSelected;
          }
        }
      } else {
        this.selectedItems = checked ? item : null;
      }

      this.$emit('input', this.selectedItems);
      this.$emit('selection-change', this.selectedItems);
    },
    // 清空选择
    clearSelection() {
      this.selectedItems = this.selectMode === 'multiple' ? [] : null;
      this.$emit('input', this.selectedItems);
      this.$emit('selection-change', this.selectedItems);
    }
  }
}
</script>

<style lang="scss" scoped>
.card-list-container {
  width: 100%;

  .card-list-header {
    padding: 15px 20px;
    border-bottom: 1px solid #ebeef5;

    ::v-deep > *:first-child {
      margin-top: 0;
    }

    ::v-deep > *:last-child {
      margin-bottom: 0;
    }
  }

  .card-list-toolbar {
    padding: 15px 20px;
    background-color: #f5f7fa;
    border-bottom: 1px solid #ebeef5;

    ::v-deep > *:first-child {
      margin-top: 0;
    }

    ::v-deep > *:last-child {
      margin-bottom: 0;
    }
  }

  .card-list-selection-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 20px;
    background-color: #ecf5ff;
    border-bottom: 1px solid #ebeef5;

    .selection-info {
      color: #409eff;
      font-weight: 500;
    }

    .selection-actions {
      ::v-deep .el-button {
        color: #409eff;
      }
    }
  }

  .card-list {
    display: grid;
    grid-template-columns: repeat(4, minmax(0, 1fr));
    gap: 20px;
    padding: 20px;

    .card-item {
      transition: all 0.3s ease;
      position: relative;
      cursor: pointer;

      &:hover {
        transform: translateY(-5px);
        box-shadow: 0 10px 20px rgba(0, 0, 0, 0.1);
      }

      &.card-item-selectable {
        cursor: pointer;
      }

      &.card-item-selected {
        border: 2px solid #409eff;
        transform: translateY(-5px);
      }

      .card-selection-control {
        position: absolute;
        top: 10px;
        right: 10px;
        z-index: 10;
        background: rgba(255, 255, 255, 0.9);
        border-radius: 50%;
        padding: 2px;
      }
    }

    .loading-card {
      animation: pulse 1.5s ease-in-out infinite alternate;
    }
  }

  .empty-state {
    padding: 40px 0;
    text-align: center;
  }

  .card-list-footer {
    padding: 15px 20px;
    border-top: 1px solid #ebeef5;

    ::v-deep > *:first-child {
      margin-top: 0;
    }

    ::v-deep > *:last-child {
      margin-bottom: 0;
    }
  }

  .pagination-container {
    display: flex;
    justify-content: center;
    padding: 20px;
    border-top: 1px solid #ebeef5;
  }
}

.default-card {
  height: 100%;

  .default-card-content {
    .default-card-title {
      font-size: 16px;
      font-weight: 600;
      margin-bottom: 10px;
      color: #333;
    }

    .default-card-description {
      font-size: 14px;
      color: #666;
      line-height: 1.5;
    }
  }
}

.loading-placeholder {
  .loading-content {
    .loading-line {
      height: 16px;
      background: linear-gradient(90deg, #f0f0f0 25%, #e0e0e0 50%, #f0f0f0 75%);
      background-size: 200% 100%;
      animation: loading 1.5s infinite;
      border-radius: 4px;
      margin-bottom: 10px;

      &.short {
        width: 70%;
      }
    }
  }
}

@keyframes pulse {
  0% {
    opacity: 0.6;
  }
  100% {
    opacity: 1;
  }
}

@keyframes loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

// 响应式设计
@media (max-width: 1200px) {
  .card-list {
    grid-template-columns: repeat(3, minmax(0, 1fr)) !important;
  }
}

@media (max-width: 992px) {
  .card-list {
    grid-template-columns: repeat(2, minmax(0, 1fr)) !important;
  }
}

@media (max-width: 768px) {
  .card-list {
    grid-template-columns: repeat(1, minmax(0, 1fr)) !important;
    gap: 15px !important;
    padding: 15px !important;
  }

  .card-list-header,
  .card-list-toolbar,
  .card-list-footer {
    padding: 10px 15px;
  }
}
</style>

<template>
  <div class="rdt">
    <!-- 工具条：全局搜索 + 条件重置 + 分页 -->
    <div class="rdt-toolbar">
      <el-input
        v-model="keyword"
        size="small"
        clearable
        :placeholder="$t('dataHandle.searchPlaceholder')"
        prefix-icon="el-icon-search"
        class="rdt-search"
        @input="onSearchInput"
        @clear="applyKeyword"
      />
      <el-button
        size="small"
        icon="el-icon-refresh-left"
        :disabled="!hasActiveCondition"
        @click="resetConditions"
      >{{ $t('dataHandle.resetFilter') }}</el-button>
      <div class="rdt-spacer" />
      <el-pagination
        v-if="filteredRows.length > 0"
        :current-page.sync="page"
        :page-size.sync="pageSize"
        :page-sizes="pageSizes"
        :total="filteredRows.length"
        layout="total, sizes, prev, pager, next, jumper"
        background
        small
        class="rdt-pagination"
      />
    </div>

    <!-- 数据表格 -->
    <el-table
      v-if="filteredRows.length > 0"
      :data="pagedRows"
      :row-key="rowKeyFn"
      border
      stripe
      max-height="480"
      style="width: 100%"
      class="rdt-table"
      @filter-change="onFilterChange"
      @sort-change="onSortChange"
    >
      <el-table-column
        type="index"
        :index="indexAt"
        width="60"
        fixed="left"
      />
      <el-table-column
        v-for="col in columns"
        :key="col.index"
        :column-key="`c${col.index}`"
        :prop="`c${col.index}`"
        :label="col.name"
        min-width="140"
        show-overflow-tooltip
        sortable="custom"
        :sort-orders="['ascending', 'descending', null]"
        :filters="filterOptionsByCol[col.index] || null"
        filter-placement="bottom-end"
        :filter-method="noopFilterMethod"
      >
        <template slot-scope="scope">
          <span v-if="scope.row[`c${col.index}`] === ''" class="rdt-blank">-</span>
          <span v-else>{{ scope.row[`c${col.index}`] }}</span>
        </template>
      </el-table-column>
    </el-table>

    <!-- 筛选无匹配空态 -->
    <div v-else-if="rawRows.length > 0" class="rdt-empty">
      <i class="el-icon-search" />
      <p>{{ $t('dataHandle.noMatch') }}</p>
      <el-button size="small" type="primary" plain round @click="resetConditions">
        {{ $t('dataHandle.resetFilter') }}
      </el-button>
    </div>

    <!-- 数据本身为空 -->
    <div v-else class="rdt-empty">
      <i class="el-icon-document" />
      <p>{{ $t('dataHandle.emptyResult') }}</p>
    </div>

    <!-- 统计条 -->
    <div v-if="rawRows.length > 0" class="rdt-stats">
      <span class="stat-chip" :class="{ active: hasActiveCondition }">
        <i class="el-icon-data-line" />
        {{ $t('dataHandle.filteredCount', { count: filteredRows.length, total: rawRows.length }) }}
      </span>
      <span class="stat-chip">
        <i class="el-icon-set-up" />
        {{ $t('dataHandle.columnCount', { count: columns.length }) }}
      </span>
    </div>
  </div>
</template>

<script>
import {
  FILTER_OPTIONS_LIMIT,
  PAGE_SIZES,
  DEFAULT_PAGE_SIZE,
  SEARCH_DEBOUNCE_MS
} from './shared'

/**
 * 解析结果数据表格
 *
 * 接收 TableData JSON 结构（columns + rows），内部自治完成
 * 全局搜索、列筛选、客户端排序与前端分页，纯前端计算无网络开销。
 */
export default {
  name: 'ResultDataTable',
  props: {
    /**
     * TableData JSON 结构：{ tableName, columns[{name,index}], rows[{cells[],index}] }
     */
    tableData: {
      type: Object,
      default: null
    }
  },
  data() {
    return {
      keyword: '',
      keywordApplied: '',
      searchTimer: null,
      page: 1,
      pageSize: DEFAULT_PAGE_SIZE,
      pageSizes: PAGE_SIZES,
      sortProp: '',
      sortOrder: '',
      columnFilters: {}
    }
  },
  computed: {
    columns() {
      if (!this.tableData || !this.tableData.columns) {
        return []
      }
      return [...this.tableData.columns].sort((a, b) => (a.index || 0) - (b.index || 0))
    },
    rawRows() {
      if (!this.tableData || !this.tableData.rows) {
        return []
      }
      return this.tableData.rows.map(row => {
        const item = { _idx: row.index }
        ;(row.cells || []).forEach((cell, i) => {
          item[`c${i}`] = cell === null || cell === undefined ? '' : cell
        })
        return item
      })
    },
    filterOptionsByCol() {
      const result = {}
      if (this.rawRows.length === 0 || this.columns.length === 0) {
        return result
      }
      // 单次遍历统计每列 distinct 值
      const sets = this.columns.map(() => new Set())
      this.rawRows.forEach(row => {
        this.columns.forEach((col, i) => {
          const value = row[`c${col.index}`]
          if (value !== '') {
            sets[i].add(String(value))
          }
        })
      })
      sets.forEach((set, i) => {
        if (set.size <= FILTER_OPTIONS_LIMIT) {
          result[this.columns[i].index] = Array.from(set).sort().map(v => ({ text: v, value: v }))
        }
      })
      return result
    },
    filteredRows() {
      const keyword = this.keywordApplied.toLowerCase()
      return this.rawRows.filter(row => {
        if (keyword && !Object.keys(row).some(key => {
          return key !== '_idx' && String(row[key]).toLowerCase().includes(keyword)
        })) {
          return false
        }
        return Object.keys(this.columnFilters).every(prop => {
          const selected = this.columnFilters[prop]
          if (!selected || selected.length === 0) {
            return true
          }
          return selected.includes(String(row[prop]))
        })
      })
    },
    sortedRows() {
      if (!this.sortProp || !this.sortOrder) {
        return this.filteredRows
      }
      const prop = this.sortProp
      const dir = this.sortOrder === 'ascending' ? 1 : -1
      return [...this.filteredRows].sort((a, b) => dir * this.compareCells(a[prop], b[prop]))
    },
    pagedRows() {
      const start = (this.page - 1) * this.pageSize
      return this.sortedRows.slice(start, start + this.pageSize)
    },
    hasActiveCondition() {
      if (this.keywordApplied) {
        return true
      }
      if (this.sortProp && this.sortOrder) {
        return true
      }
      return Object.values(this.columnFilters).some(selected => selected && selected.length > 0)
    }
  },
  watch: {
    tableData() {
      this.resetConditions()
    },
    filteredRows() {
      this.page = 1
    }
  },
  beforeDestroy() {
    if (this.searchTimer) {
      clearTimeout(this.searchTimer)
    }
  },
  methods: {
    rowKeyFn(row) {
      return row._idx
    },
    indexAt(index) {
      return (this.page - 1) * this.pageSize + index + 1
    },
    noopFilterMethod() {
      return true
    },
    compareCells(a, b) {
      const na = this.toComparableNumber(a)
      const nb = this.toComparableNumber(b)
      if (na === null && nb === null) {
        return String(a).localeCompare(String(b))
      }
      // 数字与文本混合时空值排末尾
      if (na === null) {
        return 1
      }
      if (nb === null) {
        return -1
      }
      return na - nb
    },
    toComparableNumber(value) {
      if (value === '' || value === null || value === undefined) {
        return null
      }
      const num = Number(value)
      return Number.isNaN(num) ? null : num
    },
    onSearchInput() {
      if (this.searchTimer) {
        clearTimeout(this.searchTimer)
      }
      this.searchTimer = setTimeout(() => {
        this.applyKeyword()
      }, SEARCH_DEBOUNCE_MS)
    },
    applyKeyword() {
      this.keywordApplied = (this.keyword || '').trim()
    },
    onFilterChange(filters) {
      this.columnFilters = { ...this.columnFilters, ...filters }
    },
    onSortChange({ prop, order }) {
      this.sortProp = prop || ''
      this.sortOrder = order || ''
      this.page = 1
    },
    resetConditions() {
      if (this.searchTimer) {
        clearTimeout(this.searchTimer)
        this.searchTimer = null
      }
      this.keyword = ''
      this.keywordApplied = ''
      this.sortProp = ''
      this.sortOrder = ''
      this.columnFilters = {}
      this.page = 1
    }
  }
}
</script>

<style lang="scss" scoped>
$dhu-ink: #2c3e50;
$dhu-muted: #94a3b3;
$dhu-primary: #4a7de0;

.rdt {
  width: 100%;
}

.rdt-toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;
}

.rdt-search {
  width: 260px;

  ::v-deep .el-input__inner {
    border-radius: 8px;
  }
}

.rdt-spacer {
  flex: 1;
}

.rdt-pagination {
  ::v-deep .el-pagination__jump {
    margin-left: 8px;
  }
}

.rdt-table {
  border-radius: 8px;
  overflow: hidden;

  ::v-deep .el-table__header th {
    background: #f6f9fd;
    color: $dhu-ink;
    font-weight: 600;
  }
}

.rdt-blank {
  color: #c3d0e0;
}

.rdt-empty {
  padding: 44px 0;
  text-align: center;
  color: $dhu-muted;

  i {
    font-size: 48px;
    color: #c3d0e0;
  }

  p {
    margin: 12px 0 14px;
    font-size: 13px;
  }
}

.rdt-stats {
  display: flex;
  gap: 10px;
  margin-top: 12px;
}

.stat-chip {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 3px 12px;
  border: 1px solid rgba(74, 125, 224, 0.18);
  border-radius: 999px;
  background: rgba(74, 125, 224, 0.06);
  font-size: 12px;
  color: $dhu-primary;
  transition: background 0.25s ease, border-color 0.25s ease;

  &.active {
    border-color: rgba(74, 125, 224, 0.45);
    background: rgba(74, 125, 224, 0.14);
  }
}

/* 手机端：搜索框与统计标签占满换行，避免固定 260px 宽度溢出窄屏 */
@media (max-width: 768px) {
  .rdt-toolbar {
    flex-wrap: wrap;
  }

  .rdt-search {
    width: 100%;
  }

  .rdt-stats {
    flex-wrap: wrap;
  }
}
</style>

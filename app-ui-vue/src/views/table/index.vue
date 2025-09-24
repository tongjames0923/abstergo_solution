<template>
  <div class="app-container card-list-demo">
    <CardList
      v-model="selectedItems"
      :columns="configForm.columns"
      :current-page="currentPage"
      :data="listData"
      :gap="configForm.gap"
      :loading="loading"
      :padding="configForm.padding"
      :page-size="configForm.pageSize"
      :pagination="configForm.pagination"
      :select-mode="configForm.selectMode"
      :selectable="configForm.selectable"
      :total="total"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      @selection-change="handleSelectionChange"
    >
      <!-- 头部插槽 -->
      <template #header>
        <div class="card-list-header-content">
          <h2>项目卡片列表</h2>
          <p>这是一个展示项目信息的卡片列表示例</p>
        </div>
      </template>

      <!-- 工具栏插槽 -->
      <template #toolbar>
        <div class="card-list-toolbar-content">
          <el-form :inline="true" :model="configForm" class="toolbar-form" label-width="0">
            <el-form-item label="选择模式">
              <el-switch
                v-model="configForm.selectable"
                active-text="可选择"
                inactive-text="不可选择"
              />
            </el-form-item>
            <el-form-item v-if="configForm.selectable" label="选择类型">
              <el-radio-group v-model="configForm.selectMode" size="mini">
                <el-radio-button label="single">单选</el-radio-button>
                <el-radio-button label="multiple">多选</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-form-item>
              <el-input
                v-model="searchForm.keyword"
                clearable
                placeholder="搜索项目"
                prefix-icon="el-icon-search"
                @clear="refreshData"
                @keyup.enter.native="refreshData"
              />
            </el-form-item>
            <el-form-item>
              <el-select v-model="searchForm.category" clearable placeholder="分类筛选">
                <el-option label="全部分类" value="" />
                <el-option label="技术类" value="tech" />
                <el-option label="设计类" value="design" />
                <el-option label="市场类" value="market" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button icon="el-icon-search" type="primary" @click="refreshData">搜索</el-button>
            </el-form-item>
            <el-form-item class="toolbar-spacer" />
            <el-form-item>
              <el-button icon="el-icon-plus" type="primary" @click="handleCreate">新增项目</el-button>
            </el-form-item>
          </el-form>
        </div>
      </template>

      <!-- 选择操作栏插槽 -->
      <template #selection-actions="{ selectedItems }">
        <el-button type="text" @click="handleBatchDelete(selectedItems)">批量删除</el-button>
        <el-button type="text" @click="handleBatchExport(selectedItems)">批量导出</el-button>
        <el-button type="text" @click="clearSelection">清空选择</el-button>
      </template>

      <!-- 自定义卡片内容 -->
      <template #card="{ item, selected }">
        <el-card class="custom-card" shadow="hover">
          <div class="card-header">
            <div class="card-avatar">
              <el-avatar :src="item.avatar" />
            </div>
            <div class="card-title">{{ item.title }}</div>
            <el-tag :type="getTagType(item.category)" size="mini">{{ item.categoryName }}</el-tag>
          </div>
          <div class="card-body">
            <div class="card-description">{{ item.description }}</div>
            <div class="card-meta">
              <span class="meta-item">
                <i class="el-icon-view" /> {{ item.views }}
              </span>
              <span class="meta-item">
                <i class="el-icon-star-off" /> {{ item.stars }}
              </span>
              <span class="meta-item">
                <i class="el-icon-time" /> {{ item.createTime }}
              </span>
            </div>
          </div>
          <div class="card-footer">
            <el-button type="text" @click.stop="handleView(item)">查看</el-button>
            <el-button type="text" @click.stop="handleEdit(item)">编辑</el-button>
            <el-button type="text" @click.stop="handleDelete(item)">删除</el-button>
          </div>
        </el-card>
      </template>

      <!-- 底部插槽 -->
      <template #footer>
        <div class="card-list-footer-content">
          <div class="footer-info">
            共 {{ total }} 个项目
            <span v-if="selectedItems.length > 0">，已选择 {{ selectedItems.length }} 项</span>
          </div>
          <div class="footer-actions">
            <el-button icon="el-icon-download" plain type="primary" @click="handleExport">导出数据</el-button>
            <el-button icon="el-icon-refresh" plain @click="refreshData">刷新</el-button>
          </div>
        </div>
      </template>

      <!-- 自定义空状态 -->
      <template #empty>
        <el-empty description="暂无相关项目">
          <el-button icon="el-icon-plus" type="primary" @click="handleCreate">创建第一个项目</el-button>
        </el-empty>
      </template>
    </CardList>

    <!-- 查看详情对话框 -->
    <el-dialog :visible.sync="detailDialogVisible" title="项目详情" width="40%" :close-on-click-modal="false">
      <el-form :model="currentItem" label-width="80px">
        <el-form-item label="项目名称">
          <el-input v-model="currentItem.title" readonly />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="currentItem.categoryName" readonly />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="currentItem.description" readonly type="textarea" />
        </el-form-item>
        <el-form-item label="浏览量">
          <el-input v-model="currentItem.views" readonly />
        </el-form-item>
        <el-form-item label="收藏数">
          <el-input v-model="currentItem.stars" readonly />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="detailDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="detailDialogVisible = false">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import CardList from '@/components/CardList'

export default {
  name: 'CardListDemo',
  components: {
    CardList
  },
  data() {
    return {
      loading: false,
      listData: [],
      total: 0,
      currentPage: 1,
      detailDialogVisible: false,
      currentItem: {},
      selectedItems: [],
      searchForm: {
        keyword: '',
        category: ''
      },
      configForm: {
        columns: 4,
        gap: 20,
        padding: 20,
        pagination: true,
        pageSize: 8,
        selectable: true,
        selectMode: 'multiple'
      }
    }
  },
  mounted() {
    this.fetchData()
  },
  methods: {
    fetchData() {
      this.loading = true

      // 模拟API请求
      setTimeout(() => {
        // 生成模拟数据
        const categoryMap = {
          'tech': '技术类',
          'design': '设计类',
          'market': '市场类'
        }

        const data = []
        const count = this.configForm.pageSize
        for (let i = 0; i < count; i++) {
          const index = (this.currentPage - 1) * this.configForm.pageSize + i + 1
          const category = ['tech', 'design', 'market'][Math.floor(Math.random() * 3)]
          data.push({
            id: index,
            title: `项目标题 ${index}`,
            description: `这是项目 ${index} 的详细描述信息，可以展示一些关于该项目的详细内容。这个项目属于${categoryMap[category]}。`,
            avatar: `https://i.pravatar.cc/150?img=${index % 70}`,
            views: Math.floor(Math.random() * 1000),
            stars: Math.floor(Math.random() * 100),
            category: category,
            categoryName: categoryMap[category],
            createTime: `${Math.floor(Math.random() * 12) + 1}-${Math.floor(Math.random() * 28) + 1}`
          })
        }

        this.listData = data
        this.total = 85 // 模拟总数据量
        this.loading = false
      }, 1000)
    },

    refreshData() {
      this.currentPage = 1
      this.fetchData()
    },

    handleSizeChange(val) {
      this.configForm.pageSize = val
      this.currentPage = 1
      this.fetchData()
    },

    handleCurrentChange(val) {
      this.currentPage = val
      this.fetchData()
    },

    handleView(item) {
      this.currentItem = { ...item }
      this.detailDialogVisible = true
    },

    handleEdit(item) {
      this.$message.info(`编辑项目: ${item.title}`)
    },

    handleDelete(item) {
      this.$confirm(`确定要删除项目 "${item.title}" 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$message.success('删除成功')
        // 实际项目中这里应该调用API删除数据并重新加载列表
      }).catch(() => {
        // 用户取消删除
      })
    },

    handleCreate() {
      this.$message.info('创建新项目')
    },

    handleExport() {
      this.$message.success('正在导出数据...')
    },

    getTagType(category) {
      const typeMap = {
        'tech': 'success',
        'design': 'warning',
        'market': 'primary'
      }
      return typeMap[category] || 'info'
    },

    // 选择相关方法
    handleSelectionChange(selectedItems) {
      console.log('选中项发生变化:', selectedItems)
    },

    clearSelection() {
      this.selectedItems = this.configForm.selectMode === 'multiple' ? [] : null
    },

    handleBatchDelete(selectedItems) {
      if (selectedItems.length === 0) {
        this.$message.warning('请先选择要删除的项目')
        return
      }

      this.$confirm(`确定要删除选中的 ${selectedItems.length} 个项目吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        this.$message.success('批量删除成功')
        this.clearSelection()
      }).catch(() => {
        // 用户取消删除
      })
    },

    handleBatchExport(selectedItems) {
      if (selectedItems.length === 0) {
        this.$message.warning('请先选择要导出的项目')
        return
      }

      this.$message.success(`正在导出 ${selectedItems.length} 个项目的数据...`)
    }
  }
}
</script>

<style lang="scss" scoped>
.card-list-demo {
  .card-list-header-content {
    h2 {
      margin: 0 0 10px 0;
      color: #333;
    }

    p {
      margin: 0;
      color: #666;
    }
  }

  .card-list-toolbar-content {
    .toolbar-form {
      width: 100%;
      display: flex;
      flex-wrap: wrap;

      .toolbar-spacer {
        flex: 1;
      }

      ::v-deep .el-form-item {
        margin-bottom: 0;
        margin-right: 15px;
      }
    }
  }

  .custom-card {
    height: 100%;
    display: flex;
    flex-direction: column;

    .card-header {
      display: flex;
      align-items: center;
      margin-bottom: 15px;

      .card-avatar {
        margin-right: 10px;
      }

      .card-title {
        font-size: 16px;
        font-weight: 600;
        color: #333;
        flex: 1;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .el-tag {
        margin-left: 10px;
      }
    }

    .card-body {
      flex: 1;
      margin-bottom: 15px;

      .card-description {
        font-size: 14px;
        color: #666;
        line-height: 1.5;
        margin-bottom: 15px;
        display: -webkit-box;
        -webkit-line-clamp: 3;
        -webkit-box-orient: vertical;
        overflow: hidden;
      }

      .card-meta {
        display: flex;
        flex-wrap: wrap;

        .meta-item {
          margin-right: 15px;
          font-size: 13px;
          color: #999;

          i {
            margin-right: 3px;
          }
        }
      }
    }

    .card-footer {
      display: flex;
      justify-content: flex-end;
      border-top: 1px solid #eee;
      padding-top: 15px;

      ::v-deep .el-button {
        margin-left: 10px;
        padding: 0;
      }
    }
  }

  .card-list-footer-content {
    display: flex;
    justify-content: space-between;
    align-items: center;

    .footer-info {
      color: #666;
      font-size: 14px;
    }

    .footer-actions {
      ::v-deep .el-button {
        margin-left: 10px;
      }
    }
  }
}

// 响应式调整
@media (max-width: 768px) {
  .card-list-demo {
    .card-list-toolbar-content {
      .toolbar-form {
        .toolbar-spacer {
          display: none;
        }

        ::v-deep .el-form-item {
          margin-bottom: 10px;
        }
      }
    }

    .card-list-footer-content {
      flex-direction: column;

      .footer-info,
      .footer-actions {
        margin-bottom: 10px;
      }
    }
  }
}
</style>

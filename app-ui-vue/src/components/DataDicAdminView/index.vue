<template>
  <div class="dic-admin">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-left">
        <h2 class="page-title">
          <i class="el-icon-notebook-2 title-icon" />
          数据字典管理
        </h2>
        <span class="page-subtitle">管理系统数据字典及其字典项配置</span>
      </div>
    </div>

    <!-- 主体区域：左侧字典列表 + 右侧字典项 -->
    <div class="main-content">
      <!-- 左侧面板：字典列表 -->
      <div class="left-panel">
        <div class="panel-header">
          <span class="panel-title">字典列表</span>
          <el-button
            type="primary"
            icon="el-icon-plus"
            size="small"
            class="add-dic-btn"
            @click="handleAddDic"
          >
            新增
          </el-button>
        </div>

        <!-- 搜索框 -->
        <div class="search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索字典名称或编码"
            prefix-icon="el-icon-search"
            size="small"
            clearable
            @input="handleSearchInput"
          />
        </div>

        <!-- 字典列表 -->
        <div class="dic-list" v-loading="loadingDicList">
          <div
            v-if="filteredDicList.length === 0 && !loadingDicList"
            class="empty-list"
          >
            <i class="el-icon-folder-opened empty-icon" />
            <span class="empty-text">暂无字典数据</span>
          </div>
          <div
            v-for="(dic, index) in filteredDicList"
            :key="dic.dicCode"
            class="dic-item"
            :class="{ active: selectedDic && selectedDic.dicCode === dic.dicCode }"
            @click="handleSelectDic(dic)"
          >
            <div class="dic-item-main">
              <div class="dic-item-info">
                <span class="dic-item-name">{{ dic.dicName }}</span>
                <span class="dic-item-code">{{ dic.dicCode }}</span>
              </div>
              <div class="dic-item-meta">
                <span class="dic-item-count">
                  <i class="el-icon-collection-tag" />
                  {{ (dic.items || []).length }}
                </span>
                <el-tag
                  :type="dic.flag === 1 ? 'success' : 'info'"
                  size="mini"
                  effect="light"
                  class="dic-status-tag"
                >
                  {{ dic.flag === 1 ? '启用' : '禁用' }}
                </el-tag>
              </div>
            </div>
            <div class="dic-item-actions">
              <el-tooltip content="编辑" placement="top" :open-delay="300">
                <i class="el-icon-edit action-icon" @click.stop="handleEditDic(dic)" />
              </el-tooltip>
              <el-tooltip
                :content="dic.flag === 1 ? '禁用' : '启用'"
                placement="top"
                :open-delay="300"
              >
                <i
                  class="action-icon"
                  :class="dic.flag === 1 ? 'el-icon-turn-off' : 'el-icon-open'"
                  @click.stop="handleToggleDic(dic)"
                />
              </el-tooltip>
              <el-tooltip content="删除" placement="top" :open-delay="300">
                <i class="el-icon-delete action-icon danger" @click.stop="handleDeleteDic(dic)" />
              </el-tooltip>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧面板：字典项管理 -->
      <div class="right-panel">
        <!-- 未选中字典时的占位 -->
        <div v-if="!selectedDic" class="placeholder-state">
          <div class="placeholder-graphic">
            <svg width="120" height="120" viewBox="0 0 120 120" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect x="10" y="20" width="100" height="80" rx="8" stroke="#c0c4cc" stroke-width="2" fill="none" />
              <line x1="10" y1="44" x2="110" y2="44" stroke="#c0c4cc" stroke-width="2" />
              <line x1="60" y1="44" x2="60" y2="100" stroke="#c0c4cc" stroke-width="2" />
              <rect x="20" y="52" width="32" height="6" rx="3" fill="#e4e7ed" />
              <rect x="20" y="64" width="32" height="6" rx="3" fill="#e4e7ed" />
              <rect x="20" y="76" width="32" height="6" rx="3" fill="#e4e7ed" />
              <rect x="68" y="52" width="32" height="6" rx="3" fill="#e4e7ed" />
              <rect x="68" y="64" width="32" height="6" rx="3" fill="#e4e7ed" />
              <rect x="68" y="76" width="32" height="6" rx="3" fill="#e4e7ed" />
            </svg>
          </div>
          <p class="placeholder-title">请选择一个字典</p>
          <p class="placeholder-desc">从左侧列表中选择字典以查看和管理其字典项</p>
        </div>

        <!-- 选中字典后的字典项管理 -->
        <div v-else class="items-panel">
          <div class="panel-header items-header">
            <div class="items-header-left">
              <span class="panel-title">{{ selectedDic.dicName }}</span>
              <el-tag size="small" type="info" effect="plain" class="items-code-tag">
                {{ selectedDic.dicCode }}
              </el-tag>
            </div>
            <div class="items-header-actions">
              <el-tooltip content="刷新字典项" placement="top">
                <el-button
                  icon="el-icon-refresh"
                  circle
                  size="small"
                  @click="handleRefreshItems"
                />
              </el-tooltip>
              <el-button
                type="primary"
                icon="el-icon-plus"
                size="small"
                @click="handleAddDicItem"
              >
                新增字典项
              </el-button>
            </div>
          </div>

          <!-- 字典项表格 -->
          <el-table
            :data="selectedDic.items || []"
            border
            stripe
            size="small"
            class="items-table"
            empty-text="暂无字典项数据"
            :header-cell-style="{ background: '#f5f7fa', color: '#606266', fontWeight: '600' }"
          >
            <el-table-column type="index" label="#" width="50" align="center" />
            <el-table-column prop="label" label="标签" min-width="140">
              <template slot-scope="{ row }">
                <span class="item-label">{{ row.label }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="value" label="值" min-width="140">
              <template slot-scope="{ row }">
                <code class="item-value">{{ row.value }}</code>
              </template>
            </el-table-column>
            <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
            <el-table-column prop="flag" label="状态" width="90" align="center">
              <template slot-scope="{ row }">
                <el-tag
                  :type="row.flag === 1 ? 'success' : 'info'"
                  size="mini"
                  effect="light"
                >
                  {{ row.flag === 1 ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" align="center" fixed="right">
              <template slot-scope="{ row }">
                <el-button
                  size="mini"
                  type="text"
                  :class="row.flag === 1 ? 'warning-text' : 'success-text'"
                  @click="handleToggleItem(row)"
                >
                  {{ row.flag === 1 ? '禁用' : '启用' }}
                </el-button>
                <el-button
                  size="mini"
                  type="text"
                  class="danger-text"
                  @click="handleDeleteItem(row)"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </div>

    <!-- 新增/编辑字典弹窗 -->
    <el-dialog
      :title="dicDialogTitle"
      :visible.sync="dicDialogVisible"
      width="480px"
      :close-on-click-modal="false"
      @close="resetDicForm"
    >
      <el-form
        ref="dicForm"
        :model="dicForm"
        :rules="dicFormRules"
        label-width="80px"
        class="dic-dialog-form"
      >
        <el-form-item label="字典编码" prop="dicCode">
          <el-input
            v-model="dicForm.dicCode"
            placeholder="请输入字典编码"
            :disabled="dicFormType === 'edit'"
            maxlength="64"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="字典名称" prop="dicName">
          <el-input
            v-model="dicForm.dicName"
            placeholder="请输入字典名称"
            maxlength="128"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="dicForm.dicDesc"
            type="textarea"
            placeholder="请输入字典描述（可选）"
            :rows="3"
            maxlength="512"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dicDialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="savingDic" @click="handleSaveDic">
          {{ savingDic ? '保存中...' : '确 定' }}
        </el-button>
      </div>
    </el-dialog>

    <!-- 新增字典项弹窗 -->
    <el-dialog
      title="新增字典项"
      :visible.sync="itemDialogVisible"
      width="480px"
      :close-on-click-modal="false"
      @close="resetItemForm"
    >
      <el-form
        ref="itemForm"
        :model="itemForm"
        :rules="itemFormRules"
        label-width="100px"
        class="dic-dialog-form"
      >
        <el-form-item label="字典编码">
          <el-input :value="selectedDic ? selectedDic.dicCode : ''" disabled />
        </el-form-item>
        <el-form-item label="字典项标签" prop="dicItemLabel">
          <el-input
            v-model="itemForm.dicItemLabel"
            placeholder="请输入字典项标签（显示名称）"
            maxlength="128"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="字典项值" prop="dicItemValue">
          <el-input
            v-model="itemForm.dicItemValue"
            placeholder="请输入字典项值（存储值）"
            maxlength="128"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="itemForm.dicItemDesc"
            type="textarea"
            placeholder="请输入字典项描述（可选）"
            :rows="2"
            maxlength="512"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="itemDialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="savingItem" @click="handleSaveDicItem">
          {{ savingItem ? '保存中...' : '确 定' }}
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {
  queryDics,
  saveDic,
  enableDic,
  disableDic,
  deleteDic,
  saveDicItem,
  enableDicItem,
  disableDicItem,
  deleteDicItem
} from '@/api/dic'

export default {
  name: 'DataDicAdminView',
  data() {
    return {
      // 字典列表
      dicList: [],
      loadingDicList: false,
      searchKeyword: '',
      searchTimer: null,

      // 选中的字典
      selectedDic: null,

      // 字典表单
      dicDialogVisible: false,
      dicFormType: 'add', // 'add' | 'edit'
      dicForm: { dicCode: '', dicName: '', dicDesc: '' },
      dicFormRules: {
        dicCode: [
          { required: true, message: '请输入字典编码', trigger: 'blur' }
        ],
        dicName: [
          { required: true, message: '请输入字典名称', trigger: 'blur' }
        ]
      },
      savingDic: false,

      // 字典项表单
      itemDialogVisible: false,
      itemForm: { dicItemLabel: '', dicItemValue: '', dicItemDesc: '' },
      itemFormRules: {
        dicItemLabel: [
          { required: true, message: '请输入字典项标签', trigger: 'blur' }
        ],
        dicItemValue: [
          { required: true, message: '请输入字典项值', trigger: 'blur' }
        ]
      },
      savingItem: false
    }
  },
  computed: {
    filteredDicList() {
      if (!this.searchKeyword) {
        return this.dicList
      }
      const keyword = this.searchKeyword.toLowerCase()
      return this.dicList.filter(dic => {
        return (dic.dicName && dic.dicName.toLowerCase().includes(keyword)) ||
               (dic.dicCode && dic.dicCode.toLowerCase().includes(keyword))
      })
    },
    dicDialogTitle() {
      return this.dicFormType === 'add' ? '新增数据字典' : '编辑数据字典'
    }
  },
  mounted() {
    this.loadDicList()
  },
  methods: {
    // ========== 字典列表操作 ==========

    async loadDicList() {
      this.loadingDicList = true
      try {
        const res = await queryDics({ dicCode: '', dicName: '' })
        this.dicList = res.data || []
      } catch (error) {
        console.error('加载字典列表失败:', error)
        this.$message.error('加载字典列表失败')
      } finally {
        this.loadingDicList = false
      }
    },

    handleSearchInput() {
      // 防抖处理，这里直接使用 computed 的 filteredDicList
      // 输入时自然过滤
    },

    handleSelectDic(dic) {
      this.selectedDic = dic
    },

    handleRefreshItems() {
      // 重新加载字典列表以刷新选中字典的数据
      this.loadDicList().then(() => {
        if (this.selectedDic) {
          // 重新选中同字典
          const updated = this.dicList.find(d => d.dicCode === this.selectedDic.dicCode)
          if (updated) {
            this.selectedDic = updated
          } else {
            this.selectedDic = null
          }
        }
      })
    },

    // ========== 字典 CRUD ==========

    handleAddDic() {
      this.dicFormType = 'add'
      this.dicForm = { dicCode: '', dicName: '', dicDesc: '' }
      this.dicDialogVisible = true
      this.$nextTick(() => {
        this.$refs.dicForm && this.$refs.dicForm.clearValidate()
      })
    },

    handleEditDic(dic) {
      this.dicFormType = 'edit'
      this.dicForm = { dicCode: dic.dicCode, dicName: dic.dicName, dicDesc: dic.remark || '' }
      this.dicDialogVisible = true
      this.$nextTick(() => {
        this.$refs.dicForm && this.$refs.dicForm.clearValidate()
      })
    },

    async handleSaveDic() {
      try {
        await this.$refs.dicForm.validate()
      } catch {
        return
      }

      this.savingDic = true
      try {
        await saveDic({
          dicCode: this.dicForm.dicCode,
          dicName: this.dicForm.dicName,
          dicDesc: this.dicForm.dicDesc
        })
        this.$message.success(this.dicFormType === 'add' ? '新增字典成功' : '编辑字典成功')
        this.dicDialogVisible = false
        await this.loadDicList()
        // 如果编辑的是当前选中的字典，更新选中状态
        if (this.dicFormType === 'edit' && this.selectedDic) {
          const updated = this.dicList.find(d => d.dicCode === this.dicForm.dicCode)
          if (updated) {
            this.selectedDic = updated
          }
        }
      } catch (error) {
        console.error('保存字典失败:', error)
        this.$message.error('保存字典失败')
      } finally {
        this.savingDic = false
      }
    },

    resetDicForm() {
      if (this.$refs.dicForm) {
        this.$refs.dicForm.resetFields()
      }
    },

    async handleToggleDic(dic) {
      const isEnable = dic.flag !== 1
      const action = isEnable ? '启用' : '禁用'
      try {
        await this.$confirm(
          `确认要${action}字典"${dic.dicName}"吗？`,
          '操作确认',
          { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
        )
        if (isEnable) {
          await enableDic(dic.dicCode)
        } else {
          await disableDic(dic.dicCode)
        }
        this.$message.success(`${action}字典成功`)
        await this.loadDicList()
        // 更新选中状态
        if (this.selectedDic && this.selectedDic.dicCode === dic.dicCode) {
          const updated = this.dicList.find(d => d.dicCode === dic.dicCode)
          this.selectedDic = updated || null
        }
      } catch (error) {
        if (error !== 'cancel') {
          console.error(`${action}字典失败:`, error)
          this.$message.error(`${action}字典失败`)
        }
      }
    },

    async handleDeleteDic(dic) {
      try {
        await this.$confirm(
          `确认要删除字典"${dic.dicName}"吗？此操作不可恢复。`,
          '删除确认',
          { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'error' }
        )
        await deleteDic(dic.dicCode)
        this.$message.success('删除字典成功')
        if (this.selectedDic && this.selectedDic.dicCode === dic.dicCode) {
          this.selectedDic = null
        }
        await this.loadDicList()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除字典失败:', error)
          this.$message.error('删除字典失败')
        }
      }
    },

    // ========== 字典项操作 ==========

    handleAddDicItem() {
      this.itemForm = { dicItemLabel: '', dicItemValue: '', dicItemDesc: '' }
      this.itemDialogVisible = true
      this.$nextTick(() => {
        this.$refs.itemForm && this.$refs.itemForm.clearValidate()
      })
    },

    async handleSaveDicItem() {
      try {
        await this.$refs.itemForm.validate()
      } catch {
        return
      }

      this.savingItem = true
      try {
        await saveDicItem({
          dicCode: this.selectedDic.dicCode,
          dicItemLabel: this.itemForm.dicItemLabel,
          dicItemValue: this.itemForm.dicItemValue,
          dicItemDesc: this.itemForm.dicItemDesc
        })
        this.$message.success('新增字典项成功')
        this.itemDialogVisible = false
        await this.handleRefreshItems()
      } catch (error) {
        console.error('保存字典项失败:', error)
        this.$message.error('保存字典项失败')
      } finally {
        this.savingItem = false
      }
    },

    resetItemForm() {
      if (this.$refs.itemForm) {
        this.$refs.itemForm.resetFields()
      }
    },

    async handleToggleItem(row) {
      const isEnable = row.flag !== 1
      const action = isEnable ? '启用' : '禁用'
      try {
        await this.$confirm(
          `确认要${action}字典项"${row.label}"吗？`,
          '操作确认',
          { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
        )
        if (isEnable) {
          await enableDicItem(this.selectedDic.dicCode, row.label)
        } else {
          await disableDicItem(this.selectedDic.dicCode, row.label)
        }
        this.$message.success(`${action}字典项成功`)
        await this.handleRefreshItems()
      } catch (error) {
        if (error !== 'cancel') {
          console.error(`${action}字典项失败:`, error)
          this.$message.error(`${action}字典项失败`)
        }
      }
    },

    async handleDeleteItem(row) {
      try {
        await this.$confirm(
          `确认要删除字典项"${row.label}"吗？此操作不可恢复。`,
          '删除确认',
          { confirmButtonText: '确定删除', cancelButtonText: '取消', type: 'error' }
        )
        await deleteDicItem(this.selectedDic.dicCode, row.label)
        this.$message.success('删除字典项成功')
        await this.handleRefreshItems()
      } catch (error) {
        if (error !== 'cancel') {
          console.error('删除字典项失败:', error)
          this.$message.error('删除字典项失败')
        }
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.dic-admin {
  padding: 0;
  min-height: calc(100vh - 84px);
}

/* ========== 页面标题 ========== */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 24px 28px 20px;
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.04);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.03);
}

.page-title {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: #1a1a2e;
  display: flex;
  align-items: center;
  gap: 10px;
  letter-spacing: -0.02em;
}

.title-icon {
  color: #3b82f6;
  font-size: 24px;
}

.page-subtitle {
  display: block;
  margin-top: 4px;
  font-size: 13px;
  color: #8c8c8c;
  letter-spacing: 0;
}

/* ========== 主体区域 ========== */
.main-content {
  display: flex;
  gap: 20px;
  min-height: calc(100vh - 220px);
}

/* ========== 左侧面板 ========== */
.left-panel {
  width: 380px;
  min-width: 320px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  background: #fff;
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.04);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.03);
  overflow: hidden;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  background: linear-gradient(180deg, rgba(248, 250, 252, 0.8) 0%, rgba(255, 255, 255, 0) 100%);
}

.panel-title {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.add-dic-btn {
  border-radius: 6px;
  font-weight: 500;
}

.search-box {
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;

  ::v-deep .el-input__inner {
    border-radius: 8px;
    background: #f5f7fa;
    border: 1px solid transparent;
    transition: all 0.3s;

    &:hover {
      border-color: #dcdfe6;
    }

    &:focus {
      border-color: #3b82f6;
      background: #fff;
      box-shadow: 0 0 0 2px rgba(59, 130, 246, 0.1);
    }
  }
}

/* ========== 字典列表 ========== */
.dic-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;

  &::-webkit-scrollbar {
    width: 5px;
  }

  &::-webkit-scrollbar-thumb {
    background: #dcdfe6;
    border-radius: 3px;
  }

  &::-webkit-scrollbar-thumb:hover {
    background: #c0c4cc;
  }
}

.empty-list {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #c0c4cc;

  .empty-icon {
    font-size: 48px;
    margin-bottom: 12px;
  }

  .empty-text {
    font-size: 14px;
  }
}

.dic-item {
  position: relative;
  padding: 14px 16px;
  margin-bottom: 4px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid transparent;

  &:hover {
    background: #f5f7fa;
    border-color: #ebeef5;

    .dic-item-actions {
      opacity: 1;
      transform: translateX(0);
    }
  }

  &.active {
    background: linear-gradient(135deg, rgba(59, 130, 246, 0.06) 0%, rgba(59, 130, 246, 0.03) 100%);
    border-color: rgba(59, 130, 246, 0.2);
    box-shadow: 0 2px 8px rgba(59, 130, 246, 0.08);

    .dic-item-name {
      color: #3b82f6;
    }

    .dic-item-actions {
      opacity: 1;
      transform: translateX(0);
    }
  }
}

.dic-item-main {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.dic-item-info {
  flex: 1;
  min-width: 0;
}

.dic-item-name {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  margin-bottom: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dic-item-code {
  display: block;
  font-size: 12px;
  color: #909399;
  font-family: 'SF Mono', 'Fira Code', monospace;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dic-item-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
  margin-left: 12px;
}

.dic-item-count {
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 2px;
}

.dic-status-tag {
  transform: scale(0.85);
}

.dic-item-actions {
  position: absolute;
  right: 12px;
  top: 50%;
  transform: translateY(-50%) translateX(8px);
  display: flex;
  gap: 6px;
  opacity: 0;
  transition: all 0.2s;
  z-index: 1;
}

.action-icon {
  font-size: 14px;
  color: #909399;
  padding: 4px;
  border-radius: 4px;
  transition: all 0.2s;

  &:hover {
    background: #e8eaed;
    color: #303133;
  }

  &.danger:hover {
    background: #fef0f0;
    color: #f56c6c;
  }
}

/* ========== 右侧面板 ========== */
.right-panel {
  flex: 1;
  background: #fff;
  border-radius: 12px;
  border: 1px solid rgba(0, 0, 0, 0.04);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.03);
  overflow: hidden;
}

.placeholder-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  min-height: 500px;
  color: #c0c4cc;
}

.placeholder-graphic {
  margin-bottom: 20px;
  opacity: 0.6;
}

.placeholder-title {
  font-size: 16px;
  font-weight: 500;
  color: #909399;
  margin: 0 0 8px;
}

.placeholder-desc {
  font-size: 13px;
  color: #c0c4cc;
  margin: 0;
}

.items-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.items-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.items-header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.items-code-tag {
  font-family: 'SF Mono', 'Fira Code', monospace;
}

.items-header-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.items-table {
  flex: 1;

  .item-label {
    font-weight: 500;
    color: #303133;
  }

  .item-value {
    font-family: 'SF Mono', 'Fira Code', monospace;
    font-size: 12px;
    background: #f5f7fa;
    padding: 2px 8px;
    border-radius: 4px;
    color: #606266;
    border: 1px solid #ebeef5;
  }
}

/* ========== 表格操作按钮颜色 ========== */
.warning-text {
  color: #e6a23c !important;

  &:hover {
    color: #ebb563 !important;
  }
}

.success-text {
  color: #67c23a !important;

  &:hover {
    color: #85ce61 !important;
  }
}

.danger-text {
  color: #f56c6c !important;

  &:hover {
    color: #f78989 !important;
  }
}

/* ========== 弹窗表单 ========== */
.dic-dialog-form {
  padding: 10px 20px 0;

  ::v-deep .el-input__inner {
    border-radius: 6px;
  }

  ::v-deep .el-form-item {
    margin-bottom: 22px;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 0 20px 10px;
}

/* ========== 响应式 ========== */
@media (max-width: 900px) {
  .main-content {
    flex-direction: column;
  }

  .left-panel {
    width: 100%;
    min-width: auto;
    max-height: 400px;
  }

  .right-panel {
    min-height: 400px;
  }

  .page-header {
    padding: 18px 16px 14px;
  }

  .page-title {
    font-size: 18px;
  }
}

@media (max-width: 600px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .items-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .dic-item-actions {
    opacity: 1;
    transform: translateX(0);
  }
}
</style>

<template>
  <div class="department-management">
    <div class="header-section">
      <h2>部门管理</h2>
      <div class="header-actions">
        <el-button type="primary" icon="el-icon-plus" @click="handleAddDepartment">新增部门</el-button>
      </div>
    </div>

    <!-- 部门树 -->
    <el-card class="tree-card" shadow="never">
      <div slot="header" class="clearfix">
        <span>部门结构</span>
        <div class="tree-header-actions">
          <el-input
            v-model="filterText"
            placeholder="输入部门名称进行过滤"
            size="small"
            style="width: 200px; margin-right: 10px;"
            clearable
          >
            <i slot="prefix" class="el-input__icon el-icon-search"></i>
          </el-input>
          <el-button
            icon="el-icon-refresh"
            circle
            size="mini"
            @click="loadDepartments"
            title="刷新数据"
          ></el-button>
        </div>
      </div>

      <el-tree
        ref="departmentTree"
        :data="departmentTreeData"
        :props="treeProps"
        :filter-node-method="filterNode"
        :expand-on-click-node="false"
        :default-expand-all="true"
        node-key="id"
        highlight-current
        @node-click="handleNodeClick"
      >
        <span class="custom-tree-node" slot-scope="{ node, data }">
          <span>{{ node.label }}</span>
          <span>
            <el-button
              type="text"
              size="mini"
              @click="handleAddChild(data)"
              icon="el-icon-plus"
            >
              添加子部门
            </el-button>
            <el-button
              type="text"
              size="mini"
              @click="handleEdit(data)"
              icon="el-icon-edit"
            >
              编辑
            </el-button>
            <el-button
              type="text"
              size="mini"
              @click="handleDelete(data)"
              icon="el-icon-delete"
              :disabled="data.id === 1"
            >
              删除
            </el-button>
          </span>
        </span>
      </el-tree>
    </el-card>

    <!-- 部门详情 -->
    <el-card class="detail-card" shadow="never" v-if="currentDepartment">
      <div slot="header" class="clearfix">
        <span>部门详情</span>
      </div>

      <el-descriptions :column="1" border>
        <el-descriptions-item label="部门ID">{{ currentDepartment.id }}</el-descriptions-item>
        <el-descriptions-item label="部门名称">{{ currentDepartment.departmentName }}</el-descriptions-item>
        <el-descriptions-item label="父级部门">{{
            getParentDepartmentName(currentDepartment.parentId)
          }}
        </el-descriptions-item>
        <el-descriptions-item label="排序">{{ currentDepartment.sortOrder || '无' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDate(currentDepartment.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{
            formatDate(currentDepartment.updateTime) || '无'
          }}
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="currentDepartment.flag === 1 ? 'success' : 'danger'">
            {{ currentDepartment.flag === 1 ? '启用' : '禁用' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="部门描述">{{ currentDepartment.description || '无' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 部门编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="500px"
      @close="handleDialogClose"
    >
      <el-form
        :model="departmentForm"
        :rules="departmentRules"
        ref="departmentForm"
        label-width="100px"
      >
        <el-form-item label="部门名称" prop="departmentName">
          <el-input v-model="departmentForm.departmentName"/>
        </el-form-item>
        <el-form-item label="父级部门" prop="parentId">
          <el-select
            v-model="departmentForm.parentId"
            placeholder="请选择父级部门"
            style="width: 100%"
            :disabled="isEdit && departmentForm.id === 1"
          >
            <el-option
              v-for="dept in departmentOptions"
              :key="dept.id"
              :label="dept.departmentName"
              :value="dept.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number
            v-model="departmentForm.sortOrder"
            :min="0"
            controls-position="right"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态" prop="flag">
          <el-switch
            v-model="departmentForm.flag"
            :active-value="1"
            :inactive-value="0"
          />
        </el-form-item>
        <el-form-item label="部门描述" prop="description">
          <el-input
            v-model="departmentForm.description"
            type="textarea"
            :rows="3"
          />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitDepartment">确 定</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {fetchDepartmentList} from '@/api/department'
import {formatDate} from '@/utils'

export default {
  name: 'DepartmentAdminView',
  data() {
    return {
      filterText: '',
      departmentTreeData: [],
      treeProps: {
        children: 'children',
        label: 'departmentName'
      },
      currentDepartment: null,
      dialogVisible: false,
      dialogTitle: '',
      isEdit: false,
      departmentForm: {
        departmentName: '',
        parentId: 1,
        sortOrder: 0,
        flag: 1,
        description: ''
      },
      departmentRules: {
        departmentName: [
          {required: true, message: '请输入部门名称', trigger: 'blur'}
        ],
        parentId: [
          {required: true, message: '请选择父级部门', trigger: 'change'}
        ]
      },
      departmentOptions: [],
      loading: false
    }
  },
  watch: {
    filterText(val) {
      this.$refs.departmentTree.filter(val)
    }
  },
  created() {
    this.loadDepartments()
  },
  methods: {
    formatDate(date) {
      return formatDate(date)
    },

    // 加载部门数据
    async loadDepartments() {
      this.loading = true
      try {
        const response = await fetchDepartmentList({})
        if (response.code === 200) {
          // 根据后端返回的数据结构处理数据
          // 后端返回的是AjaxResult对象，其中data字段包含PageVO对象
          // PageVO对象包含total和data字段
          let departments = []
          if (response.data && response.data.data) {
            // 如果是分页数据结构 PageVO
            departments = response.data.data
          } else if (response.data) {
            // 如果是普通列表数据
            departments = response.data
          }

          // 构建树形结构
          this.departmentTreeData = this.buildTree(departments)
          // 设置下拉选项
          this.departmentOptions = departments
          // 默认选中根部门
          if (this.departmentTreeData.length > 0) {
            this.currentDepartment = this.departmentTreeData[0]
          }
        } else {
          this.$message.error(response.msg || '获取部门数据失败')
        }
      } catch (error) {
        this.$message.error('获取部门数据失败: ' + error.message)
      } finally {
        this.loading = false
      }
    },

    // 构建部门树
    buildTree(departments) {
      // 创建映射表
      const map = {}
      const roots = []

      // 初始化所有节点
      departments.forEach(dept => {
        map[dept.id] = {...dept, children: []}
      })

      // 构建树结构
      departments.forEach(dept => {
        const node = map[dept.id]
        if (dept.parentId === 0 || dept.parentId === null) {
          roots.push(node)
        } else {
          const parent = map[dept.parentId]
          if (parent) {
            parent.children.push(node)
          }
        }
      })

      return roots
    },

    // 过滤节点
    filterNode(value, data) {
      if (!value) return true
      return data.departmentName.indexOf(value) !== -1
    },

    // 节点点击事件
    handleNodeClick(data) {
      this.currentDepartment = data
    },

    // 获取父级部门名称
    getParentDepartmentName(parentId) {
      if (parentId === 0 || parentId === null) return '根节点'
      const parent = this.departmentOptions.find(dept => dept.id === parentId)
      return parent ? parent.departmentName : '未知'
    },

    // 新增部门
    handleAddDepartment() {
      this.isEdit = false
      this.dialogTitle = '新增部门'
      this.departmentForm = {
        departmentName: '',
        parentId: 1,
        sortOrder: 0,
        flag: 1,
        description: ''
      }
      this.dialogVisible = true
    },

    // 添加子部门
    handleAddChild(data) {
      this.isEdit = false
      this.dialogTitle = '新增子部门'
      this.departmentForm = {
        departmentName: '',
        parentId: data.id,
        sortOrder: 0,
        flag: 1,
        description: ''
      }
      this.dialogVisible = true
    },

    // 编辑部门
    handleEdit(data) {
      this.isEdit = true
      this.dialogTitle = '编辑部门'
      this.departmentForm = {...data}
      this.dialogVisible = true
    },

    // 删除部门
    handleDelete(data) {
      this.$confirm(`确定要删除部门 "${data.departmentName}" 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        // 这里应该调用删除接口
        this.$message.success('删除成功')
        // 重新加载数据
        this.loadDepartments()
      }).catch(() => {
        this.$message.info('已取消删除')
      })
    },

    // 提交部门表单
    submitDepartment() {
      this.$refs.departmentForm.validate(async (valid) => {
        if (valid) {
          try {
            // 这里应该调用保存接口
            this.$message.success(this.isEdit ? '修改成功' : '新增成功')
            this.dialogVisible = false
            // 重新加载数据
            this.loadDepartments()
          } catch (error) {
            this.$message.error((this.isEdit ? '修改' : '新增') + '失败: ' + error.message)
          }
        }
      })
    },

    // 关闭对话框
    handleDialogClose() {
      this.$refs.departmentForm.resetFields()
    }
  }
}
</script>

<style scoped>
.department-management {
  padding: 20px;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-section h2 {
  margin: 0;
}

.tree-card,
.detail-card {
  margin-bottom: 20px;
}

.tree-header-actions {
  float: right;
  display: flex;
  align-items: center;
}

.custom-tree-node {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 14px;
  padding-right: 8px;
}

.custom-tree-node .el-button {
  padding: 4px;
  margin-left: 8px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.clearfix:before,
.clearfix:after {
  display: table;
  content: "";
}

.clearfix:after {
  clear: both;
}
</style>

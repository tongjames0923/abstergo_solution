<template>
  <div class="route-manager-container">
    <el-card class="box-card">
      <div slot="header" class="clearfix">
        <span>{{ $t('routeManager.title') }}</span>
        <el-button style="float: right; padding: 3px 0" type="text" @click="handleRefresh">
          <i class="el-icon-refresh"></i> {{ $t('routeManager.refreshCache') }}
        </el-button>
      </div>

      <el-row :gutter="20">
        <el-col :span="6">
          <div class="group-list">
            <div class="group-header">
              <span>{{ $t('routeManager.routeGroup') }}</span>
              <el-button type="text" icon="el-icon-plus" @click="handleAddGroup">{{ $t('routeManager.addGroup') }}</el-button>
            </div>
            <el-menu
              :default-active="activeGroup"
              @select="handleGroupSelect"
              class="group-menu"
            >
              <el-menu-item
                v-for="group in routeGroups"
                :key="group"
                :index="group"
              >
                <span>{{ group }}</span>
                <el-button
                  type="text"
                  icon="el-icon-delete"
                  class="delete-btn"
                  @click.stop="handleDeleteGroup(group)"
                ></el-button>
              </el-menu-item>
            </el-menu>
          </div>
        </el-col>

        <el-col :span="18">
          <div class="route-editor">
            <div class="editor-header">
              <span>{{ $t('routeManager.routeConfig') }} - {{ activeGroup || $t('routeManager.selectGroup') }}</span>
              <div class="editor-actions">
                <el-button
                  type="primary"
                  size="small"
                  :disabled="!activeGroup"
                  @click="handleSaveRoutes"
                >
                  {{ $t('routeManager.saveConfig') }}
                </el-button>
                <el-button
                  type="success"
                  size="small"
                  :disabled="!activeGroup"
                  @click="handleAddRoute"
                >
                  {{ $t('routeManager.addRoute') }}
                </el-button>
              </div>
            </div>

            <div v-if="activeGroup" class="route-content">
              <el-table
                :data="currentRoutes"
                border
                style="width: 100%"
                row-key="path"
                :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
              >
                <el-table-column prop="path" :label="$t('routeManager.path')" width="200"></el-table-column>
                <el-table-column prop="component" :label="$t('routeManager.component')" width="200"></el-table-column>
                <el-table-column prop="name" :label="$t('routeManager.name')" width="150"></el-table-column>
                <el-table-column prop="redirect" :label="$t('routeManager.redirect')" width="150"></el-table-column>
                <el-table-column :label="$t('routeManager.hidden')" width="80">
                  <template slot-scope="scope">
                    <el-tag :type="scope.row.hidden ? 'danger' : 'success'" size="small">
                      {{ scope.row.hidden ? $t('routeManager.yes') : $t('routeManager.no') }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column :label="$t('routeManager.alwaysShow')" width="80">
                  <template slot-scope="scope">
                    <el-tag :type="scope.row.alwaysShow ? 'success' : 'info'" size="small">
                      {{ scope.row.alwaysShow ? $t('routeManager.yes') : $t('routeManager.no') }}
                    </el-tag>
                  </template>
                </el-table-column>
                <el-table-column :label="$t('common.operation')" width="200">
                  <template slot-scope="scope">
                    <el-button
                      type="text"
                      size="small"
                      @click="handleEditRoute(scope.row)"
                    >
                      {{ $t('routeManager.editRoute') }}
                    </el-button>
                    <el-button
                      type="text"
                      size="small"
                      @click="handleAddChildRoute(scope.row)"
                    >
                      {{ $t('routeManager.addChildRoute') }}
                    </el-button>
                    <el-button
                      type="text"
                      size="small"
                      class="delete-btn"
                      @click="handleDeleteRoute(scope.row)"
                    >
                      {{ $t('routeManager.deleteRoute') }}
                    </el-button>
                  </template>
                </el-table-column>
              </el-table>
            </div>

            <div v-else class="empty-state">
              <i class="el-icon-folder-opened"></i>
              <p>{{ $t('routeManager.selectGroup') }}</p>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 路由编辑对话框 -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form :model="routeForm" :rules="routeRules" ref="routeForm" label-width="100px">
        <el-form-item :label="$t('routeManager.path')" prop="path">
          <el-input v-model="routeForm.path" :placeholder="$t('routeManager.inputPath')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('routeManager.component')" prop="component">
          <el-select
            v-model="routeForm.component"
            filterable
            allow-create
            :placeholder="$t('routeManager.inputComponent')"
            style="width: 100%"
          >
            <el-option
              v-for="item in viewComponents"
              :key="item"
              :label="item"
              :value="item"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('routeManager.name')" prop="name">
          <el-input v-model="routeForm.name" :placeholder="$t('routeManager.inputName')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('routeManager.redirect')" prop="redirect">
          <el-input v-model="routeForm.redirect" :placeholder="$t('routeManager.inputRedirect')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('routeManager.hidden')">
          <el-switch v-model="routeForm.hidden"></el-switch>
        </el-form-item>
        <el-form-item :label="$t('routeManager.alwaysShow')">
          <el-switch v-model="routeForm.alwaysShow"></el-switch>
        </el-form-item>

        <el-divider content-position="left">{{ $t('routeManager.metaConfig') }}</el-divider>

        <el-form-item :label="$t('routeManager.title')" prop="meta.title">
          <el-input v-model="routeForm.meta.title" :placeholder="$t('routeManager.inputTitle')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('routeManager.icon')" prop="meta.icon">
          <el-input v-model="routeForm.meta.icon" :placeholder="$t('routeManager.inputIcon')"></el-input>
        </el-form-item>
        <el-form-item :label="$t('routeManager.roles')">
          <el-select
            v-model="routeForm.meta.roles"
            multiple
            filterable
            allow-create
            default-first-option
            :placeholder="$t('routeManager.selectRoles')"
          >
            <el-option
              v-for="item in availableRoles"
              :key="item.role"
              :label="item.name"
              :value="item.role"
            ></el-option>
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('routeManager.breadcrumb')">
          <el-switch v-model="routeForm.meta.breadcrumb"></el-switch>
        </el-form-item>
        <el-form-item :label="$t('routeManager.activeMenu')" prop="meta.activeMenu">
          <el-input v-model="routeForm.meta.activeMenu" :placeholder="$t('routeManager.inputActiveMenu')"></el-input>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleDialogConfirm">{{ $t('common.confirm') }}</el-button>
      </span>
    </el-dialog>
  </div>
</template>

<script>
import {
  getRouteGroups,
  getRouteConfigsByGroup,
  saveRouteConfigs,
  deleteRouteConfigs,
  refreshRouteCache
} from '@/api/view'
import { queryRoles } from '@/api/role'

export default {
  name: 'RouteManager',
  data() {
    return {
      routeGroups: [],
      activeGroup: '',
      currentRoutes: [],
      dialogVisible: false,
      dialogType: 'add', // add, edit, addChild
      editingRoute: null,
      routeForm: {
        path: '',
        component: '',
        name: '',
        redirect: '',
        hidden: false,
        alwaysShow: false,
        meta: {
          title: '',
          icon: '',
          roles: [],
          breadcrumb: true,
          activeMenu: ''
        }
      },
      routeRules: {
        path: [{ required: true, message: this.$t('routeManager.inputPath'), trigger: 'blur' }],
        component: [{ required: true, message: this.$t('routeManager.inputComponent'), trigger: 'blur' }]
      },
      availableRoles: [],
      viewComponents: []
    }
  },
  computed: {
    dialogTitle() {
      const titles = {
        add: this.$t('routeManager.addRoute'),
        edit: this.$t('routeManager.editRoute'),
        addChild: this.$t('routeManager.addChildRoute')
      }
      return titles[this.dialogType] || this.$t('routeManager.routeConfig')
    }
  },
  created() {
    this.loadRouteGroups()
    this.loadViewComponents()
    this.loadMenuRoles()
  },
  methods: {
    async loadRouteGroups() {
      try {
        const response = await getRouteGroups()
        this.routeGroups = response.data || []
      } catch (error) {
        console.error('获取路由分组失败:', error)
        this.$message.error(this.$t('routeManager.fetchGroupsFailed'))
      }
    },

    loadViewComponents() {
      // 通过 require.context 读取 views 目录下所有 .vue 文件
      const requireContext = require.context('@/views', true, /\.vue$/)
      const components = requireContext.keys()
        .map(key => {
          // key 形如 './admin/user/index.vue'
          // 转换为路由组件路径 '/admin/user/index.vue'
          return '/' + key.replace(/^\.\//, '')
        })
        .filter(path => {
          // 排除 403、404 和登录页
          return !path.includes('403') && !path.includes('404') && !path.includes('login')
        })
        .sort()
      
      // 添加 Layout 组件选项
      components.unshift('Layout')
      
      this.viewComponents = components
    },

    async loadMenuRoles() {
      try {
        // 查询所有菜单类型的权限（type=1），状态为激活（flag=1）
        const response = await queryRoles({ typeEquals: [1], flagEquals: [1] }, 1, 1000)
        const roles = response.data?.data || []
        // 存储完整的权限对象，包含编码和名称
        this.availableRoles = roles.map(r => ({
          role: r.role,
          name: r.name
        }))
      } catch (error) {
        console.error('获取菜单权限失败:', error)
      }
    },

    async handleGroupSelect(group) {
      this.activeGroup = group
      await this.loadRouteConfigs(group)
    },

    async loadRouteConfigs(group) {
      try {
        const response = await getRouteConfigsByGroup(group)
        this.currentRoutes = response.data || []
      } catch (error) {
        console.error('获取路由配置失败:', error)
        this.$message.error(this.$t('routeManager.fetchRoutesFailed'))
      }
    },

    handleAddGroup() {
      this.$prompt(this.$t('routeManager.groupNameInput'), this.$t('routeManager.addGroupTitle'), {
        confirmButtonText: this.$t('common.confirm'),
        cancelButtonText: this.$t('common.cancel'),
        inputPattern: /^[a-zA-Z0-9_-]+$/,
        inputErrorMessage: this.$t('routeManager.groupNamePattern')
      }).then(({ value }) => {
        if (!this.routeGroups.includes(value)) {
          this.routeGroups.push(value)
          this.$message.success(this.$t('routeManager.groupAddSuccess'))
        } else {
          this.$message.warning(this.$t('routeManager.groupExists'))
        }
      }).catch(() => {})
    },

    handleDeleteGroup(group) {
      this.$confirm(this.$t('routeManager.deleteGroupConfirm', { group }), this.$t('common.tip'), {
        confirmButtonText: this.$t('common.confirm'),
        cancelButtonText: this.$t('common.cancel'),
        type: 'warning'
      }).then(async () => {
        try {
          await deleteRouteConfigs(group)
          this.routeGroups = this.routeGroups.filter(g => g !== group)
          if (this.activeGroup === group) {
            this.activeGroup = ''
            this.currentRoutes = []
          }
          this.$message.success(this.$t('routeManager.groupDeleteSuccess'))
        } catch (error) {
          console.error('删除分组失败:', error)
          this.$message.error(this.$t('routeManager.deleteGroupFailed'))
        }
      }).catch(() => {})
    },

    handleAddRoute() {
      this.dialogType = 'add'
      this.editingRoute = null
      this.resetRouteForm()
      this.dialogVisible = true
    },

    handleEditRoute(route) {
      this.dialogType = 'edit'
      this.editingRoute = route
      this.routeForm = {
        path: route.path || '',
        component: route.component || '',
        name: route.name || '',
        redirect: route.redirect || '',
        hidden: route.hidden || false,
        alwaysShow: route.alwaysShow || false,
        meta: {
          title: route.meta?.title || '',
          icon: route.meta?.icon || '',
          roles: route.meta?.roles || [],
          breadcrumb: route.meta?.breadcrumb !== false,
          activeMenu: route.meta?.activeMenu || ''
        }
      }
      this.dialogVisible = true
    },

    handleAddChildRoute(parentRoute) {
      this.dialogType = 'addChild'
      this.editingRoute = parentRoute
      this.resetRouteForm()
      this.dialogVisible = true
    },

    handleDeleteRoute(route) {
      this.$confirm(this.$t('routeManager.deleteRouteConfirm', { path: route.path }), this.$t('common.tip'), {
        confirmButtonText: this.$t('common.confirm'),
        cancelButtonText: this.$t('common.cancel'),
        type: 'warning'
      }).then(() => {
        this.removeRouteFromTree(this.currentRoutes, route)
        this.$message.success(this.$t('routeManager.routeDeleteSuccess'))
      }).catch(() => {})
    },

    removeRouteFromTree(routes, targetRoute) {
      for (let i = 0; i < routes.length; i++) {
        if (routes[i].path === targetRoute.path) {
          routes.splice(i, 1)
          return true
        }
        if (routes[i].children && this.removeRouteFromTree(routes[i].children, targetRoute)) {
          return true
        }
      }
      return false
    },

    resetRouteForm() {
      this.routeForm = {
        path: '',
        component: '',
        name: '',
        redirect: '',
        hidden: false,
        alwaysShow: false,
        meta: {
          title: '',
          icon: '',
          roles: [],
          breadcrumb: true,
          activeMenu: ''
        }
      }
    },

    handleDialogConfirm() {
      this.$refs.routeForm.validate(valid => {
        if (valid) {
          const routeData = {
            path: this.routeForm.path,
            component: this.routeForm.component,
            name: this.routeForm.name,
            redirect: this.routeForm.redirect,
            hidden: this.routeForm.hidden,
            alwaysShow: this.routeForm.alwaysShow,
            meta: {
              routeGroup: this.activeGroup,
              title: this.routeForm.meta.title,
              icon: this.routeForm.meta.icon,
              roles: this.routeForm.meta.roles,
              breadcrumb: this.routeForm.meta.breadcrumb,
              activeMenu: this.routeForm.meta.activeMenu
            }
          }

          if (this.dialogType === 'add') {
            this.currentRoutes.push(routeData)
          } else if (this.dialogType === 'edit') {
            Object.assign(this.editingRoute, routeData)
          } else if (this.dialogType === 'addChild') {
            if (!this.editingRoute.children) {
              this.$set(this.editingRoute, 'children', [])
            }
            this.editingRoute.children.push(routeData)
          }

          this.dialogVisible = false
          this.$message.success(this.$t('routeManager.routeUpdateSuccess'))
        }
      })
    },

    handleDialogClose() {
      this.$refs.routeForm?.resetFields()
    },

    async handleSaveRoutes() {
      try {
        await saveRouteConfigs(this.activeGroup, this.currentRoutes)
        this.$message.success(this.$t('routeManager.routeSaveSuccess'))
      } catch (error) {
        console.error('保存路由配置失败:', error)
        this.$message.error(this.$t('routeManager.saveRoutesFailed'))
      }
    },

    async handleRefresh() {
      try {
        await refreshRouteCache()
        this.$message.success(this.$t('routeManager.cacheRefreshSuccess'))
      } catch (error) {
        console.error('刷新路由缓存失败:', error)
        this.$message.error(this.$t('routeManager.refreshCacheFailed'))
      }
    }
  }
}
</script>

<style scoped>
.route-manager-container {
  padding: 20px;
}

.group-list {
  border: 1px solid #e6e6e6;
  border-radius: 4px;
  height: calc(100vh - 200px);
  overflow-y: auto;
}

.group-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 15px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #e6e6e6;
}

.group-menu {
  border-right: none;
}

.group-menu .el-menu-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.delete-btn {
  color: #f56c6c;
}

.delete-btn:hover {
  color: #f78989;
}

.route-editor {
  border: 1px solid #e6e6e6;
  border-radius: 4px;
  height: calc(100vh - 200px);
  overflow-y: auto;
}

.editor-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 15px;
  background-color: #f5f7fa;
  border-bottom: 1px solid #e6e6e6;
}

.editor-actions {
  display: flex;
  gap: 10px;
}

.route-content {
  padding: 15px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 300px;
  color: #909399;
}

.empty-state i {
  font-size: 48px;
  margin-bottom: 20px;
}
</style>
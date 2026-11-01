<template>
  <div class="route-studio">
    <!-- Atmospheric background -->
    <div class="bg">
      <div class="bg__orb bg__orb--1" />
      <div class="bg__orb bg__orb--2" />
      <div class="bg__orb bg__orb--3" />
      <div class="bg__grain" />
    </div>

    <div class="studio-inner">
      <!-- Header -->
      <header class="studio-header" :class="{ _on: ready }">
        <div class="header-content">
          <div class="header-left">
            <div class="header-icon">
              <i class="fas fa-route" />
            </div>
            <div class="header-text">
              <h1 class="header-title">{{ $t('routeManager.title') }}</h1>
              <p class="header-subtitle">{{ $t('routeManager.subtitle') }}</p>
            </div>
          </div>
          <div class="header-actions">
            <el-button class="ghost-btn" :loading="refreshing" @click="handleRefresh">
              <i v-if="!refreshing" class="fas fa-sync-alt" />
              <span>{{ $t('routeManager.refreshCache') }}</span>
            </el-button>
          </div>
        </div>
      </header>

      <!-- Workspace: group rail + route editor -->
      <div class="workspace" :class="{ _on: ready }">
        <!-- Group rail -->
        <aside class="group-rail">
          <div class="rail-head">
            <span class="rail-title">
              <i class="fas fa-layer-group" />
              {{ $t('routeManager.routeGroup') }}
            </span>
            <el-tooltip :content="$t('routeManager.addGroup')" placement="top">
              <button class="rail-add" @click="handleAddGroup">
                <i class="fas fa-plus" />
              </button>
            </el-tooltip>
          </div>

          <div class="rail-list">
            <div
              v-for="group in routeGroups"
              :key="group"
              class="rail-item"
              :class="{ _active: group === activeGroup }"
              @click="handleGroupSelect(group)"
            >
              <span class="rail-item__dot" />
              <span class="rail-item__name">{{ group }}</span>
              <span
                v-if="group === activeGroup && saving"
                class="rail-item__spin"
              >
                <i class="fas fa-circle-notch fa-spin" />
              </span>
              <button class="rail-item__del" @click.stop="handleDeleteGroup(group)">
                <i class="fas fa-trash-alt" />
              </button>
            </div>

            <div v-if="routeGroups.length === 0" class="rail-empty">
              <i class="fas fa-inbox" />
              <p>{{ $t('routeManager.noGroups') }}</p>
            </div>
          </div>
        </aside>

        <!-- Route editor -->
        <section class="route-pane">
          <template v-if="activeGroup">
            <div class="pane-toolbar">
              <div class="pane-meta">
                <span class="pane-group-chip">
                  <i class="fas fa-folder-open" />
                  {{ activeGroup }}
                </span>
                <span class="pane-count">{{ routeCount }} {{ $t('routeManager.routesUnit') }}</span>
                <transition name="fade">
                  <span v-if="dirty" class="pane-dirty">
                    <i class="fas fa-exclamation-circle" />
                    {{ $t('routeManager.unsavedTip') }}
                  </span>
                </transition>
              </div>
              <div class="pane-actions">
                <el-button
                  size="small"
                  icon="el-icon-plus"
                  :disabled="!activeGroup"
                  @click="handleAddRoute"
                >
                  {{ $t('routeManager.addRoute') }}
                </el-button>
                <el-button
                  size="small"
                  icon="el-icon-time"
                  :disabled="!activeGroup"
                  @click="handleOpenVersions"
                >
                  {{ $t('routeManager.versionHistory') }}
                </el-button>
                <el-button
                  type="primary"
                  size="small"
                  icon="el-icon-check"
                  :loading="saving"
                  :disabled="!activeGroup || !dirty"
                  @click="handleSaveRoutes"
                >
                  {{ $t('routeManager.saveConfig') }}
                </el-button>
              </div>
            </div>

            <div v-loading="loadingRoutes" class="pane-table">
              <el-table
                :key="tableVersion"
                :data="currentRoutes"
                row-key="path"
                :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
                :header-cell-style="{ background: 'rgba(30,64,175,0.045)', color: '#5a6072', fontWeight: 600 }"
                style="width: 100%"
              >
                <el-table-column :label="$t('routeManager.sortOrder')" width="92" align="center">
                  <template slot-scope="scope">
                    <div class="sort-cell">
                      <button
                        class="sort-btn"
                        :disabled="isFirst(scope.row)"
                        :title="$t('routeManager.moveUp')"
                        @click="handleMove(scope.row, -1)"
                      >
                        <i class="el-icon-caret-top" />
                      </button>
                      <button
                        class="sort-btn"
                        :disabled="isLast(scope.row)"
                        :title="$t('routeManager.moveDown')"
                        @click="handleMove(scope.row, 1)"
                      >
                        <i class="el-icon-caret-bottom" />
                      </button>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column prop="path" :label="$t('routeManager.path')" min-width="170">
                  <template slot-scope="scope">
                    <span class="mono path-cell">{{ scope.row.path }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="component" :label="$t('routeManager.component')" min-width="190">
                  <template slot-scope="scope">
                    <el-tag
                      v-if="scope.row.component === 'Layout'"
                      size="mini"
                      effect="plain"
                      class="layout-tag"
                    >
                      <i class="fas fa-columns" /> Layout
                    </el-tag>
                    <span v-else class="mono comp-cell">{{ scope.row.component }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="name" :label="$t('routeManager.name')" min-width="140">
                  <template slot-scope="scope">
                    <span class="name-cell">{{ scope.row.name }}</span>
                  </template>
                </el-table-column>
                <el-table-column prop="redirect" :label="$t('routeManager.redirect')" min-width="130">
                  <template slot-scope="scope">
                    <span v-if="scope.row.redirect" class="mono redirect-cell">
                      <i class="fas fa-share" /> {{ scope.row.redirect }}
                    </span>
                    <span v-else class="dim">—</span>
                  </template>
                </el-table-column>
                <el-table-column :label="$t('routeManager.hidden')" width="76" align="center">
                  <template slot-scope="scope">
                    <span class="flag-dot" :class="scope.row.hidden ? 'flag-dot--warn' : 'flag-dot--ok'">
                      {{ scope.row.hidden ? $t('routeManager.yes') : $t('routeManager.no') }}
                    </span>
                  </template>
                </el-table-column>
                <el-table-column :label="$t('routeManager.alwaysShow')" width="86" align="center">
                  <template slot-scope="scope">
                    <span class="flag-dot" :class="scope.row.alwaysShow ? 'flag-dot--ok' : 'flag-dot--off'">
                      {{ scope.row.alwaysShow ? $t('routeManager.yes') : $t('routeManager.no') }}
                    </span>
                  </template>
                </el-table-column>
                <el-table-column :label="$t('common.operation')" width="210" align="right">
                  <template slot-scope="scope">
                    <button class="op-btn" @click="handleEditRoute(scope.row)">
                      <i class="fas fa-pen" /> {{ $t('routeManager.editRoute') }}
                    </button>
                    <button class="op-btn" @click="handleAddChildRoute(scope.row)">
                      <i class="fas fa-plus" /> {{ $t('routeManager.addChildRoute') }}
                    </button>
                    <button class="op-btn op-btn--danger" @click="handleDeleteRoute(scope.row)">
                      <i class="fas fa-trash-alt" />
                    </button>
                  </template>
                </el-table-column>

                <template slot="empty">
                  <div class="table-empty">
                    <i class="fas fa-map-signs" />
                    <p>{{ $t('routeManager.noRoutes') }}</p>
                  </div>
                </template>
              </el-table>
            </div>
          </template>

          <div v-else class="pane-placeholder">
            <div class="placeholder-orb">
              <i class="fas fa-route" />
            </div>
            <p>{{ $t('routeManager.selectGroup') }}</p>
          </div>
        </section>
      </div>
    </div>

    <!-- Route edit dialog -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="640px"
      custom-class="route-dialog"
      @close="handleDialogClose"
    >
      <el-form ref="routeForm" :model="routeForm" :rules="routeRules" label-width="104px">
        <el-form-item :label="$t('routeManager.path')" prop="path">
          <el-input v-model="routeForm.path" :placeholder="$t('routeManager.inputPath')" />
        </el-form-item>
        <el-form-item :label="$t('routeManager.component')" prop="component">
          <el-select
            v-model="routeForm.component"
            filterable
            allow-create
            default-first-option
            :placeholder="$t('routeManager.inputComponent')"
            style="width: 100%"
          >
            <el-option
              v-for="item in viewComponents"
              :key="item"
              :label="item"
              :value="item"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('routeManager.name')" prop="name">
          <el-input v-model="routeForm.name" :placeholder="$t('routeManager.inputName')" />
        </el-form-item>
        <el-form-item :label="$t('routeManager.redirect')" prop="redirect">
          <el-input v-model="routeForm.redirect" :placeholder="$t('routeManager.inputRedirect')" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12">
            <el-form-item :label="$t('routeManager.hidden')">
              <el-switch v-model="routeForm.hidden" />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12">
            <el-form-item :label="$t('routeManager.alwaysShow')">
              <el-switch v-model="routeForm.alwaysShow" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">{{ $t('routeManager.metaConfig') }}</el-divider>

        <el-form-item label="title" prop="meta.title">
          <el-input v-model="routeForm.meta.title" :placeholder="$t('routeManager.inputTitle')" />
        </el-form-item>
        <el-form-item :label="$t('routeManager.icon')" prop="meta.icon">
          <el-input v-model="routeForm.meta.icon" :placeholder="$t('routeManager.inputIcon')">
            <template v-if="routeForm.meta.icon" slot="append">
              <i :class="routeForm.meta.icon" />
            </template>
          </el-input>
        </el-form-item>
        <el-form-item :label="$t('routeManager.roles')">
          <el-select
            ref="roleSelect"
            v-model="routeForm.meta.roles"
            multiple
            filterable
            default-first-option
            :placeholder="$t('routeManager.selectRoles')"
            style="width: 100%"
          >
            <el-option
              v-for="item in availableRoles"
              :key="item.role"
              :label="item.name"
              :value="item.role"
            />
            <!-- 下拉底部常驻入口：非 el-option，不受 filterable 过滤影响。
                 权限编码一律走内联新增落库，不再允许手输，避免写入 system_role 中不存在的脏编码 -->
            <div class="roles-add-entry" @click.stop.prevent="openAddRoleDialog">
              <i class="el-icon-plus" />
              {{ $t('routeManager.addMenuRole') }}
            </div>
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :xs="24" :sm="12">
            <el-form-item :label="$t('routeManager.breadcrumb')">
              <el-switch v-model="routeForm.meta.breadcrumb" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item :label="$t('routeManager.activeMenu')" prop="meta.activeMenu">
          <el-input v-model="routeForm.meta.activeMenu" :placeholder="$t('routeManager.inputActiveMenu')" />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleDialogConfirm">{{ $t('common.confirm') }}</el-button>
      </span>
    </el-dialog>

    <!-- 内联新增菜单权限对话框：编码/名称/描述三项，类型固定菜单权限、创建后默认启用 -->
    <el-dialog
      :title="$t('routeManager.addMenuRoleTitle')"
      :visible.sync="roleDialogVisible"
      width="480px"
      custom-class="route-dialog"
      :close-on-click-modal="false"
      append-to-body
    >
      <el-alert
        :title="$t('routeManager.menuRoleTip')"
        type="info"
        :closable="false"
        show-icon
        style="margin-bottom: 16px"
      />
      <el-form ref="roleForm" :model="roleForm" :rules="roleRules" label-width="96px">
        <el-form-item :label="$t('role.permissionCode')" prop="role">
          <el-input v-model="roleForm.role" maxlength="128" :placeholder="$t('routeManager.inputRoleCode')" />
        </el-form-item>
        <el-form-item :label="$t('role.name')" prop="name">
          <el-input v-model="roleForm.name" maxlength="128" :placeholder="$t('routeManager.inputRoleName')" />
        </el-form-item>
        <el-form-item :label="$t('role.description')">
          <el-input
            v-model="roleForm.description"
            type="textarea"
            :rows="2"
            maxlength="500"
            show-word-limit
            :placeholder="$t('routeManager.inputRoleDesc')"
          />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="roleDialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="roleSaving" @click="handleAddRoleConfirm">{{ $t('common.confirm') }}</el-button>
      </span>
    </el-dialog>

    <!-- Save description dialog -->
    <el-dialog
      :title="$t('routeManager.saveConfigTitle')"
      :visible.sync="saveDialogVisible"
      width="480px"
      custom-class="route-dialog"
    >
      <el-form label-width="96px">
        <el-form-item :label="$t('routeManager.routeGroup')">
          <span class="mono">{{ activeGroup }}</span>
        </el-form-item>
        <el-form-item :label="$t('routeManager.versionNote')">
          <el-input
            v-model="saveDescription"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            :placeholder="$t('routeManager.versionNotePlaceholder')"
          />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="saveDialogVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" :loading="saving" @click="handleSaveConfirm">{{ $t('common.confirm') }}</el-button>
      </span>
    </el-dialog>

    <!-- Version history drawer -->
    <el-drawer
      :title="versionDrawerTitle"
      :visible.sync="versionDrawerVisible"
      direction="rtl"
      size="460px"
    >
      <div v-loading="loadingVersions" class="drawer-body">
        <el-timeline v-if="versionList.length">
          <el-timeline-item
            v-for="(v, idx) in versionList"
            :key="v.id"
            :type="idx === 0 ? 'primary' : ''"
            :timestamp="formatTime(v.createTime)"
            placement="top"
          >
            <div class="version-card" :class="{ 'version-card--current': idx === 0 }">
              <div class="version-card__head">
                <span class="version-no">v{{ v.versionNo }}</span>
                <el-tag v-if="idx === 0" size="mini" type="success" effect="dark">
                  {{ $t('routeManager.currentVersion') }}
                </el-tag>
                <el-button
                  v-else
                  size="mini"
                  type="text"
                  class="rollback-btn"
                  :loading="rollingBackId === v.id"
                  @click="handleRollback(v)"
                >
                  <i v-if="rollingBackId !== v.id" class="fas fa-history" />
                  {{ $t('routeManager.rollbackTo') }}
                </el-button>
              </div>
              <p v-if="v.description" class="version-desc">{{ v.description }}</p>
              <p v-else class="version-desc dim">{{ $t('routeManager.noDescription') }}</p>
            </div>
          </el-timeline-item>
        </el-timeline>
        <div v-else class="drawer-empty">
          <i class="fas fa-inbox" />
          <p>{{ $t('routeManager.noVersions') }}</p>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import {
  getRouteGroups,
  getRouteConfigsByGroup,
  saveRouteConfigs,
  deleteRouteConfigs,
  refreshRouteCache,
  getRouteVersions,
  rollbackRouteVersion
} from '@/api/view'
import { queryRoles, saveRole, activateRole } from '@/api/role'

export default {
  name: 'RouteManager',
  data() {
    return {
      ready: false,
      routeGroups: [],
      activeGroup: '',
      currentRoutes: [],
      loadingRoutes: false,
      saving: false,
      refreshing: false,
      dirty: false,
      // 版本号：路由树结构变化时递增，强制 el-table 重建（解决树表格重排不刷新问题）
      tableVersion: 0,
      dialogVisible: false,
      dialogType: 'add', // add, edit, addChild
      editingRoute: null,
      routeForm: this.createEmptyForm(),
      routeRules: {
        path: [{ required: true, message: this.$t('routeManager.inputPath'), trigger: 'blur' }],
        component: [{ required: true, message: this.$t('routeManager.inputComponent'), trigger: 'change' }]
      },
      availableRoles: [],
      viewComponents: [],
      // 内联新增菜单权限
      roleDialogVisible: false,
      roleSaving: false,
      roleForm: this.createEmptyRoleForm(),
      // 编码格式对齐库中既有约定（MENU.ADMIN / MENU.ROUTE 等大写的点分编码），长度上限同 DB 字段 128
      roleRules: {
        role: [
          { required: true, message: this.$t('routeManager.roleCodeRequired'), trigger: 'blur' },
          { pattern: /^[A-Z][A-Z0-9_.-]{1,127}$/, message: this.$t('routeManager.roleCodePattern'), trigger: 'blur' }
        ],
        name: [{ required: true, message: this.$t('routeManager.roleNameRequired'), trigger: 'blur' }]
      },
      // 保存说明对话框
      saveDialogVisible: false,
      saveDescription: '',
      // 版本历史抽屉
      versionDrawerVisible: false,
      loadingVersions: false,
      versionList: [],
      rollingBackId: null
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
    },
    routeCount() {
      return this.countRoutes(this.currentRoutes)
    },
    versionDrawerTitle() {
      return this.activeGroup
        ? `${this.$t('routeManager.versionHistory')} · ${this.activeGroup}`
        : this.$t('routeManager.versionHistory')
    }
  },
  created() {
    this.loadRouteGroups()
    this.loadViewComponents()
    this.loadMenuRoles()
  },
  mounted() {
    setTimeout(() => { this.ready = true }, 60)
  },
  methods: {
    createEmptyForm() {
      return {
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

    createEmptyRoleForm() {
      return {
        role: '',
        name: '',
        description: ''
      }
    },

    countRoutes(routes) {
      return routes.reduce((acc, r) => {
        return acc + 1 + (r.children && r.children.length ? this.countRoutes(r.children) : 0)
      }, 0)
    },

    async loadRouteGroups() {
      try {
        const response = await getRouteGroups()
        this.routeGroups = response.data || []
        // 自动选中第一个分组
        if (this.routeGroups.length > 0 && !this.activeGroup) {
          await this.handleGroupSelect(this.routeGroups[0])
        }
      } catch (error) {
        console.error('获取路由分组失败:', error)
        this.$message.error(this.$t('routeManager.fetchGroupsFailed'))
      }
    },

    loadViewComponents() {
      // 通过 require.context 读取 views 目录下所有 .vue 文件
      const requireContext = require.context('@/views', true, /\.vue$/)
      const components = requireContext.keys()
        .map(key => '/' + key.replace(/^\.\//, ''))
        .filter(path => !path.includes('403') && !path.includes('404') && !path.includes('login'))
        .sort()
      // Layout 组件选项置顶
      components.unshift('Layout')
      this.viewComponents = components
    },

    async loadMenuRoles() {
      try {
        // 查询所有菜单类型的权限（type=1），状态为激活（flag=1）
        const response = await queryRoles({ typeEquals: [1], flagEquals: [1] }, 1, 1000)
        const roles = response.data?.data || []
        this.availableRoles = roles.map(r => ({
          role: r.role,
          name: r.name
        }))
      } catch (error) {
        console.error('获取菜单权限失败:', error)
      }
    },

    // 打开内联新增弹窗；先收起下拉浮层，避免其残留并压在二级弹窗之上
    openAddRoleDialog() {
      this.$refs.roleSelect?.blur()
      this.roleForm = this.createEmptyRoleForm()
      this.roleDialogVisible = true
      this.$nextTick(() => {
        this.$refs.roleForm?.clearValidate()
      })
    },

    async handleAddRoleConfirm() {
      try {
        await this.$refs.roleForm.validate()
      } catch (e) {
        return
      }
      const code = this.roleForm.role.trim()
      const name = this.roleForm.name.trim()
      this.roleSaving = true
      try {
        // 判重用 roleIn（IN 精确匹配）：roleEquals 虽名为"精确"，实际映射 ContrastEnum.LLIKE 左模糊，
        // 传 MENU.A 会命中 MENU.ADMIN，据此判重会误报编码被占用
        const response = await queryRoles({ roleIn: [code] }, 1, 10)
        const matched = response.data?.data || []
        // 同编码可能同时存在启用与禁用两条记录（服务层判重只比对启用态），优先取启用中的那条
        const existed = matched.find(item => item.flag === 1) || matched[0]
        if (existed) {
          await this.handleExistedRole(existed)
        } else {
          // 类型固定 1（菜单权限）；不传 flag/dataScope：新增由服务层强制置为激活，数据范围对菜单无意义
          await saveRole({ role: code, name, type: 1, description: this.roleForm.description })
          await this.loadMenuRoles()
          this.selectRoleCode(code, name)
          this.roleDialogVisible = false
          this.$message.success(this.$t('routeManager.roleAddSuccess', { name }))
        }
      } catch (error) {
        // 失败原因（如编码重复）由响应拦截器统一提示；此处不重复弹错，且保留弹窗与已填内容供修正
        console.error('新增菜单权限失败:', error)
      } finally {
        this.roleSaving = false
      }
    },

    // 编码已存在的分支处理：启用中的菜单权限直接选中，禁用中的菜单权限确认后启用，
    // 非菜单类型与系统内置权限一律拒绝，不擅自改动他人权限
    handleExistedRole(existed) {
      const code = existed.role
      if (existed.flag === 2) {
        this.$message.error(this.$t('routeManager.roleCodeSystem', { code }))
        return
      }
      if (existed.type !== 1) {
        this.$message.error(this.$t('routeManager.roleCodeOccupied', { code }))
        return
      }
      if (existed.flag === 1) {
        this.selectRoleCode(code, existed.name)
        this.roleDialogVisible = false
        this.$message.warning(this.$t('routeManager.roleCodeExists', { code }))
        return
      }
      // flag=0 禁用态：确认后才启用并选中，取消则弹窗与填写内容保持原样
      this.$confirm(this.$t('routeManager.enableRoleConfirm', { code }), this.$t('common.tip'), {
        confirmButtonText: this.$t('common.confirm'),
        cancelButtonText: this.$t('common.cancel'),
        type: 'warning'
      }).then(async() => {
        await activateRole(existed.id)
        await this.loadMenuRoles()
        this.selectRoleCode(code, existed.name)
        this.roleDialogVisible = false
        this.$message.success(this.$t('routeManager.roleEnableSuccess', { name: existed.name }))
      }).catch(() => {})
    },

    // 选中指定权限编码，并在下拉选项中兜底补齐（超出加载上限时仍能显示名称）
    selectRoleCode(code, name) {
      if (!this.availableRoles.some(item => item.role === code)) {
        this.availableRoles.push({ role: code, name: name || code })
      }
      if (!this.routeForm.meta.roles.includes(code)) {
        this.routeForm.meta.roles.push(code)
      }
    },

    async handleGroupSelect(group) {
      if (group === this.activeGroup) return
      // 有未保存更改时先确认是否放弃
      if (this.dirty) {
        try {
          await this.$confirm(this.$t('routeManager.discardChangesConfirm'), this.$t('common.tip'), {
            confirmButtonText: this.$t('common.confirm'),
            cancelButtonText: this.$t('common.cancel'),
            type: 'warning'
          })
        } catch (e) {
          return
        }
      }
      this.activeGroup = group
      await this.loadRouteConfigs(group)
    },

    async loadRouteConfigs(group) {
      this.loadingRoutes = true
      try {
        const response = await getRouteConfigsByGroup(group)
        const routes = response.data || []
        // 兼容后端返回无 sortOrder 的旧数据
        this.normalizeSortOrder(routes)
        // 后端已按 sortOrder 排序，前端再兜底排序保证数组顺序与序号一致
        this.sortByOrder(routes)
        this.currentRoutes = routes
        this.dirty = false
        this.tableVersion++
      } catch (error) {
        console.error('获取路由配置失败:', error)
        this.$message.error(this.$t('routeManager.fetchRoutesFailed'))
      } finally {
        this.loadingRoutes = false
      }
    },

    // 递归为无 sortOrder 的路由按当前顺序初始化序号
    normalizeSortOrder(routes) {
      routes.forEach((route, idx) => {
        if (route.sortOrder === null || route.sortOrder === undefined) {
          route.sortOrder = idx
        }
        if (route.children && route.children.length > 0) {
          this.normalizeSortOrder(route.children)
        }
      })
    },

    // 递归按 sortOrder 排序
    sortByOrder(routes) {
      routes.sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
      routes.forEach(route => {
        if (route.children && route.children.length > 0) {
          this.sortByOrder(route.children)
        }
      })
    },

    // 递归按数组顺序重新分配 sortOrder（保存前调用）
    reassignSortOrder(routes) {
      routes.forEach((route, idx) => {
        route.sortOrder = idx
        if (route.children && route.children.length > 0) {
          this.reassignSortOrder(route.children)
        }
      })
    },

    // 在树中查找包含目标路由的数组（顶层返回 currentRoutes）
    findContainer(routes, route) {
      for (const r of routes) {
        if (r === route) return routes
        if (r.children && r.children.length) {
          const found = this.findContainer(r.children, route)
          if (found) return found
        }
      }
      return null
    },

    isFirst(route) {
      const container = this.findContainer(this.currentRoutes, route)
      return !container || container.indexOf(route) <= 0
    },

    isLast(route) {
      const container = this.findContainer(this.currentRoutes, route)
      return !container || container.indexOf(route) >= container.length - 1
    },

    // 上移 / 下移（direction: -1 上移, 1 下移），仅修改本地状态并标记未保存，点“保存配置”后才落库并产生新版本
    handleMove(route, direction) {
      const container = this.findContainer(this.currentRoutes, route)
      if (!container) return
      const index = container.indexOf(route)
      const target = index + direction
      if (target < 0 || target >= container.length) return

      container.splice(index, 1)
      container.splice(target, 0, route)
      this.reassignSortOrder(this.currentRoutes)
      // 强制树表格重建，确保新顺序立即可见
      this.tableVersion++
      this.dirty = true
    },

    // 打开保存说明对话框，确认后再真正保存
    handleSaveRoutes() {
      if (this.currentRoutes.length === 0) {
        this.$message.warning(this.$t('routeManager.emptyGroupTip'))
        return
      }
      this.reassignSortOrder(this.currentRoutes)
      this.saveDescription = ''
      this.saveDialogVisible = true
    },

    async handleSaveConfirm() {
      this.saving = true
      try {
        const response = await saveRouteConfigs(this.activeGroup, this.currentRoutes, this.saveDescription)
        this.dirty = false
        this.saveDialogVisible = false
        const version = response && response.data ? response.data : null
        if (version && version.versionNo) {
          this.$message.success(this.$t('routeManager.saveAsSuccess', { version: version.versionNo }))
        } else {
          this.$message.success(this.$t('routeManager.routeSaveSuccess'))
        }
        // 若版本抽屉已打开，刷新版本列表
        if (this.versionDrawerVisible) {
          this.loadVersionHistory()
        }
      } catch (error) {
        console.error('保存路由配置失败:', error)
        this.$message.error(this.$t('routeManager.saveRoutesFailed'))
      } finally {
        this.saving = false
      }
    },

    // ── 版本历史 ──
    async handleOpenVersions() {
      if (!this.activeGroup) return
      this.versionDrawerVisible = true
      await this.loadVersionHistory()
    },

    async loadVersionHistory() {
      this.loadingVersions = true
      try {
        const response = await getRouteVersions(this.activeGroup)
        this.versionList = response.data || []
      } catch (error) {
        console.error('获取版本历史失败:', error)
        this.$message.error(this.$t('routeManager.fetchVersionsFailed'))
      } finally {
        this.loadingVersions = false
      }
    },

    handleRollback(version) {
      this.$confirm(
        this.$t('routeManager.rollbackConfirm', { version: version.versionNo }),
        this.$t('common.tip'),
        {
          confirmButtonText: this.$t('common.confirm'),
          cancelButtonText: this.$t('common.cancel'),
          type: 'warning'
        }
      ).then(async() => {
        this.rollingBackId = version.id
        try {
          const response = await rollbackRouteVersion(this.activeGroup, version.id)
          const newVersion = response && response.data ? response.data : null
          this.$message.success(this.$t('routeManager.rollbackSuccess', {
            version: newVersion && newVersion.versionNo ? newVersion.versionNo : ''
          }))
          await Promise.all([this.loadVersionHistory(), this.loadRouteConfigs(this.activeGroup)])
        } catch (error) {
          console.error('回滚失败:', error)
          this.$message.error(this.$t('routeManager.rollbackFailed'))
        } finally {
          this.rollingBackId = null
        }
      }).catch(() => {})
    },

    formatTime(time) {
      if (!time) return ''
      const d = new Date(time)
      if (isNaN(d.getTime())) return time
      const pad = n => String(n).padStart(2, '0')
      return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`
    },

    async handleRefresh() {
      this.refreshing = true
      try {
        await refreshRouteCache()
        this.$message.success(this.$t('routeManager.cacheRefreshSuccess'))
      } catch (error) {
        console.error('刷新路由缓存失败:', error)
        this.$message.error(this.$t('routeManager.refreshCacheFailed'))
      } finally {
        this.refreshing = false
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
          this.handleGroupSelect(value)
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
      }).then(async() => {
        try {
          await deleteRouteConfigs(group)
          this.routeGroups = this.routeGroups.filter(g => g !== group)
          if (this.activeGroup === group) {
            this.activeGroup = ''
            this.currentRoutes = []
            // 自动切换到第一个剩余分组
            if (this.routeGroups.length > 0) {
              await this.handleGroupSelect(this.routeGroups[0])
            }
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
      this.routeForm = this.createEmptyForm()
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
      this.routeForm = this.createEmptyForm()
      this.dialogVisible = true
    },

    handleDeleteRoute(route) {
      this.$confirm(this.$t('routeManager.deleteRouteConfirm', { path: route.path }), this.$t('common.tip'), {
        confirmButtonText: this.$t('common.confirm'),
        cancelButtonText: this.$t('common.cancel'),
        type: 'warning'
      }).then(() => {
        this.removeRouteFromTree(this.currentRoutes, route)
        this.reassignSortOrder(this.currentRoutes)
        this.tableVersion++
        this.dirty = true
      }).catch(() => {})
    },

    removeRouteFromTree(routes, targetRoute) {
      for (let i = 0; i < routes.length; i++) {
        if (routes[i] === targetRoute) {
          routes.splice(i, 1)
          return true
        }
        if (routes[i].children && this.removeRouteFromTree(routes[i].children, targetRoute)) {
          return true
        }
      }
      return false
    },

    handleDialogConfirm() {
      this.$refs.routeForm.validate(valid => {
        if (!valid) return
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
          routeData.sortOrder = this.currentRoutes.length // 新路由排在最后
          this.currentRoutes.push(routeData)
        } else if (this.dialogType === 'edit') {
          routeData.sortOrder = this.editingRoute.sortOrder
          routeData.children = this.editingRoute.children
          Object.assign(this.editingRoute, routeData)
        } else if (this.dialogType === 'addChild') {
          if (!this.editingRoute.children) {
            this.$set(this.editingRoute, 'children', [])
          }
          routeData.sortOrder = this.editingRoute.children.length
          this.editingRoute.children.push(routeData)
        }

        this.reassignSortOrder(this.currentRoutes)
        this.tableVersion++
        this.dialogVisible = false
        this.dirty = true
      })
    },

    handleDialogClose() {
      this.$refs.routeForm?.resetFields()
    }
  }
}
</script>

<style lang="scss" scoped>
@import url('https://fonts.googleapis.com/css2?family=DM+Sans:ital,opsz,wght@0,9..40,300;0,9..40,400;0,9..40,500;0,9..40,600;0,9..40,700;1,9..40,400&family=Outfit:wght@300;400;500;600;700;800&display=swap');
@import "~@/styles/variables.scss";

/* ── Palette ── */
$bg: #f5f5f7;
$surface: rgba(255, 255, 255, 0.72);
$surface-h: rgba(255, 255, 255, 0.88);
$border: rgba(0, 0, 0, 0.06);
$border-h: rgba(0, 0, 0, 0.1);
$primary: #1E40AF;
$primary-l: #3B82F6;
$primary-bg: rgba(30, 64, 175, 0.06);
$amber: #F97316;
$green: #10B981;
$red: #EF4444;
$text-1: #1a1d23;
$text-2: #5a6072;
$text-3: #8e95a9;
$radius: 18px;
$radius-sm: 12px;
$radius-xs: 8px;
$ease: cubic-bezier(0.22, 1, 0.36, 1);
$font-display: 'Outfit', -apple-system, BlinkMacSystemFont, sans-serif;
$font-body: 'DM Sans', -apple-system, BlinkMacSystemFont, 'PingFang SC', sans-serif;
$font-mono: 'SF Mono', 'Fira Code', 'JetBrains Mono', Consolas, monospace;

.route-studio {
  position: relative;
  min-height: calc(100vh - 44px);
  background: $bg;
  font-family: $font-body;
  color: $text-1;
  overflow: hidden;
}

/* ── Atmospheric Background ── */
.bg {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
}

.bg__orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.5;
  will-change: transform;
}

.bg__orb--1 {
  width: 600px;
  height: 600px;
  top: -12%;
  left: -8%;
  background: radial-gradient(circle, rgba(30, 64, 175, 0.12) 0%, transparent 70%);
  animation: orb-drift 20s ease-in-out infinite;
}

.bg__orb--2 {
  width: 500px;
  height: 500px;
  top: 20%;
  right: -10%;
  background: radial-gradient(circle, rgba(249, 115, 22, 0.10) 0%, transparent 70%);
  animation: orb-drift 25s ease-in-out infinite reverse;
}

.bg__orb--3 {
  width: 400px;
  height: 400px;
  bottom: -5%;
  left: 30%;
  background: radial-gradient(circle, rgba(16, 185, 129, 0.10) 0%, transparent 70%);
  animation: orb-drift 22s ease-in-out infinite 3s;
}

.bg__grain {
  position: absolute;
  inset: 0;
  opacity: 0.35;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)' opacity='0.04'/%3E%3C/svg%3E");
  background-size: 200px 200px;
}

@keyframes orb-drift {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(30px, -20px) scale(1.05); }
  66% { transform: translate(-20px, 25px) scale(0.97); }
}

/* ── Layout ── */
.studio-inner {
  position: relative;
  z-index: 1;
  padding: 20px 24px 24px;
  display: flex;
  flex-direction: column;
  gap: 18px;
  min-height: calc(100vh - 44px);
  box-sizing: border-box;
}

/* ── Header ── */
.studio-header {
  opacity: 0;
  transform: translateY(-20px);
  transition: all 0.6s $ease;

  &._on {
    opacity: 1;
    transform: translateY(0);
  }
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 22px 26px;
  background: $surface;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: $radius;
  border: 1px solid $border;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.02), 0 4px 16px rgba(0, 0, 0, 0.02);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon {
  width: 54px;
  height: 54px;
  border-radius: 16px;
  background: linear-gradient(135deg, $primary 0%, #1E3A8A 60%, #06B6D4 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 22px;
  box-shadow: 0 8px 32px rgba(30, 64, 175, 0.25);
}

.header-title {
  font-family: $font-display;
  font-size: 26px;
  font-weight: 800;
  margin: 0 0 3px 0;
  letter-spacing: -0.5px;
  background: linear-gradient(135deg, $text-1 0%, #374151 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.header-subtitle {
  font-size: 13px;
  color: $text-3;
  margin: 0;
  letter-spacing: 0.2px;
}

.ghost-btn {
  background: $surface-h;
  border: 1px solid $border-h;
  border-radius: $radius-sm;
  padding: 10px 20px;
  font-weight: 600;
  color: $text-2;
  transition: all 0.3s $ease;

  &:hover {
    color: $primary;
    border-color: rgba(30, 64, 175, 0.3);
    transform: translateY(-1px);
    box-shadow: 0 4px 14px rgba(30, 64, 175, 0.12);
  }

  i {
    margin-right: 7px;
  }
}

/* ── Workspace ── */
.workspace {
  display: grid;
  grid-template-columns: 240px 1fr;
  gap: 18px;
  flex: 1;
  min-height: 0;
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.6s $ease 0.15s;

  &._on {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ── Group Rail ── */
.group-rail {
  display: flex;
  flex-direction: column;
  background: $surface;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: $radius;
  border: 1px solid $border;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.02), 0 4px 16px rgba(0, 0, 0, 0.02);
  overflow: hidden;
}

.rail-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 16px 12px;
  border-bottom: 1px solid $border;
}

.rail-title {
  font-family: $font-display;
  font-size: 14px;
  font-weight: 700;
  color: $text-2;
  letter-spacing: 0.4px;

  i {
    margin-right: 7px;
    color: $primary;
  }
}

.rail-add {
  width: 28px;
  height: 28px;
  border-radius: 9px;
  border: 1px dashed rgba(30, 64, 175, 0.35);
  background: $primary-bg;
  color: $primary;
  cursor: pointer;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.25s $ease;

  &:hover {
    background: $primary;
    border-style: solid;
    color: #fff;
    transform: scale(1.06);
  }
}

.rail-list {
  flex: 1;
  overflow-y: auto;
  padding: 10px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.rail-item {
  position: relative;
  display: flex;
  align-items: center;
  gap: 9px;
  padding: 10px 12px;
  border-radius: $radius-sm;
  cursor: pointer;
  border: 1px solid transparent;
  transition: all 0.25s $ease;

  &:hover {
    background: rgba(0, 0, 0, 0.03);

    .rail-item__del {
      opacity: 1;
    }
  }

  &._active {
    background: linear-gradient(135deg, rgba(30, 64, 175, 0.10) 0%, rgba(59, 130, 246, 0.07) 100%);
    border-color: rgba(30, 64, 175, 0.18);

    .rail-item__dot {
      background: $primary-l;
      box-shadow: 0 0 0 4px rgba(59, 130, 246, 0.15);
    }

    .rail-item__name {
      color: $primary;
      font-weight: 700;
    }
  }
}

.rail-item__dot {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #c9cedb;
  flex-shrink: 0;
  transition: all 0.25s $ease;
}

.rail-item__name {
  flex: 1;
  font-size: 13.5px;
  color: $text-2;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.rail-item__spin {
  color: $primary;
  font-size: 12px;
}

.rail-item__del {
  opacity: 0;
  border: none;
  background: none;
  color: $text-3;
  cursor: pointer;
  font-size: 12px;
  padding: 3px;
  border-radius: 6px;
  transition: all 0.2s $ease;
  flex-shrink: 0;

  &:hover {
    color: $red;
    background: rgba(239, 68, 68, 0.1);
  }
}

.rail-empty {
  padding: 36px 12px;
  text-align: center;
  color: $text-3;
  font-size: 13px;

  i {
    font-size: 30px;
    margin-bottom: 10px;
    opacity: 0.5;
  }

  p {
    margin: 0;
  }
}

/* ── Route Pane ── */
.route-pane {
  display: flex;
  flex-direction: column;
  background: $surface;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: $radius;
  border: 1px solid $border;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.02), 0 4px 16px rgba(0, 0, 0, 0.02);
  overflow: hidden;
  min-width: 0;
}

.pane-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 18px;
  border-bottom: 1px solid $border;
  flex-wrap: wrap;
  gap: 10px;
}

.pane-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.pane-group-chip {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 6px 14px;
  border-radius: 999px;
  background: linear-gradient(135deg, rgba(30, 64, 175, 0.10), rgba(59, 130, 246, 0.07));
  border: 1px solid rgba(30, 64, 175, 0.16);
  color: $primary;
  font-family: $font-display;
  font-weight: 700;
  font-size: 13px;
}

.pane-count {
  font-size: 12.5px;
  color: $text-3;
}

.pane-dirty {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  font-size: 12px;
  color: $amber;
  font-weight: 600;
}

.pane-actions {
  display: flex;
  gap: 8px;

  ::v-deep .el-button--primary {
    background: linear-gradient(135deg, $primary 0%, #1E3A8A 100%);
    border: none;
    box-shadow: 0 3px 12px rgba(30, 64, 175, 0.28);

    &:hover {
      box-shadow: 0 5px 18px rgba(30, 64, 175, 0.38);
      transform: translateY(-1px);
    }
  }
}

.pane-table {
  flex: 1;
  overflow: auto;
  padding: 6px 10px 14px;

  ::v-deep .el-table {
    background: transparent;

    &::before {
      background-color: $border;
    }

    th.el-table__cell,
    td.el-table__cell {
      border-bottom: 1px solid rgba(0, 0, 0, 0.045);
    }

    tr {
      background: transparent;
    }

    tbody tr:hover > td {
      background: rgba(30, 64, 175, 0.04) !important;
    }

    .el-table__row--striped td {
      background: transparent;
    }

    .el-table__expand-icon {
      color: $text-3;
    }
  }
}

.mono {
  font-family: $font-mono;
  font-size: 12.5px;
}

.path-cell {
  color: $text-1;
  font-weight: 600;
}

.comp-cell {
  color: $text-2;
}

.name-cell {
  font-size: 13px;
  color: $text-2;
}

.redirect-cell {
  color: $text-3;

  i {
    font-size: 10px;
    margin-right: 3px;
  }
}

.dim {
  color: #c9cedb;
}

.layout-tag {
  color: $primary;
  border-color: rgba(30, 64, 175, 0.25);
  background: $primary-bg;

  i {
    margin-right: 3px;
  }
}

.flag-dot {
  display: inline-block;
  padding: 2px 9px;
  border-radius: 999px;
  font-size: 11.5px;
  font-weight: 600;

  &--ok {
    color: $green;
    background: rgba(16, 185, 129, 0.1);
  }

  &--warn {
    color: $amber;
    background: rgba(249, 115, 22, 0.1);
  }

  &--off {
    color: $text-3;
    background: rgba(0, 0, 0, 0.04);
  }
}

/* ── Sort Cell ── */
.sort-cell {
  display: inline-flex;
  align-items: center;
  gap: 2px;
  background: rgba(0, 0, 0, 0.035);
  border-radius: 8px;
  padding: 2px;
}

.sort-btn {
  border: none;
  background: transparent;
  cursor: pointer;
  color: $text-2;
  width: 24px;
  height: 24px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  transition: all 0.2s $ease;

  &:hover:not(:disabled) {
    background: #fff;
    color: $primary;
    box-shadow: 0 2px 6px rgba(30, 64, 175, 0.18);
  }

  &:disabled {
    opacity: 0.3;
    cursor: not-allowed;
  }
}

/* ── Operation Buttons ── */
.op-btn {
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: 12.5px;
  color: $primary;
  padding: 5px 8px;
  border-radius: 7px;
  transition: all 0.2s $ease;
  font-family: $font-body;

  &:hover {
    background: $primary-bg;
  }

  i {
    margin-right: 4px;
    font-size: 11px;
  }

  &--danger {
    color: $red;

    &:hover {
      background: rgba(239, 68, 68, 0.08);
    }
  }
}

/* ── Empty / Placeholder ── */
.table-empty {
  padding: 44px 0;
  text-align: center;
  color: $text-3;
  font-size: 13px;

  i {
    font-size: 34px;
    opacity: 0.45;
    margin-bottom: 10px;
  }

  p {
    margin: 0;
  }
}

.pane-placeholder {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  color: $text-3;
  font-size: 13.5px;
  min-height: 320px;

  p {
    margin: 0;
  }
}

.placeholder-orb {
  width: 84px;
  height: 84px;
  border-radius: 26px;
  background: linear-gradient(135deg, rgba(30, 64, 175, 0.09), rgba(6, 182, 212, 0.08));
  border: 1px solid rgba(30, 64, 175, 0.14);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 30px;
  color: $primary-l;
  animation: float-y 3.4s ease-in-out infinite;
}

@keyframes float-y {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-9px); }
}

/* ── Transitions ── */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s $ease;
}

.fade-enter,
.fade-leave-to {
  opacity: 0;
}

/* ── Version history drawer ── */
.drawer-body {
  padding: 4px 24px 24px;
  min-height: 200px;
}

.version-card {
  background: $surface-h;
  border: 1px solid $border;
  border-radius: $radius-sm;
  padding: 12px 14px;
  transition: all 0.25s $ease;

  &:hover {
    border-color: rgba(30, 64, 175, 0.22);
    box-shadow: 0 4px 14px rgba(30, 64, 175, 0.08);
  }

  &--current {
    border-color: rgba(16, 185, 129, 0.35);
    background: rgba(16, 185, 129, 0.045);
  }
}

.version-card__head {
  display: flex;
  align-items: center;
  gap: 10px;
}

.version-no {
  font-family: $font-mono;
  font-weight: 700;
  font-size: 14px;
  color: $primary;
}

.rollback-btn {
  margin-left: auto;
  padding: 0;
  color: $text-2;
  font-weight: 600;

  &:hover {
    color: $amber;
  }
}

.version-desc {
  margin: 8px 0 0;
  font-size: 13px;
  color: $text-2;
  line-height: 1.55;
  word-break: break-all;

  &.dim {
    color: $text-3;
  }
}

.drawer-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 48px 0;
  color: $text-3;
  font-size: 26px;

  p {
    margin: 0;
    font-size: 13px;
  }
}

/* ── 权限角色下拉：内联新增入口 ── */
.roles-add-entry {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 9px 20px;
  border-top: 1px solid $border;
  color: $primary;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s $ease;

  &:hover {
    background: $primary-bg;
  }

  i {
    font-size: 13px;
  }
}

/* ── 窄屏适配：路由组侧栏转为横向 chip 条，内容区单列堆叠 ── */
@media only screen and (max-width: #{$bp-tablet}) {
  .studio-inner {
    padding: 12px;
    gap: 12px;
  }

  .header-content {
    flex-wrap: wrap;
    gap: 12px;
    padding: 16px 18px;
  }

  .header-title {
    font-size: 22px;
  }

  .workspace {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .group-rail {
    flex-direction: row;
    align-items: stretch;
  }

  .rail-head {
    flex: 0 0 auto;
    border-bottom: none;
    border-right: 1px solid $border;
    padding: 10px 12px;
  }

  /* 窄屏只保留图标，节省横向空间 */
  .rail-title {
    font-size: 0;

    i {
      margin-right: 0;
      font-size: 14px;
    }
  }

  .rail-list {
    flex-direction: row;
    overflow-x: auto;
    overflow-y: hidden;
    padding: 8px;
    gap: 8px;
  }

  .rail-item {
    flex: 0 0 auto;
    padding: 8px 12px;
  }

  .rail-item__name {
    flex: 0 1 auto;
    max-width: 140px;
  }

  /* 触摸设备常显删除入口，不依赖 hover */
  .rail-item__del {
    opacity: 1;
  }

  .pane-actions {
    flex-wrap: wrap;
    gap: 8px;
  }
}

@media only screen and (max-width: #{$bp-phone}) {
  .studio-inner {
    padding: 10px 10px 16px;
  }

  .header-icon {
    width: 44px;
    height: 44px;
    font-size: 18px;
  }

  .header-title {
    font-size: 20px;
  }

  .header-actions {
    width: 100%;
  }

  .rail-item__name {
    max-width: 110px;
  }
}
</style>

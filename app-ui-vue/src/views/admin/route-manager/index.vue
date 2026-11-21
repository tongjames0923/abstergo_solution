<template>
  <div class="route-studio">
    <!-- Atmospheric background -->
    <div class="bg" aria-hidden="true">
      <div class="bg__grid" />
      <div class="bg__orb bg__orb--1" />
      <div class="bg__orb bg__orb--2" />
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
          <div class="header-right">
            <div class="header-stats">
              <div class="stat">
                <span class="stat__num">{{ routeGroups.length }}</span>
                <span class="stat__label">{{ $t('routeManager.groupsUnit') }}</span>
              </div>
              <span class="stat__sep" />
              <div class="stat">
                <span class="stat__num">{{ routeCount }}</span>
                <span class="stat__label">{{ $t('routeManager.routesUnit') }}</span>
              </div>
            </div>
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
              <span class="rail-item__bar" />
              <span class="rail-item__name">{{ group }}</span>
              <span class="rail-item__count">{{ groupRouteCounts[group] == null ? '·' : groupRouteCounts[group] }}</span>
              <button class="rail-item__del" :title="$t('routeManager.deleteGroup')" @click.stop="handleDeleteGroup(group)">
                <i class="fas fa-trash-alt" />
              </button>
            </div>

            <div v-if="routeGroups.length === 0" class="rail-empty">
              <i class="fas fa-inbox" />
              <p>{{ $t('routeManager.noGroups') }}</p>
            </div>
          </div>

          <div class="rail-foot">
            <i class="fas fa-lightbulb" />
            <span>{{ $t('routeManager.railTip') }}</span>
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
                <span class="pane-stat"><b>{{ routeCount }}</b>{{ $t('routeManager.routesUnit') }}</span>
                <span class="pane-stat pane-stat--dim"><b>{{ topLevelCount }}</b>{{ $t('routeManager.topRoutesUnit') }}</span>
              </div>
              <div class="pane-actions">
                <el-input
                  v-model="searchKeyword"
                  class="pane-search"
                  size="small"
                  clearable
                  prefix-icon="el-icon-search"
                  :placeholder="$t('routeManager.searchPlaceholder')"
                />
                <el-tooltip :content="expandAll ? $t('routeManager.collapseAll') : $t('routeManager.expandAll')" placement="top">
                  <button class="icon-btn" @click="handleToggleExpandAll">
                    <i :class="expandAll ? 'el-icon-folder' : 'el-icon-folder-opened'" />
                  </button>
                </el-tooltip>
                <el-tooltip :content="$t('routeManager.versionHistory')" placement="top">
                  <button class="icon-btn" @click="handleOpenVersions">
                    <i class="fas fa-history" />
                  </button>
                </el-tooltip>
                <el-button type="primary" size="small" icon="el-icon-plus" @click="handleAddRoute">
                  {{ $t('routeManager.addRoute') }}
                </el-button>
              </div>
            </div>

            <div v-loading="loadingRoutes" class="pane-table">
              <el-table
                :key="tableVersion"
                :data="filteredRoutes"
                row-key="_rowKey"
                :tree-props="{ children: 'children', hasChildren: 'hasChildren' }"
                :default-expand-all="expandAll"
                :header-cell-style="{ background: 'rgba(30,64,175,0.045)', color: '#5a6072', fontWeight: 600 }"
                style="width: 100%"
              >
                <el-table-column prop="path" :label="$t('routeManager.path')" min-width="260">
                  <template slot-scope="scope">
                    <div class="route-path-cell">
                      <span class="level-pill" :class="'level-pill--' + (scope.row._level || 1)">
                        L{{ scope.row._level || 1 }}
                      </span>
                      <span class="mono path-text">{{ scope.row.path }}</span>
                      <span v-if="scope.row.component === 'Layout'" class="tag tag--layout">
                        <i class="fas fa-columns" />Layout
                      </span>
                      <span v-else-if="scope.row.children && scope.row.children.length" class="tag tag--parent">
                        <i class="fas fa-folder" />{{ $t('routeManager.parentRoute') }}
                        <b class="child-count">{{ scope.row.children.length }}</b>
                      </span>
                      <span v-if="scope.row.hidden" class="tag tag--hidden">{{ $t('routeManager.hidden') }}</span>
                    </div>
                    <div v-if="scope.row._level > 1 && scope.row._chain.length > 1" class="route-chain mono" :title="scope.row._chain.join(' / ')">
                      ↳ {{ scope.row._chain.slice(0, -1).join(' / ') }}
                    </div>
                    <div v-if="scope.row.meta && scope.row.meta.title" class="route-sub-cell">
                      {{ scope.row.meta.title }}
                    </div>
                    <div class="route-sub-cell mono">
                      <span v-if="scope.row.component && scope.row.component !== 'Layout'">{{ scope.row.component }}</span>
                      <span v-if="scope.row.meta && scope.row.meta.icon" class="sub-sep">{{ scope.row.meta.icon }}</span>
                      <span v-for="role in rolesOf(scope.row)" :key="role" class="role-chip">{{ role }}</span>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column :label="$t('routeManager.level')" width="86" align="center">
                  <template slot-scope="scope">
                    <span class="level-tree" :class="'level-tree--' + (scope.row._level || 1)">
                      <i v-for="n in (scope.row._level || 1)" :key="n" class="level-tree__dash" />
                    </span>
                  </template>
                </el-table-column>
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
                <el-table-column prop="name" :label="$t('routeManager.name')" min-width="130">
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
                <el-table-column :label="$t('routeManager.flags')" width="104" align="center">
                  <template slot-scope="scope">
                    <div class="inline-flags">
                      <el-tooltip :content="$t('routeManager.hidden')" placement="top">
                        <button
                          class="flag-toggle flag-toggle--warn"
                          :class="{ 'is-on': scope.row.hidden }"
                          @click="handleToggleFlag(scope.row, 'hidden')"
                        >
                          <i class="fas fa-eye-slash" />
                        </button>
                      </el-tooltip>
                      <el-tooltip :content="$t('routeManager.alwaysShow')" placement="top">
                        <button
                          class="flag-toggle"
                          :class="{ 'is-on': scope.row.alwaysShow }"
                          @click="handleToggleFlag(scope.row, 'alwaysShow')"
                        >
                          <i class="fas fa-stream" />
                        </button>
                      </el-tooltip>
                    </div>
                  </template>
                </el-table-column>
                <el-table-column :label="$t('common.operation')" width="200" align="right">
                  <template slot-scope="scope">
                    <div class="inline-ops">
                      <button class="op-icon" :title="$t('routeManager.editRoute')" @click="handleEditRoute(scope.row)">
                        <i class="fas fa-pen" />
                      </button>
                      <button class="op-icon" :title="$t('routeManager.addChildRoute')" @click="handleAddChildRoute(scope.row)">
                        <i class="fas fa-plus" />
                      </button>
                      <button class="op-icon" :title="$t('routeManager.moveNode')" @click="handleMoveNode(scope.row)">
                        <i class="fas fa-arrows-alt" />
                      </button>
                      <button class="op-icon" :title="$t('routeManager.copyNode')" @click="handleCopyNode(scope.row)">
                        <i class="fas fa-copy" />
                      </button>
                      <button class="op-icon op-icon--danger" :title="$t('common.delete')" @click="handleDeleteRoute(scope.row)">
                        <i class="fas fa-trash-alt" />
                      </button>
                    </div>
                  </template>
                </el-table-column>
                <template slot="empty">
                  <div class="table-empty">
                    <i :class="loadError ? 'fas fa-exclamation-triangle' : (searchKeyword ? 'fas fa-search' : 'fas fa-map-signs')" />
                    <p v-if="loadError">{{ $t('routeManager.loadRoutesFailedTip') }}</p>
                    <p v-else>{{ searchKeyword ? $t('routeManager.noSearchResult') : $t('routeManager.noRoutes') }}</p>
                    <div class="table-empty__hint">
                      <span class="mono">{{ activeGroup }}</span>
                      <el-button v-if="loadError || !searchKeyword" size="mini" icon="el-icon-refresh" @click="handleReloadCurrentGroup">
                        {{ $t('routeManager.reloadRoutes') }}
                      </el-button>
                      <el-button v-else size="mini" @click="searchKeyword = ''">
                        {{ $t('routeManager.clearSearch') }}
                      </el-button>
                    </div>
                  </div>
                </template>
              </el-table>
            </div>

            <transition name="dirty-slide">
              <div v-if="dirty" class="dirty-bar">
                <div class="dirty-bar__msg">
                  <span class="dirty-dot" />
                  {{ $t('routeManager.unsavedShort') }}
                </div>
                <div class="dirty-bar__actions">
                  <el-button size="mini" @click="handleRevert">{{ $t('routeManager.revertChanges') }}</el-button>
                  <el-button
                    type="primary"
                    size="mini"
                    :loading="saving"
                    :title="$t('routeManager.saveShortcut')"
                    @click="handleSaveRoutes"
                  >
                    {{ $t('routeManager.saveConfig') }}
                  </el-button>
                </div>
              </div>
            </transition>
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
      width="720px"
      custom-class="route-dialog"
      @close="handleDialogClose"
    >
      <div class="dialog-head">
        <div class="dialog-head__icon"><i class="fas fa-route" /></div>
        <div class="dialog-head__text">
          <div class="dialog-head__title">{{ dialogTitle }}</div>
          <div class="dialog-head__path mono">{{ $t('routeManager.previewPath') }} · {{ previewPath }}</div>
        </div>
      </div>

      <el-form ref="routeForm" :model="routeForm" :rules="routeRules" label-width="96px" class="route-form">
        <div class="form-section">
          <div class="form-section__head">
            <span class="form-section__no">01</span>{{ $t('routeManager.basicInfo') }}
          </div>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item :label="$t('routeManager.path')" prop="path">
                <el-input v-model="routeForm.path" :placeholder="$t('routeManager.inputPath')" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
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
            </el-col>
          </el-row>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item :label="$t('routeManager.name')" prop="name">
                <el-input v-model="routeForm.name" :placeholder="$t('routeManager.inputName')" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item :label="$t('routeManager.redirect')" prop="redirect">
                <el-input v-model="routeForm.redirect" :placeholder="$t('routeManager.inputRedirect')" />
              </el-form-item>
            </el-col>
          </el-row>
          <div class="switch-grid">
            <el-form-item :label="$t('routeManager.hidden')">
              <el-switch v-model="routeForm.hidden" />
            </el-form-item>
            <el-form-item :label="$t('routeManager.alwaysShow')">
              <el-switch v-model="routeForm.alwaysShow" />
            </el-form-item>
            <el-form-item :label="$t('routeManager.breadcrumb')">
              <el-switch v-model="routeForm.meta.breadcrumb" />
            </el-form-item>
          </div>
        </div>

        <div class="form-section">
          <div class="form-section__head">
            <span class="form-section__no">02</span>{{ $t('routeManager.metaConfig') }}
          </div>
          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="title" prop="meta.title">
                <el-input v-model="routeForm.meta.title" :placeholder="$t('routeManager.inputTitle')" />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item :label="$t('routeManager.icon')" prop="meta.icon">
                <el-input v-model="routeForm.meta.icon" :placeholder="$t('routeManager.inputIcon')">
                  <template v-if="routeForm.meta.icon" slot="append">
                    <i :class="routeForm.meta.icon" />
                  </template>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>
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
          <el-form-item :label="$t('routeManager.activeMenu')" prop="meta.activeMenu">
            <el-input v-model="routeForm.meta.activeMenu" :placeholder="$t('routeManager.inputActiveMenu')" />
          </el-form-item>
        </div>
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

    <!-- Move / Copy node dialog -->
    <el-dialog
      :title="nodeOpTitle"
      :visible.sync="nodeOpVisible"
      width="600px"
      custom-class="route-dialog"
      append-to-body
    >
      <div class="node-op-source">
        <span class="node-op-source__label">{{ $t('routeManager.sourceNode') }}</span>
        <span class="mono">{{ nodeOpRoute ? nodeOpRoute.path : '' }}</span>
        <span v-if="nodeOpRoute && nodeOpRoute.meta && nodeOpRoute.meta.title" class="node-op-source__title">
          {{ nodeOpRoute.meta.title }}
        </span>
      </div>
      <el-form label-width="96px">
        <el-form-item :label="$t('routeManager.targetParent')">
          <el-select v-model="nodeOpTargetKey" filterable clearable :placeholder="$t('routeManager.selectTargetParent')" style="width: 100%">
            <el-option :label="$t('routeManager.rootLevel')" value="__root__" />
            <el-option
              v-for="opt in nodeOpParentOptions"
              :key="opt.value"
              :label="opt.label"
              :value="opt.value"
            />
          </el-select>
          <p class="node-op-hint">{{ $t('routeManager.targetParentTip') }}</p>
        </el-form-item>
        <el-form-item v-if="nodeOpMode === 'copy'" :label="$t('routeManager.copyPath')">
          <el-input v-model="nodeOpCopyPath" :placeholder="$t('routeManager.inputPath')" />
        </el-form-item>
        <el-form-item v-if="nodeOpMode === 'copy'" :label="$t('routeManager.copyName')">
          <el-input v-model="nodeOpCopyName" :placeholder="$t('routeManager.inputName')" />
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button @click="nodeOpVisible = false">{{ $t('common.cancel') }}</el-button>
        <el-button type="primary" @click="handleNodeOpConfirm">{{ $t('common.confirm') }}</el-button>
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
        <div class="drawer-summary">
          <i class="fas fa-history" />
          <span>{{ $t('routeManager.versionCount', { count: versionList.length }) }}</span>
        </div>
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
  deleteRouteConfigs,
  getRouteConfigsByGroup,
  getRouteGroups,
  getRouteVersions,
  refreshRouteCache,
  rollbackRouteVersion,
  saveRouteConfigs
} from '@/api/view'
import {activateRole, queryRoles, saveRole} from '@/api/role'

export default {
  name: 'RouteManager',
  data() {
    return {
      ready: false,
      routeGroups: [],
      groupRouteCounts: {},
      activeGroup: '',
      currentRoutes: [],
      loadingRoutes: false,
      loadError: false,
      saving: false,
      refreshing: false,
      dirty: false,
      searchKeyword: '',
      expandAll: true,
      tableVersion: 0,
      // 节点移动 / 复制
      nodeOpVisible: false,
      nodeOpMode: 'move', // move, copy
      nodeOpRoute: null,
      nodeOpTargetKey: '__root__',
      nodeOpCopyPath: '',
      nodeOpCopyName: '',
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
    topLevelCount() {
      return this.currentRoutes.length
    },
    filteredRoutes() {
      const keyword = this.searchKeyword.trim().toLowerCase()
      if (!keyword) return this.currentRoutes
      const filterTree = routes => routes.reduce((acc, route) => {
        const children = filterTree(route.children || [])
        if (this.matchRoute(route, keyword) || children.length) {
          acc.push({ ...route, children })
        }
        return acc
      }, [])
      return filterTree(this.currentRoutes)
    },
    nodeOpTitle() {
      return this.nodeOpMode === 'copy'
        ? this.$t('routeManager.copyNode')
        : this.$t('routeManager.moveNode')
    },
    nodeOpParentOptions() {
      const options = []
      const walk = (routes, prefix) => {
        routes.forEach(route => {
          if (route === this.nodeOpRoute || this.isDescendant(this.nodeOpRoute, route)) return
          const label = `${prefix}${route.path || '/'}`
          options.push({ value: route._rowKey, label })
          if (route.children && route.children.length) {
            walk(route.children, `${label} / `)
          }
        })
      }
      walk(this.currentRoutes, '')
      return options
    },
    previewPath() {
      const path = (this.routeForm.path || '').trim()
      if (this.dialogType !== 'addChild' || !this.editingRoute) return path || '/'
      const parent = (this.editingRoute.path || '').replace(/\/$/, '')
      return `${parent}/${path.replace(/^\//, '')}`
    },
    versionDrawerTitle() {
      return this.activeGroup
        ? `${this.$t('routeManager.versionHistory')} · ${this.activeGroup}`
        : this.$t('routeManager.versionHistory')
    }
  },
  watch: {
    activeGroup() {
      this.searchKeyword = ''
    }
  },
  created() {
    this.loadRouteGroups()
    this.loadViewComponents()
    this.loadMenuRoles()
  },
  mounted() {
    setTimeout(() => { this.ready = true }, 60)
    window.addEventListener('keydown', this.handleGlobalKeydown)
  },
  beforeDestroy() {
    window.removeEventListener('keydown', this.handleGlobalKeydown)
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

    rolesOf(route) {
      return (route.meta && route.meta.roles) || []
    },

    matchRoute(route, keyword) {
      const meta = route.meta || {}
      return [route.path, route.component, route.name, route.redirect, meta.title, meta.icon, meta.activeMenu]
        .concat(this.rolesOf(route))
        .some(value => String(value || '').toLowerCase().includes(keyword))
    },

    // 为树中每个节点分配唯一 key、层级和面包屑链，供 row-key 与层级展示使用
    assignRowKeys(routes, parentKey = '', level = 1, chain = []) {
      routes.forEach((route, index) => {
        route._rowKey = `${parentKey}/${index}-${route.path || route.component || 'node'}`
        route._level = level
        route._chain = chain.concat(route.path || '/')
        if (route.children && route.children.length) {
          this.assignRowKeys(route.children, route._rowKey, level + 1, route._chain)
        }
      })
    },

    findRouteByKey(routes, key) {
      for (const route of routes) {
        if (route._rowKey === key) return route
        if (route.children && route.children.length) {
          const found = this.findRouteByKey(route.children, key)
          if (found) return found
        }
      }
      return null
    },

    isDescendant(parent, child) {
      if (!parent || !child || !parent.children) return false
      return parent.children.some(item => item === child || this.isDescendant(item, child))
    },

    // 解析搜索结果中的副本节点回真实节点（filteredRoutes 会 spread 出新对象）
    resolveRoute(route) {
      if (!route || !route._rowKey) return route
      return this.findRouteByKey(this.currentRoutes, route._rowKey) || route
    },

    syncActiveGroupCount() {
      if (this.activeGroup) {
        this.$set(this.groupRouteCounts, this.activeGroup, this.routeCount)
      }
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

    handleReloadCurrentGroup() {
      if (this.activeGroup) {
        this.loadRouteConfigs(this.activeGroup)
      }
    },

    async loadRouteConfigs(group) {
      this.loadingRoutes = true
      this.loadError = false
      try {
        const response = await getRouteConfigsByGroup(group)
        const routes = response.data || []
        // 兼容后端返回无 sortOrder 的旧数据
        this.normalizeSortOrder(routes)
        // 后端已按 sortOrder 排序，前端再兜底排序保证数组顺序与序号一致
        this.sortByOrder(routes)
        this.assignRowKeys(routes)
        this.currentRoutes = routes
        this.tableVersion++
        this.dirty = false
        this.syncActiveGroupCount()
      } catch (error) {
        console.error('获取路由配置失败:', error)
        this.loadError = true
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
      const real = this.resolveRoute(route)
      const container = this.findContainer(this.currentRoutes, real)
      return !container || container.indexOf(real) <= 0
    },

    isLast(route) {
      const real = this.resolveRoute(route)
      const container = this.findContainer(this.currentRoutes, real)
      return !container || container.indexOf(real) >= container.length - 1
    },

    // 上移 / 下移（direction: -1 上移, 1 下移），仅修改本地状态并标记未保存，点“保存配置”后才落库并产生新版本
    handleMove(route, direction) {
      const real = this.resolveRoute(route)
      const container = this.findContainer(this.currentRoutes, real)
      if (!container) return
      const index = container.indexOf(real)
      const target = index + direction
      if (target < 0 || target >= container.length) return

      container.splice(index, 1)
      container.splice(target, 0, real)
      this.reassignSortOrder(this.currentRoutes)
      this.assignRowKeys(this.currentRoutes)
      this.tableVersion++
      this.dirty = true
    },

    openNodeOp(route, mode) {
      const real = this.resolveRoute(route)
      if (!real) return
      this.nodeOpRoute = real
      this.nodeOpMode = mode
      this.nodeOpTargetKey = '__root__'
      if (mode === 'copy') {
        this.nodeOpCopyPath = `${real.path || ''}_copy`
        this.nodeOpCopyName = `${real.name || ''} 副本`
      }
      this.nodeOpVisible = true
    },

    handleMoveNode(route) {
      this.openNodeOp(route, 'move')
    },

    handleCopyNode(route) {
      this.openNodeOp(route, 'copy')
    },

    handleNodeOpConfirm() {
      if (!this.nodeOpRoute) return
      if (this.nodeOpMode === 'copy' && !this.nodeOpCopyPath.trim()) {
        this.$message.warning(this.$t('routeManager.inputPath'))
        return
      }

      const sourceContainer = this.findContainer(this.currentRoutes, this.nodeOpRoute)
      if (!sourceContainer) return

      let targetContainer = this.currentRoutes
      if (this.nodeOpTargetKey !== '__root__') {
        const target = this.findRouteByKey(this.currentRoutes, this.nodeOpTargetKey)
        if (!target) return
        if (!target.children) {
          this.$set(target, 'children', [])
        }
        targetContainer = target.children
      }

      if (this.nodeOpMode === 'move') {
        // 禁止把节点移动到自己或自己的子孙下
        if (this.nodeOpTargetKey !== '__root__') {
          const target = this.findRouteByKey(this.currentRoutes, this.nodeOpTargetKey)
          if (target === this.nodeOpRoute || this.isDescendant(this.nodeOpRoute, target)) {
            this.$message.warning(this.$t('routeManager.invalidMoveTarget'))
            return
          }
        }
        sourceContainer.splice(sourceContainer.indexOf(this.nodeOpRoute), 1)
        targetContainer.push(this.nodeOpRoute)
      } else {
        const clone = JSON.parse(JSON.stringify(this.nodeOpRoute))
        delete clone._rowKey
        delete clone._level
        delete clone._chain
        clone.path = this.nodeOpCopyPath.trim()
        clone.name = this.nodeOpCopyName.trim()
        targetContainer.push(clone)
      }

      this.reassignSortOrder(this.currentRoutes)
      this.assignRowKeys(this.currentRoutes)
      this.tableVersion++
      this.syncActiveGroupCount()
      this.dirty = true
      this.nodeOpVisible = false
      this.$message.success(this.$t(this.nodeOpMode === 'move' ? 'routeManager.moveNodeSuccess' : 'routeManager.copyNodeSuccess'))
    },

    handleToggleExpandAll() {
      this.expandAll = !this.expandAll
      this.tableVersion++
    },

    handleToggleFlag(route, key) {
      const real = this.resolveRoute(route)
      this.$set(real, key, !real[key])
      this.dirty = true
    },

    handleRevert() {
      this.$confirm(this.$t('routeManager.revertConfirm'), this.$t('common.tip'), {
        confirmButtonText: this.$t('common.confirm'),
        cancelButtonText: this.$t('common.cancel'),
        type: 'warning'
      }).then(async() => {
        await this.loadRouteConfigs(this.activeGroup)
      }).catch(() => {})
    },

    handleGlobalKeydown(event) {
      if (!(event.ctrlKey || event.metaKey) || event.key !== 's') return
      if (this.dialogVisible || this.roleDialogVisible || this.saveDialogVisible || this.versionDrawerVisible) return
      if (!this.dirty || !this.activeGroup) return
      event.preventDefault()
      this.handleSaveRoutes()
    },

    // 打开保存说明对话框，确认后再真正保存
    handleSaveRoutes() {
      if (this.currentRoutes.length === 0) {
        this.$message.warning(this.$t('routeManager.emptyGroupTip'))
        return
      }
      this.reassignSortOrder(this.currentRoutes)
      this.assignRowKeys(this.currentRoutes)
      this.saveDescription = ''
      this.saveDialogVisible = true
    },

    async handleSaveConfirm() {
      this.saving = true
      try {
        const response = await saveRouteConfigs(this.activeGroup, this.currentRoutes, this.saveDescription)
        this.dirty = false
        this.saveDialogVisible = false
        this.syncActiveGroupCount()
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
          this.$set(this.groupRouteCounts, value, 0)
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
          this.$delete(this.groupRouteCounts, group)
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
      const real = this.resolveRoute(route)
      this.dialogType = 'edit'
      this.editingRoute = real
      this.routeForm = {
        path: real.path || '',
        component: real.component || '',
        name: real.name || '',
        redirect: real.redirect || '',
        hidden: real.hidden || false,
        alwaysShow: real.alwaysShow || false,
        meta: {
          title: real.meta?.title || '',
          icon: real.meta?.icon || '',
          roles: real.meta?.roles || [],
          breadcrumb: real.meta?.breadcrumb !== false,
          activeMenu: real.meta?.activeMenu || ''
        }
      }
      this.dialogVisible = true
    },

    handleAddChildRoute(parentRoute) {
      this.dialogType = 'addChild'
      this.editingRoute = this.resolveRoute(parentRoute)
      this.routeForm = this.createEmptyForm()
      this.dialogVisible = true
    },

    handleDeleteRoute(route) {
      const real = this.resolveRoute(route)
      this.$confirm(this.$t('routeManager.deleteRouteConfirm', { path: real.path }), this.$t('common.tip'), {
        confirmButtonText: this.$t('common.confirm'),
        cancelButtonText: this.$t('common.cancel'),
        type: 'warning'
      }).then(() => {
        this.removeRouteFromTree(this.currentRoutes, real)
        this.reassignSortOrder(this.currentRoutes)
        this.assignRowKeys(this.currentRoutes)
        this.tableVersion++
        this.syncActiveGroupCount()
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
        this.assignRowKeys(this.currentRoutes)
        this.tableVersion++
        this.syncActiveGroupCount()
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
@import '~@/styles/variables.scss';
@import '~@/styles/mixin.scss';

/* ── Palette ── */
$bg: #f6f7fb;
$surface: rgba(255, 255, 255, 0.78);
$surface-h: rgba(255, 255, 255, 0.92);
$border: rgba(21, 26, 45, 0.07);
$border-h: rgba(21, 26, 45, 0.12);
$primary: #1d4ed8;
$primary-l: #3b82f6;
$primary-bg: rgba(29, 78, 216, 0.07);
$amber: #d97706;
$green: #0e9f6e;
$red: #e5484d;
$ink: #151a2d;
$ink-2: #4b5571;
$ink-3: #8b93a8;
$ink-4: #b3bacb;
$radius: 18px;
$radius-sm: 12px;
$ease: cubic-bezier(0.22, 1, 0.36, 1);
$font-display: 'Outfit', -apple-system, BlinkMacSystemFont, sans-serif;
$font-body: 'DM Sans', -apple-system, BlinkMacSystemFont, 'PingFang SC', sans-serif;
$font-mono: 'SF Mono', 'Fira Code', 'JetBrains Mono', Consolas, monospace;

.route-studio {
  position: relative;
  min-height: calc(100vh - 44px);
  background: $bg;
  font-family: $font-body;
  color: $ink;
  overflow-x: hidden;
}

/* ── Atmospheric Background ── */
.bg {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
}

.bg__grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(29, 78, 216, 0.035) 1px, transparent 1px),
    linear-gradient(90deg, rgba(29, 78, 216, 0.035) 1px, transparent 1px);
  background-size: 36px 36px;
  -webkit-mask-image: linear-gradient(180deg, #000 0%, transparent 82%);
  mask-image: linear-gradient(180deg, #000 0%, transparent 82%);
}

.bg__orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(90px);
  opacity: 0.45;
  will-change: transform;
}

.bg__orb--1 {
  width: 560px;
  height: 560px;
  top: -14%;
  left: -8%;
  background: radial-gradient(circle, rgba(29, 78, 216, 0.14) 0%, transparent 70%);
  animation: orb-drift 20s ease-in-out infinite;
}

.bg__orb--2 {
  width: 460px;
  height: 460px;
  top: 24%;
  right: -10%;
  background: radial-gradient(circle, rgba(14, 159, 110, 0.11) 0%, transparent 70%);
  animation: orb-drift 25s ease-in-out infinite reverse;
}

.bg__grain {
  position: absolute;
  inset: 0;
  opacity: 0.3;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)' opacity='0.04'/%3E%3C/svg%3E");
  background-size: 200px 200px;
}

@keyframes orb-drift {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(28px, -18px) scale(1.05); }
  66% { transform: translate(-18px, 22px) scale(0.97); }
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
  gap: 16px;
  padding: 20px 26px;
  background: $surface;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: $radius;
  border: 1px solid $border;
  box-shadow: 0 1px 2px rgba(21, 26, 45, 0.02), 0 4px 16px rgba(21, 26, 45, 0.03);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
  min-width: 0;
}

.header-icon {
  width: 54px;
  height: 54px;
  border-radius: 16px;
  background: linear-gradient(135deg, $primary 0%, #1e3a8a 62%, #0e9f6e 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 22px;
  box-shadow: 0 8px 26px rgba(29, 78, 216, 0.24);
  flex-shrink: 0;
}

.header-title {
  font-family: $font-display;
  font-size: 26px;
  font-weight: 800;
  margin: 0 0 3px 0;
  letter-spacing: -0.5px;
  background: linear-gradient(135deg, $ink 0%, #374151 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.header-subtitle {
  font-size: 13px;
  color: $ink-3;
  margin: 0;
  letter-spacing: 0.2px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}

.header-stats {
  display: flex;
  align-items: center;
  gap: 14px;
}

.stat {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  line-height: 1.1;
}

.stat__num {
  font-family: $font-display;
  font-size: 20px;
  font-weight: 700;
  color: $ink;
  font-variant-numeric: tabular-nums;
}

.stat__label {
  font-size: 11px;
  color: $ink-3;
  margin-top: 3px;
}

.stat__sep {
  width: 1px;
  height: 28px;
  background: $border-h;
}

.ghost-btn {
  background: $surface-h;
  border: 1px solid $border-h;
  border-radius: $radius-sm;
  padding: 10px 18px;
  font-weight: 600;
  color: $ink-2;
  transition: all 0.3s $ease;

  &:hover {
    color: $primary;
    border-color: rgba(29, 78, 216, 0.3);
    transform: translateY(-1px);
    box-shadow: 0 4px 14px rgba(29, 78, 216, 0.12);
  }

  i {
    margin-right: 7px;
  }
}

/* ── Workspace ── */
.workspace {
  display: grid;
  grid-template-columns: 252px 1fr;
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
  box-shadow: 0 1px 2px rgba(21, 26, 45, 0.02), 0 4px 16px rgba(21, 26, 45, 0.03);
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
  color: $ink-2;
  letter-spacing: 0.4px;

  i {
    margin-right: 7px;
    color: $primary;
  }
}

.rail-add {
  width: 30px;
  height: 30px;
  border-radius: 9px;
  border: 1px dashed rgba(29, 78, 216, 0.35);
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
  padding: 11px 11px 11px 14px;
  border-radius: 11px;
  cursor: pointer;
  border: 1px solid transparent;
  transition: all 0.25s $ease;

  &:hover {
    background: rgba(21, 26, 45, 0.035);

    .rail-item__del {
      opacity: 1;
    }
  }

  &._active {
    background: linear-gradient(135deg, rgba(29, 78, 216, 0.1) 0%, rgba(59, 130, 246, 0.06) 100%);
    border-color: rgba(29, 78, 216, 0.18);

    .rail-item__bar {
      background: $primary-l;
    }

    .rail-item__name {
      color: $primary;
      font-weight: 700;
    }

    .rail-item__count {
      color: $primary;
      background: rgba(255, 255, 255, 0.75);
    }
  }
}

.rail-item__bar {
  position: absolute;
  left: 0;
  top: 22%;
  bottom: 22%;
  width: 3px;
  border-radius: 0 3px 3px 0;
  background: transparent;
  transition: background 0.25s $ease;
}

.rail-item__name {
  flex: 1;
  font-size: 13.5px;
  color: $ink-2;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.rail-item__count {
  font-family: $font-display;
  font-size: 11px;
  font-weight: 700;
  color: $ink-3;
  background: rgba(21, 26, 45, 0.05);
  border-radius: 999px;
  min-width: 24px;
  text-align: center;
  padding: 2px 6px;
  font-variant-numeric: tabular-nums;
}

.rail-item__del {
  opacity: 0;
  border: none;
  background: none;
  color: $ink-3;
  cursor: pointer;
  font-size: 12px;
  padding: 3px;
  border-radius: 6px;
  transition: all 0.2s $ease;
  flex-shrink: 0;

  &:hover {
    color: $red;
    background: rgba(229, 72, 77, 0.1);
  }
}

.rail-empty {
  padding: 36px 12px;
  text-align: center;
  color: $ink-3;
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

.rail-foot {
  display: flex;
  align-items: flex-start;
  gap: 7px;
  padding: 12px 14px;
  border-top: 1px solid $border;
  color: $ink-3;
  font-size: 11px;
  line-height: 1.5;

  i {
    color: $amber;
    margin-top: 2px;
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
  box-shadow: 0 1px 2px rgba(21, 26, 45, 0.02), 0 4px 16px rgba(21, 26, 45, 0.03);
  overflow: hidden;
  min-width: 0;
}

.pane-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 13px 16px;
  border-bottom: 1px solid $border;
  flex-wrap: wrap;
  gap: 10px;
}

.pane-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.pane-group-chip {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  padding: 6px 14px;
  border-radius: 999px;
  background: linear-gradient(135deg, rgba(29, 78, 216, 0.1), rgba(59, 130, 246, 0.06));
  border: 1px solid rgba(29, 78, 216, 0.16);
  color: $primary;
  font-family: $font-display;
  font-weight: 700;
  font-size: 13px;
}

.pane-stat {
  display: inline-flex;
  align-items: baseline;
  gap: 4px;
  font-size: 12px;
  color: $ink-3;

  b {
    font-family: $font-display;
    font-size: 14px;
    color: $ink-2;
    font-variant-numeric: tabular-nums;
  }

  &--dim b {
    color: $ink-3;
  }
}

.pane-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.pane-search {
  width: 230px;

  ::v-deep .el-input__inner {
    border-radius: 999px;
    background: rgba(255, 255, 255, 0.8);
    border-color: $border-h;
    font-size: 12.5px;

    &:focus {
      border-color: $primary-l;
    }
  }
}

.icon-btn {
  width: 34px;
  height: 32px;
  border-radius: 10px;
  border: 1px solid $border-h;
  background: rgba(255, 255, 255, 0.72);
  color: $ink-2;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  transition: all 0.2s $ease;

  &:hover {
    color: $primary;
    border-color: rgba(29, 78, 216, 0.32);
    background: #fff;
    transform: translateY(-1px);
  }
}

.pane-actions ::v-deep .el-button--primary {
  background: linear-gradient(135deg, $primary 0%, #1e3a8a 100%);
  border: none;
  box-shadow: 0 3px 12px rgba(29, 78, 216, 0.24);

  &:hover {
    box-shadow: 0 5px 18px rgba(29, 78, 216, 0.34);
    transform: translateY(-1px);
  }
}

/* ── Route Table ── */
.pane-table {
  flex: 1;
  overflow: auto;
  min-height: 180px;

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
      color: $ink-3;
    }
  }
}

.route-path-cell {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.path-text {
  font-size: 13px;
  font-weight: 700;
  color: $ink;
  word-break: break-all;
}

.level-pill {
  font-family: $font-mono;
  font-size: 10px;
  font-weight: 700;
  padding: 2px 6px;
  border-radius: 5px;
  background: rgba(29, 78, 216, 0.09);
  color: $primary;
  flex-shrink: 0;

  &--2 { background: rgba(124, 58, 237, 0.09); color: #7c3aed; }
  &--3 { background: rgba(217, 119, 6, 0.1); color: $amber; }
  &--4 { background: rgba(14, 159, 110, 0.1); color: $green; }
}

.child-count {
  margin-left: 4px;
  font-family: $font-mono;
  font-size: 10px;
  opacity: 0.75;
}

.route-chain {
  font-size: 10.5px;
  color: $ink-3;
  margin-top: 3px;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.level-tree {
  display: inline-flex;
  align-items: center;
  gap: 2px;

  &__dash {
    display: inline-block;
    width: 9px;
    height: 3px;
    border-radius: 2px;
    background: $primary-l;

    &:last-child {
      background: $primary;
    }
  }
}

.route-sub-cell {
  font-size: 11px;
  color: $ink-3;
  line-height: 1.7;
  word-break: break-all;

  .sub-sep {
    margin-left: 8px;
  }
}

.tag {
  font-size: 10.5px;
  font-weight: 700;
  padding: 2px 7px;
  border-radius: 6px;
  white-space: nowrap;

  i {
    margin-right: 3px;
    font-size: 9px;
  }

  &--layout {
    color: $primary;
    background: $primary-bg;
  }

  &--parent {
    color: #7c3aed;
    background: rgba(124, 58, 237, 0.08);
  }

  &--hidden {
    color: $amber;
    background: rgba(217, 119, 6, 0.1);
  }
}

.role-chip {
  font-family: $font-mono;
  font-size: 10px;
  color: $primary;
  background: $primary-bg;
  border: 1px solid rgba(29, 78, 216, 0.12);
  padding: 1px 6px;
  border-radius: 999px;
  margin-left: 6px;
}

.inline-flags,
.inline-ops {
  display: flex;
  align-items: center;
  gap: 6px;
}

.inline-ops {
  justify-content: flex-end;
}

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
  color: $ink-2;
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

.name-cell {
  font-size: 12.5px;
  color: $ink-2;
}

.redirect-cell {
  color: $ink-3;

  i {
    font-size: 10px;
    margin-right: 3px;
  }
}

.dim {
  color: #c9cedb;
}

.flag-toggle {
  width: 30px;
  height: 30px;
  border-radius: 9px;
  border: 1px solid $border;
  background: rgba(255, 255, 255, 0.55);
  color: $ink-4;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  transition: all 0.2s $ease;

  &:hover {
    transform: translateY(-1px);
    border-color: rgba(29, 78, 216, 0.25);
  }

  &.is-on {
    color: $primary;
    background: $primary-bg;
    border-color: rgba(29, 78, 216, 0.22);
  }

  &--warn.is-on {
    color: $amber;
    background: rgba(217, 119, 6, 0.1);
    border-color: rgba(217, 119, 6, 0.28);
  }
}

.op-icon {
  width: 30px;
  height: 30px;
  border: none;
  background: transparent;
  color: $ink-2;
  border-radius: 8px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  transition: all 0.2s $ease;

  &:hover {
    background: $primary-bg;
    color: $primary;
  }

  &--danger:hover {
    background: rgba(229, 72, 77, 0.1);
    color: $red;
  }
}

.mono {
  font-family: $font-mono;
}

/* ── Empty / Placeholder ── */
.table-empty {
  padding: 44px 0;
  text-align: center;
  color: $ink-3;
  font-size: 13px;

  i {
    font-size: 34px;
    opacity: 0.45;
    margin-bottom: 10px;
  }

  p {
    margin: 0 0 12px;
  }
}

.table-empty__hint {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  flex-wrap: wrap;

  .mono {
    color: $ink-3;
  }
}

.pane-placeholder {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  color: $ink-3;
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
  background: linear-gradient(135deg, rgba(29, 78, 216, 0.09), rgba(14, 159, 110, 0.08));
  border: 1px solid rgba(29, 78, 216, 0.14);
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

/* ── Dirty state bar ── */
.dirty-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin: 0 12px 12px;
  padding: 9px 14px;
  border-radius: $radius-sm;
  background: linear-gradient(135deg, rgba(217, 119, 6, 0.1), rgba(245, 158, 11, 0.05));
  border: 1px solid rgba(217, 119, 6, 0.22);
  flex-wrap: wrap;
}

.dirty-bar__msg {
  display: flex;
  align-items: center;
  gap: 9px;
  font-size: 12.5px;
  font-weight: 600;
  color: #92400e;
}

.dirty-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: $amber;
  box-shadow: 0 0 0 4px rgba(217, 119, 6, 0.15);
  animation: dirty-pulse 1.8s ease-in-out infinite;
}

.dirty-bar__actions {
  display: flex;
  gap: 8px;
  margin-left: auto;
}

@keyframes dirty-pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.45; }
}

.dirty-slide-enter-active,
.dirty-slide-leave-active {
  transition: all 0.3s $ease;
}

.dirty-slide-enter,
.dirty-slide-leave-to {
  opacity: 0;
  transform: translateY(10px);
}

/* ── Version history drawer ── */
.drawer-body {
  padding: 4px 24px 24px;
  min-height: 200px;
}

.drawer-summary {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  margin-bottom: 16px;
  border-radius: 10px;
  background: $primary-bg;
  color: $primary;
  font-size: 12.5px;
  font-weight: 600;
}

.version-card {
  background: $surface-h;
  border: 1px solid $border;
  border-radius: $radius-sm;
  padding: 12px 14px;
  transition: all 0.25s $ease;

  &:hover {
    border-color: rgba(29, 78, 216, 0.22);
    box-shadow: 0 4px 14px rgba(29, 78, 216, 0.08);
  }

  &--current {
    border-color: rgba(14, 159, 110, 0.35);
    background: rgba(14, 159, 110, 0.045);
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
  color: $ink-2;
  font-weight: 600;

  &:hover {
    color: $amber;
  }
}

.version-desc {
  margin: 8px 0 0;
  font-size: 13px;
  color: $ink-2;
  line-height: 1.55;
  word-break: break-all;

  &.dim {
    color: $ink-3;
  }
}

.drawer-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 48px 0;
  color: $ink-3;
  font-size: 26px;

  p {
    margin: 0;
    font-size: 13px;
  }
}

/* ── Move / Copy node dialog ── */
.node-op-source {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  padding: 12px 14px;
  margin-bottom: 18px;
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(29, 78, 216, 0.08), rgba(14, 159, 110, 0.05));
  border: 1px solid rgba(29, 78, 216, 0.14);

  &__label {
    font-size: 12px;
    font-weight: 700;
    color: $ink-2;
  }

  .mono {
    font-size: 12.5px;
    color: $primary;
    font-weight: 700;
  }

  &__title {
    font-size: 12px;
    color: $ink-3;
  }
}

.node-op-hint {
  margin: 6px 0 0;
  font-size: 11.5px;
  color: $ink-3;
  line-height: 1.5;
}

/* ── Dialog ── */
.route-dialog {
  border-radius: 20px !important;
  overflow: hidden;

  ::v-deep .el-dialog__header {
    padding: 18px 24px 12px;
    border-bottom: 1px solid $border;
  }

  ::v-deep .el-dialog__title {
    font-family: $font-display;
    font-weight: 700;
    font-size: 18px;
    color: $ink;
  }

  ::v-deep .el-dialog__body {
    padding: 18px 24px 6px;
    max-height: 66vh;
    overflow-y: auto;
  }

  ::v-deep .el-dialog__footer {
    padding: 12px 24px 18px;
    border-top: 1px solid $border;
  }
}

.dialog-head {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  margin-bottom: 18px;
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(29, 78, 216, 0.08), rgba(14, 159, 110, 0.05));
  border: 1px solid rgba(29, 78, 216, 0.14);
}

.dialog-head__icon {
  width: 38px;
  height: 38px;
  border-radius: 11px;
  background: $primary;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  flex-shrink: 0;
}

.dialog-head__text {
  min-width: 0;
}

.dialog-head__title {
  font-family: $font-display;
  font-size: 14px;
  font-weight: 700;
  color: $ink;
}

.dialog-head__path {
  margin-top: 3px;
  font-size: 11.5px;
  color: $primary;
  word-break: break-all;
}

.form-section {
  margin-bottom: 18px;

  &:last-child {
    margin-bottom: 0;
  }
}

.form-section__head {
  display: flex;
  align-items: center;
  gap: 8px;
  padding-bottom: 9px;
  margin-bottom: 14px;
  border-bottom: 1px dashed $border-h;
  font-family: $font-display;
  font-size: 13px;
  font-weight: 700;
  color: $ink;
}

.form-section__no {
  font-family: $font-mono;
  font-size: 10px;
  color: $primary;
  background: $primary-bg;
  padding: 2px 6px;
  border-radius: 6px;
}

.switch-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
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

/* ── 触摸设备：不依赖 hover 的操作入口 ── */
@media (hover: none) {
  .rail-item__del,
  .order-arrows,
  .drag-grip {
    opacity: 1;
  }
}

/* ── 窄屏适配：分组栏转横向 chip，路由行改卡片布局 ── */
@include respond-to(mobile) {
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

    .rail-item__bar {
      display: none;
    }
  }

  .rail-item__name {
    flex: 0 1 auto;
    max-width: 140px;
  }

  .rail-foot {
    display: none;
  }

  .pane-search {
    width: 100%;
  }

  .switch-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@include respond-to(phone) {
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

  .header-stats {
    display: none;
  }

  .header-right {
    width: 100%;
  }

  .rail-item__name {
    max-width: 110px;
  }

  .switch-grid {
    grid-template-columns: 1fr;
  }

  .dirty-bar {
    margin: 0 8px 8px;
  }
}
</style>

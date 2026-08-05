<template>
  <div class="permission-control-panel">
    <!-- Atmospheric background -->
    <div class="bg">
      <div class="bg__orb bg__orb--1" />
      <div class="bg__orb bg__orb--2" />
      <div class="bg__orb bg__orb--3" />
      <div class="bg__grain" />
    </div>

    <div class="panel-inner">
      <!-- Header section -->
      <header class="panel-header" :class="{ _on: ready }">
        <div class="header-content">
          <div class="header-left">
            <div class="header-icon">
              <i class="fas fa-shield-alt" />
            </div>
            <div class="header-text">
              <h1 class="header-title">{{ $t('role.controlPanel') }}</h1>
              <p class="header-subtitle">{{ $t('role.controlPanelSubtitle') }}</p>
            </div>
          </div>
          <div class="header-actions">
            <el-button
              type="primary"
              class="add-btn"
              @click="handleAddRole"
            >
              <i class="fas fa-plus" />
              <span>{{ $t('role.addRole') }}</span>
            </el-button>
          </div>
        </div>
      </header>

      <!-- Stats section -->
      <div class="stats-section" :class="{ _on: ready }">
        <div class="stat-card">
          <div class="stat-icon stat-icon--total">
            <i class="fas fa-database" />
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.total }}</div>
            <div class="stat-label">{{ $t('role.totalPermissions') }}</div>
          </div>
          <div class="stat-indicator" />
        </div>
        <div class="stat-card">
          <div class="stat-icon stat-icon--active">
            <i class="fas fa-check-circle" />
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.active }}</div>
            <div class="stat-label">{{ $t('role.activePermissions') }}</div>
          </div>
          <div class="stat-indicator stat-indicator--active" />
        </div>
        <div class="stat-card">
          <div class="stat-icon stat-icon--inactive">
            <i class="fas fa-times-circle" />
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.inactive }}</div>
            <div class="stat-label">{{ $t('role.inactivePermissions') }}</div>
          </div>
          <div class="stat-indicator stat-indicator--inactive" />
        </div>
        <div class="stat-card">
          <div class="stat-icon stat-icon--system">
            <i class="fas fa-cog" />
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.system }}</div>
            <div class="stat-label">{{ $t('role.systemPermissions') }}</div>
          </div>
          <div class="stat-indicator stat-indicator--system" />
        </div>
      </div>

      <!-- Filter and search section -->
      <div class="filter-section" :class="{ _on: ready }">
        <div class="filter-content">
          <div class="filter-left">
            <el-select
              v-model="selectedType"
              :placeholder="$t('role.filterByType')"
              class="type-filter"
              @change="handleTypeChange"
            >
              <el-option
                :label="$t('role.allTypes')"
                :value="null"
              />
              <el-option
                :label="$t('role.menuPermission')"
                :value="1"
              />
              <el-option
                :label="$t('role.dataPermission')"
                :value="2"
              />
              <el-option
                :label="$t('role.apiPermission')"
                :value="3"
              />
            </el-select>
          </div>
          <div class="filter-right">
            <el-input
              v-model="searchKeyword"
              :placeholder="$t('role.searchPlaceholder')"
              class="search-input"
              prefix-icon="el-icon-search"
              clearable
              @clear="handleSearch"
              @keyup.enter.native="handleSearch"
            />
          </div>
        </div>
      </div>

      <!-- Permission cards grid -->
      <div class="permissions-grid" :class="{ _on: ready }">
        <div
          v-for="(permission, index) in filteredPermissions"
          :key="permission.id"
          class="permission-card"
          :class="getPermissionCardClass(permission)"
          :style="{ animationDelay: `${index * 0.05}s` }"
          @click="handleEditRole(permission)"
        >
          <!-- Card header -->
          <div class="card-header">
            <div class="permission-type-badge" :class="getTypeBadgeClass(permission.type)">
              <i :class="getTypeIcon(permission.type)" />
              <span>{{ getTypeLabel(permission.type) }}</span>
            </div>
            <div class="permission-status" :class="getStatusClass(permission.flag)">
              <span class="status-dot" />
              <span class="status-text">{{ getStatusLabel(permission.flag) }}</span>
            </div>
          </div>

          <!-- Card content -->
          <div class="card-content">
            <h3 class="permission-name">{{ permission.name }}</h3>
            <div class="permission-code">
              <i class="fas fa-key" />
              <span>{{ permission.role }}</span>
            </div>
            <p class="permission-description">
              {{ permission.description || $t('role.noDescription') }}
            </p>
          </div>

          <!-- Card footer -->
          <div class="card-footer">
            <div class="card-meta">
              <div class="meta-item">
                <i class="fas fa-clock" />
                <span>{{ formatDate(permission.createTime) }}</span>
              </div>
            </div>
            <div class="card-actions" @click.stop>
              <el-dropdown trigger="click" @command="(cmd) => handleCommand(cmd, permission)">
                <el-button class="action-btn" size="mini">
                  <i class="fas fa-ellipsis-v" />
                </el-button>
                <el-dropdown-menu slot="dropdown" class="action-dropdown">
                  <el-dropdown-item command="edit">
                    <i class="fas fa-edit" />
                    <span>{{ $t('common.edit') }}</span>
                  </el-dropdown-item>
                  <el-dropdown-item
                    v-if="permission.flag !== 2"
                    :command="permission.flag === 1 ? 'disable' : 'enable'"
                  >
                    <i :class="permission.flag === 1 ? 'fas fa-ban' : 'fas fa-check'" />
                    <span>{{ permission.flag === 1 ? $t('role.disable') : $t('role.enable') }}</span>
                  </el-dropdown-item>
                  <el-dropdown-item
                    v-if="permission.flag !== 2"
                    command="delete"
                    divided
                  >
                    <i class="fas fa-trash" />
                    <span>{{ $t('common.delete') }}</span>
                  </el-dropdown-item>
                </el-dropdown-menu>
              </el-dropdown>
            </div>
          </div>

          <!-- Hover glow effect -->
          <div class="card-glow" />
        </div>
      </div>

      <!-- Empty state -->
      <div v-if="filteredPermissions.length === 0 && !loading" class="empty-state" :class="{ _on: ready }">
        <div class="empty-icon">
          <i class="fas fa-shield-alt" />
        </div>
        <h3 class="empty-title">{{ $t('common.noData') }}</h3>
        <p class="empty-description">{{ $t('role.controlPanelSubtitle') }}</p>
        <el-button type="primary" @click="handleAddRole">
          <i class="fas fa-plus" />
          <span>{{ $t('role.addRole') }}</span>
        </el-button>
      </div>

      <!-- Loading state -->
      <div v-if="loading" class="loading-state" :class="{ _on: ready }">
        <div class="loading-spinner">
          <div class="spinner-ring" />
          <div class="spinner-ring" />
          <div class="spinner-ring" />
        </div>
        <p class="loading-text">{{ $t('common.loading') }}</p>
      </div>
    </div>

    <!-- Edit/Add dialog -->
    <el-dialog
      :title="dialogTitle"
      :visible.sync="dialogVisible"
      width="600px"
      class="permission-dialog"
      :close-on-click-modal="false"
    >
      <el-form
        ref="permissionForm"
        :model="formData"
        :rules="formRules"
        label-width="120px"
        class="permission-form"
      >
        <el-form-item :label="$t('role.permissionCode')" prop="role">
          <el-input
            v-model="formData.role"
            :placeholder="$t('role.permissionCode')"
            :disabled="isSystemPermission"
          />
        </el-form-item>
        <el-form-item :label="$t('role.name')" prop="name">
          <el-input
            v-model="formData.name"
            :placeholder="$t('role.name')"
          />
        </el-form-item>
        <el-form-item :label="$t('role.permissionType')" prop="type">
          <el-select
            v-model="formData.type"
            :placeholder="$t('role.permissionType')"
            :disabled="isSystemPermission"
          >
            <el-option
              :label="$t('role.menuPermission')"
              :value="1"
            />
            <el-option
              :label="$t('role.dataPermission')"
              :value="2"
            />
            <el-option
              :label="$t('role.apiPermission')"
              :value="3"
            />
          </el-select>
        </el-form-item>
        <el-form-item :label="$t('role.description')" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            :placeholder="$t('role.description')"
          />
        </el-form-item>
        <el-form-item :label="$t('role.status')" prop="flag">
          <el-select
            v-model="formData.flag"
            :placeholder="$t('role.status')"
            :disabled="isSystemPermission"
          >
            <el-option
              :label="$t('role.active')"
              :value="1"
            />
            <el-option
              :label="$t('role.inactive')"
              :value="0"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogVisible = false">
          {{ $t('common.cancel') }}
        </el-button>
        <el-button type="primary" :loading="saving" @click="handleSubmit">
          {{ $t('common.save') }}
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import { queryRoles, saveRole, deleteRole, activateRole, disableRole } from '@/api/role'

export default {
  name: 'PermissionControlPanel',
  data() {
    return {
      ready: false,
      loading: false,
      saving: false,
      permissions: [],
      selectedType: null,
      searchKeyword: '',
      dialogVisible: false,
      isEditing: false,
      currentPermission: null,
      formData: {
        id: null,
        role: '',
        name: '',
        type: 1,
        description: '',
        flag: 1
      },
      formRules: {
        role: [
          { required: true, message: this.$t('role.permissionCode') + '不能为空', trigger: 'blur' }
        ],
        name: [
          { required: true, message: this.$t('role.name') + '不能为空', trigger: 'blur' }
        ],
        type: [
          { required: true, message: this.$t('role.permissionType') + '不能为空', trigger: 'change' }
        ]
      }
    }
  },
  computed: {
    filteredPermissions() {
      let result = this.permissions

      // Filter by type
      if (this.selectedType !== null) {
        result = result.filter(p => p.type === this.selectedType)
      }

      // Filter by search keyword
      if (this.searchKeyword) {
        const keyword = this.searchKeyword.toLowerCase()
        result = result.filter(p =>
          p.role.toLowerCase().includes(keyword) ||
          p.name.toLowerCase().includes(keyword)
        )
      }

      return result
    },
    stats() {
      const total = this.permissions.length
      const active = this.permissions.filter(p => p.flag === 1).length
      const inactive = this.permissions.filter(p => p.flag === 0).length
      const system = this.permissions.filter(p => p.flag === 2).length

      return { total, active, inactive, system }
    },
    dialogTitle() {
      return this.isEditing ? this.$t('role.editRole') : this.$t('role.addRole')
    },
    isSystemPermission() {
      return this.isEditing && this.currentPermission && this.currentPermission.flag === 2
    }
  },
  mounted() {
    this.fetchPermissions()
    requestAnimationFrame(() => {
      setTimeout(() => { this.ready = true }, 60)
    })
  },
  methods: {
    async fetchPermissions() {
      this.loading = true
      try {
        const response = await queryRoles({}, 1, 1000)
        this.permissions = response.data?.data || []
      } catch (error) {
        console.error('Failed to fetch permissions:', error)
      } finally {
        this.loading = false
      }
    },
    handleTypeChange() {
      // Type filter is handled by computed property
    },
    handleSearch() {
      // Search is handled by computed property
    },
    handleAddRole() {
      this.isEditing = false
      this.currentPermission = null
      this.formData = {
        id: null,
        role: '',
        name: '',
        type: 1,
        description: '',
        flag: 1
      }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.permissionForm && this.$refs.permissionForm.clearValidate()
      })
    },
    handleEditRole(permission) {
      this.isEditing = true
      this.currentPermission = permission
      this.formData = {
        id: permission.id,
        role: permission.role,
        name: permission.name,
        type: permission.type,
        description: permission.description,
        flag: permission.flag
      }
      this.dialogVisible = true
      this.$nextTick(() => {
        this.$refs.permissionForm && this.$refs.permissionForm.clearValidate()
      })
    },
    async handleSubmit() {
      try {
        await this.$refs.permissionForm.validate()
        this.saving = true

        await saveRole(this.formData)

        // Refresh permissions list
        await this.fetchPermissions()

        this.dialogVisible = false
        this.$message.success(this.$t('common.success'))
      } catch (error) {
        if (error !== false) {
          console.error('Failed to save permission:', error)
        }
      } finally {
        this.saving = false
      }
    },
    async handleDeleteRole(permission) {
      try {
        await this.$confirm(
          this.$t('role.deleteConfirmText', { name: permission.name }),
          this.$t('common.tip'),
          {
            confirmButtonText: this.$t('common.confirm'),
            cancelButtonText: this.$t('common.cancel'),
            type: 'warning'
          }
        )

        await deleteRole(permission.id)
        await this.fetchPermissions()
        this.$message.success(this.$t('common.success'))
      } catch (error) {
        if (error !== 'cancel') {
          console.error('Failed to delete permission:', error)
        }
      }
    },
    async handleToggleStatus(permission) {
      const isActive = permission.flag === 1
      const confirmMessage = isActive ? this.$t('role.confirmDisable') : this.$t('role.confirmEnable')

      try {
        await this.$confirm(confirmMessage, this.$t('common.tip'), {
          confirmButtonText: this.$t('common.confirm'),
          cancelButtonText: this.$t('common.cancel'),
          type: 'warning'
        })

        if (isActive) {
          await disableRole(permission.id)
        } else {
          await activateRole(permission.id)
        }

        await this.fetchPermissions()
        this.$message.success(this.$t('common.success'))
      } catch (error) {
        if (error !== 'cancel') {
          console.error('Failed to toggle permission status:', error)
        }
      }
    },
    handleCommand(command, permission) {
      switch (command) {
        case 'edit':
          this.handleEditRole(permission)
          break
        case 'enable':
        case 'disable':
          this.handleToggleStatus(permission)
          break
        case 'delete':
          this.handleDeleteRole(permission)
          break
      }
    },
    getTypeLabel(type) {
      const typeMap = {
        1: this.$t('role.menuPermission'),
        2: this.$t('role.dataPermission'),
        3: this.$t('role.apiPermission')
      }
      return typeMap[type] || this.$t('role.unknownType')
    },
    getTypeIcon(type) {
      const iconMap = {
        1: 'fas fa-bars',
        2: 'fas fa-database',
        3: 'fas fa-plug'
      }
      return iconMap[type] || 'fas fa-question'
    },
    getTypeBadgeClass(type) {
      const classMap = {
        1: 'type-badge--menu',
        2: 'type-badge--data',
        3: 'type-badge--api'
      }
      return classMap[type] || 'type-badge--unknown'
    },
    getStatusLabel(flag) {
      const statusMap = {
        0: this.$t('role.inactive'),
        1: this.$t('role.active'),
        2: this.$t('role.system')
      }
      return statusMap[flag] || this.$t('role.unknownStatus')
    },
    getStatusClass(flag) {
      const classMap = {
        0: 'status--inactive',
        1: 'status--active',
        2: 'status--system'
      }
      return classMap[flag] || 'status--unknown'
    },
    getPermissionCardClass(permission) {
      return {
        'permission-card--active': permission.flag === 1,
        'permission-card--inactive': permission.flag === 0,
        'permission-card--system': permission.flag === 2
      }
    },
    formatDate(dateString) {
      if (!dateString) return ''
      const date = new Date(dateString)
      return date.toLocaleDateString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
      })
    }
  }
}
</script>

<style lang="scss" scoped>
@import url('https://fonts.googleapis.com/css2?family=DM+Sans:ital,opsz,wght@0,9..40,300;0,9..40,400;0,9..40,500;0,9..40,600;0,9..40,700;1,9..40,400&family=Outfit:wght@300;400;500;600;700;800&display=swap');

/* ── Palette ── */
$bg: #f5f5f7;
$surface: rgba(255, 255, 255, 0.72);
$surface-h: rgba(255, 255, 255, 0.85);
$border: rgba(0, 0, 0, 0.06);
$border-h: rgba(0, 0, 0, 0.1);
$primary: #1E40AF;
$primary-l: #3B82F6;
$primary-bg: rgba(30, 64, 175, 0.06);
$amber: #F97316;
$amber-l: #FB923C;
$amber-bg: rgba(249, 115, 22, 0.07);
$amber-bd: rgba(249, 115, 22, 0.18);
$green: #06B6D4;
$green-bg: rgba(6, 182, 212, 0.07);
$green-bd: rgba(6, 182, 212, 0.18);
$purple: #F43F5E;
$purple-bg: rgba(244, 63, 94, 0.06);
$purple-bd: rgba(244, 63, 94, 0.18);
$red: #EF4444;
$red-bg: rgba(239, 68, 68, 0.06);
$red-bd: rgba(239, 68, 68, 0.18);
$text-1: #1a1d23;
$text-2: #5a6072;
$text-3: #8e95a9;
$radius: 18px;
$radius-sm: 12px;
$radius-xs: 8px;
$ease: cubic-bezier(0.22, 1, 0.36, 1);
$ease-out: cubic-bezier(0.16, 1, 0.3, 1);
$font-display: 'Outfit', -apple-system, BlinkMacSystemFont, sans-serif;
$font-body: 'DM Sans', -apple-system, BlinkMacSystemFont, 'PingFang SC', sans-serif;
$font-mono: 'SF Mono', 'Fira Code', 'JetBrains Mono', Consolas, monospace;

.permission-control-panel {
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
  background: radial-gradient(circle, rgba(6, 182, 212, 0.10) 0%, transparent 70%);
  animation: orb-drift 22s ease-in-out infinite 3s;
}

.bg__grain {
  position: absolute;
  inset: 0;
  opacity: 0.35;
  background-image: url("data:image/svg+xml,%3Csvg viewBox='0 0 256 256' xmlns='http://www.w3.org/2000/svg'%3E%3Cfilter id='n'%3E%3CfeTurbulence type='fractalNoise' baseFrequency='0.9' numOctaves='4' stitchTiles='stitch'/%3E%3C/filter%3E%3Crect width='100%25' height='100%25' filter='url(%23n)' opacity='0.04'/%3E%3C/svg%3E");
  background-size: 200px 200px;
}

/* ── Panel Inner ── */
.panel-inner {
  position: relative;
  z-index: 1;
  max-width: 100%;
  margin: 0;
  padding: 20px 24px 24px;
  display: flex;
  flex-direction: column;
  min-height: 100%;
  box-sizing: border-box;
  gap: 20px;
}

/* ── Header Section ── */
.panel-header {
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
  padding: 24px 28px;
  background: $surface;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: $radius;
  border: 1px solid $border;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.02), 0 4px 16px rgba(0, 0, 0, 0.02);

  @media (max-width: 768px) {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: linear-gradient(135deg, $primary 0%, #1E3A8A 60%, $purple 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 24px;
  box-shadow: 0 8px 32px rgba(30, 64, 175, 0.25);
}

.header-title {
  font-family: $font-display;
  font-size: 28px;
  font-weight: 800;
  margin: 0 0 4px 0;
  color: $text-1;
  letter-spacing: -0.5px;
  background: linear-gradient(135deg, $text-1 0%, #374151 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.header-subtitle {
  font-size: 14px;
  color: $text-3;
  margin: 0;
  letter-spacing: 0.2px;
}

.add-btn {
  background: linear-gradient(135deg, $primary 0%, #1E3A8A 100%);
  border: none;
  border-radius: 12px;
  padding: 12px 24px;
  font-weight: 600;
  letter-spacing: 0.3px;
  box-shadow: 0 4px 16px rgba(30, 64, 175, 0.3);
  transition: all 0.3s $ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 24px rgba(30, 64, 175, 0.4);
  }

  i {
    margin-right: 8px;
  }
}

/* ── Stats Section ── */
.stats-section {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.6s $ease 0.2s;

  &._on {
    opacity: 1;
    transform: translateY(0);
  }

  @media (max-width: 1200px) {
    grid-template-columns: repeat(2, 1fr);
  }

  @media (max-width: 600px) {
    grid-template-columns: 1fr;
  }
}

.stat-card {
  background: $surface;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: $radius-sm;
  border: 1px solid $border;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  position: relative;
  overflow: hidden;
  transition: all 0.35s $ease;

  &:hover {
    transform: translateY(-3px);
    border-color: $border-h;
    box-shadow: 0 4px 24px rgba(0, 0, 0, 0.06), 0 1px 4px rgba(0, 0, 0, 0.04);
  }
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  flex-shrink: 0;

  &--total {
    background: $primary-bg;
    color: $primary;
    border: 1px solid rgba(10, 132, 255, 0.12);
  }

  &--active {
    background: $green-bg;
    color: $green;
    border: 1px solid $green-bd;
  }

  &--inactive {
    background: $red-bg;
    color: $red;
    border: 1px solid $red-bd;
  }

  &--system {
    background: $amber-bg;
    color: $amber;
    border: 1px solid $amber-bd;
  }
}

.stat-info {
  flex: 1;
  min-width: 0;
}

.stat-value {
  font-family: $font-display;
  font-size: 28px;
  font-weight: 700;
  color: $text-1;
  line-height: 1;
  letter-spacing: -0.5px;
}

.stat-label {
  font-size: 13px;
  color: $text-3;
  margin-top: 4px;
  letter-spacing: 0.2px;
}

.stat-indicator {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: $text-3;
  opacity: 0.5;

  &--active {
    background: $green;
    box-shadow: 0 0 0 3px rgba(6, 182, 212, 0.2);
    opacity: 1;
  }

  &--inactive {
    background: $red;
    box-shadow: 0 0 0 3px rgba(239, 68, 68, 0.2);
    opacity: 1;
  }

  &--system {
    background: $amber;
    box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.2);
    opacity: 1;
  }
}

/* ── Filter Section ── */
.filter-section {
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.6s $ease 0.3s;

  &._on {
    opacity: 1;
    transform: translateY(0);
  }
}

.filter-content {
  display: flex;
  gap: 16px;
  padding: 16px 20px;
  background: $surface;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: $radius-sm;
  border: 1px solid $border;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.02), 0 4px 16px rgba(0, 0, 0, 0.02);

  @media (max-width: 768px) {
    flex-direction: column;
  }
}

.filter-left {
  flex: 1;
  min-width: 200px;
}

.filter-right {
  flex: 2;
}

.type-filter,
.search-input {
  width: 100%;

  ::v-deep .el-input__inner {
    border-radius: $radius-xs;
    border: 1px solid $border;
    background: rgba(255, 255, 255, 0.5);
    transition: all 0.3s $ease;

    &:focus {
      border-color: $primary;
      box-shadow: 0 0 0 3px rgba(30, 64, 175, 0.1);
    }
  }
}

/* ── Permissions Grid ── */
.permissions-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.6s $ease 0.4s;

  &._on {
    opacity: 1;
    transform: translateY(0);
  }
}

.permission-card {
  background: $surface;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: $radius-sm;
  border: 1px solid $border;
  padding: 20px;
  position: relative;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.35s $ease;
  animation: card-fade-in 0.6s $ease-out forwards;
  opacity: 0;

  &:hover {
    transform: translateY(-4px);
    border-color: $border-h;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08), 0 2px 8px rgba(0, 0, 0, 0.04);

    .card-glow {
      opacity: 1;
    }
  }

  &--active {
    border-left: 4px solid $green;
  }

  &--inactive {
    border-left: 4px solid $red;
    opacity: 0.7;
  }

  &--system {
    border-left: 4px solid $amber;
  }
}

.card-glow {
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at 50% 50%, rgba(30, 64, 175, 0.05) 0%, transparent 70%);
  opacity: 0;
  transition: opacity 0.3s $ease;
  pointer-events: none;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.permission-type-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;

  &.type-badge--menu {
    background: $primary-bg;
    color: $primary;
    border: 1px solid rgba(10, 132, 255, 0.12);
  }

  &.type-badge--data {
    background: $green-bg;
    color: $green;
    border: 1px solid $green-bd;
  }

  &.type-badge--api {
    background: $amber-bg;
    color: $amber;
    border: 1px solid $amber-bd;
  }

  &.type-badge--unknown {
    background: rgba(142, 142, 147, 0.1);
    color: #8e8e93;
    border: 1px solid rgba(142, 142, 147, 0.18);
  }
}

.permission-status {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  font-weight: 500;

  &.status--active {
    color: $green;
  }

  &.status--inactive {
    color: $red;
  }

  &.status--system {
    color: $amber;
  }

  &.status--unknown {
    color: $text-3;
  }
}

.status-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: currentColor;
  box-shadow: 0 0 0 2px rgba(0, 0, 0, 0.1);
}

.card-content {
  margin-bottom: 16px;
}

.permission-name {
  font-family: $font-display;
  font-size: 18px;
  font-weight: 700;
  color: $text-1;
  margin: 0 0 8px 0;
  letter-spacing: -0.3px;
  line-height: 1.3;
}

.permission-code {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: $text-3;
  margin-bottom: 8px;
  font-family: $font-mono;

  i {
    font-size: 12px;
    opacity: 0.7;
  }
}

.permission-description {
  font-size: 14px;
  color: $text-2;
  margin: 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 16px;
  border-top: 1px solid $border;
}

.card-meta {
  display: flex;
  gap: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: $text-3;

  i {
    font-size: 11px;
    opacity: 0.7;
  }
}

.card-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid $border;
  display: flex;
  align-items: center;
  justify-content: center;
  color: $text-2;
  transition: all 0.2s $ease;

  &:hover {
    background: rgba(255, 255, 255, 0.8);
    border-color: $border-h;
    color: $text-1;
  }
}

/* ── Empty State ── */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  text-align: center;
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.6s $ease 0.4s;

  &._on {
    opacity: 1;
    transform: translateY(0);
  }
}

.empty-icon {
  width: 80px;
  height: 80px;
  border-radius: 20px;
  background: $primary-bg;
  border: 1px solid rgba(30, 64, 175, 0.12);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  color: $primary;
  margin-bottom: 20px;
}

.empty-title {
  font-family: $font-display;
  font-size: 20px;
  font-weight: 700;
  color: $text-1;
  margin: 0 0 8px 0;
}

.empty-description {
  font-size: 14px;
  color: $text-3;
  margin: 0 0 24px 0;
  max-width: 400px;
}

/* ── Loading State ── */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.6s $ease 0.4s;

  &._on {
    opacity: 1;
    transform: translateY(0);
  }
}

.loading-spinner {
  position: relative;
  width: 60px;
  height: 60px;
  margin-bottom: 20px;
}

.spinner-ring {
  position: absolute;
  inset: 0;
  border: 3px solid transparent;
  border-top-color: $primary;
  border-radius: 50%;
  animation: spin 1.2s linear infinite;

  &:nth-child(2) {
    inset: 6px;
    border-top-color: $amber;
    animation-delay: -0.4s;
  }

  &:nth-child(3) {
    inset: 12px;
    border-top-color: $green;
    animation-delay: -0.8s;
  }
}

.loading-text {
  font-size: 14px;
  color: $text-3;
  margin: 0;
}

/* ── Dialog Styles ── */
.permission-dialog {
  ::v-deep .el-dialog {
    border-radius: $radius;
    overflow: hidden;
  }

  ::v-deep .el-dialog__header {
    background: linear-gradient(135deg, $primary 0%, #1E3A8A 100%);
    padding: 20px 24px;
    margin: 0;
  }

  ::v-deep .el-dialog__title {
    color: white;
    font-family: $font-display;
    font-weight: 700;
    font-size: 18px;
  }

  ::v-deep .el-dialog__headerbtn .el-dialog__close {
    color: white;
  }

  ::v-deep .el-dialog__body {
    padding: 24px;
  }

  ::v-deep .el-dialog__footer {
    padding: 16px 24px;
    background: #f8f9fa;
    border-top: 1px solid $border;
  }
}

.permission-form {
  .el-form-item {
    margin-bottom: 20px;
  }

  .el-input__inner,
  .el-textarea__inner {
    border-radius: $radius-xs;
    border: 1px solid $border;
    transition: all 0.3s $ease;

    &:focus {
      border-color: $primary;
      box-shadow: 0 0 0 3px rgba(30, 64, 175, 0.1);
    }
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* ── Animations ── */
@keyframes orb-drift {
  0%, 100% {
    transform: translate(0, 0) rotate(0deg);
  }
  25% {
    transform: translate(20px, -30px) rotate(90deg);
  }
  50% {
    transform: translate(-15px, 20px) rotate(180deg);
  }
  75% {
    transform: translate(25px, 15px) rotate(270deg);
  }
}

@keyframes card-fade-in {
  from {
    opacity: 0;
    transform: translateY(20px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 0.5;
  }
  50% {
    opacity: 1;
  }
}
</style>

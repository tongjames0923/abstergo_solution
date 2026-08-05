<template>
  <div class="user-permission-panel">
    <!-- Atmospheric background -->
    <div class="bg">
      <div class="bg__orb bg__orb--1" />
      <div class="bg__orb bg__orb--2" />
      <div class="bg__grain" />
    </div>

    <div class="panel-inner">
      <!-- Header section -->
      <header class="panel-header" :class="{ _on: ready }">
        <div class="header-content">
          <div class="header-left">
            <div class="header-icon">
              <i class="fas fa-user-shield" />
            </div>
            <div class="header-text">
              <h1 class="header-title">{{ $t('user.permissionPanel') }}</h1>
              <p class="header-subtitle">{{ $t('user.permissionPanelSubtitle') }}</p>
            </div>
          </div>
          <div class="header-actions">
            <el-button
              class="refresh-btn"
              @click="refreshPermissions"
            >
              <i class="fas fa-sync-alt" />
              <span>{{ $t('user.refreshPermissions') }}</span>
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
            <div class="stat-label">{{ $t('user.totalPermissions') }}</div>
          </div>
          <div class="stat-indicator" />
        </div>
        <div class="stat-card">
          <div class="stat-icon stat-icon--assigned">
            <i class="fas fa-check-circle" />
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.assigned }}</div>
            <div class="stat-label">{{ $t('user.assignedCount') }}</div>
          </div>
          <div class="stat-indicator stat-indicator--assigned" />
        </div>
        <div class="stat-card">
          <div class="stat-icon stat-icon--available">
            <i class="fas fa-plus-circle" />
          </div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.available }}</div>
            <div class="stat-label">{{ $t('user.availableCount') }}</div>
          </div>
          <div class="stat-indicator stat-indicator--available" />
        </div>
      </div>

      <!-- Content sections -->
      <div class="content-section" :class="{ _on: ready }">
        <!-- Assigned permissions -->
        <div class="assigned-section">
          <div class="section-header">
            <div class="section-title">
              <div class="section-icon section-icon--assigned">
                <i class="fas fa-shield-alt" />
              </div>
              <h2 class="section-heading">{{ $t('user.assignedPermissions') }}</h2>
              <span class="section-count">({{ userRoles.length }})</span>
            </div>
          </div>
          <div class="assigned-content">
            <div v-if="userRoles.length > 0" class="assigned-grid">
              <div
                v-for="role in userRoles"
                :key="role.id"
                class="assigned-card"
                :class="getTypeClass(role.type)"
              >
                <div class="assigned-card-header">
                  <div class="assigned-type-badge">
                    <i :class="getTypeIcon(role.type)" />
                    <span>{{ getTypeLabel(role.type) }}</span>
                  </div>
                  <button class="assigned-remove-btn" @click="handleCancelPermission(role)">
                    <i class="fas fa-times" />
                  </button>
                </div>
                <div class="assigned-card-body">
                  <h4 class="assigned-name">{{ role.roleName }}</h4>
                  <div class="assigned-code">
                    <i class="fas fa-key" />
                    <span>{{ role.role }}</span>
                  </div>
                  <p class="assigned-desc">
                    {{ role.description || $t('role.noDescription') }}
                  </p>
                </div>
              </div>
            </div>
            <div v-else class="empty-assigned">
              <div class="empty-icon">
                <i class="fas fa-shield-alt" />
              </div>
              <p class="empty-text">{{ $t('user.noPermissions') }}</p>
            </div>
          </div>
        </div>

        <!-- Available permissions -->
        <div class="available-section">
          <div class="section-header">
            <div class="section-title">
              <div class="section-icon section-icon--available">
                <i class="fas fa-list" />
              </div>
              <h2 class="section-heading">{{ $t('user.availablePermissions') }}</h2>
              <span class="section-count">({{ totalRoles }})</span>
            </div>
            <div class="search-box">
              <el-input
                v-model="searchKeyword"
                :placeholder="$t('user.searchPermissionPlaceholder')"
                prefix-icon="el-icon-search"
                clearable
                @clear="handleSearch"
                @keyup.enter.native="handleSearch"
              />
            </div>
          </div>
          <div class="available-content">
            <div v-if="loading" class="loading-state">
              <div class="loading-spinner">
                <div class="spinner-ring" />
                <div class="spinner-ring" />
                <div class="spinner-ring" />
              </div>
              <p class="loading-text">{{ $t('user.loadingPermissions') }}</p>
            </div>
            <div v-else-if="filteredRoles.length > 0" class="available-grid">
              <div
                v-for="role in filteredRoles"
                :key="role.id"
                class="permission-card"
                :class="{ 'permission-card--assigned': hasRole(role.id) }"
              >
                <div class="card-header">
                  <div class="permission-type-badge" :class="getTypeBadgeClass(role.type)">
                    <i :class="getTypeIcon(role.type)" />
                    <span>{{ getTypeLabel(role.type) }}</span>
                  </div>
                  <div v-if="hasRole(role.id)" class="assigned-badge">
                    <i class="fas fa-check" />
                    <span>{{ $t('user.assignedCount') }}</span>
                  </div>
                </div>
                <div class="card-content">
                  <h3 class="permission-name">{{ role.roleName }}</h3>
                  <div class="permission-code">
                    <i class="fas fa-key" />
                    <span>{{ role.role }}</span>
                  </div>
                  <p class="permission-description">
                    {{ role.description || $t('role.noDescription') }}
                  </p>
                </div>
                <div class="card-footer">
                  <el-button
                    v-if="!hasRole(role.id)"
                    type="primary"
                    size="small"
                    class="assign-btn"
                    @click="handleAssignPermission(role)"
                  >
                    <i class="fas fa-plus" />
                    <span>{{ $t('user.assignPermission') }}</span>
                  </el-button>
                  <el-button
                    v-else
                    type="danger"
                    size="small"
                    class="cancel-btn"
                    @click="handleCancelPermission(role)"
                  >
                    <i class="fas fa-minus" />
                    <span>{{ $t('user.cancelPermission') }}</span>
                  </el-button>
                </div>
              </div>
            </div>
            <div v-else class="empty-available">
              <div class="empty-icon">
                <i class="fas fa-search" />
              </div>
              <p class="empty-text">{{ $t('user.noAvailablePermissions') }}</p>
              <el-button type="primary" @click="refreshPermissions">
                <i class="fas fa-sync-alt" />
                <span>{{ $t('user.refreshPermissions') }}</span>
              </el-button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { disableRoleForUser, enableRoleForUser, getRolesByUserIdAdmin, queryRoles } from '@/api/admin_user'

export default {
  name: 'UserPermissionPanel',
  props: {
    userId: {
      type: [Number, String],
      required: true
    }
  },
  data() {
    return {
      ready: false,
      loading: false,
      userRoles: [],
      availableRoles: [],
      searchKeyword: '',
      currentPage: 1,
      pageSize: 10,
      totalRoles: 0
    }
  },
  computed: {
    filteredRoles() {
      if (this.searchKeyword) {
        return this.availableRoles.filter(role =>
          role.role.toLowerCase().includes(this.searchKeyword.toLowerCase()) ||
          role.roleName.toLowerCase().includes(this.searchKeyword.toLowerCase())
        )
      }
      return this.availableRoles
    },
    stats() {
      const total = this.availableRoles.length
      const assigned = this.userRoles.length
      const available = total - assigned
      return { total, assigned, available }
    }
  },
  watch: {
    userId: {
      handler() {
        this.fetchUserRoles()
        this.fetchAllRoles()
      },
      immediate: true
    }
  },
  mounted() {
    requestAnimationFrame(() => {
      setTimeout(() => { this.ready = true }, 60)
    })
  },
  methods: {
    async fetchUserRoles() {
      if (this.userId === undefined) return
      this.loading = true
      try {
        const response = await getRolesByUserIdAdmin(this.userId)
        this.userRoles = response.data || []
      } catch (error) {
        console.error('获取用户权限失败:', error)
      } finally {
        this.loading = false
      }
    },
    async fetchAllRoles() {
      this.loading = true
      try {
        const response = await queryRoles({
          flagEquals: [1, 2],
          pageNum: this.currentPage,
          pageSize: this.pageSize
        })
        this.availableRoles = response.data?.data || []
        this.totalRoles = response.data?.total || 0
      } catch (error) {
        console.error('获取权限列表失败:', error)
      } finally {
        this.loading = false
      }
    },
    handleSearch() {
      this.currentPage = 1
      this.fetchAllRoles()
    },
    refreshPermissions() {
      this.fetchUserRoles()
      this.fetchAllRoles()
    },
    async handleAssignPermission(role) {
      try {
        await this.$confirm(this.$t('user.confirmAssign'), this.$t('common.tip'), {
          confirmButtonText: this.$t('common.confirm'),
          cancelButtonText: this.$t('common.cancel'),
          type: 'warning'
        })
        await enableRoleForUser(this.userId, role.id)
        this.userRoles.push({ ...role })
        this.$message.success(this.$t('user.permissionAssigned'))
      } catch (error) {
        if (error !== 'cancel') {
          console.error('分配权限失败:', error)
        }
      }
    },
    async handleCancelPermission(role) {
      try {
        await this.$confirm(this.$t('user.confirmCancel'), this.$t('common.tip'), {
          confirmButtonText: this.$t('common.confirm'),
          cancelButtonText: this.$t('common.cancel'),
          type: 'warning'
        })
        await disableRoleForUser(this.userId, role.id)
        this.userRoles = this.userRoles.filter(r => r.id !== role.id)
        this.$message.success(this.$t('user.permissionCancelled'))
      } catch (error) {
        if (error !== 'cancel') {
          console.error('取消权限失败:', error)
        }
      }
    },
    hasRole(roleId) {
      return this.userRoles.some(role => role.id === roleId)
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
    getTypeClass(type) {
      const classMap = {
        1: 'type--menu',
        2: 'type--data',
        3: 'type--api'
      }
      return classMap[type] || 'type--unknown'
    },
    getTypeBadgeClass(type) {
      const classMap = {
        1: 'type-badge--menu',
        2: 'type-badge--data',
        3: 'type-badge--api'
      }
      return classMap[type] || 'type-badge--unknown'
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

.user-permission-panel {
  position: relative;
  min-height: 100%;
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
  width: 500px;
  height: 500px;
  top: -10%;
  left: -5%;
  background: radial-gradient(circle, rgba(30, 64, 175, 0.12) 0%, transparent 70%);
  animation: orb-drift 20s ease-in-out infinite;
}

.bg__orb--2 {
  width: 400px;
  height: 400px;
  bottom: -5%;
  right: -5%;
  background: radial-gradient(circle, rgba(6, 182, 212, 0.10) 0%, transparent 70%);
  animation: orb-drift 25s ease-in-out infinite reverse;
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
  padding: 20px 24px;
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
  width: 48px;
  height: 48px;
  border-radius: 14px;
  background: linear-gradient(135deg, $primary 0%, #1E3A8A 60%, $purple 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 20px;
  box-shadow: 0 8px 32px rgba(30, 64, 175, 0.25);
}

.header-title {
  font-family: $font-display;
  font-size: 22px;
  font-weight: 700;
  margin: 0 0 2px 0;
  color: $text-1;
  letter-spacing: -0.3px;
}

.header-subtitle {
  font-size: 13px;
  color: $text-3;
  margin: 0;
  letter-spacing: 0.2px;
}

.refresh-btn {
  background: rgba(255, 255, 255, 0.5);
  border: 1px solid $border;
  border-radius: $radius-xs;
  padding: 10px 16px;
  font-weight: 500;
  transition: all 0.3s $ease;

  &:hover {
    background: rgba(255, 255, 255, 0.8);
    border-color: $border-h;
  }

  i {
    margin-right: 6px;
  }
}

/* ── Stats Section ── */
.stats-section {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.6s $ease 0.2s;

  &._on {
    opacity: 1;
    transform: translateY(0);
  }
}

.stat-card {
  background: $surface;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: $radius-sm;
  border: 1px solid $border;
  padding: 16px 20px;
  display: flex;
  align-items: center;
  gap: 14px;
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
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  flex-shrink: 0;

  &--total {
    background: $primary-bg;
    color: $primary;
    border: 1px solid rgba(10, 132, 255, 0.12);
  }

  &--assigned {
    background: $green-bg;
    color: $green;
    border: 1px solid $green-bd;
  }

  &--available {
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
  font-size: 24px;
  font-weight: 700;
  color: $text-1;
  line-height: 1;
  letter-spacing: -0.5px;
}

.stat-label {
  font-size: 12px;
  color: $text-3;
  margin-top: 4px;
  letter-spacing: 0.2px;
}

.stat-indicator {
  position: absolute;
  top: 10px;
  right: 10px;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: $text-3;
  opacity: 0.5;

  &--assigned {
    background: $green;
    box-shadow: 0 0 0 3px rgba(6, 182, 212, 0.2);
    opacity: 1;
  }

  &--available {
    background: $amber;
    box-shadow: 0 0 0 3px rgba(249, 115, 22, 0.2);
    opacity: 1;
  }
}

/* ── Content Section ── */
.content-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.6s $ease 0.3s;

  &._on {
    opacity: 1;
    transform: translateY(0);
  }
}

/* ── Section Styles ── */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.section-icon {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;

  &--assigned {
    background: $green-bg;
    color: $green;
    border: 1px solid $green-bd;
  }

  &--available {
    background: $primary-bg;
    color: $primary;
    border: 1px solid rgba(30, 64, 175, 0.12);
  }
}

.section-heading {
  font-family: $font-display;
  font-size: 16px;
  font-weight: 700;
  color: $text-1;
  margin: 0;
  letter-spacing: -0.2px;
}

.section-count {
  font-size: 13px;
  color: $text-3;
  font-weight: 500;
}

.search-box {
  width: 280px;

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

/* ── Assigned Section ── */
.assigned-section {
  background: $surface;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: $radius;
  border: 1px solid $border;
  padding: 20px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.02), 0 4px 16px rgba(0, 0, 0, 0.02);
}

.assigned-content {
  min-height: 80px;
}

.assigned-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(260px, 1fr));
  gap: 12px;
}

.assigned-card {
  background: rgba(255, 255, 255, 0.6);
  border-radius: $radius-sm;
  border: 1px solid $border;
  padding: 14px;
  transition: all 0.35s $ease;
  animation: card-fade-in 0.4s $ease-out forwards;
  opacity: 0;

  &.type--menu {
    border-left: 3px solid $primary;
  }

  &.type--data {
    border-left: 3px solid $green;
  }

  &.type--api {
    border-left: 3px solid $amber;
  }

  &.type--unknown {
    border-left: 3px solid $text-3;
  }

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
    border-color: $border-h;
  }
}

.assigned-card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.assigned-type-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 600;
  background: rgba(0, 0, 0, 0.04);
  color: $text-2;
}

.assigned-remove-btn {
  width: 28px;
  height: 28px;
  border-radius: 6px;
  border: 1px solid $border;
  background: rgba(255, 255, 255, 0.8);
  color: $text-3;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  cursor: pointer;
  transition: all 0.2s $ease;

  &:hover {
    background: $red-bg;
    color: $red;
    border-color: $red-bd;
  }
}

.assigned-card-body {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.assigned-name {
  font-family: $font-display;
  font-size: 15px;
  font-weight: 700;
  color: $text-1;
  margin: 0;
  letter-spacing: -0.2px;
  line-height: 1.3;
}

.assigned-code {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: $text-3;
  font-family: $font-mono;

  i {
    font-size: 11px;
    opacity: 0.7;
  }
}

.assigned-desc {
  font-size: 12px;
  color: $text-2;
  margin: 4px 0 0 0;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.empty-assigned {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 30px;
  text-align: center;
}

.empty-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: $primary-bg;
  border: 1px solid rgba(30, 64, 175, 0.12);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  color: $primary;
  margin-bottom: 12px;
}

.empty-text {
  font-size: 14px;
  color: $text-3;
  margin: 0;
}

/* ── Available Section ── */
.available-section {
  background: $surface;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: $radius;
  border: 1px solid $border;
  padding: 20px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.02), 0 4px 16px rgba(0, 0, 0, 0.02);
}

.available-content {
  min-height: 200px;
}

.available-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;
}

.permission-card {
  background: rgba(255, 255, 255, 0.5);
  border-radius: $radius-sm;
  border: 1px solid $border;
  padding: 16px;
  transition: all 0.35s $ease;
  animation: card-fade-in 0.6s $ease-out forwards;
  opacity: 0;

  &:hover {
    transform: translateY(-3px);
    border-color: $border-h;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08), 0 2px 8px rgba(0, 0, 0, 0.04);
  }

  &--assigned {
    border-color: $green;
    border-width: 2px;
    background: $green-bg;
  }
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.permission-type-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 11px;
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

.assigned-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 11px;
  font-weight: 600;
  color: $green;
  padding: 4px 8px;
  background: $green-bg;
  border-radius: 6px;
  border: 1px solid $green-bd;
}

.card-content {
  margin-bottom: 12px;
}

.permission-name {
  font-family: $font-display;
  font-size: 16px;
  font-weight: 700;
  color: $text-1;
  margin: 0 0 6px 0;
  letter-spacing: -0.2px;
  line-height: 1.3;
}

.permission-code {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: $text-3;
  margin-bottom: 6px;
  font-family: $font-mono;

  i {
    font-size: 11px;
    opacity: 0.7;
  }
}

.permission-description {
  font-size: 13px;
  color: $text-2;
  margin: 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  padding-top: 12px;
  border-top: 1px solid $border;
}

.assign-btn,
.cancel-btn {
  width: 100%;
  border-radius: $radius-xs;
  font-weight: 600;
  letter-spacing: 0.3px;
  transition: all 0.3s $ease;

  &:hover {
    transform: translateY(-1px);
  }

  i {
    margin-right: 6px;
  }
}

.assign-btn {
  background: linear-gradient(135deg, $primary 0%, #1E3A8A 100%);
  border: none;
  box-shadow: 0 4px 16px rgba(30, 64, 175, 0.3);

  &:hover {
    box-shadow: 0 6px 24px rgba(30, 64, 175, 0.4);
  }
}

.cancel-btn {
  background: linear-gradient(135deg, $red 0%, #B91C1C 100%);
  border: none;
  box-shadow: 0 4px 16px rgba(239, 68, 68, 0.3);

  &:hover {
    box-shadow: 0 6px 24px rgba(239, 68, 68, 0.4);
  }
}

/* ── Loading State ── */
.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
}

.loading-spinner {
  position: relative;
  width: 50px;
  height: 50px;
  margin-bottom: 16px;
}

.spinner-ring {
  position: absolute;
  inset: 0;
  border: 3px solid transparent;
  border-top-color: $primary;
  border-radius: 50%;
  animation: spin 1.2s linear infinite;

  &:nth-child(2) {
    inset: 5px;
    border-top-color: $amber;
    animation-delay: -0.4s;
  }

  &:nth-child(3) {
    inset: 10px;
    border-top-color: $green;
    animation-delay: -0.8s;
  }
}

.loading-text {
  font-size: 13px;
  color: $text-3;
  margin: 0;
}

/* ── Empty Available State ── */
.empty-available {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  text-align: center;
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

@keyframes tag-fade-in {
  from {
    opacity: 0;
    transform: scale(0.9) translateY(10px);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0);
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
</style>

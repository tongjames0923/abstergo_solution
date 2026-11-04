
<template>
  <div class="navbar-macos">
    <hamburger :is-active="sidebar.opened" class="hamburger-container" @toggleClick="toggleSideBar" />

    <breadcrumb class="breadcrumb-container" />

    <div class="right-menu">
      <!-- 推送消息：铃铛 + 未读红点 + 连接状态 -->
      <div class="push-bell" @click="openCenter">
        <span
          class="push-bell__status"
          :class="'is-' + connectionStatus"
          :title="$t('push.status.' + connectionStatus)"
        />
        <el-badge :value="unreadCount" :max="99" :hidden="!unreadCount" class="push-bell__badge">
          <i class="el-icon-bell" />
        </el-badge>
      </div>

      <!-- 非健康/过渡态且会话仍有效（有 token）时提供手动重连入口：
           reconnect_failed（自动重连耗尽）与 idle/closed（未连/断线）均可点；
           登录失效（无 token）不展示，由全局鉴权流程引导重新登录 -->
      <el-button
        v-if="canReconnect"
        class="push-reconnect-btn"
        size="mini"
        type="danger"
        icon="el-icon-refresh"
        @click="handleReconnect"
      >{{ $t('push.reconnect') }}</el-button>

      <push-notification-center :visible.sync="centerVisible" />

      <!-- 语言切换 -->
      <lang-select class="lang-select-container" />

      <!-- 用户信息 -->
      <div class="user-info">
        <el-tooltip v-if="currentDepartment" effect="light" placement="bottom" popper-class="user-tooltip">
          <div class="user-dept">
            <i class="el-icon-office-building" />
            <span>{{ currentDepartment.departmentName }}</span>
          </div>
          <div slot="content" class="tooltip-content">
            <div class="tooltip-title">当前部门</div>
            <div class="tooltip-item">
              <span class="tooltip-label">部门名称</span>
              <span class="tooltip-value">{{ currentDepartment.departmentName }}</span>
            </div>
            <div v-if="currentDepartment.companyName" class="tooltip-item">
              <span class="tooltip-label">所属公司</span>
              <span class="tooltip-value">{{ currentDepartment.companyName }}</span>
            </div>
          </div>
        </el-tooltip>

        <el-tooltip v-if="allRoleGroups.length > 0" effect="light" placement="bottom" popper-class="user-tooltip">
          <div class="user-role">
            <i class="el-icon-s-check" />
            <span class="role-tag">
              {{ allRoleGroups[0].group.roleGroupName }}
            </span>
            <span v-if="allRoleGroups.length > 1" class="role-count">+{{ allRoleGroups.length - 1 }}</span>
          </div>
          <div slot="content" class="tooltip-content">
            <div class="tooltip-title">权限组 ({{ allRoleGroups.length }})</div>
            <div v-for="(vg, index) in allRoleGroups" :key="index" class="tooltip-item">
              <span class="tooltip-value">{{ vg.group.roleGroupName }}</span>
              <span class="tooltip-label" style="min-width:auto;color:#999">({{ vg.permissions.length }} 个权限)</span>
            </div>
          </div>
        </el-tooltip>
      </div>

      <el-dropdown class="avatar-container" trigger="click">
        <div class="avatar-wrapper">
          <div class="avatar-with-name">
            <img :src="avatar+'?imageView2/1/w/80/h/80'" class="user-avatar">
            <div class="user-name-wrapper">
              <span class="user-display-name">{{ displayName }}</span>
              <span v-if="accountStatusStr" class="user-status" :class="statusClass">{{ accountStatusStr }}</span>
            </div>
          </div>
          <i class="el-icon-caret-bottom" />
        </div>
        <el-dropdown-menu slot="dropdown" class="user-dropdown">
          <div class="dropdown-user-info">
            <img :src="avatar+'?imageView2/1/w/80/h/80'" class="dropdown-avatar">
            <div class="dropdown-user-detail">
              <span class="dropdown-user-name">{{ displayName }}</span>
              <span class="dropdown-user-username">@{{ userName }}</span>
            </div>
          </div>
          <el-dropdown-item divided>
            <router-link to="/">
              <span style="display:block;">{{ $t('nav.home') }}</span>
            </router-link>
          </el-dropdown-item>
          <el-dropdown-item divided @click.native="showAbout">
            <span style="display:block;">{{ $t('nav.about') }}</span>
          </el-dropdown-item>
          <el-dropdown-item divided @click.native="logout">
            <span style="display:block;">{{ $t('login.logout') }}</span>
          </el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>
    </div>

    <!-- 关于系统：展示后端网关构建信息 -->
    <el-dialog
      :title="$t('about.title')"
      :visible.sync="aboutVisible"
      width="360px"
      custom-class="about-dialog"
      append-to-body
    >
      <div v-loading="aboutLoading" class="about-content">
        <div class="about-logo">
          <span class="about-monogram">A</span>
          <span class="about-name">{{ systemTitle }}</span>
          <span v-if="versionInfo.version" class="about-version-badge">v{{ versionInfo.version }}</span>
        </div>
        <div class="about-row">
          <span class="about-label">{{ $t('about.version') }}</span>
          <span class="about-value">{{ versionInfo.version || '-' }}</span>
        </div>
        <div class="about-row">
          <span class="about-label">{{ $t('about.artifact') }}</span>
          <span class="about-value">{{ versionInfo.artifact || '-' }}</span>
        </div>
        <div class="about-row">
          <span class="about-label">{{ $t('about.buildTime') }}</span>
          <span class="about-value">{{ formattedBuildTime }}</span>
        </div>
        <div class="about-row">
          <span class="about-label">{{ $t('about.instancePort') }}</span>
          <span class="about-value">{{ versionInfo.instancePort || '-' }}</span>
        </div>

        <!-- 视图路由版本区块 -->
        <div class="view-version-block">
          <div class="view-version-head">
            <span class="view-version-title">
              <i class="el-icon-view" /> {{ $t('about.viewVersion') }}
            </span>
            <el-tag
              v-if="hasViewVersionInfo"
              size="mini"
              :type="viewUpToDate ? 'success' : 'warning'"
              effect="plain"
            >
              {{ viewUpToDate ? $t('about.latest') : $t('about.hasUpdate') }}
            </el-tag>
          </div>
          <div class="about-row">
            <span class="about-label">{{ $t('about.serverVersion') }}</span>
            <span class="about-value">{{ serverViewVersion || '-' }}</span>
          </div>
          <div class="about-row">
            <span class="about-label">{{ $t('about.localVersion') }}</span>
            <span class="about-value">{{ localViewVersion || '-' }}</span>
          </div>
          <el-button
            v-if="!viewUpToDate && hasViewVersionInfo"
            class="update-view-btn"
            size="mini"
            type="primary"
            plain
            :loading="updatingView"
            @click="handleUpdateView"
          >
            <i v-if="!updatingView" class="el-icon-refresh" /> {{ $t('about.updateView') }}
          </el-button>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script>
import {mapGetters} from 'vuex'
import Breadcrumb from '@/components/Breadcrumb'
import Hamburger from '@/components/Hamburger'
import LangSelect from '@/components/LangSelect'
import PushNotificationCenter from '@/components/PushNotificationCenter'
import sidebar from '@/layout/components/Sidebar/index.vue'
import {getGatewayVersion} from '@/api/system'
import {getToken} from '@/utils/auth'
import routeCacheService from '@/utils/route-cache'
import {refreshRoutes} from '@/router'
import settings from '@/settings'

export default {
  name: 'Navbar',
  components: {
    Breadcrumb,
    Hamburger,
    LangSelect,
    PushNotificationCenter
  },
  data() {
    return {
      // 消息中心抽屉可见性
      centerVisible: false,
      // 关于系统弹窗状态与后端版本信息
      aboutVisible: false,
      aboutLoading: false,
      versionInfo: {},
      updatingView: false
    }
  },
  computed: {
    systemTitle() {
      return settings.title || ''
    },
    // 构建时间可能为 unknown 或 ISO 格式，统一展示为本地时间或原始值
    formattedBuildTime() {
      const raw = this.versionInfo.buildTime
      if (!raw || raw === 'unknown') {
        return '-'
      }
      const date = new Date(raw)
      if (isNaN(date.getTime())) {
        return raw
      }
      return date.toLocaleString()
    },
    // 服务端视图版本
    serverViewVersion() {
      return this.versionInfo.viewVersion || ''
    },
    // 本地缓存的视图版本（实际拉取路由时记录）
    localViewVersion() {
      return routeCacheService.getLocalVersion() || ''
    },
    // 两端版本信息是否齐备（任一缺失时不展示比对状态）
    hasViewVersionInfo() {
      return !!this.serverViewVersion && this.serverViewVersion !== '0' && !!this.localViewVersion
    },
    // 版本号不相等即视为有更新
    viewUpToDate() {
      return this.serverViewVersion === this.localViewVersion
    },
    sidebar() {
      return sidebar
    },
    displayName() {
      return this.name || this.userName || '用户'
    },
    statusClass() {
      if (this.accountStatus === 1) return 'status-active'
      if (this.accountStatus === 2) return 'status-locked'
      return 'status-disabled'
    },
    // 获取权限组列表（用于导航栏显示）
    allRoleGroups() {
      return this.$store.state.user.roleGroups || []
    },
    ...mapGetters([
      'sidebar',
      'avatar',
      'name',
      'userName',
      'currentDepartment',
      'accountStatus',
      'accountStatusStr'
    ]),
    ...mapGetters('push', [
      'pushConnectionStatus',
      'pushUnreadCount'
    ]),
    connectionStatus() {
      return this.pushConnectionStatus
    },
    /**
     * 是否展示手动重连按钮：连接处于非健康且非过渡态时给予用户手动救回的入口。
     * <p>
     * reconnect_failed（自动重连耗尽）恒可重连；idle/closed 仅当登录态仍在
     * （有 token）才展示，避免登出/会话失效时空按钮误导（那种情况应重新登录）。
     * </p>
     */
    canReconnect() {
      const status = this.connectionStatus
      if (status === 'connected' || status === 'connecting' || status === 'reconnecting') {
        return false
      }
      if (status === 'reconnect_failed') {
        return true
      }
      // idle / closed：会话仍有效才允许手动重连
      return !!getToken()
    },
    unreadCount() {
      return this.pushUnreadCount
    }
  },
  watch: {
    // 自动重连次数耗尽：弹常驻错误通知（用户关闭或重连成功前不消失），醒目提示推送已停
    connectionStatus(status) {
      if (status === 'reconnect_failed') {
        this.$notify.error({
          title: this.$t('push.reconnectFailedTitle'),
          message: this.$t('push.reconnectFailedTip'),
          duration: 0
        })
      }
    }
  },
  methods: {
    toggleSideBar() {
      this.$store.dispatch('app/toggleSideBar')
    },
    // 手动重连推送连接（重连按钮/错误通知处触发）
    handleReconnect() {
      this.$store.dispatch('push/manualReconnect')
    },
    // 打开消息中心抽屉
    openCenter() {
      this.centerVisible = true
    },
    // 打开关于系统弹窗，拉取后端网关版本信息
    async showAbout() {
      this.aboutVisible = true
      this.aboutLoading = true
      try {
        const res = await getGatewayVersion()
        this.versionInfo = (res && res.data) || {}
      } catch (e) {
        this.versionInfo = {}
        this.$message.error(this.$t('about.fetchFailed'))
      } finally {
        this.aboutLoading = false
      }
    },
    // 更新视图：清除本地路由缓存并重新拉取路由（同时刷新本地版本号）
    async handleUpdateView() {
      this.updatingView = true
      try {
        await refreshRoutes()
        const res = await getGatewayVersion()
        this.versionInfo = (res && res.data) || this.versionInfo
        this.$message.success(this.$t('about.updateViewSuccess'))
      } catch (e) {
        console.error('更新视图失败:', e)
        this.$message.error(this.$t('about.updateViewFailed'))
      } finally {
        this.updatingView = false
      }
    },
    async logout() {
      await this.$store.dispatch('user/logout')
      this.$router.push(`/login?redirect=${encodeURIComponent(this.$route.fullPath)}`)
    },
    getRoleColor(index) {
      const colors = ['#0071e3', '#34c759', '#ff9500', '#5856d6', '#ff3b30', '#af52de']
      return colors[index % colors.length]
    },
    // 获取权限类型对应的样式类
    getRoleTypeClass(type) {
      const typeMap = {
        1: 'role-type-menu',
        2: 'role-type-data',
        3: 'role-type-api'
      }
      return typeMap[type] || 'role-type-default'
    },
    // 获取权限类型标签
    getRoleTypeLabel(type) {
      const typeMap = {
        1: '菜单',
        2: '数据',
        3: 'API'
      }
      return typeMap[type] || '未知'
    }
  }
}
</script>

<style lang="scss" scoped>
@import "~@/styles/variables.scss";

.navbar-macos {
  height: 44px;
  overflow: hidden;
  position: relative;
  /* macOS 毛玻璃质感：半透明白底 + 模糊 */
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: saturate(180%) blur(12px);
  -webkit-backdrop-filter: saturate(180%) blur(12px);
  box-shadow: 0 1px 0 rgba(0, 0, 0, 0.06);
  margin: 0;
  border-radius: 0;
  border-bottom: 1px solid #eaeaea;

  .hamburger-container {
    line-height: 40px;
    height: 100%;
    float: left;
    cursor: pointer;
    transition: background .2s;
    -webkit-tap-highlight-color: transparent;
    border-radius: 0;
    padding: 0 12px;

    &:hover {
      background: #fafafa;
    }
  }

  .breadcrumb-container {
    float: left;
    line-height: 44px;
    margin-left: 4px;
  }

  .right-menu {
    float: right;
    height: 100%;
    display: flex;
    align-items: center;
    gap: 10px;

    &:focus {
      outline: none;
    }

    .lang-select-container {
      display: flex;
      align-items: center;
      padding: 0 8px;
      cursor: pointer;
      color: #666;

      &:hover {
        color: #333;
      }
    }

    .push-bell {
      position: relative;
      display: flex;
      align-items: center;
      justify-content: center;
      width: 36px;
      height: 36px;
      border-radius: 8px;
      cursor: pointer;
      color: #5a5a5a;
      transition: background 0.2s, color 0.2s;

      &:hover {
        background: #f0f0f2;
        color: #1d1d1f;
      }

      .push-bell__badge {
        display: flex;
        align-items: center;

        ::v-deep .el-badge__content {
          border: none;
          box-shadow: 0 0 0 2px rgba(255, 255, 255, 0.82);
        }
      }

      .el-icon-bell {
        font-size: 18px;
      }

      /* 连接状态圆点：绿=已连 / 黄=重连中 / 灰=断开 / 蓝=连接中 / 红闪烁=重连失败待手动 */
      .push-bell__status {
        position: absolute;
        top: 6px;
        right: 6px;
        width: 8px;
        height: 8px;
        border-radius: 50%;
        background: #c0c4cc;
        transition: background 0.3s;

        &.is-connected {
          background: #34c759;
          box-shadow: 0 0 0 3px rgba(52, 199, 89, 0.18);
        }

        &.is-reconnect_failed {
          background: #ff3b30;
          animation: push-bell-pulse-red 1.2s infinite;
        }

        &.is-connecting,
        &.is-reconnecting {
          background: #ff9500;
          animation: push-bell-pulse 1.2s infinite;
        }

        &.is-closed,
        &.is-idle {
          background: #c0c4cc;
        }
      }
    }

    @keyframes push-bell-pulse {
      0% {
        box-shadow: 0 0 0 0 rgba(255, 149, 0, 0.4);
      }

      70% {
        box-shadow: 0 0 0 5px rgba(255, 149, 0, 0);
      }

      100% {
        box-shadow: 0 0 0 0 rgba(255, 149, 0, 0);
      }
    }

    @keyframes push-bell-pulse-red {
      0% {
        box-shadow: 0 0 0 0 rgba(255, 59, 48, 0.45);
      }

      70% {
        box-shadow: 0 0 0 5px rgba(255, 59, 48, 0);
      }

      100% {
        box-shadow: 0 0 0 0 rgba(255, 59, 48, 0);
      }
    }

    /* 重连失败后的手动重连按钮：导航栏内联，红色醒目 */
    .push-reconnect-btn {
      margin-left: -4px;
    }

    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      height: 100%;
    }

    .user-dept,
    .user-role {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 12px;
      color: #666;
      padding: 3px 8px;
      background: #f5f5f5;
      border-radius: 4px;
      white-space: nowrap;
      max-width: 160px;
      overflow: hidden;

      span {
        overflow: hidden;
        text-overflow: ellipsis;
      }

      i {
        font-size: 12px;
        color: #999;
        flex-shrink: 0;
      }
    }

    .role-tag {
      padding: 2px 6px;
      border-radius: 3px;
      font-size: 11px;
      font-weight: 500;
    }

    .role-count {
      font-size: 10px;
      background: #0071e3;
      color: white;
      padding: 1px 4px;
      border-radius: 3px;
      margin-left: 2px;
    }

    .avatar-container {
      margin-right: 12px;

      .avatar-wrapper {
        display: flex;
        align-items: center;
        gap: 8px;
        cursor: pointer;
        padding: 3px 8px;
        border-radius: 6px;
        transition: background .2s;

        &:hover {
          background: #fafafa;
        }

        .avatar-with-name {
          display: flex;
          align-items: center;
          gap: 8px;
        }

        .user-avatar {
          width: 28px;
          height: 28px;
          border-radius: 50%;
          border: 1px solid #eaeaea;
        }

        .user-name-wrapper {
          display: flex;
          flex-direction: column;
          line-height: 1.2;
        }

        .user-display-name {
          font-size: 13px;
          font-weight: 500;
          color: #333;
        }

        .user-status {
          font-size: 10px;
          font-weight: 500;

          &.status-active {
            color: #34c759;
          }

          &.status-locked {
            color: #ff9500;
          }

          &.status-disabled {
            color: #dc2626;
          }
        }

        .el-icon-caret-bottom {
          font-size: 10px;
          color: #999;
          margin-left: 4px;
        }
      }
    }
  }
}

.user-dropdown {
  .dropdown-user-info {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 12px 16px;
    border-bottom: 1px solid #eaeaea;

    .dropdown-avatar {
      width: 40px;
      height: 40px;
      border-radius: 50%;
      border: 1px solid #eaeaea;
    }

    .dropdown-user-detail {
      display: flex;
      flex-direction: column;

      .dropdown-user-name {
        font-size: 14px;
        font-weight: 600;
        color: #333;
      }

      .dropdown-user-username {
        font-size: 12px;
        color: #999;
      }
    }
  }
}

/* 移动端适配：平板收纳部门/角色信息，手机进一步精简导航，触摸目标 >= 44px */
@media only screen and (max-width: #{$bp-tablet}) {
  // 导航栏改 flex 布局：面包屑占据剩余宽度并按需裁切。
  // 原实现用 fixed right 预留值给右侧菜单让位，但右侧宽度随用户名/语言切换变化，预留值必然失准导致重叠
  .navbar-macos {
    display: flex;
    align-items: center;

    // 部门/角色标签小屏收纳进头像下拉，避免导航溢出
    .right-menu .user-info {
      display: none;
    }

    .breadcrumb-container {
      float: none;
      flex: 1 1 auto;
      min-width: 0;
      display: block;
      line-height: 44px;
      margin-left: 0;
      overflow: hidden;
      white-space: nowrap;
      text-overflow: ellipsis;
    }

    .right-menu {
      float: none;
      position: static;
      flex: 0 0 auto;
      margin-left: auto;
      z-index: 1;
    }

    // 长用户名不应撑开右侧菜单
    .user-name-wrapper {
      max-width: 96px;
      overflow: hidden;
    }

    // 触摸目标扩展：汉堡按钮保持 >= 44px 可点击区域
    .hamburger-container {
      float: none;
      flex: 0 0 auto;
      padding: 0 14px;
    }
  }
}

@media only screen and (max-width: #{$bp-phone}) {
  .navbar-macos {
    .right-menu {
      gap: 2px;
    }

    // 手机端隐藏语言切换与展开箭头，仅保留头像入口
    .right-menu .lang-select-container,
    .avatar-container .el-icon-caret-bottom {
      display: none;
    }

    .avatar-container {
      margin-right: 4px;

      .avatar-wrapper {
        // 44px 触摸目标
        min-height: 36px;
        padding: 6px 10px;
      }

      .user-name-wrapper {
        max-width: 72px;
        overflow: hidden;

        .user-display-name {
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }
    }
  }
}

// 手机横屏：导航栏压缩为浮层式，最大化内容高度
@media only screen and (orientation: landscape) and (max-width: #{$bp-phone-landscape}) {
  .navbar-macos {
    height: 38px;

    .breadcrumb-container {
      line-height: 38px;
    }

    .right-menu .avatar-container .avatar-wrapper {
      min-height: 30px;
      padding: 4px 8px;
    }
  }
}
</style>

<style lang="scss">
.user-tooltip {
  padding: 0 !important;
  border: 1px solid #eaeaea !important;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08) !important;
  border-radius: 8px !important;
  max-width: 280px !important;

  .tooltip-content {
    padding: 10px 14px;
  }

  .tooltip-title {
    font-size: 13px;
    font-weight: 600;
    color: #333;
    margin-bottom: 6px;
    padding-bottom: 6px;
    border-bottom: 1px solid #eaeaea;
  }

  .tooltip-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 3px 0;
    font-size: 12px;
  }

  .tooltip-label {
    color: #999;
    min-width: 60px;
  }

  .tooltip-value {
    color: #333;
    font-weight: 500;
  }

  .tooltip-dot {
    width: 8px;
    height: 8px;
    border-radius: 50%;
    flex-shrink: 0;
  }
}

/* 权限类型样式 */
.role-type-menu {
  background: rgba(0, 113, 227, 0.1);
  color: #0071e3;
}

.role-type-data {
  background: rgba(52, 199, 89, 0.1);
  color: #34c759;
}

.role-type-api {
  background: rgba(255, 149, 0, 0.1);
  color: #ff9500;
}

.role-type-default {
  background: rgba(142, 142, 147, 0.1);
  color: #8e8e93;
}

.role-badge {
  display: inline-block;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 10px;
  font-weight: 600;
  line-height: 1.4;
}

/* 关于系统弹窗：macOS 风格简洁卡片 */
.about-dialog {
  border-radius: 12px;
  overflow: hidden;

  .el-dialog__header {
    padding: 16px 20px 8px;

    .el-dialog__title {
      font-size: 15px;
      font-weight: 600;
      color: #333;
    }
  }

  .el-dialog__body {
    padding: 8px 20px 20px;
  }

  .about-content {
    min-height: 120px;
  }

  .about-logo {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 14px;
    padding-bottom: 12px;
    border-bottom: 1px solid #eaeaea;

    .about-monogram {
      width: 28px;
      height: 28px;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      border-radius: 7px;
      background: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%);
      color: #fff;
      font-size: 14px;
      font-weight: 600;
    }

    .about-name {
      font-size: 14px;
      font-weight: 600;
      color: #333;
    }

    .about-version-badge {
      margin-left: auto;
      font-size: 11px;
      font-weight: 500;
      color: #0071e3;
      background: rgba(0, 113, 227, 0.1);
      padding: 2px 8px;
      border-radius: 10px;
    }
  }

  .about-row {
    display: flex;
    align-items: center;
    padding: 6px 0;
    font-size: 13px;

    .about-label {
      width: 90px;
      flex-shrink: 0;
      color: #999;
    }

    .about-value {
      color: #333;
      font-weight: 500;
      word-break: break-all;
    }
  }

  .view-version-block {
    margin-top: 12px;
    padding: 10px 12px;
    border: 1px solid #eaeaea;
    border-radius: 10px;
    background: rgba(0, 113, 227, 0.03);

    .view-version-head {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-bottom: 2px;
    }

    .view-version-title {
      font-size: 12px;
      font-weight: 600;
      color: #555;

      i {
        color: #0071e3;
      }
    }

    .about-row {
      padding: 3px 0;
      font-size: 12px;
    }

    .update-view-btn {
      margin-top: 6px;
      width: 100%;
    }
  }
}
</style>

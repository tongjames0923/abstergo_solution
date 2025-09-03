<template>
  <div :class="{'has-logo':showLogo}">
    <logo v-if="showLogo" :collapse="isCollapse" />
    <el-scrollbar wrap-class="scrollbar-wrapper">
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :background-color="variables.menuBg"
        :text-color="variables.menuText"
        :unique-opened="false"
        :active-text-color="variables.menuActiveText"
        :collapse-transition="false"
        mode="vertical"
      >
        <sidebar-item v-for="route in routes" :key="route.path" :item="route" :base-path="route.path" />
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import Logo from './Logo'
import SidebarItem from './SidebarItem'
import variables from '@/styles/variables.scss'

export default {
  components: { SidebarItem, Logo },
  computed: {
    ...mapGetters([
      'sidebar'
    ]),
    routes() {
      return this.filterRoutesByRole(this.$router.options.routes, this.$store.getters.roles)
    },
    activeMenu() {
      const route = this.$route
      const { meta, path } = route
      // if set path, the sidebar will highlight the path you set
      if (meta.activeMenu) {
        return meta.activeMenu
      }
      return path
    },
    showLogo() {
      return this.$store.state.settings.sidebarLogo
    },
    variables() {
      return variables
    },
    isCollapse() {
      return !this.sidebar.opened
    }
  },
  methods: {
    filterRoutesByRole(routes, roles) {
      const res = []
      routes.forEach(route => {
        const tmp = { ...route }
        if (this.hasRolePermission(roles, tmp)) {
          if (tmp.children) {
            tmp.children = this.filterRoutesByRole(tmp.children, roles)
          }
          res.push(tmp)
        }
      })
      return res
    },
    hasRolePermission(roles, route) {
      if (route.meta && route.meta.roles) {
        return roles.some(role => route.meta.roles.includes(role))
      } else {
        // 如果路由没有设置角色，则默认可见
        return true
      }
    }
  }
}
</script>


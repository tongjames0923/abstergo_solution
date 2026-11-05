const getters = {
  sidebar: state => state.app.sidebar,
  device: state => state.app.device,
  language: state => state.app.language,
  token: state => state.user.token,
  avatar: state => state.user.avatar,
  name: state => state.user.name,
  userName: state => state.user.userName,
  roles: state => state.user.roles,
  roleGroups: state => state.user.roleGroups,
  departments: state => state.user.departments,
  currentDepartment: state => state.user.currentDepartment,
  accountStatus: state => state.user.accountStatus,
  accountStatusStr: state => state.user.accountStatusStr,
  routes: state => state.user.routes,
  visitedViews: state => state.tagsView.visitedViews
}
export default getters

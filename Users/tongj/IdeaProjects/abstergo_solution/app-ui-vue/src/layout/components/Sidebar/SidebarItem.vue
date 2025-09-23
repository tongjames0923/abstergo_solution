<style scoped>
/* 为子菜单添加动画优化 */
.nest-menu ::v-deep .el-submenu .el-menu {
  transform-origin: 0 0;
  transform: translateZ(0);
  backface-visibility: hidden;
  /* 添加更优雅的过渡效果 */
  transition: all 0.3s cubic-bezier(0.25, 0.1, 0.25, 1) !important;
  background: transparent;
}

.nest-menu ::v-deep .el-submenu .el-menu--vertical {
  will-change: transform, opacity;
  /* 添加更优雅的过渡效果 */
  transition: all 0.3s cubic-bezier(0.25, 0.1, 0.25, 1) !important;
  background: transparent;
}

/* 适配毛玻璃效果的菜单项样式 */
::v-deep .el-menu-item,
::v-deep .el-submenu__title {
  margin: 0 8px 4px 8px;
  border-radius: 6px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  background-color: transparent !important;
  backdrop-filter: blur(5px);
  -webkit-backdrop-filter: blur(5px);
  transition: all 0.3s cubic-bezier(0.25, 0.1, 0.25, 1);
  overflow: hidden;
}

::v-deep .el-menu-item:hover,
::v-deep .el-submenu__title:hover {
  background-color: transparent !important; // 修改为透明背景
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
  border-radius: 6px;
  overflow: hidden;
}

::v-deep .el-menu-item.is-active {
  background-color: rgba(64, 158, 255, 0.1) !important;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.1);
  border-radius: 6px;
  overflow: hidden;
}

/* 收起状态下的样式 */
.hideSidebar ::v-deep .el-menu-item,
.hideSidebar ::v-deep .el-submenu__title {
  margin: 0 5px 2px 5px;
  background-color: transparent !important;
  backdrop-filter: blur(15px);
  -webkit-backdrop-filter: blur(15px);
  border-radius: 4px;
}

.hideSidebar ::v-deep .el-menu-item:hover,
.hideSidebar ::v-deep .el-submenu__title:hover {
  background-color: transparent !important; // 修改为透明背景
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 4px;
}

.hideSidebar ::v-deep .el-menu-item.is-active {
  background-color: rgba(64, 158, 255, 0.15) !important;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 4px;
}

/* 子菜单项样式 */
::v-deep .el-menu--popup .el-menu-item,
::v-deep .el-menu .el-submenu .el-menu .el-menu-item {
  margin: 0 8px 4px 8px;
  border-radius: 6px;
  border: 1px solid rgba(255, 255, 255, 0.1);
  background-color: transparent !important;
  backdrop-filter: blur(5px);
  -webkit-backdrop-filter: blur(5px);
  transition: all 0.3s cubic-bezier(0.25, 0.1, 0.25, 1);
  overflow: hidden;
}

::v-deep .el-menu--popup .el-menu-item:hover,
::v-deep .el-menu .el-submenu .el-menu .el-menu-item:hover {
  background-color: transparent !important; // 修改为透明背景
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.1);
  border-radius: 6px;
  overflow: hidden;
}

::v-deep .el-menu--popup .el-menu-item.is-active,
::v-deep .el-menu .el-submenu .el-menu .el-menu-item.is-active {
  background-color: rgba(64, 158, 255, 0.1) !important;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  box-shadow: 0 6px 16px rgba(64, 158, 255, 0.1);
  border-radius: 6px;
  overflow: hidden;
}
</style>
-- 动态视图路由配置表结构
-- 创建时间: 2026-08-10

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for view_route_configration
-- ----------------------------
DROP TABLE IF EXISTS `view_route_configration`;
CREATE TABLE `view_route_configration`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `route_group` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '路由分组（如 common, admin, develop）',
  `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '路由路径',
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '组件路径',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '路由名称',
  `redirect` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '重定向路径',
  `hidden` tinyint(1) NULL DEFAULT 0 COMMENT '是否隐藏（0:否，1:是）',
  `always_show` tinyint(1) NULL DEFAULT 0 COMMENT '是否始终显示（0:否，1:是）',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父路由ID（用于构建树形结构）',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序顺序',
  `flag` int NULL DEFAULT 1 COMMENT '状态标志（0:禁用，1:启用）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建人',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新人',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_route_group`(`route_group` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_flag`(`flag` ASC) USING BTREE,
  INDEX `idx_sort_order`(`sort_order` ASC) USING BTREE,
  UNIQUE KEY `uk_group_path_parent`(`route_group`, `path`, `parent_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '视图路由配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for view_route_meta
-- ----------------------------
DROP TABLE IF EXISTS `view_route_meta`;
CREATE TABLE `view_route_meta`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `route_id` bigint NOT NULL COMMENT '关联的路由ID',
  `route_group` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '路由分组',
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '路由标题',
  `icon` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '路由图标',
  `breadcrumb` tinyint(1) NULL DEFAULT 1 COMMENT '是否显示面包屑（0:否，1:是）',
  `active_menu` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '激活菜单路径',
  `flag` int NULL DEFAULT 1 COMMENT '状态标志（0:禁用，1:启用）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_route_id`(`route_id` ASC) USING BTREE,
  UNIQUE KEY `uk_route_id`(`route_id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '视图路由元数据表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for view_route_role
-- ----------------------------
DROP TABLE IF EXISTS `view_route_role`;
CREATE TABLE `view_route_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `route_id` bigint NOT NULL COMMENT '关联的路由ID',
  `role_code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL COMMENT '权限编码',
  `flag` int NULL DEFAULT 1 COMMENT '状态标志（0:禁用，1:启用）',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_route_id`(`route_id` ASC) USING BTREE,
  INDEX `idx_role_code`(`role_code` ASC) USING BTREE,
  UNIQUE KEY `uk_route_role`(`route_id`, `role_code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin COMMENT = '视图路由权限表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- 初始化数据：从 view-route.yml 导入现有路由配置
-- ----------------------------

-- 清空现有数据
TRUNCATE TABLE `view_route_role`;
TRUNCATE TABLE `view_route_meta`;
TRUNCATE TABLE `view_route_configration`;

-- 插入 common 分组路由
INSERT INTO `view_route_configration` (`route_group`, `path`, `component`, `name`, `redirect`, `hidden`, `always_show`, `parent_id`, `sort_order`, `flag`) VALUES
('default', '/common', 'Layout', 'view.menu.common', '/common/user', 0, 0, NULL, 1, 1),
('default', 'user', '/user/index.vue', 'view.menu.common.user', NULL, 0, 0, 1, 1, 1);

-- 插入 common 分组路由元数据
INSERT INTO `view_route_meta` (`route_id`, `route_group`, `title`, `icon`, `breadcrumb`, `active_menu`) VALUES
(1, 'default', 'view.menu.common', 'el-icon-user', 1, NULL),
(2, 'default', 'view.menu.common.user', 'el-icon-user', 1, NULL);

-- 插入 admin 分组路由
INSERT INTO `view_route_configration` (`route_group`, `path`, `component`, `name`, `redirect`, `hidden`, `always_show`, `parent_id`, `sort_order`, `flag`) VALUES
('default', '/admin', 'Layout', 'view.menu.admin.title', '/admin/user', 0, 0, NULL, 2, 1),
('default', 'user', '/admin/user/index.vue', 'view.menu.admin.user', NULL, 0, 0, 3, 1, 1),
('default', 'role', '/admin/role/index.vue', 'view.menu.admin.role', NULL, 0, 0, 3, 2, 1),
('default', 'company', '/admin/company/index.vue', 'view.menu.admin.company', NULL, 0, 0, 3, 3, 1),
('default', 'role-group', '/admin/role_group/index.vue', 'view.menu.admin.role.group', NULL, 0, 0, 3, 4, 1);

-- 插入 admin 分组路由元数据
INSERT INTO `view_route_meta` (`route_id`, `route_group`, `title`, `icon`, `breadcrumb`, `active_menu`) VALUES
(3, 'default', 'view.menu.admin.title', 'el-icon-setting', 1, NULL),
(4, 'default', 'view.menu.admin.user', 'el-icon-user', 1, NULL),
(5, 'default', 'view.menu.admin.role', 'el-icon-lock', 1, NULL),
(6, 'default', 'view.menu.admin.company', 'el-icon-office-building', 1, NULL),
(7, 'default', 'view.menu.admin.role.group', 'el-icon-s-management', 1, NULL);

-- 插入 admin 分组路由权限
INSERT INTO `view_route_role` (`route_id`, `role_code`, `flag`) VALUES
(3, 'MENU.ADMIN', 1);

-- 插入 develop 分组路由
INSERT INTO `view_route_configration` (`route_group`, `path`, `component`, `name`, `redirect`, `hidden`, `always_show`, `parent_id`, `sort_order`, `flag`) VALUES
('default', '/develop', 'Layout', 'view.menu.develop', '/develop/form-manager', 0, 0, NULL, 3, 1),
('default', 'form-manager', '/form/FormManager.vue', 'view.menu.develop.form.manager', NULL, 0, 0, 8, 1, 1),
('default', 'form', '/form/FormCreator.vue', 'view.menu.develop.form.create', NULL, 0, 0, 8, 2, 1),
('default', 'dic', '/admin/dic/index.vue', 'view.menu.develop.dic', NULL, 0, 0, 8, 3, 1),
('default', 'generic-api', '/admin/generic-api/index.vue', 'view.menu.develop.generic.api', NULL, 0, 0, 8, 4, 1),
('default', 'systemlog', '/admin/systemlog/index.vue', 'view.menu.develop.systemlog', NULL, 0, 0, 8, 5, 1),
('default', 'cache', '/cache/index.vue', 'view.menu.develop.cache', NULL, 0, 0, 8, 6, 1);

-- 插入 develop 分组路由元数据
INSERT INTO `view_route_meta` (`route_id`, `route_group`, `title`, `icon`, `breadcrumb`, `active_menu`) VALUES
(8, 'default', 'view.menu.develop.title', 'el-icon-s-tools', 1, NULL),
(9, 'default', 'view.menu.develop.form.manager', 'el-icon-document', 1, NULL),
(10, 'default', 'view.menu.develop.form.create', 'el-icon-edit', 1, NULL),
(11, 'default', 'view.menu.develop.dic', 'el-icon-notebook-2', 1, NULL),
(12, 'default', 'view.menu.develop.generic.api', 'el-icon-connection', 1, NULL),
(13, 'default', 'view.menu.develop.systemlog', 'el-icon-data-line', 1, NULL),
(14, 'default', 'view.menu.develop.cache', 'el-icon-s-data', 1, NULL);

-- 插入 develop 分组路由权限
INSERT INTO `view_route_role` (`route_id`, `role_code`, `flag`) VALUES
(8, 'MENU.DEV', 1);

SET FOREIGN_KEY_CHECKS = 1;
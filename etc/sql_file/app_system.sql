/*
 Navicat Premium Dump SQL

 Source Server         : docker_3306
 Source Server Type    : MySQL
 Source Server Version : 80413 (8.4.13)
 Source Host           : localhost:3306
 Source Schema         : core_app

 Target Server Type    : MySQL
 Target Server Version : 80413 (8.4.13)
 File Encoding         : 65001

 Date: 21/11/2026 10:19:03
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for app_package
-- ----------------------------
DROP TABLE IF EXISTS `app_package`;
CREATE TABLE `app_package`  (
  `id` bigint NOT NULL COMMENT '主键ID（雪花算法生成）',
  `bucket_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'OSS 存储桶名称',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建者用户ID',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `download_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '安装包下载地址 URL',
  `flag` int NULL DEFAULT NULL COMMENT '数据状态标志：1-启用，-1-禁用，0-删除',
  `md5` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '安装包文件 MD5 校验值',
  `object_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'OSS 对象名称（文件路径）',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新者用户ID',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `version_id` bigint NULL DEFAULT NULL COMMENT '关联的应用版本ID（援引 app_version_info.id）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_version_id`(`version_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '应用安装包表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of app_package
-- ----------------------------
INSERT INTO `app_package` VALUES (744820834266251264, 'app-files', 1, '2026-08-18 15:32:04.968000', '', 0, '\"4e66df1e5714b379cb6d0c274ad769fa-30\"', 'app-package/744815003223195648/1787038313465_app-1.0-SNAPSHOT.jar', NULL, NULL, 744815003223195648);
INSERT INTO `app_package` VALUES (744821100587778048, 'app-files', 1, '2026-08-18 15:33:08.476000', '', 0, 'db48daea3064a8cc45beeee187594455', 'app-package/744815003223195648/1787038381297_dist.zip', NULL, NULL, 744815003223195648);
INSERT INTO `app_package` VALUES (744821857512849408, 'app-files', 1, '2026-08-18 15:36:08.946000', '', 0, '\"4e66df1e5714b379cb6d0c274ad769fa-30\"', 'app-package/744821750109306880/1787038556747_app-1.0-SNAPSHOT.jar', NULL, NULL, 744821750109306880);
INSERT INTO `app_package` VALUES (744821898889658368, 'app-files', 1, '2026-08-18 15:36:18.812000', '', 0, 'db48daea3064a8cc45beeee187594455', 'app-package/744821750109306880/1787038576995_dist.zip', NULL, NULL, 744821750109306880);

-- ----------------------------
-- Table structure for app_version_compatibility_policy
-- ----------------------------
DROP TABLE IF EXISTS `app_version_compatibility_policy`;
CREATE TABLE `app_version_compatibility_policy`  (
  `id` bigint NOT NULL COMMENT '主键ID（雪花算法生成）',
  `action` int NOT NULL COMMENT '动作类型（不兼容时的处理策略）：1-force 强制更新，2-recommend 建议更新，3-notify 仅提醒',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建者用户ID',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `flag` int NULL DEFAULT NULL COMMENT '数据状态标志：1-启用，-1-禁用，0-删除',
  `min_compat_version` bigint NOT NULL COMMENT '目标应用最低兼容版本号（低于此版本将触发更新）',
  `source_app_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '源应用标识（如 backend-service、web-console），表示哪个应用发布版本触发该策略',
  `source_platform` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '源平台（如 ANDROID、IOS、WEB、BACKEND）',
  `source_version_code` bigint NULL DEFAULT NULL COMMENT '源应用版本号（可空，为空表示所有版本均适用）',
  `target_app_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '目标应用标识（表示对哪个应用提出版本要求）',
  `target_platform` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目标平台（如 ANDROID、IOS、WEB）',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新者用户ID',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_source`(`source_app_key` ASC, `source_version_code` ASC, `source_platform` ASC) USING BTREE,
  INDEX `idx_target`(`target_app_key` ASC, `target_platform` ASC, `flag` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '应用版本兼容策略表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of app_version_compatibility_policy
-- ----------------------------

-- ----------------------------
-- Table structure for app_version_info
-- ----------------------------
DROP TABLE IF EXISTS `app_version_info`;
CREATE TABLE `app_version_info`  (
  `id` bigint NOT NULL COMMENT '主键ID（雪花算法生成）',
  `app_key` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '应用唯一标识（如 backend-service、mobile-app）',
  `build_info` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '构建信息（JSON 格式的构建元数据）',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建者用户ID',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `flag` int NULL DEFAULT NULL COMMENT '版本状态：0-草稿，1-发布，2-待审核，-1-下线，-2-回滚，-3-归档',
  `offline_time` datetime(6) NULL DEFAULT NULL COMMENT '版本下线时间',
  `platform` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目标平台（如 ANDROID、IOS、WEB）',
  `release_note` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '版本更新日志（描述本次版本的变更内容）',
  `release_time` datetime(6) NULL DEFAULT NULL COMMENT '版本发布时间',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新者用户ID',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `version_code` bigint NULL DEFAULT NULL COMMENT '内部版本号（递增整数，用于版本比较）',
  `version_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '外部版本名称（用户可见的版本号，如 1.2.0）',
  `version_type` int NULL DEFAULT NULL COMMENT '版本类型：1-release，2-beta，3-alpha，4-rc',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `IDXj53bupwoxufnjtu17kpjk5gu2`(`app_key` ASC, `version_code` ASC) USING BTREE,
  INDEX `IDX9cd1jj6lob0wcwer68ly9akd0`(`version_name` ASC) USING BTREE,
  INDEX `IDXi9b41sl0tw5yhwj1jkvv9mqgx`(`version_type` ASC, `platform` ASC) USING BTREE,
  INDEX `IDXl36rwti7637k1aby3jks715yo`(`flag` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '应用版本信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of app_version_info
-- ----------------------------
INSERT INTO `app_version_info` VALUES (744815003223195648, 'TEST', '', 1, '2026-08-18 15:08:54.731000', -3, NULL, 'windows', '', NULL, NULL, NULL, 1, '1', 1);
INSERT INTO `app_version_info` VALUES (744821236067991552, 'TEST', '', 1, '2026-08-18 15:33:40.760000', -3, NULL, 'windows', '', NULL, NULL, NULL, 2, '2', 1);
INSERT INTO `app_version_info` VALUES (744821750109306880, 'TEST', '', 1, '2026-08-18 15:35:43.303000', -3, '2026-08-21 15:49:43.694000', 'windows', '', '2026-08-18 15:37:12.154000', NULL, NULL, 2, '2', 1);
INSERT INTO `app_version_info` VALUES (749787246822948864, 'Test', '', 1, '2026-09-01 08:26:49.995000', 0, NULL, 'windows', '', NULL, NULL, NULL, 1, '1.0', 1);

-- ----------------------------
-- Table structure for demo
-- ----------------------------
DROP TABLE IF EXISTS `demo`;
CREATE TABLE `demo`  (
  `id` bigint NULL DEFAULT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of demo
-- ----------------------------

-- ----------------------------
-- Table structure for dynamic_form_configration
-- ----------------------------
DROP TABLE IF EXISTS `dynamic_form_configration`;
CREATE TABLE `dynamic_form_configration`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID（自增）',
  `expandable` int NULL DEFAULT NULL COMMENT '表单是否支持展开/收起（0:不可，1:可以）',
  `expanded` int NULL DEFAULT NULL COMMENT '表单字段是否展开（0:收起，1:展开）',
  `flag` int NULL DEFAULT NULL COMMENT '表单状态：1-已启用，0-未启用，-1-已删除',
  `gutter` int NULL DEFAULT NULL COMMENT '表单字段之间的间隔（像素值）',
  `label_width` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '表单标签宽度（如 100px）',
  `span` int NULL DEFAULT NULL COMMENT '表单栅格占据的列数（基于 24 栅格系统）',
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '表单标题',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `IDXnwmmyxc6ycs9enf76259ukckp`(`flag` ASC) USING BTREE,
  INDEX `IDXhpd82ymnmm8l26ylo49r363et`(`title` ASC) USING BTREE,
  INDEX `IDXivdgpn6eg2l95b8qaycymamxe`(`expandable` ASC, `expanded` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '动态表单配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of dynamic_form_configration
-- ----------------------------
INSERT INTO `dynamic_form_configration` VALUES (1, 1, 1, 1, 40, '80px', 8, '用户查询表单');
INSERT INTO `dynamic_form_configration` VALUES (2, 0, 1, 1, 40, '80px', 5, '用户新增表单');
INSERT INTO `dynamic_form_configration` VALUES (3, 0, 1, 1, 24, '80px', 20, '用户编辑表单');
INSERT INTO `dynamic_form_configration` VALUES (6, 1, 1, 1, 20, '100px', 8, '权限搜索表单');
INSERT INTO `dynamic_form_configration` VALUES (12, 1, 1, 1, 20, '100px', 8, '权限新增表单');
INSERT INTO `dynamic_form_configration` VALUES (13, 1, 1, 1, 20, '100px', 8, '权限编辑表单');
INSERT INTO `dynamic_form_configration` VALUES (14, 1, 1, 1, 20, '100px', 8, '权限组查询表单');
INSERT INTO `dynamic_form_configration` VALUES (15, 1, 1, 1, 20, '100px', 8, '权限组新增表单');

-- ----------------------------
-- Table structure for form_field_configration
-- ----------------------------
DROP TABLE IF EXISTS `form_field_configration`;
CREATE TABLE `form_field_configration`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID（自增）',
  `label` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '表单域标签（显示在表单中的字段名称）',
  `prop` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '表单域对应的数据属性名称（绑定字段名）',
  `type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '表单域组件类型（如 input、select、date 等）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `gobal_index`(`type` ASC, `prop` ASC, `label` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 128 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '表单域配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of form_field_configration
-- ----------------------------
INSERT INTO `form_field_configration` VALUES (5, '创建时间', 'createTimeRange', 'date-range');
INSERT INTO `form_field_configration` VALUES (20, '创建时间', 'createTimeRange', 'date-range');
INSERT INTO `form_field_configration` VALUES (30, '创建时间', 'createTimeRange', 'date-range');
INSERT INTO `form_field_configration` VALUES (36, '创建时间', 'createTimeRange', 'date-range');
INSERT INTO `form_field_configration` VALUES (77, '创建时间', 'createTimeRange', 'date-range');
INSERT INTO `form_field_configration` VALUES (126, '创建时间', 'createTimeRange', 'date-range');
INSERT INTO `form_field_configration` VALUES (6, '更新时间', 'updateTimeRange', 'date-range');
INSERT INTO `form_field_configration` VALUES (21, '更新时间', 'updateTimeRange', 'date-range');
INSERT INTO `form_field_configration` VALUES (31, '更新时间', 'updateTimeRange', 'date-range');
INSERT INTO `form_field_configration` VALUES (37, '更新时间', 'updateTimeRange', 'date-range');
INSERT INTO `form_field_configration` VALUES (78, '更新时间', 'updateTimeRange', 'date-range');
INSERT INTO `form_field_configration` VALUES (127, '更新时间', 'updateTimeRange', 'date-range');
INSERT INTO `form_field_configration` VALUES (121, '权限组描述', 'description', 'input');
INSERT INTO `form_field_configration` VALUES (93, '权限说明', 'description', 'input');
INSERT INTO `form_field_configration` VALUES (109, '权限组描述', 'descriptionLike', 'input');
INSERT INTO `form_field_configration` VALUES (112, '权限组描述', 'descriptionLike', 'input');
INSERT INTO `form_field_configration` VALUES (115, '权限组描述', 'descriptionLike', 'input');
INSERT INTO `form_field_configration` VALUES (118, '权限组描述', 'descriptionLike', 'input');
INSERT INTO `form_field_configration` VALUES (16, '公司Id', 'equalsId', 'input');
INSERT INTO `form_field_configration` VALUES (18, '税号', 'equalsTaxNumber', 'input');
INSERT INTO `form_field_configration` VALUES (79, '权限Id', 'idEquals', 'input');
INSERT INTO `form_field_configration` VALUES (94, '权限Id', 'idEquals', 'input');
INSERT INTO `form_field_configration` VALUES (99, '权限Id', 'idEquals', 'input');
INSERT INTO `form_field_configration` VALUES (1, '用户ID', 'idEquals', 'input');
INSERT INTO `form_field_configration` VALUES (26, '用户ID', 'idEquals', 'input');
INSERT INTO `form_field_configration` VALUES (32, '用户ID', 'idEquals', 'input');
INSERT INTO `form_field_configration` VALUES (73, '用户ID', 'idEquals', 'input');
INSERT INTO `form_field_configration` VALUES (122, '用户ID', 'idEquals', 'input');
INSERT INTO `form_field_configration` VALUES (17, '公司名称', 'likeName', 'input');
INSERT INTO `form_field_configration` VALUES (105, '权限名称', 'name', 'input');
INSERT INTO `form_field_configration` VALUES (107, '权限名称', 'nameLike', 'input');
INSERT INTO `form_field_configration` VALUES (96, '权限名称 ', 'nameLike', 'input');
INSERT INTO `form_field_configration` VALUES (101, '权限名称 ', 'nameLike', 'input');
INSERT INTO `form_field_configration` VALUES (108, '权限组名称', 'nameLike', 'input');
INSERT INTO `form_field_configration` VALUES (111, '权限组名称', 'nameLike', 'input');
INSERT INTO `form_field_configration` VALUES (114, '权限组名称', 'nameLike', 'input');
INSERT INTO `form_field_configration` VALUES (117, '权限组名称', 'nameLike', 'input');
INSERT INTO `form_field_configration` VALUES (8, '昵称', 'nickName', 'input');
INSERT INTO `form_field_configration` VALUES (13, '昵称', 'nickName', 'input');
INSERT INTO `form_field_configration` VALUES (39, '昵称', 'nickName', 'input');
INSERT INTO `form_field_configration` VALUES (44, '昵称', 'nickName', 'input');
INSERT INTO `form_field_configration` VALUES (49, '昵称', 'nickName', 'input');
INSERT INTO `form_field_configration` VALUES (54, '昵称', 'nickName', 'input');
INSERT INTO `form_field_configration` VALUES (59, '昵称', 'nickName', 'input');
INSERT INTO `form_field_configration` VALUES (64, '昵称', 'nickName', 'input');
INSERT INTO `form_field_configration` VALUES (69, '昵称', 'nickName', 'input');
INSERT INTO `form_field_configration` VALUES (3, '昵称', 'nickNameLike', 'input');
INSERT INTO `form_field_configration` VALUES (28, '昵称', 'nickNameLike', 'input');
INSERT INTO `form_field_configration` VALUES (34, '昵称', 'nickNameLike', 'input');
INSERT INTO `form_field_configration` VALUES (75, '昵称', 'nickNameLike', 'input');
INSERT INTO `form_field_configration` VALUES (124, '昵称', 'nickNameLike', 'input');
INSERT INTO `form_field_configration` VALUES (9, '密码', 'password', 'input');
INSERT INTO `form_field_configration` VALUES (40, '密码', 'password', 'input');
INSERT INTO `form_field_configration` VALUES (45, '密码', 'password', 'input');
INSERT INTO `form_field_configration` VALUES (50, '密码', 'password', 'input');
INSERT INTO `form_field_configration` VALUES (55, '密码', 'password', 'input');
INSERT INTO `form_field_configration` VALUES (60, '密码', 'password', 'input');
INSERT INTO `form_field_configration` VALUES (65, '密码', 'password', 'input');
INSERT INTO `form_field_configration` VALUES (70, '密码', 'password', 'input');
INSERT INTO `form_field_configration` VALUES (10, '备注', 'remark', 'input');
INSERT INTO `form_field_configration` VALUES (14, '备注', 'remark', 'input');
INSERT INTO `form_field_configration` VALUES (41, '备注', 'remark', 'input');
INSERT INTO `form_field_configration` VALUES (46, '备注', 'remark', 'input');
INSERT INTO `form_field_configration` VALUES (51, '备注', 'remark', 'input');
INSERT INTO `form_field_configration` VALUES (56, '备注', 'remark', 'input');
INSERT INTO `form_field_configration` VALUES (61, '备注', 'remark', 'input');
INSERT INTO `form_field_configration` VALUES (66, '备注', 'remark', 'input');
INSERT INTO `form_field_configration` VALUES (71, '备注', 'remark', 'input');
INSERT INTO `form_field_configration` VALUES (92, '权限名称', 'role', 'input');
INSERT INTO `form_field_configration` VALUES (106, '权限编号', 'role', 'input');
INSERT INTO `form_field_configration` VALUES (80, '权限名称', 'roleEquals', 'input');
INSERT INTO `form_field_configration` VALUES (95, '权限编号', 'roleEquals', 'input');
INSERT INTO `form_field_configration` VALUES (100, '权限编号', 'roleEquals', 'input');
INSERT INTO `form_field_configration` VALUES (120, '权限组名称', 'roleGroupName', 'input');
INSERT INTO `form_field_configration` VALUES (7, '用户名', 'userName', 'input');
INSERT INTO `form_field_configration` VALUES (12, '用户名', 'userName', 'input');
INSERT INTO `form_field_configration` VALUES (38, '用户名', 'userName', 'input');
INSERT INTO `form_field_configration` VALUES (43, '用户名', 'userName', 'input');
INSERT INTO `form_field_configration` VALUES (48, '用户名', 'userName', 'input');
INSERT INTO `form_field_configration` VALUES (53, '用户名', 'userName', 'input');
INSERT INTO `form_field_configration` VALUES (58, '用户名', 'userName', 'input');
INSERT INTO `form_field_configration` VALUES (63, '用户名', 'userName', 'input');
INSERT INTO `form_field_configration` VALUES (68, '用户名', 'userName', 'input');
INSERT INTO `form_field_configration` VALUES (2, '用户名', 'userNameEquals', 'input');
INSERT INTO `form_field_configration` VALUES (27, '用户名', 'userNameEquals', 'input');
INSERT INTO `form_field_configration` VALUES (33, '用户名', 'userNameEquals', 'input');
INSERT INTO `form_field_configration` VALUES (74, '用户名', 'userNameEquals', 'input');
INSERT INTO `form_field_configration` VALUES (123, '用户名', 'userNameEquals', 'input');
INSERT INTO `form_field_configration` VALUES (19, '公司状态', 'flagEquals', 'select');
INSERT INTO `form_field_configration` VALUES (81, '状态', 'flagEquals', 'select');
INSERT INTO `form_field_configration` VALUES (97, '状态', 'flagEquals', 'select');
INSERT INTO `form_field_configration` VALUES (102, '状态', 'flagEquals', 'select');
INSERT INTO `form_field_configration` VALUES (4, '用户状态', 'flagEquals', 'select');
INSERT INTO `form_field_configration` VALUES (29, '用户状态', 'flagEquals', 'select');
INSERT INTO `form_field_configration` VALUES (35, '用户状态', 'flagEquals', 'select');
INSERT INTO `form_field_configration` VALUES (76, '用户状态', 'flagEquals', 'select');
INSERT INTO `form_field_configration` VALUES (125, '用户状态', 'flagEquals', 'select');
INSERT INTO `form_field_configration` VALUES (110, '状态', 'flagIn', 'select');
INSERT INTO `form_field_configration` VALUES (113, '状态', 'flagIn', 'select');
INSERT INTO `form_field_configration` VALUES (116, '状态', 'flagIn', 'select');
INSERT INTO `form_field_configration` VALUES (119, '状态', 'flagIn', 'select');
INSERT INTO `form_field_configration` VALUES (104, '权限类型', 'type', 'select');
INSERT INTO `form_field_configration` VALUES (103, '权限类型', 'typeEquals', 'select');
INSERT INTO `form_field_configration` VALUES (98, '状态 (副本)', 'typeEquals', 'select');
INSERT INTO `form_field_configration` VALUES (11, '头像', 'avatar', 'url-image');
INSERT INTO `form_field_configration` VALUES (15, '头像', 'avatar', 'url-image');
INSERT INTO `form_field_configration` VALUES (42, '头像', 'avatar', 'url-image');
INSERT INTO `form_field_configration` VALUES (47, '头像', 'avatar', 'url-image');
INSERT INTO `form_field_configration` VALUES (52, '头像', 'avatar', 'url-image');
INSERT INTO `form_field_configration` VALUES (57, '头像', 'avatar', 'url-image');
INSERT INTO `form_field_configration` VALUES (62, '头像', 'avatar', 'url-image');
INSERT INTO `form_field_configration` VALUES (67, '头像', 'avatar', 'url-image');
INSERT INTO `form_field_configration` VALUES (72, '头像', 'avatar', 'url-image');

-- ----------------------------
-- Table structure for form_field_relation
-- ----------------------------
DROP TABLE IF EXISTS `form_field_relation`;
CREATE TABLE `form_field_relation`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID（自增）',
  `field_id` bigint NULL DEFAULT NULL COMMENT '表单域配置ID（援引 form_field_configration.id）',
  `form_id` bigint NULL DEFAULT NULL COMMENT '表单配置ID（援引 dynamic_form_configration.id）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `form_field_relation_form_id_index`(`form_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 128 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '表单与表单域关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of form_field_relation
-- ----------------------------
INSERT INTO `form_field_relation` VALUES (12, 12, 3);
INSERT INTO `form_field_relation` VALUES (13, 13, 3);
INSERT INTO `form_field_relation` VALUES (14, 14, 3);
INSERT INTO `form_field_relation` VALUES (15, 15, 3);
INSERT INTO `form_field_relation` VALUES (16, 16, 4);
INSERT INTO `form_field_relation` VALUES (17, 17, 4);
INSERT INTO `form_field_relation` VALUES (18, 18, 4);
INSERT INTO `form_field_relation` VALUES (19, 19, 4);
INSERT INTO `form_field_relation` VALUES (20, 20, 4);
INSERT INTO `form_field_relation` VALUES (21, 21, 4);
INSERT INTO `form_field_relation` VALUES (22, 22, 5);
INSERT INTO `form_field_relation` VALUES (23, 23, 5);
INSERT INTO `form_field_relation` VALUES (24, 24, 5);
INSERT INTO `form_field_relation` VALUES (25, 25, 5);
INSERT INTO `form_field_relation` VALUES (68, 68, 2);
INSERT INTO `form_field_relation` VALUES (69, 69, 2);
INSERT INTO `form_field_relation` VALUES (70, 70, 2);
INSERT INTO `form_field_relation` VALUES (71, 71, 2);
INSERT INTO `form_field_relation` VALUES (72, 72, 2);
INSERT INTO `form_field_relation` VALUES (92, 92, 12);
INSERT INTO `form_field_relation` VALUES (93, 93, 12);
INSERT INTO `form_field_relation` VALUES (99, 99, 6);
INSERT INTO `form_field_relation` VALUES (100, 100, 6);
INSERT INTO `form_field_relation` VALUES (101, 101, 6);
INSERT INTO `form_field_relation` VALUES (102, 102, 6);
INSERT INTO `form_field_relation` VALUES (103, 103, 6);
INSERT INTO `form_field_relation` VALUES (104, 104, 13);
INSERT INTO `form_field_relation` VALUES (105, 105, 13);
INSERT INTO `form_field_relation` VALUES (106, 106, 13);
INSERT INTO `form_field_relation` VALUES (117, 117, 14);
INSERT INTO `form_field_relation` VALUES (118, 118, 14);
INSERT INTO `form_field_relation` VALUES (119, 119, 14);
INSERT INTO `form_field_relation` VALUES (120, 120, 15);
INSERT INTO `form_field_relation` VALUES (121, 121, 15);
INSERT INTO `form_field_relation` VALUES (122, 122, 1);
INSERT INTO `form_field_relation` VALUES (123, 123, 1);
INSERT INTO `form_field_relation` VALUES (124, 124, 1);
INSERT INTO `form_field_relation` VALUES (125, 125, 1);
INSERT INTO `form_field_relation` VALUES (126, 126, 1);
INSERT INTO `form_field_relation` VALUES (127, 127, 1);

-- ----------------------------
-- Table structure for oss_download_code
-- ----------------------------
DROP TABLE IF EXISTS `oss_download_code`;
CREATE TABLE `oss_download_code`  (
  `id` bigint NOT NULL COMMENT '主键ID（雪花算法生成）',
  `bucket` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '存储桶名',
  `code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '下载唯一码',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `expire_at` bigint NULL DEFAULT NULL COMMENT '过期时间戳（毫秒），小于等于0表示永不过期',
  `flag` int NULL DEFAULT NULL COMMENT '下载码状态：1-有效，-1-已删除',
  `object_key` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '存储对象键',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注说明',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_code`(`code` ASC) USING BTREE,
  INDEX `idx_flag`(`flag` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'OSS 公开下载码表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of oss_download_code
-- ----------------------------
INSERT INTO `oss_download_code` VALUES (754157900758253568, 'app-files', 'SCANAPP13', '2026-09-13 09:54:15.107000', 1789363742343, 1, '20260906/ef727b5926a94868986b56220c145581.apk', '', '2026-09-14 13:19:02.344000');
INSERT INTO `oss_download_code` VALUES (754542831716007936, 'app-files', '525yLMVvojkI1Xbk', '2026-09-14 11:23:49.801000', 0, -1, '20260906/ef727b5926a94868986b56220c145581.apk', NULL, '2026-09-14 11:24:07.630000');

-- ----------------------------
-- Table structure for property_relation
-- ----------------------------
DROP TABLE IF EXISTS `property_relation`;
CREATE TABLE `property_relation`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID（自增）',
  `property_id` bigint NULL DEFAULT NULL COMMENT '属性ID（援引 property_value.id）',
  `relation_id` bigint NULL DEFAULT NULL COMMENT '关联对象ID（根据 relation_type 指向表单域ID或表单ID）',
  `relation_type` int NULL DEFAULT NULL COMMENT '关联类型（1:绑定到字段，2:绑定到表单）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `property_relation_property_id_index`(`property_id` ASC) USING BTREE,
  INDEX `property_relation_relation_id_index`(`relation_id` ASC, `relation_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 344 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '属性关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of property_relation
-- ----------------------------
INSERT INTO `property_relation` VALUES (1, 1, 1, 2);
INSERT INTO `property_relation` VALUES (2, 2, 1, 2);
INSERT INTO `property_relation` VALUES (3, 3, 1, 2);
INSERT INTO `property_relation` VALUES (4, 4, 2, 2);
INSERT INTO `property_relation` VALUES (5, 5, 2, 2);
INSERT INTO `property_relation` VALUES (6, 6, 3, 2);
INSERT INTO `property_relation` VALUES (7, 7, 3, 2);
INSERT INTO `property_relation` VALUES (8, 8, 4, 2);
INSERT INTO `property_relation` VALUES (9, 9, 4, 2);
INSERT INTO `property_relation` VALUES (10, 10, 5, 2);
INSERT INTO `property_relation` VALUES (11, 11, 5, 2);
INSERT INTO `property_relation` VALUES (12, 12, 5, 2);
INSERT INTO `property_relation` VALUES (13, 13, 5, 2);
INSERT INTO `property_relation` VALUES (14, 14, 6, 2);
INSERT INTO `property_relation` VALUES (15, 15, 6, 2);
INSERT INTO `property_relation` VALUES (16, 16, 6, 2);
INSERT INTO `property_relation` VALUES (17, 17, 6, 2);
INSERT INTO `property_relation` VALUES (18, 18, 7, 2);
INSERT INTO `property_relation` VALUES (19, 19, 7, 2);
INSERT INTO `property_relation` VALUES (20, 20, 7, 2);
INSERT INTO `property_relation` VALUES (21, 21, 7, 2);
INSERT INTO `property_relation` VALUES (22, 22, 8, 2);
INSERT INTO `property_relation` VALUES (23, 23, 8, 2);
INSERT INTO `property_relation` VALUES (24, 24, 8, 2);
INSERT INTO `property_relation` VALUES (25, 25, 8, 2);
INSERT INTO `property_relation` VALUES (26, 26, 9, 2);
INSERT INTO `property_relation` VALUES (27, 27, 9, 2);
INSERT INTO `property_relation` VALUES (28, 28, 9, 2);
INSERT INTO `property_relation` VALUES (29, 29, 9, 2);
INSERT INTO `property_relation` VALUES (30, 30, 9, 2);
INSERT INTO `property_relation` VALUES (31, 31, 9, 2);
INSERT INTO `property_relation` VALUES (32, 32, 10, 2);
INSERT INTO `property_relation` VALUES (33, 33, 10, 2);
INSERT INTO `property_relation` VALUES (34, 34, 10, 2);
INSERT INTO `property_relation` VALUES (35, 35, 10, 2);
INSERT INTO `property_relation` VALUES (36, 36, 12, 2);
INSERT INTO `property_relation` VALUES (37, 37, 12, 2);
INSERT INTO `property_relation` VALUES (38, 38, 12, 2);
INSERT INTO `property_relation` VALUES (39, 39, 12, 2);
INSERT INTO `property_relation` VALUES (40, 40, 13, 2);
INSERT INTO `property_relation` VALUES (41, 41, 13, 2);
INSERT INTO `property_relation` VALUES (42, 42, 13, 2);
INSERT INTO `property_relation` VALUES (43, 43, 13, 2);
INSERT INTO `property_relation` VALUES (44, 44, 14, 2);
INSERT INTO `property_relation` VALUES (45, 45, 14, 2);
INSERT INTO `property_relation` VALUES (46, 46, 14, 2);
INSERT INTO `property_relation` VALUES (47, 47, 14, 2);
INSERT INTO `property_relation` VALUES (48, 48, 16, 2);
INSERT INTO `property_relation` VALUES (49, 49, 16, 2);
INSERT INTO `property_relation` VALUES (50, 50, 17, 2);
INSERT INTO `property_relation` VALUES (51, 51, 17, 2);
INSERT INTO `property_relation` VALUES (52, 52, 18, 2);
INSERT INTO `property_relation` VALUES (53, 53, 18, 2);
INSERT INTO `property_relation` VALUES (54, 54, 19, 2);
INSERT INTO `property_relation` VALUES (55, 55, 19, 2);
INSERT INTO `property_relation` VALUES (56, 56, 19, 2);
INSERT INTO `property_relation` VALUES (57, 57, 20, 2);
INSERT INTO `property_relation` VALUES (58, 58, 20, 2);
INSERT INTO `property_relation` VALUES (59, 59, 20, 2);
INSERT INTO `property_relation` VALUES (60, 60, 20, 2);
INSERT INTO `property_relation` VALUES (61, 61, 21, 2);
INSERT INTO `property_relation` VALUES (62, 62, 21, 2);
INSERT INTO `property_relation` VALUES (63, 63, 21, 2);
INSERT INTO `property_relation` VALUES (64, 64, 21, 2);
INSERT INTO `property_relation` VALUES (65, 65, 22, 2);
INSERT INTO `property_relation` VALUES (66, 66, 22, 2);
INSERT INTO `property_relation` VALUES (67, 67, 22, 2);
INSERT INTO `property_relation` VALUES (68, 68, 23, 2);
INSERT INTO `property_relation` VALUES (69, 69, 23, 2);
INSERT INTO `property_relation` VALUES (70, 70, 23, 2);
INSERT INTO `property_relation` VALUES (71, 71, 24, 2);
INSERT INTO `property_relation` VALUES (72, 72, 24, 2);
INSERT INTO `property_relation` VALUES (73, 73, 24, 2);
INSERT INTO `property_relation` VALUES (74, 74, 24, 2);
INSERT INTO `property_relation` VALUES (75, 75, 25, 2);
INSERT INTO `property_relation` VALUES (76, 76, 25, 2);
INSERT INTO `property_relation` VALUES (77, 77, 25, 2);
INSERT INTO `property_relation` VALUES (78, 78, 26, 2);
INSERT INTO `property_relation` VALUES (79, 79, 26, 2);
INSERT INTO `property_relation` VALUES (80, 80, 26, 2);
INSERT INTO `property_relation` VALUES (81, 81, 27, 2);
INSERT INTO `property_relation` VALUES (82, 82, 27, 2);
INSERT INTO `property_relation` VALUES (83, 83, 28, 2);
INSERT INTO `property_relation` VALUES (84, 84, 28, 2);
INSERT INTO `property_relation` VALUES (85, 85, 29, 2);
INSERT INTO `property_relation` VALUES (86, 86, 29, 2);
INSERT INTO `property_relation` VALUES (87, 87, 29, 2);
INSERT INTO `property_relation` VALUES (88, 88, 30, 2);
INSERT INTO `property_relation` VALUES (89, 89, 30, 2);
INSERT INTO `property_relation` VALUES (90, 90, 30, 2);
INSERT INTO `property_relation` VALUES (91, 91, 30, 2);
INSERT INTO `property_relation` VALUES (92, 92, 30, 2);
INSERT INTO `property_relation` VALUES (93, 93, 31, 2);
INSERT INTO `property_relation` VALUES (94, 94, 31, 2);
INSERT INTO `property_relation` VALUES (95, 95, 31, 2);
INSERT INTO `property_relation` VALUES (96, 96, 31, 2);
INSERT INTO `property_relation` VALUES (97, 97, 31, 2);
INSERT INTO `property_relation` VALUES (98, 98, 32, 2);
INSERT INTO `property_relation` VALUES (99, 99, 32, 2);
INSERT INTO `property_relation` VALUES (100, 100, 32, 2);
INSERT INTO `property_relation` VALUES (101, 101, 33, 2);
INSERT INTO `property_relation` VALUES (102, 102, 33, 2);
INSERT INTO `property_relation` VALUES (103, 103, 33, 2);
INSERT INTO `property_relation` VALUES (104, 104, 33, 2);
INSERT INTO `property_relation` VALUES (105, 105, 33, 2);
INSERT INTO `property_relation` VALUES (106, 106, 33, 2);
INSERT INTO `property_relation` VALUES (107, 107, 34, 2);
INSERT INTO `property_relation` VALUES (108, 108, 34, 2);
INSERT INTO `property_relation` VALUES (109, 109, 34, 2);
INSERT INTO `property_relation` VALUES (110, 110, 34, 2);
INSERT INTO `property_relation` VALUES (111, 111, 34, 2);
INSERT INTO `property_relation` VALUES (112, 112, 34, 2);
INSERT INTO `property_relation` VALUES (113, 113, 35, 2);
INSERT INTO `property_relation` VALUES (114, 114, 35, 2);
INSERT INTO `property_relation` VALUES (115, 115, 35, 2);
INSERT INTO `property_relation` VALUES (116, 116, 36, 2);
INSERT INTO `property_relation` VALUES (117, 117, 36, 2);
INSERT INTO `property_relation` VALUES (118, 118, 36, 2);
INSERT INTO `property_relation` VALUES (119, 119, 36, 2);
INSERT INTO `property_relation` VALUES (120, 120, 36, 2);
INSERT INTO `property_relation` VALUES (121, 121, 36, 2);
INSERT INTO `property_relation` VALUES (122, 122, 36, 2);
INSERT INTO `property_relation` VALUES (123, 123, 37, 2);
INSERT INTO `property_relation` VALUES (124, 124, 37, 2);
INSERT INTO `property_relation` VALUES (125, 125, 37, 2);
INSERT INTO `property_relation` VALUES (126, 126, 37, 2);
INSERT INTO `property_relation` VALUES (127, 127, 37, 2);
INSERT INTO `property_relation` VALUES (128, 128, 37, 2);
INSERT INTO `property_relation` VALUES (129, 129, 37, 2);
INSERT INTO `property_relation` VALUES (130, 130, 38, 2);
INSERT INTO `property_relation` VALUES (131, 131, 38, 2);
INSERT INTO `property_relation` VALUES (132, 132, 38, 2);
INSERT INTO `property_relation` VALUES (133, 133, 38, 2);
INSERT INTO `property_relation` VALUES (134, 134, 39, 2);
INSERT INTO `property_relation` VALUES (135, 135, 39, 2);
INSERT INTO `property_relation` VALUES (136, 136, 39, 2);
INSERT INTO `property_relation` VALUES (137, 137, 39, 2);
INSERT INTO `property_relation` VALUES (138, 138, 40, 2);
INSERT INTO `property_relation` VALUES (139, 139, 40, 2);
INSERT INTO `property_relation` VALUES (140, 140, 40, 2);
INSERT INTO `property_relation` VALUES (141, 141, 40, 2);
INSERT INTO `property_relation` VALUES (142, 142, 40, 2);
INSERT INTO `property_relation` VALUES (143, 143, 40, 2);
INSERT INTO `property_relation` VALUES (144, 144, 41, 2);
INSERT INTO `property_relation` VALUES (145, 145, 41, 2);
INSERT INTO `property_relation` VALUES (146, 146, 41, 2);
INSERT INTO `property_relation` VALUES (147, 147, 41, 2);
INSERT INTO `property_relation` VALUES (148, 148, 43, 2);
INSERT INTO `property_relation` VALUES (149, 149, 43, 2);
INSERT INTO `property_relation` VALUES (150, 150, 43, 2);
INSERT INTO `property_relation` VALUES (151, 151, 43, 2);
INSERT INTO `property_relation` VALUES (152, 152, 44, 2);
INSERT INTO `property_relation` VALUES (153, 153, 44, 2);
INSERT INTO `property_relation` VALUES (154, 154, 44, 2);
INSERT INTO `property_relation` VALUES (155, 155, 44, 2);
INSERT INTO `property_relation` VALUES (156, 156, 45, 2);
INSERT INTO `property_relation` VALUES (157, 157, 45, 2);
INSERT INTO `property_relation` VALUES (158, 158, 45, 2);
INSERT INTO `property_relation` VALUES (159, 159, 45, 2);
INSERT INTO `property_relation` VALUES (160, 160, 45, 2);
INSERT INTO `property_relation` VALUES (161, 161, 45, 2);
INSERT INTO `property_relation` VALUES (162, 162, 46, 2);
INSERT INTO `property_relation` VALUES (163, 163, 46, 2);
INSERT INTO `property_relation` VALUES (164, 164, 46, 2);
INSERT INTO `property_relation` VALUES (165, 165, 46, 2);
INSERT INTO `property_relation` VALUES (166, 166, 48, 2);
INSERT INTO `property_relation` VALUES (167, 167, 48, 2);
INSERT INTO `property_relation` VALUES (168, 168, 48, 2);
INSERT INTO `property_relation` VALUES (169, 169, 48, 2);
INSERT INTO `property_relation` VALUES (170, 170, 49, 2);
INSERT INTO `property_relation` VALUES (171, 171, 49, 2);
INSERT INTO `property_relation` VALUES (172, 172, 49, 2);
INSERT INTO `property_relation` VALUES (173, 173, 49, 2);
INSERT INTO `property_relation` VALUES (174, 174, 50, 2);
INSERT INTO `property_relation` VALUES (175, 175, 50, 2);
INSERT INTO `property_relation` VALUES (176, 176, 50, 2);
INSERT INTO `property_relation` VALUES (177, 177, 50, 2);
INSERT INTO `property_relation` VALUES (178, 178, 50, 2);
INSERT INTO `property_relation` VALUES (179, 179, 50, 2);
INSERT INTO `property_relation` VALUES (180, 180, 51, 2);
INSERT INTO `property_relation` VALUES (181, 181, 51, 2);
INSERT INTO `property_relation` VALUES (182, 182, 51, 2);
INSERT INTO `property_relation` VALUES (183, 183, 51, 2);
INSERT INTO `property_relation` VALUES (184, 184, 53, 2);
INSERT INTO `property_relation` VALUES (185, 185, 53, 2);
INSERT INTO `property_relation` VALUES (186, 186, 53, 2);
INSERT INTO `property_relation` VALUES (187, 187, 53, 2);
INSERT INTO `property_relation` VALUES (188, 188, 54, 2);
INSERT INTO `property_relation` VALUES (189, 189, 54, 2);
INSERT INTO `property_relation` VALUES (190, 190, 54, 2);
INSERT INTO `property_relation` VALUES (191, 191, 54, 2);
INSERT INTO `property_relation` VALUES (192, 192, 55, 2);
INSERT INTO `property_relation` VALUES (193, 193, 55, 2);
INSERT INTO `property_relation` VALUES (194, 194, 55, 2);
INSERT INTO `property_relation` VALUES (195, 195, 55, 2);
INSERT INTO `property_relation` VALUES (196, 196, 55, 2);
INSERT INTO `property_relation` VALUES (197, 197, 55, 2);
INSERT INTO `property_relation` VALUES (198, 198, 56, 2);
INSERT INTO `property_relation` VALUES (199, 199, 56, 2);
INSERT INTO `property_relation` VALUES (200, 200, 56, 2);
INSERT INTO `property_relation` VALUES (201, 201, 56, 2);
INSERT INTO `property_relation` VALUES (202, 202, 58, 2);
INSERT INTO `property_relation` VALUES (203, 203, 58, 2);
INSERT INTO `property_relation` VALUES (204, 204, 58, 2);
INSERT INTO `property_relation` VALUES (205, 205, 58, 2);
INSERT INTO `property_relation` VALUES (206, 206, 59, 2);
INSERT INTO `property_relation` VALUES (207, 207, 59, 2);
INSERT INTO `property_relation` VALUES (208, 208, 59, 2);
INSERT INTO `property_relation` VALUES (209, 209, 59, 2);
INSERT INTO `property_relation` VALUES (210, 210, 60, 2);
INSERT INTO `property_relation` VALUES (211, 211, 60, 2);
INSERT INTO `property_relation` VALUES (212, 212, 60, 2);
INSERT INTO `property_relation` VALUES (213, 213, 60, 2);
INSERT INTO `property_relation` VALUES (214, 214, 60, 2);
INSERT INTO `property_relation` VALUES (215, 215, 60, 2);
INSERT INTO `property_relation` VALUES (216, 216, 61, 2);
INSERT INTO `property_relation` VALUES (217, 217, 61, 2);
INSERT INTO `property_relation` VALUES (218, 218, 61, 2);
INSERT INTO `property_relation` VALUES (219, 219, 61, 2);
INSERT INTO `property_relation` VALUES (220, 220, 63, 2);
INSERT INTO `property_relation` VALUES (221, 221, 63, 2);
INSERT INTO `property_relation` VALUES (222, 222, 63, 2);
INSERT INTO `property_relation` VALUES (223, 223, 63, 2);
INSERT INTO `property_relation` VALUES (224, 224, 64, 2);
INSERT INTO `property_relation` VALUES (225, 225, 64, 2);
INSERT INTO `property_relation` VALUES (226, 226, 64, 2);
INSERT INTO `property_relation` VALUES (227, 227, 64, 2);
INSERT INTO `property_relation` VALUES (228, 228, 65, 2);
INSERT INTO `property_relation` VALUES (229, 229, 65, 2);
INSERT INTO `property_relation` VALUES (230, 230, 65, 2);
INSERT INTO `property_relation` VALUES (231, 231, 65, 2);
INSERT INTO `property_relation` VALUES (232, 232, 65, 2);
INSERT INTO `property_relation` VALUES (233, 233, 65, 2);
INSERT INTO `property_relation` VALUES (234, 234, 66, 2);
INSERT INTO `property_relation` VALUES (235, 235, 66, 2);
INSERT INTO `property_relation` VALUES (236, 236, 66, 2);
INSERT INTO `property_relation` VALUES (237, 237, 66, 2);
INSERT INTO `property_relation` VALUES (238, 238, 68, 2);
INSERT INTO `property_relation` VALUES (239, 239, 68, 2);
INSERT INTO `property_relation` VALUES (240, 240, 68, 2);
INSERT INTO `property_relation` VALUES (241, 241, 68, 2);
INSERT INTO `property_relation` VALUES (242, 242, 69, 2);
INSERT INTO `property_relation` VALUES (243, 243, 69, 2);
INSERT INTO `property_relation` VALUES (244, 244, 69, 2);
INSERT INTO `property_relation` VALUES (245, 245, 69, 2);
INSERT INTO `property_relation` VALUES (246, 246, 70, 2);
INSERT INTO `property_relation` VALUES (247, 247, 70, 2);
INSERT INTO `property_relation` VALUES (248, 248, 70, 2);
INSERT INTO `property_relation` VALUES (249, 249, 70, 2);
INSERT INTO `property_relation` VALUES (250, 250, 70, 2);
INSERT INTO `property_relation` VALUES (251, 251, 70, 2);
INSERT INTO `property_relation` VALUES (252, 252, 71, 2);
INSERT INTO `property_relation` VALUES (253, 253, 71, 2);
INSERT INTO `property_relation` VALUES (254, 254, 71, 2);
INSERT INTO `property_relation` VALUES (255, 255, 71, 2);
INSERT INTO `property_relation` VALUES (256, 256, 73, 2);
INSERT INTO `property_relation` VALUES (257, 257, 73, 2);
INSERT INTO `property_relation` VALUES (258, 258, 73, 2);
INSERT INTO `property_relation` VALUES (259, 259, 74, 2);
INSERT INTO `property_relation` VALUES (260, 260, 74, 2);
INSERT INTO `property_relation` VALUES (261, 261, 74, 2);
INSERT INTO `property_relation` VALUES (262, 262, 74, 2);
INSERT INTO `property_relation` VALUES (263, 263, 74, 2);
INSERT INTO `property_relation` VALUES (264, 264, 74, 2);
INSERT INTO `property_relation` VALUES (265, 265, 75, 2);
INSERT INTO `property_relation` VALUES (266, 266, 75, 2);
INSERT INTO `property_relation` VALUES (267, 267, 75, 2);
INSERT INTO `property_relation` VALUES (268, 268, 75, 2);
INSERT INTO `property_relation` VALUES (269, 269, 75, 2);
INSERT INTO `property_relation` VALUES (270, 270, 75, 2);
INSERT INTO `property_relation` VALUES (271, 271, 76, 2);
INSERT INTO `property_relation` VALUES (272, 272, 76, 2);
INSERT INTO `property_relation` VALUES (273, 273, 76, 2);
INSERT INTO `property_relation` VALUES (274, 274, 77, 2);
INSERT INTO `property_relation` VALUES (275, 275, 77, 2);
INSERT INTO `property_relation` VALUES (276, 276, 77, 2);
INSERT INTO `property_relation` VALUES (277, 277, 77, 2);
INSERT INTO `property_relation` VALUES (278, 278, 77, 2);
INSERT INTO `property_relation` VALUES (279, 279, 77, 2);
INSERT INTO `property_relation` VALUES (280, 280, 77, 2);
INSERT INTO `property_relation` VALUES (281, 281, 78, 2);
INSERT INTO `property_relation` VALUES (282, 282, 78, 2);
INSERT INTO `property_relation` VALUES (283, 283, 78, 2);
INSERT INTO `property_relation` VALUES (284, 284, 78, 2);
INSERT INTO `property_relation` VALUES (285, 285, 78, 2);
INSERT INTO `property_relation` VALUES (286, 286, 78, 2);
INSERT INTO `property_relation` VALUES (287, 287, 78, 2);
INSERT INTO `property_relation` VALUES (288, 288, 79, 2);
INSERT INTO `property_relation` VALUES (289, 289, 81, 2);
INSERT INTO `property_relation` VALUES (290, 290, 81, 2);
INSERT INTO `property_relation` VALUES (291, 291, 94, 2);
INSERT INTO `property_relation` VALUES (292, 292, 97, 2);
INSERT INTO `property_relation` VALUES (293, 293, 97, 2);
INSERT INTO `property_relation` VALUES (294, 294, 98, 2);
INSERT INTO `property_relation` VALUES (295, 295, 98, 2);
INSERT INTO `property_relation` VALUES (296, 296, 99, 2);
INSERT INTO `property_relation` VALUES (297, 297, 102, 2);
INSERT INTO `property_relation` VALUES (298, 298, 102, 2);
INSERT INTO `property_relation` VALUES (299, 299, 103, 2);
INSERT INTO `property_relation` VALUES (300, 300, 103, 2);
INSERT INTO `property_relation` VALUES (301, 301, 104, 2);
INSERT INTO `property_relation` VALUES (302, 302, 110, 2);
INSERT INTO `property_relation` VALUES (303, 303, 113, 2);
INSERT INTO `property_relation` VALUES (304, 304, 116, 2);
INSERT INTO `property_relation` VALUES (305, 305, 119, 2);
INSERT INTO `property_relation` VALUES (306, 306, 119, 2);
INSERT INTO `property_relation` VALUES (307, 307, 120, 2);
INSERT INTO `property_relation` VALUES (308, 308, 121, 2);
INSERT INTO `property_relation` VALUES (309, 309, 121, 2);
INSERT INTO `property_relation` VALUES (310, 310, 121, 2);
INSERT INTO `property_relation` VALUES (311, 311, 121, 2);
INSERT INTO `property_relation` VALUES (312, 312, 122, 2);
INSERT INTO `property_relation` VALUES (313, 313, 122, 2);
INSERT INTO `property_relation` VALUES (314, 314, 122, 2);
INSERT INTO `property_relation` VALUES (315, 315, 123, 2);
INSERT INTO `property_relation` VALUES (316, 316, 123, 2);
INSERT INTO `property_relation` VALUES (317, 317, 123, 2);
INSERT INTO `property_relation` VALUES (318, 318, 123, 2);
INSERT INTO `property_relation` VALUES (319, 319, 123, 2);
INSERT INTO `property_relation` VALUES (320, 320, 123, 2);
INSERT INTO `property_relation` VALUES (321, 321, 124, 2);
INSERT INTO `property_relation` VALUES (322, 322, 124, 2);
INSERT INTO `property_relation` VALUES (323, 323, 124, 2);
INSERT INTO `property_relation` VALUES (324, 324, 124, 2);
INSERT INTO `property_relation` VALUES (325, 325, 124, 2);
INSERT INTO `property_relation` VALUES (326, 326, 124, 2);
INSERT INTO `property_relation` VALUES (327, 327, 125, 2);
INSERT INTO `property_relation` VALUES (328, 328, 125, 2);
INSERT INTO `property_relation` VALUES (329, 329, 125, 2);
INSERT INTO `property_relation` VALUES (330, 330, 126, 2);
INSERT INTO `property_relation` VALUES (331, 331, 126, 2);
INSERT INTO `property_relation` VALUES (332, 332, 126, 2);
INSERT INTO `property_relation` VALUES (333, 333, 126, 2);
INSERT INTO `property_relation` VALUES (334, 334, 126, 2);
INSERT INTO `property_relation` VALUES (335, 335, 126, 2);
INSERT INTO `property_relation` VALUES (336, 336, 126, 2);
INSERT INTO `property_relation` VALUES (337, 337, 127, 2);
INSERT INTO `property_relation` VALUES (338, 338, 127, 2);
INSERT INTO `property_relation` VALUES (339, 339, 127, 2);
INSERT INTO `property_relation` VALUES (340, 340, 127, 2);
INSERT INTO `property_relation` VALUES (341, 341, 127, 2);
INSERT INTO `property_relation` VALUES (342, 342, 127, 2);
INSERT INTO `property_relation` VALUES (343, 343, 127, 2);

-- ----------------------------
-- Table structure for property_value
-- ----------------------------
DROP TABLE IF EXISTS `property_value`;
CREATE TABLE `property_value`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID（自增）',
  `prop_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '属性名称（如 placeholder、disabled、rules 等）',
  `transformer_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '属性值转换器类型（通过 Spring Bean 名称获取实例，进行类型转换）',
  `prop_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '属性值（JSON 或纯文本格式）',
  `value_class` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '属性值对应的目标类全限定名（用于反序列化）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `name_idx`(`prop_name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 344 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '属性设置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of property_value
-- ----------------------------
INSERT INTO `property_value` VALUES (1, 'type', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"number\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (2, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入用户ID\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (3, 'prefix-icon', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"el-icon-search\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (4, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入用户名\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (5, 'prefix-icon', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"el-icon-user\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (6, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入昵称\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (7, 'prefix-icon', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"el-icon-star-off\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (8, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请选择用户状态\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (9, 'options', 'com.tbs.view.impl.JsonPropertyValueTransformer', '[{\"label\":\"激活的\",\"value\":\"1\"},{\"label\":\"被锁定的\",\"value\":\"2\"},{\"label\":\"禁用的\",\"value\":\"0\"}]', 'java.util.ArrayList');
INSERT INTO `property_value` VALUES (10, 'value-format', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"yyyy-MM-dd HH:mm:ss\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (11, 'format', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"yyyy-MM-dd\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (12, 'start-placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"开始日期\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (13, 'end-placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"结束日期\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (14, 'value-format', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"yyyy-MM-dd HH:mm:ss\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (15, 'format', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"yyyy-MM-dd\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (16, 'start-placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"开始日期\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (17, 'end-placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"结束日期\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (18, 'size', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"medium\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (19, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入用户名\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (20, 'clearable', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (21, 'prefix-icon', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"el-icon-user\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (22, 'size', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"medium\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (23, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入昵称\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (24, 'clearable', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (25, 'prefix-icon', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"el-icon-star-off\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (26, 'size', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"medium\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (27, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入密码\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (28, 'clearable', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (29, 'prefix-icon', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"el-icon-lock\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (30, 'type', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"password\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (31, 'showPassword', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (32, 'size', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"medium\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (33, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入备注信息\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (34, 'type', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"textarea\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (35, 'rows', 'com.tbs.view.impl.JsonPropertyValueTransformer', '2', 'java.lang.Integer');
INSERT INTO `property_value` VALUES (36, 'size', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"medium\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (37, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入用户名\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (38, 'clearable', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (39, 'prefix-icon', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"el-icon-user\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (40, 'size', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"medium\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (41, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入昵称\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (42, 'clearable', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (43, 'prefix-icon', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"el-icon-star-off\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (44, 'size', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"medium\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (45, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入备注信息\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (46, 'type', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"textarea\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (47, 'rows', 'com.tbs.view.impl.JsonPropertyValueTransformer', '2', 'java.lang.Integer');
INSERT INTO `property_value` VALUES (48, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入公司Id\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (49, 'prefix-icon', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"el-icon-search\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (50, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入公司名称\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (51, 'prefix-icon', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"el-icon-office-building\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (52, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入税号\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (53, 'prefix-icon', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"el-icon-document\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (54, 'size', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"medium\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (55, 'multiple', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (56, 'options', 'com.tbs.view.impl.JsonPropertyValueTransformer', '[{\"label\":\"系统\",\"value\":\"5\"},{\"label\":\"已激活\",\"value\":\"1\"},{\"label\":\"禁用\",\"value\":\"2\"},{\"label\":\"已删除\",\"value\":\"0\"}]', 'java.util.ArrayList');
INSERT INTO `property_value` VALUES (57, 'value-format', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"yyyy-MM-dd HH:mm:ss\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (58, 'format', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"yyyy-MM-dd\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (59, 'start-placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"开始日期\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (60, 'end-placeholer', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"结束日期\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (61, 'value-format', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"yyyy-MM-dd HH:mm:ss\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (62, 'format', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"yyyy-MM-dd\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (63, 'start-placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"开始日期\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (64, 'end-placeholer', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"结束日期\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (65, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入公司名称\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (66, 'prefix-icon', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"el-icon-office-building\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (67, 'size', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"medium\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (68, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入昵称\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (69, 'prefix-icon', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"el-icon-document\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (70, 'size', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"medium\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (71, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入公司描述信息\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (72, 'size', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"medium\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (73, 'type', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"textarea\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (74, 'rows', 'com.tbs.view.impl.JsonPropertyValueTransformer', '5', 'java.lang.Integer');
INSERT INTO `property_value` VALUES (75, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入LogoUrl\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (76, 'size', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"medium\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (77, 'prefix-icon', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"el-icon-picture-outline\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (78, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入用户ID\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (79, 'type', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"number\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (80, 'prefix-icon', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"el-icon-search\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (81, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入用户名\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (82, 'prefix-icon', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"el-icon-user\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (83, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入昵称\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (84, 'prefix-icon', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"el-icon-star-off\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (85, 'options', 'com.tbs.view.impl.JsonPropertyValueTransformer', '[{\"label\":\"激活的\",\"value\":\"1\"},{\"label\":\"被锁定的\",\"value\":\"2\"},{\"label\":\"禁用的\",\"value\":\"0\"}]', 'java.util.ArrayList');
INSERT INTO `property_value` VALUES (86, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请选择用户状态\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (87, 'multiple', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (88, 'start-placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"开始日期\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (89, 'value-format', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"yyyy-MM-dd HH:mm:ss\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (90, 'format', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"yyyy-MM-dd\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (91, 'end-placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"结束日期\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (92, 'type', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"date\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (93, 'start-placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"开始日期\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (94, 'value-format', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"yyyy-MM-dd HH:mm:ss\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (95, 'format', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"yyyy-MM-dd\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (96, 'end-placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"结束日期\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (97, 'type', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"date\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (98, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入用户ID\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (99, 'type', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"number\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (100, 'prefix-icon', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"el-icon-search\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (101, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入用户名\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (102, 'prefix-icon', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"el-icon-user\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (103, 'type', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"text\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (104, 'clearable', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (105, 'show-word-limit', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (106, 'maxlength', 'com.tbs.view.impl.JsonPropertyValueTransformer', '64', 'java.lang.Integer');
INSERT INTO `property_value` VALUES (107, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入昵称\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (108, 'prefix-icon', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"el-icon-star-off\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (109, 'maxlength', 'com.tbs.view.impl.JsonPropertyValueTransformer', '64', 'java.lang.Integer');
INSERT INTO `property_value` VALUES (110, 'clearable', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (111, 'show-word-limit', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (112, 'type', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"text\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (113, 'options', 'com.tbs.view.impl.JsonPropertyValueTransformer', '[{\"label\":\"激活的\",\"value\":\"1\"},{\"label\":\"被锁定的\",\"value\":\"2\"},{\"label\":\"禁用的\",\"value\":\"0\"}]', 'java.util.ArrayList');
INSERT INTO `property_value` VALUES (114, 'multiple', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (115, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请选择用户状态\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (116, 'start-placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"开始日期\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (117, 'value-format', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"yyyy-MM-dd HH:mm:ss\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (118, 'format', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"yyyy-MM-dd\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (119, 'type', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"date\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (120, 'end-placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"结束日期\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (121, 'size', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"small\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (122, 'clearable', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (123, 'start-placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"开始日期\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (124, 'value-format', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"yyyy-MM-dd HH:mm:ss\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (125, 'format', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"yyyy-MM-dd\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (126, 'type', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"date\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (127, 'end-placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"结束日期\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (128, 'size', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"small\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (129, 'clearable', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (130, 'clearable', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (131, 'size', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"medium\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (132, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入用户名\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (133, 'prefix-icon', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"el-icon-user\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (134, 'clearable', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (135, 'size', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"medium\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (136, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入昵称\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (137, 'prefix-icon', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"el-icon-star-off\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (138, 'clearable', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (139, 'size', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"medium\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (140, 'showPassword', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (141, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入密码\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (142, 'type', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"password\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (143, 'prefix-icon', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"el-icon-lock\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (144, 'size', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"medium\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (145, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入备注信息\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (146, 'type', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"textarea\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (147, 'rows', 'com.tbs.view.impl.JsonPropertyValueTransformer', '2', 'java.lang.Integer');
INSERT INTO `property_value` VALUES (148, 'clearable', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (149, 'size', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"medium\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (150, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入用户名\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (151, 'prefix-icon', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"el-icon-user\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (152, 'clearable', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (153, 'size', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"medium\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (154, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入昵称\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (155, 'prefix-icon', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"el-icon-star-off\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (156, 'clearable', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (157, 'size', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"medium\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (158, 'showPassword', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (159, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入密码\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (160, 'type', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"password\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (161, 'prefix-icon', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"el-icon-lock\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (162, 'size', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"medium\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (163, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入备注信息\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (164, 'type', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"textarea\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (165, 'rows', 'com.tbs.view.impl.JsonPropertyValueTransformer', '2', 'java.lang.Integer');
INSERT INTO `property_value` VALUES (166, 'clearable', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (167, 'size', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"medium\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (168, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入用户名\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (169, 'prefix-icon', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"el-icon-user\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (170, 'clearable', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (171, 'size', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"medium\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (172, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入昵称\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (173, 'prefix-icon', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"el-icon-star-off\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (174, 'clearable', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (175, 'size', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"medium\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (176, 'showPassword', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (177, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入密码\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (178, 'type', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"password\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (179, 'prefix-icon', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"el-icon-lock\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (180, 'size', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"medium\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (181, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入备注信息\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (182, 'type', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"textarea\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (183, 'rows', 'com.tbs.view.impl.JsonPropertyValueTransformer', '2', 'java.lang.Integer');
INSERT INTO `property_value` VALUES (184, 'clearable', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (185, 'size', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"medium\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (186, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入用户名\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (187, 'prefix-icon', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"el-icon-user\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (188, 'clearable', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (189, 'size', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"medium\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (190, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入昵称\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (191, 'prefix-icon', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"el-icon-star-off\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (192, 'clearable', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (193, 'size', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"medium\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (194, 'showPassword', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (195, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入密码\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (196, 'type', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"password\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (197, 'prefix-icon', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"el-icon-lock\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (198, 'size', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"medium\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (199, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入备注信息\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (200, 'type', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"textarea\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (201, 'rows', 'com.tbs.view.impl.JsonPropertyValueTransformer', '2', 'java.lang.Integer');
INSERT INTO `property_value` VALUES (202, 'clearable', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (203, 'size', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"medium\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (204, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入用户名\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (205, 'prefix-icon', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"el-icon-user\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (206, 'clearable', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (207, 'size', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"medium\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (208, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入昵称\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (209, 'prefix-icon', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"el-icon-star-off\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (210, 'clearable', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (211, 'size', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"medium\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (212, 'showPassword', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (213, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入密码\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (214, 'type', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"password\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (215, 'prefix-icon', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"el-icon-lock\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (216, 'size', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"medium\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (217, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入备注信息\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (218, 'type', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"textarea\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (219, 'rows', 'com.tbs.view.impl.JsonPropertyValueTransformer', '2', 'java.lang.Integer');
INSERT INTO `property_value` VALUES (220, 'clearable', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (221, 'size', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"medium\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (222, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入用户名\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (223, 'prefix-icon', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"el-icon-user\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (224, 'clearable', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (225, 'size', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"medium\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (226, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入昵称\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (227, 'prefix-icon', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"el-icon-star-off\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (228, 'clearable', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (229, 'size', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"medium\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (230, 'showPassword', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (231, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入密码\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (232, 'type', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"password\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (233, 'prefix-icon', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"el-icon-lock\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (234, 'size', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"medium\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (235, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入备注信息\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (236, 'type', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"textarea\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (237, 'rows', 'com.tbs.view.impl.JsonPropertyValueTransformer', '2', 'java.lang.Integer');
INSERT INTO `property_value` VALUES (238, 'clearable', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (239, 'size', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"medium\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (240, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入用户名\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (241, 'prefix-icon', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"el-icon-user\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (242, 'clearable', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (243, 'size', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"medium\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (244, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入昵称\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (245, 'prefix-icon', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"el-icon-star-off\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (246, 'clearable', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (247, 'size', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"medium\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (248, 'showPassword', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (249, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入密码\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (250, 'type', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"password\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (251, 'prefix-icon', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"el-icon-lock\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (252, 'size', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"medium\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (253, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入备注信息\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (254, 'type', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"textarea\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (255, 'rows', 'com.tbs.view.impl.JsonPropertyValueTransformer', '2', 'java.lang.Integer');
INSERT INTO `property_value` VALUES (256, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入用户ID\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (257, 'type', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"number\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (258, 'prefix-icon', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"el-icon-search\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (259, 'clearable', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (260, 'maxlength', 'com.tbs.view.impl.JsonPropertyValueTransformer', '64', 'java.lang.Integer');
INSERT INTO `property_value` VALUES (261, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入用户名\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (262, 'show-word-limit', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (263, 'type', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"text\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (264, 'prefix-icon', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"el-icon-user\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (265, 'clearable', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (266, 'maxlength', 'com.tbs.view.impl.JsonPropertyValueTransformer', '64', 'java.lang.Integer');
INSERT INTO `property_value` VALUES (267, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入昵称\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (268, 'show-word-limit', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (269, 'type', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"text\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (270, 'prefix-icon', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"el-icon-star-off\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (271, 'options', 'com.tbs.view.impl.JsonPropertyValueTransformer', '[{\"label\":\"激活的\",\"value\":\"1\"},{\"label\":\"被锁定的\",\"value\":\"2\"},{\"label\":\"禁用的\",\"value\":\"0\"}]', 'java.util.ArrayList');
INSERT INTO `property_value` VALUES (272, 'multiple', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (273, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请选择用户状态\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (274, 'clearable', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (275, 'size', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"small\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (276, 'start-placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"开始日期\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (277, 'value-format', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"yyyy-MM-dd HH:mm:ss\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (278, 'format', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"yyyy-MM-dd\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (279, 'type', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"date\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (280, 'end-placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"结束日期\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (281, 'clearable', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (282, 'size', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"small\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (283, 'start-placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"开始日期\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (284, 'value-format', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"yyyy-MM-dd HH:mm:ss\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (285, 'format', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"yyyy-MM-dd\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (286, 'type', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"date\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (287, 'end-placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"结束日期\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (288, 'type', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"number\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (289, 'multiple', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (290, 'options', 'com.tbs.view.impl.JsonPropertyValueTransformer', '[{\"label\":\"激活\",\"value\":\"1\"},{\"label\":\"系统权限\",\"value\":\"2\"},{\"label\":\"失效\",\"value\":\"0\"}]', 'java.util.ArrayList');
INSERT INTO `property_value` VALUES (291, 'type', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"number\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (292, 'multiple', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (293, 'options', 'com.tbs.view.impl.JsonPropertyValueTransformer', '[{\"label\":\"激活\",\"value\":\"1\"},{\"label\":\"系统权限\",\"value\":\"2\"},{\"label\":\"失效\",\"value\":\"0\"}]', 'java.util.ArrayList');
INSERT INTO `property_value` VALUES (294, 'multiple', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (295, 'options', 'com.tbs.view.impl.JsonPropertyValueTransformer', '[{\"label\":\"菜单权限\",\"value\":\"1\"},{\"label\":\"数据权限\",\"value\":\"2\"},{\"label\":\"API权限\",\"value\":\"3\"}]', 'java.util.ArrayList');
INSERT INTO `property_value` VALUES (296, 'type', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"number\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (297, 'multiple', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (298, 'options', 'com.tbs.view.impl.JsonPropertyValueTransformer', '[{\"label\":\"激活\",\"value\":\"1\"},{\"label\":\"系统权限\",\"value\":\"2\"},{\"label\":\"失效\",\"value\":\"0\"}]', 'java.util.ArrayList');
INSERT INTO `property_value` VALUES (299, 'multiple', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (300, 'options', 'com.tbs.view.impl.JsonPropertyValueTransformer', '[{\"label\":\"菜单权限\",\"value\":\"1\"},{\"label\":\"数据权限\",\"value\":\"2\"},{\"label\":\"API权限\",\"value\":\"3\"}]', 'java.util.ArrayList');
INSERT INTO `property_value` VALUES (301, 'options', 'com.tbs.view.impl.JsonPropertyValueTransformer', '[{\"label\":\"菜单权限\",\"value\":\"1\"},{\"label\":\"数据权限\",\"value\":\"2\"},{\"label\":\"API权限\",\"value\":\"3\"}]', 'java.util.ArrayList');
INSERT INTO `property_value` VALUES (302, 'options', 'com.tbs.view.impl.JsonPropertyValueTransformer', '[{\"label\":\"0\",\"value\":\"禁用\"},{\"label\":\"1\",\"value\":\"启用\"}]', 'java.util.ArrayList');
INSERT INTO `property_value` VALUES (303, 'options', 'com.tbs.view.impl.JsonPropertyValueTransformer', '[{\"label\":\"0\",\"value\":\"禁用\"},{\"label\":\"1\",\"value\":\"启用\"}]', 'java.util.ArrayList');
INSERT INTO `property_value` VALUES (304, 'options', 'com.tbs.view.impl.JsonPropertyValueTransformer', '[{\"label\":\"禁用\",\"value\":\"0\"},{\"label\":\"启用\",\"value\":\"1\"}]', 'java.util.ArrayList');
INSERT INTO `property_value` VALUES (305, 'multiple', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (306, 'options', 'com.tbs.view.impl.JsonPropertyValueTransformer', '[{\"label\":\"禁用\",\"value\":\"0\"},{\"label\":\"启用\",\"value\":\"1\"}]', 'java.util.ArrayList');
INSERT INTO `property_value` VALUES (307, 'clearable', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (308, 'type', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"text\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (309, 'size', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"large\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (310, 'show-word-limit', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'false', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (311, 'clearable', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (312, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入用户ID\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (313, 'type', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"number\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (314, 'prefix-icon', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"el-icon-search\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (315, 'clearable', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (316, 'maxlength', 'com.tbs.view.impl.JsonPropertyValueTransformer', '64', 'java.lang.Integer');
INSERT INTO `property_value` VALUES (317, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入用户名\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (318, 'show-word-limit', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (319, 'type', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"text\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (320, 'prefix-icon', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"el-icon-user\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (321, 'clearable', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (322, 'maxlength', 'com.tbs.view.impl.JsonPropertyValueTransformer', '64', 'java.lang.Integer');
INSERT INTO `property_value` VALUES (323, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请输入昵称\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (324, 'show-word-limit', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (325, 'type', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"text\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (326, 'prefix-icon', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"el-icon-star-off\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (327, 'options', 'com.tbs.view.impl.JsonPropertyValueTransformer', '[{\"label\":\"激活的\",\"value\":\"1\"},{\"label\":\"被锁定的\",\"value\":\"2\"},{\"label\":\"禁用的\",\"value\":\"0\"}]', 'java.util.ArrayList');
INSERT INTO `property_value` VALUES (328, 'multiple', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (329, 'placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"请选择用户状态\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (330, 'clearable', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (331, 'size', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"small\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (332, 'start-placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"开始日期\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (333, 'value-format', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"yyyy-MM-dd HH:mm:ss\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (334, 'format', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"yyyy-MM-dd\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (335, 'type', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"date\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (336, 'end-placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"结束日期\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (337, 'clearable', 'com.tbs.view.impl.JsonPropertyValueTransformer', 'true', 'java.lang.Boolean');
INSERT INTO `property_value` VALUES (338, 'size', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"small\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (339, 'start-placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"开始日期\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (340, 'value-format', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"yyyy-MM-dd HH:mm:ss\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (341, 'format', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"yyyy-MM-dd\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (342, 'type', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"date\"', 'java.lang.String');
INSERT INTO `property_value` VALUES (343, 'end-placeholder', 'com.tbs.view.impl.JsonPropertyValueTransformer', '\"结束日期\"', 'java.lang.String');

-- ----------------------------
-- Table structure for push_message
-- ----------------------------
DROP TABLE IF EXISTS `push_message`;
CREATE TABLE `push_message`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键（即业务消息标识，自增）',
  `content` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '消息正文',
  `create_time` datetime(6) NOT NULL COMMENT '创建时间（即消息发送时间）',
  `ext_info` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '扩展信息（JSON，业务自定义字段）',
  `msg_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息类型（可扩展字典，现有契约值：notify-通知，warning-告警，reminder-提醒）',
  `priority` varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息优先级：low-低，normal-普通（缺省），high-高，urgent-紧急',
  `sender` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发送者展示标识（审计权威见操作日志表 SEND 记录）',
  `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '消息标题',
  `topic` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务主题（接收人按订阅主题匹配投递）',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '点击跳转地址',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 775958078553063425 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '推送业务消息主表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of push_message
-- ----------------------------
INSERT INTO `push_message` VALUES (6, '你好', '2026-09-22 09:43:07.451000', NULL, 'notify', 'normal', '223344', 'Hello World', 'global.broadcast', '2026-09-22 09:43:07.451000', '');
INSERT INTO `push_message` VALUES (7, '你好', '2026-09-22 09:43:22.059000', NULL, 'notify', 'normal', '223344', 'Hello World', 'global.broadcast', '2026-09-22 09:43:22.059000', '');
INSERT INTO `push_message` VALUES (8, '你好', '2026-09-22 09:43:31.579000', NULL, 'notify', 'normal', '223344', 'Hello World', 'global.broadcast', '2026-09-22 09:43:31.579000', '');
INSERT INTO `push_message` VALUES (9, '请看用户信息', '2026-09-22 09:49:00.712000', NULL, 'notify', 'normal', '223344', '请看用户信息', 'global.broadcast', '2026-09-22 09:49:00.712000', '/user/details/1');
INSERT INTO `push_message` VALUES (10, '1234', '2026-09-22 09:51:16.574000', NULL, 'notify', 'normal', '223344', 'nihao', 'user.name.223344', '2026-09-22 09:51:16.574000', '');
INSERT INTO `push_message` VALUES (11, '1234', '2026-09-22 09:51:16.571000', NULL, 'notify', 'normal', '223344', 'nihao', 'user.name.01505', '2026-09-22 09:51:16.571000', '');
INSERT INTO `push_message` VALUES (12, '我很好', '2026-09-22 10:01:16.573000', NULL, 'notify', 'normal', '223344', '你好', 'global.broadcast', '2026-09-22 10:01:16.573000', '');
INSERT INTO `push_message` VALUES (13, '123', '2026-10-05 10:37:49.106000', NULL, 'notify', 'normal', '223344', '你好', 'global.broadcast', '2026-10-05 10:37:49.106000', '');
INSERT INTO `push_message` VALUES (14, '123', '2026-10-05 10:47:03.403000', NULL, 'notify', 'normal', '223344', '你好', 'global.broadcast', '2026-10-05 10:47:03.403000', '');
INSERT INTO `push_message` VALUES (774860932705681408, '123', '2026-11-09 13:00:42.557000', NULL, 'notify', 'normal', '223344', '123', 'global.broadcast', '2026-11-09 13:00:42.557000', '');
INSERT INTO `push_message` VALUES (775958078553063424, '123123', '2026-11-12 13:40:22.522000', NULL, 'notify', 'normal', '223344', '123', 'global.broadcast', '2026-11-12 13:40:22.522000', '');

-- ----------------------------
-- Table structure for push_message_operation_log
-- ----------------------------
DROP TABLE IF EXISTS `push_message_operation_log`;
CREATE TABLE `push_message_operation_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键（自增）',
  `create_time` datetime(6) NOT NULL COMMENT '创建时间',
  `ext_info` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作上下文扩展（JSON，服务名、调用方标识、点击位置等）',
  `message_id` bigint NOT NULL COMMENT '消息ID（关联 push_message.id）',
  `op_source` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作来源通道（http/rpc/job/system）',
  `op_time` datetime(6) NOT NULL COMMENT '操作发生时间',
  `op_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作类型：SEND-发送，READ-已读，CLICK-点击',
  `operator` bigint NULL DEFAULT NULL COMMENT '操作人用户ID（系统/服务发起时可空，身份进 ext_info）',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_operator_msg`(`operator` ASC, `message_id` ASC) USING BTREE,
  INDEX `idx_message`(`message_id` ASC) USING BTREE,
  INDEX `idx_op_time`(`op_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 777444072045412353 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '推送消息全链路操作日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of push_message_operation_log
-- ----------------------------
INSERT INTO `push_message_operation_log` VALUES (13, '2026-09-22 09:43:07.451000', NULL, 6, 'http', '2026-09-22 09:43:07.451000', 'SEND', 1, '2026-09-22 09:43:07.451000');
INSERT INTO `push_message_operation_log` VALUES (14, '2026-09-22 09:43:22.059000', NULL, 7, 'http', '2026-09-22 09:43:22.059000', 'SEND', 1, '2026-09-22 09:43:22.059000');
INSERT INTO `push_message_operation_log` VALUES (15, '2026-09-22 09:43:31.579000', NULL, 8, 'http', '2026-09-22 09:43:31.579000', 'SEND', 1, '2026-09-22 09:43:31.579000');
INSERT INTO `push_message_operation_log` VALUES (16, '2026-09-22 09:49:00.712000', NULL, 9, 'http', '2026-09-22 09:49:00.712000', 'SEND', 1, '2026-09-22 09:49:00.712000');
INSERT INTO `push_message_operation_log` VALUES (17, '2026-09-22 09:49:11.668000', 'message-center', 9, 'http', '2026-09-22 09:49:11.668000', 'CLICK', 1, '2026-09-22 09:49:11.668000');
INSERT INTO `push_message_operation_log` VALUES (18, '2026-09-22 09:49:11.668000', NULL, 9, 'http', '2026-09-22 09:49:11.668000', 'READ', 1, '2026-09-22 09:49:11.668000');
INSERT INTO `push_message_operation_log` VALUES (19, '2026-09-22 09:49:11.705000', NULL, 9, 'http', '2026-09-22 09:49:11.705000', 'READ', 1, '2026-09-22 09:49:11.705000');
INSERT INTO `push_message_operation_log` VALUES (20, '2026-09-22 09:50:43.949000', NULL, 6, 'http', '2026-09-22 09:50:43.949000', 'READ', 1, '2026-09-22 09:50:43.949000');
INSERT INTO `push_message_operation_log` VALUES (21, '2026-09-22 09:50:44.665000', NULL, 7, 'http', '2026-09-22 09:50:44.665000', 'READ', 1, '2026-09-22 09:50:44.665000');
INSERT INTO `push_message_operation_log` VALUES (22, '2026-09-22 09:50:45.883000', NULL, 8, 'http', '2026-09-22 09:50:45.883000', 'READ', 1, '2026-09-22 09:50:45.883000');
INSERT INTO `push_message_operation_log` VALUES (23, '2026-09-22 09:51:16.571000', NULL, 11, 'http', '2026-09-22 09:51:16.571000', 'SEND', 1, '2026-09-22 09:51:16.571000');
INSERT INTO `push_message_operation_log` VALUES (24, '2026-09-22 09:51:16.574000', NULL, 10, 'http', '2026-09-22 09:51:16.574000', 'SEND', 1, '2026-09-22 09:51:16.574000');
INSERT INTO `push_message_operation_log` VALUES (25, '2026-09-22 10:00:48.740000', NULL, 10, 'http', '2026-09-22 10:00:48.740000', 'READ', 1, '2026-09-22 10:00:48.740000');
INSERT INTO `push_message_operation_log` VALUES (26, '2026-09-22 10:01:16.573000', NULL, 12, 'http', '2026-09-22 10:01:16.573000', 'SEND', 1, '2026-09-22 10:01:16.573000');
INSERT INTO `push_message_operation_log` VALUES (27, '2026-09-22 10:01:29.276000', 'message-center', 9, 'http', '2026-09-22 10:01:29.276000', 'CLICK', 18, '2026-09-22 10:01:29.276000');
INSERT INTO `push_message_operation_log` VALUES (28, '2026-09-22 10:01:29.273000', NULL, 9, 'http', '2026-09-22 10:01:29.273000', 'READ', 18, '2026-09-22 10:01:29.273000');
INSERT INTO `push_message_operation_log` VALUES (29, '2026-09-22 10:01:29.276000', NULL, 9, 'http', '2026-09-22 10:01:29.276000', 'READ', 18, '2026-09-22 10:01:29.276000');
INSERT INTO `push_message_operation_log` VALUES (30, '2026-09-22 10:01:41.944000', NULL, 11, 'http', '2026-09-22 10:01:41.944000', 'READ', 18, '2026-09-22 10:01:41.944000');
INSERT INTO `push_message_operation_log` VALUES (31, '2026-09-22 10:01:42.750000', NULL, 12, 'http', '2026-09-22 10:01:42.750000', 'READ', 18, '2026-09-22 10:01:42.750000');
INSERT INTO `push_message_operation_log` VALUES (32, '2026-09-22 10:01:44.826000', NULL, 6, 'http', '2026-09-22 10:01:44.826000', 'READ', 18, '2026-09-22 10:01:44.826000');
INSERT INTO `push_message_operation_log` VALUES (33, '2026-09-22 10:01:44.826000', NULL, 7, 'http', '2026-09-22 10:01:44.826000', 'READ', 18, '2026-09-22 10:01:44.826000');
INSERT INTO `push_message_operation_log` VALUES (34, '2026-09-22 10:01:44.826000', NULL, 8, 'http', '2026-09-22 10:01:44.826000', 'READ', 18, '2026-09-22 10:01:44.826000');
INSERT INTO `push_message_operation_log` VALUES (35, '2026-09-22 10:03:29.010000', 'message-center', 9, 'http', '2026-09-22 10:03:29.010000', 'CLICK', 18, '2026-09-22 10:03:29.010000');
INSERT INTO `push_message_operation_log` VALUES (36, '2026-09-22 10:13:17.788000', NULL, 12, 'http', '2026-09-22 10:13:17.788000', 'READ', 1, '2026-09-22 10:13:17.788000');
INSERT INTO `push_message_operation_log` VALUES (37, '2026-09-22 10:13:41.731000', 'message-center', 9, 'http', '2026-09-22 10:13:41.731000', 'CLICK', 1, '2026-09-22 10:13:41.731000');
INSERT INTO `push_message_operation_log` VALUES (38, '2026-09-22 10:17:16.791000', 'message-center', 9, 'http', '2026-09-22 10:17:16.791000', 'CLICK', 1, '2026-09-22 10:17:16.791000');
INSERT INTO `push_message_operation_log` VALUES (39, '2026-09-22 10:17:58.219000', 'message-center', 9, 'http', '2026-09-22 10:17:58.219000', 'CLICK', 1, '2026-09-22 10:17:58.219000');
INSERT INTO `push_message_operation_log` VALUES (40, '2026-10-05 10:37:49.106000', NULL, 13, 'http', '2026-10-05 10:37:49.106000', 'SEND', 1, '2026-10-05 10:37:49.106000');
INSERT INTO `push_message_operation_log` VALUES (41, '2026-10-05 10:47:03.403000', NULL, 14, 'http', '2026-10-05 10:47:03.403000', 'SEND', 1, '2026-10-05 10:47:03.403000');
INSERT INTO `push_message_operation_log` VALUES (42, '2026-10-05 10:47:08.188000', NULL, 14, 'http', '2026-10-05 10:47:08.188000', 'READ', 1, '2026-10-05 10:47:08.188000');
INSERT INTO `push_message_operation_log` VALUES (43, '2026-10-05 10:47:08.188000', NULL, 13, 'http', '2026-10-05 10:47:08.188000', 'READ', 1, '2026-10-05 10:47:08.188000');
INSERT INTO `push_message_operation_log` VALUES (774860932739235840, '2026-11-09 13:00:42.557000', NULL, 774860932705681408, 'http', '2026-11-09 13:00:42.557000', 'SEND', 1, '2026-11-09 13:00:42.557000');
INSERT INTO `push_message_operation_log` VALUES (774860941752795136, '2026-11-09 13:00:44.724000', NULL, 774860932705681408, 'http', '2026-11-09 13:00:44.724000', 'READ', 1, '2026-11-09 13:00:44.724000');
INSERT INTO `push_message_operation_log` VALUES (775958078599200768, '2026-11-12 13:40:22.522000', NULL, 775958078553063424, 'http', '2026-11-12 13:40:22.522000', 'SEND', 1, '2026-11-12 13:40:22.522000');
INSERT INTO `push_message_operation_log` VALUES (777444072045412352, '2026-11-16 16:05:10.980000', NULL, 775958078553063424, 'http', '2026-11-16 16:05:10.980000', 'READ', 1, '2026-11-16 16:05:10.980000');

-- ----------------------------
-- Table structure for push_message_receiver
-- ----------------------------
DROP TABLE IF EXISTS `push_message_receiver`;
CREATE TABLE `push_message_receiver`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键（兼游标分页排序键，自增即到达序）',
  `create_time` datetime(6) NOT NULL COMMENT '创建时间（即消息到达时间）',
  `message_id` bigint NOT NULL COMMENT '消息ID（关联 push_message.id）',
  `receiver` bigint NOT NULL COMMENT '接收用户ID',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_receiver`(`receiver` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 775958078578229257 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '推送消息接收人关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of push_message_receiver
-- ----------------------------
INSERT INTO `push_message_receiver` VALUES (31, '2026-09-22 09:12:13.021000', 4, 1, '2026-09-22 09:12:13.021000');
INSERT INTO `push_message_receiver` VALUES (32, '2026-09-22 09:12:13.021000', 4, 14, '2026-09-22 09:12:13.021000');
INSERT INTO `push_message_receiver` VALUES (33, '2026-09-22 09:12:13.021000', 4, 18, '2026-09-22 09:12:13.021000');
INSERT INTO `push_message_receiver` VALUES (34, '2026-09-22 09:12:13.021000', 4, 19, '2026-09-22 09:12:13.021000');
INSERT INTO `push_message_receiver` VALUES (35, '2026-09-22 09:12:13.021000', 4, 737192189162946560, '2026-09-22 09:12:13.021000');
INSERT INTO `push_message_receiver` VALUES (36, '2026-09-22 09:12:13.021000', 4, 737192428523487232, '2026-09-22 09:12:13.021000');
INSERT INTO `push_message_receiver` VALUES (37, '2026-09-22 09:12:13.021000', 4, 737192677283463168, '2026-09-22 09:12:13.021000');
INSERT INTO `push_message_receiver` VALUES (38, '2026-09-22 09:12:13.021000', 4, 737192878358396928, '2026-09-22 09:12:13.021000');
INSERT INTO `push_message_receiver` VALUES (39, '2026-09-22 09:12:13.021000', 4, 737193046583541760, '2026-09-22 09:12:13.021000');
INSERT INTO `push_message_receiver` VALUES (40, '2026-09-22 09:12:13.021000', 4, 739774367356944384, '2026-09-22 09:12:13.021000');
INSERT INTO `push_message_receiver` VALUES (41, '2026-09-22 09:12:28.571000', 5, 1, '2026-09-22 09:12:28.571000');
INSERT INTO `push_message_receiver` VALUES (42, '2026-09-22 09:12:28.571000', 5, 14, '2026-09-22 09:12:28.571000');
INSERT INTO `push_message_receiver` VALUES (43, '2026-09-22 09:12:28.571000', 5, 18, '2026-09-22 09:12:28.571000');
INSERT INTO `push_message_receiver` VALUES (44, '2026-09-22 09:12:28.571000', 5, 19, '2026-09-22 09:12:28.571000');
INSERT INTO `push_message_receiver` VALUES (45, '2026-09-22 09:12:28.571000', 5, 737192189162946560, '2026-09-22 09:12:28.571000');
INSERT INTO `push_message_receiver` VALUES (46, '2026-09-22 09:12:28.571000', 5, 737192428523487232, '2026-09-22 09:12:28.571000');
INSERT INTO `push_message_receiver` VALUES (47, '2026-09-22 09:12:28.571000', 5, 737192677283463168, '2026-09-22 09:12:28.571000');
INSERT INTO `push_message_receiver` VALUES (48, '2026-09-22 09:12:28.571000', 5, 737192878358396928, '2026-09-22 09:12:28.571000');
INSERT INTO `push_message_receiver` VALUES (49, '2026-09-22 09:12:28.571000', 5, 737193046583541760, '2026-09-22 09:12:28.571000');
INSERT INTO `push_message_receiver` VALUES (50, '2026-09-22 09:12:28.571000', 5, 739774367356944384, '2026-09-22 09:12:28.571000');
INSERT INTO `push_message_receiver` VALUES (51, '2026-09-22 09:43:07.451000', 6, 1, '2026-09-22 09:43:07.451000');
INSERT INTO `push_message_receiver` VALUES (52, '2026-09-22 09:43:07.451000', 6, 14, '2026-09-22 09:43:07.451000');
INSERT INTO `push_message_receiver` VALUES (53, '2026-09-22 09:43:07.451000', 6, 18, '2026-09-22 09:43:07.451000');
INSERT INTO `push_message_receiver` VALUES (54, '2026-09-22 09:43:07.451000', 6, 19, '2026-09-22 09:43:07.451000');
INSERT INTO `push_message_receiver` VALUES (55, '2026-09-22 09:43:07.451000', 6, 737192189162946560, '2026-09-22 09:43:07.451000');
INSERT INTO `push_message_receiver` VALUES (56, '2026-09-22 09:43:07.451000', 6, 737192428523487232, '2026-09-22 09:43:07.451000');
INSERT INTO `push_message_receiver` VALUES (57, '2026-09-22 09:43:07.451000', 6, 737192677283463168, '2026-09-22 09:43:07.451000');
INSERT INTO `push_message_receiver` VALUES (58, '2026-09-22 09:43:07.451000', 6, 737192878358396928, '2026-09-22 09:43:07.451000');
INSERT INTO `push_message_receiver` VALUES (59, '2026-09-22 09:43:07.451000', 6, 737193046583541760, '2026-09-22 09:43:07.451000');
INSERT INTO `push_message_receiver` VALUES (60, '2026-09-22 09:43:07.451000', 6, 739774367356944384, '2026-09-22 09:43:07.451000');
INSERT INTO `push_message_receiver` VALUES (61, '2026-09-22 09:43:22.059000', 7, 1, '2026-09-22 09:43:22.059000');
INSERT INTO `push_message_receiver` VALUES (62, '2026-09-22 09:43:22.059000', 7, 14, '2026-09-22 09:43:22.059000');
INSERT INTO `push_message_receiver` VALUES (63, '2026-09-22 09:43:22.059000', 7, 18, '2026-09-22 09:43:22.059000');
INSERT INTO `push_message_receiver` VALUES (64, '2026-09-22 09:43:22.059000', 7, 19, '2026-09-22 09:43:22.059000');
INSERT INTO `push_message_receiver` VALUES (65, '2026-09-22 09:43:22.059000', 7, 737192189162946560, '2026-09-22 09:43:22.059000');
INSERT INTO `push_message_receiver` VALUES (66, '2026-09-22 09:43:22.059000', 7, 737192428523487232, '2026-09-22 09:43:22.059000');
INSERT INTO `push_message_receiver` VALUES (67, '2026-09-22 09:43:22.059000', 7, 737192677283463168, '2026-09-22 09:43:22.059000');
INSERT INTO `push_message_receiver` VALUES (68, '2026-09-22 09:43:22.059000', 7, 737192878358396928, '2026-09-22 09:43:22.059000');
INSERT INTO `push_message_receiver` VALUES (69, '2026-09-22 09:43:22.059000', 7, 737193046583541760, '2026-09-22 09:43:22.059000');
INSERT INTO `push_message_receiver` VALUES (70, '2026-09-22 09:43:22.059000', 7, 739774367356944384, '2026-09-22 09:43:22.059000');
INSERT INTO `push_message_receiver` VALUES (71, '2026-09-22 09:43:31.579000', 8, 1, '2026-09-22 09:43:31.579000');
INSERT INTO `push_message_receiver` VALUES (72, '2026-09-22 09:43:31.579000', 8, 14, '2026-09-22 09:43:31.579000');
INSERT INTO `push_message_receiver` VALUES (73, '2026-09-22 09:43:31.579000', 8, 18, '2026-09-22 09:43:31.579000');
INSERT INTO `push_message_receiver` VALUES (74, '2026-09-22 09:43:31.579000', 8, 19, '2026-09-22 09:43:31.579000');
INSERT INTO `push_message_receiver` VALUES (75, '2026-09-22 09:43:31.579000', 8, 737192189162946560, '2026-09-22 09:43:31.579000');
INSERT INTO `push_message_receiver` VALUES (76, '2026-09-22 09:43:31.579000', 8, 737192428523487232, '2026-09-22 09:43:31.579000');
INSERT INTO `push_message_receiver` VALUES (77, '2026-09-22 09:43:31.579000', 8, 737192677283463168, '2026-09-22 09:43:31.579000');
INSERT INTO `push_message_receiver` VALUES (78, '2026-09-22 09:43:31.579000', 8, 737192878358396928, '2026-09-22 09:43:31.579000');
INSERT INTO `push_message_receiver` VALUES (79, '2026-09-22 09:43:31.579000', 8, 737193046583541760, '2026-09-22 09:43:31.579000');
INSERT INTO `push_message_receiver` VALUES (80, '2026-09-22 09:43:31.579000', 8, 739774367356944384, '2026-09-22 09:43:31.579000');
INSERT INTO `push_message_receiver` VALUES (81, '2026-09-22 09:49:00.712000', 9, 1, '2026-09-22 09:49:00.712000');
INSERT INTO `push_message_receiver` VALUES (82, '2026-09-22 09:49:00.712000', 9, 14, '2026-09-22 09:49:00.712000');
INSERT INTO `push_message_receiver` VALUES (83, '2026-09-22 09:49:00.712000', 9, 18, '2026-09-22 09:49:00.712000');
INSERT INTO `push_message_receiver` VALUES (84, '2026-09-22 09:49:00.712000', 9, 19, '2026-09-22 09:49:00.712000');
INSERT INTO `push_message_receiver` VALUES (85, '2026-09-22 09:49:00.712000', 9, 737192189162946560, '2026-09-22 09:49:00.712000');
INSERT INTO `push_message_receiver` VALUES (86, '2026-09-22 09:49:00.712000', 9, 737192428523487232, '2026-09-22 09:49:00.712000');
INSERT INTO `push_message_receiver` VALUES (87, '2026-09-22 09:49:00.712000', 9, 737192677283463168, '2026-09-22 09:49:00.712000');
INSERT INTO `push_message_receiver` VALUES (88, '2026-09-22 09:49:00.712000', 9, 737192878358396928, '2026-09-22 09:49:00.712000');
INSERT INTO `push_message_receiver` VALUES (89, '2026-09-22 09:49:00.712000', 9, 737193046583541760, '2026-09-22 09:49:00.712000');
INSERT INTO `push_message_receiver` VALUES (90, '2026-09-22 09:49:00.712000', 9, 739774367356944384, '2026-09-22 09:49:00.712000');
INSERT INTO `push_message_receiver` VALUES (91, '2026-09-22 09:51:16.571000', 11, 18, '2026-09-22 09:51:16.571000');
INSERT INTO `push_message_receiver` VALUES (92, '2026-09-22 09:51:16.574000', 10, 1, '2026-09-22 09:51:16.574000');
INSERT INTO `push_message_receiver` VALUES (93, '2026-09-22 10:01:16.573000', 12, 1, '2026-09-22 10:01:16.573000');
INSERT INTO `push_message_receiver` VALUES (94, '2026-09-22 10:01:16.573000', 12, 14, '2026-09-22 10:01:16.573000');
INSERT INTO `push_message_receiver` VALUES (95, '2026-09-22 10:01:16.573000', 12, 18, '2026-09-22 10:01:16.573000');
INSERT INTO `push_message_receiver` VALUES (96, '2026-09-22 10:01:16.573000', 12, 19, '2026-09-22 10:01:16.573000');
INSERT INTO `push_message_receiver` VALUES (97, '2026-09-22 10:01:16.573000', 12, 737192189162946560, '2026-09-22 10:01:16.573000');
INSERT INTO `push_message_receiver` VALUES (98, '2026-09-22 10:01:16.573000', 12, 737192428523487232, '2026-09-22 10:01:16.573000');
INSERT INTO `push_message_receiver` VALUES (99, '2026-09-22 10:01:16.573000', 12, 737192677283463168, '2026-09-22 10:01:16.573000');
INSERT INTO `push_message_receiver` VALUES (100, '2026-09-22 10:01:16.573000', 12, 737192878358396928, '2026-09-22 10:01:16.573000');
INSERT INTO `push_message_receiver` VALUES (101, '2026-09-22 10:01:16.573000', 12, 737193046583541760, '2026-09-22 10:01:16.573000');
INSERT INTO `push_message_receiver` VALUES (102, '2026-09-22 10:01:16.573000', 12, 739774367356944384, '2026-09-22 10:01:16.573000');
INSERT INTO `push_message_receiver` VALUES (103, '2026-10-05 10:37:49.106000', 13, 1, '2026-10-05 10:37:49.106000');
INSERT INTO `push_message_receiver` VALUES (104, '2026-10-05 10:37:49.106000', 13, 14, '2026-10-05 10:37:49.106000');
INSERT INTO `push_message_receiver` VALUES (105, '2026-10-05 10:37:49.106000', 13, 18, '2026-10-05 10:37:49.106000');
INSERT INTO `push_message_receiver` VALUES (106, '2026-10-05 10:37:49.106000', 13, 19, '2026-10-05 10:37:49.106000');
INSERT INTO `push_message_receiver` VALUES (107, '2026-10-05 10:37:49.106000', 13, 737192189162946560, '2026-10-05 10:37:49.106000');
INSERT INTO `push_message_receiver` VALUES (108, '2026-10-05 10:37:49.106000', 13, 737192428523487232, '2026-10-05 10:37:49.106000');
INSERT INTO `push_message_receiver` VALUES (109, '2026-10-05 10:37:49.106000', 13, 737192677283463168, '2026-10-05 10:37:49.106000');
INSERT INTO `push_message_receiver` VALUES (110, '2026-10-05 10:37:49.106000', 13, 737192878358396928, '2026-10-05 10:37:49.106000');
INSERT INTO `push_message_receiver` VALUES (111, '2026-10-05 10:37:49.106000', 13, 737193046583541760, '2026-10-05 10:37:49.106000');
INSERT INTO `push_message_receiver` VALUES (112, '2026-10-05 10:37:49.106000', 13, 739774367356944384, '2026-10-05 10:37:49.106000');
INSERT INTO `push_message_receiver` VALUES (113, '2026-10-05 10:47:03.403000', 14, 1, '2026-10-05 10:47:03.403000');
INSERT INTO `push_message_receiver` VALUES (114, '2026-10-05 10:47:03.403000', 14, 14, '2026-10-05 10:47:03.403000');
INSERT INTO `push_message_receiver` VALUES (115, '2026-10-05 10:47:03.403000', 14, 18, '2026-10-05 10:47:03.403000');
INSERT INTO `push_message_receiver` VALUES (116, '2026-10-05 10:47:03.403000', 14, 19, '2026-10-05 10:47:03.403000');
INSERT INTO `push_message_receiver` VALUES (117, '2026-10-05 10:47:03.403000', 14, 737192189162946560, '2026-10-05 10:47:03.403000');
INSERT INTO `push_message_receiver` VALUES (118, '2026-10-05 10:47:03.403000', 14, 737192428523487232, '2026-10-05 10:47:03.403000');
INSERT INTO `push_message_receiver` VALUES (119, '2026-10-05 10:47:03.403000', 14, 737192677283463168, '2026-10-05 10:47:03.403000');
INSERT INTO `push_message_receiver` VALUES (120, '2026-10-05 10:47:03.403000', 14, 737192878358396928, '2026-10-05 10:47:03.403000');
INSERT INTO `push_message_receiver` VALUES (121, '2026-10-05 10:47:03.403000', 14, 737193046583541760, '2026-10-05 10:47:03.403000');
INSERT INTO `push_message_receiver` VALUES (122, '2026-10-05 10:47:03.403000', 14, 739774367356944384, '2026-10-05 10:47:03.403000');
INSERT INTO `push_message_receiver` VALUES (774860932722458624, '2026-11-09 13:00:42.557000', 774860932705681408, 1, '2026-11-09 13:00:42.557000');
INSERT INTO `push_message_receiver` VALUES (774860932722458625, '2026-11-09 13:00:42.557000', 774860932705681408, 14, '2026-11-09 13:00:42.557000');
INSERT INTO `push_message_receiver` VALUES (774860932722458626, '2026-11-09 13:00:42.557000', 774860932705681408, 18, '2026-11-09 13:00:42.557000');
INSERT INTO `push_message_receiver` VALUES (774860932722458627, '2026-11-09 13:00:42.557000', 774860932705681408, 19, '2026-11-09 13:00:42.557000');
INSERT INTO `push_message_receiver` VALUES (774860932722458628, '2026-11-09 13:00:42.557000', 774860932705681408, 737192189162946560, '2026-11-09 13:00:42.557000');
INSERT INTO `push_message_receiver` VALUES (774860932722458629, '2026-11-09 13:00:42.557000', 774860932705681408, 737192428523487232, '2026-11-09 13:00:42.557000');
INSERT INTO `push_message_receiver` VALUES (774860932722458630, '2026-11-09 13:00:42.557000', 774860932705681408, 737192677283463168, '2026-11-09 13:00:42.557000');
INSERT INTO `push_message_receiver` VALUES (774860932722458631, '2026-11-09 13:00:42.557000', 774860932705681408, 737192878358396928, '2026-11-09 13:00:42.557000');
INSERT INTO `push_message_receiver` VALUES (774860932726652928, '2026-11-09 13:00:42.557000', 774860932705681408, 737193046583541760, '2026-11-09 13:00:42.557000');
INSERT INTO `push_message_receiver` VALUES (774860932726652929, '2026-11-09 13:00:42.557000', 774860932705681408, 739774367356944384, '2026-11-09 13:00:42.557000');
INSERT INTO `push_message_receiver` VALUES (775958078574034944, '2026-11-12 13:40:22.522000', 775958078553063424, 1, '2026-11-12 13:40:22.522000');
INSERT INTO `push_message_receiver` VALUES (775958078578229248, '2026-11-12 13:40:22.522000', 775958078553063424, 14, '2026-11-12 13:40:22.522000');
INSERT INTO `push_message_receiver` VALUES (775958078578229249, '2026-11-12 13:40:22.522000', 775958078553063424, 18, '2026-11-12 13:40:22.522000');
INSERT INTO `push_message_receiver` VALUES (775958078578229250, '2026-11-12 13:40:22.522000', 775958078553063424, 19, '2026-11-12 13:40:22.522000');
INSERT INTO `push_message_receiver` VALUES (775958078578229251, '2026-11-12 13:40:22.522000', 775958078553063424, 737192189162946560, '2026-11-12 13:40:22.522000');
INSERT INTO `push_message_receiver` VALUES (775958078578229252, '2026-11-12 13:40:22.522000', 775958078553063424, 737192428523487232, '2026-11-12 13:40:22.522000');
INSERT INTO `push_message_receiver` VALUES (775958078578229253, '2026-11-12 13:40:22.522000', 775958078553063424, 737192677283463168, '2026-11-12 13:40:22.522000');
INSERT INTO `push_message_receiver` VALUES (775958078578229254, '2026-11-12 13:40:22.522000', 775958078553063424, 737192878358396928, '2026-11-12 13:40:22.522000');
INSERT INTO `push_message_receiver` VALUES (775958078578229255, '2026-11-12 13:40:22.522000', 775958078553063424, 737193046583541760, '2026-11-12 13:40:22.522000');
INSERT INTO `push_message_receiver` VALUES (775958078578229256, '2026-11-12 13:40:22.522000', 775958078553063424, 739774367356944384, '2026-11-12 13:40:22.522000');

-- ----------------------------
-- Table structure for sys_company
-- ----------------------------
DROP TABLE IF EXISTS `sys_company`;
CREATE TABLE `sys_company`  (
  `id` bigint NOT NULL COMMENT '公司ID（雪花算法生成）',
  `company_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公司名称',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建者用户ID',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '公司描述',
  `flag` int NULL DEFAULT NULL COMMENT '公司状态：0-已删除，1-已激活，2-禁用，5-系统内置',
  `logo_image` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '公司Logo图片URL',
  `tax_number` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '公司税号',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新者用户ID',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_company_name`(`company_name` ASC) USING BTREE,
  INDEX `idx_flag`(`flag` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_update_time`(`update_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '公司信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_company
-- ----------------------------
INSERT INTO `sys_company` VALUES (1, '系统公司', NULL, NULL, NULL, 5, '系统公司', '系统公司', NULL, '2026-05-12 13:09:52.000000');
INSERT INTO `sys_company` VALUES (2, '测试公司', NULL, '2025-09-03 12:20:00.000000', '这是一个测试公司', 1, '', '11223355', 1, '2026-07-13 11:12:38.000000');
INSERT INTO `sys_company` VALUES (740830978083127296, '待删除公司', 1, '2026-08-07 15:17:49.000000', '', 0, NULL, 'NeedToDelete', NULL, NULL);

-- ----------------------------
-- Table structure for sys_condition_rule
-- ----------------------------
DROP TABLE IF EXISTS `sys_condition_rule`;
CREATE TABLE `sys_condition_rule`  (
  `id` bigint NOT NULL,
  `rule_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `rule_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `getter_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `invocation_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `editor_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `enabled` int NOT NULL,
  `flag` int NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `create_by` bigint NULL DEFAULT NULL,
  `update_by` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_condition_rule_code`(`rule_code` ASC) USING BTREE,
  INDEX `idx_condition_rule_enabled`(`enabled` ASC) USING BTREE,
  INDEX `idx_condition_rule_flag`(`flag` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_condition_rule
-- ----------------------------
INSERT INTO `sys_condition_rule` VALUES (778824046589509632, 'USER.VIP', '用户VIP权限', '', 'ConditionGetter', '[{\"type\":\"operand\",\"name\":\"\",\"sort\":1,\"value\":\"{\\\"getterId\\\":\\\"UserPermissionGetter\\\",\\\"paramValues\\\":[{\\\"type\\\":\\\"string\\\",\\\"name\\\":\\\"权限编码\\\",\\\"sort\\\":1,\\\"value\\\":\\\"API.VIP\\\"},{\\\"type\\\":\\\"string\\\",\\\"name\\\":\\\"权限类型\\\",\\\"sort\\\":2,\\\"value\\\":\\\"api\\\"},{\\\"type\\\":\\\"string\\\",\\\"name\\\":\\\"用户名\\\",\\\"sort\\\":3,\\\"value\\\":\\\"223344\\\"}]}\"},{\"type\":\"string\",\"name\":\"\",\"sort\":2,\"value\":\"eq\"},{\"type\":\"operand\",\"name\":\"\",\"sort\":3,\"value\":\"true\"}]', '{\"editor\":\"condition\",\"state\":{\"left\":\"{\\\"getterId\\\":\\\"UserPermissionGetter\\\",\\\"paramValues\\\":[{\\\"type\\\":\\\"string\\\",\\\"name\\\":\\\"权限编码\\\",\\\"sort\\\":1,\\\"value\\\":\\\"API.VIP\\\"},{\\\"type\\\":\\\"string\\\",\\\"name\\\":\\\"权限类型\\\",\\\"sort\\\":2,\\\"value\\\":\\\"api\\\"},{\\\"type\\\":\\\"string\\\",\\\"name\\\":\\\"用户名\\\",\\\"sort\\\":3,\\\"value\\\":\\\"223344\\\"}]}\",\"op\":\"eq\",\"right\":\"true\"}}', 1, 1, '2026-11-20 11:28:43', NULL, 1, NULL);

-- ----------------------------
-- Table structure for sys_data_dic
-- ----------------------------
DROP TABLE IF EXISTS `sys_data_dic`;
CREATE TABLE `sys_data_dic`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID（自增）',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `data_dic_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '数据字典编码（业务侧按编码检索字典项）',
  `data_dic_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '数据字典名称',
  `flag` int NULL DEFAULT NULL COMMENT '数据字典状态：1-启用，-1-停用，0-删除',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_data_dic_name_code`(`data_dic_name` ASC, `data_dic_code` ASC) USING BTREE,
  INDEX `idx_flag`(`flag` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '数据字典主表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_data_dic
-- ----------------------------
INSERT INTO `sys_data_dic` VALUES (7, '2026-06-24 15:32:55.021000', 'SYSTEM', '系统', 1, '系统字典', NULL);
INSERT INTO `sys_data_dic` VALUES (8, '2026-06-25 13:22:15.166000', 'DIC_XH_GD_SCRK_DOC_MAP', '兴昊工单生成入库单映射', 1, '', NULL);
INSERT INTO `sys_data_dic` VALUES (9, '2026-07-01 09:43:24.350000', 'DIC_XH_SCRK_FEATURE_FALG_MAP', '兴昊生产入库ERP单据创建标志字典', 1, 'Label对应工单单据类型。Value对应功能标志。1:插入ERP生产入库单。2:插入且审核。', '2026-08-09 14:55:42.447000');
INSERT INTO `sys_data_dic` VALUES (10, '2026-07-24 09:33:52.242000', 'GENRIC_API_ID_DIC', '泛化调用接口ID字典', 0, NULL, '2026-08-04 11:15:35.003000');
INSERT INTO `sys_data_dic` VALUES (11, '2026-07-24 09:35:44.678000', 'GENRIC_API_METHOD_DIC', '泛化调用方法映射字典', 0, NULL, '2026-08-04 11:15:38.989000');
INSERT INTO `sys_data_dic` VALUES (12, '2026-07-24 14:30:44.230000', 'GENRIC_API_INTERFACE_DIC', '泛化调用接口类型字典', 0, NULL, '2026-08-04 11:15:44.949000');

-- ----------------------------
-- Table structure for sys_department
-- ----------------------------
DROP TABLE IF EXISTS `sys_department`;
CREATE TABLE `sys_department`  (
  `id` bigint NOT NULL COMMENT '部门ID（雪花算法生成）',
  `company_id` bigint NOT NULL COMMENT '所属公司ID',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建者用户ID',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `department_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '部门编码',
  `department_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '部门名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '部门描述',
  `flag` int NULL DEFAULT NULL COMMENT '部门状态：0-已删除，1-已激活，2-禁用',
  `manager` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '部门负责人姓名',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父部门ID（用于构建树形结构）',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新者用户ID',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_name_code`(`department_name` ASC, `department_code` ASC) USING BTREE,
  INDEX `idx_flag`(`flag` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_update_time`(`update_time` ASC) USING BTREE,
  INDEX `idx_company_id`(`company_id` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统部门信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_department
-- ----------------------------
INSERT INTO `sys_department` VALUES (2, 2, NULL, '2026-05-12 14:55:22.304000', '', '销售部', '', 1, '', NULL, 1, '2026-07-16 11:10:15.431000');
INSERT INTO `sys_department` VALUES (4, 2, NULL, '2026-05-12 14:56:45.687000', '', '销售A组', '', 2, '', NULL, NULL, NULL);
INSERT INTO `sys_department` VALUES (6, 2, NULL, '2026-06-15 18:45:22.072000', 'XZ1', '行政部', '', 1, '', NULL, 1, '2026-07-16 11:09:21.701000');
INSERT INTO `sys_department` VALUES (3511711354564116480, 2, 1, '2026-07-13 11:27:11.217000', 'SCB', '生产部', '', 1, '', NULL, 1, '2026-07-16 11:09:26.360000');

-- ----------------------------
-- Table structure for sys_event_action
-- ----------------------------
DROP TABLE IF EXISTS `sys_event_action`;
CREATE TABLE `sys_event_action`  (
  `id` bigint NOT NULL,
  `reporter_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `event_code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `action_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `action_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `param_json` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL,
  `sort_order` int NULL DEFAULT NULL,
  `enabled` int NOT NULL,
  `flag` int NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `create_by` bigint NULL DEFAULT NULL,
  `update_by` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_event_action_event`(`reporter_id` ASC, `event_code` ASC, `sort_order` ASC) USING BTREE,
  INDEX `idx_event_action_enabled`(`enabled` ASC) USING BTREE,
  INDEX `idx_event_action_flag`(`flag` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_event_action
-- ----------------------------
INSERT INTO `sys_event_action` VALUES (779163654938427392, 'BusinessEventReporter', 'user.created', 'PushNotifyAction', '站内通知推送', '[{\"type\":\"string\",\"name\":\"msgType\",\"sort\":1,\"value\":\"notice\"},{\"type\":\"string\",\"name\":\"title\",\"sort\":2,\"value\":\"测试\"},{\"type\":\"string\",\"name\":\"content\",\"sort\":3,\"value\":null},{\"type\":\"string\",\"name\":\"priority\",\"sort\":4,\"value\":\"low\"},{\"type\":\"string\",\"name\":\"topic\",\"sort\":5,\"value\":null},{\"type\":\"string\",\"name\":\"url\",\"sort\":6,\"value\":null}]', 0, 1, 1, '2026-11-21 09:58:11', NULL, 1, NULL);

-- ----------------------------
-- Table structure for sys_event_subscription
-- ----------------------------
DROP TABLE IF EXISTS `sys_event_subscription`;
CREATE TABLE `sys_event_subscription`  (
  `id` bigint NOT NULL,
  `reporter_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `event_code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `trigger_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
  `rule_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL,
  `sort_order` int NULL DEFAULT NULL,
  `enabled` int NOT NULL,
  `flag` int NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `create_by` bigint NULL DEFAULT NULL,
  `update_by` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_event_subscription_event`(`reporter_id` ASC, `event_code` ASC, `sort_order` ASC) USING BTREE,
  INDEX `idx_event_subscription_enabled`(`enabled` ASC) USING BTREE,
  INDEX `idx_event_subscription_flag`(`flag` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_event_subscription
-- ----------------------------

-- ----------------------------
-- Table structure for sys_generic_api
-- ----------------------------
DROP TABLE IF EXISTS `sys_generic_api`;
CREATE TABLE `sys_generic_api`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `api_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'API唯一标识',
  `api_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'API名称',
  `app_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '服务应用名（用于服务发现）',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建者用户ID',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述信息',
  `flag` int NULL DEFAULT NULL COMMENT '状态标识：1-启用，-1-禁用，0-删除',
  `interface_name` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '接口全限定名（用于Triple HTTP调用）',
  `method_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '方法名',
  `need_auth` bit(1) NOT NULL COMMENT '是否需要认证',
  `pre_auth_beans` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '预认证Bean列表（逗号分隔）',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新者用户ID',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `version` int NULL DEFAULT NULL COMMENT 'API版本号',
  `business` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '业务分区（Dubbo group / 本地实现自述的 business），空表示不约束',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_generic_api_api_id`(`api_id` ASC) USING BTREE,
  INDEX `idx_generic_api_app_name`(`app_name` ASC) USING BTREE,
  INDEX `idx_generic_api_flag`(`flag` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '泛化API配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_generic_api
-- ----------------------------
INSERT INTO `sys_generic_api` VALUES (736388709422727168, 'MES.OUTSTATION.BARCODE', 'MES出站码查询', 'dubbo-xh-mes-service', 1, '2026-07-26 09:05:49.737000', '', 0, 'com.tbs.xh.mes.service.IMesQueryService', 'queryOutStationBarCodeInfos', b'1', '', 1, '2026-07-26 17:58:48.265000', NULL, NULL);
INSERT INTO `sys_generic_api` VALUES (739354793914400768, 'OA.QUERY.USER', '根据工号查询OA用户', 'dubbo-xh-system-service', 1, '2026-08-03 13:31:59.403000', '', 0, 'com.tbs.xh.eteams.IOAService', 'getUser', b'0', '', NULL, '2026-08-03 13:31:59.401000', NULL, NULL);
INSERT INTO `sys_generic_api` VALUES (739355309184647168, 'OA.MESSAGE.SEND', 'OA发送消息', 'dubbo-xh-system-service', 1, '2026-08-03 13:34:02.264000', '', 0, 'com.tbs.xh.eteams.IOAService', 'sendMsg', b'1', 'AccountActived', NULL, '2026-08-03 13:34:02.262000', NULL, NULL);
INSERT INTO `sys_generic_api` VALUES (739720254627774464, 'ERP.SCAN.QUERY.TYPES', '获取所有已支持的表单类型', 'dubbo-xh-system-service', 1, '2026-08-04 13:44:12.027000', '', 0, 'com.tbs.xh.e10.service.IDocSaveService', 'getAllScanDocTypes', b'0', '', NULL, NULL, NULL, NULL);
INSERT INTO `sys_generic_api` VALUES (739724441906315264, 'ERP.DOC.SCAN.DETAILS', '获取单据扫描配置详情', 'dubbo-xh-system-service', 1, '2026-08-04 14:00:50.358000', '', 0, 'com.tbs.xh.e10.service.IDocSaveService', 'getDocScanConfig', b'0', '', NULL, NULL, NULL, NULL);
INSERT INTO `sys_generic_api` VALUES (739727822427258880, 'ERP.DOC.SCAN.CONFIG.SAVE', '储存单据扫描配置', 'dubbo-xh-system-service', 1, '2026-08-04 14:14:16.337000', '', 0, 'com.tbs.xh.e10.service.IDocSaveService', 'saveDocScanConfig', b'1', 'AccountActived', NULL, NULL, NULL, NULL);
INSERT INTO `sys_generic_api` VALUES (739730395020394496, 'ERP.SCAN.QUERY.SCAN_ROUTE', '获取单据保存后需要通知的用户工号', 'dubbo-xh-system-service', 1, '2026-08-04 14:24:29.694000', '', 0, 'com.tbs.xh.e10.service.IDocSaveService', 'getSaveStatusRoute', b'0', '', NULL, NULL, NULL, NULL);
INSERT INTO `sys_generic_api` VALUES (739732892111863808, 'ERP.DOC.SCAN.ADD.ROUTE', '新增单据通知人路由信息', 'dubbo-xh-system-service', 1, '2026-08-04 14:34:25.043000', '', 0, 'com.tbs.xh.e10.service.IDocSaveService', 'saveDocSaveStatusRoute', b'1', 'AccountActived', 1, '2026-08-04 14:45:59.297000', NULL, NULL);
INSERT INTO `sys_generic_api` VALUES (739734309090361344, 'ERP.DOC.SCAN.DELETE.ROUTE', '删除单据通知人信息', 'dubbo-xh-system-service', 1, '2026-08-04 14:40:02.881000', '', 0, 'com.tbs.xh.e10.service.IDocSaveService', 'deleteDocSaveStatusRoute', b'1', 'AccountActived', 1, '2026-08-04 14:46:08.642000', NULL, NULL);
INSERT INTO `sys_generic_api` VALUES (739735764882948096, 'ERP.DOC.SCAN.DELETE.CONFIG', '删除单据扫描配置。同时删除相关的通知人配置', 'dubbo-xh-system-service', 1, '2026-08-04 14:45:49.970000', '', 0, 'com.tbs.xh.e10.service.IDocSaveService', 'deleteDocScanConfig', b'1', 'AccountActived', NULL, NULL, NULL, NULL);
INSERT INTO `sys_generic_api` VALUES (773780452497424384, 'LOG.DEMO', '测试用例', 'Logging', 1, '2026-11-06 13:27:16.015000', '', 1, 'com.tbs.logging.demo.IStandaloneDemoService', 'echo', b'0', '', 1, '2026-11-06 13:29:59.257000', NULL, 'demo');
INSERT INTO `sys_generic_api` VALUES (773789430929424384, 'LOG.LIST.DEMO', '列表转化测试', '666', 1, '2026-11-06 14:02:56.632000', '', 1, 'com.tbs.logging.demo.IStandaloneDemoService', 'filter', b'0', '', NULL, NULL, NULL, '*');
INSERT INTO `sys_generic_api` VALUES (773826025086779392, 'GO.DEMO', 'asd', 'de', 1, '2026-11-06 16:28:21.365000', '', 1, 'org.apache.dubbo.samples.api.UserProvider', 'GetUser', b'0', '', 1, '2026-11-07 09:24:34.479000', NULL, '*');

-- ----------------------------
-- Table structure for sys_generic_api_param
-- ----------------------------
DROP TABLE IF EXISTS `sys_generic_api_param`;
CREATE TABLE `sys_generic_api_param`  (
  `id` bigint NOT NULL COMMENT '主键ID（雪花算法生成）',
  `api_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所属泛化API标识（援引 sys_generic_api.api_id）',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建者用户ID',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '参数说明',
  `example` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '示例值（JSON字面量文本）',
  `flag` int NULL DEFAULT NULL COMMENT '状态标识：1-启用，-1-禁用，0-删除',
  `param_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '参数名，空表示只支持按位置传参',
  `param_order` int NULL DEFAULT NULL COMMENT '参数顺序，从0开始',
  `param_type` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '参数类型：擦除后的JVM类名或基本类型关键字，不写泛型',
  `required_flag` bit(1) NULL DEFAULT NULL COMMENT '是否必填',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新者用户ID',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_generic_api_param_api_id`(`api_id` ASC) USING BTREE,
  INDEX `idx_generic_api_param_flag`(`flag` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '泛化API入参定义表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_generic_api_param
-- ----------------------------
INSERT INTO `sys_generic_api_param` VALUES (773780452577116160, 'LOG.DEMO', NULL, NULL, '内容', '', 0, '', 0, 'java.lang.String', b'0', NULL, NULL);
INSERT INTO `sys_generic_api_param` VALUES (773780452623253504, 'LOG.DEMO', NULL, NULL, '次数', '', 0, '', 1, 'java.lang.Integer', b'0', NULL, NULL);
INSERT INTO `sys_generic_api_param` VALUES (773781137309827072, 'LOG.DEMO', NULL, NULL, '内容', '', 1, '', 0, 'java.lang.String', b'0', NULL, NULL);
INSERT INTO `sys_generic_api_param` VALUES (773781137351770112, 'LOG.DEMO', NULL, NULL, '次数', '', 1, '', 1, 'java.lang.Integer', b'0', NULL, NULL);
INSERT INTO `sys_generic_api_param` VALUES (773789431000727552, 'LOG.LIST.DEMO', NULL, NULL, '', '', 1, '', 0, 'java.util.List', b'0', NULL, NULL);
INSERT INTO `sys_generic_api_param` VALUES (773789431042670592, 'LOG.LIST.DEMO', NULL, NULL, '', '', 1, '', 1, 'java.lang.String', b'0', NULL, NULL);
INSERT INTO `sys_generic_api_param` VALUES (773826368738689024, 'GO.DEMO', NULL, NULL, '', '', 0, '', 0, 'java.util.List', b'0', NULL, NULL);
INSERT INTO `sys_generic_api_param` VALUES (773831958399549440, 'GO.DEMO', NULL, NULL, '', '', 0, '', 0, 'java.lang.String', b'0', NULL, NULL);
INSERT INTO `sys_generic_api_param` VALUES (774081673502392320, 'GO.DEMO', NULL, NULL, '', '', 0, '', 0, 'java.lang.String', b'0', NULL, NULL);
INSERT INTO `sys_generic_api_param` VALUES (774081764900470784, 'GO.DEMO', NULL, NULL, '', '', 1, 'name', 0, 'java.lang.String', b'0', NULL, NULL);

-- ----------------------------
-- Table structure for sys_login_info
-- ----------------------------
DROP TABLE IF EXISTS `sys_login_info`;
CREATE TABLE `sys_login_info`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `flag` int NULL DEFAULT NULL COMMENT '登录状态：1-正常，0-已删除，-1-已撤销',
  `session_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '会话ID',
  `token` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '登录令牌',
  `token_expire_time` datetime(6) NULL DEFAULT NULL COMMENT '令牌过期时间',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `user_id` bigint NULL DEFAULT NULL COMMENT '关联用户ID',
  `current_department_id` bigint NULL DEFAULT NULL COMMENT '当前会话选择的部门ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_flag`(`flag` ASC) USING BTREE,
  INDEX `idx_time`(`create_time` ASC, `update_time` ASC, `token_expire_time` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_token`(`token` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统登录信息表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_login_info
-- ----------------------------
INSERT INTO `sys_login_info` VALUES (771698294651879424, '2026-10-31 19:33:30.906000', -1, 'ab59f8285800000ab59f8285800001ab', 'ab59f8286c00000ab59f8286c00001ab', '2026-10-31 20:03:30.779000', '2026-10-31 19:33:30.906000', 14, NULL);
INSERT INTO `sys_login_info` VALUES (771698332744548352, '2026-10-31 19:33:39.988000', 1, 'ab59f8b7ac00000ab59f8b7ac00001ab', 'ab59f8b7ac00003ab59f8b7ac00004ab', '2026-10-31 20:03:39.947000', '2026-10-31 19:33:39.988000', 1, NULL);
INSERT INTO `sys_login_info` VALUES (771995637230075904, '2026-11-01 15:15:02.900000', -1, 'ab6adf0de400000ab6adf0de800000ab', 'ab6adf0e1000000ab6adf0e1000001ab', '2026-11-01 15:45:02.659000', '2026-11-01 15:15:02.900000', 1, NULL);
INSERT INTO `sys_login_info` VALUES (771996175040512000, '2026-11-01 15:17:11.125000', -1, 'ab6ae6e31000000ab6ae6e31000001ab', 'ab6ae6e31400000ab6ae6e31400001ab', '2026-11-01 15:47:10.981000', '2026-11-01 15:17:11.125000', 1, NULL);
INSERT INTO `sys_login_info` VALUES (771996389138759680, '2026-11-01 15:18:02.170000', -1, 'ab6aea017000000ab6aea017000001ab', 'ab6aea017000003ab6aea017000004ab', '2026-11-01 15:48:02.076000', '2026-11-01 15:18:02.170000', 1, NULL);
INSERT INTO `sys_login_info` VALUES (771996752369680384, '2026-11-01 15:19:28.771000', -1, 'ab6aef49c000000ab6aef49c000001ab', 'ab6aef49c400000ab6aef49c400001ab', '2026-11-01 15:49:28.624000', '2026-11-01 15:19:28.771000', 1, NULL);
INSERT INTO `sys_login_info` VALUES (771997674072178688, '2026-11-01 15:23:08.522000', -1, 'ab6afcb40c00000ab6afcb40c00001ab', 'ab6afcb40c00003ab6afcb40c00004ab', '2026-11-01 15:53:08.419000', '2026-11-01 15:23:08.522000', 1, NULL);
INSERT INTO `sys_login_info` VALUES (772001137329963008, '2026-11-01 15:36:54.226000', 1, 'ab6b2f12e000000ab6b2f12e400000ab', 'ab6b2f130800000ab6b2f130c00000ab', '2026-11-01 16:06:53.698000', '2026-11-01 15:36:54.226000', 1, NULL);
INSERT INTO `sys_login_info` VALUES (772618072006590464, '2026-11-03 08:28:22.915000', -1, 'ab8e40aeb800000ab8e40aeb800001ab', 'ab8e40aec800000ab8e40aecc00000ab', '2026-11-03 08:58:22.834000', '2026-11-03 08:28:22.915000', 18, NULL);
INSERT INTO `sys_login_info` VALUES (772618165841559552, '2026-11-03 08:28:45.288000', -1, 'ab8e420d1400000ab8e420d1400001ab', 'ab8e420d1400003ab8e420d1400004ab', '2026-11-03 08:58:45.253000', '2026-11-03 08:28:45.288000', 1, NULL);
INSERT INTO `sys_login_info` VALUES (772618284376784896, '2026-11-03 08:29:13.549000', -1, 'ab8e43c6a400000ab8e43c6a400001ab', 'ab8e43c6a400003ab8e43c6a400004ab', '2026-11-03 08:59:13.513000', '2026-11-03 08:29:13.549000', 18, NULL);
INSERT INTO `sys_login_info` VALUES (772697389852000256, '2026-11-03 13:43:33.764000', -1, 'ab92c2e80c00000ab92c2e80c00001ab', 'ab92c2e81400000ab92c2e81400001ab', '2026-11-03 14:13:33.637000', '2026-11-03 13:43:33.764000', 1, NULL);
INSERT INTO `sys_login_info` VALUES (772697644110708736, '2026-11-03 13:44:34.384000', -1, 'ab92c69cb400000ab92c69cb400001ab', 'ab92c69cb400003ab92c69cb400004ab', '2026-11-03 14:14:34.349000', '2026-11-03 13:44:34.384000', 739774367356944384, NULL);
INSERT INTO `sys_login_info` VALUES (772697736704163840, '2026-11-03 13:44:56.460000', 1, 'ab92c7f5c000000ab92c7f5c000001ab', 'ab92c7f5c000003ab92c7f5c000004ab', '2026-11-03 14:14:56.432000', '2026-11-03 13:44:56.460000', 1, NULL);
INSERT INTO `sys_login_info` VALUES (772984608294699008, '2026-11-04 08:44:51.976000', 1, 'aba3167ccc00000aba3167cd000000ab', 'aba3167cdc00000aba3167cdc00001ab', '2026-11-04 09:14:51.895000', '2026-11-04 08:44:58.963000', 1, 2);
INSERT INTO `sys_login_info` VALUES (772997862056263680, '2026-11-04 09:37:31.919000', -1, 'ab92c7f5c000000ab92c7f5c000001ab', 'aba3d75ac400000aba3d75ac400001ab', '2026-11-04 10:07:31.825000', '2026-11-04 09:37:31.919000', 1, NULL);
INSERT INTO `sys_login_info` VALUES (773005500722708480, '2026-11-04 10:07:53.119000', -1, 'aba44683a400000aba44683a400001ab', 'aba44683a800000aba44683a800001ab', '2026-11-04 10:37:53.066000', '2026-11-04 10:07:53.119000', 1, NULL);
INSERT INTO `sys_login_info` VALUES (773005657296076800, '2026-11-04 10:08:30.450000', -1, 'aba448cb2c00000aba448cb2c00001ab', 'aba448cb2c00003aba448cb2c00004ab', '2026-11-04 10:38:30.411000', '2026-11-04 10:08:30.450000', 1, NULL);
INSERT INTO `sys_login_info` VALUES (773005752989122560, '2026-11-04 10:08:53.265000', -1, 'aba448cb2c00000aba448cb2c00001ab', 'aba44a2f9c00000aba44a2f9c00001ab', '2026-11-04 10:38:53.223000', '2026-11-04 10:08:53.265000', 1, NULL);
INSERT INTO `sys_login_info` VALUES (773017558684008448, '2026-11-04 10:55:47.962000', 1, 'aba448cb2c00000aba448cb2c00001ab', 'aba4f5fb3000000aba4f5fb3000001ab', '2026-11-04 11:25:47.916000', '2026-11-04 10:55:47.962000', 1, NULL);
INSERT INTO `sys_login_info` VALUES (773101407958990848, '2026-11-04 16:28:59.187000', -1, 'aba9ba258c00000aba9ba258c00001ab', 'aba9ba259400000aba9ba259400001ab', '2026-11-04 16:58:59.109000', '2026-11-04 16:28:59.187000', 1, NULL);
INSERT INTO `sys_login_info` VALUES (773101473415299072, '2026-11-04 16:29:14.793000', -1, 'aba9bb1a1400000aba9bb1a1400001ab', 'aba9bb1a1800000aba9bb1a1800001ab', '2026-11-04 16:59:14.758000', '2026-11-04 16:29:14.793000', 1, NULL);
INSERT INTO `sys_login_info` VALUES (773366053173460992, '2026-11-05 10:00:35.523000', -1, 'aba9bb1a1400000aba9bb1a1400001ab', 'abb8c53e4400000abb8c53e4400001ab', '2026-11-05 10:30:35.473000', '2026-11-05 10:00:35.523000', 1, NULL);
INSERT INTO `sys_login_info` VALUES (773376181025112064, '2026-11-05 10:40:50.190000', -1, 'aba9bb1a1400000aba9bb1a1400001ab', 'abb9589f6800000abb9589f6c00000ab', '2026-11-05 11:10:50.138000', '2026-11-05 10:40:50.190000', 1, NULL);
INSERT INTO `sys_login_info` VALUES (773413910358261760, '2026-11-05 13:10:45.565000', -1, 'aba9bb1a1400000aba9bb1a1400001ab', 'abbb7da85800000abbb7da85c00000ab', '2026-11-05 13:40:45.526000', '2026-11-05 13:10:45.565000', 1, NULL);
INSERT INTO `sys_login_info` VALUES (773436041964027904, '2026-11-05 14:38:42.151000', -1, 'aba9bb1a1400000aba9bb1a1400001ab', 'abbcbfb6f000000abbcbfb6f000001ab', '2026-11-05 15:08:42.108000', '2026-11-05 14:38:42.151000', 1, NULL);
INSERT INTO `sys_login_info` VALUES (773779883724636160, '2026-11-06 13:25:00.415000', -1, 'aba9bb1a1400000aba9bb1a1400001ab', 'abd04b456000000abd04b456000001ab', '2026-11-06 13:55:00.376000', '2026-11-06 13:25:00.415000', 1, NULL);
INSERT INTO `sys_login_info` VALUES (773788581947768832, '2026-11-06 13:59:34.233000', 1, 'aba9bb1a1400000aba9bb1a1400001ab', 'abd0c9d8a400000abd0c9d8a800000ab', '2026-11-06 14:29:34.185000', '2026-11-06 13:59:34.233000', 1, NULL);
INSERT INTO `sys_login_info` VALUES (773788602743128064, '2026-11-06 13:59:39.191000', 1, 'abd0ca261c00000abd0ca261c00001ab', 'abd0ca261c00003abd0ca261c00004ab', '2026-11-06 14:29:39.143000', '2026-11-06 13:59:39.191000', 1, NULL);
INSERT INTO `sys_login_info` VALUES (773796212754087936, '2026-11-06 14:29:53.558000', -1, 'abd0ca261c00000abd0ca261c00001ab', 'abd138e38000000abd138e38000001ab', '2026-11-06 14:59:53.504000', '2026-11-06 14:29:53.558000', 1, NULL);
INSERT INTO `sys_login_info` VALUES (773807710603837440, '2026-11-06 15:15:34.860000', -1, 'abd0ca261c00000abd0ca261c00001ab', 'abd1e0347400000abd1e0347400001ab', '2026-11-06 15:45:34.813000', '2026-11-06 15:15:34.860000', 1, NULL);
INSERT INTO `sys_login_info` VALUES (773825782920249344, '2026-11-06 16:27:23.636000', 1, 'abd0ca261c00000abd0ca261c00001ab', 'abd2e7313000000abd2e7313000001ab', '2026-11-06 16:57:23.596000', '2026-11-06 16:27:23.636000', 1, NULL);
INSERT INTO `sys_login_info` VALUES (773833369136922624, '2026-11-06 16:57:32.331000', -1, 'abd0ca261c00000abd0ca261c00001ab', 'abd355954800000abd355954800001ab', '2026-11-06 17:27:32.242000', '2026-11-06 16:57:32.331000', 1, NULL);
INSERT INTO `sys_login_info` VALUES (774081415124877312, '2026-11-07 09:23:11.103000', 1, 'abd0ca261c00000abd0ca261c00001ab', 'abe16f211800000abe16f211800001ab', '2026-11-07 09:53:11.046000', '2026-11-07 09:23:11.103000', 1, NULL);
INSERT INTO `sys_login_info` VALUES (774860894684315648, '2026-11-09 13:00:33.512000', 1, 'ac0dbe0c4800000ac0dbe0c4800001ac', 'ac0dbe0c5800000ac0dbe0c5800001ac', '2026-11-09 13:30:33.430000', '2026-11-09 13:00:33.512000', 1, NULL);
INSERT INTO `sys_login_info` VALUES (775879548540551168, '2026-11-12 08:28:19.516000', -1, 'ac47a569a400000ac47a569a400001ac', 'ac47a569b000000ac47a569b000001ac', '2026-11-12 08:58:19.436000', '2026-11-12 08:28:19.516000', 1, NULL);
INSERT INTO `sys_login_info` VALUES (775958025369288704, '2026-11-12 13:40:09.850000', 1, 'ac47a569a400000ac47a569a400001ac', 'ac4c1b673800000ac4c1b673800001ac', '2026-11-12 14:10:09.806000', '2026-11-12 13:40:09.850000', 1, NULL);
INSERT INTO `sys_login_info` VALUES (777444059739324416, '2026-11-16 16:05:08.054000', 1, 'aca0940bd800000aca0940bd800001ac', 'aca0940be800000aca0940be800001ac', '2026-11-16 16:35:07.962000', '2026-11-16 16:05:08.054000', 1, NULL);
INSERT INTO `sys_login_info` VALUES (778499606915842048, '2026-11-19 13:59:30.111000', -1, 'acdc94479000000acdc94479000001ac', 'acdc9447a000000acdc9447a000001ac', '2026-11-19 14:29:30.024000', '2026-11-19 13:59:30.111000', 1, NULL);
INSERT INTO `sys_login_info` VALUES (778511363956801536, '2026-11-19 14:46:13.209000', 1, 'acdc94479000000acdc94479000001ac', 'acdd3f5ea000000acdd3f5ea000001ac', '2026-11-19 15:16:13.160000', '2026-11-19 14:46:13.209000', 1, NULL);
INSERT INTO `sys_login_info` VALUES (778518920242921472, '2026-11-19 15:16:14.767000', 1, 'acdc94479000000acdc94479000001ac', 'acddad536000000acddad536000001ac', '2026-11-19 15:46:14.680000', '2026-11-19 15:16:14.767000', 1, NULL);
INSERT INTO `sys_login_info` VALUES (778524541365256192, '2026-11-19 15:38:34.947000', -1, 'acddff1eb800000acddff1ebc00000ac', 'acddff1ec400000acddff1ec400001ac', '2026-11-19 16:08:34.801000', '2026-11-19 15:38:34.947000', 1, NULL);
INSERT INTO `sys_login_info` VALUES (778775958420193280, '2026-11-20 08:17:37.445000', -1, 'acddff1eb800000acddff1ebc00000ac', 'acec49ba0400000acec49ba0400001ac', '2026-11-20 08:47:37.409000', '2026-11-20 08:17:37.445000', 1, NULL);
INSERT INTO `sys_login_info` VALUES (778783603906576384, '2026-11-20 08:48:00.271000', 1, 'acddff1eb800000acddff1ebc00000ac', 'acecb8fbb800000acecb8fbb800001ac', '2026-11-20 09:18:00.238000', '2026-11-20 08:48:00.271000', 1, NULL);
INSERT INTO `sys_login_info` VALUES (778789124982177792, '2026-11-20 09:09:56.598000', -1, 'aced09521c00000aced09521c00001ac', 'aced09522c00000aced09522c00001ac', '2026-11-20 09:39:56.491000', '2026-11-20 09:09:56.598000', 1, NULL);
INSERT INTO `sys_login_info` VALUES (778800156081389568, '2026-11-20 09:53:46.617000', -1, 'aced09521c00000aced09521c00001ac', 'aceda9d94800000aceda9d94800001ac', '2026-11-20 10:23:46.578000', '2026-11-20 09:53:46.617000', 1, NULL);
INSERT INTO `sys_login_info` VALUES (778823005768777728, '2026-11-20 11:24:34.407000', -1, 'aced09521c00000aced09521c00001ac', 'aceef65ab800000aceef65ab800001ac', '2026-11-20 11:54:34.350000', '2026-11-20 11:24:34.407000', 1, NULL);
INSERT INTO `sys_login_info` VALUES (779163216776265728, '2026-11-21 09:56:27.032000', 1, 'aced09521c00000aced09521c00001ac', 'ad024d13b400000ad024d13b400001ad', '2026-11-21 10:26:26.989000', '2026-11-21 09:56:27.032000', 1, NULL);

-- ----------------------------
-- Table structure for sys_operate_record
-- ----------------------------
DROP TABLE IF EXISTS `sys_operate_record`;
CREATE TABLE `sys_operate_record`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `cost_time` bigint NULL DEFAULT NULL COMMENT '操作耗时（毫秒）',
  `method_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'HTTP方法类型（GET/PUT/POST等）',
  `operate_content` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作内容（调用的方法描述）',
  `operate_error` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '操作异常信息',
  `operate_error_level` int NULL DEFAULT NULL COMMENT '操作异常级别：1-用户输入错误，2-业务逻辑错误，3-数据访问错误，4-系统错误，5-严重系统错误（仅异常时写入）',
  `operate_ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作用户IP地址',
  `operate_param_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '请求参数JSON',
  `operate_result_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '操作结果JSON',
  `operate_time` datetime(6) NULL DEFAULT NULL COMMENT '操作时间',
  `operate_type` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作类型',
  `operate_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求URL',
  `operate_user_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作用户名称',
  `system_log_flag` int NULL DEFAULT NULL COMMENT '系统日志标志',
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `common_index`(`operate_content` ASC, `operate_type` ASC) USING BTREE,
  INDEX `time_idx`(`operate_time` ASC) USING BTREE,
  INDEX `error_level_idx`(`operate_error_level` ASC) USING BTREE,
  INDEX `user_idx`(`operate_user_name` ASC, `operate_ip` ASC) USING BTREE,
  INDEX `url_idx`(`operate_url` ASC) USING BTREE,
  INDEX `method_idx`(`method_type` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统操作记录表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_operate_record
-- ----------------------------

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建者用户ID',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `data_scope` int NULL DEFAULT NULL COMMENT '数据权限范围：1-全部数据，2-部门数据，3-仅本人数据，4-自定义权限',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述信息',
  `flag` int NULL DEFAULT NULL COMMENT '角色状态：1-激活，0-失效，-1-删除，2-系统内置',
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '权限名称',
  `role` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '权限编码（唯一标识）',
  `type` int NULL DEFAULT NULL COMMENT '权限类型：1-菜单权限，2-数据权限，3-API权限',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新者用户ID',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_role_type`(`role` ASC, `type` ASC) USING BTREE,
  INDEX `idx_flag`(`flag` ASC) USING BTREE,
  INDEX `idx_time`(`create_time` ASC, `update_time` ASC) USING BTREE,
  INDEX `idx_name`(`name` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统角色（权限）表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (740488168846917632, 1, '2026-08-06 16:35:37.000000', 1, '', 1, '管理员权限', 'DATA.ADMIN', 2, 1, '2026-08-06 16:42:54.000000');
INSERT INTO `sys_role` VALUES (740488338347130880, 1, '2026-08-06 16:36:17.000000', 2, '', 1, '部门下数据权限', 'DATA.DEPT', 2, 1, '2026-08-06 16:42:46.000000');
INSERT INTO `sys_role` VALUES (740490129688231936, 1, '2026-08-06 16:43:25.000000', 1, '', 1, '管理员菜单权限', 'MENU.ADMIN', 1, NULL, NULL);
INSERT INTO `sys_role` VALUES (740490307421863936, 1, '2026-08-06 16:44:07.000000', 1, '', 1, '开发者菜单权限', 'MENU.DEV', 1, NULL, NULL);
INSERT INTO `sys_role` VALUES (740749058062155776, 1, '2026-08-07 09:52:18.000000', 1, '包含单个查询和条件查询', 1, '用户查询权限', 'API.QUERY.USER', 3, 1, '2026-08-07 16:26:53.000000');
INSERT INTO `sys_role` VALUES (740749150261346304, 1, '2026-08-07 09:52:40.000000', 1, '包含公司和部门的单个查询及条件查询', 1, '公司部门查询权限', 'API.QUERY.DEPT', 3, 1, '2026-08-07 16:27:15.000000');
INSERT INTO `sys_role` VALUES (740749324849250304, 1, '2026-08-07 09:53:21.000000', NULL, '', 1, '用户保存权限', 'API.SAVE.USER', 3, NULL, NULL);
INSERT INTO `sys_role` VALUES (740749478247530496, 1, '2026-08-07 09:53:58.000000', NULL, '', 1, '公司部门储存权限', 'API.SAVE.DEPT', 3, NULL, NULL);
INSERT INTO `sys_role` VALUES (740815695784181760, 1, '2026-08-07 14:17:06.000000', NULL, '', 1, '用户登录信息查询权限', 'API.QUERY.LOGIN', 3, NULL, NULL);
INSERT INTO `sys_role` VALUES (740815895567269888, 1, '2026-08-07 14:17:53.000000', NULL, '', 1, '用户登录信息操作权限', 'API.OPERATE.LOGIN', 3, NULL, NULL);
INSERT INTO `sys_role` VALUES (740816419821715456, 1, '2026-08-07 14:19:58.000000', 3, '', 1, '自己创建数据权限', 'DATA.SELF', 2, NULL, NULL);
INSERT INTO `sys_role` VALUES (741446671241576448, 1, '2026-08-09 08:04:22.000000', 1, '', 1, '兴昊扫码拉货菜单权限', 'MENU.XH.PULL.GOODS', 1, NULL, NULL);
INSERT INTO `sys_role` VALUES (741446751637995520, 1, '2026-08-09 08:04:41.000000', 1, '', 1, '兴昊生产入库菜单权限', 'MENU.XH.INBOUND', 1, NULL, NULL);
INSERT INTO `sys_role` VALUES (741836762376044544, 1, '2026-08-10 09:54:27.000000', 1, '', 1, '菜单路由设置界面', 'MENU.ROUTE', 1, NULL, NULL);
INSERT INTO `sys_role` VALUES (745882478928986112, 1, '2026-08-21 13:50:41.000000', NULL, '账户的启用和禁用权限', 1, '账户启停权限', 'API.ACTIVE.ACCOUNT', 3, NULL, NULL);
INSERT INTO `sys_role` VALUES (745882556880125952, 1, '2026-08-21 13:50:59.000000', NULL, '', 1, '权限列表查询权限', 'API.QUERY.ROLES', 3, NULL, NULL);
INSERT INTO `sys_role` VALUES (745883841117290496, 1, '2026-08-21 13:56:06.000000', NULL, '', 1, '权限组查询权限', 'API.QUERY.ROLEGROUP', 3, NULL, NULL);
INSERT INTO `sys_role` VALUES (745883941361156096, 1, '2026-08-21 13:56:29.000000', NULL, '', 1, '查询用户的权限组数据权限', 'API.QUERY.ROLEGROUP.USER', 3, NULL, NULL);
INSERT INTO `sys_role` VALUES (745884074337370112, 1, '2026-08-21 13:57:01.000000', NULL, '', 1, '权限组编辑权限', 'API.SAVE.ROLEGROUP', 3, NULL, NULL);
INSERT INTO `sys_role` VALUES (745884193489158144, 1, '2026-08-21 13:57:30.000000', NULL, '', 1, '权限组删除权限', 'API.DELETE.ROLEGROUP', 3, NULL, NULL);
INSERT INTO `sys_role` VALUES (745884996681596928, 1, '2026-08-21 14:00:41.000000', NULL, '', 1, '日志查询权限', 'API.DEV.LOG.QUERY', 3, NULL, NULL);
INSERT INTO `sys_role` VALUES (745885106358452224, 1, '2026-08-21 14:01:07.000000', NULL, '', 1, '日志刷写权限', 'API.DEV.LOG.FLUSH', 3, NULL, NULL);
INSERT INTO `sys_role` VALUES (745885343596675072, 1, '2026-08-21 14:02:04.000000', NULL, '', 1, '应用包管理查询接口权限', 'API.DEV.VERSION.PACKAGE.QUERY', 3, NULL, NULL);
INSERT INTO `sys_role` VALUES (745885602376843264, 1, '2026-08-21 14:03:05.000000', NULL, '', 1, '版本安装包保存接口权限', 'API.DEV.VERSION.PACKAGE.SAVE', 3, NULL, NULL);
INSERT INTO `sys_role` VALUES (745885740189089792, 1, '2026-08-21 14:03:38.000000', NULL, '', 1, '版本安装包删除权限', 'API.DEV.VERSION.PACKAGE.DELETE', 3, NULL, NULL);
INSERT INTO `sys_role` VALUES (745886455393419264, 1, '2026-08-21 14:06:29.000000', NULL, '', 1, '版本更新策略查询权限', 'API.DEV.VERSION.POLICY.QUERY', 3, NULL, NULL);
INSERT INTO `sys_role` VALUES (745886591846711296, 1, '2026-08-21 14:07:01.000000', NULL, '', 1, '版本更新策略保存权限', 'API.DEV.VERSION.POLICY.SAVE', 3, NULL, NULL);
INSERT INTO `sys_role` VALUES (745886731420565504, 1, '2026-08-21 14:07:35.000000', NULL, '', 1, '版本更新策略删除权限', 'API.DEV.VERSION.POLICY.DELETE', 3, NULL, NULL);
INSERT INTO `sys_role` VALUES (745886811871510528, 1, '2026-08-21 14:07:54.000000', NULL, '', 1, '版本控制查询权限', 'API.DEV.VERSION.QUERY', 3, NULL, NULL);
INSERT INTO `sys_role` VALUES (745886893253591040, 1, '2026-08-21 14:08:13.000000', NULL, '', 1, '版本控制保存权限', 'API.DEV.VERSION.SAVE', 3, NULL, NULL);
INSERT INTO `sys_role` VALUES (745886985062711296, 1, '2026-08-21 14:08:35.000000', NULL, '', 1, '版本控制删除权限', 'API.DEV.VERSION.DELETE', 3, NULL, NULL);
INSERT INTO `sys_role` VALUES (751900000000000001, 1, '2026-11-21 08:08:11.000000', NULL, '', 1, '条件规则查询权限', 'API.DEV.CONDITION.RULE.QUERY', 3, NULL, NULL);
INSERT INTO `sys_role` VALUES (751900000000000002, 1, '2026-11-21 08:08:11.000000', NULL, '', 1, '条件规则保存权限', 'API.DEV.CONDITION.RULE.SAVE', 3, NULL, NULL);
INSERT INTO `sys_role` VALUES (751900000000000003, 1, '2026-11-21 08:08:11.000000', NULL, '', 1, '条件规则删除权限', 'API.DEV.CONDITION.RULE.DELETE', 3, NULL, NULL);
INSERT INTO `sys_role` VALUES (751910000000000001, 1, '2026-11-21 08:08:33.000000', NULL, '', 1, '事件订阅查询权限', 'API.DEV.EVENT.SUBSCRIPTION.QUERY', 3, NULL, NULL);
INSERT INTO `sys_role` VALUES (751910000000000002, 1, '2026-11-21 08:08:33.000000', NULL, '', 1, '事件订阅保存权限', 'API.DEV.EVENT.SUBSCRIPTION.SAVE', 3, NULL, NULL);
INSERT INTO `sys_role` VALUES (751910000000000003, 1, '2026-11-21 08:08:33.000000', NULL, '', 1, '事件订阅删除权限', 'API.DEV.EVENT.SUBSCRIPTION.DELETE', 3, NULL, NULL);
INSERT INTO `sys_role` VALUES (751920000000000001, 1, '2026-11-21 08:08:23.000000', NULL, '', 1, '事件动作查询权限', 'API.DEV.EVENT.ACTION.QUERY', 3, NULL, NULL);
INSERT INTO `sys_role` VALUES (751920000000000002, 1, '2026-11-21 08:08:23.000000', NULL, '', 1, '事件动作保存权限', 'API.DEV.EVENT.ACTION.SAVE', 3, NULL, NULL);
INSERT INTO `sys_role` VALUES (751920000000000003, 1, '2026-11-21 08:08:23.000000', NULL, '', 1, '事件动作删除权限', 'API.DEV.EVENT.ACTION.DELETE', 3, NULL, NULL);
INSERT INTO `sys_role` VALUES (771995899634122752, 1, '2026-11-01 15:16:05.442000', NULL, '', 1, '对象储存管理菜单权限', 'MENU.OSS', 1, NULL, NULL);
INSERT INTO `sys_role` VALUES (771996092676964352, 1, '2026-11-01 15:16:51.473000', NULL, '', 1, '版本控制菜单权限', 'MENU.VERSION', 1, NULL, NULL);
INSERT INTO `sys_role` VALUES (775879819748442112, 1, '2026-11-12 08:29:24.172000', NULL, '', -1, '123', 'API.TEST', 3, NULL, NULL);
INSERT INTO `sys_role` VALUES (778800957147316224, 1, '2026-11-20 09:56:57.599000', NULL, '', 1, '条件规则查询权限', 'API.DEV.CONDITION.RULE.QUERY', 3, NULL, NULL);
INSERT INTO `sys_role` VALUES (778801071286910976, 1, '2026-11-20 09:57:24.815000', NULL, '', 1, '条件规则储存接口', 'API.DEV.CONDITION.RULE.SAVE', 3, NULL, NULL);
INSERT INTO `sys_role` VALUES (778801171962789888, 1, '2026-11-20 09:57:48.819000', 1, '', 1, '条件规则删除权限', 'API.DEV.CONDITION.RULE.DELETE', 1, NULL, NULL);
INSERT INTO `sys_role` VALUES (778824155477835776, 1, '2026-11-20 11:29:08.514000', NULL, '', 1, 'VIP权限', 'API.VIP', 3, NULL, NULL);

-- ----------------------------
-- Table structure for sys_role_group
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_group`;
CREATE TABLE `sys_role_group`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建者用户ID',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `delete_by` bigint NULL DEFAULT NULL COMMENT '删除者用户ID',
  `delete_time` datetime(6) NULL DEFAULT NULL COMMENT '逻辑删除时间',
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色组描述',
  `flag` int NULL DEFAULT NULL COMMENT '角色组状态标识：1-启用，-1-禁用，0-删除',
  `role_group_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色组名称',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新者用户ID',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sys_role_group_flag`(`flag` ASC) USING BTREE,
  INDEX `idx_sys_role_group_role_group_name`(`role_group_name` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统角色组表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_group
-- ----------------------------
INSERT INTO `sys_role_group` VALUES (740124338757304320, 1, '2026-08-05 16:29:53.174000', NULL, NULL, '系统中最基础的权限', 1, '基础用户权限', NULL, NULL);
INSERT INTO `sys_role_group` VALUES (740491594708287488, 1, '2026-08-06 16:49:13.827000', NULL, NULL, '管理员操作菜单权限', 1, '管理员', 1, '2026-08-11 17:03:19.980000');
INSERT INTO `sys_role_group` VALUES (740721847200382976, 1, '2026-08-07 08:04:10.053000', NULL, '2026-11-20 09:11:32.791000', '网页端开发者操作菜单权限', 1, '开发者', 1, '2026-08-09 16:03:27.065000');
INSERT INTO `sys_role_group` VALUES (775879692421955584, 1, '2026-11-12 08:28:53.819000', 1, '2026-11-20 09:08:29.216000', NULL, 0, '测试权限组', NULL, NULL);
INSERT INTO `sys_role_group` VALUES (778824187337768960, 1, '2026-11-20 11:29:16.114000', NULL, NULL, NULL, 1, 'VIP', NULL, NULL);

-- ----------------------------
-- Table structure for sys_role_group_relation
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_group_relation`;
CREATE TABLE `sys_role_group_relation`  (
  `id` bigint NOT NULL COMMENT '主键ID（雪花算法生成）',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建者用户ID',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `delete_by` bigint NULL DEFAULT NULL COMMENT '删除者用户ID',
  `delete_time` datetime(6) NULL DEFAULT NULL COMMENT '逻辑删除时间',
  `flag` int NULL DEFAULT NULL COMMENT '逻辑删除标志位：1-启用，-1-禁用，0-删除',
  `group_id` bigint NULL DEFAULT NULL COMMENT '角色组ID（援引 sys_role_group.id）',
  `role_id` bigint NULL DEFAULT NULL COMMENT '角色ID（援引 sys_role.id）',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新者用户ID',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_role_group_relation_role_id`(`role_id` ASC) USING BTREE,
  INDEX `idx_role_group_relation_group_id`(`group_id` ASC) USING BTREE,
  INDEX `idx_flag`(`flag` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统角色与角色组关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_group_relation
-- ----------------------------
INSERT INTO `sys_role_group_relation` VALUES (740491647237750784, NULL, NULL, NULL, NULL, 1, 740491594708287488, 740490129688231936, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (740491648600899584, NULL, NULL, NULL, NULL, 1, 740491594708287488, 740488168846917632, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (740721964045303808, NULL, NULL, NULL, NULL, 1, 740721847200382976, 740490307421863936, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (740750249487761408, NULL, NULL, NULL, NULL, 1, 740491594708287488, 740749058062155776, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (740750249684893696, NULL, NULL, NULL, NULL, 1, 740491594708287488, 740749150261346304, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (740750249890414592, NULL, NULL, NULL, NULL, 1, 740491594708287488, 740749324849250304, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (740750250049798144, NULL, NULL, NULL, NULL, 1, 740491594708287488, 740749478247530496, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (740750346464264192, NULL, NULL, NULL, NULL, 1, 740124338757304320, 740749058062155776, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (740816474725154816, NULL, NULL, NULL, NULL, 1, 740124338757304320, 740816419821715456, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (740816602362019840, NULL, NULL, NULL, NULL, 1, 740491594708287488, 740815895567269888, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (740816602483654656, NULL, NULL, NULL, NULL, 1, 740491594708287488, 740815695784181760, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (741446922807541760, NULL, NULL, NULL, NULL, 1, 741446807162191872, 741446671241576448, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (741446959163768832, NULL, NULL, NULL, NULL, 1, 741446839999397888, 741446751637995520, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (741836832899072000, NULL, NULL, NULL, NULL, 1, 740721847200382976, 741836762376044544, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (742670643136299008, NULL, NULL, NULL, NULL, 1, 740124338757304320, 742670422062923776, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (745882640485187584, NULL, NULL, NULL, NULL, 1, 740491594708287488, 745882478928986112, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (745882640699097088, NULL, NULL, 1, '2026-08-21 15:30:18.168000', 1, 740491594708287488, 745882556880125952, 1, '2026-08-21 15:30:27.844000');
INSERT INTO `sys_role_group_relation` VALUES (745884308555694080, NULL, NULL, 1, '2026-08-21 15:30:15.923000', 1, 740491594708287488, 745883841117290496, 1, '2026-08-21 15:30:27.907000');
INSERT INTO `sys_role_group_relation` VALUES (745884308710883328, NULL, NULL, NULL, NULL, 1, 740491594708287488, 745883941361156096, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (745884308861878272, NULL, NULL, 1, '2026-08-21 15:30:11.205000', 1, 740491594708287488, 745884074337370112, 1, '2026-08-21 15:30:27.951000');
INSERT INTO `sys_role_group_relation` VALUES (745884309226782720, NULL, NULL, 1, '2026-08-21 15:30:09.037000', 1, 740491594708287488, 745884193489158144, 1, '2026-08-21 15:30:27.990000');
INSERT INTO `sys_role_group_relation` VALUES (745885170451611648, NULL, NULL, NULL, NULL, 1, 740721847200382976, 745884996681596928, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (745885170602606592, NULL, NULL, NULL, NULL, 1, 740721847200382976, 745885106358452224, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (745885794626961408, NULL, NULL, NULL, NULL, 1, 740721847200382976, 745885602376843264, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (745885794773762048, NULL, NULL, NULL, NULL, 1, 740721847200382976, 745885343596675072, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (745885794924756992, NULL, NULL, NULL, NULL, 1, 740721847200382976, 745885740189089792, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (745887074321694720, NULL, NULL, NULL, NULL, 1, 740721847200382976, 745886985062711296, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (745887074485272576, NULL, NULL, NULL, NULL, 1, 740721847200382976, 745886893253591040, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (745887074598518784, NULL, NULL, NULL, NULL, 1, 740721847200382976, 745886811871510528, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (745887074749513728, NULL, NULL, NULL, NULL, 1, 740721847200382976, 745886455393419264, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (745887074908897280, NULL, NULL, NULL, NULL, 1, 740721847200382976, 745886591846711296, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (745887075038920704, NULL, NULL, NULL, NULL, 1, 740721847200382976, 745886731420565504, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (771996315218345984, 1, '2026-11-01 15:17:44.544000', NULL, NULL, 1, 740721847200382976, 771996092676964352, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (771996315608416256, 1, '2026-11-01 15:17:44.638000', NULL, NULL, 1, 740721847200382976, 771995899634122752, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (775879883707383808, 1, '2026-11-12 08:29:39.426000', 1, '2026-11-20 08:45:02.703000', 0, 775879692421955584, 775879819748442112, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (778801256188608512, 1, '2026-11-20 09:58:08.902000', 1, '2026-11-20 11:26:22.937000', 1, 740721847200382976, 778800957147316224, 1, '2026-11-20 11:26:36.837000');
INSERT INTO `sys_role_group_relation` VALUES (778801256188608513, 1, '2026-11-20 09:58:08.902000', NULL, NULL, 1, 740721847200382976, 778801071286910976, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (778824209349476352, 1, '2026-11-20 11:29:21.363000', NULL, NULL, 1, 778824187337768960, 778824155477835776, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (779163295415271424, 1, '2026-11-21 09:56:45.779000', NULL, NULL, 1, 740721847200382976, 751910000000000001, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (779163295415271425, 1, '2026-11-21 09:56:45.781000', NULL, NULL, 1, 740721847200382976, 751900000000000001, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (779163295427854336, 1, '2026-11-21 09:56:45.783000', NULL, NULL, 1, 740721847200382976, 751900000000000003, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (779163295444631552, 1, '2026-11-21 09:56:45.787000', NULL, NULL, 1, 740721847200382976, 751900000000000002, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (779163295453020160, 1, '2026-11-21 09:56:45.790000', NULL, NULL, 1, 740721847200382976, 751910000000000002, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (779163295708872704, 1, '2026-11-21 09:56:45.851000', NULL, NULL, 1, 740721847200382976, 751910000000000003, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (779163296056999936, 1, '2026-11-21 09:56:45.934000', NULL, NULL, 1, 740721847200382976, 751920000000000003, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (779163296065388544, 1, '2026-11-21 09:56:45.936000', NULL, NULL, 1, 740721847200382976, 751920000000000002, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (779163296077971456, 1, '2026-11-21 09:56:45.939000', NULL, NULL, 1, 740721847200382976, 751920000000000001, NULL, NULL);

-- ----------------------------
-- Table structure for sys_token
-- ----------------------------
DROP TABLE IF EXISTS `sys_token`;
CREATE TABLE `sys_token`  (
  `id` bigint NOT NULL COMMENT '主键ID',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `create_type` int NULL DEFAULT NULL COMMENT '令牌创建方式：1-登录，2-刷新替换，3-第三方登录',
  `expire_time` datetime(6) NULL DEFAULT NULL COMMENT '过期时间',
  `flag` int NULL DEFAULT NULL COMMENT '令牌状态：1-有效，0-已删除，-1-已撤销，-2-已过期，-3-无效',
  `ip_address` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '登录设备IP地址',
  `mac_address` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '登录设备MAC地址',
  `session_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '会话ID',
  `token` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '令牌字符串',
  `token_type` int NOT NULL COMMENT '令牌类型：1-访问令牌，2-刷新令牌，3-应用访问令牌，4-第三方访问令牌',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `user_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名称',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_token`(`token` ASC) USING BTREE,
  INDEX `ix_user`(`user_name` ASC) USING BTREE,
  INDEX `ix_time`(`create_time` ASC, `update_time` ASC) USING BTREE,
  INDEX `ix_flag`(`flag` ASC) USING BTREE,
  INDEX `ix_expire`(`expire_time` ASC) USING BTREE,
  INDEX `ix_token_type`(`token_type` ASC) USING BTREE,
  INDEX `ix_session`(`session_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统令牌表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_token
-- ----------------------------
INSERT INTO `sys_token` VALUES (771698294400221184, '2026-10-31 19:33:30.842000', 1, '2026-10-31 20:03:30.779000', -3, '127.0.0.1', NULL, 'ab59f8285800000ab59f8285800001ab', 'ab59f8286c00000ab59f8286c00001ab', 1, '2026-10-31 19:33:36.447000', '123456');
INSERT INTO `sys_token` VALUES (771698332622913536, '2026-10-31 19:33:39.959000', 1, '2026-10-31 20:03:39.947000', 1, '127.0.0.1', NULL, 'ab59f8b7ac00000ab59f8b7ac00001ab', 'ab59f8b7ac00003ab59f8b7ac00004ab', 1, NULL, '223344');
INSERT INTO `sys_token` VALUES (771995636554792960, '2026-11-01 15:15:02.731000', 1, '2026-11-01 15:45:02.659000', -3, '127.0.0.1', NULL, 'ab6adf0de400000ab6adf0de800000ab', 'ab6adf0e1000000ab6adf0e1000001ab', 1, '2026-11-01 15:17:09.366000', '223344');
INSERT INTO `sys_token` VALUES (771996174625275904, '2026-11-01 15:17:11.025000', 1, '2026-11-01 15:47:10.981000', -3, '127.0.0.1', NULL, 'ab6ae6e31000000ab6ae6e31000001ab', 'ab6ae6e31400000ab6ae6e31400001ab', 1, '2026-11-01 15:18:00.772000', '223344');
INSERT INTO `sys_token` VALUES (771996388882907136, '2026-11-01 15:18:02.108000', 1, '2026-11-01 15:48:02.076000', -3, '127.0.0.1', NULL, 'ab6aea017000000ab6aea017000001ab', 'ab6aea017000003ab6aea017000004ab', 1, '2026-11-01 15:19:27.334000', '223344');
INSERT INTO `sys_token` VALUES (771996751983804416, '2026-11-01 15:19:28.679000', 1, '2026-11-01 15:49:28.624000', -3, '127.0.0.1', NULL, 'ab6aef49c000000ab6aef49c000001ab', 'ab6aef49c400000ab6aef49c400001ab', 1, '2026-11-01 15:23:07.161000', '223344');
INSERT INTO `sys_token` VALUES (771997673782771712, '2026-11-01 15:23:08.453000', 1, '2026-11-01 15:53:08.419000', -3, '127.0.0.1', NULL, 'ab6afcb40c00000ab6afcb40c00001ab', 'ab6afcb40c00003ab6afcb40c00004ab', 1, '2026-11-01 15:25:06.877000', '223344');
INSERT INTO `sys_token` VALUES (772001136453353472, '2026-11-01 15:36:54.006000', 1, '2026-11-01 16:06:53.698000', 1, '127.0.0.1', NULL, 'ab6b2f12e000000ab6b2f12e400000ab', 'ab6b2f130800000ab6b2f130c00000ab', 1, NULL, '223344');
INSERT INTO `sys_token` VALUES (772618071792680960, '2026-11-03 08:28:22.862000', 1, '2026-11-03 08:58:22.834000', -3, '127.0.0.1', NULL, 'ab8e40aeb800000ab8e40aeb800001ab', 'ab8e40aec800000ab8e40aecc00000ab', 1, '2026-11-03 08:28:42.510000', '01505');
INSERT INTO `sys_token` VALUES (772618165732507648, '2026-11-03 08:28:45.262000', 1, '2026-11-03 08:58:45.253000', -3, '127.0.0.1', NULL, 'ab8e420d1400000ab8e420d1400001ab', 'ab8e420d1400003ab8e420d1400004ab', 1, '2026-11-03 08:29:06.753000', '223344');
INSERT INTO `sys_token` VALUES (772618284292898816, '2026-11-03 08:29:13.529000', 1, '2026-11-03 08:59:13.513000', -3, '127.0.0.1', NULL, 'ab8e43c6a400000ab8e43c6a400001ab', 'ab8e43c6a400003ab8e43c6a400004ab', 1, '2026-11-03 08:29:18.177000', '01505');
INSERT INTO `sys_token` VALUES (772697389554204672, '2026-11-03 13:43:33.690000', 1, '2026-11-03 14:13:33.637000', -3, '127.0.0.1', NULL, 'ab92c2e80c00000ab92c2e80c00001ab', 'ab92c2e81400000ab92c2e81400001ab', 1, '2026-11-03 13:44:22.523000', '223344');
INSERT INTO `sys_token` VALUES (772697390338539520, '2026-11-03 13:43:33.879000', 2, '2026-11-10 13:43:33.864000', -3, '127.0.0.1', NULL, 'ab92c2e80c00000ab92c2e80c00001ab', 'ab92c2eba000000ab92c2eba000001ab', 2, '2026-11-03 13:44:22.567000', '223344');
INSERT INTO `sys_token` VALUES (772697644018434048, '2026-11-03 13:44:34.362000', 1, '2026-11-03 14:14:34.349000', -3, '127.0.0.1', NULL, 'ab92c69cb400000ab92c69cb400001ab', 'ab92c69cb400003ab92c69cb400004ab', 1, '2026-11-03 13:44:54.077000', 'admin');
INSERT INTO `sys_token` VALUES (772697644349784064, '2026-11-03 13:44:34.441000', 2, '2026-11-10 13:44:34.432000', -3, '127.0.0.1', NULL, 'ab92c69cb400000ab92c69cb400001ab', 'ab92c69e0000000ab92c69e0000001ab', 2, '2026-11-03 13:44:54.120000', 'admin');
INSERT INTO `sys_token` VALUES (772697736624472064, '2026-11-03 13:44:56.441000', 1, '2026-11-03 14:14:56.432000', 1, '127.0.0.1', NULL, 'ab92c7f5c000000ab92c7f5c000001ab', 'ab92c7f5c000003ab92c7f5c000004ab', 1, NULL, '223344');
INSERT INTO `sys_token` VALUES (772697736918073344, '2026-11-03 13:44:56.511000', 2, '2026-11-10 13:44:56.502000', -3, '127.0.0.1', NULL, 'ab92c7f5c000000ab92c7f5c000001ab', 'ab92c7f6d800000ab92c7f6d800001ab', 2, '2026-11-04 09:37:31.943000', '223344');
INSERT INTO `sys_token` VALUES (772984608097566720, '2026-11-04 08:44:51.925000', 1, '2026-11-04 09:14:51.895000', -1, '127.0.0.1', NULL, 'aba3167ccc00000aba3167cd000000ab', 'aba3167cdc00000aba3167cdc00001ab', 1, '2026-11-04 09:00:30.852000', '223344');
INSERT INTO `sys_token` VALUES (772997861829771264, '2026-11-04 09:37:31.862000', 2, '2026-11-04 10:07:31.825000', -2, '127.0.0.1', NULL, 'ab92c7f5c000000ab92c7f5c000001ab', 'aba3d75ac400000aba3d75ac400001ab', 1, '2026-11-04 10:07:49.518000', '223344');
INSERT INTO `sys_token` VALUES (773005500563324928, '2026-11-04 10:07:53.081000', 1, '2026-11-04 10:37:53.066000', -1, '127.0.0.1', NULL, 'aba44683a400000aba44683a400001ab', 'aba44683a800000aba44683a800001ab', 1, '2026-11-04 10:08:10.441000', '223344');
INSERT INTO `sys_token` VALUES (773005657191219200, '2026-11-04 10:08:30.425000', 1, '2026-11-04 10:38:30.411000', -1, '127.0.0.1', NULL, 'aba448cb2c00000aba448cb2c00001ab', 'aba448cb2c00003aba448cb2c00004ab', 1, '2026-11-04 10:08:49.875000', '223344');
INSERT INTO `sys_token` VALUES (773005657530957824, '2026-11-04 10:08:30.506000', 2, '2026-11-11 10:08:30.496000', -3, '127.0.0.1', NULL, 'aba448cb2c00000aba448cb2c00001ab', 'aba448cc8000000aba448cc8000001ab', 2, '2026-11-04 10:08:53.287000', '223344');
INSERT INTO `sys_token` VALUES (773005752859099136, '2026-11-04 10:08:53.234000', 2, '2026-11-04 10:38:53.223000', -2, '127.0.0.1', NULL, 'aba448cb2c00000aba448cb2c00001ab', 'aba44a2f9c00000aba44a2f9c00001ab', 1, '2026-11-04 10:55:47.425000', '223344');
INSERT INTO `sys_token` VALUES (773005753366609920, '2026-11-04 10:08:53.355000', 2, '2026-11-11 10:08:53.343000', -3, '127.0.0.1', NULL, 'aba448cb2c00000aba448cb2c00001ab', 'aba44a317c00000aba44a317c00001ab', 2, '2026-11-04 10:55:47.984000', '223344');
INSERT INTO `sys_token` VALUES (773017558549790720, '2026-11-04 10:55:47.929000', 2, '2026-11-04 11:25:47.916000', 1, '127.0.0.1', NULL, 'aba448cb2c00000aba448cb2c00001ab', 'aba4f5fb3000000aba4f5fb3000001ab', 1, NULL, '223344');
INSERT INTO `sys_token` VALUES (773017559363485696, '2026-11-04 10:55:48.124000', 2, '2026-11-11 10:55:48.111000', 1, '127.0.0.1', NULL, 'aba448cb2c00000aba448cb2c00001ab', 'aba4f5fe3c00000aba4f5fe3c00001ab', 2, NULL, '223344');
INSERT INTO `sys_token` VALUES (773101407749275648, '2026-11-04 16:28:59.134000', 1, '2026-11-04 16:58:59.109000', -3, '127.0.0.1', NULL, 'aba9ba258c00000aba9ba258c00001ab', 'aba9ba259400000aba9ba259400001ab', 1, '2026-11-04 16:29:09.623000', '223344');
INSERT INTO `sys_token` VALUES (773101408411975680, '2026-11-04 16:28:59.295000', 2, '2026-11-11 16:28:59.284000', -3, '127.0.0.1', NULL, 'aba9ba258c00000aba9ba258c00001ab', 'aba9ba285000000aba9ba285000001ab', 2, '2026-11-04 16:29:09.690000', '223344');
INSERT INTO `sys_token` VALUES (773101473306247168, '2026-11-04 16:29:14.767000', 1, '2026-11-04 16:59:14.758000', -2, '127.0.0.1', NULL, 'aba9bb1a1400000aba9bb1a1400001ab', 'aba9bb1a1800000aba9bb1a1800001ab', 1, '2026-11-05 10:00:35.031000', '223344');
INSERT INTO `sys_token` VALUES (773101473662763008, '2026-11-04 16:29:14.852000', 2, '2026-11-11 16:29:14.842000', -3, '127.0.0.1', NULL, 'aba9bb1a1400000aba9bb1a1400001ab', 'aba9bb1b6800000aba9bb1b6800001ab', 2, '2026-11-05 10:00:35.553000', '223344');
INSERT INTO `sys_token` VALUES (773366053018271744, '2026-11-05 10:00:35.485000', 2, '2026-11-05 10:30:35.473000', -2, '127.0.0.1', NULL, 'aba9bb1a1400000aba9bb1a1400001ab', 'abb8c53e4400000abb8c53e4400001ab', 1, '2026-11-05 10:40:49.738000', '223344');
INSERT INTO `sys_token` VALUES (773366053920047104, '2026-11-05 10:00:35.700000', 2, '2026-11-12 10:00:35.687000', -3, '127.0.0.1', NULL, 'aba9bb1a1400000aba9bb1a1400001ab', 'abb8c5419c00000abb8c541a000000ab', 2, '2026-11-05 10:40:50.216000', '223344');
INSERT INTO `sys_token` VALUES (773376180865728512, '2026-11-05 10:40:50.152000', 2, '2026-11-05 11:10:50.138000', -2, '127.0.0.1', NULL, 'aba9bb1a1400000aba9bb1a1400001ab', 'abb9589f6800000abb9589f6c00000ab', 1, '2026-11-05 13:10:45.101000', '223344');
INSERT INTO `sys_token` VALUES (773376181788475392, '2026-11-05 10:40:50.373000', 2, '2026-11-12 10:40:50.354000', -3, '127.0.0.1', NULL, 'aba9bb1a1400000aba9bb1a1400001ab', 'abb958a2c800000abb958a2c800001ab', 2, '2026-11-05 13:10:45.588000', '223344');
INSERT INTO `sys_token` VALUES (773413910240821248, '2026-11-05 13:10:45.537000', 2, '2026-11-05 13:40:45.526000', -2, '127.0.0.1', NULL, 'aba9bb1a1400000aba9bb1a1400001ab', 'abbb7da85800000abbb7da85c00000ab', 1, '2026-11-05 14:38:41.771000', '223344');
INSERT INTO `sys_token` VALUES (773413910983213056, '2026-11-05 13:10:45.714000', 2, '2026-11-12 13:10:45.700000', -3, '127.0.0.1', NULL, 'aba9bb1a1400000aba9bb1a1400001ab', 'abbb7dab1000000abbb7dab1000001ab', 2, '2026-11-05 14:38:42.174000', '223344');
INSERT INTO `sys_token` VALUES (773436041842393088, '2026-11-05 14:38:42.121000', 2, '2026-11-05 15:08:42.108000', -2, '127.0.0.1', NULL, 'aba9bb1a1400000aba9bb1a1400001ab', 'abbcbfb6f000000abbcbfb6f000001ab', 1, '2026-11-06 13:24:59.923000', '223344');
INSERT INTO `sys_token` VALUES (773436042685448192, '2026-11-05 14:38:42.323000', 2, '2026-11-12 14:38:42.308000', -3, '127.0.0.1', NULL, 'aba9bb1a1400000aba9bb1a1400001ab', 'abbcbfba1000000abbcbfba1400000ab', 2, '2026-11-06 13:25:00.438000', '223344');
INSERT INTO `sys_token` VALUES (773779883615584256, '2026-11-06 13:25:00.388000', 2, '2026-11-06 13:55:00.376000', -2, '127.0.0.1', NULL, 'aba9bb1a1400000aba9bb1a1400001ab', 'abd04b456000000abd04b456000001ab', 1, '2026-11-06 13:59:29.284000', '223344');
INSERT INTO `sys_token` VALUES (773779884378947584, '2026-11-06 13:25:00.571000', 2, '2026-11-13 13:25:00.556000', -3, '127.0.0.1', NULL, 'aba9bb1a1400000aba9bb1a1400001ab', 'abd04b483000000abd04b483400000ab', 2, '2026-11-06 13:59:34.251000', '223344');
INSERT INTO `sys_token` VALUES (773788581809356800, '2026-11-06 13:59:34.199000', 2, '2026-11-06 14:29:34.185000', 1, '127.0.0.1', NULL, 'aba9bb1a1400000aba9bb1a1400001ab', 'abd0c9d8a400000abd0c9d8a800000ab', 1, NULL, '223344');
INSERT INTO `sys_token` VALUES (773788582572720128, '2026-11-06 13:59:34.382000', 2, '2026-11-13 13:59:34.369000', 1, '127.0.0.1', NULL, 'aba9bb1a1400000aba9bb1a1400001ab', 'abd0c9db8400000abd0c9db8400001ab', 2, NULL, '223344');
INSERT INTO `sys_token` VALUES (773788602604716032, '2026-11-06 13:59:39.158000', 1, '2026-11-06 14:29:39.143000', 1, '127.0.0.1', NULL, 'abd0ca261c00000abd0ca261c00001ab', 'abd0ca261c00003abd0ca261c00004ab', 1, NULL, '223344');
INSERT INTO `sys_token` VALUES (773788603066089472, '2026-11-06 13:59:39.268000', 2, '2026-11-13 13:59:39.252000', -3, '127.0.0.1', NULL, 'abd0ca261c00000abd0ca261c00001ab', 'abd0ca27d000000abd0ca27d000001ab', 2, '2026-11-06 14:29:53.579000', '223344');
INSERT INTO `sys_token` VALUES (773796212607287296, '2026-11-06 14:29:53.522000', 2, '2026-11-06 14:59:53.504000', -2, '127.0.0.1', NULL, 'abd0ca261c00000abd0ca261c00001ab', 'abd138e38000000abd138e38000001ab', 1, '2026-11-06 15:15:34.447000', '223344');
INSERT INTO `sys_token` VALUES (773796213144158208, '2026-11-06 14:29:53.652000', 2, '2026-11-13 14:29:53.636000', -3, '127.0.0.1', NULL, 'abd0ca261c00000abd0ca261c00001ab', 'abd138e59000000abd138e59000001ab', 2, '2026-11-06 15:15:34.882000', '223344');
INSERT INTO `sys_token` VALUES (773807710465425408, '2026-11-06 15:15:34.827000', 2, '2026-11-06 15:45:34.813000', -2, '127.0.0.1', NULL, 'abd0ca261c00000abd0ca261c00001ab', 'abd1e0347400000abd1e0347400001ab', 1, '2026-11-06 16:27:23.096000', '223344');
INSERT INTO `sys_token` VALUES (773807712411582464, '2026-11-06 15:15:35.290000', 2, '2026-11-13 15:15:35.274000', -3, '127.0.0.1', NULL, 'abd0ca261c00000abd0ca261c00001ab', 'abd1e03ba800000abd1e03ba800001ab', 2, '2026-11-06 16:27:23.653000', '223344');
INSERT INTO `sys_token` VALUES (773825782807003136, '2026-11-06 16:27:23.608000', 2, '2026-11-06 16:57:23.596000', 1, '127.0.0.1', NULL, 'abd0ca261c00000abd0ca261c00001ab', 'abd2e7313000000abd2e7313000001ab', 1, NULL, '223344');
INSERT INTO `sys_token` VALUES (773825783687806976, '2026-11-06 16:27:23.819000', 2, '2026-11-13 16:27:23.799000', -3, '127.0.0.1', NULL, 'abd0ca261c00000abd0ca261c00001ab', 'abd2e7345c00000abd2e7345c00001ab', 2, '2026-11-06 16:57:32.352000', '223344');
INSERT INTO `sys_token` VALUES (773833368923013120, '2026-11-06 16:57:32.277000', 2, '2026-11-06 17:27:32.242000', -2, '127.0.0.1', NULL, 'abd0ca261c00000abd0ca261c00001ab', 'abd355954800000abd355954800001ab', 1, '2026-11-07 09:23:10.432000', '223344');
INSERT INTO `sys_token` VALUES (773833369543770112, '2026-11-06 16:57:32.428000', 2, '2026-11-13 16:57:32.418000', -3, '127.0.0.1', NULL, 'abd0ca261c00000abd0ca261c00001ab', 'abd355980800000abd355980800001ab', 2, '2026-11-07 09:23:11.132000', '223344');
INSERT INTO `sys_token` VALUES (774081414961299456, '2026-11-07 09:23:11.064000', 2, '2026-11-07 09:53:11.046000', 1, '127.0.0.1', NULL, 'abd0ca261c00000abd0ca261c00001ab', 'abe16f211800000abe16f211800001ab', 1, NULL, '223344');
INSERT INTO `sys_token` VALUES (774081416030846976, '2026-11-07 09:23:11.319000', 2, '2026-11-14 09:23:11.301000', 1, '127.0.0.1', NULL, 'abd0ca261c00000abd0ca261c00001ab', 'abe16f251400000abe16f251400001ab', 2, NULL, '223344');
INSERT INTO `sys_token` VALUES (774860894453628928, '2026-11-09 13:00:33.454000', 1, '2026-11-09 13:30:33.430000', 1, '127.0.0.1', NULL, 'ac0dbe0c4800000ac0dbe0c4800001ac', 'ac0dbe0c5800000ac0dbe0c5800001ac', 1, NULL, '223344');
INSERT INTO `sys_token` VALUES (774860896215236608, '2026-11-09 13:00:33.877000', 2, '2026-11-16 13:00:33.861000', 1, '127.0.0.1', NULL, 'ac0dbe0c4800000ac0dbe0c4800001ac', 'ac0dbe131400000ac0dbe131400001ac', 2, NULL, '223344');
INSERT INTO `sys_token` VALUES (775879548318253056, '2026-11-12 08:28:19.460000', 1, '2026-11-12 08:58:19.436000', -2, '127.0.0.1', NULL, 'ac47a569a400000ac47a569a400001ac', 'ac47a569b000000ac47a569b000001ac', 1, '2026-11-12 13:40:09.387000', '223344');
INSERT INTO `sys_token` VALUES (775879548972564480, '2026-11-12 08:28:19.619000', 2, '2026-11-19 08:28:19.605000', -3, '127.0.0.1', NULL, 'ac47a569a400000ac47a569a400001ac', 'ac47a56c5400000ac47a56c5400001ac', 2, '2026-11-12 13:40:09.867000', '223344');
INSERT INTO `sys_token` VALUES (775958025247653888, '2026-11-12 13:40:09.822000', 2, '2026-11-12 14:10:09.806000', 1, '127.0.0.1', NULL, 'ac47a569a400000ac47a569a400001ac', 'ac4c1b673800000ac4c1b673800001ac', 1, NULL, '223344');
INSERT INTO `sys_token` VALUES (775958025990045696, '2026-11-12 13:40:09.999000', 2, '2026-11-19 13:40:09.984000', 1, '127.0.0.1', NULL, 'ac47a569a400000ac47a569a400001ac', 'ac4c1b6a0000000ac4c1b6a0000001ac', 2, NULL, '223344');
INSERT INTO `sys_token` VALUES (777444059496054784, '2026-11-16 16:05:07.992000', 1, '2026-11-16 16:35:07.962000', 1, '127.0.0.1', NULL, 'aca0940bd800000aca0940bd800001ac', 'aca0940be800000aca0940be800001ac', 1, NULL, '223344');
INSERT INTO `sys_token` VALUES (777444060112617472, '2026-11-16 16:05:08.143000', 2, '2026-11-23 16:05:08.131000', 1, '127.0.0.1', NULL, 'aca0940bd800000aca0940bd800001ac', 'aca0940e8c00000aca0940e9000000ac', 2, NULL, '223344');
INSERT INTO `sys_token` VALUES (778499606689349632, '2026-11-19 13:59:30.052000', 1, '2026-11-19 14:29:30.024000', -2, '127.0.0.1', NULL, 'acdc94479000000acdc94479000001ac', 'acdc9447a000000acdc9447a000001ac', 1, '2026-11-19 14:46:12.683000', '223344');
INSERT INTO `sys_token` VALUES (778499607314300928, '2026-11-19 13:59:30.207000', 2, '2026-11-26 13:59:30.195000', -3, '127.0.0.1', NULL, 'acdc94479000000acdc94479000001ac', 'acdc944a4c00000acdc944a4c00001ac', 2, '2026-11-19 14:46:13.232000', '223344');
INSERT INTO `sys_token` VALUES (778511363822583808, '2026-11-19 14:46:13.176000', 2, '2026-11-19 15:16:13.160000', 1, '127.0.0.1', NULL, 'acdc94479000000acdc94479000001ac', 'acdd3f5ea000000acdd3f5ea000001ac', 1, NULL, '223344');
INSERT INTO `sys_token` VALUES (778511364690804736, '2026-11-19 14:46:13.384000', 2, '2026-11-26 14:46:13.360000', -3, '127.0.0.1', NULL, 'acdc94479000000acdc94479000001ac', 'acdd3f61c000000acdd3f61c000001ac', 2, '2026-11-19 15:16:14.788000', '223344');
INSERT INTO `sys_token` VALUES (778518920108703744, '2026-11-19 15:16:14.731000', 2, '2026-11-19 15:46:14.680000', 1, '127.0.0.1', NULL, 'acdc94479000000acdc94479000001ac', 'acddad536000000acddad536000001ac', 1, NULL, '223344');
INSERT INTO `sys_token` VALUES (778518920553299968, '2026-11-19 15:16:14.842000', 2, '2026-11-26 15:16:14.832000', 1, '127.0.0.1', NULL, 'acdc94479000000acdc94479000001ac', 'acddad55c000000acddad55c400000ac', 2, NULL, '223344');
INSERT INTO `sys_token` VALUES (778524541063266304, '2026-11-19 15:38:34.871000', 1, '2026-11-19 16:08:34.801000', -2, '127.0.0.1', NULL, 'acddff1eb800000acddff1ebc00000ac', 'acddff1ec400000acddff1ec400001ac', 1, '2026-11-20 08:17:36.999000', '223344');
INSERT INTO `sys_token` VALUES (778524541935681536, '2026-11-19 15:38:35.084000', 2, '2026-11-26 15:38:35.065000', -3, '127.0.0.1', NULL, 'acddff1eb800000acddff1ebc00000ac', 'acddff22e400000acddff22e400001ac', 2, '2026-11-20 08:17:37.461000', '223344');
INSERT INTO `sys_token` VALUES (778775958319529984, '2026-11-20 08:17:37.421000', 2, '2026-11-20 08:47:37.409000', -2, '127.0.0.1', NULL, 'acddff1eb800000acddff1ebc00000ac', 'acec49ba0400000acec49ba0400001ac', 1, '2026-11-20 08:48:00.147000', '223344');
INSERT INTO `sys_token` VALUES (778775959007395840, '2026-11-20 08:17:37.585000', 2, '2026-11-27 08:17:37.573000', -3, '127.0.0.1', NULL, 'acddff1eb800000acddff1ebc00000ac', 'acec49bc9400000acec49bc9400001ac', 2, '2026-11-20 08:48:00.288000', '223344');
INSERT INTO `sys_token` VALUES (778783603810107392, '2026-11-20 08:48:00.248000', 2, '2026-11-20 09:18:00.238000', 1, '127.0.0.1', NULL, 'acddff1eb800000acddff1ebc00000ac', 'acecb8fbb800000acecb8fbb800001ac', 1, NULL, '223344');
INSERT INTO `sys_token` VALUES (778783604326006784, '2026-11-20 08:48:00.371000', 2, '2026-11-27 08:48:00.362000', 1, '127.0.0.1', NULL, 'acddff1eb800000acddff1ebc00000ac', 'acecb8fda800000acecb8fda800001ac', 2, NULL, '223344');
INSERT INTO `sys_token` VALUES (778789124755685376, '2026-11-20 09:09:56.540000', 1, '2026-11-20 09:39:56.491000', -2, '127.0.0.1', NULL, 'aced09521c00000aced09521c00001ac', 'aced09522c00000aced09522c00001ac', 1, '2026-11-20 09:53:46.289000', '223344');
INSERT INTO `sys_token` VALUES (778789125707792384, '2026-11-20 09:09:56.771000', 2, '2026-11-27 09:09:56.750000', -3, '127.0.0.1', NULL, 'aced09521c00000aced09521c00001ac', 'aced09563800000aced09564000000ac', 2, '2026-11-20 09:53:46.638000', '223344');
INSERT INTO `sys_token` VALUES (778800155972337664, '2026-11-20 09:53:46.590000', 2, '2026-11-20 10:23:46.578000', -2, '127.0.0.1', NULL, 'aced09521c00000aced09521c00001ac', 'aceda9d94800000aceda9d94800001ac', 1, '2026-11-20 11:24:33.981000', '223344');
INSERT INTO `sys_token` VALUES (778800156844752896, '2026-11-20 09:53:46.799000', 2, '2026-11-27 09:53:46.783000', -3, '127.0.0.1', NULL, 'aced09521c00000aced09521c00001ac', 'aceda9dc7c00000aceda9dc7c00001ac', 2, '2026-11-20 11:24:34.430000', '223344');
INSERT INTO `sys_token` VALUES (778823005592616960, '2026-11-20 11:24:34.365000', 2, '2026-11-20 11:54:34.350000', -2, '127.0.0.1', NULL, 'aced09521c00000aced09521c00001ac', 'aceef65ab800000aceef65ab800001ac', 1, '2026-11-21 09:56:26.524000', '223344');
INSERT INTO `sys_token` VALUES (778823006452449280, '2026-11-20 11:24:34.570000', 2, '2026-11-27 11:24:34.558000', -3, '127.0.0.1', NULL, 'aced09521c00000aced09521c00001ac', 'aceef65df800000aceef65df800001ac', 2, '2026-11-21 09:56:27.056000', '223344');
INSERT INTO `sys_token` VALUES (779163216654630912, '2026-11-21 09:56:27.003000', 2, '2026-11-21 10:26:26.989000', 1, '127.0.0.1', NULL, 'aced09521c00000aced09521c00001ac', 'ad024d13b400000ad024d13b400001ad', 1, NULL, '223344');
INSERT INTO `sys_token` VALUES (779163217480908800, '2026-11-21 09:56:27.200000', 2, '2026-11-28 09:56:27.182000', 1, '127.0.0.1', NULL, 'aced09521c00000aced09521c00001ac', 'ad024d16b800000ad024d16b800001ad', 2, NULL, '223344');

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL COMMENT '用户ID（雪花算法生成）',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像URL',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建者用户ID',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `dept_id` bigint NULL DEFAULT NULL COMMENT '主要所属部门ID',
  `flag` int NULL DEFAULT NULL COMMENT '账号状态：1-激活，2-锁定，0-禁用，-1-删除',
  `nick_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '昵称',
  `password` varchar(72) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码（加密存储）',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `update_by` bigint NULL DEFAULT NULL COMMENT '最后更新者用户ID',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '修改时间',
  `user_name` varchar(72) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_username_password`(`user_name` ASC, `password` ASC) USING BTREE,
  INDEX `idx_nickname`(`nick_name` ASC) USING BTREE,
  INDEX `idx_flag`(`flag` ASC) USING BTREE,
  INDEX `idx_time`(`create_time` ASC, `update_time` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'https://q9.itc.cn/q_70/images03/20241013/d770472d4906402c866b9c71a0c9927c.jpeg', NULL, '2025-07-23 16:34:17.000000', NULL, 1, 'abstergo', '!Ty19970923', '管理员账户', 1, '2026-08-16 07:51:27.000000', '223344');
INSERT INTO `sys_user` VALUES (2, 'https://ts1.tc.mm.bing.net/th/id/OIP-C.LjLA176cgjhpSHo2kHU_1AAAAA?rs=1&pid=ImgDetMain&o=7&rm=3', 1, '2025-08-16 11:56:25.000000', NULL, -1, '测试用户1', '!Ty19970923', 'te367898', NULL, '2026-06-30 13:54:46.000000', 'test123456');
INSERT INTO `sys_user` VALUES (3, '', 1, '2025-08-16 12:13:10.000000', NULL, -1, 'jackBauer', 'Ty1997923!', 'test123', NULL, '2026-06-30 13:54:49.000000', '456789');
INSERT INTO `sys_user` VALUES (4, '', 1, '2025-08-16 12:38:43.000000', NULL, -1, 'TonyAmeda', 'Ty1997923!', '', NULL, '2026-06-30 13:54:52.000000', '789456');
INSERT INTO `sys_user` VALUES (5, '', 1, '2025-08-16 18:21:16.000000', NULL, -1, '123456789', '!Ty1997923', '', NULL, '2026-06-30 13:54:57.000000', '123456789');
INSERT INTO `sys_user` VALUES (6, 'https://img0.baidu.com/it/u=288127173,2280192168&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500', 1, '2025-08-16 19:04:14.000000', NULL, -1, 'tonyJames', '!Ty1997923', '', NULL, '2026-06-30 13:55:00.000000', '1234565');
INSERT INTO `sys_user` VALUES (7, '', 1, '2025-08-21 12:10:51.000000', NULL, -1, 'teset123456', '!Ty199709923', '', NULL, '2026-06-30 13:55:02.000000', 'winTest1234');
INSERT INTO `sys_user` VALUES (8, NULL, 1, '2025-09-03 10:57:06.000000', NULL, -1, 'jackTom', '!Ty19970923', NULL, NULL, '2026-06-30 13:55:05.000000', '77889945');
INSERT INTO `sys_user` VALUES (9, NULL, 1, '2025-09-13 18:46:40.000000', NULL, -1, 'jamesJohn', '!Ty19970923', NULL, NULL, '2026-06-30 13:55:08.000000', '789456Tony');
INSERT INTO `sys_user` VALUES (10, 'https://q2.itc.cn/q_70/images03/20250225/e8117dd40aae4db5a64461f1ea0d16fc.jpeg', 1, '2025-09-13 18:49:17.000000', NULL, -1, '123456', '!Ty19970923', NULL, NULL, '2026-06-30 13:55:11.000000', 'tonyJACK');
INSERT INTO `sys_user` VALUES (11, NULL, 1, '2025-11-12 17:23:59.000000', NULL, -1, '22334567', '!Ty19970923', NULL, NULL, '2026-06-30 13:54:35.000000', 'helloworld');
INSERT INTO `sys_user` VALUES (12, NULL, 1, '2025-11-14 18:40:54.000000', NULL, -1, 'gotoMaker', '!Ty19970923', NULL, NULL, NULL, 'gotoMaker');
INSERT INTO `sys_user` VALUES (13, NULL, 1, '2025-11-17 17:10:51.000000', NULL, -1, '测试用户', 'Password123!', NULL, NULL, NULL, 'testuser123');
INSERT INTO `sys_user` VALUES (14, 'https://fuss10.elemecdn.com/e/5d/4a731a90594a4af544c0c25941171jpeg.jpeg', 1, '2025-07-23 16:13:10.000000', NULL, 1, '超级用户', '!Ty19970923', 'super user for me', NULL, '2025-11-17 17:11:10.000000', '123456');
INSERT INTO `sys_user` VALUES (16, NULL, 1, '2026-06-15 18:31:21.000000', NULL, -1, 'bbbbb', '!Ty19970923', NULL, 1, '2026-07-31 11:16:46.000000', 'aaaaaaaa');
INSERT INTO `sys_user` VALUES (17, NULL, 1, '2026-06-15 18:45:51.000000', NULL, -1, 'bbbbb', '!Ty19970923', NULL, 1, '2026-07-31 11:16:32.000000', 'aaaaaa');
INSERT INTO `sys_user` VALUES (18, NULL, 1, '2026-06-25 14:46:30.000000', NULL, 1, '童泊森', '123456', NULL, 1, '2026-11-03 08:29:01.818000', '01505');
INSERT INTO `sys_user` VALUES (19, NULL, 1, '2026-06-25 15:02:30.000000', NULL, 1, '李书倩', '00633', NULL, NULL, '2026-06-25 16:27:42.000000', '00633');
INSERT INTO `sys_user` VALUES (737192189162946560, NULL, 1, '2026-07-28 14:18:33.000000', NULL, 1, '史小艾', '00413', NULL, 1, '2026-07-28 14:18:51.000000', '00413');
INSERT INTO `sys_user` VALUES (737192428523487232, NULL, 1, '2026-07-28 14:19:31.000000', NULL, 1, '张进', '00129', NULL, 1, '2026-07-28 14:22:21.000000', '00219');
INSERT INTO `sys_user` VALUES (737192677283463168, NULL, 1, '2026-07-28 14:20:31.000000', NULL, 1, '汪宗超', '00688', NULL, 1, '2026-07-28 14:22:09.000000', '00688');
INSERT INTO `sys_user` VALUES (737192878358396928, NULL, 1, '2026-07-28 14:21:19.000000', NULL, 1, '肖立涛', '00341', NULL, 1, '2026-07-28 14:22:12.000000', '00341');
INSERT INTO `sys_user` VALUES (737193046583541760, NULL, 1, '2026-07-28 14:21:59.000000', NULL, 1, '陆辉', '01060', NULL, 1, '2026-07-28 14:22:15.000000', '01060');
INSERT INTO `sys_user` VALUES (738291152687464448, NULL, 17, '2026-07-31 15:05:27.000000', NULL, -1, 'test123', '!Ty19970923', NULL, NULL, NULL, 'Test123');
INSERT INTO `sys_user` VALUES (739774367356944384, NULL, 1, '2026-08-04 17:19:13.000000', NULL, 1, 'admin', '123456', '兴昊管理员账号', 1, '2026-08-09 15:59:46.000000', 'admin');
INSERT INTO `sys_user` VALUES (740814928180412416, NULL, 18, '2026-08-07 14:14:01.000000', NULL, -1, '123444', '01505', NULL, NULL, NULL, 'test12345');
INSERT INTO `sys_user` VALUES (740822608424992768, NULL, 1, '2026-08-07 14:44:34.000000', NULL, -1, '223344', '!Ty19970923', NULL, NULL, NULL, 'helloworld');

-- ----------------------------
-- Table structure for sys_user_external_identity
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_external_identity`;
CREATE TABLE `sys_user_external_identity`  (
  `id` bigint NOT NULL COMMENT '绑定ID（雪花算法生成）',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建者用户ID',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `external_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '外部身份标识（如企微 userid）',
  `flag` int NULL DEFAULT NULL COMMENT '绑定状态（1为有效）',
  `identity_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '外部身份类型（如 wecom）',
  `update_by` bigint NULL DEFAULT NULL COMMENT '最后更新者用户ID',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '修改时间',
  `user_id` bigint NOT NULL COMMENT '绑定的系统用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_identity_type_external_id`(`identity_type` ASC, `external_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_flag`(`flag` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统用户外部身份绑定表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_external_identity
-- ----------------------------
INSERT INTO `sys_user_external_identity` VALUES (755294327596384256, NULL, '2026-09-16 13:10:00.385000', '15606810923', 0, 'wecom', NULL, '2026-09-16 13:46:29.806000', 1);
INSERT INTO `sys_user_external_identity` VALUES (755305353435938816, NULL, '2026-09-16 13:53:49.152000', '15606810923', 0, 'wecom', NULL, '2026-09-16 14:01:23.276000', 1);
INSERT INTO `sys_user_external_identity` VALUES (755307277606453248, NULL, '2026-09-16 14:01:27.910000', '15606810923', 1, 'wecom', NULL, NULL, 1);

-- ----------------------------
-- Table structure for system_data_dic_item
-- ----------------------------
DROP TABLE IF EXISTS `system_data_dic_item`;
CREATE TABLE `system_data_dic_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID（自增）',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `dic_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '所属数据字典编码',
  `flag` int NULL DEFAULT NULL COMMENT '数据字典项状态：1-启用，-1-停用，0-删除',
  `label` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典项显示标签（展示给用户的名称）',
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `sort` int NULL DEFAULT NULL COMMENT '排序号（升序）',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '修改时间',
  `value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '字典项取值（业务存储与比较的实际值）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_system_data_dic_item_dic_code`(`dic_code` ASC) USING BTREE,
  INDEX `idx_flag`(`flag` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '数据字典项表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of system_data_dic_item
-- ----------------------------
INSERT INTO `system_data_dic_item` VALUES (1, '2026-06-24 15:38:08.818000', 'SYSTEM', -1, '用户名密码校验功能开关', '开启用户名密码校验功能', 0, '2026-08-10 09:23:45.379000', '1');
INSERT INTO `system_data_dic_item` VALUES (2, '2026-06-25 13:23:30.480000', 'DIC_XH_GD_SCRK_DOC_MAP', 1, '5101', '注塑工单转注塑入库单', 0, '2026-07-01 09:39:49.714000', '5801');
INSERT INTO `system_data_dic_item` VALUES (4, '2026-07-24 09:35:23.239000', 'GENRIC_API_ID_DIC', 0, 'MES.OUTSTATION.BARCODE', NULL, NULL, '2026-07-24 09:55:19.167000', 'com.tbs.xh.mes.service.IMesQueryService');
INSERT INTO `system_data_dic_item` VALUES (5, '2026-07-24 09:36:09.140000', 'GENRIC_API_METHOD_DIC', 0, 'MES.OUTSTATION.BARCODE', NULL, NULL, '2026-08-04 11:15:10.963000', 'queryOutStationBarCodeInfos');
INSERT INTO `system_data_dic_item` VALUES (6, '2026-07-24 09:55:35.758000', 'GENRIC_API_ID_DIC', 0, 'MES.OUTSTATION.BARCODE', NULL, NULL, '2026-07-24 14:24:15.323000', 'dubbo-xh-mes-service');
INSERT INTO `system_data_dic_item` VALUES (7, '2026-07-24 14:24:40.788000', 'GENRIC_API_ID_DIC', 0, 'MES.OUTSTATION.BARCODE', 'MES出站数据查询', NULL, '2026-07-24 14:31:48.961000', 'com.tbs.xh.mes.service.IMesQueryService');
INSERT INTO `system_data_dic_item` VALUES (8, '2026-07-24 14:31:43.031000', 'GENRIC_API_INTERFACE_DIC', 0, 'MES.OUTSTATION.BARCODE', NULL, NULL, '2026-08-04 11:15:42.216000', 'com.tbs.xh.mes.service.IMesQueryService');
INSERT INTO `system_data_dic_item` VALUES (9, '2026-07-24 14:32:16.077000', 'GENRIC_API_ID_DIC', 0, 'MES.OUTSTATION.BARCODE', NULL, NULL, '2026-08-04 11:15:32.632000', 'dubbo-xh-mes-service');

-- ----------------------------
-- Table structure for system_role_group_user_relation
-- ----------------------------
DROP TABLE IF EXISTS `system_role_group_user_relation`;
CREATE TABLE `system_role_group_user_relation`  (
  `id` bigint NOT NULL COMMENT '主键ID（雪花算法生成）',
  `role_group_id` bigint NULL DEFAULT NULL COMMENT '角色组ID（援引 sys_role_group.id）',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID（援引 sys_user.id）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_system_role_group_user_relation_role_group_id`(`role_group_id` ASC) USING BTREE,
  INDEX `idx_system_role_group_user_relation_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色组与用户关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of system_role_group_user_relation
-- ----------------------------
INSERT INTO `system_role_group_user_relation` VALUES (740750631991508992, 740491594708287488, 739774367356944384);
INSERT INTO `system_role_group_user_relation` VALUES (740816960463306752, 740124338757304320, 18);
INSERT INTO `system_role_group_user_relation` VALUES (740848877569048576, 740721847200382976, 1);
INSERT INTO `system_role_group_user_relation` VALUES (741449398893936640, 741446807162191872, 18);
INSERT INTO `system_role_group_user_relation` VALUES (741449399086874624, 741446807162191872, 19);
INSERT INTO `system_role_group_user_relation` VALUES (741449399216898048, 741446807162191872, 737192428523487232);
INSERT INTO `system_role_group_user_relation` VALUES (741449399405641728, 741446807162191872, 737192677283463168);
INSERT INTO `system_role_group_user_relation` VALUES (741449399539859456, 741446807162191872, 737192878358396928);
INSERT INTO `system_role_group_user_relation` VALUES (741449399841849344, 741446807162191872, 737193046583541760);
INSERT INTO `system_role_group_user_relation` VALUES (741455298560000000, 741446839999397888, 19);
INSERT INTO `system_role_group_user_relation` VALUES (741455298706800640, 741446839999397888, 737192428523487232);
INSERT INTO `system_role_group_user_relation` VALUES (741455298832629760, 741446839999397888, 737192677283463168);
INSERT INTO `system_role_group_user_relation` VALUES (741455298975236096, 741446839999397888, 737192878358396928);
INSERT INTO `system_role_group_user_relation` VALUES (741455299122036736, 741446839999397888, 737193046583541760);
INSERT INTO `system_role_group_user_relation` VALUES (772697696904413184, 740491594708287488, 1);
INSERT INTO `system_role_group_user_relation` VALUES (778824235874254848, 778824187337768960, 1);

-- ----------------------------
-- Table structure for system_user_department_relation
-- ----------------------------
DROP TABLE IF EXISTS `system_user_department_relation`;
CREATE TABLE `system_user_department_relation`  (
  `id` bigint NOT NULL COMMENT '主键ID（雪花算法生成）',
  `department_id` bigint NULL DEFAULT NULL COMMENT '部门ID（援引 sys_department.id）',
  `user_id` bigint NULL DEFAULT NULL COMMENT '用户ID（援引 sys_user.id）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id_department_id`(`user_id` ASC, `department_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_department_id`(`department_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统用户与部门关系表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of system_user_department_relation
-- ----------------------------
INSERT INTO `system_user_department_relation` VALUES (3, 2, 1);
INSERT INTO `system_user_department_relation` VALUES (5, 4, 6);
INSERT INTO `system_user_department_relation` VALUES (4, 4, 7);
INSERT INTO `system_user_department_relation` VALUES (2, 4, 11);
INSERT INTO `system_user_department_relation` VALUES (738233469200498688, 4, 17);
INSERT INTO `system_user_department_relation` VALUES (3509885629540335616, 6, 17);

-- ----------------------------
-- Table structure for third_party_app
-- ----------------------------
DROP TABLE IF EXISTS `third_party_app`;
CREATE TABLE `third_party_app`  (
  `id` bigint NOT NULL COMMENT '主键ID（雪花算法生成）',
  `app_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '应用标识（第三方应用唯一标识，未传入时由系统以 UUID 生成）',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建者用户ID',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `flag` int NULL DEFAULT NULL COMMENT '数据状态标志：1-启用，-1-禁用，0-删除',
  `grant_type` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '登录授权类型（对应授权提供方的标识，如 password / wecom / third_party）',
  `proxy_id` bigint NULL DEFAULT NULL COMMENT '代理用户ID（关联用户表，作为该操作的执行人账号）',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新者用户ID',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_app_id`(`app_id` ASC) USING BTREE,
  INDEX `idx_flag`(`flag` ASC) USING BTREE,
  INDEX `idx_proxy_id`(`proxy_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '第三方应用接出登记表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of third_party_app
-- ----------------------------
INSERT INTO `third_party_app` VALUES (759566440134082560, 'TEST', 1, '2026-09-28 08:05:51.368000', 1, 'third_party', 739774367356944384, NULL, NULL);

-- ----------------------------
-- Table structure for tree_closure
-- ----------------------------
DROP TABLE IF EXISTS `tree_closure`;
CREATE TABLE `tree_closure`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键（自增）',
  `ancestor_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '祖先节点ID',
  `depth` int NOT NULL COMMENT '层级深度（0 为自身，1 为父子，2 为祖孙，依次类推）',
  `descendant_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '后代节点ID',
  `group_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '树组名称（作为树的唯一标识）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK4q1vrtow52u2ih3utui5nxues`(`ancestor_id` ASC, `descendant_id` ASC) USING BTREE,
  INDEX `IDXlcjh1709srvurk844d5l9jjtu`(`ancestor_id` ASC) USING BTREE,
  INDEX `IDX5spq70g67u0mhko5tuwntwxa3`(`descendant_id` ASC) USING BTREE,
  INDEX `IDXgkkwbnbsyakv2u533604sdto1`(`depth` ASC) USING BTREE,
  INDEX `IDXi7fuflkybyivi3mhmaj5cfynt`(`group_name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 64 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '树闭包关系表（存储任意祖先-后代关系对）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tree_closure
-- ----------------------------
INSERT INTO `tree_closure` VALUES (58, 'COMPANY_TREE::2', 0, 'COMPANY_TREE::2', 'COMPANY_TREE');
INSERT INTO `tree_closure` VALUES (59, 'DEPARTMENT_TREE::2', 0, 'DEPARTMENT_TREE::2', 'DEPARTMENT_TREE');
INSERT INTO `tree_closure` VALUES (60, 'DEPARTMENT_TREE::3511711354564116480', 0, 'DEPARTMENT_TREE::3511711354564116480', 'DEPARTMENT_TREE');
INSERT INTO `tree_closure` VALUES (61, 'DEPARTMENT_TREE::4', 0, 'DEPARTMENT_TREE::4', 'DEPARTMENT_TREE');
INSERT INTO `tree_closure` VALUES (62, 'DEPARTMENT_TREE::2', 1, 'DEPARTMENT_TREE::4', 'DEPARTMENT_TREE');
INSERT INTO `tree_closure` VALUES (63, 'DEPARTMENT_TREE::6', 0, 'DEPARTMENT_TREE::6', 'DEPARTMENT_TREE');

-- ----------------------------
-- Table structure for tree_node
-- ----------------------------
DROP TABLE IF EXISTS `tree_node`;
CREATE TABLE `tree_node`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID（自增）',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `entity_id` bigint NULL DEFAULT NULL COMMENT '节点对应的业务实体ID',
  `group_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '树组名称（作为树的唯一标识，同一树组内节点ID 不重复）',
  `node_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '节点ID（树内唯一标识）',
  `parent_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '父节点ID（关系备份，闭包关系不可靠时可恢复；null 表示根节点）',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UKj31ps1fvxja2xe7166bjvweme`(`group_name` ASC, `node_id` ASC) USING BTREE,
  INDEX `IDXdnk5hk5vr111xcunlqkjfni6h`(`group_name` ASC, `parent_id` ASC) USING BTREE,
  INDEX `IDX5ytbu0cqp6qalcb0fdf9kl31r`(`entity_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 29 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '树节点表（闭包式树结构的节点主档）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tree_node
-- ----------------------------
INSERT INTO `tree_node` VALUES (18, '2026-05-12 14:26:12.767000', 2, 'COMPANY_TREE', 'COMPANY_TREE::2', NULL, NULL);
INSERT INTO `tree_node` VALUES (22, '2026-05-12 14:55:22.323000', 2, 'DEPARTMENT_TREE', 'DEPARTMENT_TREE::2', NULL, NULL);
INSERT INTO `tree_node` VALUES (23, '2026-05-12 14:56:45.706000', 4, 'DEPARTMENT_TREE', 'DEPARTMENT_TREE::4', 'DEPARTMENT_TREE::2', '2026-05-12 14:57:02.332000');
INSERT INTO `tree_node` VALUES (27, '2026-06-15 18:45:22.100000', 6, 'DEPARTMENT_TREE', 'DEPARTMENT_TREE::6', NULL, NULL);
INSERT INTO `tree_node` VALUES (28, '2026-07-13 11:27:11.464000', 3511711354564116480, 'DEPARTMENT_TREE', 'DEPARTMENT_TREE::3511711354564116480', NULL, NULL);

-- ----------------------------
-- Table structure for view_route_configration
-- ----------------------------
DROP TABLE IF EXISTS `view_route_configration`;
CREATE TABLE `view_route_configration`  (
  `id` bigint NOT NULL COMMENT '主键ID（雪花算法生成）',
  `always_show` int NULL DEFAULT NULL COMMENT '是否始终显示在菜单中（0:否，1:是）',
  `component` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '前端组件路径（Vue 组件文件路径）',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建者用户ID',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `flag` int NULL DEFAULT NULL COMMENT '状态标志（0:禁用，1:启用）',
  `hidden` int NULL DEFAULT NULL COMMENT '是否隐藏（0:否，1:是）',
  `name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '路由名称（用于编程式导航）',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父路由ID（用于构建树形路由结构）',
  `path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '路由路径（前端路由 path）',
  `redirect` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '重定向路径',
  `route_group` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '路由分组（如 common、admin、develop）',
  `sort_order` int NULL DEFAULT NULL COMMENT '排序顺序（数值越小越靠前）',
  `update_by` bigint NULL DEFAULT NULL COMMENT '更新者用户ID',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `version` bigint NULL DEFAULT NULL COMMENT '所属版本ID（援引 view_route_version.id）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_route_group`(`route_group` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_flag`(`flag` ASC) USING BTREE,
  INDEX `idx_sort_order`(`sort_order` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '视图路由配置表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of view_route_configration
-- ----------------------------
INSERT INTO `view_route_configration` VALUES (741849291038916608, 0, 'Layout', NULL, NULL, 1, 0, 'view.menu.common', NULL, '/common', '/common/user', 'default', 0, NULL, '2026-08-10 11:29:24.000000', 1);
INSERT INTO `view_route_configration` VALUES (741849291122802688, 0, '/user/index.vue', NULL, NULL, 1, 0, 'view.menu.common.user', 741849291038916608, 'user', NULL, 'default', 0, NULL, '2026-08-10 11:29:25.000000', 1);
INSERT INTO `view_route_configration` VALUES (741849291194105856, 0, 'Layout', NULL, NULL, 1, 0, 'route-manager', NULL, '/route-manager', '', 'default', 1, NULL, '2026-08-10 11:29:26.000000', 1);
INSERT INTO `view_route_configration` VALUES (741849291311546368, 0, '/admin/route-manager/index.vue', NULL, NULL, 1, 0, 'routeManager', 741849291194105856, 'index', '', 'default', 0, NULL, '2026-08-10 11:29:27.000000', 1);
INSERT INTO `view_route_configration` VALUES (741849291508678656, 0, 'Layout', NULL, NULL, 1, 0, 'view.menu.admin.title', NULL, '/admin', '/admin/user', 'default', 2, NULL, '2026-08-10 11:29:29.000000', 1);
INSERT INTO `view_route_configration` VALUES (741849291978440704, 0, '/admin/user/index.vue', NULL, NULL, 1, 0, 'view.menu.admin.user', 741849291508678656, 'user', NULL, 'default', 0, NULL, '2026-08-10 11:29:30.000000', 1);
INSERT INTO `view_route_configration` VALUES (741849292125241344, 0, '/admin/role/index.vue', NULL, NULL, 1, 0, 'view.menu.admin.role', 741849291508678656, 'role', NULL, 'default', 1, NULL, '2026-08-10 11:29:31.000000', 1);
INSERT INTO `view_route_configration` VALUES (741849292204933120, 0, '/admin/company/index.vue', NULL, NULL, 1, 0, 'view.menu.admin.company', 741849291508678656, 'company', NULL, 'default', 2, NULL, '2026-08-10 11:29:31.000000', 1);
INSERT INTO `view_route_configration` VALUES (741849292368510976, 0, '/admin/role_group/index.vue', NULL, NULL, 1, 0, 'view.menu.admin.role.group', 741849291508678656, 'role-group', NULL, 'default', 3, NULL, '2026-08-10 11:29:32.000000', 1);
INSERT INTO `view_route_configration` VALUES (741849292435619840, 0, 'Layout', NULL, NULL, 1, 0, 'view.menu.develop', NULL, '/develop', '/develop/form-manager', 'default', 3, NULL, '2026-08-10 11:29:33.000000', 1);
INSERT INTO `view_route_configration` VALUES (741849292657917952, 0, '/form/FormManager.vue', NULL, NULL, 1, 0, 'view.menu.develop.form.manager', 741849292435619840, 'form-manager', NULL, 'default', 0, NULL, '2026-08-10 11:29:34.000000', 1);
INSERT INTO `view_route_configration` VALUES (741849292741804032, 0, '/form/FormCreator.vue', NULL, NULL, 1, 0, 'view.menu.develop.form.create', 741849292435619840, 'form', NULL, 'default', 1, NULL, '2026-08-10 11:29:35.000000', 1);
INSERT INTO `view_route_configration` VALUES (741849292808912896, 0, '/admin/dic/index.vue', NULL, NULL, 1, 0, 'view.menu.develop.dic', 741849292435619840, 'dic', NULL, 'default', 2, NULL, '2026-08-10 11:29:36.000000', 1);
INSERT INTO `view_route_configration` VALUES (741849293157040128, 0, '/admin/generic-api/index.vue', NULL, NULL, 1, 0, 'view.menu.develop.generic.api', 741849292435619840, 'generic-api', NULL, 'default', 3, NULL, '2026-08-10 11:29:37.000000', 1);
INSERT INTO `view_route_configration` VALUES (741849293219954688, 0, '/admin/systemlog/index.vue', NULL, NULL, 1, 0, 'view.menu.develop.systemlog', 741849292435619840, 'systemlog', NULL, 'default', 4, NULL, '2026-08-10 11:29:38.000000', 1);
INSERT INTO `view_route_configration` VALUES (741849293295452160, 0, '/cache/index.vue', NULL, NULL, 1, 0, 'view.menu.develop.cache', 741849292435619840, 'cache', NULL, 'default', 5, NULL, '2026-08-10 11:29:39.000000', 1);
INSERT INTO `view_route_configration` VALUES (741861855894437888, 0, 'Layout', 1, '2026-08-10 11:34:10.000000', 1, 0, 'route-manager', NULL, '/route-manager', '', 'default', 0, NULL, NULL, 741861855823134720);
INSERT INTO `view_route_configration` VALUES (741861856003489792, 0, '/admin/route-manager/index.vue', 1, '2026-08-10 11:34:10.000000', 1, 0, 'routeManager', 741861855894437888, 'index', '', 'default', 0, NULL, NULL, 741861855823134720);
INSERT INTO `view_route_configration` VALUES (741861856062210048, 0, 'Layout', 1, '2026-08-10 11:34:10.000000', 1, 0, 'view.menu.common', NULL, '/common', '/common/user', 'default', 1, NULL, NULL, 741861855823134720);
INSERT INTO `view_route_configration` VALUES (741861856137707520, 0, '/user/index.vue', 1, '2026-08-10 11:34:10.000000', 1, 0, 'view.menu.common.user', 741861856062210048, 'user', NULL, 'default', 0, NULL, NULL, 741861855823134720);
INSERT INTO `view_route_configration` VALUES (741861856217399296, 0, 'Layout', 1, '2026-08-10 11:34:10.000000', 1, 0, 'view.menu.admin.title', NULL, '/admin', '/admin/user', 'default', 2, NULL, NULL, 741861855823134720);
INSERT INTO `view_route_configration` VALUES (741861856305479680, 0, '/admin/user/index.vue', 1, '2026-08-10 11:34:10.000000', 1, 0, 'view.menu.admin.user', 741861856217399296, 'user', NULL, 'default', 0, NULL, NULL, 741861855823134720);
INSERT INTO `view_route_configration` VALUES (741861856380977152, 0, '/admin/role/index.vue', 1, '2026-08-10 11:34:10.000000', 1, 0, 'view.menu.admin.role', 741861856217399296, 'role', NULL, 'default', 1, NULL, NULL, 741861855823134720);
INSERT INTO `view_route_configration` VALUES (741861856448086016, 0, '/admin/company/index.vue', 1, '2026-08-10 11:34:10.000000', 1, 0, 'view.menu.admin.company', 741861856217399296, 'company', NULL, 'default', 2, NULL, NULL, 741861855823134720);
INSERT INTO `view_route_configration` VALUES (741861856511000576, 0, '/admin/role_group/index.vue', 1, '2026-08-10 11:34:10.000000', 1, 0, 'view.menu.admin.role.group', 741861856217399296, 'role-group', NULL, 'default', 3, NULL, NULL, 741861855823134720);
INSERT INTO `view_route_configration` VALUES (741861856578109440, 0, 'Layout', 1, '2026-08-10 11:34:10.000000', 1, 0, 'view.menu.develop', NULL, '/develop', '/develop/form-manager', 'default', 3, NULL, NULL, 741861855823134720);
INSERT INTO `view_route_configration` VALUES (741861856687161344, 0, '/form/FormManager.vue', 1, '2026-08-10 11:34:10.000000', 1, 0, 'view.menu.develop.form.manager', 741861856578109440, 'form-manager', NULL, 'default', 0, NULL, NULL, 741861855823134720);
INSERT INTO `view_route_configration` VALUES (741861856745881600, 0, '/form/FormCreator.vue', 1, '2026-08-10 11:34:10.000000', 1, 0, 'view.menu.develop.form.create', 741861856578109440, 'form', NULL, 'default', 1, NULL, NULL, 741861855823134720);
INSERT INTO `view_route_configration` VALUES (741861856804601856, 0, '/admin/dic/index.vue', 1, '2026-08-10 11:34:10.000000', 1, 0, 'view.menu.develop.dic', 741861856578109440, 'dic', NULL, 'default', 2, NULL, NULL, 741861855823134720);
INSERT INTO `view_route_configration` VALUES (741861856884293632, 0, '/admin/generic-api/index.vue', 1, '2026-08-10 11:34:10.000000', 1, 0, 'view.menu.develop.generic.api', 741861856578109440, 'generic-api', NULL, 'default', 3, NULL, NULL, 741861855823134720);
INSERT INTO `view_route_configration` VALUES (741861856963985408, 0, '/admin/systemlog/index.vue', 1, '2026-08-10 11:34:10.000000', 1, 0, 'view.menu.develop.systemlog', 741861856578109440, 'systemlog', NULL, 'default', 4, NULL, NULL, 741861855823134720);
INSERT INTO `view_route_configration` VALUES (741861857052065792, 0, '/cache/index.vue', 1, '2026-08-10 11:34:10.000000', 1, 0, 'view.menu.develop.cache', 741861856578109440, 'cache', NULL, 'default', 5, NULL, NULL, 741861855823134720);
INSERT INTO `view_route_configration` VALUES (741861862823428096, 0, 'Layout', 1, '2026-08-10 11:34:11.000000', 1, 0, 'route-manager', NULL, '/route-manager', '', 'default', 0, NULL, NULL, 741861862789873664);
INSERT INTO `view_route_configration` VALUES (741861863381270528, 0, '/admin/route-manager/index.vue', 1, '2026-08-10 11:34:11.000000', 1, 0, 'routeManager', 741861862823428096, 'index', '', 'default', 0, NULL, NULL, 741861862789873664);
INSERT INTO `view_route_configration` VALUES (741861863565819904, 0, 'Layout', 1, '2026-08-10 11:34:11.000000', 1, 0, 'view.menu.admin.title', NULL, '/admin', '/admin/user', 'default', 1, NULL, NULL, 741861862789873664);
INSERT INTO `view_route_configration` VALUES (741861863700037632, 0, '/admin/user/index.vue', 1, '2026-08-10 11:34:11.000000', 1, 0, 'view.menu.admin.user', 741861863565819904, 'user', NULL, 'default', 0, NULL, NULL, 741861862789873664);
INSERT INTO `view_route_configration` VALUES (741861863758757888, 0, '/admin/role/index.vue', 1, '2026-08-10 11:34:11.000000', 1, 0, 'view.menu.admin.role', 741861863565819904, 'role', NULL, 'default', 1, NULL, NULL, 741861862789873664);
INSERT INTO `view_route_configration` VALUES (741861863846838272, 0, '/admin/company/index.vue', 1, '2026-08-10 11:34:11.000000', 1, 0, 'view.menu.admin.company', 741861863565819904, 'company', NULL, 'default', 2, NULL, NULL, 741861862789873664);
INSERT INTO `view_route_configration` VALUES (741861863943307264, 0, '/admin/role_group/index.vue', 1, '2026-08-10 11:34:11.000000', 1, 0, 'view.menu.admin.role.group', 741861863565819904, 'role-group', NULL, 'default', 3, NULL, NULL, 741861862789873664);
INSERT INTO `view_route_configration` VALUES (741861864022999040, 0, 'Layout', 1, '2026-08-10 11:34:12.000000', 1, 0, 'view.menu.common', NULL, '/common', '/common/user', 'default', 2, NULL, NULL, 741861862789873664);
INSERT INTO `view_route_configration` VALUES (741861864081719296, 0, '/user/index.vue', 1, '2026-08-10 11:34:12.000000', 1, 0, 'view.menu.common.user', 741861864022999040, 'user', NULL, 'default', 0, NULL, NULL, 741861862789873664);
INSERT INTO `view_route_configration` VALUES (741861864169799680, 0, 'Layout', 1, '2026-08-10 11:34:12.000000', 1, 0, 'view.menu.develop', NULL, '/develop', '/develop/form-manager', 'default', 3, NULL, NULL, 741861862789873664);
INSERT INTO `view_route_configration` VALUES (741861864270462976, 0, '/form/FormManager.vue', 1, '2026-08-10 11:34:12.000000', 1, 0, 'view.menu.develop.form.manager', 741861864169799680, 'form-manager', NULL, 'default', 0, NULL, NULL, 741861862789873664);
INSERT INTO `view_route_configration` VALUES (741861864358543360, 0, '/form/FormCreator.vue', 1, '2026-08-10 11:34:12.000000', 1, 0, 'view.menu.develop.form.create', 741861864169799680, 'form', NULL, 'default', 1, NULL, NULL, 741861862789873664);
INSERT INTO `view_route_configration` VALUES (741861864404680704, 0, '/admin/dic/index.vue', 1, '2026-08-10 11:34:12.000000', 1, 0, 'view.menu.develop.dic', 741861864169799680, 'dic', NULL, 'default', 2, NULL, NULL, 741861862789873664);
INSERT INTO `view_route_configration` VALUES (741861864480178176, 0, '/admin/generic-api/index.vue', 1, '2026-08-10 11:34:12.000000', 1, 0, 'view.menu.develop.generic.api', 741861864169799680, 'generic-api', NULL, 'default', 3, NULL, NULL, 741861862789873664);
INSERT INTO `view_route_configration` VALUES (741861864564064256, 0, '/admin/systemlog/index.vue', 1, '2026-08-10 11:34:12.000000', 1, 0, 'view.menu.develop.systemlog', 741861864169799680, 'systemlog', NULL, 'default', 4, NULL, NULL, 741861862789873664);
INSERT INTO `view_route_configration` VALUES (741861864614395904, 0, '/cache/index.vue', 1, '2026-08-10 11:34:12.000000', 1, 0, 'view.menu.develop.cache', 741861864169799680, 'cache', NULL, 'default', 5, NULL, NULL, 741861862789873664);
INSERT INTO `view_route_configration` VALUES (741861866757685248, 0, 'Layout', 1, '2026-08-10 11:34:12.000000', 1, 0, 'route-manager', NULL, '/route-manager', '', 'default', 0, NULL, NULL, 741861866719936512);
INSERT INTO `view_route_configration` VALUES (741861866858348544, 0, '/admin/route-manager/index.vue', 1, '2026-08-10 11:34:12.000000', 1, 0, 'routeManager', 741861866757685248, 'index', '', 'default', 0, NULL, NULL, 741861866719936512);
INSERT INTO `view_route_configration` VALUES (741861866946428928, 0, 'Layout', 1, '2026-08-10 11:34:12.000000', 1, 0, 'view.menu.admin.title', NULL, '/admin', '/admin/user', 'default', 1, NULL, NULL, 741861866719936512);
INSERT INTO `view_route_configration` VALUES (741861867026120704, 0, '/admin/user/index.vue', 1, '2026-08-10 11:34:12.000000', 1, 0, 'view.menu.admin.user', 741861866946428928, 'user', NULL, 'default', 0, NULL, NULL, 741861866719936512);
INSERT INTO `view_route_configration` VALUES (741861867076452352, 0, '/admin/role/index.vue', 1, '2026-08-10 11:34:12.000000', 1, 0, 'view.menu.admin.role', 741861866946428928, 'role', NULL, 'default', 1, NULL, NULL, 741861866719936512);
INSERT INTO `view_route_configration` VALUES (741861867143561216, 0, '/admin/company/index.vue', 1, '2026-08-10 11:34:12.000000', 1, 0, 'view.menu.admin.company', 741861866946428928, 'company', NULL, 'default', 2, NULL, NULL, 741861866719936512);
INSERT INTO `view_route_configration` VALUES (741861867214864384, 0, '/admin/role_group/index.vue', 1, '2026-08-10 11:34:12.000000', 1, 0, 'view.menu.admin.role.group', 741861866946428928, 'role-group', NULL, 'default', 3, NULL, NULL, 741861866719936512);
INSERT INTO `view_route_configration` VALUES (741861867281973248, 0, 'Layout', 1, '2026-08-10 11:34:12.000000', 1, 0, 'view.menu.develop', NULL, '/develop', '/develop/form-manager', 'default', 2, NULL, NULL, 741861866719936512);
INSERT INTO `view_route_configration` VALUES (741861867386830848, 0, '/form/FormManager.vue', 1, '2026-08-10 11:34:12.000000', 1, 0, 'view.menu.develop.form.manager', 741861867281973248, 'form-manager', NULL, 'default', 0, NULL, NULL, 741861866719936512);
INSERT INTO `view_route_configration` VALUES (741861867453939712, 0, '/form/FormCreator.vue', 1, '2026-08-10 11:34:12.000000', 1, 0, 'view.menu.develop.form.create', 741861867281973248, 'form', NULL, 'default', 1, NULL, NULL, 741861866719936512);
INSERT INTO `view_route_configration` VALUES (741861867516854272, 0, '/admin/dic/index.vue', 1, '2026-08-10 11:34:12.000000', 1, 0, 'view.menu.develop.dic', 741861867281973248, 'dic', NULL, 'default', 2, NULL, NULL, 741861866719936512);
INSERT INTO `view_route_configration` VALUES (741861867592351744, 0, '/admin/generic-api/index.vue', 1, '2026-08-10 11:34:12.000000', 1, 0, 'view.menu.develop.generic.api', 741861867281973248, 'generic-api', NULL, 'default', 3, NULL, NULL, 741861866719936512);
INSERT INTO `view_route_configration` VALUES (741861867663654912, 0, '/admin/systemlog/index.vue', 1, '2026-08-10 11:34:12.000000', 1, 0, 'view.menu.develop.systemlog', 741861867281973248, 'systemlog', NULL, 'default', 4, NULL, NULL, 741861866719936512);
INSERT INTO `view_route_configration` VALUES (741861867730763776, 0, '/cache/index.vue', 1, '2026-08-10 11:34:12.000000', 1, 0, 'view.menu.develop.cache', 741861867281973248, 'cache', NULL, 'default', 5, NULL, NULL, 741861866719936512);
INSERT INTO `view_route_configration` VALUES (741861867823038464, 0, 'Layout', 1, '2026-08-10 11:34:12.000000', 1, 0, 'view.menu.common', NULL, '/common', '/common/user', 'default', 3, NULL, NULL, 741861866719936512);
INSERT INTO `view_route_configration` VALUES (741861867898535936, 0, '/user/index.vue', 1, '2026-08-10 11:34:12.000000', 1, 0, 'view.menu.common.user', 741861867823038464, 'user', NULL, 'default', 0, NULL, NULL, 741861866719936512);
INSERT INTO `view_route_configration` VALUES (741861879873273856, 0, 'Layout', 1, '2026-08-10 11:34:15.000000', 1, 0, 'view.menu.admin.title', NULL, '/admin', '/admin/user', 'default', 0, NULL, NULL, 741861879848108032);
INSERT INTO `view_route_configration` VALUES (741861879957159936, 0, '/admin/user/index.vue', 1, '2026-08-10 11:34:15.000000', 1, 0, 'view.menu.admin.user', 741861879873273856, 'user', NULL, 'default', 0, NULL, NULL, 741861879848108032);
INSERT INTO `view_route_configration` VALUES (741861880024268800, 0, '/admin/role/index.vue', 1, '2026-08-10 11:34:15.000000', 1, 0, 'view.menu.admin.role', 741861879873273856, 'role', NULL, 'default', 1, NULL, NULL, 741861879848108032);
INSERT INTO `view_route_configration` VALUES (741861880091377664, 0, '/admin/company/index.vue', 1, '2026-08-10 11:34:15.000000', 1, 0, 'view.menu.admin.company', 741861879873273856, 'company', NULL, 'default', 2, NULL, NULL, 741861879848108032);
INSERT INTO `view_route_configration` VALUES (741861880154292224, 0, '/admin/role_group/index.vue', 1, '2026-08-10 11:34:15.000000', 1, 0, 'view.menu.admin.role.group', 741861879873273856, 'role-group', NULL, 'default', 3, NULL, NULL, 741861879848108032);
INSERT INTO `view_route_configration` VALUES (741861880271732736, 0, 'Layout', 1, '2026-08-10 11:34:15.000000', 1, 0, 'route-manager', NULL, '/route-manager', '', 'default', 1, NULL, NULL, 741861879848108032);
INSERT INTO `view_route_configration` VALUES (741861880368201728, 0, '/admin/route-manager/index.vue', 1, '2026-08-10 11:34:15.000000', 1, 0, 'routeManager', 741861880271732736, 'index', '', 'default', 0, NULL, NULL, 741861879848108032);
INSERT INTO `view_route_configration` VALUES (741861880435310592, 0, 'Layout', 1, '2026-08-10 11:34:15.000000', 1, 0, 'view.menu.develop', NULL, '/develop', '/develop/form-manager', 'default', 2, NULL, NULL, 741861879848108032);
INSERT INTO `view_route_configration` VALUES (741861880556945408, 0, '/form/FormManager.vue', 1, '2026-08-10 11:34:15.000000', 1, 0, 'view.menu.develop.form.manager', 741861880435310592, 'form-manager', NULL, 'default', 0, NULL, NULL, 741861879848108032);
INSERT INTO `view_route_configration` VALUES (741861880745689088, 0, '/form/FormCreator.vue', 1, '2026-08-10 11:34:15.000000', 1, 0, 'view.menu.develop.form.create', 741861880435310592, 'form', NULL, 'default', 1, NULL, NULL, 741861879848108032);
INSERT INTO `view_route_configration` VALUES (741861880812797952, 0, '/admin/dic/index.vue', 1, '2026-08-10 11:34:16.000000', 1, 0, 'view.menu.develop.dic', 741861880435310592, 'dic', NULL, 'default', 2, NULL, NULL, 741861879848108032);
INSERT INTO `view_route_configration` VALUES (741861880913461248, 0, '/admin/generic-api/index.vue', 1, '2026-08-10 11:34:16.000000', 1, 0, 'view.menu.develop.generic.api', 741861880435310592, 'generic-api', NULL, 'default', 3, NULL, NULL, 741861879848108032);
INSERT INTO `view_route_configration` VALUES (741861880980570112, 0, '/admin/systemlog/index.vue', 1, '2026-08-10 11:34:16.000000', 1, 0, 'view.menu.develop.systemlog', 741861880435310592, 'systemlog', NULL, 'default', 4, NULL, NULL, 741861879848108032);
INSERT INTO `view_route_configration` VALUES (741861881060261888, 0, '/cache/index.vue', 1, '2026-08-10 11:34:16.000000', 1, 0, 'view.menu.develop.cache', 741861880435310592, 'cache', NULL, 'default', 5, NULL, NULL, 741861879848108032);
INSERT INTO `view_route_configration` VALUES (741861881173508096, 0, 'Layout', 1, '2026-08-10 11:34:16.000000', 1, 0, 'view.menu.common', NULL, '/common', '/common/user', 'default', 3, NULL, NULL, 741861879848108032);
INSERT INTO `view_route_configration` VALUES (741861881228034048, 0, '/user/index.vue', 1, '2026-08-10 11:34:16.000000', 1, 0, 'view.menu.common.user', 741861881173508096, 'user', NULL, 'default', 0, NULL, NULL, 741861879848108032);
INSERT INTO `view_route_configration` VALUES (741861925779931136, 0, 'Layout', 1, '2026-08-10 11:34:26.000000', 1, 0, 'view.menu.admin.title', NULL, '/admin', '/admin/user', 'default', 0, NULL, NULL, 741861925725405184);
INSERT INTO `view_route_configration` VALUES (741861925926731776, 0, '/admin/user/index.vue', 1, '2026-08-10 11:34:26.000000', 1, 0, 'view.menu.admin.user', 741861925779931136, 'user', NULL, 'default', 0, NULL, NULL, 741861925725405184);
INSERT INTO `view_route_configration` VALUES (741861926027395072, 0, '/admin/role/index.vue', 1, '2026-08-10 11:34:26.000000', 1, 0, 'view.menu.admin.role', 741861925779931136, 'role', NULL, 'default', 1, NULL, NULL, 741861925725405184);
INSERT INTO `view_route_configration` VALUES (741861926094503936, 0, '/admin/company/index.vue', 1, '2026-08-10 11:34:26.000000', 1, 0, 'view.menu.admin.company', 741861925779931136, 'company', NULL, 'default', 2, NULL, NULL, 741861925725405184);
INSERT INTO `view_route_configration` VALUES (741861926178390016, 0, '/admin/role_group/index.vue', 1, '2026-08-10 11:34:26.000000', 1, 0, 'view.menu.admin.role.group', 741861925779931136, 'role-group', NULL, 'default', 3, NULL, NULL, 741861925725405184);
INSERT INTO `view_route_configration` VALUES (741861926253887488, 0, 'Layout', 1, '2026-08-10 11:34:26.000000', 1, 0, 'route-manager', NULL, '/route-manager', '', 'default', 1, NULL, NULL, 741861925725405184);
INSERT INTO `view_route_configration` VALUES (741861926333579264, 0, '/admin/route-manager/index.vue', 1, '2026-08-10 11:34:26.000000', 1, 0, 'routeManager', 741861926253887488, 'index', '', 'default', 0, NULL, NULL, 741861925725405184);
INSERT INTO `view_route_configration` VALUES (741861926404882432, 0, 'Layout', 1, '2026-08-10 11:34:26.000000', 1, 0, 'view.menu.develop', NULL, '/develop', '/develop/form-manager', 'default', 2, NULL, NULL, 741861925725405184);
INSERT INTO `view_route_configration` VALUES (741861926505545728, 0, '/form/FormManager.vue', 1, '2026-08-10 11:34:26.000000', 1, 0, 'view.menu.develop.form.manager', 741861926404882432, 'form-manager', NULL, 'default', 0, NULL, NULL, 741861925725405184);
INSERT INTO `view_route_configration` VALUES (741861926564265984, 0, '/form/FormCreator.vue', 1, '2026-08-10 11:34:26.000000', 1, 0, 'view.menu.develop.form.create', 741861926404882432, 'form', NULL, 'default', 1, NULL, NULL, 741861925725405184);
INSERT INTO `view_route_configration` VALUES (741861926643957760, 0, '/admin/dic/index.vue', 1, '2026-08-10 11:34:26.000000', 1, 0, 'view.menu.develop.dic', 741861926404882432, 'dic', NULL, 'default', 2, NULL, NULL, 741861925725405184);
INSERT INTO `view_route_configration` VALUES (741861926786564096, 0, '/admin/generic-api/index.vue', 1, '2026-08-10 11:34:26.000000', 1, 0, 'view.menu.develop.generic.api', 741861926404882432, 'generic-api', NULL, 'default', 3, NULL, NULL, 741861925725405184);
INSERT INTO `view_route_configration` VALUES (741861926849478656, 0, '/admin/systemlog/index.vue', 1, '2026-08-10 11:34:26.000000', 1, 0, 'view.menu.develop.systemlog', 741861926404882432, 'systemlog', NULL, 'default', 4, NULL, NULL, 741861925725405184);
INSERT INTO `view_route_configration` VALUES (741861926983696384, 0, '/cache/index.vue', 1, '2026-08-10 11:34:27.000000', 1, 0, 'view.menu.develop.cache', 741861926404882432, 'cache', NULL, 'default', 5, NULL, NULL, 741861925725405184);
INSERT INTO `view_route_configration` VALUES (741861927059193856, 0, 'Layout', 1, '2026-08-10 11:34:27.000000', 1, 0, 'view.menu.common', NULL, '/common', '/common/user', 'default', 3, NULL, NULL, 741861925725405184);
INSERT INTO `view_route_configration` VALUES (741861927130497024, 0, '/user/index.vue', 1, '2026-08-10 11:34:27.000000', 1, 0, 'view.menu.common.user', 741861927059193856, 'user', NULL, 'default', 0, NULL, NULL, 741861925725405184);
INSERT INTO `view_route_configration` VALUES (741862940847636480, 0, 'Layout', 1, '2026-08-10 11:38:28.000000', 1, 0, 'view.menu.admin.title', NULL, '/admin', '/admin/user', 'default', 0, NULL, NULL, 741862940793110528);
INSERT INTO `view_route_configration` VALUES (741862940986048512, 0, '/admin/user/index.vue', 1, '2026-08-10 11:38:28.000000', 1, 0, 'view.menu.admin.user', 741862940847636480, 'user', NULL, 'default', 0, NULL, NULL, 741862940793110528);
INSERT INTO `view_route_configration` VALUES (741862941048963072, 0, '/admin/role/index.vue', 1, '2026-08-10 11:38:28.000000', 1, 0, 'view.menu.admin.role', 741862940847636480, 'role', NULL, 'default', 1, NULL, NULL, 741862940793110528);
INSERT INTO `view_route_configration` VALUES (741862941120266240, 0, '/admin/company/index.vue', 1, '2026-08-10 11:38:28.000000', 1, 0, 'view.menu.admin.company', 741862940847636480, 'company', NULL, 'default', 2, NULL, NULL, 741862940793110528);
INSERT INTO `view_route_configration` VALUES (741862941178986496, 0, '/admin/role_group/index.vue', 1, '2026-08-10 11:38:28.000000', 1, 0, 'view.menu.admin.role.group', 741862940847636480, 'role-group', NULL, 'default', 3, NULL, NULL, 741862940793110528);
INSERT INTO `view_route_configration` VALUES (741862941233512448, 0, 'Layout', 1, '2026-08-10 11:38:28.000000', 1, 0, 'route-manager', NULL, '/route-manager', '', 'default', 1, NULL, NULL, 741862940793110528);
INSERT INTO `view_route_configration` VALUES (741862941380313088, 0, '/admin/route-manager/index.vue', 1, '2026-08-10 11:38:28.000000', 1, 0, 'routeManager', 741862941233512448, 'index', '', 'default', 0, NULL, NULL, 741862940793110528);
INSERT INTO `view_route_configration` VALUES (741862941434839040, 0, 'Layout', 1, '2026-08-10 11:38:28.000000', 1, 0, 'view.menu.develop', NULL, '/develop', '/develop/form-manager', 'default', 2, NULL, NULL, 741862940793110528);
INSERT INTO `view_route_configration` VALUES (741862941514530816, 0, '/form/FormManager.vue', 1, '2026-08-10 11:38:28.000000', 1, 0, 'view.menu.develop.form.manager', 741862941434839040, 'form-manager', NULL, 'default', 0, NULL, NULL, 741862940793110528);
INSERT INTO `view_route_configration` VALUES (741862941594222592, 0, '/form/FormCreator.vue', 1, '2026-08-10 11:38:28.000000', 1, 0, 'view.menu.develop.form.create', 741862941434839040, 'form', NULL, 'default', 1, NULL, NULL, 741862940793110528);
INSERT INTO `view_route_configration` VALUES (741862941657137152, 0, '/admin/dic/index.vue', 1, '2026-08-10 11:38:28.000000', 1, 0, 'view.menu.develop.dic', 741862941434839040, 'dic', NULL, 'default', 2, NULL, NULL, 741862940793110528);
INSERT INTO `view_route_configration` VALUES (741862941724246016, 0, '/admin/generic-api/index.vue', 1, '2026-08-10 11:38:28.000000', 1, 0, 'view.menu.develop.generic.api', 741862941434839040, 'generic-api', NULL, 'default', 3, NULL, NULL, 741862940793110528);
INSERT INTO `view_route_configration` VALUES (741862941837492224, 0, '/admin/systemlog/index.vue', 1, '2026-08-10 11:38:28.000000', 1, 0, 'view.menu.develop.systemlog', 741862941434839040, 'systemlog', NULL, 'default', 4, NULL, NULL, 741862940793110528);
INSERT INTO `view_route_configration` VALUES (741862941908795392, 0, '/cache/index.vue', 1, '2026-08-10 11:38:28.000000', 1, 0, 'view.menu.develop.cache', 741862941434839040, 'cache', NULL, 'default', 5, NULL, NULL, 741862940793110528);
INSERT INTO `view_route_configration` VALUES (741862941967515648, 0, 'Layout', 1, '2026-08-10 11:38:29.000000', 1, 0, 'view.menu.common', NULL, '/common', '/common/user', 'default', 3, NULL, NULL, 741862940793110528);
INSERT INTO `view_route_configration` VALUES (741862942160453632, 0, '/user/index.vue', 1, '2026-08-10 11:38:29.000000', 1, 0, 'view.menu.common.user', 741862941967515648, 'user', NULL, 'default', 0, NULL, NULL, 741862940793110528);
INSERT INTO `view_route_configration` VALUES (741902418752569344, 0, '/admin/scan-config/index.vue', 1, '2026-08-10 14:15:21.000000', 1, 0, '单据改动扫描配置', NULL, 'doc-scan-config', '', 'default', 0, NULL, NULL, 741902418702237696);
INSERT INTO `view_route_configration` VALUES (741902418853232640, 0, 'Layout', 1, '2026-08-10 14:15:21.000000', 1, 0, 'view.menu.admin.title', NULL, '/admin', '/admin/user', 'default', 1, NULL, NULL, 741902418702237696);
INSERT INTO `view_route_configration` VALUES (741902418991644672, 0, '/admin/user/index.vue', 1, '2026-08-10 14:15:21.000000', 1, 0, 'view.menu.admin.user', 741902418853232640, 'user', NULL, 'default', 0, NULL, NULL, 741902418702237696);
INSERT INTO `view_route_configration` VALUES (741902419226525696, 0, '/admin/role/index.vue', 1, '2026-08-10 14:15:21.000000', 1, 0, 'view.menu.admin.role', 741902418853232640, 'role', NULL, 'default', 1, NULL, NULL, 741902418702237696);
INSERT INTO `view_route_configration` VALUES (741902419356549120, 0, '/admin/company/index.vue', 1, '2026-08-10 14:15:21.000000', 1, 0, 'view.menu.admin.company', 741902418853232640, 'company', NULL, 'default', 2, NULL, NULL, 741902418702237696);
INSERT INTO `view_route_configration` VALUES (741902419419463680, 0, '/admin/role_group/index.vue', 1, '2026-08-10 14:15:21.000000', 1, 0, 'view.menu.admin.role.group', 741902418853232640, 'role-group', NULL, 'default', 3, NULL, NULL, 741902418702237696);
INSERT INTO `view_route_configration` VALUES (741902419494961152, 0, 'Layout', 1, '2026-08-10 14:15:21.000000', 1, 0, 'route-manager', NULL, '/route-manager', '', 'default', 2, NULL, NULL, 741902418702237696);
INSERT INTO `view_route_configration` VALUES (741902419704676352, 0, '/admin/route-manager/index.vue', 1, '2026-08-10 14:15:21.000000', 1, 0, 'routeManager', 741902419494961152, 'index', '', 'default', 0, NULL, NULL, 741902418702237696);
INSERT INTO `view_route_configration` VALUES (741902419859865600, 0, 'Layout', 1, '2026-08-10 14:15:21.000000', 1, 0, 'view.menu.develop', NULL, '/develop', '/develop/form-manager', 'default', 3, NULL, NULL, 741902418702237696);
INSERT INTO `view_route_configration` VALUES (741902420199604224, 0, '/form/FormManager.vue', 1, '2026-08-10 14:15:21.000000', 1, 0, 'view.menu.develop.form.manager', 741902419859865600, 'form-manager', NULL, 'default', 0, NULL, NULL, 741902418702237696);
INSERT INTO `view_route_configration` VALUES (741902420258324480, 0, '/form/FormCreator.vue', 1, '2026-08-10 14:15:21.000000', 1, 0, 'view.menu.develop.form.create', 741902419859865600, 'form', NULL, 'default', 1, NULL, NULL, 741902418702237696);
INSERT INTO `view_route_configration` VALUES (741902420396736512, 0, '/admin/dic/index.vue', 1, '2026-08-10 14:15:21.000000', 1, 0, 'view.menu.develop.dic', 741902419859865600, 'dic', NULL, 'default', 2, NULL, NULL, 741902418702237696);
INSERT INTO `view_route_configration` VALUES (741902420472233984, 0, '/admin/generic-api/index.vue', 1, '2026-08-10 14:15:21.000000', 1, 0, 'view.menu.develop.generic.api', 741902419859865600, 'generic-api', NULL, 'default', 3, NULL, NULL, 741902418702237696);
INSERT INTO `view_route_configration` VALUES (741902420660977664, 0, '/admin/systemlog/index.vue', 1, '2026-08-10 14:15:21.000000', 1, 0, 'view.menu.develop.systemlog', 741902419859865600, 'systemlog', NULL, 'default', 4, NULL, NULL, 741902418702237696);
INSERT INTO `view_route_configration` VALUES (741902420736475136, 0, '/cache/index.vue', 1, '2026-08-10 14:15:21.000000', 1, 0, 'view.menu.develop.cache', 741902419859865600, 'cache', NULL, 'default', 5, NULL, NULL, 741902418702237696);
INSERT INTO `view_route_configration` VALUES (741902420912635904, 0, 'Layout', 1, '2026-08-10 14:15:21.000000', 1, 0, 'view.menu.common', NULL, '/common', '/common/user', 'default', 4, NULL, NULL, 741902418702237696);
INSERT INTO `view_route_configration` VALUES (741902421067825152, 0, '/user/index.vue', 1, '2026-08-10 14:15:21.000000', 1, 0, 'view.menu.common.user', 741902420912635904, 'user', NULL, 'default', 0, NULL, NULL, 741902418702237696);
INSERT INTO `view_route_configration` VALUES (741902674860965888, 0, '/admin/scan-config/index.vue', 1, '2026-08-10 14:16:22.000000', 1, 0, 'doc-scan-config', NULL, 'doc-scan-config', '', 'default', 0, NULL, NULL, 741902674630279168);
INSERT INTO `view_route_configration` VALUES (741902674974212096, 0, 'Layout', 1, '2026-08-10 14:16:22.000000', 1, 0, 'view.menu.admin.title', NULL, '/admin', '/admin/user', 'default', 1, NULL, NULL, 741902674630279168);
INSERT INTO `view_route_configration` VALUES (741902675204898816, 0, '/admin/user/index.vue', 1, '2026-08-10 14:16:22.000000', 1, 0, 'view.menu.admin.user', 741902674974212096, 'user', NULL, 'default', 0, NULL, NULL, 741902674630279168);
INSERT INTO `view_route_configration` VALUES (741902675490111488, 0, '/admin/role/index.vue', 1, '2026-08-10 14:16:22.000000', 1, 0, 'view.menu.admin.role', 741902674974212096, 'role', NULL, 'default', 1, NULL, NULL, 741902674630279168);
INSERT INTO `view_route_configration` VALUES (741902675821461504, 0, '/admin/company/index.vue', 1, '2026-08-10 14:16:22.000000', 1, 0, 'view.menu.admin.company', 741902674974212096, 'company', NULL, 'default', 2, NULL, NULL, 741902674630279168);
INSERT INTO `view_route_configration` VALUES (741902676173783040, 0, '/admin/role_group/index.vue', 1, '2026-08-10 14:16:22.000000', 1, 0, 'view.menu.admin.role.group', 741902674974212096, 'role-group', NULL, 'default', 3, NULL, NULL, 741902674630279168);
INSERT INTO `view_route_configration` VALUES (741902676421246976, 0, 'Layout', 1, '2026-08-10 14:16:22.000000', 1, 0, 'route-manager', NULL, '/route-manager', '', 'default', 2, NULL, NULL, 741902674630279168);
INSERT INTO `view_route_configration` VALUES (741902676672905216, 0, '/admin/route-manager/index.vue', 1, '2026-08-10 14:16:22.000000', 1, 0, 'routeManager', 741902676421246976, 'index', '', 'default', 0, NULL, NULL, 741902674630279168);
INSERT INTO `view_route_configration` VALUES (741902676983283712, 0, 'Layout', 1, '2026-08-10 14:16:22.000000', 1, 0, 'view.menu.develop', NULL, '/develop', '/develop/form-manager', 'default', 3, NULL, NULL, 741902674630279168);
INSERT INTO `view_route_configration` VALUES (741902677134278656, 0, '/form/FormManager.vue', 1, '2026-08-10 14:16:22.000000', 1, 0, 'view.menu.develop.form.manager', 741902676983283712, 'form-manager', NULL, 'default', 0, NULL, NULL, 741902674630279168);
INSERT INTO `view_route_configration` VALUES (741902677264302080, 0, '/form/FormCreator.vue', 1, '2026-08-10 14:16:22.000000', 1, 0, 'view.menu.develop.form.create', 741902676983283712, 'form', NULL, 'default', 1, NULL, NULL, 741902674630279168);
INSERT INTO `view_route_configration` VALUES (741902677474017280, 0, '/admin/dic/index.vue', 1, '2026-08-10 14:16:22.000000', 1, 0, 'view.menu.develop.dic', 741902676983283712, 'dic', NULL, 'default', 2, NULL, NULL, 741902674630279168);
INSERT INTO `view_route_configration` VALUES (741902677687926784, 0, '/admin/generic-api/index.vue', 1, '2026-08-10 14:16:22.000000', 1, 0, 'view.menu.develop.generic.api', 741902676983283712, 'generic-api', NULL, 'default', 3, NULL, NULL, 741902674630279168);
INSERT INTO `view_route_configration` VALUES (741902677855698944, 0, '/admin/systemlog/index.vue', 1, '2026-08-10 14:16:22.000000', 1, 0, 'view.menu.develop.systemlog', 741902676983283712, 'systemlog', NULL, 'default', 4, NULL, NULL, 741902674630279168);
INSERT INTO `view_route_configration` VALUES (741902678111551488, 0, '/cache/index.vue', 1, '2026-08-10 14:16:22.000000', 1, 0, 'view.menu.develop.cache', 741902676983283712, 'cache', NULL, 'default', 5, NULL, NULL, 741902674630279168);
INSERT INTO `view_route_configration` VALUES (741902678296100864, 0, 'Layout', 1, '2026-08-10 14:16:22.000000', 1, 0, 'view.menu.common', NULL, '/common', '/common/user', 'default', 4, NULL, NULL, 741902674630279168);
INSERT INTO `view_route_configration` VALUES (741902678598090752, 0, '/user/index.vue', 1, '2026-08-10 14:16:22.000000', 1, 0, 'view.menu.common.user', 741902678296100864, 'user', NULL, 'default', 0, NULL, NULL, 741902674630279168);
INSERT INTO `view_route_configration` VALUES (741903113165733888, 0, 'Layout', 1, '2026-08-10 14:18:06.000000', 1, 0, 'xh', NULL, '/xh', '', 'default', 0, NULL, NULL, 741903113123790848);
INSERT INTO `view_route_configration` VALUES (741903113253814272, 0, '/admin/scan-config/index.vue', 1, '2026-08-10 14:18:06.000000', 1, 0, 'xh-doc-scan', 741903113165733888, '/xh/index', '', 'default', 0, NULL, NULL, 741903113123790848);
INSERT INTO `view_route_configration` VALUES (741903113316728832, 0, 'Layout', 1, '2026-08-10 14:18:06.000000', 1, 0, 'view.menu.admin.title', NULL, '/admin', '/admin/user', 'default', 1, NULL, NULL, 741903113123790848);
INSERT INTO `view_route_configration` VALUES (741903113425780736, 0, '/admin/user/index.vue', 1, '2026-08-10 14:18:06.000000', 1, 0, 'view.menu.admin.user', 741903113316728832, 'user', NULL, 'default', 0, NULL, NULL, 741903113123790848);
INSERT INTO `view_route_configration` VALUES (741903113501278208, 0, '/admin/role/index.vue', 1, '2026-08-10 14:18:06.000000', 1, 0, 'view.menu.admin.role', 741903113316728832, 'role', NULL, 'default', 1, NULL, NULL, 741903113123790848);
INSERT INTO `view_route_configration` VALUES (741903113585164288, 0, '/admin/company/index.vue', 1, '2026-08-10 14:18:06.000000', 1, 0, 'view.menu.admin.company', 741903113316728832, 'company', NULL, 'default', 2, NULL, NULL, 741903113123790848);
INSERT INTO `view_route_configration` VALUES (741903113673244672, 0, '/admin/role_group/index.vue', 1, '2026-08-10 14:18:06.000000', 1, 0, 'view.menu.admin.role.group', 741903113316728832, 'role-group', NULL, 'default', 3, NULL, NULL, 741903113123790848);
INSERT INTO `view_route_configration` VALUES (741903113811656704, 0, 'Layout', 1, '2026-08-10 14:18:06.000000', 1, 0, 'route-manager', NULL, '/route-manager', '', 'default', 2, NULL, NULL, 741903113123790848);
INSERT INTO `view_route_configration` VALUES (741903114033954816, 0, '/admin/route-manager/index.vue', 1, '2026-08-10 14:18:06.000000', 1, 0, 'routeManager', 741903113811656704, 'index', '', 'default', 0, NULL, NULL, 741903113123790848);
INSERT INTO `view_route_configration` VALUES (741903114126229504, 0, 'Layout', 1, '2026-08-10 14:18:06.000000', 1, 0, 'view.menu.develop', NULL, '/develop', '/develop/form-manager', 'default', 3, NULL, NULL, 741903113123790848);
INSERT INTO `view_route_configration` VALUES (741903114549854208, 0, '/form/FormManager.vue', 1, '2026-08-10 14:18:06.000000', 1, 0, 'view.menu.develop.form.manager', 741903114126229504, 'form-manager', NULL, 'default', 0, NULL, NULL, 741903113123790848);
INSERT INTO `view_route_configration` VALUES (741903114742792192, 0, '/form/FormCreator.vue', 1, '2026-08-10 14:18:06.000000', 1, 0, 'view.menu.develop.form.create', 741903114126229504, 'form', NULL, 'default', 1, NULL, NULL, 741903113123790848);
INSERT INTO `view_route_configration` VALUES (741903114814095360, 0, '/admin/dic/index.vue', 1, '2026-08-10 14:18:06.000000', 1, 0, 'view.menu.develop.dic', 741903114126229504, 'dic', NULL, 'default', 2, NULL, NULL, 741903113123790848);
INSERT INTO `view_route_configration` VALUES (741903114986061824, 0, '/admin/generic-api/index.vue', 1, '2026-08-10 14:18:07.000000', 1, 0, 'view.menu.develop.generic.api', 741903114126229504, 'generic-api', NULL, 'default', 3, NULL, NULL, 741903113123790848);
INSERT INTO `view_route_configration` VALUES (741903115048976384, 0, '/admin/systemlog/index.vue', 1, '2026-08-10 14:18:07.000000', 1, 0, 'view.menu.develop.systemlog', 741903114126229504, 'systemlog', NULL, 'default', 4, NULL, NULL, 741903113123790848);
INSERT INTO `view_route_configration` VALUES (741903116688949248, 0, '/cache/index.vue', 1, '2026-08-10 14:18:07.000000', 1, 0, 'view.menu.develop.cache', 741903114126229504, 'cache', NULL, 'default', 5, NULL, NULL, 741903113123790848);
INSERT INTO `view_route_configration` VALUES (741903117741719552, 0, 'Layout', 1, '2026-08-10 14:18:07.000000', 1, 0, 'view.menu.common', NULL, '/common', '/common/user', 'default', 4, NULL, NULL, 741903113123790848);
INSERT INTO `view_route_configration` VALUES (741903117892714496, 0, '/user/index.vue', 1, '2026-08-10 14:18:07.000000', 1, 0, 'view.menu.common.user', 741903117741719552, 'user', NULL, 'default', 0, NULL, NULL, 741903113123790848);
INSERT INTO `view_route_configration` VALUES (741903476816084992, 0, 'Layout', 1, '2026-08-10 14:19:33.000000', 1, 0, 'xh', NULL, '/xh', '', 'default', 0, NULL, NULL, 741903476560232448);
INSERT INTO `view_route_configration` VALUES (741903476874805248, 0, '/admin/scan-config/index.vue', 1, '2026-08-10 14:19:33.000000', 1, 0, 'xh-doc-scan', 741903476816084992, '/xh/index', '', 'default', 0, NULL, NULL, 741903476560232448);
INSERT INTO `view_route_configration` VALUES (741903476941914112, 0, 'Layout', 1, '2026-08-10 14:19:33.000000', 1, 0, 'view.menu.admin.title', NULL, '/admin', '/admin/user', 'default', 1, NULL, NULL, 741903476560232448);
INSERT INTO `view_route_configration` VALUES (741903477076131840, 0, '/admin/user/index.vue', 1, '2026-08-10 14:19:33.000000', 1, 0, 'view.menu.admin.user', 741903476941914112, 'user', NULL, 'default', 0, NULL, NULL, 741903476560232448);
INSERT INTO `view_route_configration` VALUES (741903477130657792, 0, '/admin/role/index.vue', 1, '2026-08-10 14:19:33.000000', 1, 0, 'view.menu.admin.role', 741903476941914112, 'role', NULL, 'default', 1, NULL, NULL, 741903476560232448);
INSERT INTO `view_route_configration` VALUES (741903477218738176, 0, '/admin/company/index.vue', 1, '2026-08-10 14:19:33.000000', 1, 0, 'view.menu.admin.company', 741903476941914112, 'company', NULL, 'default', 2, NULL, NULL, 741903476560232448);
INSERT INTO `view_route_configration` VALUES (741903477277458432, 0, '/admin/role_group/index.vue', 1, '2026-08-10 14:19:33.000000', 1, 0, 'view.menu.admin.role.group', 741903476941914112, 'role-group', NULL, 'default', 3, NULL, NULL, 741903476560232448);
INSERT INTO `view_route_configration` VALUES (741903477344567296, 0, 'Layout', 1, '2026-08-10 14:19:33.000000', 1, 0, 'route-manager', NULL, '/route-manager', '', 'default', 2, NULL, NULL, 741903476560232448);
INSERT INTO `view_route_configration` VALUES (741903477432647680, 0, '/admin/route-manager/index.vue', 1, '2026-08-10 14:19:33.000000', 1, 0, 'routeManager', 741903477344567296, 'index', '', 'default', 0, NULL, NULL, 741903476560232448);
INSERT INTO `view_route_configration` VALUES (741903477529116672, 0, 'Layout', 1, '2026-08-10 14:19:33.000000', 1, 0, 'view.menu.develop', NULL, '/develop', '/develop/form-manager', 'default', 3, NULL, NULL, 741903476560232448);
INSERT INTO `view_route_configration` VALUES (741903477646557184, 0, '/form/FormManager.vue', 1, '2026-08-10 14:19:33.000000', 1, 0, 'view.menu.develop.form.manager', 741903477529116672, 'form-manager', NULL, 'default', 0, NULL, NULL, 741903476560232448);
INSERT INTO `view_route_configration` VALUES (741903477717860352, 0, '/form/FormCreator.vue', 1, '2026-08-10 14:19:33.000000', 1, 0, 'view.menu.develop.form.create', 741903477529116672, 'form', NULL, 'default', 1, NULL, NULL, 741903476560232448);
INSERT INTO `view_route_configration` VALUES (741903477797552128, 0, '/admin/dic/index.vue', 1, '2026-08-10 14:19:33.000000', 1, 0, 'view.menu.develop.dic', 741903477529116672, 'dic', NULL, 'default', 2, NULL, NULL, 741903476560232448);
INSERT INTO `view_route_configration` VALUES (741903477847883776, 0, '/admin/generic-api/index.vue', 1, '2026-08-10 14:19:33.000000', 1, 0, 'view.menu.develop.generic.api', 741903477529116672, 'generic-api', NULL, 'default', 3, NULL, NULL, 741903476560232448);
INSERT INTO `view_route_configration` VALUES (741903477961129984, 0, '/admin/systemlog/index.vue', 1, '2026-08-10 14:19:33.000000', 1, 0, 'view.menu.develop.systemlog', 741903477529116672, 'systemlog', NULL, 'default', 4, NULL, NULL, 741903476560232448);
INSERT INTO `view_route_configration` VALUES (741903478053404672, 0, '/cache/index.vue', 1, '2026-08-10 14:19:33.000000', 1, 0, 'view.menu.develop.cache', 741903477529116672, 'cache', NULL, 'default', 5, NULL, NULL, 741903476560232448);
INSERT INTO `view_route_configration` VALUES (741903478116319232, 0, 'Layout', 1, '2026-08-10 14:19:33.000000', 1, 0, 'view.menu.common', NULL, '/common', '/common/user', 'default', 4, NULL, NULL, 741903476560232448);
INSERT INTO `view_route_configration` VALUES (741903478191816704, 0, '/user/index.vue', 1, '2026-08-10 14:19:33.000000', 1, 0, 'view.menu.common.user', 741903478116319232, 'user', NULL, 'default', 0, NULL, NULL, 741903476560232448);
INSERT INTO `view_route_configration` VALUES (743373888087392256, 0, 'Layout', 1, '2026-08-14 15:42:26.000000', 1, 0, 'view.menu.admin.title', NULL, '/admin', '/admin/user', 'default', 0, NULL, NULL, 743373888045449216);
INSERT INTO `view_route_configration` VALUES (743373888129335296, 0, '/admin/user/index.vue', 1, '2026-08-14 15:42:26.000000', 1, 0, 'view.menu.admin.user', 743373888087392256, 'user', NULL, 'default', 0, NULL, NULL, 743373888045449216);
INSERT INTO `view_route_configration` VALUES (743373888150306816, 0, '/admin/role/index.vue', 1, '2026-08-14 15:42:26.000000', 1, 0, 'view.menu.admin.role', 743373888087392256, 'role', NULL, 'default', 1, NULL, NULL, 743373888045449216);
INSERT INTO `view_route_configration` VALUES (743373888188055552, 0, '/admin/company/index.vue', 1, '2026-08-14 15:42:26.000000', 1, 0, 'view.menu.admin.company', 743373888087392256, 'company', NULL, 'default', 2, NULL, NULL, 743373888045449216);
INSERT INTO `view_route_configration` VALUES (743373888213221376, 0, '/admin/role_group/index.vue', 1, '2026-08-14 15:42:26.000000', 1, 0, 'view.menu.admin.role.group', 743373888087392256, 'role-group', NULL, 'default', 3, NULL, NULL, 743373888045449216);
INSERT INTO `view_route_configration` VALUES (743373888242581504, 0, 'Layout', 1, '2026-08-14 15:42:26.000000', 1, 0, 'route-manager', NULL, '/route-manager', '', 'default', 1, NULL, NULL, 743373888045449216);
INSERT INTO `view_route_configration` VALUES (743373888276135936, 0, '/admin/route-manager/index.vue', 1, '2026-08-14 15:42:26.000000', 1, 0, 'routeManager', 743373888242581504, 'index', '', 'default', 0, NULL, NULL, 743373888045449216);
INSERT INTO `view_route_configration` VALUES (743373888297107456, 0, 'Layout', 1, '2026-08-14 15:42:26.000000', 1, 0, 'view.menu.develop', NULL, '/develop', '/develop/form-manager', 'default', 2, NULL, NULL, 743373888045449216);
INSERT INTO `view_route_configration` VALUES (743373888347439104, 0, '/form/FormManager.vue', 1, '2026-08-14 15:42:26.000000', 1, 0, 'view.menu.develop.form.manager', 743373888297107456, 'form-manager', NULL, 'default', 0, NULL, NULL, 743373888045449216);
INSERT INTO `view_route_configration` VALUES (743373888368410624, 0, '/form/FormCreator.vue', 1, '2026-08-14 15:42:26.000000', 1, 0, 'view.menu.develop.form.create', 743373888297107456, 'form', NULL, 'default', 1, NULL, NULL, 743373888045449216);
INSERT INTO `view_route_configration` VALUES (743373888389382144, 0, '/admin/dic/index.vue', 1, '2026-08-14 15:42:26.000000', 1, 0, 'view.menu.develop.dic', 743373888297107456, 'dic', NULL, 'default', 2, NULL, NULL, 743373888045449216);
INSERT INTO `view_route_configration` VALUES (743373888410353664, 0, '/admin/generic-api/index.vue', 1, '2026-08-14 15:42:26.000000', 1, 0, 'view.menu.develop.generic.api', 743373888297107456, 'generic-api', NULL, 'default', 3, NULL, NULL, 743373888045449216);
INSERT INTO `view_route_configration` VALUES (743373888427130880, 0, '/admin/systemlog/index.vue', 1, '2026-08-14 15:42:26.000000', 1, 0, 'view.menu.develop.systemlog', 743373888297107456, 'systemlog', NULL, 'default', 4, NULL, NULL, 743373888045449216);
INSERT INTO `view_route_configration` VALUES (743373888448102400, 0, '/cache/index.vue', 1, '2026-08-14 15:42:26.000000', 1, 0, 'view.menu.develop.cache', 743373888297107456, 'cache', NULL, 'default', 5, NULL, NULL, 743373888045449216);
INSERT INTO `view_route_configration` VALUES (743373888464879616, 0, 'Layout', 1, '2026-08-14 15:42:26.000000', 1, 0, 'view.menu.common', NULL, '/common', '/common/user', 'default', 3, NULL, NULL, 743373888045449216);
INSERT INTO `view_route_configration` VALUES (743373888485851136, 0, '/user/index.vue', 1, '2026-08-14 15:42:26.000000', 1, 0, 'view.menu.common.user', 743373888464879616, 'user', NULL, 'default', 0, NULL, NULL, 743373888045449216);
INSERT INTO `view_route_configration` VALUES (744347082403348480, 0, 'Layout', 1, '2026-08-17 08:09:34.000000', 1, 0, 'view.menu.admin.title', NULL, '/admin', '/admin/user', 'default', 0, NULL, NULL, 744347082327851008);
INSERT INTO `view_route_configration` VALUES (744347082462068736, 0, '/admin/user/index.vue', 1, '2026-08-17 08:09:34.000000', 1, 0, 'view.menu.admin.user', 744347082403348480, 'user', NULL, 'default', 0, NULL, NULL, 744347082327851008);
INSERT INTO `view_route_configration` VALUES (744347082495623168, 0, '/admin/role/index.vue', 1, '2026-08-17 08:09:34.000000', 1, 0, 'view.menu.admin.role', 744347082403348480, 'role', NULL, 'default', 1, NULL, NULL, 744347082327851008);
INSERT INTO `view_route_configration` VALUES (744347082520788992, 0, '/admin/company/index.vue', 1, '2026-08-17 08:09:34.000000', 1, 0, 'view.menu.admin.company', 744347082403348480, 'company', NULL, 'default', 2, NULL, NULL, 744347082327851008);
INSERT INTO `view_route_configration` VALUES (744347082541760512, 0, '/admin/role_group/index.vue', 1, '2026-08-17 08:09:34.000000', 1, 0, 'view.menu.admin.role.group', 744347082403348480, 'role-group', NULL, 'default', 3, NULL, NULL, 744347082327851008);
INSERT INTO `view_route_configration` VALUES (744347082566926336, 0, 'Layout', 1, '2026-08-17 08:09:34.000000', 1, 0, 'route-manager', NULL, '/route-manager', '', 'default', 1, NULL, NULL, 744347082327851008);
INSERT INTO `view_route_configration` VALUES (744347082608869376, 0, '/admin/route-manager/index.vue', 1, '2026-08-17 08:09:34.000000', 1, 0, 'routeManager', 744347082566926336, 'index', '', 'default', 0, NULL, NULL, 744347082327851008);
INSERT INTO `view_route_configration` VALUES (744347082638229504, 0, 'Layout', 1, '2026-08-17 08:09:34.000000', 1, 0, 'view.menu.develop', NULL, '/develop', '/develop/form-manager', 'default', 2, NULL, NULL, 744347082327851008);
INSERT INTO `view_route_configration` VALUES (744347082684366848, 0, '/form/FormManager.vue', 1, '2026-08-17 08:09:34.000000', 1, 0, 'view.menu.develop.form.manager', 744347082638229504, 'form-manager', NULL, 'default', 0, NULL, NULL, 744347082327851008);
INSERT INTO `view_route_configration` VALUES (744347082705338368, 0, '/form/FormCreator.vue', 1, '2026-08-17 08:09:34.000000', 1, 0, 'view.menu.develop.form.create', 744347082638229504, 'form', NULL, 'default', 1, NULL, NULL, 744347082327851008);
INSERT INTO `view_route_configration` VALUES (744347082730504192, 0, '/admin/dic/index.vue', 1, '2026-08-17 08:09:34.000000', 1, 0, 'view.menu.develop.dic', 744347082638229504, 'dic', NULL, 'default', 2, NULL, NULL, 744347082327851008);
INSERT INTO `view_route_configration` VALUES (744347082751475712, 0, '/admin/generic-api/index.vue', 1, '2026-08-17 08:09:34.000000', 1, 0, 'view.menu.develop.generic.api', 744347082638229504, 'generic-api', NULL, 'default', 3, NULL, NULL, 744347082327851008);
INSERT INTO `view_route_configration` VALUES (744347082776641536, 0, '/admin/systemlog/index.vue', 1, '2026-08-17 08:09:34.000000', 1, 0, 'view.menu.develop.systemlog', 744347082638229504, 'systemlog', NULL, 'default', 4, NULL, NULL, 744347082327851008);
INSERT INTO `view_route_configration` VALUES (744347082797613056, 0, '/cache/index.vue', 1, '2026-08-17 08:09:34.000000', 1, 0, 'view.menu.develop.cache', 744347082638229504, 'cache', NULL, 'default', 5, NULL, NULL, 744347082327851008);
INSERT INTO `view_route_configration` VALUES (744347082818584576, 0, 'Layout', 1, '2026-08-17 08:09:34.000000', 1, 0, 'view.menu.common', NULL, '/common', '/common/user', 'default', 3, NULL, NULL, 744347082327851008);
INSERT INTO `view_route_configration` VALUES (744347082839556096, 0, '/user/index.vue', 1, '2026-08-17 08:09:34.000000', 1, 0, 'view.menu.common.user', 744347082818584576, 'user', NULL, 'default', 0, NULL, NULL, 744347082327851008);
INSERT INTO `view_route_configration` VALUES (744347082856333312, 0, 'Layout', 1, '2026-08-17 08:09:34.000000', 1, 0, 'oss', NULL, 'oss', '', 'default', 4, NULL, NULL, 744347082327851008);
INSERT INTO `view_route_configration` VALUES (744347082919247872, 0, '/oss/index.vue', 1, '2026-08-17 08:09:34.000000', 1, 0, 'oss-manange', 744347082856333312, 'oss-manange', '', 'default', 0, NULL, NULL, 744347082327851008);
INSERT INTO `view_route_configration` VALUES (744347450730348544, 0, 'Layout', 1, '2026-08-17 08:11:02.000000', 1, 0, 'view.menu.admin.title', NULL, '/admin', '/admin/user', 'default', 0, NULL, NULL, 744347450713571328);
INSERT INTO `view_route_configration` VALUES (744347450772291584, 0, '/admin/user/index.vue', 1, '2026-08-17 08:11:02.000000', 1, 0, 'view.menu.admin.user', 744347450730348544, 'user', NULL, 'default', 0, NULL, NULL, 744347450713571328);
INSERT INTO `view_route_configration` VALUES (744347450793263104, 0, '/admin/role/index.vue', 1, '2026-08-17 08:11:02.000000', 1, 0, 'view.menu.admin.role', 744347450730348544, 'role', NULL, 'default', 1, NULL, NULL, 744347450713571328);
INSERT INTO `view_route_configration` VALUES (744347450977812480, 0, '/admin/company/index.vue', 1, '2026-08-17 08:11:02.000000', 1, 0, 'view.menu.admin.company', 744347450730348544, 'company', NULL, 'default', 2, NULL, NULL, 744347450713571328);
INSERT INTO `view_route_configration` VALUES (744347451002978304, 0, '/admin/role_group/index.vue', 1, '2026-08-17 08:11:02.000000', 1, 0, 'view.menu.admin.role.group', 744347450730348544, 'role-group', NULL, 'default', 3, NULL, NULL, 744347450713571328);
INSERT INTO `view_route_configration` VALUES (744347451019755520, 0, 'Layout', 1, '2026-08-17 08:11:02.000000', 1, 0, 'route-manager', NULL, '/route-manager', '', 'default', 1, NULL, NULL, 744347450713571328);
INSERT INTO `view_route_configration` VALUES (744347451049115648, 0, '/admin/route-manager/index.vue', 1, '2026-08-17 08:11:02.000000', 1, 0, 'routeManager', 744347451019755520, 'index', '', 'default', 0, NULL, NULL, 744347450713571328);
INSERT INTO `view_route_configration` VALUES (744347451065892864, 0, 'Layout', 1, '2026-08-17 08:11:02.000000', 1, 0, 'view.menu.develop', NULL, '/develop', '/develop/form-manager', 'default', 2, NULL, NULL, 744347450713571328);
INSERT INTO `view_route_configration` VALUES (744347451091058688, 0, '/form/FormManager.vue', 1, '2026-08-17 08:11:02.000000', 1, 0, 'view.menu.develop.form.manager', 744347451065892864, 'form-manager', NULL, 'default', 0, NULL, NULL, 744347450713571328);
INSERT INTO `view_route_configration` VALUES (744347451116224512, 0, '/form/FormCreator.vue', 1, '2026-08-17 08:11:02.000000', 1, 0, 'view.menu.develop.form.create', 744347451065892864, 'form', NULL, 'default', 1, NULL, NULL, 744347450713571328);
INSERT INTO `view_route_configration` VALUES (744347451137196032, 0, '/admin/dic/index.vue', 1, '2026-08-17 08:11:02.000000', 1, 0, 'view.menu.develop.dic', 744347451065892864, 'dic', NULL, 'default', 2, NULL, NULL, 744347450713571328);
INSERT INTO `view_route_configration` VALUES (744347451153973248, 0, '/admin/generic-api/index.vue', 1, '2026-08-17 08:11:02.000000', 1, 0, 'view.menu.develop.generic.api', 744347451065892864, 'generic-api', NULL, 'default', 3, NULL, NULL, 744347450713571328);
INSERT INTO `view_route_configration` VALUES (744347451170750464, 0, '/admin/systemlog/index.vue', 1, '2026-08-17 08:11:02.000000', 1, 0, 'view.menu.develop.systemlog', 744347451065892864, 'systemlog', NULL, 'default', 4, NULL, NULL, 744347450713571328);
INSERT INTO `view_route_configration` VALUES (744347451183333376, 0, '/cache/index.vue', 1, '2026-08-17 08:11:02.000000', 1, 0, 'view.menu.develop.cache', 744347451065892864, 'cache', NULL, 'default', 5, NULL, NULL, 744347450713571328);
INSERT INTO `view_route_configration` VALUES (744347451195916288, 0, 'Layout', 1, '2026-08-17 08:11:02.000000', 1, 0, 'view.menu.common', NULL, '/common', '/common/user', 'default', 3, NULL, NULL, 744347450713571328);
INSERT INTO `view_route_configration` VALUES (744347451212693504, 0, '/user/index.vue', 1, '2026-08-17 08:11:02.000000', 1, 0, 'view.menu.common.user', 744347451195916288, 'user', NULL, 'default', 0, NULL, NULL, 744347450713571328);
INSERT INTO `view_route_configration` VALUES (744347451233665024, 0, 'Layout', 1, '2026-08-17 08:11:02.000000', 1, 0, 'oss', NULL, '/oss', '', 'default', 4, NULL, NULL, 744347450713571328);
INSERT INTO `view_route_configration` VALUES (744347451250442240, 0, '/oss/index.vue', 1, '2026-08-17 08:11:02.000000', 1, 0, 'oss-manange', 744347451233665024, 'manange', '', 'default', 0, NULL, NULL, 744347450713571328);
INSERT INTO `view_route_configration` VALUES (744456570582073344, 0, 'Layout', 1, '2026-08-17 15:24:38.000000', 1, 0, 'view.menu.admin.title', NULL, '/admin', '/admin/user', 'default', 0, NULL, NULL, 744456570561101824);
INSERT INTO `view_route_configration` VALUES (744456570632404992, 0, '/admin/user/index.vue', 1, '2026-08-17 15:24:38.000000', 1, 0, 'view.menu.admin.user', 744456570582073344, 'user', NULL, 'default', 0, NULL, NULL, 744456570561101824);
INSERT INTO `view_route_configration` VALUES (744456570649182208, 0, '/admin/role/index.vue', 1, '2026-08-17 15:24:38.000000', 1, 0, 'view.menu.admin.role', 744456570582073344, 'role', NULL, 'default', 1, NULL, NULL, 744456570561101824);
INSERT INTO `view_route_configration` VALUES (744456570670153728, 0, '/admin/company/index.vue', 1, '2026-08-17 15:24:38.000000', 1, 0, 'view.menu.admin.company', 744456570582073344, 'company', NULL, 'default', 2, NULL, NULL, 744456570561101824);
INSERT INTO `view_route_configration` VALUES (744456570695319552, 0, '/admin/role_group/index.vue', 1, '2026-08-17 15:24:38.000000', 1, 0, 'view.menu.admin.role.group', 744456570582073344, 'role-group', NULL, 'default', 3, NULL, NULL, 744456570561101824);
INSERT INTO `view_route_configration` VALUES (744456570712096768, 0, 'Layout', 1, '2026-08-17 15:24:38.000000', 1, 0, 'route-manager', NULL, '/route-manager', '', 'default', 1, NULL, NULL, 744456570561101824);
INSERT INTO `view_route_configration` VALUES (744456570737262592, 0, '/admin/route-manager/index.vue', 1, '2026-08-17 15:24:38.000000', 1, 0, 'routeManager', 744456570712096768, 'index', '', 'default', 0, NULL, NULL, 744456570561101824);
INSERT INTO `view_route_configration` VALUES (744456570758234112, 0, 'Layout', 1, '2026-08-17 15:24:38.000000', 1, 0, 'view.menu.develop', NULL, '/develop', '/develop/form-manager', 'default', 2, NULL, NULL, 744456570561101824);
INSERT INTO `view_route_configration` VALUES (744456570783399936, 0, '/form/FormManager.vue', 1, '2026-08-17 15:24:38.000000', 1, 0, 'view.menu.develop.form.manager', 744456570758234112, 'form-manager', NULL, 'default', 0, NULL, NULL, 744456570561101824);
INSERT INTO `view_route_configration` VALUES (744456570800177152, 0, '/form/FormCreator.vue', 1, '2026-08-17 15:24:38.000000', 1, 0, 'view.menu.develop.form.create', 744456570758234112, 'form', NULL, 'default', 1, NULL, NULL, 744456570561101824);
INSERT INTO `view_route_configration` VALUES (744456570821148672, 0, '/admin/dic/index.vue', 1, '2026-08-17 15:24:38.000000', 1, 0, 'view.menu.develop.dic', 744456570758234112, 'dic', NULL, 'default', 2, NULL, NULL, 744456570561101824);
INSERT INTO `view_route_configration` VALUES (744456570837925888, 0, '/admin/generic-api/index.vue', 1, '2026-08-17 15:24:38.000000', 1, 0, 'view.menu.develop.generic.api', 744456570758234112, 'generic-api', NULL, 'default', 3, NULL, NULL, 744456570561101824);
INSERT INTO `view_route_configration` VALUES (744456570854703104, 0, '/admin/systemlog/index.vue', 1, '2026-08-17 15:24:38.000000', 1, 0, 'view.menu.develop.systemlog', 744456570758234112, 'systemlog', NULL, 'default', 4, NULL, NULL, 744456570561101824);
INSERT INTO `view_route_configration` VALUES (744456570871480320, 0, '/cache/index.vue', 1, '2026-08-17 15:24:38.000000', 1, 0, 'view.menu.develop.cache', 744456570758234112, 'cache', NULL, 'default', 5, NULL, NULL, 744456570561101824);
INSERT INTO `view_route_configration` VALUES (744456570888257536, 0, 'Layout', 1, '2026-08-17 15:24:38.000000', 1, 0, 'view.menu.common', NULL, '/common', '/common/user', 'default', 3, NULL, NULL, 744456570561101824);
INSERT INTO `view_route_configration` VALUES (744456570905034752, 0, '/user/index.vue', 1, '2026-08-17 15:24:38.000000', 1, 0, 'view.menu.common.user', 744456570888257536, 'user', NULL, 'default', 0, NULL, NULL, 744456570561101824);
INSERT INTO `view_route_configration` VALUES (744456570921811968, 0, 'Layout', 1, '2026-08-17 15:24:38.000000', 1, 0, 'oss', NULL, '/oss', '', 'default', 4, NULL, NULL, 744456570561101824);
INSERT INTO `view_route_configration` VALUES (744456570938589184, 0, '/oss/index.vue', 1, '2026-08-17 15:24:38.000000', 1, 0, 'oss-manange', 744456570921811968, 'manange', '', 'default', 0, NULL, NULL, 744456570561101824);
INSERT INTO `view_route_configration` VALUES (744456570955366400, 0, 'Layout', 1, '2026-08-17 15:24:38.000000', 1, 0, 'version', NULL, '/version', '', 'default', 5, NULL, NULL, 744456570561101824);
INSERT INTO `view_route_configration` VALUES (744456570972143616, 0, '/admin/version/index.vue', 1, '2026-08-17 15:24:38.000000', 1, 0, 'version-manage', 744456570955366400, 'manage', '', 'default', 0, NULL, NULL, 744456570561101824);
INSERT INTO `view_route_configration` VALUES (744459411426115584, 0, 'Layout', 1, '2026-08-17 15:35:55.000000', 1, 0, 'view.menu.admin.title', NULL, '/admin', '/admin/user', 'default', 0, NULL, NULL, 744459411392561152);
INSERT INTO `view_route_configration` VALUES (744459411455475712, 0, '/admin/user/index.vue', 1, '2026-08-17 15:35:55.000000', 1, 0, 'view.menu.admin.user', 744459411426115584, 'user', NULL, 'default', 0, NULL, NULL, 744459411392561152);
INSERT INTO `view_route_configration` VALUES (744459411472252928, 0, '/admin/role/index.vue', 1, '2026-08-17 15:35:55.000000', 1, 0, 'view.menu.admin.role', 744459411426115584, 'role', NULL, 'default', 1, NULL, NULL, 744459411392561152);
INSERT INTO `view_route_configration` VALUES (744459411484835840, 0, '/admin/company/index.vue', 1, '2026-08-17 15:35:55.000000', 1, 0, 'view.menu.admin.company', 744459411426115584, 'company', NULL, 'default', 2, NULL, NULL, 744459411392561152);
INSERT INTO `view_route_configration` VALUES (744459411501613056, 0, '/admin/role_group/index.vue', 1, '2026-08-17 15:35:55.000000', 1, 0, 'view.menu.admin.role.group', 744459411426115584, 'role-group', NULL, 'default', 3, NULL, NULL, 744459411392561152);
INSERT INTO `view_route_configration` VALUES (744459411514195968, 0, 'Layout', 1, '2026-08-17 15:35:55.000000', 1, 0, 'route-manager', NULL, '/route-manager', '', 'default', 1, NULL, NULL, 744459411392561152);
INSERT INTO `view_route_configration` VALUES (744459411556139008, 0, '/admin/route-manager/index.vue', 1, '2026-08-17 15:35:55.000000', 1, 0, 'routeManager', 744459411514195968, 'index', '', 'default', 0, NULL, NULL, 744459411392561152);
INSERT INTO `view_route_configration` VALUES (744459411581304832, 0, 'Layout', 1, '2026-08-17 15:35:55.000000', 1, 0, 'view.menu.develop', NULL, '/develop', '/develop/form-manager', 'default', 2, NULL, NULL, 744459411392561152);
INSERT INTO `view_route_configration` VALUES (744459411610664960, 0, '/form/FormManager.vue', 1, '2026-08-17 15:35:55.000000', 1, 0, 'view.menu.develop.form.manager', 744459411581304832, 'form-manager', NULL, 'default', 0, NULL, NULL, 744459411392561152);
INSERT INTO `view_route_configration` VALUES (744459411623247872, 0, '/form/FormCreator.vue', 1, '2026-08-17 15:35:55.000000', 1, 0, 'view.menu.develop.form.create', 744459411581304832, 'form', NULL, 'default', 1, NULL, NULL, 744459411392561152);
INSERT INTO `view_route_configration` VALUES (744459411640025088, 0, '/admin/dic/index.vue', 1, '2026-08-17 15:35:55.000000', 1, 0, 'view.menu.develop.dic', 744459411581304832, 'dic', NULL, 'default', 2, NULL, NULL, 744459411392561152);
INSERT INTO `view_route_configration` VALUES (744459411660996608, 0, '/admin/generic-api/index.vue', 1, '2026-08-17 15:35:55.000000', 1, 0, 'view.menu.develop.generic.api', 744459411581304832, 'generic-api', NULL, 'default', 3, NULL, NULL, 744459411392561152);
INSERT INTO `view_route_configration` VALUES (744459411681968128, 0, '/admin/systemlog/index.vue', 1, '2026-08-17 15:35:55.000000', 1, 0, 'view.menu.develop.systemlog', 744459411581304832, 'systemlog', NULL, 'default', 4, NULL, NULL, 744459411392561152);
INSERT INTO `view_route_configration` VALUES (744459411698745344, 0, '/cache/index.vue', 1, '2026-08-17 15:35:55.000000', 1, 0, 'view.menu.develop.cache', 744459411581304832, 'cache', NULL, 'default', 5, NULL, NULL, 744459411392561152);
INSERT INTO `view_route_configration` VALUES (744459411711328256, 0, 'Layout', 1, '2026-08-17 15:35:55.000000', 1, 0, 'view.menu.common', NULL, '/common', '/common/user', 'default', 3, NULL, NULL, 744459411392561152);
INSERT INTO `view_route_configration` VALUES (744459411723911168, 0, '/user/index.vue', 1, '2026-08-17 15:35:55.000000', 1, 0, 'view.menu.common.user', 744459411711328256, 'user', NULL, 'default', 0, NULL, NULL, 744459411392561152);
INSERT INTO `view_route_configration` VALUES (744459411740688384, 0, 'Layout', 1, '2026-08-17 15:35:55.000000', 1, 0, 'oss', NULL, '/oss', '', 'default', 4, NULL, NULL, 744459411392561152);
INSERT INTO `view_route_configration` VALUES (744459411753271296, 0, '/oss/index.vue', 1, '2026-08-17 15:35:55.000000', 1, 0, 'oss-manange', 744459411740688384, 'manange', '', 'default', 0, NULL, NULL, 744459411392561152);
INSERT INTO `view_route_configration` VALUES (744459411765854208, 0, 'Layout', 1, '2026-08-17 15:35:55.000000', 1, 0, 'version', NULL, '/version', '', 'default', 5, NULL, NULL, 744459411392561152);
INSERT INTO `view_route_configration` VALUES (744459411782631424, 0, '/admin/version/index.vue', 1, '2026-08-17 15:35:55.000000', 1, 0, 'version-manage', 744459411765854208, 'manage', '', 'default', 0, NULL, NULL, 744459411392561152);
INSERT INTO `view_route_configration` VALUES (744461304273567744, 0, 'Layout', 1, '2026-08-17 15:43:26.000000', 1, 0, 'view.menu.admin.title', NULL, '/admin', '/admin/user', 'default', 0, NULL, NULL, 744461304235819008);
INSERT INTO `view_route_configration` VALUES (744461304328093696, 0, '/admin/user/index.vue', 1, '2026-08-17 15:43:26.000000', 1, 0, 'view.menu.admin.user', 744461304273567744, 'user', NULL, 'default', 0, NULL, NULL, 744461304235819008);
INSERT INTO `view_route_configration` VALUES (744461304344870912, 0, '/admin/role/index.vue', 1, '2026-08-17 15:43:26.000000', 1, 0, 'view.menu.admin.role', 744461304273567744, 'role', NULL, 'default', 1, NULL, NULL, 744461304235819008);
INSERT INTO `view_route_configration` VALUES (744461304365842432, 0, '/admin/company/index.vue', 1, '2026-08-17 15:43:26.000000', 1, 0, 'view.menu.admin.company', 744461304273567744, 'company', NULL, 'default', 2, NULL, NULL, 744461304235819008);
INSERT INTO `view_route_configration` VALUES (744461304386813952, 0, '/admin/role_group/index.vue', 1, '2026-08-17 15:43:26.000000', 1, 0, 'view.menu.admin.role.group', 744461304273567744, 'role-group', NULL, 'default', 3, NULL, NULL, 744461304235819008);
INSERT INTO `view_route_configration` VALUES (744461304407785472, 0, 'Layout', 1, '2026-08-17 15:43:26.000000', 1, 0, 'route-manager', NULL, '/route-manager', '', 'default', 1, NULL, NULL, 744461304235819008);
INSERT INTO `view_route_configration` VALUES (744461304437145600, 0, '/admin/route-manager/index.vue', 1, '2026-08-17 15:43:26.000000', 1, 0, 'routeManager', 744461304407785472, 'index', '', 'default', 0, NULL, NULL, 744461304235819008);
INSERT INTO `view_route_configration` VALUES (744461304458117120, 0, 'Layout', 1, '2026-08-17 15:43:26.000000', 1, 0, 'view.menu.develop', NULL, '/develop', '/develop/form-manager', 'default', 2, NULL, NULL, 744461304235819008);
INSERT INTO `view_route_configration` VALUES (744461304483282944, 0, '/form/FormManager.vue', 1, '2026-08-17 15:43:26.000000', 1, 0, 'view.menu.develop.form.manager', 744461304458117120, 'form-manager', NULL, 'default', 0, NULL, NULL, 744461304235819008);
INSERT INTO `view_route_configration` VALUES (744461304504254464, 0, '/form/FormCreator.vue', 1, '2026-08-17 15:43:26.000000', 1, 0, 'view.menu.develop.form.create', 744461304458117120, 'form', NULL, 'default', 1, NULL, NULL, 744461304235819008);
INSERT INTO `view_route_configration` VALUES (744461304521031680, 0, '/admin/dic/index.vue', 1, '2026-08-17 15:43:26.000000', 1, 0, 'view.menu.develop.dic', 744461304458117120, 'dic', NULL, 'default', 2, NULL, NULL, 744461304235819008);
INSERT INTO `view_route_configration` VALUES (744461304546197504, 0, '/admin/generic-api/index.vue', 1, '2026-08-17 15:43:26.000000', 1, 0, 'view.menu.develop.generic.api', 744461304458117120, 'generic-api', NULL, 'default', 3, NULL, NULL, 744461304235819008);
INSERT INTO `view_route_configration` VALUES (744461304571363328, 0, '/admin/systemlog/index.vue', 1, '2026-08-17 15:43:26.000000', 1, 0, 'view.menu.develop.systemlog', 744461304458117120, 'systemlog', NULL, 'default', 4, NULL, NULL, 744461304235819008);
INSERT INTO `view_route_configration` VALUES (744461304592334848, 0, '/cache/index.vue', 1, '2026-08-17 15:43:26.000000', 1, 0, 'view.menu.develop.cache', 744461304458117120, 'cache', NULL, 'default', 5, NULL, NULL, 744461304235819008);
INSERT INTO `view_route_configration` VALUES (744461304617500672, 0, 'Layout', 1, '2026-08-17 15:43:26.000000', 1, 0, 'view.menu.common', NULL, '/common', '/common/user', 'default', 3, NULL, NULL, 744461304235819008);
INSERT INTO `view_route_configration` VALUES (744461304642666496, 0, '/user/index.vue', 1, '2026-08-17 15:43:26.000000', 1, 0, 'view.menu.common.user', 744461304617500672, 'user', NULL, 'default', 0, NULL, NULL, 744461304235819008);
INSERT INTO `view_route_configration` VALUES (744461304659443712, 0, 'Layout', 1, '2026-08-17 15:43:26.000000', 1, 0, 'oss', NULL, '/oss', '', 'default', 4, NULL, NULL, 744461304235819008);
INSERT INTO `view_route_configration` VALUES (744461304684609536, 0, '/oss/index.vue', 1, '2026-08-17 15:43:26.000000', 1, 0, 'oss-manange', 744461304659443712, 'manange', '', 'default', 0, NULL, NULL, 744461304235819008);
INSERT INTO `view_route_configration` VALUES (744461304705581056, 0, 'Layout', 1, '2026-08-17 15:43:26.000000', 1, 0, 'version', NULL, '/version', '', 'default', 5, NULL, NULL, 744461304235819008);
INSERT INTO `view_route_configration` VALUES (744461304722358272, 0, '/admin/version/index.vue', 1, '2026-08-17 15:43:26.000000', 1, 0, 'version-manage', 744461304705581056, 'manage', '', 'default', 0, NULL, NULL, 744461304235819008);
INSERT INTO `view_route_configration` VALUES (753118960395747328, 0, 'Layout', 1, '2026-09-10 13:05:52.000000', 1, 0, 'view.menu.admin.title', NULL, '/admin', '/admin/user', 'default', 0, NULL, NULL, 753118960366387200);
INSERT INTO `view_route_configration` VALUES (753118960446078976, 0, '/admin/user/index.vue', 1, '2026-09-10 13:05:52.000000', 1, 0, 'view.menu.admin.user', 753118960395747328, 'user', NULL, 'default', 0, NULL, NULL, 753118960366387200);
INSERT INTO `view_route_configration` VALUES (753118960467050496, 0, '/admin/role/index.vue', 1, '2026-09-10 13:05:52.000000', 1, 0, 'view.menu.admin.role', 753118960395747328, 'role', NULL, 'default', 1, NULL, NULL, 753118960366387200);
INSERT INTO `view_route_configration` VALUES (753118960483827712, 0, '/admin/company/index.vue', 1, '2026-09-10 13:05:52.000000', 1, 0, 'view.menu.admin.company', 753118960395747328, 'company', NULL, 'default', 2, NULL, NULL, 753118960366387200);
INSERT INTO `view_route_configration` VALUES (753118960508993536, 0, '/admin/role_group/index.vue', 1, '2026-09-10 13:05:52.000000', 1, 0, 'view.menu.admin.role.group', 753118960395747328, 'role-group', NULL, 'default', 3, NULL, NULL, 753118960366387200);
INSERT INTO `view_route_configration` VALUES (753118960525770752, 0, 'Layout', 1, '2026-09-10 13:05:52.000000', 1, 0, 'route-manager', NULL, '/route-manager', '', 'default', 1, NULL, NULL, 753118960366387200);
INSERT INTO `view_route_configration` VALUES (753118960576102400, 0, '/admin/route-manager/index.vue', 1, '2026-09-10 13:05:52.000000', 1, 0, 'routeManager', 753118960525770752, 'index', '', 'default', 0, NULL, NULL, 753118960366387200);
INSERT INTO `view_route_configration` VALUES (753118960605462528, 0, 'Layout', 1, '2026-09-10 13:05:52.000000', 1, 0, 'view.menu.develop', NULL, '/develop', '/develop/form-manager', 'default', 2, NULL, NULL, 753118960366387200);
INSERT INTO `view_route_configration` VALUES (753118960630628352, 0, '/form/FormManager.vue', 1, '2026-09-10 13:05:52.000000', 1, 0, 'view.menu.develop.form.manager', 753118960605462528, 'form-manager', NULL, 'default', 0, NULL, NULL, 753118960366387200);
INSERT INTO `view_route_configration` VALUES (753118960647405568, 0, '/form/FormCreator.vue', 1, '2026-09-10 13:05:52.000000', 1, 0, 'view.menu.develop.form.create', 753118960605462528, 'form', NULL, 'default', 1, NULL, NULL, 753118960366387200);
INSERT INTO `view_route_configration` VALUES (753118960685154304, 0, '/admin/dic/index.vue', 1, '2026-09-10 13:05:53.000000', 1, 0, 'view.menu.develop.dic', 753118960605462528, 'dic', NULL, 'default', 2, NULL, NULL, 753118960366387200);
INSERT INTO `view_route_configration` VALUES (753118960701931520, 0, '/admin/generic-api/index.vue', 1, '2026-09-10 13:05:53.000000', 1, 0, 'view.menu.develop.generic.api', 753118960605462528, 'generic-api', NULL, 'default', 3, NULL, NULL, 753118960366387200);
INSERT INTO `view_route_configration` VALUES (753118960722903040, 0, '/admin/systemlog/index.vue', 1, '2026-09-10 13:05:53.000000', 1, 0, 'view.menu.develop.systemlog', 753118960605462528, 'systemlog', NULL, 'default', 4, NULL, NULL, 753118960366387200);
INSERT INTO `view_route_configration` VALUES (753118960743874560, 0, '/cache/index.vue', 1, '2026-09-10 13:05:53.000000', 1, 0, 'view.menu.develop.cache', 753118960605462528, 'cache', NULL, 'default', 5, NULL, NULL, 753118960366387200);
INSERT INTO `view_route_configration` VALUES (753118960760651776, 0, '/data/DataHandle.vue', 1, '2026-09-10 13:05:53.000000', 1, 0, 'view.menu.data.handle', 753118960605462528, 'data-handle', '', 'default', 6, NULL, NULL, 753118960366387200);
INSERT INTO `view_route_configration` VALUES (753118960773234688, 0, 'Layout', 1, '2026-09-10 13:05:53.000000', 1, 0, 'view.menu.common', NULL, '/common', '/common/user', 'default', 3, NULL, NULL, 753118960366387200);
INSERT INTO `view_route_configration` VALUES (753118960790011904, 0, '/user/index.vue', 1, '2026-09-10 13:05:53.000000', 1, 0, 'view.menu.common.user', 753118960773234688, 'user', NULL, 'default', 0, NULL, NULL, 753118960366387200);
INSERT INTO `view_route_configration` VALUES (753118960810983424, 0, 'Layout', 1, '2026-09-10 13:05:53.000000', 1, 0, 'oss', NULL, '/oss', '', 'default', 4, NULL, NULL, 753118960366387200);
INSERT INTO `view_route_configration` VALUES (753118960827760640, 0, '/oss/index.vue', 1, '2026-09-10 13:05:53.000000', 1, 0, 'oss-manange', 753118960810983424, 'manange', '', 'default', 0, NULL, NULL, 753118960366387200);
INSERT INTO `view_route_configration` VALUES (753118960848732160, 0, 'Layout', 1, '2026-09-10 13:05:53.000000', 1, 0, 'version', NULL, '/version', '', 'default', 5, NULL, NULL, 753118960366387200);
INSERT INTO `view_route_configration` VALUES (753118960865509376, 0, '/admin/version/index.vue', 1, '2026-09-10 13:05:53.000000', 1, 0, 'version-manage', 753118960848732160, 'manage', '', 'default', 0, NULL, NULL, 753118960366387200);
INSERT INTO `view_route_configration` VALUES (755305983357485056, 0, 'Layout', 1, '2026-09-16 13:56:19.000000', 1, 0, 'view.menu.admin.title', NULL, '/admin', '/admin/user', 'default', 0, NULL, NULL, 755305983332319232);
INSERT INTO `view_route_configration` VALUES (755305983416205312, 0, '/admin/user/index.vue', 1, '2026-09-16 13:56:19.000000', 1, 0, 'view.menu.admin.user', 755305983357485056, 'user', NULL, 'default', 0, NULL, NULL, 755305983332319232);
INSERT INTO `view_route_configration` VALUES (755305983432982528, 0, '/admin/role/index.vue', 1, '2026-09-16 13:56:19.000000', 1, 0, 'view.menu.admin.role', 755305983357485056, 'role', NULL, 'default', 1, NULL, NULL, 755305983332319232);
INSERT INTO `view_route_configration` VALUES (755305983453954048, 0, '/admin/company/index.vue', 1, '2026-09-16 13:56:19.000000', 1, 0, 'view.menu.admin.company', 755305983357485056, 'company', NULL, 'default', 2, NULL, NULL, 755305983332319232);
INSERT INTO `view_route_configration` VALUES (755305983470731264, 0, '/admin/role_group/index.vue', 1, '2026-09-16 13:56:19.000000', 1, 0, 'view.menu.admin.role.group', 755305983357485056, 'role-group', NULL, 'default', 3, NULL, NULL, 755305983332319232);
INSERT INTO `view_route_configration` VALUES (755305983487508480, 0, '/admin/external-identity/index.vue', 1, '2026-09-16 13:56:19.000000', 1, 0, 'Ex-id', 755305983357485056, 'Ex-id', '', 'default', 4, NULL, NULL, 755305983332319232);
INSERT INTO `view_route_configration` VALUES (755305983512674304, 0, 'Layout', 1, '2026-09-16 13:56:19.000000', 1, 0, 'route-manager', NULL, '/route-manager', '', 'default', 1, NULL, NULL, 755305983332319232);
INSERT INTO `view_route_configration` VALUES (755305983554617344, 0, '/admin/route-manager/index.vue', 1, '2026-09-16 13:56:19.000000', 1, 0, 'routeManager', 755305983512674304, 'index', '', 'default', 0, NULL, NULL, 755305983332319232);
INSERT INTO `view_route_configration` VALUES (755305983567200256, 0, 'Layout', 1, '2026-09-16 13:56:19.000000', 1, 0, 'view.menu.develop', NULL, '/develop', '/develop/form-manager', 'default', 2, NULL, NULL, 755305983332319232);
INSERT INTO `view_route_configration` VALUES (755305983592366080, 0, '/form/FormManager.vue', 1, '2026-09-16 13:56:19.000000', 1, 0, 'view.menu.develop.form.manager', 755305983567200256, 'form-manager', NULL, 'default', 0, NULL, NULL, 755305983332319232);
INSERT INTO `view_route_configration` VALUES (755305983617531904, 0, '/form/FormCreator.vue', 1, '2026-09-16 13:56:19.000000', 1, 0, 'view.menu.develop.form.create', 755305983567200256, 'form', NULL, 'default', 1, NULL, NULL, 755305983332319232);
INSERT INTO `view_route_configration` VALUES (755305983630114816, 0, '/admin/dic/index.vue', 1, '2026-09-16 13:56:19.000000', 1, 0, 'view.menu.develop.dic', 755305983567200256, 'dic', NULL, 'default', 2, NULL, NULL, 755305983332319232);
INSERT INTO `view_route_configration` VALUES (755305983646892032, 0, '/admin/generic-api/index.vue', 1, '2026-09-16 13:56:19.000000', 1, 0, 'view.menu.develop.generic.api', 755305983567200256, 'generic-api', NULL, 'default', 3, NULL, NULL, 755305983332319232);
INSERT INTO `view_route_configration` VALUES (755305983667863552, 0, '/admin/systemlog/index.vue', 1, '2026-09-16 13:56:19.000000', 1, 0, 'view.menu.develop.systemlog', 755305983567200256, 'systemlog', NULL, 'default', 4, NULL, NULL, 755305983332319232);
INSERT INTO `view_route_configration` VALUES (755305983701417984, 0, '/cache/index.vue', 1, '2026-09-16 13:56:19.000000', 1, 0, 'view.menu.develop.cache', 755305983567200256, 'cache', NULL, 'default', 5, NULL, NULL, 755305983332319232);
INSERT INTO `view_route_configration` VALUES (755305983718195200, 0, '/data/DataHandle.vue', 1, '2026-09-16 13:56:19.000000', 1, 0, 'view.menu.data.handle', 755305983567200256, 'data-handle', '', 'default', 6, NULL, NULL, 755305983332319232);
INSERT INTO `view_route_configration` VALUES (755305983730778112, 0, 'Layout', 1, '2026-09-16 13:56:19.000000', 1, 0, 'view.menu.common', NULL, '/common', '/common/user', 'default', 3, NULL, NULL, 755305983332319232);
INSERT INTO `view_route_configration` VALUES (755305983747555328, 0, '/user/index.vue', 1, '2026-09-16 13:56:19.000000', 1, 0, 'view.menu.common.user', 755305983730778112, 'user', NULL, 'default', 0, NULL, NULL, 755305983332319232);
INSERT INTO `view_route_configration` VALUES (755305983760138240, 0, 'Layout', 1, '2026-09-16 13:56:19.000000', 1, 0, 'oss', NULL, '/oss', '', 'default', 4, NULL, NULL, 755305983332319232);
INSERT INTO `view_route_configration` VALUES (755305983776915456, 0, '/oss/index.vue', 1, '2026-09-16 13:56:19.000000', 1, 0, 'oss-manange', 755305983760138240, 'manange', '', 'default', 0, NULL, NULL, 755305983332319232);
INSERT INTO `view_route_configration` VALUES (755305983793692672, 0, 'Layout', 1, '2026-09-16 13:56:19.000000', 1, 0, 'version', NULL, '/version', '', 'default', 5, NULL, NULL, 755305983332319232);
INSERT INTO `view_route_configration` VALUES (755305983948881920, 0, '/admin/version/index.vue', 1, '2026-09-16 13:56:19.000000', 1, 0, 'version-manage', 755305983793692672, 'manage', '', 'default', 0, NULL, NULL, 755305983332319232);
INSERT INTO `view_route_configration` VALUES (756378452642234368, 0, 'Layout', 1, '2026-09-19 12:57:56.000000', 1, 0, 'view.menu.admin.title', NULL, '/admin', '/admin/user', 'default', 0, NULL, NULL, 756378452625457152);
INSERT INTO `view_route_configration` VALUES (756378452696760320, 0, '/admin/user/index.vue', 1, '2026-09-19 12:57:56.000000', 1, 0, 'view.menu.admin.user', 756378452642234368, 'user', NULL, 'default', 0, NULL, NULL, 756378452625457152);
INSERT INTO `view_route_configration` VALUES (756378452709343232, 0, '/admin/role/index.vue', 1, '2026-09-19 12:57:56.000000', 1, 0, 'view.menu.admin.role', 756378452642234368, 'role', NULL, 'default', 1, NULL, NULL, 756378452625457152);
INSERT INTO `view_route_configration` VALUES (756378452721926144, 0, '/admin/company/index.vue', 1, '2026-09-19 12:57:56.000000', 1, 0, 'view.menu.admin.company', 756378452642234368, 'company', NULL, 'default', 2, NULL, NULL, 756378452625457152);
INSERT INTO `view_route_configration` VALUES (756378452738703360, 0, '/admin/role_group/index.vue', 1, '2026-09-19 12:57:56.000000', 1, 0, 'view.menu.admin.role.group', 756378452642234368, 'role-group', NULL, 'default', 3, NULL, NULL, 756378452625457152);
INSERT INTO `view_route_configration` VALUES (756378452751286272, 0, '/admin/external-identity/index.vue', 1, '2026-09-19 12:57:56.000000', 1, 0, 'Ex-id', 756378452642234368, 'Ex-id', '', 'default', 4, NULL, NULL, 756378452625457152);
INSERT INTO `view_route_configration` VALUES (756378452776452096, 0, '/push/notify-admin/index.vue', 1, '2026-09-19 12:57:56.000000', 1, 0, 'message-notice', 756378452642234368, 'message-notice', '', 'default', 5, NULL, NULL, 756378452625457152);
INSERT INTO `view_route_configration` VALUES (756378452801617920, 0, 'Layout', 1, '2026-09-19 12:57:56.000000', 1, 0, 'route-manager', NULL, '/route-manager', '', 'default', 1, NULL, NULL, 756378452625457152);
INSERT INTO `view_route_configration` VALUES (756378452822589440, 0, '/admin/route-manager/index.vue', 1, '2026-09-19 12:57:56.000000', 1, 0, 'routeManager', 756378452801617920, 'index', '', 'default', 0, NULL, NULL, 756378452625457152);
INSERT INTO `view_route_configration` VALUES (756378452835172352, 0, 'Layout', 1, '2026-09-19 12:57:56.000000', 1, 0, 'view.menu.develop', NULL, '/develop', '/develop/form-manager', 'default', 2, NULL, NULL, 756378452625457152);
INSERT INTO `view_route_configration` VALUES (756378452847755264, 0, '/form/FormManager.vue', 1, '2026-09-19 12:57:56.000000', 1, 0, 'view.menu.develop.form.manager', 756378452835172352, 'form-manager', NULL, 'default', 0, NULL, NULL, 756378452625457152);
INSERT INTO `view_route_configration` VALUES (756378452856143872, 0, '/form/FormCreator.vue', 1, '2026-09-19 12:57:56.000000', 1, 0, 'view.menu.develop.form.create', 756378452835172352, 'form', NULL, 'default', 1, NULL, NULL, 756378452625457152);
INSERT INTO `view_route_configration` VALUES (756378452872921088, 0, '/admin/dic/index.vue', 1, '2026-09-19 12:57:56.000000', 1, 0, 'view.menu.develop.dic', 756378452835172352, 'dic', NULL, 'default', 2, NULL, NULL, 756378452625457152);
INSERT INTO `view_route_configration` VALUES (756378452885504000, 0, '/admin/generic-api/index.vue', 1, '2026-09-19 12:57:56.000000', 1, 0, 'view.menu.develop.generic.api', 756378452835172352, 'generic-api', NULL, 'default', 3, NULL, NULL, 756378452625457152);
INSERT INTO `view_route_configration` VALUES (756378452898086912, 0, '/admin/systemlog/index.vue', 1, '2026-09-19 12:57:56.000000', 1, 0, 'view.menu.develop.systemlog', 756378452835172352, 'systemlog', NULL, 'default', 4, NULL, NULL, 756378452625457152);
INSERT INTO `view_route_configration` VALUES (756378452914864128, 0, '/cache/index.vue', 1, '2026-09-19 12:57:56.000000', 1, 0, 'view.menu.develop.cache', 756378452835172352, 'cache', NULL, 'default', 5, NULL, NULL, 756378452625457152);
INSERT INTO `view_route_configration` VALUES (756378452923252736, 0, '/data/DataHandle.vue', 1, '2026-09-19 12:57:56.000000', 1, 0, 'view.menu.data.handle', 756378452835172352, 'data-handle', '', 'default', 6, NULL, NULL, 756378452625457152);
INSERT INTO `view_route_configration` VALUES (756378452940029952, 0, 'Layout', 1, '2026-09-19 12:57:56.000000', 1, 0, 'view.menu.common', NULL, '/common', '/common/user', 'default', 3, NULL, NULL, 756378452625457152);
INSERT INTO `view_route_configration` VALUES (756378452948418560, 0, '/user/index.vue', 1, '2026-09-19 12:57:56.000000', 1, 0, 'view.menu.common.user', 756378452940029952, 'user', NULL, 'default', 0, NULL, NULL, 756378452625457152);
INSERT INTO `view_route_configration` VALUES (756378452956807168, 0, 'Layout', 1, '2026-09-19 12:57:56.000000', 1, 0, 'oss', NULL, '/oss', '', 'default', 4, NULL, NULL, 756378452625457152);
INSERT INTO `view_route_configration` VALUES (756378452969390080, 0, '/oss/index.vue', 1, '2026-09-19 12:57:56.000000', 1, 0, 'oss-manange', 756378452956807168, 'manange', '', 'default', 0, NULL, NULL, 756378452625457152);
INSERT INTO `view_route_configration` VALUES (756378452981972992, 0, 'Layout', 1, '2026-09-19 12:57:56.000000', 1, 0, 'version', NULL, '/version', '', 'default', 5, NULL, NULL, 756378452625457152);
INSERT INTO `view_route_configration` VALUES (756378452990361600, 0, '/admin/version/index.vue', 1, '2026-09-19 12:57:56.000000', 1, 0, 'version-manage', 756378452981972992, 'manage', '', 'default', 0, NULL, NULL, 756378452625457152);
INSERT INTO `view_route_configration` VALUES (758238409310339072, 0, 'Layout', 1, '2026-09-24 16:08:44.000000', 1, 0, 'view.menu.admin.title', NULL, '/admin', '/admin/user', 'default', 0, NULL, NULL, 758238409289367552);
INSERT INTO `view_route_configration` VALUES (758238409369059328, 0, '/admin/user/index.vue', 1, '2026-09-24 16:08:44.000000', 1, 0, 'view.menu.admin.user', 758238409310339072, 'user', NULL, 'default', 0, NULL, NULL, 758238409289367552);
INSERT INTO `view_route_configration` VALUES (758238409385836544, 0, '/admin/role/index.vue', 1, '2026-09-24 16:08:44.000000', 1, 0, 'view.menu.admin.role', 758238409310339072, 'role', NULL, 'default', 1, NULL, NULL, 758238409289367552);
INSERT INTO `view_route_configration` VALUES (758238409406808064, 0, '/admin/company/index.vue', 1, '2026-09-24 16:08:44.000000', 1, 0, 'view.menu.admin.company', 758238409310339072, 'company', NULL, 'default', 2, NULL, NULL, 758238409289367552);
INSERT INTO `view_route_configration` VALUES (758238409427779584, 0, '/admin/role_group/index.vue', 1, '2026-09-24 16:08:44.000000', 1, 0, 'view.menu.admin.role.group', 758238409310339072, 'role-group', NULL, 'default', 3, NULL, NULL, 758238409289367552);
INSERT INTO `view_route_configration` VALUES (758238409444556800, 0, '/admin/external-identity/index.vue', 1, '2026-09-24 16:08:44.000000', 1, 0, 'Ex-id', 758238409310339072, 'Ex-id', '', 'default', 4, NULL, NULL, 758238409289367552);
INSERT INTO `view_route_configration` VALUES (758238409469722624, 0, '/push/notify-admin/index.vue', 1, '2026-09-24 16:08:44.000000', 1, 0, 'message-notice', 758238409310339072, 'message-notice', '', 'default', 5, NULL, NULL, 758238409289367552);
INSERT INTO `view_route_configration` VALUES (758238409494888448, 0, 'Layout', 1, '2026-09-24 16:08:44.000000', 1, 0, 'route-manager', NULL, '/route-manager', '', 'default', 1, NULL, NULL, 758238409289367552);
INSERT INTO `view_route_configration` VALUES (758238409515859968, 0, '/admin/route-manager/index.vue', 1, '2026-09-24 16:08:44.000000', 1, 0, 'routeManager', 758238409494888448, 'index', '', 'default', 0, NULL, NULL, 758238409289367552);
INSERT INTO `view_route_configration` VALUES (758238409536831488, 0, 'Layout', 1, '2026-09-24 16:08:44.000000', 1, 0, 'view.menu.develop', NULL, '/develop', '/develop/form-manager', 'default', 2, NULL, NULL, 758238409289367552);
INSERT INTO `view_route_configration` VALUES (758238409566191616, 0, '/form/FormManager.vue', 1, '2026-09-24 16:08:44.000000', 1, 0, 'view.menu.develop.form.manager', 758238409536831488, 'form-manager', NULL, 'default', 0, NULL, NULL, 758238409289367552);
INSERT INTO `view_route_configration` VALUES (758238409599746048, 0, '/form/FormCreator.vue', 1, '2026-09-24 16:08:44.000000', 1, 0, 'view.menu.develop.form.create', 758238409536831488, 'form', NULL, 'default', 1, NULL, NULL, 758238409289367552);
INSERT INTO `view_route_configration` VALUES (758238409620717568, 0, '/admin/dic/index.vue', 1, '2026-09-24 16:08:44.000000', 1, 0, 'view.menu.develop.dic', 758238409536831488, 'dic', NULL, 'default', 2, NULL, NULL, 758238409289367552);
INSERT INTO `view_route_configration` VALUES (758238409641689088, 0, '/admin/generic-api/index.vue', 1, '2026-09-24 16:08:44.000000', 1, 0, 'view.menu.develop.generic.api', 758238409536831488, 'generic-api', NULL, 'default', 3, NULL, NULL, 758238409289367552);
INSERT INTO `view_route_configration` VALUES (758238409746546688, 0, '/admin/systemlog/index.vue', 1, '2026-09-24 16:08:44.000000', 1, 0, 'view.menu.develop.systemlog', 758238409536831488, 'systemlog', NULL, 'default', 4, NULL, NULL, 758238409289367552);
INSERT INTO `view_route_configration` VALUES (758238409775906816, 0, '/cache/index.vue', 1, '2026-09-24 16:08:44.000000', 1, 0, 'view.menu.develop.cache', 758238409536831488, 'cache', NULL, 'default', 5, NULL, NULL, 758238409289367552);
INSERT INTO `view_route_configration` VALUES (758238409792684032, 0, '/data/DataHandle.vue', 1, '2026-09-24 16:08:44.000000', 1, 0, 'view.menu.data.handle', 758238409536831488, 'data-handle', '', 'default', 6, NULL, NULL, 758238409289367552);
INSERT INTO `view_route_configration` VALUES (758238409809461248, 0, '/admin/third-party/index.vue', 1, '2026-09-24 16:08:44.000000', 1, 0, 'view.menu.develop.third-party', 758238409536831488, 'third-party', '', 'default', 7, NULL, NULL, 758238409289367552);
INSERT INTO `view_route_configration` VALUES (758238409834627072, 0, 'Layout', 1, '2026-09-24 16:08:44.000000', 1, 0, 'view.menu.common', NULL, '/common', '/common/user', 'default', 3, NULL, NULL, 758238409289367552);
INSERT INTO `view_route_configration` VALUES (758238409851404288, 0, '/user/index.vue', 1, '2026-09-24 16:08:44.000000', 1, 0, 'view.menu.common.user', 758238409834627072, 'user', NULL, 'default', 0, NULL, NULL, 758238409289367552);
INSERT INTO `view_route_configration` VALUES (758238409863987200, 0, 'Layout', 1, '2026-09-24 16:08:44.000000', 1, 0, 'oss', NULL, '/oss', '', 'default', 4, NULL, NULL, 758238409289367552);
INSERT INTO `view_route_configration` VALUES (758238409889153024, 0, '/oss/index.vue', 1, '2026-09-24 16:08:44.000000', 1, 0, 'oss-manange', 758238409863987200, 'manange', '', 'default', 0, NULL, NULL, 758238409289367552);
INSERT INTO `view_route_configration` VALUES (758238409901735936, 0, 'Layout', 1, '2026-09-24 16:08:44.000000', 1, 0, 'version', NULL, '/version', '', 'default', 5, NULL, NULL, 758238409289367552);
INSERT INTO `view_route_configration` VALUES (758238409918513152, 0, '/admin/version/index.vue', 1, '2026-09-24 16:08:44.000000', 1, 0, 'version-manage', 758238409901735936, 'manage', '', 'default', 0, NULL, NULL, 758238409289367552);
INSERT INTO `view_route_configration` VALUES (771996112490856448, 0, 'Layout', 1, '2026-11-01 15:16:56.211000', 1, 0, 'view.menu.admin.title', NULL, '/admin', '/admin/user', 'default', 0, NULL, NULL, 771996112461496320);
INSERT INTO `view_route_configration` VALUES (771996112591519744, 0, '/admin/user/index.vue', 1, '2026-11-01 15:16:56.236000', 1, 0, 'view.menu.admin.user', 771996112490856448, 'user', NULL, 'default', 0, NULL, NULL, 771996112461496320);
INSERT INTO `view_route_configration` VALUES (771996112641851392, 0, '/admin/role/index.vue', 1, '2026-11-01 15:16:56.248000', 1, 0, 'view.menu.admin.role', 771996112490856448, 'role', NULL, 'default', 1, NULL, NULL, 771996112461496320);
INSERT INTO `view_route_configration` VALUES (771996112692183040, 0, '/admin/company/index.vue', 1, '2026-11-01 15:16:56.260000', 1, 0, 'view.menu.admin.company', 771996112490856448, 'company', NULL, 'default', 2, NULL, NULL, 771996112461496320);
INSERT INTO `view_route_configration` VALUES (771996112738320384, 0, '/admin/role_group/index.vue', 1, '2026-11-01 15:16:56.270000', 1, 0, 'view.menu.admin.role.group', 771996112490856448, 'role-group', NULL, 'default', 3, NULL, NULL, 771996112461496320);
INSERT INTO `view_route_configration` VALUES (771996112780263424, 0, '/admin/external-identity/index.vue', 1, '2026-11-01 15:16:56.281000', 1, 0, 'Ex-id', 771996112490856448, 'Ex-id', '', 'default', 4, NULL, NULL, 771996112461496320);
INSERT INTO `view_route_configration` VALUES (771996112843177984, 0, '/push/notify-admin/index.vue', 1, '2026-11-01 15:16:56.296000', 1, 0, 'message-notice', 771996112490856448, 'message-notice', '', 'default', 5, NULL, NULL, 771996112461496320);
INSERT INTO `view_route_configration` VALUES (771996112897703936, 0, 'Layout', 1, '2026-11-01 15:16:56.309000', 1, 0, 'route-manager', NULL, '/route-manager', '', 'default', 1, NULL, NULL, 771996112461496320);
INSERT INTO `view_route_configration` VALUES (771996112969007104, 0, '/admin/route-manager/index.vue', 1, '2026-11-01 15:16:56.326000', 1, 0, 'routeManager', 771996112897703936, 'index', '', 'default', 0, NULL, NULL, 771996112461496320);
INSERT INTO `view_route_configration` VALUES (771996113010950144, 0, 'Layout', 1, '2026-11-01 15:16:56.335000', 1, 0, 'view.menu.develop', NULL, '/develop', '/develop/form-manager', 'default', 2, NULL, NULL, 771996112461496320);
INSERT INTO `view_route_configration` VALUES (771996113069670400, 0, '/form/FormManager.vue', 1, '2026-11-01 15:16:56.350000', 1, 0, 'view.menu.develop.form.manager', 771996113010950144, 'form-manager', NULL, 'default', 0, NULL, NULL, 771996112461496320);
INSERT INTO `view_route_configration` VALUES (771996113111613440, 0, '/form/FormCreator.vue', 1, '2026-11-01 15:16:56.360000', 1, 0, 'view.menu.develop.form.create', 771996113010950144, 'form', NULL, 'default', 1, NULL, NULL, 771996112461496320);
INSERT INTO `view_route_configration` VALUES (771996113157750784, 0, '/admin/dic/index.vue', 1, '2026-11-01 15:16:56.371000', 1, 0, 'view.menu.develop.dic', 771996113010950144, 'dic', NULL, 'default', 2, NULL, NULL, 771996112461496320);
INSERT INTO `view_route_configration` VALUES (771996113208082432, 0, '/admin/generic-api/index.vue', 1, '2026-11-01 15:16:56.383000', 1, 0, 'view.menu.develop.generic.api', 771996113010950144, 'generic-api', NULL, 'default', 3, NULL, NULL, 771996112461496320);
INSERT INTO `view_route_configration` VALUES (771996113279385600, 0, '/admin/systemlog/index.vue', 1, '2026-11-01 15:16:56.400000', 1, 0, 'view.menu.develop.systemlog', 771996113010950144, 'systemlog', NULL, 'default', 4, NULL, NULL, 771996112461496320);
INSERT INTO `view_route_configration` VALUES (771996113317134336, 0, '/cache/index.vue', 1, '2026-11-01 15:16:56.409000', 1, 0, 'view.menu.develop.cache', 771996113010950144, 'cache', NULL, 'default', 5, NULL, NULL, 771996112461496320);
INSERT INTO `view_route_configration` VALUES (771996113363271680, 0, '/data/DataHandle.vue', 1, '2026-11-01 15:16:56.420000', 1, 0, 'view.menu.data.handle', 771996113010950144, 'data-handle', '', 'default', 6, NULL, NULL, 771996112461496320);
INSERT INTO `view_route_configration` VALUES (771996113405214720, 0, '/admin/third-party/index.vue', 1, '2026-11-01 15:16:56.430000', 1, 0, 'view.menu.develop.third-party', 771996113010950144, 'third-party', '', 'default', 7, NULL, NULL, 771996112461496320);
INSERT INTO `view_route_configration` VALUES (771996113463934976, 0, 'Layout', 1, '2026-11-01 15:16:56.443000', 1, 0, 'view.menu.common', NULL, '/common', '/common/user', 'default', 3, NULL, NULL, 771996112461496320);
INSERT INTO `view_route_configration` VALUES (771996113497489408, 0, '/user/index.vue', 1, '2026-11-01 15:16:56.452000', 1, 0, 'view.menu.common.user', 771996113463934976, 'user', NULL, 'default', 0, NULL, NULL, 771996112461496320);
INSERT INTO `view_route_configration` VALUES (771996113543626752, 0, 'Layout', 1, '2026-11-01 15:16:56.463000', 1, 0, 'oss', NULL, '/oss', '', 'default', 4, NULL, NULL, 771996112461496320);
INSERT INTO `view_route_configration` VALUES (771996113602347008, 0, '/oss/index.vue', 1, '2026-11-01 15:16:56.477000', 1, 0, 'oss-manange', 771996113543626752, 'manange', '', 'default', 0, NULL, NULL, 771996112461496320);
INSERT INTO `view_route_configration` VALUES (771996113640095744, 0, 'Layout', 1, '2026-11-01 15:16:56.486000', 1, 0, 'version', NULL, '/version', '', 'default', 5, NULL, NULL, 771996112461496320);
INSERT INTO `view_route_configration` VALUES (771996113698816000, 0, '/admin/version/index.vue', 1, '2026-11-01 15:16:56.499000', 1, 0, 'version-manage', 771996113640095744, 'manage', '', 'default', 0, NULL, NULL, 771996112461496320);
INSERT INTO `view_route_configration` VALUES (773366535451312128, 0, 'Layout', 1, '2026-11-05 10:02:30.506000', 1, 0, 'view.menu.admin.title', NULL, '/admin', '/admin/user', 'default', 0, NULL, NULL, 773366535438729216);
INSERT INTO `view_route_configration` VALUES (773366535480672256, 0, '/admin/user/index.vue', 1, '2026-11-05 10:02:30.514000', 1, 0, 'view.menu.admin.user', 773366535451312128, 'user', NULL, 'default', 0, NULL, NULL, 773366535438729216);
INSERT INTO `view_route_configration` VALUES (773366535493255168, 0, '/admin/role/index.vue', 1, '2026-11-05 10:02:30.517000', 1, 0, 'view.menu.admin.role', 773366535451312128, 'role', NULL, 'default', 1, NULL, NULL, 773366535438729216);
INSERT INTO `view_route_configration` VALUES (773366535505838080, 0, '/admin/company/index.vue', 1, '2026-11-05 10:02:30.520000', 1, 0, 'view.menu.admin.company', 773366535451312128, 'company', NULL, 'default', 2, NULL, NULL, 773366535438729216);
INSERT INTO `view_route_configration` VALUES (773366535518420992, 0, '/admin/role_group/index.vue', 1, '2026-11-05 10:02:30.523000', 1, 0, 'view.menu.admin.role.group', 773366535451312128, 'role-group', NULL, 'default', 3, NULL, NULL, 773366535438729216);
INSERT INTO `view_route_configration` VALUES (773366535535198208, 0, '/admin/external-identity/index.vue', 1, '2026-11-05 10:02:30.526000', 1, 0, 'Ex-id', 773366535451312128, 'Ex-id', '', 'default', 4, NULL, NULL, 773366535438729216);
INSERT INTO `view_route_configration` VALUES (773366535551975424, 0, '/push/notify-admin/index.vue', 1, '2026-11-05 10:02:30.531000', 1, 0, 'message-notice', 773366535451312128, 'message-notice', '', 'default', 5, NULL, NULL, 773366535438729216);
INSERT INTO `view_route_configration` VALUES (773366535568752640, 0, 'Layout', 1, '2026-11-05 10:02:30.535000', 1, 0, 'route-manager', NULL, '/route-manager', '', 'default', 1, NULL, NULL, 773366535438729216);
INSERT INTO `view_route_configration` VALUES (773366535593918464, 0, '/admin/route-manager/index.vue', 1, '2026-11-05 10:02:30.541000', 1, 0, 'routeManager', 773366535568752640, 'index', '', 'default', 0, NULL, NULL, 773366535438729216);
INSERT INTO `view_route_configration` VALUES (773366535606501376, 0, 'Layout', 1, '2026-11-05 10:02:30.544000', 1, 0, 'view.menu.develop', NULL, '/develop', '/develop/form-manager', 'default', 2, NULL, NULL, 773366535438729216);
INSERT INTO `view_route_configration` VALUES (773366535623278592, 0, '/form/FormManager.vue', 1, '2026-11-05 10:02:30.548000', 1, 0, 'view.menu.develop.form.manager', 773366535606501376, 'form-manager', NULL, 'default', 0, NULL, NULL, 773366535438729216);
INSERT INTO `view_route_configration` VALUES (773366535635861504, 0, '/form/FormCreator.vue', 1, '2026-11-05 10:02:30.551000', 1, 0, 'view.menu.develop.form.create', 773366535606501376, 'form', NULL, 'default', 1, NULL, NULL, 773366535438729216);
INSERT INTO `view_route_configration` VALUES (773366535648444416, 0, '/admin/dic/index.vue', 1, '2026-11-05 10:02:30.554000', 1, 0, 'view.menu.develop.dic', 773366535606501376, 'dic', NULL, 'default', 2, NULL, NULL, 773366535438729216);
INSERT INTO `view_route_configration` VALUES (773366535665221632, 0, '/admin/generic-api/index.vue', 1, '2026-11-05 10:02:30.558000', 1, 0, 'view.menu.develop.generic.api', 773366535606501376, 'generic-api', NULL, 'default', 3, NULL, NULL, 773366535438729216);
INSERT INTO `view_route_configration` VALUES (773366535690387456, 0, '/admin/systemlog/index.vue', 1, '2026-11-05 10:02:30.564000', 1, 0, 'view.menu.develop.systemlog', 773366535606501376, 'systemlog', NULL, 'default', 4, NULL, NULL, 773366535438729216);
INSERT INTO `view_route_configration` VALUES (773366535707164672, 0, '/cache/index.vue', 1, '2026-11-05 10:02:30.568000', 1, 0, 'view.menu.develop.cache', 773366535606501376, 'cache', NULL, 'default', 5, NULL, NULL, 773366535438729216);
INSERT INTO `view_route_configration` VALUES (773366535723941888, 0, '/data/DataHandle.vue', 1, '2026-11-05 10:02:30.572000', 1, 0, 'view.menu.data.handle', 773366535606501376, 'data-handle', '', 'default', 6, NULL, NULL, 773366535438729216);
INSERT INTO `view_route_configration` VALUES (773366535740719104, 0, '/admin/third-party/index.vue', 1, '2026-11-05 10:02:30.576000', 1, 0, 'view.menu.develop.third-party', 773366535606501376, 'third-party', '', 'default', 7, NULL, NULL, 773366535438729216);
INSERT INTO `view_route_configration` VALUES (773366535761690624, 0, '/iframe/index.vue', 1, '2026-11-05 10:02:30.580000', 1, 0, 'xxl-job', 773366535606501376, 'xxl-job', 'http://127.0.0.1:9898/xxl-job-admin', 'default', 8, NULL, NULL, 773366535438729216);
INSERT INTO `view_route_configration` VALUES (773366535774273536, 0, 'Layout', 1, '2026-11-05 10:02:30.584000', 1, 0, 'view.menu.common', NULL, '/common', '/common/user', 'default', 3, NULL, NULL, 773366535438729216);
INSERT INTO `view_route_configration` VALUES (773366535786856448, 0, '/user/index.vue', 1, '2026-11-05 10:02:30.587000', 1, 0, 'view.menu.common.user', 773366535774273536, 'user', NULL, 'default', 0, NULL, NULL, 773366535438729216);
INSERT INTO `view_route_configration` VALUES (773366535807827968, 0, 'Layout', 1, '2026-11-05 10:02:30.592000', 1, 0, 'oss', NULL, '/oss', '', 'default', 4, NULL, NULL, 773366535438729216);
INSERT INTO `view_route_configration` VALUES (773366535828799488, 0, '/oss/index.vue', 1, '2026-11-05 10:02:30.597000', 1, 0, 'oss-manange', 773366535807827968, 'manange', '', 'default', 0, NULL, NULL, 773366535438729216);
INSERT INTO `view_route_configration` VALUES (773366535845576704, 0, 'Layout', 1, '2026-11-05 10:02:30.601000', 1, 0, 'version', NULL, '/version', '', 'default', 5, NULL, NULL, 773366535438729216);
INSERT INTO `view_route_configration` VALUES (773366535866548224, 0, '/admin/version/index.vue', 1, '2026-11-05 10:02:30.606000', 1, 0, 'version-manage', 773366535845576704, 'manage', '', 'default', 0, NULL, NULL, 773366535438729216);
INSERT INTO `view_route_configration` VALUES (773376290236399616, 0, 'Layout', 1, '2026-11-05 10:41:16.228000', 1, 0, 'view.menu.admin.title', NULL, '/admin', '/admin/user', 'default', 0, NULL, NULL, 773376290223816704);
INSERT INTO `view_route_configration` VALUES (773376290265759744, 0, '/admin/user/index.vue', 1, '2026-11-05 10:41:16.236000', 1, 0, 'view.menu.admin.user', 773376290236399616, 'user', NULL, 'default', 0, NULL, NULL, 773376290223816704);
INSERT INTO `view_route_configration` VALUES (773376290282536960, 0, '/admin/role/index.vue', 1, '2026-11-05 10:41:16.240000', 1, 0, 'view.menu.admin.role', 773376290236399616, 'role', NULL, 'default', 1, NULL, NULL, 773376290223816704);
INSERT INTO `view_route_configration` VALUES (773376290290925568, 0, '/admin/company/index.vue', 1, '2026-11-05 10:41:16.242000', 1, 0, 'view.menu.admin.company', 773376290236399616, 'company', NULL, 'default', 2, NULL, NULL, 773376290223816704);
INSERT INTO `view_route_configration` VALUES (773376290303508480, 0, '/admin/role_group/index.vue', 1, '2026-11-05 10:41:16.245000', 1, 0, 'view.menu.admin.role.group', 773376290236399616, 'role-group', NULL, 'default', 3, NULL, NULL, 773376290223816704);
INSERT INTO `view_route_configration` VALUES (773376290316091392, 0, '/admin/external-identity/index.vue', 1, '2026-11-05 10:41:16.248000', 1, 0, 'Ex-id', 773376290236399616, 'Ex-id', '', 'default', 4, NULL, NULL, 773376290223816704);
INSERT INTO `view_route_configration` VALUES (773376290332868608, 0, '/push/notify-admin/index.vue', 1, '2026-11-05 10:41:16.252000', 1, 0, 'message-notice', 773376290236399616, 'message-notice', '', 'default', 5, NULL, NULL, 773376290223816704);
INSERT INTO `view_route_configration` VALUES (773376290349645824, 0, 'Layout', 1, '2026-11-05 10:41:16.256000', 1, 0, 'route-manager', NULL, '/route-manager', '', 'default', 1, NULL, NULL, 773376290223816704);
INSERT INTO `view_route_configration` VALUES (773376290366423040, 0, '/admin/route-manager/index.vue', 1, '2026-11-05 10:41:16.260000', 1, 0, 'routeManager', 773376290349645824, 'index', '', 'default', 0, NULL, NULL, 773376290223816704);
INSERT INTO `view_route_configration` VALUES (773376290379005952, 0, 'Layout', 1, '2026-11-05 10:41:16.263000', 1, 0, 'view.menu.develop', NULL, '/develop', '/develop/form-manager', 'default', 2, NULL, NULL, 773376290223816704);
INSERT INTO `view_route_configration` VALUES (773376290408366080, 0, '/form/FormManager.vue', 1, '2026-11-05 10:41:16.270000', 1, 0, 'view.menu.develop.form.manager', 773376290379005952, 'form-manager', NULL, 'default', 0, NULL, NULL, 773376290223816704);
INSERT INTO `view_route_configration` VALUES (773376290420948992, 0, '/form/FormCreator.vue', 1, '2026-11-05 10:41:16.273000', 1, 0, 'view.menu.develop.form.create', 773376290379005952, 'form', NULL, 'default', 1, NULL, NULL, 773376290223816704);
INSERT INTO `view_route_configration` VALUES (773376290433531904, 0, '/admin/dic/index.vue', 1, '2026-11-05 10:41:16.275000', 1, 0, 'view.menu.develop.dic', 773376290379005952, 'dic', NULL, 'default', 2, NULL, NULL, 773376290223816704);
INSERT INTO `view_route_configration` VALUES (773376290441920512, 0, '/admin/generic-api/index.vue', 1, '2026-11-05 10:41:16.277000', 1, 0, 'view.menu.develop.generic.api', 773376290379005952, 'generic-api', NULL, 'default', 3, NULL, NULL, 773376290223816704);
INSERT INTO `view_route_configration` VALUES (773376290450309120, 0, '/admin/systemlog/index.vue', 1, '2026-11-05 10:41:16.280000', 1, 0, 'view.menu.develop.systemlog', 773376290379005952, 'systemlog', NULL, 'default', 4, NULL, NULL, 773376290223816704);
INSERT INTO `view_route_configration` VALUES (773376290475474944, 0, '/cache/index.vue', 1, '2026-11-05 10:41:16.286000', 1, 0, 'view.menu.develop.cache', 773376290379005952, 'cache', NULL, 'default', 5, NULL, NULL, 773376290223816704);
INSERT INTO `view_route_configration` VALUES (773376290488057856, 0, '/data/DataHandle.vue', 1, '2026-11-05 10:41:16.289000', 1, 0, 'view.menu.data.handle', 773376290379005952, 'data-handle', '', 'default', 6, NULL, NULL, 773376290223816704);
INSERT INTO `view_route_configration` VALUES (773376290500640768, 0, '/admin/third-party/index.vue', 1, '2026-11-05 10:41:16.292000', 1, 0, 'view.menu.develop.third-party', 773376290379005952, 'third-party', '', 'default', 7, NULL, NULL, 773376290223816704);
INSERT INTO `view_route_configration` VALUES (773376290513223681, 0, '/iframe/index.vue', 1, '2026-11-05 10:41:16.295000', 1, 0, 'xxl-job', 773376290379005952, 'xxl-job', '', 'default', 8, NULL, NULL, 773376290223816704);
INSERT INTO `view_route_configration` VALUES (773376290534195200, 0, 'Layout', 1, '2026-11-05 10:41:16.299000', 1, 0, 'view.menu.common', NULL, '/common', '/common/user', 'default', 3, NULL, NULL, 773376290223816704);
INSERT INTO `view_route_configration` VALUES (773376290542583808, 0, '/user/index.vue', 1, '2026-11-05 10:41:16.302000', 1, 0, 'view.menu.common.user', 773376290534195200, 'user', NULL, 'default', 0, NULL, NULL, 773376290223816704);
INSERT INTO `view_route_configration` VALUES (773376290555166720, 0, 'Layout', 1, '2026-11-05 10:41:16.305000', 1, 0, 'oss', NULL, '/oss', '', 'default', 4, NULL, NULL, 773376290223816704);
INSERT INTO `view_route_configration` VALUES (773376290571943936, 0, '/oss/index.vue', 1, '2026-11-05 10:41:16.309000', 1, 0, 'oss-manange', 773376290555166720, 'manange', '', 'default', 0, NULL, NULL, 773376290223816704);
INSERT INTO `view_route_configration` VALUES (773376290580332544, 0, 'Layout', 1, '2026-11-05 10:41:16.311000', 1, 0, 'version', NULL, '/version', '', 'default', 5, NULL, NULL, 773376290223816704);
INSERT INTO `view_route_configration` VALUES (773376290597109760, 0, '/admin/version/index.vue', 1, '2026-11-05 10:41:16.315000', 1, 0, 'version-manage', 773376290580332544, 'manage', '', 'default', 0, NULL, NULL, 773376290223816704);
INSERT INTO `view_route_configration` VALUES (773780005049073664, 0, 'Layout', 1, '2026-11-06 13:25:29.340000', 1, 0, 'view.menu.admin.title', NULL, '/admin', '/admin/user', 'default', 0, NULL, NULL, 773780005032296448);
INSERT INTO `view_route_configration` VALUES (773780005086822400, 0, '/admin/user/index.vue', 1, '2026-11-06 13:25:29.350000', 1, 0, 'view.menu.admin.user', 773780005049073664, 'user', NULL, 'default', 0, NULL, NULL, 773780005032296448);
INSERT INTO `view_route_configration` VALUES (773780005099405312, 0, '/admin/role/index.vue', 1, '2026-11-06 13:25:29.353000', 1, 0, 'view.menu.admin.role', 773780005049073664, 'role', NULL, 'default', 1, NULL, NULL, 773780005032296448);
INSERT INTO `view_route_configration` VALUES (773780005111988224, 0, '/admin/company/index.vue', 1, '2026-11-06 13:25:29.356000', 1, 0, 'view.menu.admin.company', 773780005049073664, 'company', NULL, 'default', 2, NULL, NULL, 773780005032296448);
INSERT INTO `view_route_configration` VALUES (773780005124571136, 0, '/admin/role_group/index.vue', 1, '2026-11-06 13:25:29.359000', 1, 0, 'view.menu.admin.role.group', 773780005049073664, 'role-group', NULL, 'default', 3, NULL, NULL, 773780005032296448);
INSERT INTO `view_route_configration` VALUES (773780005132959744, 0, '/admin/external-identity/index.vue', 1, '2026-11-06 13:25:29.361000', 1, 0, 'Ex-id', 773780005049073664, 'Ex-id', '', 'default', 4, NULL, NULL, 773780005032296448);
INSERT INTO `view_route_configration` VALUES (773780005153931264, 0, '/push/notify-admin/index.vue', 1, '2026-11-06 13:25:29.366000', 1, 0, 'message-notice', 773780005049073664, 'message-notice', '', 'default', 5, NULL, NULL, 773780005032296448);
INSERT INTO `view_route_configration` VALUES (773780005170708480, 0, 'Layout', 1, '2026-11-06 13:25:29.370000', 1, 0, 'route-manager', NULL, '/route-manager', '', 'default', 1, NULL, NULL, 773780005032296448);
INSERT INTO `view_route_configration` VALUES (773780005187485696, 0, '/admin/route-manager/index.vue', 1, '2026-11-06 13:25:29.374000', 1, 0, 'routeManager', 773780005170708480, 'index', '', 'default', 0, NULL, NULL, 773780005032296448);
INSERT INTO `view_route_configration` VALUES (773780005204262912, 0, 'Layout', 1, '2026-11-06 13:25:29.378000', 1, 0, 'view.menu.develop', NULL, '/develop', '/develop/form-manager', 'default', 2, NULL, NULL, 773780005032296448);
INSERT INTO `view_route_configration` VALUES (773780005225234432, 0, '/form/FormManager.vue', 1, '2026-11-06 13:25:29.383000', 1, 0, 'view.menu.develop.form.manager', 773780005204262912, 'form-manager', NULL, 'default', 0, NULL, NULL, 773780005032296448);
INSERT INTO `view_route_configration` VALUES (773780005237817344, 0, '/form/FormCreator.vue', 1, '2026-11-06 13:25:29.386000', 1, 0, 'view.menu.develop.form.create', 773780005204262912, 'form', NULL, 'default', 1, NULL, NULL, 773780005032296448);
INSERT INTO `view_route_configration` VALUES (773780005250400256, 0, '/admin/dic/index.vue', 1, '2026-11-06 13:25:29.389000', 1, 0, 'view.menu.develop.dic', 773780005204262912, 'dic', NULL, 'default', 2, NULL, NULL, 773780005032296448);
INSERT INTO `view_route_configration` VALUES (773780005262983168, 0, '/admin/generic-api-config/index.vue', 1, '2026-11-06 13:25:29.392000', 1, 0, 'view.menu.develop.generic.api', 773780005204262912, 'generic-api', '', 'default', 3, NULL, NULL, 773780005032296448);
INSERT INTO `view_route_configration` VALUES (773780005275566080, 0, '/admin/systemlog/index.vue', 1, '2026-11-06 13:25:29.395000', 1, 0, 'view.menu.develop.systemlog', 773780005204262912, 'systemlog', NULL, 'default', 4, NULL, NULL, 773780005032296448);
INSERT INTO `view_route_configration` VALUES (773780005288148992, 0, '/cache/index.vue', 1, '2026-11-06 13:25:29.398000', 1, 0, 'view.menu.develop.cache', 773780005204262912, 'cache', NULL, 'default', 5, NULL, NULL, 773780005032296448);
INSERT INTO `view_route_configration` VALUES (773780005296537600, 0, '/data/DataHandle.vue', 1, '2026-11-06 13:25:29.400000', 1, 0, 'view.menu.data.handle', 773780005204262912, 'data-handle', '', 'default', 6, NULL, NULL, 773780005032296448);
INSERT INTO `view_route_configration` VALUES (773780005313314816, 0, '/admin/third-party/index.vue', 1, '2026-11-06 13:25:29.404000', 1, 0, 'view.menu.develop.third-party', 773780005204262912, 'third-party', '', 'default', 7, NULL, NULL, 773780005032296448);
INSERT INTO `view_route_configration` VALUES (773780005330092032, 0, '/iframe/index.vue', 1, '2026-11-06 13:25:29.408000', 1, 0, 'xxl-job', 773780005204262912, 'xxl-job', '', 'default', 8, NULL, NULL, 773780005032296448);
INSERT INTO `view_route_configration` VALUES (773780005342674944, 0, 'Layout', 1, '2026-11-06 13:25:29.411000', 1, 0, 'view.menu.common', NULL, '/common', '/common/user', 'default', 3, NULL, NULL, 773780005032296448);
INSERT INTO `view_route_configration` VALUES (773780005355257856, 0, '/user/index.vue', 1, '2026-11-06 13:25:29.414000', 1, 0, 'view.menu.common.user', 773780005342674944, 'user', NULL, 'default', 0, NULL, NULL, 773780005032296448);
INSERT INTO `view_route_configration` VALUES (773780005372035072, 0, 'Layout', 1, '2026-11-06 13:25:29.418000', 1, 0, 'oss', NULL, '/oss', '', 'default', 4, NULL, NULL, 773780005032296448);
INSERT INTO `view_route_configration` VALUES (773780005388812288, 0, '/oss/index.vue', 1, '2026-11-06 13:25:29.422000', 1, 0, 'oss-manange', 773780005372035072, 'manange', '', 'default', 0, NULL, NULL, 773780005032296448);
INSERT INTO `view_route_configration` VALUES (773780005401395200, 0, 'Layout', 1, '2026-11-06 13:25:29.425000', 1, 0, 'version', NULL, '/version', '', 'default', 5, NULL, NULL, 773780005032296448);
INSERT INTO `view_route_configration` VALUES (773780005418172416, 0, '/admin/version/index.vue', 1, '2026-11-06 13:25:29.429000', 1, 0, 'version-manage', 773780005401395200, 'manage', '', 'default', 0, NULL, NULL, 773780005032296448);

-- ----------------------------
-- Table structure for view_route_meta
-- ----------------------------
DROP TABLE IF EXISTS `view_route_meta`;
CREATE TABLE `view_route_meta`  (
  `id` bigint NOT NULL COMMENT '主键ID（雪花算法生成）',
  `active_menu` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '激活菜单路径（高亮指定的菜单项）',
  `breadcrumb` int NULL DEFAULT NULL COMMENT '是否显示面包屑（0:否，1:是）',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `flag` int NULL DEFAULT NULL COMMENT '状态标志（0:禁用，1:启用）',
  `icon` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '路由图标（菜单图标名称）',
  `route_group` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '路由分组（与路由配置的 route_group 对应）',
  `route_id` bigint NOT NULL COMMENT '关联的路由ID（援引 view_route_configration.id）',
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '路由标题（显示在菜单和面包屑中）',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  `link` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '内嵌链接地址（内嵌链接组件 iframe 的 src，仅 http/https）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_route_id`(`route_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '视图路由元数据表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of view_route_meta
-- ----------------------------
INSERT INTO `view_route_meta` VALUES (741849291089248256, NULL, 1, NULL, 1, 'el-icon-user', 'default', 741849291038916608, 'view.menu.common', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741849291147968512, NULL, 1, NULL, 1, 'el-icon-user', 'default', 741849291122802688, 'view.menu.common.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741849291231854592, '', 1, NULL, 1, 'el-icon-folder-opened', 'default', 741849291194105856, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741849291336712192, '', 1, NULL, 1, 'el-icon-document-add', 'default', 741849291311546368, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741849291550621696, NULL, 1, NULL, 1, 'el-icon-setting', 'default', 741849291508678656, 'view.menu.admin.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741849292007800832, NULL, 1, NULL, 1, 'el-icon-user', 'default', 741849291978440704, 'view.menu.admin.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741849292167184384, NULL, 1, NULL, 1, 'el-icon-lock', 'default', 741849292125241344, 'view.menu.admin.role', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741849292313985024, NULL, 1, NULL, 1, 'el-icon-office-building', 'default', 741849292204933120, 'view.menu.admin.company', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741849292406259712, NULL, 1, NULL, 1, 'el-icon-s-management', 'default', 741849292368510976, 'view.menu.admin.role.group', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741849292544671744, NULL, 1, NULL, 1, 'el-icon-s-tools', 'default', 741849292435619840, 'view.menu.develop.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741849292712443904, NULL, 1, NULL, 1, 'el-icon-document', 'default', 741849292657917952, 'view.menu.develop.form.manager', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741849292775358464, NULL, 1, NULL, 1, 'el-icon-edit', 'default', 741849292741804032, 'view.menu.develop.form.create', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741849293089931264, NULL, 1, NULL, 1, 'el-icon-notebook-2', 'default', 741849292808912896, 'view.menu.develop.dic', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741849293190594560, NULL, 1, NULL, 1, 'el-icon-connection', 'default', 741849293157040128, 'view.menu.develop.generic.api', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741849293266092032, NULL, 1, NULL, 1, 'el-icon-data-line', 'default', 741849293219954688, 'view.menu.develop.systemlog', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741849293337395200, NULL, 1, NULL, 1, 'el-icon-s-data', 'default', 741849293295452160, 'view.menu.develop.cache', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861855927992320, '', 1, NULL, 1, 'el-icon-folder-opened', 'default', 741861855894437888, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861856032849920, '', 1, NULL, 1, 'el-icon-document-add', 'default', 741861856003489792, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861856104153088, NULL, 1, NULL, 1, 'el-icon-user', 'default', 741861856062210048, 'view.menu.common', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861856188039168, NULL, 1, NULL, 1, 'el-icon-user', 'default', 741861856137707520, 'view.menu.common.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861856246759424, NULL, 1, NULL, 1, 'el-icon-setting', 'default', 741861856217399296, 'view.menu.admin.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861856334839808, NULL, 1, NULL, 1, 'el-icon-user', 'default', 741861856305479680, 'view.menu.admin.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861856422920192, NULL, 1, NULL, 1, 'el-icon-lock', 'default', 741861856380977152, 'view.menu.admin.role', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861856485834752, NULL, 1, NULL, 1, 'el-icon-office-building', 'default', 741861856448086016, 'view.menu.admin.company', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861856548749312, NULL, 1, NULL, 1, 'el-icon-s-management', 'default', 741861856511000576, 'view.menu.admin.role.group', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861856632635392, NULL, 1, NULL, 1, 'el-icon-s-tools', 'default', 741861856578109440, 'view.menu.develop.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861856720715776, NULL, 1, NULL, 1, 'el-icon-document', 'default', 741861856687161344, 'view.menu.develop.form.manager', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861856775241728, NULL, 1, NULL, 1, 'el-icon-edit', 'default', 741861856745881600, 'view.menu.develop.form.create', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861856850739200, NULL, 1, NULL, 1, 'el-icon-notebook-2', 'default', 741861856804601856, 'view.menu.develop.dic', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861856930430976, NULL, 1, NULL, 1, 'el-icon-connection', 'default', 741861856884293632, 'view.menu.develop.generic.api', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861856997539840, NULL, 1, NULL, 1, 'el-icon-data-line', 'default', 741861856963985408, 'view.menu.develop.systemlog', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861857098203136, NULL, 1, NULL, 1, 'el-icon-s-data', 'default', 741861857052065792, 'view.menu.develop.cache', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861863301578752, '', 1, NULL, 1, 'el-icon-folder-opened', 'default', 741861862823428096, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861863427407872, '', 1, NULL, 1, 'el-icon-document-add', 'default', 741861863381270528, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861863616151552, NULL, 1, NULL, 1, 'el-icon-setting', 'default', 741861863565819904, 'view.menu.admin.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861863733592064, NULL, 1, NULL, 1, 'el-icon-user', 'default', 741861863700037632, 'view.menu.admin.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861863804895232, NULL, 1, NULL, 1, 'el-icon-lock', 'default', 741861863758757888, 'view.menu.admin.role', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861863913947136, NULL, 1, NULL, 1, 'el-icon-office-building', 'default', 741861863846838272, 'view.menu.admin.company', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861863968473088, NULL, 1, NULL, 1, 'el-icon-s-management', 'default', 741861863943307264, 'view.menu.admin.role.group', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861864052359168, NULL, 1, NULL, 1, 'el-icon-user', 'default', 741861864022999040, 'view.menu.common', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861864140439552, NULL, 1, NULL, 1, 'el-icon-user', 'default', 741861864081719296, 'view.menu.common.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861864194965504, NULL, 1, NULL, 1, 'el-icon-s-tools', 'default', 741861864169799680, 'view.menu.develop.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861864304017408, NULL, 1, NULL, 1, 'el-icon-document', 'default', 741861864270462976, 'view.menu.develop.form.manager', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861864383709184, NULL, 1, NULL, 1, 'el-icon-edit', 'default', 741861864358543360, 'view.menu.develop.form.create', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861864455012352, NULL, 1, NULL, 1, 'el-icon-notebook-2', 'default', 741861864404680704, 'view.menu.develop.dic', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861864505344000, NULL, 1, NULL, 1, 'el-icon-connection', 'default', 741861864480178176, 'view.menu.develop.generic.api', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861864593424384, NULL, 1, NULL, 1, 'el-icon-data-line', 'default', 741861864564064256, 'view.menu.develop.systemlog', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861864639561728, NULL, 1, NULL, 1, 'el-icon-s-data', 'default', 741861864614395904, 'view.menu.develop.cache', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861866787045376, '', 1, NULL, 1, 'el-icon-folder-opened', 'default', 741861866757685248, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861866887708672, '', 1, NULL, 1, 'el-icon-document-add', 'default', 741861866858348544, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861866971594752, NULL, 1, NULL, 1, 'el-icon-setting', 'default', 741861866946428928, 'view.menu.admin.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861867051286528, NULL, 1, NULL, 1, 'el-icon-user', 'default', 741861867026120704, 'view.menu.admin.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861867105812480, NULL, 1, NULL, 1, 'el-icon-lock', 'default', 741861867076452352, 'view.menu.admin.role', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861867172921344, NULL, 1, NULL, 1, 'el-icon-office-building', 'default', 741861867143561216, 'view.menu.admin.company', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861867240030208, NULL, 1, NULL, 1, 'el-icon-s-management', 'default', 741861867214864384, 'view.menu.admin.role.group', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861867311333376, NULL, 1, NULL, 1, 'el-icon-s-tools', 'default', 741861867281973248, 'view.menu.develop.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861867416190976, NULL, 1, NULL, 1, 'el-icon-document', 'default', 741861867386830848, 'view.menu.develop.form.manager', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861867479105536, NULL, 1, NULL, 1, 'el-icon-edit', 'default', 741861867453939712, 'view.menu.develop.form.create', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861867550408704, NULL, 1, NULL, 1, 'el-icon-notebook-2', 'default', 741861867516854272, 'view.menu.develop.dic', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861867621711872, NULL, 1, NULL, 1, 'el-icon-connection', 'default', 741861867592351744, 'view.menu.develop.generic.api', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861867701403648, NULL, 1, NULL, 1, 'el-icon-data-line', 'default', 741861867663654912, 'view.menu.develop.systemlog', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861867785289728, NULL, 1, NULL, 1, 'el-icon-s-data', 'default', 741861867730763776, 'view.menu.develop.cache', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861867860787200, NULL, 1, NULL, 1, 'el-icon-user', 'default', 741861867823038464, 'view.menu.common', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861867923701760, NULL, 1, NULL, 1, 'el-icon-user', 'default', 741861867898535936, 'view.menu.common.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861879902633984, NULL, 1, NULL, 1, 'el-icon-setting', 'default', 741861879873273856, 'view.menu.admin.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861879982325760, NULL, 1, NULL, 1, 'el-icon-user', 'default', 741861879957159936, 'view.menu.admin.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861880053628928, NULL, 1, NULL, 1, 'el-icon-lock', 'default', 741861880024268800, 'view.menu.admin.role', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861880116543488, NULL, 1, NULL, 1, 'el-icon-office-building', 'default', 741861880091377664, 'view.menu.admin.company', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861880187846656, NULL, 1, NULL, 1, 'el-icon-s-management', 'default', 741861880154292224, 'view.menu.admin.role.group', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861880305287168, '', 1, NULL, 1, 'el-icon-folder-opened', 'default', 741861880271732736, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861880397561856, '', 1, NULL, 1, 'el-icon-document-add', 'default', 741861880368201728, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861880477253632, NULL, 1, NULL, 1, 'el-icon-s-tools', 'default', 741861880435310592, 'view.menu.develop.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861880716328960, NULL, 1, NULL, 1, 'el-icon-document', 'default', 741861880556945408, 'view.menu.develop.form.manager', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861880779243520, NULL, 1, NULL, 1, 'el-icon-edit', 'default', 741861880745689088, 'view.menu.develop.form.create', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861880846352384, NULL, 1, NULL, 1, 'el-icon-notebook-2', 'default', 741861880812797952, 'view.menu.develop.dic', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861880934432768, NULL, 1, NULL, 1, 'el-icon-connection', 'default', 741861880913461248, 'view.menu.develop.generic.api', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861881030901760, NULL, 1, NULL, 1, 'el-icon-data-line', 'default', 741861880980570112, 'view.menu.develop.systemlog', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861881139953664, NULL, 1, NULL, 1, 'el-icon-s-data', 'default', 741861881060261888, 'view.menu.develop.cache', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861881198673920, NULL, 1, NULL, 1, 'el-icon-user', 'default', 741861881173508096, 'view.menu.common', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861881253199872, NULL, 1, NULL, 1, 'el-icon-user', 'default', 741861881228034048, 'view.menu.common.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861925813485568, NULL, 1, NULL, 1, 'el-icon-setting', 'default', 741861925779931136, 'view.menu.admin.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861926006423552, NULL, 1, NULL, 1, 'el-icon-user', 'default', 741861925926731776, 'view.menu.admin.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861926056755200, NULL, 1, NULL, 1, 'el-icon-lock', 'default', 741861926027395072, 'view.menu.admin.role', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861926132252672, NULL, 1, NULL, 1, 'el-icon-office-building', 'default', 741861926094503936, 'view.menu.admin.company', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861926220333056, NULL, 1, NULL, 1, 'el-icon-s-management', 'default', 741861926178390016, 'view.menu.admin.role.group', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861926279053312, '', 1, NULL, 1, 'el-icon-folder-opened', 'default', 741861926253887488, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861926358745088, '', 1, NULL, 1, 'el-icon-document-add', 'default', 741861926333579264, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861926430048256, NULL, 1, NULL, 1, 'el-icon-s-tools', 'default', 741861926404882432, 'view.menu.develop.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861926534905856, NULL, 1, NULL, 1, 'el-icon-document', 'default', 741861926505545728, 'view.menu.develop.form.manager', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861926606209024, NULL, 1, NULL, 1, 'el-icon-edit', 'default', 741861926564265984, 'view.menu.develop.form.create', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861926748815360, NULL, 1, NULL, 1, 'el-icon-notebook-2', 'default', 741861926643957760, 'view.menu.develop.dic', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861926820118528, NULL, 1, NULL, 1, 'el-icon-connection', 'default', 741861926786564096, 'view.menu.develop.generic.api', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861926954336256, NULL, 1, NULL, 1, 'el-icon-data-line', 'default', 741861926849478656, 'view.menu.develop.systemlog', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861927038222336, NULL, 1, NULL, 1, 'el-icon-s-data', 'default', 741861926983696384, 'view.menu.develop.cache', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861927101136896, NULL, 1, NULL, 1, 'el-icon-user', 'default', 741861927059193856, 'view.menu.common', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741861927159857152, NULL, 1, NULL, 1, 'el-icon-user', 'default', 741861927130497024, 'view.menu.common.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741862940889579520, NULL, 1, NULL, 1, 'el-icon-setting', 'default', 741862940847636480, 'view.menu.admin.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741862941011214336, NULL, 1, NULL, 1, 'el-icon-user', 'default', 741862940986048512, 'view.menu.admin.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741862941086711808, NULL, 1, NULL, 1, 'el-icon-lock', 'default', 741862941048963072, 'view.menu.admin.role', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741862941153820672, NULL, 1, NULL, 1, 'el-icon-office-building', 'default', 741862941120266240, 'view.menu.admin.company', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741862941199958016, NULL, 1, NULL, 1, 'el-icon-s-management', 'default', 741862941178986496, 'view.menu.admin.role.group', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741862941275455488, '', 1, NULL, 1, 'el-icon-folder-opened', 'default', 741862941233512448, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741862941409673216, '', 1, NULL, 1, 'el-icon-document-add', 'default', 741862941380313088, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741862941460004864, NULL, 1, NULL, 1, 'el-icon-s-tools', 'default', 741862941434839040, 'view.menu.develop.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741862941548085248, NULL, 1, NULL, 1, 'el-icon-document', 'default', 741862941514530816, 'view.menu.develop.form.manager', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741862941627777024, NULL, 1, NULL, 1, 'el-icon-edit', 'default', 741862941594222592, 'view.menu.develop.form.create', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741862941686497280, NULL, 1, NULL, 1, 'el-icon-notebook-2', 'default', 741862941657137152, 'view.menu.develop.dic', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741862941782966272, NULL, 1, NULL, 1, 'el-icon-connection', 'default', 741862941724246016, 'view.menu.develop.generic.api', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741862941875240960, NULL, 1, NULL, 1, 'el-icon-data-line', 'default', 741862941837492224, 'view.menu.develop.systemlog', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741862941938155520, NULL, 1, NULL, 1, 'el-icon-s-data', 'default', 741862941908795392, 'view.menu.develop.cache', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741862942009458688, NULL, 1, NULL, 1, 'el-icon-user', 'default', 741862941967515648, 'view.menu.common', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741862942210785280, NULL, 1, NULL, 1, 'el-icon-user', 'default', 741862942160453632, 'view.menu.common.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741902418802900992, '', 1, NULL, 1, '', 'default', 741902418752569344, '单据改动扫描配置', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741902418895175680, NULL, 1, NULL, 1, 'el-icon-setting', 'default', 741902418853232640, 'view.menu.admin.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741902419138445312, NULL, 1, NULL, 1, 'el-icon-user', 'default', 741902418991644672, 'view.menu.admin.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741902419255885824, NULL, 1, NULL, 1, 'el-icon-lock', 'default', 741902419226525696, 'view.menu.admin.role', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741902419390103552, NULL, 1, NULL, 1, 'el-icon-office-building', 'default', 741902419356549120, 'view.menu.admin.company', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741902419453018112, NULL, 1, NULL, 1, 'el-icon-s-management', 'default', 741902419419463680, 'view.menu.admin.role.group', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741902419624984576, '', 1, NULL, 1, 'el-icon-folder-opened', 'default', 741902419494961152, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741902419822116864, '', 1, NULL, 1, 'el-icon-document-add', 'default', 741902419704676352, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741902419935363072, NULL, 1, NULL, 1, 'el-icon-s-tools', 'default', 741902419859865600, 'view.menu.develop.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741902420233158656, NULL, 1, NULL, 1, 'el-icon-document', 'default', 741902420199604224, 'view.menu.develop.form.manager', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741902420358987776, NULL, 1, NULL, 1, 'el-icon-edit', 'default', 741902420258324480, 'view.menu.develop.form.create', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741902420434485248, NULL, 1, NULL, 1, 'el-icon-notebook-2', 'default', 741902420396736512, 'view.menu.develop.dic', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741902420614840320, NULL, 1, NULL, 1, 'el-icon-connection', 'default', 741902420472233984, 'view.menu.develop.generic.api', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741902420707115008, NULL, 1, NULL, 1, 'el-icon-data-line', 'default', 741902420660977664, 'view.menu.develop.systemlog', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741902420866498560, NULL, 1, NULL, 1, 'el-icon-s-data', 'default', 741902420736475136, 'view.menu.develop.cache', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741902420954578944, NULL, 1, NULL, 1, 'el-icon-user', 'default', 741902420912635904, 'view.menu.common', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741902421097185280, NULL, 1, NULL, 1, 'el-icon-user', 'default', 741902421067825152, 'view.menu.common.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741902674932269056, '', 1, NULL, 1, '', 'default', 741902674860965888, '单据改动扫描配置', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741902675037126656, NULL, 1, NULL, 1, 'el-icon-setting', 'default', 741902674974212096, 'view.menu.admin.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741902675255230464, NULL, 1, NULL, 1, 'el-icon-user', 'default', 741902675204898816, 'view.menu.admin.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741902675553026048, NULL, 1, NULL, 1, 'el-icon-lock', 'default', 741902675490111488, 'view.menu.admin.role', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741902676127645696, NULL, 1, NULL, 1, 'el-icon-office-building', 'default', 741902675821461504, 'view.menu.admin.company', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741902676249280512, NULL, 1, NULL, 1, 'el-icon-s-management', 'default', 741902676173783040, 'view.menu.admin.role.group', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741902676526104576, '', 1, NULL, 1, 'el-icon-folder-opened', 'default', 741902676421246976, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741902676937146368, '', 1, NULL, 1, 'el-icon-document-add', 'default', 741902676672905216, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741902677046198272, NULL, 1, NULL, 1, 'el-icon-s-tools', 'default', 741902676983283712, 'view.menu.develop.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741902677188804608, NULL, 1, NULL, 1, 'el-icon-document', 'default', 741902677134278656, 'view.menu.develop.form.manager', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741902677343993856, NULL, 1, NULL, 1, 'el-icon-edit', 'default', 741902677264302080, 'view.menu.develop.form.create', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741902677641789440, NULL, 1, NULL, 1, 'el-icon-notebook-2', 'default', 741902677474017280, 'view.menu.develop.dic', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741902677759229952, NULL, 1, NULL, 1, 'el-icon-connection', 'default', 741902677687926784, 'view.menu.develop.generic.api', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741902678002499584, NULL, 1, NULL, 1, 'el-icon-data-line', 'default', 741902677855698944, 'view.menu.develop.systemlog', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741902678258352128, NULL, 1, NULL, 1, 'el-icon-s-data', 'default', 741902678111551488, 'view.menu.develop.cache', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741902678510010368, NULL, 1, NULL, 1, 'el-icon-user', 'default', 741902678296100864, 'view.menu.common', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741902678669393920, NULL, 1, NULL, 1, 'el-icon-user', 'default', 741902678598090752, 'view.menu.common.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741903113203482624, '', 1, NULL, 1, '', 'default', 741903113165733888, '兴昊塑业', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741903113287368704, '', 1, NULL, 1, '', 'default', 741903113253814272, '兴昊ERP单据变动扫描配置', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741903113350283264, NULL, 1, NULL, 1, 'el-icon-setting', 'default', 741903113316728832, 'view.menu.admin.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741903113455140864, NULL, 1, NULL, 1, 'el-icon-user', 'default', 741903113425780736, 'view.menu.admin.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741903113543221248, NULL, 1, NULL, 1, 'el-icon-lock', 'default', 741903113501278208, 'view.menu.admin.role', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741903113622913024, NULL, 1, NULL, 1, 'el-icon-office-building', 'default', 741903113585164288, 'view.menu.admin.company', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741903113778102272, NULL, 1, NULL, 1, 'el-icon-s-management', 'default', 741903113673244672, 'view.menu.admin.role.group', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741903113878765568, '', 1, NULL, 1, 'el-icon-folder-opened', 'default', 741903113811656704, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741903114084286464, '', 1, NULL, 1, 'el-icon-document-add', 'default', 741903114033954816, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741903114163978240, NULL, 1, NULL, 1, 'el-icon-s-tools', 'default', 741903114126229504, 'view.menu.develop.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741903114671489024, NULL, 1, NULL, 1, 'el-icon-document', 'default', 741903114549854208, 'view.menu.develop.form.manager', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741903114780540928, NULL, 1, NULL, 1, 'el-icon-edit', 'default', 741903114742792192, 'view.menu.develop.form.create', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741903114881204224, NULL, 1, NULL, 1, 'el-icon-notebook-2', 'default', 741903114814095360, 'view.menu.develop.dic', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741903115019616256, NULL, 1, NULL, 1, 'el-icon-connection', 'default', 741903114986061824, 'view.menu.develop.generic.api', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741903115086725120, NULL, 1, NULL, 1, 'el-icon-data-line', 'default', 741903115048976384, 'view.menu.develop.systemlog', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741903117355843584, NULL, 1, NULL, 1, 'el-icon-s-data', 'default', 741903116688949248, 'view.menu.develop.cache', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741903117783662592, NULL, 1, NULL, 1, 'el-icon-user', 'default', 741903117741719552, 'view.menu.common', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741903117930463232, NULL, 1, NULL, 1, 'el-icon-user', 'default', 741903117892714496, 'view.menu.common.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741903476849639424, '', 1, NULL, 1, '', 'default', 741903476816084992, '兴昊塑业', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741903476916748288, '', 1, NULL, 1, '', 'default', 741903476874805248, '单据扫描', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741903476971274240, NULL, 1, NULL, 1, 'el-icon-setting', 'default', 741903476941914112, 'view.menu.admin.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741903477101297664, NULL, 1, NULL, 1, 'el-icon-user', 'default', 741903477076131840, 'view.menu.admin.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741903477155823616, NULL, 1, NULL, 1, 'el-icon-lock', 'default', 741903477130657792, 'view.menu.admin.role', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741903477243904000, NULL, 1, NULL, 1, 'el-icon-office-building', 'default', 741903477218738176, 'view.menu.admin.company', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741903477311012864, NULL, 1, NULL, 1, 'el-icon-s-management', 'default', 741903477277458432, 'view.menu.admin.role.group', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741903477373927424, '', 1, NULL, 1, 'el-icon-folder-opened', 'default', 741903477344567296, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741903477478785024, '', 1, NULL, 1, 'el-icon-document-add', 'default', 741903477432647680, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741903477562671104, NULL, 1, NULL, 1, 'el-icon-s-tools', 'default', 741903477529116672, 'view.menu.develop.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741903477684305920, NULL, 1, NULL, 1, 'el-icon-document', 'default', 741903477646557184, 'view.menu.develop.form.manager', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741903477759803392, NULL, 1, NULL, 1, 'el-icon-edit', 'default', 741903477717860352, 'view.menu.develop.form.create', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741903477822717952, NULL, 1, NULL, 1, 'el-icon-notebook-2', 'default', 741903477797552128, 'view.menu.develop.dic', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741903477927575552, NULL, 1, NULL, 1, 'el-icon-connection', 'default', 741903477847883776, 'view.menu.develop.generic.api', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741903478007267328, NULL, 1, NULL, 1, 'el-icon-data-line', 'default', 741903477961129984, 'view.menu.develop.systemlog', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741903478091153408, NULL, 1, NULL, 1, 'el-icon-s-data', 'default', 741903478053404672, 'view.menu.develop.cache', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741903478158262272, NULL, 1, NULL, 1, 'el-icon-user', 'default', 741903478116319232, 'view.menu.common', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (741903478221176832, NULL, 1, NULL, 1, 'el-icon-user', 'default', 741903478191816704, 'view.menu.common.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (743373888104169472, NULL, 1, NULL, 1, 'el-icon-setting', 'default', 743373888087392256, 'view.menu.admin.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (743373888141918208, NULL, 1, NULL, 1, 'el-icon-user', 'default', 743373888129335296, 'view.menu.admin.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (743373888162889728, NULL, 1, NULL, 1, 'el-icon-lock', 'default', 743373888150306816, 'view.menu.admin.role', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (743373888200638464, NULL, 1, NULL, 1, 'el-icon-office-building', 'default', 743373888188055552, 'view.menu.admin.company', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (743373888225804288, NULL, 1, NULL, 1, 'el-icon-s-management', 'default', 743373888213221376, 'view.menu.admin.role.group', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (743373888255164416, '', 1, NULL, 1, 'el-icon-folder-opened', 'default', 743373888242581504, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (743373888284524544, '', 1, NULL, 1, 'el-icon-document-add', 'default', 743373888276135936, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (743373888309690368, NULL, 1, NULL, 1, 'el-icon-s-tools', 'default', 743373888297107456, 'view.menu.develop.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (743373888355827712, NULL, 1, NULL, 1, 'el-icon-document', 'default', 743373888347439104, 'view.menu.develop.form.manager', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (743373888376799232, NULL, 1, NULL, 1, 'el-icon-edit', 'default', 743373888368410624, 'view.menu.develop.form.create', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (743373888397770752, NULL, 1, NULL, 1, 'el-icon-notebook-2', 'default', 743373888389382144, 'view.menu.develop.dic', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (743373888418742272, NULL, 1, NULL, 1, 'el-icon-connection', 'default', 743373888410353664, 'view.menu.develop.generic.api', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (743373888439713792, NULL, 1, NULL, 1, 'el-icon-data-line', 'default', 743373888427130880, 'view.menu.develop.systemlog', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (743373888452296704, NULL, 1, NULL, 1, 'el-icon-s-data', 'default', 743373888448102400, 'view.menu.develop.cache', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (743373888477462528, NULL, 1, NULL, 1, 'el-icon-user', 'default', 743373888464879616, 'view.menu.common', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (743373888511016960, NULL, 1, NULL, 1, 'el-icon-user', 'default', 743373888485851136, 'view.menu.common.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744347082428514304, NULL, 1, NULL, 1, 'el-icon-setting', 'default', 744347082403348480, 'view.menu.admin.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744347082478845952, NULL, 1, NULL, 1, 'el-icon-user', 'default', 744347082462068736, 'view.menu.admin.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744347082508206080, NULL, 1, NULL, 1, 'el-icon-lock', 'default', 744347082495623168, 'view.menu.admin.role', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744347082533371904, NULL, 1, NULL, 1, 'el-icon-office-building', 'default', 744347082520788992, 'view.menu.admin.company', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744347082554343424, NULL, 1, NULL, 1, 'el-icon-s-management', 'default', 744347082541760512, 'view.menu.admin.role.group', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744347082579509248, '', 1, NULL, 1, 'el-icon-folder-opened', 'default', 744347082566926336, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744347082625646592, '', 1, NULL, 1, 'el-icon-document-add', 'default', 744347082608869376, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744347082655006720, NULL, 1, NULL, 1, 'el-icon-s-tools', 'default', 744347082638229504, 'view.menu.develop.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744347082696949760, NULL, 1, NULL, 1, 'el-icon-document', 'default', 744347082684366848, 'view.menu.develop.form.manager', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744347082713726976, NULL, 1, NULL, 1, 'el-icon-edit', 'default', 744347082705338368, 'view.menu.develop.form.create', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744347082738892800, NULL, 1, NULL, 1, 'el-icon-notebook-2', 'default', 744347082730504192, 'view.menu.develop.dic', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744347082764058624, NULL, 1, NULL, 1, 'el-icon-connection', 'default', 744347082751475712, 'view.menu.develop.generic.api', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744347082789224448, NULL, 1, NULL, 1, 'el-icon-data-line', 'default', 744347082776641536, 'view.menu.develop.systemlog', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744347082810195968, NULL, 1, NULL, 1, 'el-icon-s-data', 'default', 744347082797613056, 'view.menu.develop.cache', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744347082826973184, NULL, 1, NULL, 1, 'el-icon-user', 'default', 744347082818584576, 'view.menu.common', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744347082847944704, NULL, 1, NULL, 1, 'el-icon-user', 'default', 744347082839556096, 'view.menu.common.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744347082906664960, '', 1, NULL, 1, 'el-icon-files', 'default', 744347082856333312, 'view.menu.oss', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744347082931830784, '', 1, NULL, 1, 'el-icon-files', 'default', 744347082919247872, 'view.menu.oss.management', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744347450755514368, NULL, 1, NULL, 1, 'el-icon-setting', 'default', 744347450730348544, 'view.menu.admin.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744347450784874496, NULL, 1, NULL, 1, 'el-icon-user', 'default', 744347450772291584, 'view.menu.admin.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744347450797457408, NULL, 1, NULL, 1, 'el-icon-lock', 'default', 744347450793263104, 'view.menu.admin.role', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744347450990395392, NULL, 1, NULL, 1, 'el-icon-office-building', 'default', 744347450977812480, 'view.menu.admin.company', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744347451011366912, NULL, 1, NULL, 1, 'el-icon-s-management', 'default', 744347451002978304, 'view.menu.admin.role.group', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744347451028144128, '', 1, NULL, 1, 'el-icon-folder-opened', 'default', 744347451019755520, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744347451057504256, '', 1, NULL, 1, 'el-icon-document-add', 'default', 744347451049115648, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744347451078475776, NULL, 1, NULL, 1, 'el-icon-s-tools', 'default', 744347451065892864, 'view.menu.develop.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744347451103641600, NULL, 1, NULL, 1, 'el-icon-document', 'default', 744347451091058688, 'view.menu.develop.form.manager', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744347451124613120, NULL, 1, NULL, 1, 'el-icon-edit', 'default', 744347451116224512, 'view.menu.develop.form.create', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744347451145584640, NULL, 1, NULL, 1, 'el-icon-notebook-2', 'default', 744347451137196032, 'view.menu.develop.dic', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744347451162361856, NULL, 1, NULL, 1, 'el-icon-connection', 'default', 744347451153973248, 'view.menu.develop.generic.api', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744347451179139072, NULL, 1, NULL, 1, 'el-icon-data-line', 'default', 744347451170750464, 'view.menu.develop.systemlog', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744347451191721984, NULL, 1, NULL, 1, 'el-icon-s-data', 'default', 744347451183333376, 'view.menu.develop.cache', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744347451208499200, NULL, 1, NULL, 1, 'el-icon-user', 'default', 744347451195916288, 'view.menu.common', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744347451221082112, NULL, 1, NULL, 1, 'el-icon-user', 'default', 744347451212693504, 'view.menu.common.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744347451242053632, '', 1, NULL, 1, 'el-icon-files', 'default', 744347451233665024, 'view.menu.oss', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744347451254636544, '', 1, NULL, 1, 'el-icon-files', 'default', 744347451250442240, 'view.menu.oss.management', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744456570603044864, NULL, 1, NULL, 1, 'el-icon-setting', 'default', 744456570582073344, 'view.menu.admin.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744456570640793600, NULL, 1, NULL, 1, 'el-icon-user', 'default', 744456570632404992, 'view.menu.admin.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744456570661765120, NULL, 1, NULL, 1, 'el-icon-lock', 'default', 744456570649182208, 'view.menu.admin.role', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744456570682736640, NULL, 1, NULL, 1, 'el-icon-office-building', 'default', 744456570670153728, 'view.menu.admin.company', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744456570703708160, NULL, 1, NULL, 1, 'el-icon-s-management', 'default', 744456570695319552, 'view.menu.admin.role.group', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744456570720485376, '', 1, NULL, 1, 'el-icon-folder-opened', 'default', 744456570712096768, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744456570749845504, '', 1, NULL, 1, 'el-icon-document-add', 'default', 744456570737262592, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744456570766622720, NULL, 1, NULL, 1, 'el-icon-s-tools', 'default', 744456570758234112, 'view.menu.develop.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744456570791788544, NULL, 1, NULL, 1, 'el-icon-document', 'default', 744456570783399936, 'view.menu.develop.form.manager', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744456570808565760, NULL, 1, NULL, 1, 'el-icon-edit', 'default', 744456570800177152, 'view.menu.develop.form.create', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744456570829537280, NULL, 1, NULL, 1, 'el-icon-notebook-2', 'default', 744456570821148672, 'view.menu.develop.dic', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744456570846314496, NULL, 1, NULL, 1, 'el-icon-connection', 'default', 744456570837925888, 'view.menu.develop.generic.api', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744456570863091712, NULL, 1, NULL, 1, 'el-icon-data-line', 'default', 744456570854703104, 'view.menu.develop.systemlog', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744456570879868928, NULL, 1, NULL, 1, 'el-icon-s-data', 'default', 744456570871480320, 'view.menu.develop.cache', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744456570896646144, NULL, 1, NULL, 1, 'el-icon-user', 'default', 744456570888257536, 'view.menu.common', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744456570913423360, NULL, 1, NULL, 1, 'el-icon-user', 'default', 744456570905034752, 'view.menu.common.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744456570930200576, '', 1, NULL, 1, 'el-icon-files', 'default', 744456570921811968, 'view.menu.oss', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744456570942783488, '', 1, NULL, 1, 'el-icon-files', 'default', 744456570938589184, 'view.menu.oss.management', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744456570959560704, '', 1, NULL, 1, 'el-icon-bell', 'default', 744456570955366400, 'menu.version', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744456570976337920, '', 1, NULL, 1, 'el-icon-bell', 'default', 744456570972143616, 'menu.version.management', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744459411438698496, NULL, 1, NULL, 1, 'el-icon-setting', 'default', 744459411426115584, 'view.menu.admin.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744459411463864320, NULL, 1, NULL, 1, 'el-icon-user', 'default', 744459411455475712, 'view.menu.admin.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744459411476447232, NULL, 1, NULL, 1, 'el-icon-lock', 'default', 744459411472252928, 'view.menu.admin.role', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744459411493224448, NULL, 1, NULL, 1, 'el-icon-office-building', 'default', 744459411484835840, 'view.menu.admin.company', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744459411505807360, NULL, 1, NULL, 1, 'el-icon-s-management', 'default', 744459411501613056, 'view.menu.admin.role.group', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744459411522584576, '', 1, NULL, 1, 'el-icon-folder-opened', 'default', 744459411514195968, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744459411564527616, '', 1, NULL, 1, 'el-icon-document-add', 'default', 744459411556139008, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744459411589693440, NULL, 1, NULL, 1, 'el-icon-s-tools', 'default', 744459411581304832, 'view.menu.develop.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744459411614859264, NULL, 1, NULL, 1, 'el-icon-document', 'default', 744459411610664960, 'view.menu.develop.form.manager', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744459411631636480, NULL, 1, NULL, 1, 'el-icon-edit', 'default', 744459411623247872, 'view.menu.develop.form.create', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744459411648413696, NULL, 1, NULL, 1, 'el-icon-notebook-2', 'default', 744459411640025088, 'view.menu.develop.dic', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744459411673579520, NULL, 1, NULL, 1, 'el-icon-connection', 'default', 744459411660996608, 'view.menu.develop.generic.api', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744459411690356736, NULL, 1, NULL, 1, 'el-icon-data-line', 'default', 744459411681968128, 'view.menu.develop.systemlog', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744459411707133952, NULL, 1, NULL, 1, 'el-icon-s-data', 'default', 744459411698745344, 'view.menu.develop.cache', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744459411719716864, NULL, 1, NULL, 1, 'el-icon-user', 'default', 744459411711328256, 'view.menu.common', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744459411732299776, NULL, 1, NULL, 1, 'el-icon-user', 'default', 744459411723911168, 'view.menu.common.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744459411749076992, '', 1, NULL, 1, 'el-icon-files', 'default', 744459411740688384, 'view.menu.oss', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744459411761659904, '', 1, NULL, 1, 'el-icon-files', 'default', 744459411753271296, 'view.menu.oss.management', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744459411774242816, '', 1, NULL, 1, 'el-icon-bell', 'default', 744459411765854208, 'view.menu.version', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744459411786825728, '', 1, NULL, 1, 'el-icon-bell', 'default', 744459411782631424, 'view.menu.version.management', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744461304302927872, NULL, 1, NULL, 1, 'el-icon-setting', 'default', 744461304273567744, 'view.menu.admin.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744461304336482304, NULL, 1, NULL, 1, 'el-icon-user', 'default', 744461304328093696, 'view.menu.admin.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744461304357453824, NULL, 1, NULL, 1, 'el-icon-lock', 'default', 744461304344870912, 'view.menu.admin.role', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744461304374231040, NULL, 1, NULL, 1, 'el-icon-office-building', 'default', 744461304365842432, 'view.menu.admin.company', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744461304399396864, NULL, 1, NULL, 1, 'el-icon-s-management', 'default', 744461304386813952, 'view.menu.admin.role.group', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744461304416174080, '', 1, NULL, 1, 'el-icon-folder-opened', 'default', 744461304407785472, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744461304445534208, '', 1, NULL, 1, 'el-icon-document-add', 'default', 744461304437145600, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744461304466505728, NULL, 1, NULL, 1, 'el-icon-s-tools', 'default', 744461304458117120, 'view.menu.develop.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744461304491671552, NULL, 1, NULL, 1, 'el-icon-document', 'default', 744461304483282944, 'view.menu.develop.form.manager', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744461304512643072, NULL, 1, NULL, 1, 'el-icon-edit', 'default', 744461304504254464, 'view.menu.develop.form.create', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744461304533614592, NULL, 1, NULL, 1, 'el-icon-notebook-2', 'default', 744461304521031680, 'view.menu.develop.dic', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744461304562974720, NULL, 1, NULL, 1, 'el-icon-connection', 'default', 744461304546197504, 'view.menu.develop.generic.api', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744461304583946240, NULL, 1, NULL, 1, 'el-icon-data-line', 'default', 744461304571363328, 'view.menu.develop.systemlog', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744461304604917760, NULL, 1, NULL, 1, 'el-icon-s-data', 'default', 744461304592334848, 'view.menu.develop.cache', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744461304630083584, NULL, 1, NULL, 1, 'el-icon-user', 'default', 744461304617500672, 'view.menu.common', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744461304651055104, NULL, 1, NULL, 1, 'el-icon-user', 'default', 744461304642666496, 'view.menu.common.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744461304672026624, '', 1, NULL, 1, 'el-icon-files', 'default', 744461304659443712, 'view.menu.oss', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744461304692998144, '', 1, NULL, 1, 'el-icon-files', 'default', 744461304684609536, 'view.menu.oss.management', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744461304713969664, '', 1, NULL, 1, 'el-icon-bell', 'default', 744461304705581056, 'view.menu.version', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (744461304734941184, '', 1, NULL, 1, 'el-icon-bell', 'default', 744461304722358272, 'view.menu.version.management', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (753118960416718848, NULL, 1, NULL, 1, 'el-icon-setting', 'default', 753118960395747328, 'view.menu.admin.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (753118960454467584, NULL, 1, NULL, 1, 'el-icon-user', 'default', 753118960446078976, 'view.menu.admin.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (753118960475439104, NULL, 1, NULL, 1, 'el-icon-lock', 'default', 753118960467050496, 'view.menu.admin.role', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (753118960500604928, NULL, 1, NULL, 1, 'el-icon-office-building', 'default', 753118960483827712, 'view.menu.admin.company', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (753118960517382144, NULL, 1, NULL, 1, 'el-icon-s-management', 'default', 753118960508993536, 'view.menu.admin.role.group', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (753118960542547968, '', 1, NULL, 1, 'el-icon-folder-opened', 'default', 753118960525770752, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (753118960588685312, '', 1, NULL, 1, 'el-icon-document-add', 'default', 753118960576102400, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (753118960613851136, NULL, 1, NULL, 1, 'el-icon-s-tools', 'default', 753118960605462528, 'view.menu.develop.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (753118960639016960, NULL, 1, NULL, 1, 'el-icon-document', 'default', 753118960630628352, 'view.menu.develop.form.manager', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (753118960676765696, NULL, 1, NULL, 1, 'el-icon-edit', 'default', 753118960647405568, 'view.menu.develop.form.create', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (753118960693542912, NULL, 1, NULL, 1, 'el-icon-notebook-2', 'default', 753118960685154304, 'view.menu.develop.dic', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (753118960714514432, NULL, 1, NULL, 1, 'el-icon-connection', 'default', 753118960701931520, 'view.menu.develop.generic.api', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (753118960731291648, NULL, 1, NULL, 1, 'el-icon-data-line', 'default', 753118960722903040, 'view.menu.develop.systemlog', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (753118960752263168, NULL, 1, NULL, 1, 'el-icon-s-data', 'default', 753118960743874560, 'view.menu.develop.cache', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (753118960764846080, '', 1, NULL, 1, '', 'default', 753118960760651776, 'view.menu.data.handle', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (753118960785817600, NULL, 1, NULL, 1, 'el-icon-user', 'default', 753118960773234688, 'view.menu.common', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (753118960798400512, NULL, 1, NULL, 1, 'el-icon-user', 'default', 753118960790011904, 'view.menu.common.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (753118960819372032, '', 1, NULL, 1, 'el-icon-files', 'default', 753118960810983424, 'view.menu.oss', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (753118960836149248, '', 1, NULL, 1, 'el-icon-files', 'default', 753118960827760640, 'view.menu.oss.management', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (753118960857120768, '', 1, NULL, 1, 'el-icon-bell', 'default', 753118960848732160, 'view.menu.version', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (753118960873897984, '', 1, NULL, 1, 'el-icon-bell', 'default', 753118960865509376, 'view.menu.version.management', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (755305983386845184, NULL, 1, NULL, 1, 'el-icon-setting', 'default', 755305983357485056, 'view.menu.admin.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (755305983424593920, NULL, 1, NULL, 1, 'el-icon-user', 'default', 755305983416205312, 'view.menu.admin.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (755305983445565440, NULL, 1, NULL, 1, 'el-icon-lock', 'default', 755305983432982528, 'view.menu.admin.role', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (755305983462342656, NULL, 1, NULL, 1, 'el-icon-office-building', 'default', 755305983453954048, 'view.menu.admin.company', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (755305983479119872, NULL, 1, NULL, 1, 'el-icon-s-management', 'default', 755305983470731264, 'view.menu.admin.role.group', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (755305983500091392, '', 1, NULL, 1, '', 'default', 755305983487508480, 'Ex-id', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (755305983521062912, '', 1, NULL, 1, 'el-icon-folder-opened', 'default', 755305983512674304, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (755305983558811648, '', 1, NULL, 1, 'el-icon-document-add', 'default', 755305983554617344, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (755305983575588864, NULL, 1, NULL, 1, 'el-icon-s-tools', 'default', 755305983567200256, 'view.menu.develop.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (755305983609143296, NULL, 1, NULL, 1, 'el-icon-document', 'default', 755305983592366080, 'view.menu.develop.form.manager', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (755305983621726208, NULL, 1, NULL, 1, 'el-icon-edit', 'default', 755305983617531904, 'view.menu.develop.form.create', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (755305983638503424, NULL, 1, NULL, 1, 'el-icon-notebook-2', 'default', 755305983630114816, 'view.menu.develop.dic', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (755305983659474944, NULL, 1, NULL, 1, 'el-icon-connection', 'default', 755305983646892032, 'view.menu.develop.generic.api', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (755305983688835072, NULL, 1, NULL, 1, 'el-icon-data-line', 'default', 755305983667863552, 'view.menu.develop.systemlog', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (755305983709806592, NULL, 1, NULL, 1, 'el-icon-s-data', 'default', 755305983701417984, 'view.menu.develop.cache', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (755305983722389504, '', 1, NULL, 1, '', 'default', 755305983718195200, 'view.menu.data.handle', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (755305983739166720, NULL, 1, NULL, 1, 'el-icon-user', 'default', 755305983730778112, 'view.menu.common', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (755305983755943936, NULL, 1, NULL, 1, 'el-icon-user', 'default', 755305983747555328, 'view.menu.common.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (755305983772721152, '', 1, NULL, 1, 'el-icon-files', 'default', 755305983760138240, 'view.menu.oss', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (755305983785304064, '', 1, NULL, 1, 'el-icon-files', 'default', 755305983776915456, 'view.menu.oss.management', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (755305983860801536, '', 1, NULL, 1, 'el-icon-bell', 'default', 755305983793692672, 'view.menu.version', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (755305983982436352, '', 1, NULL, 1, 'el-icon-bell', 'default', 755305983948881920, 'view.menu.version.management', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (756378452675788800, NULL, 1, NULL, 1, 'el-icon-setting', 'default', 756378452642234368, 'view.menu.admin.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (756378452700954624, NULL, 1, NULL, 1, 'el-icon-user', 'default', 756378452696760320, 'view.menu.admin.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (756378452713537536, NULL, 1, NULL, 1, 'el-icon-lock', 'default', 756378452709343232, 'view.menu.admin.role', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (756378452730314752, NULL, 1, NULL, 1, 'el-icon-office-building', 'default', 756378452721926144, 'view.menu.admin.company', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (756378452747091968, NULL, 1, NULL, 1, 'el-icon-s-management', 'default', 756378452738703360, 'view.menu.admin.role.group', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (756378452763869184, '', 1, NULL, 1, '', 'default', 756378452751286272, 'Ex-id', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (756378452780646400, '', 1, NULL, 1, '', 'default', 756378452776452096, '消息提醒', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (756378452810006528, '', 1, NULL, 1, 'el-icon-folder-opened', 'default', 756378452801617920, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (756378452826783744, '', 1, NULL, 1, 'el-icon-document-add', 'default', 756378452822589440, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (756378452839366656, NULL, 1, NULL, 1, 'el-icon-s-tools', 'default', 756378452835172352, 'view.menu.develop.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (756378452851949568, NULL, 1, NULL, 1, 'el-icon-document', 'default', 756378452847755264, 'view.menu.develop.form.manager', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (756378452864532480, NULL, 1, NULL, 1, 'el-icon-edit', 'default', 756378452856143872, 'view.menu.develop.form.create', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (756378452881309696, NULL, 1, NULL, 1, 'el-icon-notebook-2', 'default', 756378452872921088, 'view.menu.develop.dic', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (756378452889698304, NULL, 1, NULL, 1, 'el-icon-connection', 'default', 756378452885504000, 'view.menu.develop.generic.api', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (756378452910669824, NULL, 1, NULL, 1, 'el-icon-data-line', 'default', 756378452898086912, 'view.menu.develop.systemlog', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (756378452919058432, NULL, 1, NULL, 1, 'el-icon-s-data', 'default', 756378452914864128, 'view.menu.develop.cache', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (756378452931641344, '', 1, NULL, 1, '', 'default', 756378452923252736, 'view.menu.data.handle', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (756378452944224256, NULL, 1, NULL, 1, 'el-icon-user', 'default', 756378452940029952, 'view.menu.common', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (756378452952612864, NULL, 1, NULL, 1, 'el-icon-user', 'default', 756378452948418560, 'view.menu.common.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (756378452965195776, '', 1, NULL, 1, 'el-icon-files', 'default', 756378452956807168, 'view.menu.oss', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (756378452973584384, '', 1, NULL, 1, 'el-icon-files', 'default', 756378452969390080, 'view.menu.oss.management', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (756378452986167296, '', 1, NULL, 1, 'el-icon-bell', 'default', 756378452981972992, 'view.menu.version', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (756378452994555904, '', 1, NULL, 1, 'el-icon-bell', 'default', 756378452990361600, 'view.menu.version.management', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (758238409339699200, NULL, 1, NULL, 1, 'el-icon-setting', 'default', 758238409310339072, 'view.menu.admin.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (758238409377447936, NULL, 1, NULL, 1, 'el-icon-user', 'default', 758238409369059328, 'view.menu.admin.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (758238409398419456, NULL, 1, NULL, 1, 'el-icon-lock', 'default', 758238409385836544, 'view.menu.admin.role', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (758238409415196672, NULL, 1, NULL, 1, 'el-icon-office-building', 'default', 758238409406808064, 'view.menu.admin.company', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (758238409436168192, NULL, 1, NULL, 1, 'el-icon-s-management', 'default', 758238409427779584, 'view.menu.admin.role.group', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (758238409452945408, '', 1, NULL, 1, '', 'default', 758238409444556800, 'Ex-id', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (758238409478111232, '', 1, NULL, 1, '', 'default', 758238409469722624, '消息提醒', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (758238409503277056, '', 1, NULL, 1, 'el-icon-folder-opened', 'default', 758238409494888448, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (758238409528442880, '', 1, NULL, 1, 'el-icon-document-add', 'default', 758238409515859968, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (758238409545220096, NULL, 1, NULL, 1, 'el-icon-s-tools', 'default', 758238409536831488, 'view.menu.develop.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (758238409591357440, NULL, 1, NULL, 1, 'el-icon-document', 'default', 758238409566191616, 'view.menu.develop.form.manager', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (758238409612328960, NULL, 1, NULL, 1, 'el-icon-edit', 'default', 758238409599746048, 'view.menu.develop.form.create', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (758238409633300480, NULL, 1, NULL, 1, 'el-icon-notebook-2', 'default', 758238409620717568, 'view.menu.develop.dic', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (758238409650077696, NULL, 1, NULL, 1, 'el-icon-connection', 'default', 758238409641689088, 'view.menu.develop.generic.api', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (758238409759129600, NULL, 1, NULL, 1, 'el-icon-data-line', 'default', 758238409746546688, 'view.menu.develop.systemlog', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (758238409784295424, NULL, 1, NULL, 1, 'el-icon-s-data', 'default', 758238409775906816, 'view.menu.develop.cache', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (758238409796878336, '', 1, NULL, 1, '', 'default', 758238409792684032, 'view.menu.data.handle', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (758238409817849856, '', 1, NULL, 1, '', 'default', 758238409809461248, 'view.menu.develop.third-party', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (758238409843015680, NULL, 1, NULL, 1, 'el-icon-user', 'default', 758238409834627072, 'view.menu.common', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (758238409855598592, NULL, 1, NULL, 1, 'el-icon-user', 'default', 758238409851404288, 'view.menu.common.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (758238409876570112, '', 1, NULL, 1, 'el-icon-files', 'default', 758238409863987200, 'view.menu.oss', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (758238409897541632, '', 1, NULL, 1, 'el-icon-files', 'default', 758238409889153024, 'view.menu.oss.management', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (758238409910124544, '', 1, NULL, 1, 'el-icon-bell', 'default', 758238409901735936, 'view.menu.version', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (758238409922707456, '', 1, NULL, 1, 'el-icon-bell', 'default', 758238409918513152, 'view.menu.version.management', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (771996112528605184, NULL, 1, NULL, 1, 'el-icon-setting', 'default', 771996112490856448, 'view.menu.admin.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (771996112616685568, NULL, 1, NULL, 1, 'el-icon-user', 'default', 771996112591519744, 'view.menu.admin.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (771996112671211520, NULL, 1, NULL, 1, 'el-icon-lock', 'default', 771996112641851392, 'view.menu.admin.role', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (771996112717348864, NULL, 1, NULL, 1, 'el-icon-office-building', 'default', 771996112692183040, 'view.menu.admin.company', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (771996112759291904, NULL, 1, NULL, 1, 'el-icon-s-management', 'default', 771996112738320384, 'view.menu.admin.role.group', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (771996112801234944, '', 1, NULL, 1, '', 'default', 771996112780263424, 'Ex-id', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (771996112864149504, '', 1, NULL, 1, '', 'default', 771996112843177984, '消息提醒', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (771996112922869760, '', 1, NULL, 1, 'el-icon-folder-opened', 'default', 771996112897703936, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (771996112989978624, '', 1, NULL, 1, 'el-icon-document-add', 'default', 771996112969007104, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (771996113036115968, NULL, 1, NULL, 1, 'el-icon-s-tools', 'default', 771996113010950144, 'view.menu.develop.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (771996113090641920, NULL, 1, NULL, 1, 'el-icon-document', 'default', 771996113069670400, 'view.menu.develop.form.manager', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (771996113136779264, NULL, 1, NULL, 1, 'el-icon-edit', 'default', 771996113111613440, 'view.menu.develop.form.create', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (771996113187110912, NULL, 1, NULL, 1, 'el-icon-notebook-2', 'default', 771996113157750784, 'view.menu.develop.dic', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (771996113258414080, NULL, 1, NULL, 1, 'el-icon-connection', 'default', 771996113208082432, 'view.menu.develop.generic.api', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (771996113300357120, NULL, 1, NULL, 1, 'el-icon-data-line', 'default', 771996113279385600, 'view.menu.develop.systemlog', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (771996113342300160, NULL, 1, NULL, 1, 'el-icon-s-data', 'default', 771996113317134336, 'view.menu.develop.cache', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (771996113388437504, '', 1, NULL, 1, '', 'default', 771996113363271680, 'view.menu.data.handle', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (771996113426186240, '', 1, NULL, 1, '', 'default', 771996113405214720, 'view.menu.develop.third-party', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (771996113480712192, NULL, 1, NULL, 1, 'el-icon-user', 'default', 771996113463934976, 'view.menu.common', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (771996113526849536, NULL, 1, NULL, 1, 'el-icon-user', 'default', 771996113497489408, 'view.menu.common.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (771996113564598272, '', 1, NULL, 1, 'el-icon-files', 'default', 771996113543626752, 'view.menu.oss', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (771996113623318528, '', 1, NULL, 1, 'el-icon-files', 'default', 771996113602347008, 'view.menu.oss.management', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (771996113661067264, '', 1, NULL, 1, 'el-icon-bell', 'default', 771996113640095744, 'view.menu.version', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (771996113719787520, '', 1, NULL, 1, 'el-icon-bell', 'default', 771996113698816000, 'view.menu.version.management', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773366535463895040, NULL, 1, NULL, 1, 'el-icon-setting', 'default', 773366535451312128, 'view.menu.admin.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773366535489060864, NULL, 1, NULL, 1, 'el-icon-user', 'default', 773366535480672256, 'view.menu.admin.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773366535501643776, NULL, 1, NULL, 1, 'el-icon-lock', 'default', 773366535493255168, 'view.menu.admin.role', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773366535510032384, NULL, 1, NULL, 1, 'el-icon-office-building', 'default', 773366535505838080, 'view.menu.admin.company', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773366535531003904, NULL, 1, NULL, 1, 'el-icon-s-management', 'default', 773366535518420992, 'view.menu.admin.role.group', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773366535539392512, '', 1, NULL, 1, '', 'default', 773366535535198208, 'Ex-id', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773366535560364032, '', 1, NULL, 1, '', 'default', 773366535551975424, '消息提醒', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773366535581335552, '', 1, NULL, 1, 'el-icon-folder-opened', 'default', 773366535568752640, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773366535598112768, '', 1, NULL, 1, 'el-icon-document-add', 'default', 773366535593918464, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773366535610695680, NULL, 1, NULL, 1, 'el-icon-s-tools', 'default', 773366535606501376, 'view.menu.develop.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773366535631667200, NULL, 1, NULL, 1, 'el-icon-document', 'default', 773366535623278592, 'view.menu.develop.form.manager', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773366535644250112, NULL, 1, NULL, 1, 'el-icon-edit', 'default', 773366535635861504, 'view.menu.develop.form.create', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773366535656833024, NULL, 1, NULL, 1, 'el-icon-notebook-2', 'default', 773366535648444416, 'view.menu.develop.dic', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773366535669415936, NULL, 1, NULL, 1, 'el-icon-connection', 'default', 773366535665221632, 'view.menu.develop.generic.api', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773366535702970368, NULL, 1, NULL, 1, 'el-icon-data-line', 'default', 773366535690387456, 'view.menu.develop.systemlog', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773366535719747584, NULL, 1, NULL, 1, 'el-icon-s-data', 'default', 773366535707164672, 'view.menu.develop.cache', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773366535732330496, '', 1, NULL, 1, '', 'default', 773366535723941888, 'view.menu.data.handle', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773366535749107712, '', 1, NULL, 1, '', 'default', 773366535740719104, 'view.menu.develop.third-party', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773366535765884928, '', 1, NULL, 1, '', 'default', 773366535761690624, 'XXL JOB调度平台', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773366535782662144, NULL, 1, NULL, 1, 'el-icon-user', 'default', 773366535774273536, 'view.menu.common', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773366535795245056, NULL, 1, NULL, 1, 'el-icon-user', 'default', 773366535786856448, 'view.menu.common.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773366535816216576, '', 1, NULL, 1, 'el-icon-files', 'default', 773366535807827968, 'view.menu.oss', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773366535837188096, '', 1, NULL, 1, 'el-icon-files', 'default', 773366535828799488, 'view.menu.oss.management', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773366535853965312, '', 1, NULL, 1, 'el-icon-bell', 'default', 773366535845576704, 'view.menu.version', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773366535874936832, '', 1, NULL, 1, 'el-icon-bell', 'default', 773366535866548224, 'view.menu.version.management', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773376290248982528, NULL, 1, NULL, 1, 'el-icon-setting', 'default', 773376290236399616, 'view.menu.admin.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773376290274148352, NULL, 1, NULL, 1, 'el-icon-user', 'default', 773376290265759744, 'view.menu.admin.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773376290286731264, NULL, 1, NULL, 1, 'el-icon-lock', 'default', 773376290282536960, 'view.menu.admin.role', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773376290299314176, NULL, 1, NULL, 1, 'el-icon-office-building', 'default', 773376290290925568, 'view.menu.admin.company', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773376290311897088, NULL, 1, NULL, 1, 'el-icon-s-management', 'default', 773376290303508480, 'view.menu.admin.role.group', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773376290320285696, '', 1, NULL, 1, '', 'default', 773376290316091392, 'Ex-id', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773376290341257216, '', 1, NULL, 1, '', 'default', 773376290332868608, '消息提醒', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773376290358034432, '', 1, NULL, 1, 'el-icon-folder-opened', 'default', 773376290349645824, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773376290370617344, '', 1, NULL, 1, 'el-icon-document-add', 'default', 773376290366423040, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773376290391588864, NULL, 1, NULL, 1, 'el-icon-s-tools', 'default', 773376290379005952, 'view.menu.develop.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773376290416754688, NULL, 1, NULL, 1, 'el-icon-document', 'default', 773376290408366080, 'view.menu.develop.form.manager', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773376290425143296, NULL, 1, NULL, 1, 'el-icon-edit', 'default', 773376290420948992, 'view.menu.develop.form.create', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773376290433531905, NULL, 1, NULL, 1, 'el-icon-notebook-2', 'default', 773376290433531904, 'view.menu.develop.dic', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773376290446114816, NULL, 1, NULL, 1, 'el-icon-connection', 'default', 773376290441920512, 'view.menu.develop.generic.api', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773376290471280640, NULL, 1, NULL, 1, 'el-icon-data-line', 'default', 773376290450309120, 'view.menu.develop.systemlog', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773376290483863552, NULL, 1, NULL, 1, 'el-icon-s-data', 'default', 773376290475474944, 'view.menu.develop.cache', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773376290496446464, '', 1, NULL, 1, '', 'default', 773376290488057856, 'view.menu.data.handle', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773376290504835072, '', 1, NULL, 1, '', 'default', 773376290500640768, 'view.menu.develop.third-party', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773376290525806592, '', 1, NULL, 1, '', 'default', 773376290513223681, 'XXL JOB调度平台', NULL, 'http://127.0.0.1:9898/xxl-job-admin');
INSERT INTO `view_route_meta` VALUES (773376290538389504, NULL, 1, NULL, 1, 'el-icon-user', 'default', 773376290534195200, 'view.menu.common', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773376290550972416, NULL, 1, NULL, 1, 'el-icon-user', 'default', 773376290542583808, 'view.menu.common.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773376290559361024, '', 1, NULL, 1, 'el-icon-files', 'default', 773376290555166720, 'view.menu.oss', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773376290571943937, '', 1, NULL, 1, 'el-icon-files', 'default', 773376290571943936, 'view.menu.oss.management', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773376290584526848, '', 1, NULL, 1, 'el-icon-bell', 'default', 773376290580332544, 'view.menu.version', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773376290601304064, '', 1, NULL, 1, 'el-icon-bell', 'default', 773376290597109760, 'view.menu.version.management', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773780005065850880, NULL, 1, NULL, 1, 'el-icon-setting', 'default', 773780005049073664, 'view.menu.admin.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773780005091016704, NULL, 1, NULL, 1, 'el-icon-user', 'default', 773780005086822400, 'view.menu.admin.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773780005107793920, NULL, 1, NULL, 1, 'el-icon-lock', 'default', 773780005099405312, 'view.menu.admin.role', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773780005120376832, NULL, 1, NULL, 1, 'el-icon-office-building', 'default', 773780005111988224, 'view.menu.admin.company', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773780005128765440, NULL, 1, NULL, 1, 'el-icon-s-management', 'default', 773780005124571136, 'view.menu.admin.role.group', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773780005141348352, '', 1, NULL, 1, '', 'default', 773780005132959744, 'Ex-id', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773780005158125568, '', 1, NULL, 1, '', 'default', 773780005153931264, '消息提醒', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773780005179097088, '', 1, NULL, 1, 'el-icon-folder-opened', 'default', 773780005170708480, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773780005195874304, '', 1, NULL, 1, 'el-icon-document-add', 'default', 773780005187485696, '路由管理', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773780005212651520, NULL, 1, NULL, 1, 'el-icon-s-tools', 'default', 773780005204262912, 'view.menu.develop.title', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773780005233623040, NULL, 1, NULL, 1, 'el-icon-document', 'default', 773780005225234432, 'view.menu.develop.form.manager', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773780005246205952, NULL, 1, NULL, 1, 'el-icon-edit', 'default', 773780005237817344, 'view.menu.develop.form.create', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773780005258788864, NULL, 1, NULL, 1, 'el-icon-notebook-2', 'default', 773780005250400256, 'view.menu.develop.dic', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773780005271371776, '', 1, NULL, 1, 'el-icon-connection', 'default', 773780005262983168, 'view.menu.develop.generic.api', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773780005279760384, NULL, 1, NULL, 1, 'el-icon-data-line', 'default', 773780005275566080, 'view.menu.develop.systemlog', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773780005292343296, NULL, 1, NULL, 1, 'el-icon-s-data', 'default', 773780005288148992, 'view.menu.develop.cache', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773780005309120512, '', 1, NULL, 1, '', 'default', 773780005296537600, 'view.menu.data.handle', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773780005321703424, '', 1, NULL, 1, '', 'default', 773780005313314816, 'view.menu.develop.third-party', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773780005338480640, '', 1, NULL, 1, '', 'default', 773780005330092032, 'XXL JOB调度平台', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773780005346869248, NULL, 1, NULL, 1, 'el-icon-user', 'default', 773780005342674944, 'view.menu.common', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773780005363646464, NULL, 1, NULL, 1, 'el-icon-user', 'default', 773780005355257856, 'view.menu.common.user', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773780005376229376, '', 1, NULL, 1, 'el-icon-files', 'default', 773780005372035072, 'view.menu.oss', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773780005397200896, '', 1, NULL, 1, 'el-icon-files', 'default', 773780005388812288, 'view.menu.oss.management', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773780005409783808, '', 1, NULL, 1, 'el-icon-bell', 'default', 773780005401395200, 'view.menu.version', NULL, NULL);
INSERT INTO `view_route_meta` VALUES (773780005430755328, '', 1, NULL, 1, 'el-icon-bell', 'default', 773780005418172416, 'view.menu.version.management', NULL, NULL);

-- ----------------------------
-- Table structure for view_route_role
-- ----------------------------
DROP TABLE IF EXISTS `view_route_role`;
CREATE TABLE `view_route_role`  (
  `id` bigint NOT NULL COMMENT '主键ID（雪花算法生成）',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `flag` int NULL DEFAULT NULL COMMENT '状态标志（0:禁用，1:启用）',
  `role_code` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '权限编码（对应角色的 roleCode）',
  `route_id` bigint NOT NULL COMMENT '关联的路由ID（援引 view_route_configration.id）',
  `update_time` datetime(6) NULL DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_route_id`(`route_id` ASC) USING BTREE,
  INDEX `idx_role_code`(`role_code` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '视图路由与角色关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of view_route_role
-- ----------------------------
INSERT INTO `view_route_role` VALUES (741849291282186240, NULL, 1, 'MENU.ROUTE', 741849291194105856, NULL);
INSERT INTO `view_route_role` VALUES (741849291953274880, NULL, 1, 'MENU.ADMIN', 741849291508678656, NULL);
INSERT INTO `view_route_role` VALUES (741849292611780608, NULL, 1, 'MENU.DEV', 741849292435619840, NULL);
INSERT INTO `view_route_role` VALUES (741861855974129664, NULL, 1, 'MENU.ROUTE', 741861855894437888, NULL);
INSERT INTO `view_route_role` VALUES (741861856276119552, NULL, 1, 'MENU.ADMIN', 741861856217399296, NULL);
INSERT INTO `view_route_role` VALUES (741861856661995520, NULL, 1, 'MENU.DEV', 741861856578109440, NULL);
INSERT INTO `view_route_role` VALUES (741861863351910400, NULL, 1, 'MENU.ROUTE', 741861862823428096, NULL);
INSERT INTO `view_route_role` VALUES (741861863641317376, NULL, 1, 'MENU.ADMIN', 741861863565819904, NULL);
INSERT INTO `view_route_role` VALUES (741861864241102848, NULL, 1, 'MENU.DEV', 741861864169799680, NULL);
INSERT INTO `view_route_role` VALUES (741861866820599808, NULL, 1, 'MENU.ROUTE', 741861866757685248, NULL);
INSERT INTO `view_route_role` VALUES (741861867000954880, NULL, 1, 'MENU.ADMIN', 741861866946428928, NULL);
INSERT INTO `view_route_role` VALUES (741861867361665024, NULL, 1, 'MENU.DEV', 741861867281973248, NULL);
INSERT INTO `view_route_role` VALUES (741861879931994112, NULL, 1, 'MENU.ADMIN', 741861879873273856, NULL);
INSERT INTO `view_route_role` VALUES (741861880330452992, NULL, 1, 'MENU.ROUTE', 741861880271732736, NULL);
INSERT INTO `view_route_role` VALUES (741861880535973888, NULL, 1, 'MENU.DEV', 741861880435310592, NULL);
INSERT INTO `view_route_role` VALUES (741861925838651392, NULL, 1, 'MENU.ADMIN', 741861925779931136, NULL);
INSERT INTO `view_route_role` VALUES (741861926304219136, NULL, 1, 'MENU.ROUTE', 741861926253887488, NULL);
INSERT INTO `view_route_role` VALUES (741861926467796992, NULL, 1, 'MENU.DEV', 741861926404882432, NULL);
INSERT INTO `view_route_role` VALUES (741862940944105472, NULL, 1, 'MENU.ADMIN', 741862940847636480, NULL);
INSERT INTO `view_route_role` VALUES (741862941309009920, NULL, 1, 'MENU.ROUTE', 741862941233512448, NULL);
INSERT INTO `view_route_role` VALUES (741862941489364992, NULL, 1, 'MENU.DEV', 741862941434839040, NULL);
INSERT INTO `view_route_role` VALUES (741902418928730112, NULL, 1, 'MENU.ADMIN', 741902418853232640, NULL);
INSERT INTO `view_route_role` VALUES (741902419675316224, NULL, 1, 'MENU.ROUTE', 741902419494961152, NULL);
INSERT INTO `view_route_role` VALUES (741902420161855488, NULL, 1, 'MENU.DEV', 741902419859865600, NULL);
INSERT INTO `view_route_role` VALUES (741902675104235520, NULL, 1, 'MENU.ADMIN', 741902674974212096, NULL);
INSERT INTO `view_route_role` VALUES (741902676593213440, NULL, 1, 'MENU.ROUTE', 741902676421246976, NULL);
INSERT INTO `view_route_role` VALUES (741902677100724224, NULL, 1, 'MENU.DEV', 741902676983283712, NULL);
INSERT INTO `view_route_role` VALUES (741903113383837696, NULL, 1, 'MENU.ADMIN', 741903113316728832, NULL);
INSERT INTO `view_route_role` VALUES (741903113979428864, NULL, 1, 'MENU.ROUTE', 741903113811656704, NULL);
INSERT INTO `view_route_role` VALUES (741903114348527616, NULL, 1, 'MENU.DEV', 741903114126229504, NULL);
INSERT INTO `view_route_role` VALUES (741903477009022976, NULL, 1, 'MENU.ADMIN', 741903476941914112, NULL);
INSERT INTO `view_route_role` VALUES (741903477403287552, NULL, 1, 'MENU.ROUTE', 741903477344567296, NULL);
INSERT INTO `view_route_role` VALUES (741903477596225536, NULL, 1, 'MENU.DEV', 741903477529116672, NULL);
INSERT INTO `view_route_role` VALUES (743373888120946688, NULL, 1, 'MENU.ADMIN', 743373888087392256, NULL);
INSERT INTO `view_route_role` VALUES (743373888267747328, NULL, 1, 'MENU.ROUTE', 743373888242581504, NULL);
INSERT INTO `view_route_role` VALUES (743373888334856192, NULL, 1, 'MENU.DEV', 743373888297107456, NULL);
INSERT INTO `view_route_role` VALUES (744347082445291520, NULL, 1, 'MENU.ADMIN', 744347082403348480, NULL);
INSERT INTO `view_route_role` VALUES (744347082596286464, NULL, 1, 'MENU.ROUTE', 744347082566926336, NULL);
INSERT INTO `view_route_role` VALUES (744347082675978240, NULL, 1, 'MENU.DEV', 744347082638229504, NULL);
INSERT INTO `view_route_role` VALUES (744347450763902976, NULL, 1, 'MENU.ADMIN', 744347450730348544, NULL);
INSERT INTO `view_route_role` VALUES (744347451036532736, NULL, 1, 'MENU.ROUTE', 744347451019755520, NULL);
INSERT INTO `view_route_role` VALUES (744347451086864384, NULL, 1, 'MENU.DEV', 744347451065892864, NULL);
INSERT INTO `view_route_role` VALUES (744456570619822080, NULL, 1, 'MENU.ADMIN', 744456570582073344, NULL);
INSERT INTO `view_route_role` VALUES (744456570733068288, NULL, 1, 'MENU.ROUTE', 744456570712096768, NULL);
INSERT INTO `view_route_role` VALUES (744456570775011328, NULL, 1, 'MENU.DEV', 744456570758234112, NULL);
INSERT INTO `view_route_role` VALUES (744459411447087104, NULL, 1, 'MENU.ADMIN', 744459411426115584, NULL);
INSERT INTO `view_route_role` VALUES (744459411543556096, NULL, 1, 'MENU.ROUTE', 744459411514195968, NULL);
INSERT INTO `view_route_role` VALUES (744459411602276352, NULL, 1, 'MENU.DEV', 744459411581304832, NULL);
INSERT INTO `view_route_role` VALUES (744461304315510784, NULL, 1, 'MENU.ADMIN', 744461304273567744, NULL);
INSERT INTO `view_route_role` VALUES (744461304428756992, NULL, 1, 'MENU.ROUTE', 744461304407785472, NULL);
INSERT INTO `view_route_role` VALUES (744461304474894336, NULL, 1, 'MENU.DEV', 744461304458117120, NULL);
INSERT INTO `view_route_role` VALUES (753118960433496064, NULL, 1, 'MENU.ADMIN', 753118960395747328, NULL);
INSERT INTO `view_route_role` VALUES (753118960559325184, NULL, 1, 'MENU.ROUTE', 753118960525770752, NULL);
INSERT INTO `view_route_role` VALUES (753118960622239744, NULL, 1, 'MENU.DEV', 753118960605462528, NULL);
INSERT INTO `view_route_role` VALUES (755305983403622400, NULL, 1, 'MENU.ADMIN', 755305983357485056, NULL);
INSERT INTO `view_route_role` VALUES (755305983504285696, NULL, 1, 'MENU.ADMIN', 755305983487508480, NULL);
INSERT INTO `view_route_role` VALUES (755305983542034432, NULL, 1, 'MENU.ROUTE', 755305983512674304, NULL);
INSERT INTO `view_route_role` VALUES (755305983583977472, NULL, 1, 'MENU.DEV', 755305983567200256, NULL);
INSERT INTO `view_route_role` VALUES (756378452684177408, NULL, 1, 'MENU.ADMIN', 756378452642234368, NULL);
INSERT INTO `view_route_role` VALUES (756378452768063488, NULL, 1, 'MENU.ADMIN', 756378452751286272, NULL);
INSERT INTO `view_route_role` VALUES (756378452789035008, NULL, 1, 'MENU.ADMIN', 756378452776452096, NULL);
INSERT INTO `view_route_role` VALUES (756378452814200832, NULL, 1, 'MENU.ROUTE', 756378452801617920, NULL);
INSERT INTO `view_route_role` VALUES (756378452843560960, NULL, 1, 'MENU.DEV', 756378452835172352, NULL);
INSERT INTO `view_route_role` VALUES (758238409356476416, NULL, 1, 'MENU.ADMIN', 758238409310339072, NULL);
INSERT INTO `view_route_role` VALUES (758238409461334016, NULL, 1, 'MENU.ADMIN', 758238409444556800, NULL);
INSERT INTO `view_route_role` VALUES (758238409482305536, NULL, 1, 'MENU.ADMIN', 758238409469722624, NULL);
INSERT INTO `view_route_role` VALUES (758238409511665664, NULL, 1, 'MENU.ROUTE', 758238409494888448, NULL);
INSERT INTO `view_route_role` VALUES (758238409553608704, NULL, 1, 'MENU.DEV', 758238409536831488, NULL);
INSERT INTO `view_route_role` VALUES (758238409826238464, NULL, 1, 'MENU.DEV', 758238409809461248, NULL);
INSERT INTO `view_route_role` VALUES (771996112562159616, NULL, 1, 'MENU.ADMIN', 771996112490856448, NULL);
INSERT INTO `view_route_role` VALUES (771996112826400768, NULL, 1, 'MENU.ADMIN', 771996112780263424, NULL);
INSERT INTO `view_route_role` VALUES (771996112880926720, NULL, 1, 'MENU.ADMIN', 771996112843177984, NULL);
INSERT INTO `view_route_role` VALUES (771996112952229888, NULL, 1, 'MENU.ROUTE', 771996112897703936, NULL);
INSERT INTO `view_route_role` VALUES (771996113052893184, NULL, 1, 'MENU.DEV', 771996113010950144, NULL);
INSERT INTO `view_route_role` VALUES (771996113447157760, NULL, 1, 'MENU.DEV', 771996113405214720, NULL);
INSERT INTO `view_route_role` VALUES (771996113581375488, NULL, 1, 'MENU.OSS', 771996113543626752, NULL);
INSERT INTO `view_route_role` VALUES (771996113682038784, NULL, 1, 'MENU.VERSION', 771996113640095744, NULL);
INSERT INTO `view_route_role` VALUES (773366535472283648, NULL, 1, 'MENU.ADMIN', 773366535451312128, NULL);
INSERT INTO `view_route_role` VALUES (773366535547781120, NULL, 1, 'MENU.ADMIN', 773366535535198208, NULL);
INSERT INTO `view_route_role` VALUES (773366535564558336, NULL, 1, 'MENU.ADMIN', 773366535551975424, NULL);
INSERT INTO `view_route_role` VALUES (773366535585529856, NULL, 1, 'MENU.ROUTE', 773366535568752640, NULL);
INSERT INTO `view_route_role` VALUES (773366535619084288, NULL, 1, 'MENU.DEV', 773366535606501376, NULL);
INSERT INTO `view_route_role` VALUES (773366535753302016, NULL, 1, 'MENU.DEV', 773366535740719104, NULL);
INSERT INTO `view_route_role` VALUES (773366535824605184, NULL, 1, 'MENU.OSS', 773366535807827968, NULL);
INSERT INTO `view_route_role` VALUES (773366535862353920, NULL, 1, 'MENU.VERSION', 773366535845576704, NULL);
INSERT INTO `view_route_role` VALUES (773376290257371136, NULL, 1, 'MENU.ADMIN', 773376290236399616, NULL);
INSERT INTO `view_route_role` VALUES (773376290328674304, NULL, 1, 'MENU.ADMIN', 773376290316091392, NULL);
INSERT INTO `view_route_role` VALUES (773376290345451520, NULL, 1, 'MENU.ADMIN', 773376290332868608, NULL);
INSERT INTO `view_route_role` VALUES (773376290362228736, NULL, 1, 'MENU.ROUTE', 773376290349645824, NULL);
INSERT INTO `view_route_role` VALUES (773376290399977472, NULL, 1, 'MENU.DEV', 773376290379005952, NULL);
INSERT INTO `view_route_role` VALUES (773376290513223680, NULL, 1, 'MENU.DEV', 773376290500640768, NULL);
INSERT INTO `view_route_role` VALUES (773376290563555328, NULL, 1, 'MENU.OSS', 773376290555166720, NULL);
INSERT INTO `view_route_role` VALUES (773376290592915456, NULL, 1, 'MENU.VERSION', 773376290580332544, NULL);
INSERT INTO `view_route_role` VALUES (773780005078433792, NULL, 1, 'MENU.ADMIN', 773780005049073664, NULL);
INSERT INTO `view_route_role` VALUES (773780005145542656, NULL, 1, 'MENU.ADMIN', 773780005132959744, NULL);
INSERT INTO `view_route_role` VALUES (773780005166514176, NULL, 1, 'MENU.ADMIN', 773780005153931264, NULL);
INSERT INTO `view_route_role` VALUES (773780005183291392, NULL, 1, 'MENU.ROUTE', 773780005170708480, NULL);
INSERT INTO `view_route_role` VALUES (773780005221040128, NULL, 1, 'MENU.DEV', 773780005204262912, NULL);
INSERT INTO `view_route_role` VALUES (773780005325897728, NULL, 1, 'MENU.DEV', 773780005313314816, NULL);
INSERT INTO `view_route_role` VALUES (773780005384617984, NULL, 1, 'MENU.OSS', 773780005372035072, NULL);
INSERT INTO `view_route_role` VALUES (773780005413978112, NULL, 1, 'MENU.VERSION', 773780005401395200, NULL);

-- ----------------------------
-- Table structure for view_route_version
-- ----------------------------
DROP TABLE IF EXISTS `view_route_version`;
CREATE TABLE `view_route_version`  (
  `id` bigint NOT NULL COMMENT '版本ID（雪花算法生成）',
  `create_by` bigint NULL DEFAULT NULL COMMENT '创建者用户ID',
  `create_time` datetime(6) NULL DEFAULT NULL COMMENT '创建时间',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '版本说明（描述本次变更内容）',
  `flag` int NULL DEFAULT NULL COMMENT '状态标志（0:禁用，1:启用）',
  `route_group` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '路由分组（如 common、admin、develop）',
  `version_no` int NOT NULL COMMENT '组内版本序号（递增，如 v1、v2...）',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_group`(`route_group` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '视图路由版本表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of view_route_version
-- ----------------------------
INSERT INTO `view_route_version` VALUES (1, 1, '2026-08-10 11:29:04.000000', '最初版本', 1, 'default', 1);
INSERT INTO `view_route_version` VALUES (741861855823134720, 1, '2026-08-10 11:34:09.554000', NULL, 1, 'default', 2);
INSERT INTO `view_route_version` VALUES (741861862789873664, 1, '2026-08-10 11:34:11.215000', NULL, 1, 'default', 3);
INSERT INTO `view_route_version` VALUES (741861866719936512, 1, '2026-08-10 11:34:12.153000', NULL, 1, 'default', 4);
INSERT INTO `view_route_version` VALUES (741861879848108032, 1, '2026-08-10 11:34:15.283000', NULL, 1, 'default', 5);
INSERT INTO `view_route_version` VALUES (741861925725405184, 1, '2026-08-10 11:34:26.220000', '位置变化', 1, 'default', 6);
INSERT INTO `view_route_version` VALUES (741862940793110528, 1, '2026-08-10 11:38:28.232000', '测试', 1, 'default', 7);
INSERT INTO `view_route_version` VALUES (741902418702237696, 1, '2026-08-10 14:15:20.496000', '加入兴昊ERP表单扫描配置菜单', 1, 'default', 8);
INSERT INTO `view_route_version` VALUES (741902674630279168, 1, '2026-08-10 14:16:21.517000', '修改单据扫描改动name', 1, 'default', 9);
INSERT INTO `view_route_version` VALUES (741903113123790848, 1, '2026-08-10 14:18:06.061000', NULL, 1, 'default', 10);
INSERT INTO `view_route_version` VALUES (741903476560232448, 1, '2026-08-10 14:19:32.711000', NULL, 1, 'default', 11);
INSERT INTO `view_route_version` VALUES (743373888045449216, 1, '2026-08-14 15:42:26.124000', '删除多余配置', 1, 'default', 12);
INSERT INTO `view_route_version` VALUES (744347082327851008, 1, '2026-08-17 08:09:33.696000', '加入oss', 1, 'default', 13);
INSERT INTO `view_route_version` VALUES (744347450713571328, 1, '2026-08-17 08:11:01.556000', NULL, 1, 'default', 14);
INSERT INTO `view_route_version` VALUES (744456570561101824, 1, '2026-08-17 15:24:37.755000', NULL, 1, 'default', 15);
INSERT INTO `view_route_version` VALUES (744459411392561152, 1, '2026-08-17 15:35:55.063000', NULL, 1, 'default', 16);
INSERT INTO `view_route_version` VALUES (744461304235819008, 1, '2026-08-17 15:43:26.347000', NULL, 1, 'default', 17);
INSERT INTO `view_route_version` VALUES (753118960366387200, 1, '2026-09-10 13:05:52.422000', '加入数据处理界面', 1, 'default', 18);
INSERT INTO `view_route_version` VALUES (755305983332319232, 1, '2026-09-16 13:56:19.331000', NULL, 1, 'default', 19);
INSERT INTO `view_route_version` VALUES (756378452625457152, 1, '2026-09-19 12:57:55.935000', NULL, 1, 'default', 20);
INSERT INTO `view_route_version` VALUES (758238409289367552, 1, '2026-09-24 16:08:44.161000', NULL, 1, 'default', 21);
INSERT INTO `view_route_version` VALUES (771996112461496320, 1, '2026-11-01 15:16:56.203000', NULL, 1, 'default', 22);
INSERT INTO `view_route_version` VALUES (773366535438729216, 1, '2026-11-05 10:02:30.503000', NULL, 1, 'default', 23);
INSERT INTO `view_route_version` VALUES (773376290223816704, 1, '2026-11-05 10:41:16.226000', NULL, 1, 'default', 24);
INSERT INTO `view_route_version` VALUES (773780005032296448, 1, '2026-11-06 13:25:29.337000', NULL, 1, 'default', 25);

SET FOREIGN_KEY_CHECKS = 1;

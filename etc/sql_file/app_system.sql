/*
 Navicat Premium Dump SQL

 Source Server         : 192.168.80.32_3306
 Source Server Type    : MySQL
 Source Server Version : 80041 (8.0.41)
 Source Host           : 192.168.80.32:3306
 Source Schema         : core_app

 Target Server Type    : MySQL
 Target Server Version : 80041 (8.0.41)
 File Encoding         : 65001

 Date: 07/08/2026 16:55:38
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for dynamic_form_configration
-- ----------------------------
DROP TABLE IF EXISTS `dynamic_form_configration`;
CREATE TABLE `dynamic_form_configration`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `expandable` int NULL DEFAULT NULL,
  `expanded` int NULL DEFAULT NULL,
  `gutter` int NULL DEFAULT NULL,
  `label_width` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `span` int NULL DEFAULT NULL,
  `title` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '表单标题',
  `flag` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `IDXnwmmyxc6ycs9enf76259ukckp`(`flag` ASC) USING BTREE,
  INDEX `IDXhpd82ymnmm8l26ylo49r363et`(`title` ASC) USING BTREE,
  INDEX `IDXivdgpn6eg2l95b8qaycymamxe`(`expandable` ASC, `expanded` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of dynamic_form_configration
-- ----------------------------
INSERT INTO `dynamic_form_configration` VALUES (1, 1, 1, 40, '80px', 8, '用户查询表单', 1);
INSERT INTO `dynamic_form_configration` VALUES (2, 0, 1, 40, '80px', 5, '用户新增表单', 1);
INSERT INTO `dynamic_form_configration` VALUES (3, 0, 1, 24, '80px', 20, '用户编辑表单', 1);
INSERT INTO `dynamic_form_configration` VALUES (6, 1, 1, 20, '100px', 8, '权限搜索表单', 0);
INSERT INTO `dynamic_form_configration` VALUES (12, 1, 1, 20, '100px', 8, '权限新增表单', 0);
INSERT INTO `dynamic_form_configration` VALUES (13, 1, 1, 20, '100px', 8, '权限编辑表单', 0);
INSERT INTO `dynamic_form_configration` VALUES (14, 1, 1, 20, '100px', 8, '权限组查询表单', 0);
INSERT INTO `dynamic_form_configration` VALUES (15, 1, 1, 20, '100px', 8, '权限组新增表单', 0);

-- ----------------------------
-- Table structure for form_field_configration
-- ----------------------------
DROP TABLE IF EXISTS `form_field_configration`;
CREATE TABLE `form_field_configration`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `label` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `prop` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `gobal_index`(`type` ASC, `prop` ASC, `label` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 122 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of form_field_configration
-- ----------------------------
INSERT INTO `form_field_configration` VALUES (5, '创建时间', 'createTimeRange', 'date-range');
INSERT INTO `form_field_configration` VALUES (20, '创建时间', 'createTimeRange', 'date-range');
INSERT INTO `form_field_configration` VALUES (30, '创建时间', 'createTimeRange', 'date-range');
INSERT INTO `form_field_configration` VALUES (36, '创建时间', 'createTimeRange', 'date-range');
INSERT INTO `form_field_configration` VALUES (77, '创建时间', 'createTimeRange', 'date-range');
INSERT INTO `form_field_configration` VALUES (6, '更新时间', 'updateTimeRange', 'date-range');
INSERT INTO `form_field_configration` VALUES (21, '更新时间', 'updateTimeRange', 'date-range');
INSERT INTO `form_field_configration` VALUES (31, '更新时间', 'updateTimeRange', 'date-range');
INSERT INTO `form_field_configration` VALUES (37, '更新时间', 'updateTimeRange', 'date-range');
INSERT INTO `form_field_configration` VALUES (78, '更新时间', 'updateTimeRange', 'date-range');
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
INSERT INTO `form_field_configration` VALUES (17, '公司名称', 'likeName', 'input');
INSERT INTO `form_field_configration` VALUES (105, '权限名称', 'name', 'input');
INSERT INTO `form_field_configration` VALUES (96, '权限名称 ', 'nameLike', 'input');
INSERT INTO `form_field_configration` VALUES (101, '权限名称 ', 'nameLike', 'input');
INSERT INTO `form_field_configration` VALUES (107, '权限名称', 'nameLike', 'input');
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
INSERT INTO `form_field_configration` VALUES (19, '公司状态', 'flagEquals', 'select');
INSERT INTO `form_field_configration` VALUES (81, '状态', 'flagEquals', 'select');
INSERT INTO `form_field_configration` VALUES (97, '状态', 'flagEquals', 'select');
INSERT INTO `form_field_configration` VALUES (102, '状态', 'flagEquals', 'select');
INSERT INTO `form_field_configration` VALUES (4, '用户状态', 'flagEquals', 'select');
INSERT INTO `form_field_configration` VALUES (29, '用户状态', 'flagEquals', 'select');
INSERT INTO `form_field_configration` VALUES (35, '用户状态', 'flagEquals', 'select');
INSERT INTO `form_field_configration` VALUES (76, '用户状态', 'flagEquals', 'select');
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
  `id` bigint NOT NULL AUTO_INCREMENT,
  `field_id` bigint NULL DEFAULT NULL,
  `form_id` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `form_field_relation_form_id_index`(`form_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 122 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

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
INSERT INTO `form_field_relation` VALUES (73, 73, 1);
INSERT INTO `form_field_relation` VALUES (74, 74, 1);
INSERT INTO `form_field_relation` VALUES (75, 75, 1);
INSERT INTO `form_field_relation` VALUES (76, 76, 1);
INSERT INTO `form_field_relation` VALUES (77, 77, 1);
INSERT INTO `form_field_relation` VALUES (78, 78, 1);
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

-- ----------------------------
-- Table structure for property_relation
-- ----------------------------
DROP TABLE IF EXISTS `property_relation`;
CREATE TABLE `property_relation`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `property_id` bigint NULL DEFAULT NULL,
  `relation_id` bigint NULL DEFAULT NULL,
  `relation_type` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `property_relation_property_id_index`(`property_id` ASC) USING BTREE,
  INDEX `property_relation_relation_id_index`(`relation_id` ASC, `relation_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 312 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

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

-- ----------------------------
-- Table structure for property_value
-- ----------------------------
DROP TABLE IF EXISTS `property_value`;
CREATE TABLE `property_value`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `prop_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `transformer_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `prop_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
  `value_class` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `name_idx`(`prop_name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 312 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

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

-- ----------------------------
-- Table structure for sys_company
-- ----------------------------
DROP TABLE IF EXISTS `sys_company`;
CREATE TABLE `sys_company`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `company_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
  `flag` int NULL DEFAULT NULL,
  `logo_image` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `tax_number` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `create_by` bigint NULL DEFAULT NULL,
  `update_by` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_company_name`(`company_name` ASC) USING BTREE,
  INDEX `idx_flag`(`flag` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_update_time`(`update_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 740830978083127297 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_company
-- ----------------------------
INSERT INTO `sys_company` VALUES (1, '系统公司', NULL, NULL, 5, '系统公司', '系统公司', '2026-05-12 13:09:52', NULL, NULL);
INSERT INTO `sys_company` VALUES (2, '测试公司', '2025-09-03 12:20:00', '这是一个测试公司', 1, '', '11223355', '2026-07-13 11:12:38', NULL, 1);
INSERT INTO `sys_company` VALUES (740830978083127296, '待删除公司', '2026-08-07 15:17:49', '', 0, NULL, 'NeedToDelete', NULL, 1, NULL);

-- ----------------------------
-- Table structure for sys_data_dic
-- ----------------------------
DROP TABLE IF EXISTS `sys_data_dic`;
CREATE TABLE `sys_data_dic`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `create_time` datetime(6) NULL DEFAULT NULL,
  `data_dic_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `data_dic_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `flag` int NULL DEFAULT NULL,
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `update_time` datetime(6) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_data_dic_name_code`(`data_dic_name` ASC, `data_dic_code` ASC) USING BTREE,
  INDEX `idx_flag`(`flag` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_data_dic
-- ----------------------------
INSERT INTO `sys_data_dic` VALUES (7, '2026-06-24 15:32:55.021000', 'SYSTEM', '系统', 1, '系统字典', NULL);
INSERT INTO `sys_data_dic` VALUES (8, '2026-06-25 13:22:15.166000', 'DIC_XH_GD_SCRK_DOC_MAP', '兴昊工单生成入库单映射', 1, '', NULL);
INSERT INTO `sys_data_dic` VALUES (9, '2026-07-01 09:43:24.350000', 'DIC_XH_SCRK_FEATURE_FALG_MAP', '兴昊ERP单据创建标志字典', 1, 'Label对应工单单据类型。Value对应功能标志。1:插入ERP生产入库单。2:插入且审核。', NULL);
INSERT INTO `sys_data_dic` VALUES (10, '2026-07-24 09:33:52.242000', 'GENRIC_API_ID_DIC', '泛化调用接口ID字典', 0, NULL, '2026-08-04 11:15:35.003000');
INSERT INTO `sys_data_dic` VALUES (11, '2026-07-24 09:35:44.678000', 'GENRIC_API_METHOD_DIC', '泛化调用方法映射字典', 0, NULL, '2026-08-04 11:15:38.989000');
INSERT INTO `sys_data_dic` VALUES (12, '2026-07-24 14:30:44.230000', 'GENRIC_API_INTERFACE_DIC', '泛化调用接口类型字典', 0, NULL, '2026-08-04 11:15:44.949000');

-- ----------------------------
-- Table structure for sys_department
-- ----------------------------
DROP TABLE IF EXISTS `sys_department`;
CREATE TABLE `sys_department`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `company_id` bigint NOT NULL,
  `create_time` datetime(6) NULL DEFAULT NULL,
  `department_code` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `department_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `flag` int NULL DEFAULT NULL,
  `manager` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `update_time` datetime(6) NULL DEFAULT NULL,
  `create_by` bigint NULL DEFAULT NULL,
  `update_by` bigint NULL DEFAULT NULL,
  `parent_id` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_name_code`(`department_name` ASC, `department_code` ASC) USING BTREE,
  INDEX `idx_flag`(`flag` ASC) USING BTREE,
  INDEX `idx_create_time`(`create_time` ASC) USING BTREE,
  INDEX `idx_update_time`(`update_time` ASC) USING BTREE,
  INDEX `idx_company_id`(`company_id` ASC) USING BTREE,
  INDEX `idx_parent_id`(`parent_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3511711354564116481 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_department
-- ----------------------------
INSERT INTO `sys_department` VALUES (2, 2, '2026-05-12 14:55:22.304000', '', '销售部', '', 1, '', '2026-07-16 11:10:15.431000', NULL, 1, NULL);
INSERT INTO `sys_department` VALUES (4, 2, '2026-05-12 14:56:45.687000', '', '销售A组', '', 2, '', NULL, NULL, NULL, NULL);
INSERT INTO `sys_department` VALUES (6, 2, '2026-06-15 18:45:22.072000', 'XZ1', '行政部', '', 1, '', '2026-07-16 11:09:21.701000', NULL, 1, NULL);
INSERT INTO `sys_department` VALUES (3511711354564116480, 2, '2026-07-13 11:27:11.217000', 'SCB', '生产部', '', 1, '', '2026-07-16 11:09:26.360000', 1, 1, NULL);

-- ----------------------------
-- Table structure for sys_generic_api
-- ----------------------------
DROP TABLE IF EXISTS `sys_generic_api`;
CREATE TABLE `sys_generic_api`  (
  `id` bigint NOT NULL,
  `api_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `api_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `app_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `create_by` bigint NULL DEFAULT NULL,
  `create_time` datetime(6) NULL DEFAULT NULL,
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `flag` int NULL DEFAULT NULL,
  `interface_name` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `method_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `need_auth` bit(1) NOT NULL,
  `update_by` bigint NULL DEFAULT NULL,
  `update_time` datetime(6) NULL DEFAULT NULL,
  `version` int NULL DEFAULT NULL,
  `pre_auth_beans` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_generic_api_api_id`(`api_id` ASC) USING BTREE,
  INDEX `idx_generic_api_app_name`(`app_name` ASC) USING BTREE,
  INDEX `idx_generic_api_flag`(`flag` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_generic_api
-- ----------------------------
INSERT INTO `sys_generic_api` VALUES (736388709422727168, 'MES.OUTSTATION.BARCODE', 'MES出站码查询', 'dubbo-xh-mes-service', 1, '2026-07-26 09:05:49.737000', '', 1, 'com.tbs.xh.mes.service.IMesQueryService', 'queryOutStationBarCodeInfos', b'1', 1, '2026-07-26 17:58:48.265000', NULL, '');
INSERT INTO `sys_generic_api` VALUES (739354793914400768, 'OA.QUERY.USER', '根据工号查询OA用户', 'dubbo-xh-system-service', 1, '2026-08-03 13:31:59.403000', '', 1, 'com.tbs.xh.eteams.IOAService', 'getUser', b'0', NULL, '2026-08-03 13:31:59.401000', NULL, '');
INSERT INTO `sys_generic_api` VALUES (739355309184647168, 'OA.MESSAGE.SEND', 'OA发送消息', 'dubbo-xh-system-service', 1, '2026-08-03 13:34:02.264000', '', 1, 'com.tbs.xh.eteams.IOAService', 'sendMsg', b'1', NULL, '2026-08-03 13:34:02.262000', NULL, 'AccountActived');
INSERT INTO `sys_generic_api` VALUES (739720254627774464, 'ERP.SCAN.QUERY.TYPES', '获取所有已支持的表单类型', 'dubbo-xh-system-service', 1, '2026-08-04 13:44:12.027000', '', 1, 'com.tbs.xh.e10.service.IDocSaveService', 'getAllScanDocTypes', b'0', NULL, NULL, NULL, '');
INSERT INTO `sys_generic_api` VALUES (739724441906315264, 'ERP.DOC.SCAN.DETAILS', '获取单据扫描配置详情', 'dubbo-xh-system-service', 1, '2026-08-04 14:00:50.358000', '', 1, 'com.tbs.xh.e10.service.IDocSaveService', 'getDocScanConfig', b'0', NULL, NULL, NULL, '');
INSERT INTO `sys_generic_api` VALUES (739727822427258880, 'ERP.DOC.SCAN.CONFIG.SAVE', '储存单据扫描配置', 'dubbo-xh-system-service', 1, '2026-08-04 14:14:16.337000', '', 1, 'com.tbs.xh.e10.service.IDocSaveService', 'saveDocScanConfig', b'1', NULL, NULL, NULL, 'AccountActived');
INSERT INTO `sys_generic_api` VALUES (739730395020394496, 'ERP.SCAN.QUERY.SCAN_ROUTE', '获取单据保存后需要通知的用户工号', 'dubbo-xh-system-service', 1, '2026-08-04 14:24:29.694000', '', 1, 'com.tbs.xh.e10.service.IDocSaveService', 'getSaveStatusRoute', b'0', NULL, NULL, NULL, '');
INSERT INTO `sys_generic_api` VALUES (739732892111863808, 'ERP.DOC.SCAN.ADD.ROUTE', '新增单据通知人路由信息', 'dubbo-xh-system-service', 1, '2026-08-04 14:34:25.043000', '', 1, 'com.tbs.xh.e10.service.IDocSaveService', 'saveDocSaveStatusRoute', b'1', 1, '2026-08-04 14:45:59.297000', NULL, 'AccountActived');
INSERT INTO `sys_generic_api` VALUES (739734309090361344, 'ERP.DOC.SCAN.DELETE.ROUTE', '删除单据通知人信息', 'dubbo-xh-system-service', 1, '2026-08-04 14:40:02.881000', '', 1, 'com.tbs.xh.e10.service.IDocSaveService', 'deleteDocSaveStatusRoute', b'1', 1, '2026-08-04 14:46:08.642000', NULL, 'AccountActived');
INSERT INTO `sys_generic_api` VALUES (739735764882948096, 'ERP.DOC.SCAN.DELETE.CONFIG', '删除单据扫描配置。同时删除相关的通知人配置', 'dubbo-xh-system-service', 1, '2026-08-04 14:45:49.970000', '', 1, 'com.tbs.xh.e10.service.IDocSaveService', 'deleteDocScanConfig', b'1', NULL, NULL, NULL, 'AccountActived');

-- ----------------------------
-- Table structure for sys_login_info
-- ----------------------------
DROP TABLE IF EXISTS `sys_login_info`;
CREATE TABLE `sys_login_info`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `create_time` datetime NULL DEFAULT NULL,
  `flag` int NULL DEFAULT NULL,
  `token` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `user_details` mediumblob NULL,
  `user_details_target_class` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `user_details_transformer_impl` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `user_id` bigint NULL DEFAULT NULL,
  `token_expire_time` datetime NULL DEFAULT NULL,
  `session_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_flag`(`flag` ASC) USING BTREE,
  INDEX `idx_time`(`create_time` ASC, `update_time` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_token`(`token` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3513492933309366275 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_login_info
-- ----------------------------

-- ----------------------------
-- Table structure for sys_operate_record
-- ----------------------------
DROP TABLE IF EXISTS `sys_operate_record`;
CREATE TABLE `sys_operate_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cost_time` bigint NULL DEFAULT NULL,
  `method_type` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `operate_content` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `operate_error` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
  `operate_ip` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `operate_param_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
  `operate_result_json` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL,
  `operate_time` datetime NULL DEFAULT NULL,
  `operate_type` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `operate_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `operate_user_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `operate_error_level` int NULL DEFAULT NULL,
  `system_log_flag` int NULL DEFAULT NULL,
  `trace_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `common_index`(`operate_content` ASC, `operate_type` ASC) USING BTREE,
  INDEX `time_idx`(`operate_time` ASC) USING BTREE,
  INDEX `error_level_idx`(`operate_error_level` ASC) USING BTREE,
  INDEX `user_idx`(`operate_user_name` ASC, `operate_ip` ASC) USING BTREE,
  INDEX `url_idx`(`operate_url` ASC) USING BTREE,
  INDEX `method_idx`(`method_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 41105 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_operate_record
-- ----------------------------

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `create_time` datetime NULL DEFAULT NULL,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `flag` int NULL DEFAULT NULL,
  `role` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `create_by` bigint NULL DEFAULT NULL,
  `update_by` bigint NULL DEFAULT NULL,
  `data_scope` int NULL DEFAULT NULL,
  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `type` int NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UKmwbqlu5c82jfd2w9oa9d6e87d`(`role` ASC) USING BTREE,
  INDEX `idx_flag`(`flag` ASC) USING BTREE,
  INDEX `idx_time`(`create_time` ASC, `update_time` ASC) USING BTREE,
  INDEX `idx_role`(`role` ASC) USING BTREE,
  INDEX `idx_role_type`(`role` ASC, `type` ASC) USING BTREE,
  INDEX `idx_name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3509573881151094785 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (740488168846917632, '2026-08-06 16:35:37', '', 1, 'DATA.ADMIN', '2026-08-06 16:42:54', 1, 1, 1, '管理员权限', 2);
INSERT INTO `sys_role` VALUES (740488338347130880, '2026-08-06 16:36:17', '', 1, 'DATA.DEPT', '2026-08-06 16:42:46', 1, 1, 2, '部门下数据权限', 2);
INSERT INTO `sys_role` VALUES (740490129688231936, '2026-08-06 16:43:25', '', 1, 'MENU.ADMIN', NULL, 1, NULL, 1, '管理员菜单权限', 1);
INSERT INTO `sys_role` VALUES (740490307421863936, '2026-08-06 16:44:07', '', 1, 'MENU.DEV', NULL, 1, NULL, 1, '开发者菜单权限', 1);
INSERT INTO `sys_role` VALUES (740749058062155776, '2026-08-07 09:52:18', '包含单个查询和条件查询', 1, 'API.QUERY.USER', '2026-08-07 16:26:53', 1, 1, 1, '用户查询权限', 3);
INSERT INTO `sys_role` VALUES (740749150261346304, '2026-08-07 09:52:40', '包含公司和部门的单个查询及条件查询', 1, 'API.QUERY.DEPT', '2026-08-07 16:27:15', 1, 1, 1, '公司部门查询权限', 3);
INSERT INTO `sys_role` VALUES (740749324849250304, '2026-08-07 09:53:21', '', 1, 'API.SAVE.USER', NULL, 1, NULL, NULL, '用户保存权限', 3);
INSERT INTO `sys_role` VALUES (740749478247530496, '2026-08-07 09:53:58', '', 1, 'API.SAVE.DEPT', NULL, 1, NULL, NULL, '公司部门储存权限', 3);
INSERT INTO `sys_role` VALUES (740815695784181760, '2026-08-07 14:17:06', '', 1, 'API.QUERY.LOGIN', NULL, 1, NULL, NULL, '用户登录信息查询权限', 3);
INSERT INTO `sys_role` VALUES (740815895567269888, '2026-08-07 14:17:53', '', 1, 'API.OPERATE.LOGIN', NULL, 1, NULL, NULL, '用户登录信息操作权限', 3);
INSERT INTO `sys_role` VALUES (740816419821715456, '2026-08-07 14:19:58', '', 1, 'DATA.SELF', NULL, 1, NULL, 3, '自己创建数据权限', 2);

-- ----------------------------
-- Table structure for sys_role_group
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_group`;
CREATE TABLE `sys_role_group`  (
  `id` bigint NOT NULL,
  `create_by` bigint NULL DEFAULT NULL,
  `create_time` datetime(6) NULL DEFAULT NULL,
  `delete_by` bigint NULL DEFAULT NULL,
  `delete_time` datetime(6) NULL DEFAULT NULL,
  `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `flag` int NULL DEFAULT NULL,
  `role_group_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `update_by` bigint NULL DEFAULT NULL,
  `update_time` datetime(6) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_sys_role_group_flag`(`flag` ASC) USING BTREE,
  INDEX `idx_sys_role_group_role_group_name`(`role_group_name` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_group
-- ----------------------------
INSERT INTO `sys_role_group` VALUES (740124338757304320, 1, '2026-08-05 16:29:53.174000', NULL, NULL, '系统中最基础的权限', 1, '基础用户权限', NULL, NULL);
INSERT INTO `sys_role_group` VALUES (740491594708287488, 1, '2026-08-06 16:49:13.827000', NULL, NULL, NULL, 1, '管理员权限', NULL, NULL);
INSERT INTO `sys_role_group` VALUES (740721847200382976, 1, '2026-08-07 08:04:10.053000', NULL, NULL, NULL, 1, '开发者', NULL, NULL);

-- ----------------------------
-- Table structure for sys_role_group_relation
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_group_relation`;
CREATE TABLE `sys_role_group_relation`  (
  `id` bigint NOT NULL,
  `create_by` bigint NULL DEFAULT NULL,
  `create_time` datetime(6) NULL DEFAULT NULL,
  `delete_by` bigint NULL DEFAULT NULL,
  `delete_time` datetime(6) NULL DEFAULT NULL,
  `group_id` bigint NULL DEFAULT NULL,
  `role_id` bigint NULL DEFAULT NULL,
  `update_by` bigint NULL DEFAULT NULL,
  `update_time` datetime(6) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_role_group_relation_role_id`(`role_id` ASC) USING BTREE,
  INDEX `idx_role_group_relation_group_id`(`group_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role_group_relation
-- ----------------------------
INSERT INTO `sys_role_group_relation` VALUES (740491647237750784, NULL, NULL, NULL, NULL, 740491594708287488, 740490129688231936, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (740491648600899584, NULL, NULL, NULL, NULL, 740491594708287488, 740488168846917632, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (740721964045303808, NULL, NULL, NULL, NULL, 740721847200382976, 740490307421863936, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (740750249487761408, NULL, NULL, NULL, NULL, 740491594708287488, 740749058062155776, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (740750249684893696, NULL, NULL, NULL, NULL, 740491594708287488, 740749150261346304, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (740750249890414592, NULL, NULL, NULL, NULL, 740491594708287488, 740749324849250304, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (740750250049798144, NULL, NULL, NULL, NULL, 740491594708287488, 740749478247530496, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (740750346464264192, NULL, NULL, NULL, NULL, 740124338757304320, 740749058062155776, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (740750346594287616, NULL, NULL, NULL, NULL, 740124338757304320, 740749150261346304, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (740816474725154816, NULL, NULL, NULL, NULL, 740124338757304320, 740816419821715456, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (740816602362019840, NULL, NULL, NULL, NULL, 740491594708287488, 740815895567269888, NULL, NULL);
INSERT INTO `sys_role_group_relation` VALUES (740816602483654656, NULL, NULL, NULL, NULL, 740491594708287488, 740815695784181760, NULL, NULL);

-- ----------------------------
-- Table structure for sys_token
-- ----------------------------
DROP TABLE IF EXISTS `sys_token`;
CREATE TABLE `sys_token`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `create_time` datetime NULL DEFAULT NULL,
  `create_type` int NULL DEFAULT NULL,
  `expire_time` datetime NULL DEFAULT NULL,
  `flag` int NULL DEFAULT NULL,
  `ip_address` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `mac_address` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `token` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `token_type` int NOT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `user_name` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `session_id` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_token`(`token` ASC) USING BTREE,
  INDEX `ix_user`(`user_name` ASC) USING BTREE,
  INDEX `ix_time`(`create_time` ASC, `update_time` ASC) USING BTREE,
  INDEX `ix_flag`(`flag` ASC) USING BTREE,
  INDEX `ix_expire`(`expire_time` ASC) USING BTREE,
  INDEX `ix_token_type`(`token_type` ASC) USING BTREE,
  INDEX `ix_session`(`session_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6771699607021295902 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_token
-- ----------------------------

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `create_time` datetime NULL DEFAULT NULL,
  `flag` int NULL DEFAULT NULL,
  `nick_name` varchar(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `password` varchar(72) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `user_name` varchar(72) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NOT NULL,
  `create_by` bigint NULL DEFAULT NULL,
  `update_by` bigint NULL DEFAULT NULL,
  `dept_id` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_username_password`(`user_name` ASC, `password` ASC) USING BTREE,
  INDEX `idx_nickname`(`nick_name` ASC) USING BTREE,
  INDEX `idx_flag`(`flag` ASC) USING BTREE,
  INDEX `idx_time`(`create_time` ASC, `update_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 740822608424992769 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (1, 'https://q9.itc.cn/q_70/images03/20241013/d770472d4906402c866b9c71a0c9927c.jpeg', '2025-07-23 16:34:17', 1, 'abstergo', '!Ty19970923', '管理员账户!', '2026-06-06 09:05:33', '223344', NULL, NULL, NULL);
INSERT INTO `sys_user` VALUES (2, 'https://ts1.tc.mm.bing.net/th/id/OIP-C.LjLA176cgjhpSHo2kHU_1AAAAA?rs=1&pid=ImgDetMain&o=7&rm=3', '2025-08-16 11:56:25', -1, '测试用户1', '!Ty19970923', 'te367898', '2026-06-30 13:54:46', 'test123456', 1, NULL, NULL);
INSERT INTO `sys_user` VALUES (3, '', '2025-08-16 12:13:10', -1, 'jackBauer', 'Ty1997923!', 'test123', '2026-06-30 13:54:49', '456789', 1, NULL, NULL);
INSERT INTO `sys_user` VALUES (4, '', '2025-08-16 12:38:43', -1, 'TonyAmeda', 'Ty1997923!', '', '2026-06-30 13:54:52', '789456', 1, NULL, NULL);
INSERT INTO `sys_user` VALUES (5, '', '2025-08-16 18:21:16', -1, '123456789', '!Ty1997923', '', '2026-06-30 13:54:57', '123456789', 1, NULL, NULL);
INSERT INTO `sys_user` VALUES (6, 'https://img0.baidu.com/it/u=288127173,2280192168&fm=253&fmt=auto&app=138&f=JPEG?w=500&h=500', '2025-08-16 19:04:14', -1, 'tonyJames', '!Ty1997923', '', '2026-06-30 13:55:00', '1234565', 1, NULL, NULL);
INSERT INTO `sys_user` VALUES (7, '', '2025-08-21 12:10:51', -1, 'teset123456', '!Ty199709923', '', '2026-06-30 13:55:02', 'winTest1234', 1, NULL, NULL);
INSERT INTO `sys_user` VALUES (8, NULL, '2025-09-03 10:57:06', -1, 'jackTom', '!Ty19970923', NULL, '2026-06-30 13:55:05', '77889945', 1, NULL, NULL);
INSERT INTO `sys_user` VALUES (9, NULL, '2025-09-13 18:46:40', -1, 'jamesJohn', '!Ty19970923', NULL, '2026-06-30 13:55:08', '789456Tony', 1, NULL, NULL);
INSERT INTO `sys_user` VALUES (10, 'https://q2.itc.cn/q_70/images03/20250225/e8117dd40aae4db5a64461f1ea0d16fc.jpeg', '2025-09-13 18:49:17', -1, '123456', '!Ty19970923', NULL, '2026-06-30 13:55:11', 'tonyJACK', 1, NULL, NULL);
INSERT INTO `sys_user` VALUES (11, NULL, '2025-11-12 17:23:59', -1, '22334567', '!Ty19970923', NULL, '2026-06-30 13:54:35', 'helloworld', 1, NULL, NULL);
INSERT INTO `sys_user` VALUES (12, NULL, '2025-11-14 18:40:54', -1, 'gotoMaker', '!Ty19970923', NULL, NULL, 'gotoMaker', 1, NULL, NULL);
INSERT INTO `sys_user` VALUES (13, NULL, '2025-11-17 17:10:51', -1, '测试用户', 'Password123!', NULL, NULL, 'testuser123', 1, NULL, NULL);
INSERT INTO `sys_user` VALUES (14, 'https://fuss10.elemecdn.com/e/5d/4a731a90594a4af544c0c25941171jpeg.jpeg', '2025-07-23 16:13:10', 1, '超级用户', '!Ty19970923', 'super user for me', '2025-11-17 17:11:10', '123456', 1, NULL, NULL);
INSERT INTO `sys_user` VALUES (16, NULL, '2026-06-15 18:31:21', 1, 'bbbbb', '!Ty19970923', NULL, '2026-07-31 11:16:46', 'aaaaaaaa', 1, 1, NULL);
INSERT INTO `sys_user` VALUES (17, NULL, '2026-06-15 18:45:51', 1, 'bbbbb', '!Ty19970923', NULL, '2026-07-31 11:16:32', 'aaaaaa', 1, 1, NULL);
INSERT INTO `sys_user` VALUES (18, NULL, '2026-06-25 14:46:30', 1, '童泊森', '01505', NULL, '2026-06-25 14:49:41', '01505', 1, NULL, NULL);
INSERT INTO `sys_user` VALUES (19, NULL, '2026-06-25 15:02:30', 1, '李书倩', '00633', NULL, '2026-06-25 16:27:42', '00633', 1, NULL, NULL);
INSERT INTO `sys_user` VALUES (737192189162946560, NULL, '2026-07-28 14:18:33', 1, '史小艾', '00413', NULL, '2026-07-28 14:18:51', '00413', 1, 1, NULL);
INSERT INTO `sys_user` VALUES (737192428523487232, NULL, '2026-07-28 14:19:31', 1, '张进', '00129', NULL, '2026-07-28 14:22:21', '00219', 1, 1, NULL);
INSERT INTO `sys_user` VALUES (737192677283463168, NULL, '2026-07-28 14:20:31', 1, '汪宗超', '00688', NULL, '2026-07-28 14:22:09', '00688', 1, 1, NULL);
INSERT INTO `sys_user` VALUES (737192878358396928, NULL, '2026-07-28 14:21:19', 1, '肖立涛', '00341', NULL, '2026-07-28 14:22:12', '00341', 1, 1, NULL);
INSERT INTO `sys_user` VALUES (737193046583541760, NULL, '2026-07-28 14:21:59', 1, '陆辉', '01060', NULL, '2026-07-28 14:22:15', '01060', 1, 1, NULL);
INSERT INTO `sys_user` VALUES (738291152687464448, NULL, '2026-07-31 15:05:27', -1, 'test123', '!Ty19970923', NULL, NULL, 'Test123', 17, NULL, NULL);
INSERT INTO `sys_user` VALUES (739774367356944384, NULL, '2026-08-04 17:19:13', 1, 'admin', '123456', NULL, '2026-08-04 17:20:38', 'admin', 1, 1, NULL);
INSERT INTO `sys_user` VALUES (740814928180412416, NULL, '2026-08-07 14:14:01', -1, '123444', '01505', NULL, NULL, 'test12345', 18, NULL, NULL);
INSERT INTO `sys_user` VALUES (740822608424992768, NULL, '2026-08-07 14:44:34', 2, '223344', '!Ty19970923', NULL, NULL, 'helloworld', 1, NULL, NULL);

-- ----------------------------
-- Table structure for system_data_dic_item
-- ----------------------------
DROP TABLE IF EXISTS `system_data_dic_item`;
CREATE TABLE `system_data_dic_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `create_time` datetime(6) NULL DEFAULT NULL,
  `dic_code` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `flag` int NULL DEFAULT NULL,
  `label` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `remark` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  `sort` int NULL DEFAULT NULL,
  `update_time` datetime(6) NULL DEFAULT NULL,
  `value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_system_data_dic_item_dic_code`(`dic_code` ASC) USING BTREE,
  INDEX `idx_flag`(`flag` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of system_data_dic_item
-- ----------------------------
INSERT INTO `system_data_dic_item` VALUES (1, '2026-06-24 15:38:08.818000', 'SYSTEM', -1, '用户名密码校验功能开关', '开启用户名密码校验功能', 0, '2026-07-07 08:27:13.288000', '0');
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
  `id` bigint NOT NULL,
  `role_group_id` bigint NULL DEFAULT NULL,
  `user_id` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_system_role_group_user_relation_role_group_id`(`role_group_id` ASC) USING BTREE,
  INDEX `idx_system_role_group_user_relation_user_id`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of system_role_group_user_relation
-- ----------------------------
INSERT INTO `system_role_group_user_relation` VALUES (740499023877636096, 740491594708287488, 1);
INSERT INTO `system_role_group_user_relation` VALUES (740750631991508992, 740491594708287488, 739774367356944384);
INSERT INTO `system_role_group_user_relation` VALUES (740816960463306752, 740124338757304320, 18);
INSERT INTO `system_role_group_user_relation` VALUES (740848877569048576, 740721847200382976, 1);

-- ----------------------------
-- Table structure for system_user_company_relation
-- ----------------------------
DROP TABLE IF EXISTS `system_user_company_relation`;
CREATE TABLE `system_user_company_relation`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `company_id` bigint NULL DEFAULT NULL,
  `user_id` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id_company_id`(`user_id` ASC, `company_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_company_id`(`company_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_bin ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of system_user_company_relation
-- ----------------------------
INSERT INTO `system_user_company_relation` VALUES (1, 1, 0);

-- ----------------------------
-- Table structure for system_user_department_relation
-- ----------------------------
DROP TABLE IF EXISTS `system_user_department_relation`;
CREATE TABLE `system_user_department_relation`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `department_id` bigint NULL DEFAULT NULL,
  `user_id` bigint NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_id_department_id`(`user_id` ASC, `department_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_department_id`(`department_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3509885629540335617 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

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
-- Table structure for tree_closure
-- ----------------------------
DROP TABLE IF EXISTS `tree_closure`;
CREATE TABLE `tree_closure`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `ancestor_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `depth` int NOT NULL,
  `descendant_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `group_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK4q1vrtow52u2ih3utui5nxues`(`ancestor_id` ASC, `descendant_id` ASC) USING BTREE,
  INDEX `IDXlcjh1709srvurk844d5l9jjtu`(`ancestor_id` ASC) USING BTREE,
  INDEX `IDX5spq70g67u0mhko5tuwntwxa3`(`descendant_id` ASC) USING BTREE,
  INDEX `IDXgkkwbnbsyakv2u533604sdto1`(`depth` ASC) USING BTREE,
  INDEX `IDXi7fuflkybyivi3mhmaj5cfynt`(`group_name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 47 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tree_closure
-- ----------------------------
INSERT INTO `tree_closure` VALUES (23, 'COMPANY_TREE::2', 0, 'COMPANY_TREE::2', 'COMPANY_TREE');
INSERT INTO `tree_closure` VALUES (32, 'DEPARTMENT_TREE::2', 0, 'DEPARTMENT_TREE::2', 'DEPARTMENT_TREE');
INSERT INTO `tree_closure` VALUES (34, 'DEPARTMENT_TREE::4', 0, 'DEPARTMENT_TREE::4', 'DEPARTMENT_TREE');
INSERT INTO `tree_closure` VALUES (35, 'DEPARTMENT_TREE::2', 1, 'DEPARTMENT_TREE::4', 'DEPARTMENT_TREE');
INSERT INTO `tree_closure` VALUES (44, 'DEPARTMENT_TREE::6', 0, 'DEPARTMENT_TREE::6', 'DEPARTMENT_TREE');
INSERT INTO `tree_closure` VALUES (45, 'DEPARTMENT_TREE::3511711354564116480', 0, 'DEPARTMENT_TREE::3511711354564116480', 'DEPARTMENT_TREE');

-- ----------------------------
-- Table structure for tree_node
-- ----------------------------
DROP TABLE IF EXISTS `tree_node`;
CREATE TABLE `tree_node`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `create_time` datetime(6) NULL DEFAULT NULL,
  `entity_id` bigint NULL DEFAULT NULL,
  `group_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `node_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `parent_id` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `update_time` datetime(6) NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UKj31ps1fvxja2xe7166bjvweme`(`group_name` ASC, `node_id` ASC) USING BTREE,
  INDEX `IDXdnk5hk5vr111xcunlqkjfni6h`(`group_name` ASC, `parent_id` ASC) USING BTREE,
  INDEX `IDX5ytbu0cqp6qalcb0fdf9kl31r`(`entity_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 30 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of tree_node
-- ----------------------------
INSERT INTO `tree_node` VALUES (18, '2026-05-12 14:26:12.767000', 2, 'COMPANY_TREE', 'COMPANY_TREE::2', NULL, NULL);
INSERT INTO `tree_node` VALUES (22, '2026-05-12 14:55:22.323000', 2, 'DEPARTMENT_TREE', 'DEPARTMENT_TREE::2', NULL, NULL);
INSERT INTO `tree_node` VALUES (23, '2026-05-12 14:56:45.706000', 4, 'DEPARTMENT_TREE', 'DEPARTMENT_TREE::4', 'DEPARTMENT_TREE::2', '2026-05-12 14:57:02.332000');
INSERT INTO `tree_node` VALUES (27, '2026-06-15 18:45:22.100000', 6, 'DEPARTMENT_TREE', 'DEPARTMENT_TREE::6', NULL, NULL);
INSERT INTO `tree_node` VALUES (28, '2026-07-13 11:27:11.464000', 3511711354564116480, 'DEPARTMENT_TREE', 'DEPARTMENT_TREE::3511711354564116480', NULL, NULL);

SET FOREIGN_KEY_CHECKS = 1;

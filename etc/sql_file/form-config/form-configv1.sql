/*
 Navicat Premium Data Transfer

 Source Server         : nas
 Source Server Type    : MariaDB
 Source Server Version : 101106
 Source Host           : 192.168.50.198:3306
 Source Schema         : app_system

 Target Server Type    : MariaDB
 Target Server Version : 101106
 File Encoding         : 65001

 Date: 03/11/2025 17:09:55
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for property_value
-- ----------------------------
DROP TABLE IF EXISTS `property_value`;
CREATE TABLE `property_value`
(
    `id`               bigint(20)                                             NOT NULL AUTO_INCREMENT,
    `prop_name`        varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL DEFAULT NULL,
    `transformer_type` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin  NULL DEFAULT NULL,
    `prop_value`       text CHARACTER SET utf8mb4 COLLATE utf8mb4_bin         NULL DEFAULT NULL,
    `value_class`      varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 288
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of property_value
-- ----------------------------
INSERT INTO `property_value`
VALUES (1, 'type', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"number\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (2, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入用户ID\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (3, 'prefix-icon', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"el-icon-search\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (4, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入用户名\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (5, 'prefix-icon', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"el-icon-user\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (6, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入昵称\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (7, 'prefix-icon', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"el-icon-star-off\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (8, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请选择用户状态\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (9, 'options', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer',
        '[{\"label\":\"激活的\",\"value\":\"1\"},{\"label\":\"被锁定的\",\"value\":\"2\"},{\"label\":\"禁用的\",\"value\":\"0\"}]',
        'java.util.ArrayList');
INSERT INTO `property_value`
VALUES (10, 'value-format', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer',
        '\"yyyy-MM-dd HH:mm:ss\"', 'java.lang.String');
INSERT INTO `property_value`
VALUES (11, 'format', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"yyyy-MM-dd\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (12, 'start-placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"开始日期\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (13, 'end-placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"结束日期\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (14, 'value-format', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer',
        '\"yyyy-MM-dd HH:mm:ss\"', 'java.lang.String');
INSERT INTO `property_value`
VALUES (15, 'format', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"yyyy-MM-dd\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (16, 'start-placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"开始日期\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (17, 'end-placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"结束日期\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (18, 'size', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"medium\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (19, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入用户名\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (20, 'clearable', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (21, 'prefix-icon', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"el-icon-user\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (22, 'size', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"medium\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (23, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入昵称\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (24, 'clearable', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (25, 'prefix-icon', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"el-icon-star-off\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (26, 'size', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"medium\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (27, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入密码\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (28, 'clearable', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (29, 'prefix-icon', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"el-icon-lock\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (30, 'type', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"password\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (31, 'showPassword', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (32, 'size', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"medium\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (33, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入备注信息\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (34, 'type', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"textarea\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (35, 'rows', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '2', 'java.lang.Integer');
INSERT INTO `property_value`
VALUES (36, 'size', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"medium\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (37, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入用户名\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (38, 'clearable', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (39, 'prefix-icon', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"el-icon-user\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (40, 'size', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"medium\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (41, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入昵称\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (42, 'clearable', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (43, 'prefix-icon', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"el-icon-star-off\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (44, 'size', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"medium\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (45, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入备注信息\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (46, 'type', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"textarea\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (47, 'rows', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '2', 'java.lang.Integer');
INSERT INTO `property_value`
VALUES (48, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入公司Id\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (49, 'prefix-icon', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"el-icon-search\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (50, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入公司名称\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (51, 'prefix-icon', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer',
        '\"el-icon-office-building\"', 'java.lang.String');
INSERT INTO `property_value`
VALUES (52, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入税号\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (53, 'prefix-icon', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"el-icon-document\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (54, 'size', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"medium\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (55, 'multiple', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (56, 'options', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer',
        '[{\"label\":\"系统\",\"value\":\"5\"},{\"label\":\"已激活\",\"value\":\"1\"},{\"label\":\"禁用\",\"value\":\"2\"},{\"label\":\"已删除\",\"value\":\"0\"}]',
        'java.util.ArrayList');
INSERT INTO `property_value`
VALUES (57, 'value-format', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer',
        '\"yyyy-MM-dd HH:mm:ss\"', 'java.lang.String');
INSERT INTO `property_value`
VALUES (58, 'format', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"yyyy-MM-dd\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (59, 'start-placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"开始日期\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (60, 'end-placeholer', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"结束日期\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (61, 'value-format', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer',
        '\"yyyy-MM-dd HH:mm:ss\"', 'java.lang.String');
INSERT INTO `property_value`
VALUES (62, 'format', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"yyyy-MM-dd\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (63, 'start-placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"开始日期\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (64, 'end-placeholer', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"结束日期\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (65, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入公司名称\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (66, 'prefix-icon', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer',
        '\"el-icon-office-building\"', 'java.lang.String');
INSERT INTO `property_value`
VALUES (67, 'size', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"medium\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (68, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入昵称\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (69, 'prefix-icon', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"el-icon-document\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (70, 'size', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"medium\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (71, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入公司描述信息\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (72, 'size', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"medium\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (73, 'type', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"textarea\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (74, 'rows', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '5', 'java.lang.Integer');
INSERT INTO `property_value`
VALUES (75, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入LogoUrl\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (76, 'size', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"medium\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (77, 'prefix-icon', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer',
        '\"el-icon-picture-outline\"', 'java.lang.String');
INSERT INTO `property_value`
VALUES (78, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入用户ID\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (79, 'type', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"number\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (80, 'prefix-icon', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"el-icon-search\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (81, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入用户名\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (82, 'prefix-icon', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"el-icon-user\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (83, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入昵称\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (84, 'prefix-icon', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"el-icon-star-off\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (85, 'options', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer',
        '[{\"label\":\"激活的\",\"value\":\"1\"},{\"label\":\"被锁定的\",\"value\":\"2\"},{\"label\":\"禁用的\",\"value\":\"0\"}]',
        'java.util.ArrayList');
INSERT INTO `property_value`
VALUES (86, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请选择用户状态\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (87, 'multiple', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (88, 'start-placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"开始日期\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (89, 'value-format', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer',
        '\"yyyy-MM-dd HH:mm:ss\"', 'java.lang.String');
INSERT INTO `property_value`
VALUES (90, 'format', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"yyyy-MM-dd\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (91, 'end-placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"结束日期\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (92, 'type', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"date\"', 'java.lang.String');
INSERT INTO `property_value`
VALUES (93, 'start-placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"开始日期\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (94, 'value-format', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer',
        '\"yyyy-MM-dd HH:mm:ss\"', 'java.lang.String');
INSERT INTO `property_value`
VALUES (95, 'format', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"yyyy-MM-dd\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (96, 'end-placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"结束日期\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (97, 'type', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"date\"', 'java.lang.String');
INSERT INTO `property_value`
VALUES (98, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入用户ID\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (99, 'type', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"number\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (100, 'prefix-icon', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"el-icon-search\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (101, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入用户名\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (102, 'prefix-icon', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"el-icon-user\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (103, 'type', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"text\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (104, 'clearable', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (105, 'show-word-limit', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (106, 'maxlength', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '64',
        'java.lang.Integer');
INSERT INTO `property_value`
VALUES (107, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入昵称\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (108, 'prefix-icon', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"el-icon-star-off\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (109, 'maxlength', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '64',
        'java.lang.Integer');
INSERT INTO `property_value`
VALUES (110, 'clearable', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (111, 'show-word-limit', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (112, 'type', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"text\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (113, 'options', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer',
        '[{\"label\":\"激活的\",\"value\":\"1\"},{\"label\":\"被锁定的\",\"value\":\"2\"},{\"label\":\"禁用的\",\"value\":\"0\"}]',
        'java.util.ArrayList');
INSERT INTO `property_value`
VALUES (114, 'multiple', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (115, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请选择用户状态\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (116, 'start-placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"开始日期\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (117, 'value-format', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer',
        '\"yyyy-MM-dd HH:mm:ss\"', 'java.lang.String');
INSERT INTO `property_value`
VALUES (118, 'format', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"yyyy-MM-dd\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (119, 'type', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"date\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (120, 'end-placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"结束日期\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (121, 'size', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"small\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (122, 'clearable', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (123, 'start-placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"开始日期\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (124, 'value-format', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer',
        '\"yyyy-MM-dd HH:mm:ss\"', 'java.lang.String');
INSERT INTO `property_value`
VALUES (125, 'format', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"yyyy-MM-dd\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (126, 'type', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"date\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (127, 'end-placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"结束日期\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (128, 'size', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"small\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (129, 'clearable', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (130, 'clearable', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (131, 'size', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"medium\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (132, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入用户名\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (133, 'prefix-icon', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"el-icon-user\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (134, 'clearable', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (135, 'size', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"medium\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (136, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入昵称\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (137, 'prefix-icon', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"el-icon-star-off\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (138, 'clearable', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (139, 'size', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"medium\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (140, 'showPassword', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (141, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入密码\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (142, 'type', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"password\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (143, 'prefix-icon', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"el-icon-lock\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (144, 'size', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"medium\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (145, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入备注信息\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (146, 'type', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"textarea\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (147, 'rows', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '2', 'java.lang.Integer');
INSERT INTO `property_value`
VALUES (148, 'clearable', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (149, 'size', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"medium\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (150, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入用户名\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (151, 'prefix-icon', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"el-icon-user\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (152, 'clearable', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (153, 'size', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"medium\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (154, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入昵称\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (155, 'prefix-icon', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"el-icon-star-off\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (156, 'clearable', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (157, 'size', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"medium\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (158, 'showPassword', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (159, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入密码\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (160, 'type', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"password\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (161, 'prefix-icon', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"el-icon-lock\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (162, 'size', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"medium\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (163, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入备注信息\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (164, 'type', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"textarea\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (165, 'rows', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '2', 'java.lang.Integer');
INSERT INTO `property_value`
VALUES (166, 'clearable', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (167, 'size', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"medium\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (168, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入用户名\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (169, 'prefix-icon', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"el-icon-user\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (170, 'clearable', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (171, 'size', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"medium\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (172, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入昵称\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (173, 'prefix-icon', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"el-icon-star-off\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (174, 'clearable', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (175, 'size', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"medium\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (176, 'showPassword', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (177, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入密码\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (178, 'type', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"password\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (179, 'prefix-icon', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"el-icon-lock\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (180, 'size', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"medium\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (181, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入备注信息\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (182, 'type', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"textarea\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (183, 'rows', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '2', 'java.lang.Integer');
INSERT INTO `property_value`
VALUES (184, 'clearable', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (185, 'size', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"medium\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (186, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入用户名\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (187, 'prefix-icon', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"el-icon-user\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (188, 'clearable', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (189, 'size', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"medium\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (190, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入昵称\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (191, 'prefix-icon', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"el-icon-star-off\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (192, 'clearable', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (193, 'size', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"medium\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (194, 'showPassword', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (195, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入密码\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (196, 'type', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"password\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (197, 'prefix-icon', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"el-icon-lock\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (198, 'size', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"medium\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (199, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入备注信息\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (200, 'type', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"textarea\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (201, 'rows', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '2', 'java.lang.Integer');
INSERT INTO `property_value`
VALUES (202, 'clearable', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (203, 'size', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"medium\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (204, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入用户名\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (205, 'prefix-icon', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"el-icon-user\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (206, 'clearable', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (207, 'size', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"medium\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (208, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入昵称\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (209, 'prefix-icon', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"el-icon-star-off\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (210, 'clearable', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (211, 'size', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"medium\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (212, 'showPassword', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (213, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入密码\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (214, 'type', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"password\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (215, 'prefix-icon', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"el-icon-lock\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (216, 'size', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"medium\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (217, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入备注信息\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (218, 'type', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"textarea\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (219, 'rows', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '2', 'java.lang.Integer');
INSERT INTO `property_value`
VALUES (220, 'clearable', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (221, 'size', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"medium\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (222, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入用户名\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (223, 'prefix-icon', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"el-icon-user\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (224, 'clearable', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (225, 'size', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"medium\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (226, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入昵称\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (227, 'prefix-icon', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"el-icon-star-off\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (228, 'clearable', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (229, 'size', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"medium\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (230, 'showPassword', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (231, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入密码\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (232, 'type', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"password\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (233, 'prefix-icon', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"el-icon-lock\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (234, 'size', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"medium\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (235, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入备注信息\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (236, 'type', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"textarea\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (237, 'rows', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '2', 'java.lang.Integer');
INSERT INTO `property_value`
VALUES (238, 'clearable', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (239, 'size', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"medium\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (240, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入用户名\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (241, 'prefix-icon', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"el-icon-user\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (242, 'clearable', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (243, 'size', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"medium\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (244, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入昵称\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (245, 'prefix-icon', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"el-icon-star-off\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (246, 'clearable', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (247, 'size', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"medium\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (248, 'showPassword', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (249, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入密码\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (250, 'type', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"password\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (251, 'prefix-icon', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"el-icon-lock\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (252, 'size', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"medium\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (253, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入备注信息\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (254, 'type', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"textarea\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (255, 'rows', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '2', 'java.lang.Integer');
INSERT INTO `property_value`
VALUES (256, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入用户ID\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (257, 'type', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"number\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (258, 'prefix-icon', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"el-icon-search\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (259, 'clearable', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (260, 'maxlength', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '64',
        'java.lang.Integer');
INSERT INTO `property_value`
VALUES (261, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入用户名\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (262, 'show-word-limit', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (263, 'type', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"text\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (264, 'prefix-icon', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"el-icon-user\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (265, 'clearable', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (266, 'maxlength', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '64',
        'java.lang.Integer');
INSERT INTO `property_value`
VALUES (267, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请输入昵称\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (268, 'show-word-limit', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (269, 'type', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"text\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (270, 'prefix-icon', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"el-icon-star-off\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (271, 'options', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer',
        '[{\"label\":\"激活的\",\"value\":\"1\"},{\"label\":\"被锁定的\",\"value\":\"2\"},{\"label\":\"禁用的\",\"value\":\"0\"}]',
        'java.util.ArrayList');
INSERT INTO `property_value`
VALUES (272, 'multiple', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (273, 'placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"请选择用户状态\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (274, 'clearable', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (275, 'size', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"small\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (276, 'start-placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"开始日期\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (277, 'value-format', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer',
        '\"yyyy-MM-dd HH:mm:ss\"', 'java.lang.String');
INSERT INTO `property_value`
VALUES (278, 'format', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"yyyy-MM-dd\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (279, 'type', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"date\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (280, 'end-placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"结束日期\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (281, 'clearable', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', 'true',
        'java.lang.Boolean');
INSERT INTO `property_value`
VALUES (282, 'size', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"small\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (283, 'start-placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"开始日期\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (284, 'value-format', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer',
        '\"yyyy-MM-dd HH:mm:ss\"', 'java.lang.String');
INSERT INTO `property_value`
VALUES (285, 'format', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"yyyy-MM-dd\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (286, 'type', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"date\"',
        'java.lang.String');
INSERT INTO `property_value`
VALUES (287, 'end-placeholder', 'com.tbs.shared.interfaces.view.impl.JsonPropertyValueTransformer', '\"结束日期\"',
        'java.lang.String');

-- ----------------------------
-- Table structure for property_relation
-- ----------------------------
DROP TABLE IF EXISTS `property_relation`;
CREATE TABLE `property_relation`
(
    `id`            bigint(20) NOT NULL AUTO_INCREMENT,
    `property_id`   bigint(20) NULL DEFAULT NULL,
    `relation_id`   bigint(20) NULL DEFAULT NULL,
    `relation_type` int(11)    NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `property_relation_property_id_index` (`property_id`) USING BTREE,
    INDEX `property_relation_relation_id_index` (`relation_id`, `relation_type`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 288
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of property_relation
-- ----------------------------
INSERT INTO `property_relation`
VALUES (1, 1, 1, 2);
INSERT INTO `property_relation`
VALUES (2, 2, 1, 2);
INSERT INTO `property_relation`
VALUES (3, 3, 1, 2);
INSERT INTO `property_relation`
VALUES (4, 4, 2, 2);
INSERT INTO `property_relation`
VALUES (5, 5, 2, 2);
INSERT INTO `property_relation`
VALUES (6, 6, 3, 2);
INSERT INTO `property_relation`
VALUES (7, 7, 3, 2);
INSERT INTO `property_relation`
VALUES (8, 8, 4, 2);
INSERT INTO `property_relation`
VALUES (9, 9, 4, 2);
INSERT INTO `property_relation`
VALUES (10, 10, 5, 2);
INSERT INTO `property_relation`
VALUES (11, 11, 5, 2);
INSERT INTO `property_relation`
VALUES (12, 12, 5, 2);
INSERT INTO `property_relation`
VALUES (13, 13, 5, 2);
INSERT INTO `property_relation`
VALUES (14, 14, 6, 2);
INSERT INTO `property_relation`
VALUES (15, 15, 6, 2);
INSERT INTO `property_relation`
VALUES (16, 16, 6, 2);
INSERT INTO `property_relation`
VALUES (17, 17, 6, 2);
INSERT INTO `property_relation`
VALUES (18, 18, 7, 2);
INSERT INTO `property_relation`
VALUES (19, 19, 7, 2);
INSERT INTO `property_relation`
VALUES (20, 20, 7, 2);
INSERT INTO `property_relation`
VALUES (21, 21, 7, 2);
INSERT INTO `property_relation`
VALUES (22, 22, 8, 2);
INSERT INTO `property_relation`
VALUES (23, 23, 8, 2);
INSERT INTO `property_relation`
VALUES (24, 24, 8, 2);
INSERT INTO `property_relation`
VALUES (25, 25, 8, 2);
INSERT INTO `property_relation`
VALUES (26, 26, 9, 2);
INSERT INTO `property_relation`
VALUES (27, 27, 9, 2);
INSERT INTO `property_relation`
VALUES (28, 28, 9, 2);
INSERT INTO `property_relation`
VALUES (29, 29, 9, 2);
INSERT INTO `property_relation`
VALUES (30, 30, 9, 2);
INSERT INTO `property_relation`
VALUES (31, 31, 9, 2);
INSERT INTO `property_relation`
VALUES (32, 32, 10, 2);
INSERT INTO `property_relation`
VALUES (33, 33, 10, 2);
INSERT INTO `property_relation`
VALUES (34, 34, 10, 2);
INSERT INTO `property_relation`
VALUES (35, 35, 10, 2);
INSERT INTO `property_relation`
VALUES (36, 36, 12, 2);
INSERT INTO `property_relation`
VALUES (37, 37, 12, 2);
INSERT INTO `property_relation`
VALUES (38, 38, 12, 2);
INSERT INTO `property_relation`
VALUES (39, 39, 12, 2);
INSERT INTO `property_relation`
VALUES (40, 40, 13, 2);
INSERT INTO `property_relation`
VALUES (41, 41, 13, 2);
INSERT INTO `property_relation`
VALUES (42, 42, 13, 2);
INSERT INTO `property_relation`
VALUES (43, 43, 13, 2);
INSERT INTO `property_relation`
VALUES (44, 44, 14, 2);
INSERT INTO `property_relation`
VALUES (45, 45, 14, 2);
INSERT INTO `property_relation`
VALUES (46, 46, 14, 2);
INSERT INTO `property_relation`
VALUES (47, 47, 14, 2);
INSERT INTO `property_relation`
VALUES (48, 48, 16, 2);
INSERT INTO `property_relation`
VALUES (49, 49, 16, 2);
INSERT INTO `property_relation`
VALUES (50, 50, 17, 2);
INSERT INTO `property_relation`
VALUES (51, 51, 17, 2);
INSERT INTO `property_relation`
VALUES (52, 52, 18, 2);
INSERT INTO `property_relation`
VALUES (53, 53, 18, 2);
INSERT INTO `property_relation`
VALUES (54, 54, 19, 2);
INSERT INTO `property_relation`
VALUES (55, 55, 19, 2);
INSERT INTO `property_relation`
VALUES (56, 56, 19, 2);
INSERT INTO `property_relation`
VALUES (57, 57, 20, 2);
INSERT INTO `property_relation`
VALUES (58, 58, 20, 2);
INSERT INTO `property_relation`
VALUES (59, 59, 20, 2);
INSERT INTO `property_relation`
VALUES (60, 60, 20, 2);
INSERT INTO `property_relation`
VALUES (61, 61, 21, 2);
INSERT INTO `property_relation`
VALUES (62, 62, 21, 2);
INSERT INTO `property_relation`
VALUES (63, 63, 21, 2);
INSERT INTO `property_relation`
VALUES (64, 64, 21, 2);
INSERT INTO `property_relation`
VALUES (65, 65, 22, 2);
INSERT INTO `property_relation`
VALUES (66, 66, 22, 2);
INSERT INTO `property_relation`
VALUES (67, 67, 22, 2);
INSERT INTO `property_relation`
VALUES (68, 68, 23, 2);
INSERT INTO `property_relation`
VALUES (69, 69, 23, 2);
INSERT INTO `property_relation`
VALUES (70, 70, 23, 2);
INSERT INTO `property_relation`
VALUES (71, 71, 24, 2);
INSERT INTO `property_relation`
VALUES (72, 72, 24, 2);
INSERT INTO `property_relation`
VALUES (73, 73, 24, 2);
INSERT INTO `property_relation`
VALUES (74, 74, 24, 2);
INSERT INTO `property_relation`
VALUES (75, 75, 25, 2);
INSERT INTO `property_relation`
VALUES (76, 76, 25, 2);
INSERT INTO `property_relation`
VALUES (77, 77, 25, 2);
INSERT INTO `property_relation`
VALUES (78, 78, 26, 2);
INSERT INTO `property_relation`
VALUES (79, 79, 26, 2);
INSERT INTO `property_relation`
VALUES (80, 80, 26, 2);
INSERT INTO `property_relation`
VALUES (81, 81, 27, 2);
INSERT INTO `property_relation`
VALUES (82, 82, 27, 2);
INSERT INTO `property_relation`
VALUES (83, 83, 28, 2);
INSERT INTO `property_relation`
VALUES (84, 84, 28, 2);
INSERT INTO `property_relation`
VALUES (85, 85, 29, 2);
INSERT INTO `property_relation`
VALUES (86, 86, 29, 2);
INSERT INTO `property_relation`
VALUES (87, 87, 29, 2);
INSERT INTO `property_relation`
VALUES (88, 88, 30, 2);
INSERT INTO `property_relation`
VALUES (89, 89, 30, 2);
INSERT INTO `property_relation`
VALUES (90, 90, 30, 2);
INSERT INTO `property_relation`
VALUES (91, 91, 30, 2);
INSERT INTO `property_relation`
VALUES (92, 92, 30, 2);
INSERT INTO `property_relation`
VALUES (93, 93, 31, 2);
INSERT INTO `property_relation`
VALUES (94, 94, 31, 2);
INSERT INTO `property_relation`
VALUES (95, 95, 31, 2);
INSERT INTO `property_relation`
VALUES (96, 96, 31, 2);
INSERT INTO `property_relation`
VALUES (97, 97, 31, 2);
INSERT INTO `property_relation`
VALUES (98, 98, 32, 2);
INSERT INTO `property_relation`
VALUES (99, 99, 32, 2);
INSERT INTO `property_relation`
VALUES (100, 100, 32, 2);
INSERT INTO `property_relation`
VALUES (101, 101, 33, 2);
INSERT INTO `property_relation`
VALUES (102, 102, 33, 2);
INSERT INTO `property_relation`
VALUES (103, 103, 33, 2);
INSERT INTO `property_relation`
VALUES (104, 104, 33, 2);
INSERT INTO `property_relation`
VALUES (105, 105, 33, 2);
INSERT INTO `property_relation`
VALUES (106, 106, 33, 2);
INSERT INTO `property_relation`
VALUES (107, 107, 34, 2);
INSERT INTO `property_relation`
VALUES (108, 108, 34, 2);
INSERT INTO `property_relation`
VALUES (109, 109, 34, 2);
INSERT INTO `property_relation`
VALUES (110, 110, 34, 2);
INSERT INTO `property_relation`
VALUES (111, 111, 34, 2);
INSERT INTO `property_relation`
VALUES (112, 112, 34, 2);
INSERT INTO `property_relation`
VALUES (113, 113, 35, 2);
INSERT INTO `property_relation`
VALUES (114, 114, 35, 2);
INSERT INTO `property_relation`
VALUES (115, 115, 35, 2);
INSERT INTO `property_relation`
VALUES (116, 116, 36, 2);
INSERT INTO `property_relation`
VALUES (117, 117, 36, 2);
INSERT INTO `property_relation`
VALUES (118, 118, 36, 2);
INSERT INTO `property_relation`
VALUES (119, 119, 36, 2);
INSERT INTO `property_relation`
VALUES (120, 120, 36, 2);
INSERT INTO `property_relation`
VALUES (121, 121, 36, 2);
INSERT INTO `property_relation`
VALUES (122, 122, 36, 2);
INSERT INTO `property_relation`
VALUES (123, 123, 37, 2);
INSERT INTO `property_relation`
VALUES (124, 124, 37, 2);
INSERT INTO `property_relation`
VALUES (125, 125, 37, 2);
INSERT INTO `property_relation`
VALUES (126, 126, 37, 2);
INSERT INTO `property_relation`
VALUES (127, 127, 37, 2);
INSERT INTO `property_relation`
VALUES (128, 128, 37, 2);
INSERT INTO `property_relation`
VALUES (129, 129, 37, 2);
INSERT INTO `property_relation`
VALUES (130, 130, 38, 2);
INSERT INTO `property_relation`
VALUES (131, 131, 38, 2);
INSERT INTO `property_relation`
VALUES (132, 132, 38, 2);
INSERT INTO `property_relation`
VALUES (133, 133, 38, 2);
INSERT INTO `property_relation`
VALUES (134, 134, 39, 2);
INSERT INTO `property_relation`
VALUES (135, 135, 39, 2);
INSERT INTO `property_relation`
VALUES (136, 136, 39, 2);
INSERT INTO `property_relation`
VALUES (137, 137, 39, 2);
INSERT INTO `property_relation`
VALUES (138, 138, 40, 2);
INSERT INTO `property_relation`
VALUES (139, 139, 40, 2);
INSERT INTO `property_relation`
VALUES (140, 140, 40, 2);
INSERT INTO `property_relation`
VALUES (141, 141, 40, 2);
INSERT INTO `property_relation`
VALUES (142, 142, 40, 2);
INSERT INTO `property_relation`
VALUES (143, 143, 40, 2);
INSERT INTO `property_relation`
VALUES (144, 144, 41, 2);
INSERT INTO `property_relation`
VALUES (145, 145, 41, 2);
INSERT INTO `property_relation`
VALUES (146, 146, 41, 2);
INSERT INTO `property_relation`
VALUES (147, 147, 41, 2);
INSERT INTO `property_relation`
VALUES (148, 148, 43, 2);
INSERT INTO `property_relation`
VALUES (149, 149, 43, 2);
INSERT INTO `property_relation`
VALUES (150, 150, 43, 2);
INSERT INTO `property_relation`
VALUES (151, 151, 43, 2);
INSERT INTO `property_relation`
VALUES (152, 152, 44, 2);
INSERT INTO `property_relation`
VALUES (153, 153, 44, 2);
INSERT INTO `property_relation`
VALUES (154, 154, 44, 2);
INSERT INTO `property_relation`
VALUES (155, 155, 44, 2);
INSERT INTO `property_relation`
VALUES (156, 156, 45, 2);
INSERT INTO `property_relation`
VALUES (157, 157, 45, 2);
INSERT INTO `property_relation`
VALUES (158, 158, 45, 2);
INSERT INTO `property_relation`
VALUES (159, 159, 45, 2);
INSERT INTO `property_relation`
VALUES (160, 160, 45, 2);
INSERT INTO `property_relation`
VALUES (161, 161, 45, 2);
INSERT INTO `property_relation`
VALUES (162, 162, 46, 2);
INSERT INTO `property_relation`
VALUES (163, 163, 46, 2);
INSERT INTO `property_relation`
VALUES (164, 164, 46, 2);
INSERT INTO `property_relation`
VALUES (165, 165, 46, 2);
INSERT INTO `property_relation`
VALUES (166, 166, 48, 2);
INSERT INTO `property_relation`
VALUES (167, 167, 48, 2);
INSERT INTO `property_relation`
VALUES (168, 168, 48, 2);
INSERT INTO `property_relation`
VALUES (169, 169, 48, 2);
INSERT INTO `property_relation`
VALUES (170, 170, 49, 2);
INSERT INTO `property_relation`
VALUES (171, 171, 49, 2);
INSERT INTO `property_relation`
VALUES (172, 172, 49, 2);
INSERT INTO `property_relation`
VALUES (173, 173, 49, 2);
INSERT INTO `property_relation`
VALUES (174, 174, 50, 2);
INSERT INTO `property_relation`
VALUES (175, 175, 50, 2);
INSERT INTO `property_relation`
VALUES (176, 176, 50, 2);
INSERT INTO `property_relation`
VALUES (177, 177, 50, 2);
INSERT INTO `property_relation`
VALUES (178, 178, 50, 2);
INSERT INTO `property_relation`
VALUES (179, 179, 50, 2);
INSERT INTO `property_relation`
VALUES (180, 180, 51, 2);
INSERT INTO `property_relation`
VALUES (181, 181, 51, 2);
INSERT INTO `property_relation`
VALUES (182, 182, 51, 2);
INSERT INTO `property_relation`
VALUES (183, 183, 51, 2);
INSERT INTO `property_relation`
VALUES (184, 184, 53, 2);
INSERT INTO `property_relation`
VALUES (185, 185, 53, 2);
INSERT INTO `property_relation`
VALUES (186, 186, 53, 2);
INSERT INTO `property_relation`
VALUES (187, 187, 53, 2);
INSERT INTO `property_relation`
VALUES (188, 188, 54, 2);
INSERT INTO `property_relation`
VALUES (189, 189, 54, 2);
INSERT INTO `property_relation`
VALUES (190, 190, 54, 2);
INSERT INTO `property_relation`
VALUES (191, 191, 54, 2);
INSERT INTO `property_relation`
VALUES (192, 192, 55, 2);
INSERT INTO `property_relation`
VALUES (193, 193, 55, 2);
INSERT INTO `property_relation`
VALUES (194, 194, 55, 2);
INSERT INTO `property_relation`
VALUES (195, 195, 55, 2);
INSERT INTO `property_relation`
VALUES (196, 196, 55, 2);
INSERT INTO `property_relation`
VALUES (197, 197, 55, 2);
INSERT INTO `property_relation`
VALUES (198, 198, 56, 2);
INSERT INTO `property_relation`
VALUES (199, 199, 56, 2);
INSERT INTO `property_relation`
VALUES (200, 200, 56, 2);
INSERT INTO `property_relation`
VALUES (201, 201, 56, 2);
INSERT INTO `property_relation`
VALUES (202, 202, 58, 2);
INSERT INTO `property_relation`
VALUES (203, 203, 58, 2);
INSERT INTO `property_relation`
VALUES (204, 204, 58, 2);
INSERT INTO `property_relation`
VALUES (205, 205, 58, 2);
INSERT INTO `property_relation`
VALUES (206, 206, 59, 2);
INSERT INTO `property_relation`
VALUES (207, 207, 59, 2);
INSERT INTO `property_relation`
VALUES (208, 208, 59, 2);
INSERT INTO `property_relation`
VALUES (209, 209, 59, 2);
INSERT INTO `property_relation`
VALUES (210, 210, 60, 2);
INSERT INTO `property_relation`
VALUES (211, 211, 60, 2);
INSERT INTO `property_relation`
VALUES (212, 212, 60, 2);
INSERT INTO `property_relation`
VALUES (213, 213, 60, 2);
INSERT INTO `property_relation`
VALUES (214, 214, 60, 2);
INSERT INTO `property_relation`
VALUES (215, 215, 60, 2);
INSERT INTO `property_relation`
VALUES (216, 216, 61, 2);
INSERT INTO `property_relation`
VALUES (217, 217, 61, 2);
INSERT INTO `property_relation`
VALUES (218, 218, 61, 2);
INSERT INTO `property_relation`
VALUES (219, 219, 61, 2);
INSERT INTO `property_relation`
VALUES (220, 220, 63, 2);
INSERT INTO `property_relation`
VALUES (221, 221, 63, 2);
INSERT INTO `property_relation`
VALUES (222, 222, 63, 2);
INSERT INTO `property_relation`
VALUES (223, 223, 63, 2);
INSERT INTO `property_relation`
VALUES (224, 224, 64, 2);
INSERT INTO `property_relation`
VALUES (225, 225, 64, 2);
INSERT INTO `property_relation`
VALUES (226, 226, 64, 2);
INSERT INTO `property_relation`
VALUES (227, 227, 64, 2);
INSERT INTO `property_relation`
VALUES (228, 228, 65, 2);
INSERT INTO `property_relation`
VALUES (229, 229, 65, 2);
INSERT INTO `property_relation`
VALUES (230, 230, 65, 2);
INSERT INTO `property_relation`
VALUES (231, 231, 65, 2);
INSERT INTO `property_relation`
VALUES (232, 232, 65, 2);
INSERT INTO `property_relation`
VALUES (233, 233, 65, 2);
INSERT INTO `property_relation`
VALUES (234, 234, 66, 2);
INSERT INTO `property_relation`
VALUES (235, 235, 66, 2);
INSERT INTO `property_relation`
VALUES (236, 236, 66, 2);
INSERT INTO `property_relation`
VALUES (237, 237, 66, 2);
INSERT INTO `property_relation`
VALUES (238, 238, 68, 2);
INSERT INTO `property_relation`
VALUES (239, 239, 68, 2);
INSERT INTO `property_relation`
VALUES (240, 240, 68, 2);
INSERT INTO `property_relation`
VALUES (241, 241, 68, 2);
INSERT INTO `property_relation`
VALUES (242, 242, 69, 2);
INSERT INTO `property_relation`
VALUES (243, 243, 69, 2);
INSERT INTO `property_relation`
VALUES (244, 244, 69, 2);
INSERT INTO `property_relation`
VALUES (245, 245, 69, 2);
INSERT INTO `property_relation`
VALUES (246, 246, 70, 2);
INSERT INTO `property_relation`
VALUES (247, 247, 70, 2);
INSERT INTO `property_relation`
VALUES (248, 248, 70, 2);
INSERT INTO `property_relation`
VALUES (249, 249, 70, 2);
INSERT INTO `property_relation`
VALUES (250, 250, 70, 2);
INSERT INTO `property_relation`
VALUES (251, 251, 70, 2);
INSERT INTO `property_relation`
VALUES (252, 252, 71, 2);
INSERT INTO `property_relation`
VALUES (253, 253, 71, 2);
INSERT INTO `property_relation`
VALUES (254, 254, 71, 2);
INSERT INTO `property_relation`
VALUES (255, 255, 71, 2);
INSERT INTO `property_relation`
VALUES (256, 256, 73, 2);
INSERT INTO `property_relation`
VALUES (257, 257, 73, 2);
INSERT INTO `property_relation`
VALUES (258, 258, 73, 2);
INSERT INTO `property_relation`
VALUES (259, 259, 74, 2);
INSERT INTO `property_relation`
VALUES (260, 260, 74, 2);
INSERT INTO `property_relation`
VALUES (261, 261, 74, 2);
INSERT INTO `property_relation`
VALUES (262, 262, 74, 2);
INSERT INTO `property_relation`
VALUES (263, 263, 74, 2);
INSERT INTO `property_relation`
VALUES (264, 264, 74, 2);
INSERT INTO `property_relation`
VALUES (265, 265, 75, 2);
INSERT INTO `property_relation`
VALUES (266, 266, 75, 2);
INSERT INTO `property_relation`
VALUES (267, 267, 75, 2);
INSERT INTO `property_relation`
VALUES (268, 268, 75, 2);
INSERT INTO `property_relation`
VALUES (269, 269, 75, 2);
INSERT INTO `property_relation`
VALUES (270, 270, 75, 2);
INSERT INTO `property_relation`
VALUES (271, 271, 76, 2);
INSERT INTO `property_relation`
VALUES (272, 272, 76, 2);
INSERT INTO `property_relation`
VALUES (273, 273, 76, 2);
INSERT INTO `property_relation`
VALUES (274, 274, 77, 2);
INSERT INTO `property_relation`
VALUES (275, 275, 77, 2);
INSERT INTO `property_relation`
VALUES (276, 276, 77, 2);
INSERT INTO `property_relation`
VALUES (277, 277, 77, 2);
INSERT INTO `property_relation`
VALUES (278, 278, 77, 2);
INSERT INTO `property_relation`
VALUES (279, 279, 77, 2);
INSERT INTO `property_relation`
VALUES (280, 280, 77, 2);
INSERT INTO `property_relation`
VALUES (281, 281, 78, 2);
INSERT INTO `property_relation`
VALUES (282, 282, 78, 2);
INSERT INTO `property_relation`
VALUES (283, 283, 78, 2);
INSERT INTO `property_relation`
VALUES (284, 284, 78, 2);
INSERT INTO `property_relation`
VALUES (285, 285, 78, 2);
INSERT INTO `property_relation`
VALUES (286, 286, 78, 2);
INSERT INTO `property_relation`
VALUES (287, 287, 78, 2);

-- ----------------------------
-- Table structure for form_field_relation
-- ----------------------------
DROP TABLE IF EXISTS `form_field_relation`;
CREATE TABLE `form_field_relation`
(
    `id`       bigint(20) NOT NULL AUTO_INCREMENT,
    `field_id` bigint(20) NULL DEFAULT NULL,
    `form_id`  bigint(20) NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE,
    INDEX `form_field_relation_form_id_index` (`form_id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 79
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of form_field_relation
-- ----------------------------
INSERT INTO `form_field_relation`
VALUES (12, 12, 3);
INSERT INTO `form_field_relation`
VALUES (13, 13, 3);
INSERT INTO `form_field_relation`
VALUES (14, 14, 3);
INSERT INTO `form_field_relation`
VALUES (15, 15, 3);
INSERT INTO `form_field_relation`
VALUES (16, 16, 4);
INSERT INTO `form_field_relation`
VALUES (17, 17, 4);
INSERT INTO `form_field_relation`
VALUES (18, 18, 4);
INSERT INTO `form_field_relation`
VALUES (19, 19, 4);
INSERT INTO `form_field_relation`
VALUES (20, 20, 4);
INSERT INTO `form_field_relation`
VALUES (21, 21, 4);
INSERT INTO `form_field_relation`
VALUES (22, 22, 5);
INSERT INTO `form_field_relation`
VALUES (23, 23, 5);
INSERT INTO `form_field_relation`
VALUES (24, 24, 5);
INSERT INTO `form_field_relation`
VALUES (25, 25, 5);
INSERT INTO `form_field_relation`
VALUES (68, 68, 2);
INSERT INTO `form_field_relation`
VALUES (69, 69, 2);
INSERT INTO `form_field_relation`
VALUES (70, 70, 2);
INSERT INTO `form_field_relation`
VALUES (71, 71, 2);
INSERT INTO `form_field_relation`
VALUES (72, 72, 2);
INSERT INTO `form_field_relation`
VALUES (73, 73, 1);
INSERT INTO `form_field_relation`
VALUES (74, 74, 1);
INSERT INTO `form_field_relation`
VALUES (75, 75, 1);
INSERT INTO `form_field_relation`
VALUES (76, 76, 1);
INSERT INTO `form_field_relation`
VALUES (77, 77, 1);
INSERT INTO `form_field_relation`
VALUES (78, 78, 1);

-- ----------------------------
-- Table structure for form_field_configration
-- ----------------------------
DROP TABLE IF EXISTS `form_field_configration`;
CREATE TABLE `form_field_configration`
(
    `id`    bigint(20)                                            NOT NULL AUTO_INCREMENT,
    `label` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `prop`  varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `type`  varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 79
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of form_field_configration
-- ----------------------------
INSERT INTO `form_field_configration`
VALUES (1, '用户ID', 'idEquals', 'input');
INSERT INTO `form_field_configration`
VALUES (2, '用户名', 'userNameEquals', 'input');
INSERT INTO `form_field_configration`
VALUES (3, '昵称', 'nickNameLike', 'input');
INSERT INTO `form_field_configration`
VALUES (4, '用户状态', 'flagEquals', 'select');
INSERT INTO `form_field_configration`
VALUES (5, '创建时间', 'createTimeRange', 'date-range');
INSERT INTO `form_field_configration`
VALUES (6, '更新时间', 'updateTimeRange', 'date-range');
INSERT INTO `form_field_configration`
VALUES (7, '用户名', 'userName', 'input');
INSERT INTO `form_field_configration`
VALUES (8, '昵称', 'nickName', 'input');
INSERT INTO `form_field_configration`
VALUES (9, '密码', 'password', 'input');
INSERT INTO `form_field_configration`
VALUES (10, '备注', 'remark', 'input');
INSERT INTO `form_field_configration`
VALUES (11, '头像', 'avatar', 'url-image');
INSERT INTO `form_field_configration`
VALUES (12, '用户名', 'userName', 'input');
INSERT INTO `form_field_configration`
VALUES (13, '昵称', 'nickName', 'input');
INSERT INTO `form_field_configration`
VALUES (14, '备注', 'remark', 'input');
INSERT INTO `form_field_configration`
VALUES (15, '头像', 'avatar', 'url-image');
INSERT INTO `form_field_configration`
VALUES (16, '公司Id', 'equalsId', 'input');
INSERT INTO `form_field_configration`
VALUES (17, '公司名称', 'likeName', 'input');
INSERT INTO `form_field_configration`
VALUES (18, '税号', 'equalsTaxNumber', 'input');
INSERT INTO `form_field_configration`
VALUES (19, '公司状态', 'flagEquals', 'select');
INSERT INTO `form_field_configration`
VALUES (20, '创建时间', 'createTimeRange', 'date-range');
INSERT INTO `form_field_configration`
VALUES (21, '更新时间', 'updateTimeRange', 'date-range');
INSERT INTO `form_field_configration`
VALUES (22, '公司名称', 'companyName', 'input');
INSERT INTO `form_field_configration`
VALUES (23, '税号', 'taxNumber', 'input');
INSERT INTO `form_field_configration`
VALUES (24, '描述', 'description', 'input');
INSERT INTO `form_field_configration`
VALUES (25, '公司Logo', 'logoImage', 'url-image');
INSERT INTO `form_field_configration`
VALUES (26, '用户ID', 'idEquals', 'input');
INSERT INTO `form_field_configration`
VALUES (27, '用户名', 'userNameEquals', 'input');
INSERT INTO `form_field_configration`
VALUES (28, '昵称', 'nickNameLike', 'input');
INSERT INTO `form_field_configration`
VALUES (29, '用户状态', 'flagEquals', 'select');
INSERT INTO `form_field_configration`
VALUES (30, '创建时间', 'createTimeRange', 'date-range');
INSERT INTO `form_field_configration`
VALUES (31, '更新时间', 'updateTimeRange', 'date-range');
INSERT INTO `form_field_configration`
VALUES (32, '用户ID', 'idEquals', 'input');
INSERT INTO `form_field_configration`
VALUES (33, '用户名', 'userNameEquals', 'input');
INSERT INTO `form_field_configration`
VALUES (34, '昵称', 'nickNameLike', 'input');
INSERT INTO `form_field_configration`
VALUES (35, '用户状态', 'flagEquals', 'select');
INSERT INTO `form_field_configration`
VALUES (36, '创建时间', 'createTimeRange', 'date-range');
INSERT INTO `form_field_configration`
VALUES (37, '更新时间', 'updateTimeRange', 'date-range');
INSERT INTO `form_field_configration`
VALUES (38, '用户名', 'userName', 'input');
INSERT INTO `form_field_configration`
VALUES (39, '昵称', 'nickName', 'input');
INSERT INTO `form_field_configration`
VALUES (40, '密码', 'password', 'input');
INSERT INTO `form_field_configration`
VALUES (41, '备注', 'remark', 'input');
INSERT INTO `form_field_configration`
VALUES (42, '头像', 'avatar', 'url-image');
INSERT INTO `form_field_configration`
VALUES (43, '用户名', 'userName', 'input');
INSERT INTO `form_field_configration`
VALUES (44, '昵称', 'nickName', 'input');
INSERT INTO `form_field_configration`
VALUES (45, '密码', 'password', 'input');
INSERT INTO `form_field_configration`
VALUES (46, '备注', 'remark', 'input');
INSERT INTO `form_field_configration`
VALUES (47, '头像', 'avatar', 'url-image');
INSERT INTO `form_field_configration`
VALUES (48, '用户名', 'userName', 'input');
INSERT INTO `form_field_configration`
VALUES (49, '昵称', 'nickName', 'input');
INSERT INTO `form_field_configration`
VALUES (50, '密码', 'password', 'input');
INSERT INTO `form_field_configration`
VALUES (51, '备注', 'remark', 'input');
INSERT INTO `form_field_configration`
VALUES (52, '头像', 'avatar', 'url-image');
INSERT INTO `form_field_configration`
VALUES (53, '用户名', 'userName', 'input');
INSERT INTO `form_field_configration`
VALUES (54, '昵称', 'nickName', 'input');
INSERT INTO `form_field_configration`
VALUES (55, '密码', 'password', 'input');
INSERT INTO `form_field_configration`
VALUES (56, '备注', 'remark', 'input');
INSERT INTO `form_field_configration`
VALUES (57, '头像', 'avatar', 'url-image');
INSERT INTO `form_field_configration`
VALUES (58, '用户名', 'userName', 'input');
INSERT INTO `form_field_configration`
VALUES (59, '昵称', 'nickName', 'input');
INSERT INTO `form_field_configration`
VALUES (60, '密码', 'password', 'input');
INSERT INTO `form_field_configration`
VALUES (61, '备注', 'remark', 'input');
INSERT INTO `form_field_configration`
VALUES (62, '头像', 'avatar', 'url-image');
INSERT INTO `form_field_configration`
VALUES (63, '用户名', 'userName', 'input');
INSERT INTO `form_field_configration`
VALUES (64, '昵称', 'nickName', 'input');
INSERT INTO `form_field_configration`
VALUES (65, '密码', 'password', 'input');
INSERT INTO `form_field_configration`
VALUES (66, '备注', 'remark', 'input');
INSERT INTO `form_field_configration`
VALUES (67, '头像', 'avatar', 'url-image');
INSERT INTO `form_field_configration`
VALUES (68, '用户名', 'userName', 'input');
INSERT INTO `form_field_configration`
VALUES (69, '昵称', 'nickName', 'input');
INSERT INTO `form_field_configration`
VALUES (70, '密码', 'password', 'input');
INSERT INTO `form_field_configration`
VALUES (71, '备注', 'remark', 'input');
INSERT INTO `form_field_configration`
VALUES (72, '头像', 'avatar', 'url-image');
INSERT INTO `form_field_configration`
VALUES (73, '用户ID', 'idEquals', 'input');
INSERT INTO `form_field_configration`
VALUES (74, '用户名', 'userNameEquals', 'input');
INSERT INTO `form_field_configration`
VALUES (75, '昵称', 'nickNameLike', 'input');
INSERT INTO `form_field_configration`
VALUES (76, '用户状态', 'flagEquals', 'select');
INSERT INTO `form_field_configration`
VALUES (77, '创建时间', 'createTimeRange', 'date-range');
INSERT INTO `form_field_configration`
VALUES (78, '更新时间', 'updateTimeRange', 'date-range');

-- ----------------------------
-- Table structure for dynamic_form_configration
-- ----------------------------
DROP TABLE IF EXISTS `dynamic_form_configration`;
CREATE TABLE `dynamic_form_configration`
(
    `id`          bigint(20)                                             NOT NULL AUTO_INCREMENT,
    `expandable`  int(11)                                                NULL DEFAULT NULL,
    `expanded`    int(11)                                                NULL DEFAULT NULL,
    `gutter`      int(11)                                                NULL DEFAULT NULL,
    `label_width` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL,
    `span`        int(11)                                                NULL DEFAULT NULL,
    `title`       varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin NULL DEFAULT NULL COMMENT '表单标题',
    PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB
  AUTO_INCREMENT = 6
  CHARACTER SET = utf8mb4
  COLLATE = utf8mb4_bin
  ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of dynamic_form_configration
-- ----------------------------
INSERT INTO `dynamic_form_configration`
VALUES (1, 1, 1, 40, '80px', 8, '用户查询表单');
INSERT INTO `dynamic_form_configration`
VALUES (2, 0, 1, 40, '80px', 5, '用户新增表单');
INSERT INTO `dynamic_form_configration`
VALUES (3, 0, 1, 24, '80px', 20, '用户编辑表单');
INSERT INTO `dynamic_form_configration`
VALUES (4, 1, 1, 20, '100px', 8, '公司查询表单');
INSERT INTO `dynamic_form_configration`
VALUES (5, 0, 1, 24, '80px', 20, '公司新增/编辑表单');

SET FOREIGN_KEY_CHECKS = 1;

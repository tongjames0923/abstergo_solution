/*
 Navicat Premium Dump SQL

 Source Server         : docker_3306
 Source Server Type    : MySQL
 Source Server Version : 80413 (8.4.13)
 Source Host           : localhost:3306
 Source Schema         : xxl_job

 Target Server Type    : MySQL
 Target Server Version : 80413 (8.4.13)
 File Encoding         : 65001

 Date: 03/11/2026 16:39:48
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for xxl_job_group
-- ----------------------------
DROP TABLE IF EXISTS `xxl_job_group`;
CREATE TABLE `xxl_job_group`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `app_name` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '执行器AppName',
  `title` varchar(12) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '执行器名称',
  `address_type` tinyint NOT NULL DEFAULT 0 COMMENT '执行器地址类型：0=自动注册、1=手动录入',
  `address_list` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '执行器地址列表，多地址逗号分隔',
  `update_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of xxl_job_group
-- ----------------------------
INSERT INTO `xxl_job_group` VALUES (3, 'mainSystemApplication', '主系统任务', 0, NULL, '2026-11-03 16:39:25');

-- ----------------------------
-- Table structure for xxl_job_info
-- ----------------------------
DROP TABLE IF EXISTS `xxl_job_info`;
CREATE TABLE `xxl_job_info`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `job_group` int NOT NULL COMMENT '执行器主键ID',
  `job_desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `add_time` datetime NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  `author` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '作者',
  `alarm_email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '报警邮件',
  `schedule_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'NONE' COMMENT '调度类型',
  `schedule_conf` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '调度配置，值含义取决于调度类型',
  `misfire_strategy` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL DEFAULT 'DO_NOTHING' COMMENT '调度过期策略',
  `executor_route_strategy` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '执行器路由策略',
  `executor_handler` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '执行器任务handler',
  `executor_param` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '执行器任务参数',
  `executor_block_strategy` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '阻塞处理策略',
  `executor_timeout` int NOT NULL DEFAULT 0 COMMENT '任务执行超时时间，单位秒',
  `executor_fail_retry_count` int NOT NULL DEFAULT 0 COMMENT '失败重试次数',
  `glue_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'GLUE类型',
  `glue_source` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT 'GLUE源代码',
  `glue_remark` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'GLUE备注',
  `glue_updatetime` datetime NULL DEFAULT NULL COMMENT 'GLUE更新时间',
  `child_jobid` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '子任务ID，多个逗号分隔',
  `trigger_status` tinyint NOT NULL DEFAULT 0 COMMENT '调度状态：0-停止，1-运行',
  `trigger_last_time` bigint NOT NULL DEFAULT 0 COMMENT '上次调度时间',
  `trigger_next_time` bigint NOT NULL DEFAULT 0 COMMENT '下次调度时间',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 21 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of xxl_job_info
-- ----------------------------
INSERT INTO `xxl_job_info` VALUES (5, 3, 'json任务帮助', '2023-07-15 11:24:16', '2023-07-15 11:35:13', 'tbs', '', 'NONE', '', 'DO_NOTHING', 'FIRST', 'help_json', '', 'SERIAL_EXECUTION', 0, 0, 'BEAN', '', 'GLUE代码初始化', '2023-07-15 11:24:16', '', 0, 0, 0);
INSERT INTO `xxl_job_info` VALUES (9, 3, '通用json运行任务接口', '2024-04-21 19:02:15', '2024-04-21 19:35:41', 'tbs', '', 'NONE', '', 'DO_NOTHING', 'FIRST', 'jsonJobHandler', '{\"method\":\"tester\",\"params\":{\"stringType\":\"can be anything you need\"}}', 'SERIAL_EXECUTION', 0, 0, 'BEAN', '', 'GLUE代码初始化', '2024-04-21 19:02:15', '', 0, 0, 0);
INSERT INTO `xxl_job_info` VALUES (11, 3, '当前支持的json任务列表', '2024-04-21 19:03:59', '2024-04-21 19:03:59', 'tbs', '', 'NONE', '', 'DO_NOTHING', 'FIRST', 'available', '', 'SERIAL_EXECUTION', 0, 0, 'BEAN', '', 'GLUE代码初始化', '2024-04-21 19:03:59', '', 0, 0, 0);
INSERT INTO `xxl_job_info` VALUES (12, 3, '输出异步任务支持列表', '2024-09-17 20:24:04', '2024-09-17 20:26:51', 'tbs', '', 'NONE', '', 'DO_NOTHING', 'FIRST', 'asyncTasks', '', 'SERIAL_EXECUTION', 0, 0, 'BEAN', '', 'GLUE代码初始化', '2024-09-17 20:24:04', '', 0, 0, 0);
INSERT INTO `xxl_job_info` VALUES (13, 3, '异步任务调用', '2024-09-17 20:27:21', '2024-09-17 21:07:07', 'tbs', '', 'FIX_RATE', '3', 'DO_NOTHING', 'FIRST', 'asyncTaskInvoke', '{\r\n    \"taskId\": \"BackgroundBufferedConsumerFlusher\",\r\n}', 'SERIAL_EXECUTION', 0, 0, 'BEAN', '', 'GLUE代码初始化', '2024-09-17 20:27:21', '', 0, 0, 0);
INSERT INTO `xxl_job_info` VALUES (14, 3, '公司表树结构重构', '2026-04-18 09:08:35', '2026-07-14 08:24:22', 'tbs', '', 'NONE', '', 'DO_NOTHING', 'FIRST', 'company_tree_rebuild', '', 'SERIAL_EXECUTION', 0, 0, 'BEAN', '', 'GLUE代码初始化', '2026-04-18 09:08:35', '', 0, 0, 0);
INSERT INTO `xxl_job_info` VALUES (15, 3, '触发系统日志刷入', '2026-04-18 09:12:55', '2026-08-09 15:19:03', 'tbs', '', 'CRON', '0 0,30 * * * ?', 'DO_NOTHING', 'FAILOVER', 'flushLogData', '', 'DISCARD_LATER', 0, 0, 'BEAN', '', 'GLUE代码初始化', '2026-04-18 09:12:55', '', 1, 1793694600000, 1793696400000);
INSERT INTO `xxl_job_info` VALUES (17, 3, '部门表树结构重构', '2026-07-14 08:25:00', '2026-08-09 15:21:08', 'tbs', '', 'CRON', '0 0 0 1/14 * ?', 'DO_NOTHING', 'FAILOVER', 'department_tree_rebuild', '', 'DISCARD_LATER', 0, 0, 'BEAN', '', 'GLUE代码初始化', '2026-07-14 08:25:00', '', 0, 0, 0);
INSERT INTO `xxl_job_info` VALUES (20, 3, '后台任务', '2026-11-03 16:34:55', '2026-11-03 16:35:04', 'tbs', '', 'CRON', '0/10 * * * * ?', 'DO_NOTHING', 'FAILOVER', 'runningBackGroundJobHandler', '', 'DISCARD_LATER', 0, 0, 'BEAN', '', 'GLUE代码初始化', '2026-11-03 16:34:55', '', 1, 1793695190000, 1793695200000);

-- ----------------------------
-- Table structure for xxl_job_lock
-- ----------------------------
DROP TABLE IF EXISTS `xxl_job_lock`;
CREATE TABLE `xxl_job_lock`  (
  `lock_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '锁名称',
  PRIMARY KEY (`lock_name`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of xxl_job_lock
-- ----------------------------
INSERT INTO `xxl_job_lock` VALUES ('schedule_lock');

-- ----------------------------
-- Table structure for xxl_job_log
-- ----------------------------
DROP TABLE IF EXISTS `xxl_job_log`;
CREATE TABLE `xxl_job_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `job_group` int NOT NULL COMMENT '执行器主键ID',
  `job_id` int NOT NULL COMMENT '任务，主键ID',
  `executor_address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '执行器地址，本次执行的地址',
  `executor_handler` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '执行器任务handler',
  `executor_param` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '执行器任务参数',
  `executor_sharding_param` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '执行器任务分片参数，格式如 1/2',
  `executor_fail_retry_count` int NOT NULL DEFAULT 0 COMMENT '失败重试次数',
  `trigger_time` datetime NULL DEFAULT NULL COMMENT '调度-时间',
  `trigger_code` int NOT NULL COMMENT '调度-结果',
  `trigger_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '调度-日志',
  `handle_time` datetime NULL DEFAULT NULL COMMENT '执行-时间',
  `handle_code` int NOT NULL COMMENT '执行-状态',
  `handle_msg` text CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT '执行-日志',
  `alarm_status` tinyint NOT NULL DEFAULT 0 COMMENT '告警状态：0-默认、1-无需告警、2-告警成功、3-告警失败',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `I_trigger_time`(`trigger_time` ASC) USING BTREE,
  INDEX `I_handle_code`(`handle_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14874 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of xxl_job_log
-- ----------------------------
INSERT INTO `xxl_job_log` VALUES (14820, 3, 15, NULL, 'flushLogData', '', NULL, 0, '2026-10-31 19:30:00', 500, '任务触发类型：Cron触发<br>调度机器：172.18.0.7<br>执行器-注册方式：自动注册<br>执行器-地址列表：null<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>调度失败：执行器地址为空<br><br>', NULL, 0, NULL, 2);
INSERT INTO `xxl_job_log` VALUES (14821, 3, 15, NULL, 'flushLogData', '', NULL, 0, '2026-11-01 08:00:00', 500, '任务触发类型：Cron触发<br>调度机器：172.18.0.5<br>执行器-注册方式：自动注册<br>执行器-地址列表：null<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>调度失败：执行器地址为空<br><br>', NULL, 0, NULL, 2);
INSERT INTO `xxl_job_log` VALUES (14822, 3, 15, NULL, 'flushLogData', '', NULL, 0, '2026-11-01 08:30:00', 500, '任务触发类型：Cron触发<br>调度机器：172.18.0.5<br>执行器-注册方式：自动注册<br>执行器-地址列表：null<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>调度失败：执行器地址为空<br><br>', NULL, 0, NULL, 2);
INSERT INTO `xxl_job_log` VALUES (14823, 3, 15, NULL, 'flushLogData', '', NULL, 0, '2026-11-01 09:00:00', 500, '任务触发类型：Cron触发<br>调度机器：172.18.0.5<br>执行器-注册方式：自动注册<br>执行器-地址列表：null<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>调度失败：执行器地址为空<br><br>', NULL, 0, NULL, 2);
INSERT INTO `xxl_job_log` VALUES (14824, 3, 15, NULL, 'flushLogData', '', NULL, 0, '2026-11-01 09:30:00', 500, '任务触发类型：Cron触发<br>调度机器：172.18.0.5<br>执行器-注册方式：自动注册<br>执行器-地址列表：null<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>调度失败：执行器地址为空<br><br>', NULL, 0, NULL, 2);
INSERT INTO `xxl_job_log` VALUES (14825, 3, 15, NULL, 'flushLogData', '', NULL, 0, '2026-11-01 14:30:00', 500, '任务触发类型：Cron触发<br>调度机器：172.18.0.6<br>执行器-注册方式：自动注册<br>执行器-地址列表：null<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>调度失败：执行器地址为空<br><br>', NULL, 0, NULL, 2);
INSERT INTO `xxl_job_log` VALUES (14826, 3, 15, NULL, 'flushLogData', '', NULL, 0, '2026-11-01 15:00:00', 500, '任务触发类型：Cron触发<br>调度机器：172.18.0.6<br>执行器-注册方式：自动注册<br>执行器-地址列表：null<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>调度失败：执行器地址为空<br><br>', NULL, 0, NULL, 2);
INSERT INTO `xxl_job_log` VALUES (14827, 3, 15, NULL, 'flushLogData', '', NULL, 0, '2026-11-01 15:30:00', 500, '任务触发类型：Cron触发<br>调度机器：172.18.0.6<br>执行器-注册方式：自动注册<br>执行器-地址列表：null<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>调度失败：执行器地址为空<br><br>', NULL, 0, NULL, 2);
INSERT INTO `xxl_job_log` VALUES (14828, 3, 15, NULL, 'flushLogData', '', NULL, 0, '2026-11-03 08:30:00', 500, '任务触发类型：Cron触发<br>调度机器：172.18.0.8<br>执行器-注册方式：自动注册<br>执行器-地址列表：null<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>调度失败：执行器地址为空<br><br>', NULL, 0, NULL, 2);
INSERT INTO `xxl_job_log` VALUES (14829, 3, 15, NULL, 'flushLogData', '', NULL, 0, '2026-11-03 09:00:00', 500, '任务触发类型：Cron触发<br>调度机器：172.18.0.8<br>执行器-注册方式：自动注册<br>执行器-地址列表：null<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>调度失败：执行器地址为空<br><br>', NULL, 0, NULL, 2);
INSERT INTO `xxl_job_log` VALUES (14830, 3, 15, NULL, 'flushLogData', '', NULL, 0, '2026-11-03 09:30:00', 500, '任务触发类型：Cron触发<br>调度机器：172.18.0.8<br>执行器-注册方式：自动注册<br>执行器-地址列表：null<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>调度失败：执行器地址为空<br><br>', NULL, 0, NULL, 2);
INSERT INTO `xxl_job_log` VALUES (14831, 3, 15, NULL, 'flushLogData', '', NULL, 0, '2026-11-03 10:00:00', 500, '任务触发类型：Cron触发<br>调度机器：172.18.0.8<br>执行器-注册方式：自动注册<br>执行器-地址列表：null<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>调度失败：执行器地址为空<br><br>', NULL, 0, NULL, 2);
INSERT INTO `xxl_job_log` VALUES (14832, 3, 15, NULL, 'flushLogData', '', NULL, 0, '2026-11-03 10:30:00', 500, '任务触发类型：Cron触发<br>调度机器：172.18.0.8<br>执行器-注册方式：自动注册<br>执行器-地址列表：null<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>调度失败：执行器地址为空<br><br>', NULL, 0, NULL, 2);
INSERT INTO `xxl_job_log` VALUES (14833, 3, 15, NULL, 'flushLogData', '', NULL, 0, '2026-11-03 11:00:00', 500, '任务触发类型：Cron触发<br>调度机器：172.18.0.8<br>执行器-注册方式：自动注册<br>执行器-地址列表：null<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>调度失败：执行器地址为空<br><br>', NULL, 0, NULL, 2);
INSERT INTO `xxl_job_log` VALUES (14834, 3, 15, NULL, 'flushLogData', '', NULL, 0, '2026-11-03 11:30:00', 500, '任务触发类型：Cron触发<br>调度机器：172.18.0.8<br>执行器-注册方式：自动注册<br>执行器-地址列表：null<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>调度失败：执行器地址为空<br><br>', NULL, 0, NULL, 2);
INSERT INTO `xxl_job_log` VALUES (14835, 3, 15, NULL, 'flushLogData', '', NULL, 0, '2026-11-03 12:00:00', 500, '任务触发类型：Cron触发<br>调度机器：172.18.0.8<br>执行器-注册方式：自动注册<br>执行器-地址列表：null<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>调度失败：执行器地址为空<br><br>', NULL, 0, NULL, 2);
INSERT INTO `xxl_job_log` VALUES (14836, 3, 15, NULL, 'flushLogData', '', NULL, 0, '2026-11-03 12:30:00', 500, '任务触发类型：Cron触发<br>调度机器：172.18.0.8<br>执行器-注册方式：自动注册<br>执行器-地址列表：null<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>调度失败：执行器地址为空<br><br>', NULL, 0, NULL, 2);
INSERT INTO `xxl_job_log` VALUES (14837, 3, 15, NULL, 'flushLogData', '', NULL, 0, '2026-11-03 13:00:00', 500, '任务触发类型：Cron触发<br>调度机器：172.18.0.8<br>执行器-注册方式：自动注册<br>执行器-地址列表：null<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>调度失败：执行器地址为空<br><br>', NULL, 0, NULL, 2);
INSERT INTO `xxl_job_log` VALUES (14838, 3, 15, NULL, 'flushLogData', '', NULL, 0, '2026-11-03 13:30:00', 500, '任务触发类型：Cron触发<br>调度机器：172.18.0.8<br>执行器-注册方式：自动注册<br>执行器-地址列表：null<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>调度失败：执行器地址为空<br><br>', NULL, 0, NULL, 2);
INSERT INTO `xxl_job_log` VALUES (14839, 3, 15, NULL, 'flushLogData', '', NULL, 0, '2026-11-03 14:00:00', 500, '任务触发类型：Cron触发<br>调度机器：172.18.0.8<br>执行器-注册方式：自动注册<br>执行器-地址列表：null<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>调度失败：执行器地址为空<br><br>', NULL, 0, NULL, 2);
INSERT INTO `xxl_job_log` VALUES (14840, 3, 15, NULL, 'flushLogData', '', NULL, 0, '2026-11-03 14:30:00', 500, '任务触发类型：Cron触发<br>调度机器：172.18.0.8<br>执行器-注册方式：自动注册<br>执行器-地址列表：null<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>调度失败：执行器地址为空<br><br>', NULL, 0, NULL, 2);
INSERT INTO `xxl_job_log` VALUES (14841, 3, 15, NULL, 'flushLogData', '', NULL, 0, '2026-11-03 15:00:00', 500, '任务触发类型：Cron触发<br>调度机器：172.18.0.8<br>执行器-注册方式：自动注册<br>执行器-地址列表：null<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>调度失败：执行器地址为空<br><br>', NULL, 0, NULL, 2);
INSERT INTO `xxl_job_log` VALUES (14842, 3, 15, NULL, 'flushLogData', '', NULL, 0, '2026-11-03 15:30:00', 500, '任务触发类型：Cron触发<br>调度机器：172.18.0.8<br>执行器-注册方式：自动注册<br>执行器-地址列表：null<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>调度失败：执行器地址为空<br><br>', NULL, 0, NULL, 2);
INSERT INTO `xxl_job_log` VALUES (14843, 3, 15, NULL, 'flushLogData', '', NULL, 0, '2026-11-03 16:00:00', 500, '任务触发类型：Cron触发<br>调度机器：172.18.0.8<br>执行器-注册方式：自动注册<br>执行器-地址列表：null<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>调度失败：执行器地址为空<br><br>', NULL, 0, NULL, 2);
INSERT INTO `xxl_job_log` VALUES (14844, 3, 15, 'http://192.168.10.80:9999/', 'flushLogData', '', NULL, 0, '2026-11-03 16:30:00', 200, '任务触发类型：Cron触发<br>调度机器：172.18.0.8<br>执行器-注册方式：自动注册<br>执行器-地址列表：[http://192.168.10.80:9999/]<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>心跳检测：<br>address：http://192.168.10.80:9999/<br>code：200<br>msg：null<br><br>触发调度：<br>address：http://192.168.10.80:9999/<br>code：200<br>msg：null', '2026-11-03 16:30:00', 200, '', 0);
INSERT INTO `xxl_job_log` VALUES (14845, 3, 20, 'http://192.168.10.80:9999/', 'runningBackGroundJobHandler', '', NULL, 0, '2026-11-03 16:34:59', 200, '任务触发类型：手动触发<br>调度机器：172.18.0.8<br>执行器-注册方式：自动注册<br>执行器-地址列表：[http://192.168.10.80:9999/]<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>心跳检测：<br>address：http://192.168.10.80:9999/<br>code：200<br>msg：null<br><br>触发调度：<br>address：http://192.168.10.80:9999/<br>code：200<br>msg：null', '2026-11-03 16:34:59', 200, '', 0);
INSERT INTO `xxl_job_log` VALUES (14846, 3, 20, 'http://192.168.10.80:9999/', 'runningBackGroundJobHandler', '', NULL, 0, '2026-11-03 16:35:10', 200, '任务触发类型：Cron触发<br>调度机器：172.18.0.8<br>执行器-注册方式：自动注册<br>执行器-地址列表：[http://192.168.10.80:9999/]<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>心跳检测：<br>address：http://192.168.10.80:9999/<br>code：200<br>msg：null<br><br>触发调度：<br>address：http://192.168.10.80:9999/<br>code：200<br>msg：null', '2026-11-03 16:35:10', 200, '', 0);
INSERT INTO `xxl_job_log` VALUES (14847, 3, 20, 'http://192.168.10.80:9999/', 'runningBackGroundJobHandler', '', NULL, 0, '2026-11-03 16:35:20', 200, '任务触发类型：Cron触发<br>调度机器：172.18.0.8<br>执行器-注册方式：自动注册<br>执行器-地址列表：[http://192.168.10.80:9999/]<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>心跳检测：<br>address：http://192.168.10.80:9999/<br>code：200<br>msg：null<br><br>触发调度：<br>address：http://192.168.10.80:9999/<br>code：200<br>msg：null', '2026-11-03 16:35:20', 200, '', 0);
INSERT INTO `xxl_job_log` VALUES (14848, 3, 20, 'http://192.168.10.80:9999/', 'runningBackGroundJobHandler', '', NULL, 0, '2026-11-03 16:35:30', 200, '任务触发类型：Cron触发<br>调度机器：172.18.0.8<br>执行器-注册方式：自动注册<br>执行器-地址列表：[http://192.168.10.80:9999/]<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>心跳检测：<br>address：http://192.168.10.80:9999/<br>code：200<br>msg：null<br><br>触发调度：<br>address：http://192.168.10.80:9999/<br>code：200<br>msg：null', '2026-11-03 16:35:30', 200, '', 0);
INSERT INTO `xxl_job_log` VALUES (14849, 3, 20, 'http://192.168.10.80:9999/', 'runningBackGroundJobHandler', '', NULL, 0, '2026-11-03 16:35:40', 200, '任务触发类型：Cron触发<br>调度机器：172.18.0.8<br>执行器-注册方式：自动注册<br>执行器-地址列表：[http://192.168.10.80:9999/]<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>心跳检测：<br>address：http://192.168.10.80:9999/<br>code：200<br>msg：null<br><br>触发调度：<br>address：http://192.168.10.80:9999/<br>code：200<br>msg：null', '2026-11-03 16:35:40', 200, '', 0);
INSERT INTO `xxl_job_log` VALUES (14850, 3, 20, 'http://192.168.10.80:9999/', 'runningBackGroundJobHandler', '', NULL, 0, '2026-11-03 16:35:50', 200, '任务触发类型：Cron触发<br>调度机器：172.18.0.8<br>执行器-注册方式：自动注册<br>执行器-地址列表：[http://192.168.10.80:9999/]<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>心跳检测：<br>address：http://192.168.10.80:9999/<br>code：200<br>msg：null<br><br>触发调度：<br>address：http://192.168.10.80:9999/<br>code：200<br>msg：null', '2026-11-03 16:35:50', 200, '', 0);
INSERT INTO `xxl_job_log` VALUES (14851, 3, 20, 'http://192.168.10.80:9999/', 'runningBackGroundJobHandler', '', NULL, 0, '2026-11-03 16:36:00', 200, '任务触发类型：Cron触发<br>调度机器：172.18.0.8<br>执行器-注册方式：自动注册<br>执行器-地址列表：[http://192.168.10.80:9999/]<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>心跳检测：<br>address：http://192.168.10.80:9999/<br>code：200<br>msg：null<br><br>触发调度：<br>address：http://192.168.10.80:9999/<br>code：200<br>msg：null', '2026-11-03 16:36:00', 200, '', 0);
INSERT INTO `xxl_job_log` VALUES (14852, 3, 20, 'http://192.168.10.80:9999/', 'runningBackGroundJobHandler', '', NULL, 0, '2026-11-03 16:36:10', 200, '任务触发类型：Cron触发<br>调度机器：172.18.0.8<br>执行器-注册方式：自动注册<br>执行器-地址列表：[http://192.168.10.80:9999/]<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>心跳检测：<br>address：http://192.168.10.80:9999/<br>code：200<br>msg：null<br><br>触发调度：<br>address：http://192.168.10.80:9999/<br>code：200<br>msg：null', '2026-11-03 16:36:10', 200, '', 0);
INSERT INTO `xxl_job_log` VALUES (14853, 3, 20, 'http://192.168.10.80:9999/', 'runningBackGroundJobHandler', '', NULL, 0, '2026-11-03 16:36:20', 200, '任务触发类型：Cron触发<br>调度机器：172.18.0.8<br>执行器-注册方式：自动注册<br>执行器-地址列表：[http://192.168.10.80:9999/]<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>心跳检测：<br>address：http://192.168.10.80:9999/<br>code：200<br>msg：null<br><br>触发调度：<br>address：http://192.168.10.80:9999/<br>code：200<br>msg：null', '2026-11-03 16:36:20', 200, '', 0);
INSERT INTO `xxl_job_log` VALUES (14854, 3, 20, 'http://192.168.10.80:9999/', 'runningBackGroundJobHandler', '', NULL, 0, '2026-11-03 16:36:30', 200, '任务触发类型：Cron触发<br>调度机器：172.18.0.8<br>执行器-注册方式：自动注册<br>执行器-地址列表：[http://192.168.10.80:9999/]<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>心跳检测：<br>address：http://192.168.10.80:9999/<br>code：200<br>msg：null<br><br>触发调度：<br>address：http://192.168.10.80:9999/<br>code：200<br>msg：null', '2026-11-03 16:36:30', 200, '', 0);
INSERT INTO `xxl_job_log` VALUES (14855, 3, 20, 'http://192.168.10.80:9999/', 'runningBackGroundJobHandler', '', NULL, 0, '2026-11-03 16:36:40', 200, '任务触发类型：Cron触发<br>调度机器：172.18.0.8<br>执行器-注册方式：自动注册<br>执行器-地址列表：[http://192.168.10.80:9999/]<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>心跳检测：<br>address：http://192.168.10.80:9999/<br>code：200<br>msg：null<br><br>触发调度：<br>address：http://192.168.10.80:9999/<br>code：200<br>msg：null', '2026-11-03 16:36:40', 200, '', 0);
INSERT INTO `xxl_job_log` VALUES (14856, 3, 20, 'http://192.168.10.80:9999/', 'runningBackGroundJobHandler', '', NULL, 0, '2026-11-03 16:36:50', 200, '任务触发类型：Cron触发<br>调度机器：172.18.0.8<br>执行器-注册方式：自动注册<br>执行器-地址列表：[http://192.168.10.80:9999/]<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>心跳检测：<br>address：http://192.168.10.80:9999/<br>code：200<br>msg：null<br><br>触发调度：<br>address：http://192.168.10.80:9999/<br>code：200<br>msg：null', '2026-11-03 16:36:50', 200, '', 0);
INSERT INTO `xxl_job_log` VALUES (14857, 3, 20, NULL, 'runningBackGroundJobHandler', '', NULL, 0, '2026-11-03 16:37:00', 500, '任务触发类型：Cron触发<br>调度机器：172.18.0.8<br>执行器-注册方式：自动注册<br>执行器-地址列表：[http://192.168.10.80:9999/]<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>心跳检测：<br>address：http://192.168.10.80:9999/<br>code：500<br>msg：xxl-job remoting error(Connection refused (Connection refused)), for url : http://192.168.10.80:9999/beat<br><br>', NULL, 0, NULL, 2);
INSERT INTO `xxl_job_log` VALUES (14858, 3, 20, NULL, 'runningBackGroundJobHandler', '', NULL, 0, '2026-11-03 16:37:10', 500, '任务触发类型：Cron触发<br>调度机器：172.18.0.8<br>执行器-注册方式：自动注册<br>执行器-地址列表：[http://192.168.10.80:9999/]<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>心跳检测：<br>address：http://192.168.10.80:9999/<br>code：500<br>msg：xxl-job remoting error(Connection refused (Connection refused)), for url : http://192.168.10.80:9999/beat<br><br>', NULL, 0, NULL, 2);
INSERT INTO `xxl_job_log` VALUES (14859, 3, 20, NULL, 'runningBackGroundJobHandler', '', NULL, 0, '2026-11-03 16:37:20', 500, '任务触发类型：Cron触发<br>调度机器：172.18.0.8<br>执行器-注册方式：自动注册<br>执行器-地址列表：[http://192.168.10.80:9999/]<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>心跳检测：<br>address：http://192.168.10.80:9999/<br>code：500<br>msg：xxl-job remoting error(Connection refused (Connection refused)), for url : http://192.168.10.80:9999/beat<br><br>', NULL, 0, NULL, 2);
INSERT INTO `xxl_job_log` VALUES (14860, 3, 20, NULL, 'runningBackGroundJobHandler', '', NULL, 0, '2026-11-03 16:37:30', 500, '任务触发类型：Cron触发<br>调度机器：172.18.0.8<br>执行器-注册方式：自动注册<br>执行器-地址列表：[http://192.168.10.80:9999/]<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>心跳检测：<br>address：http://192.168.10.80:9999/<br>code：500<br>msg：xxl-job remoting error(Connection refused (Connection refused)), for url : http://192.168.10.80:9999/beat<br><br>', NULL, 0, NULL, 2);
INSERT INTO `xxl_job_log` VALUES (14861, 3, 20, NULL, 'runningBackGroundJobHandler', '', NULL, 0, '2026-11-03 16:37:40', 500, '任务触发类型：Cron触发<br>调度机器：172.18.0.8<br>执行器-注册方式：自动注册<br>执行器-地址列表：[http://192.168.10.80:9999/]<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>心跳检测：<br>address：http://192.168.10.80:9999/<br>code：500<br>msg：xxl-job remoting error(Connection refused (Connection refused)), for url : http://192.168.10.80:9999/beat<br><br>', NULL, 0, NULL, 2);
INSERT INTO `xxl_job_log` VALUES (14862, 3, 20, NULL, 'runningBackGroundJobHandler', '', NULL, 0, '2026-11-03 16:37:50', 500, '任务触发类型：Cron触发<br>调度机器：172.18.0.8<br>执行器-注册方式：自动注册<br>执行器-地址列表：[http://192.168.10.80:9999/]<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>心跳检测：<br>address：http://192.168.10.80:9999/<br>code：500<br>msg：xxl-job remoting error(Connection refused (Connection refused)), for url : http://192.168.10.80:9999/beat<br><br>', NULL, 0, NULL, 2);
INSERT INTO `xxl_job_log` VALUES (14863, 3, 20, NULL, 'runningBackGroundJobHandler', '', NULL, 0, '2026-11-03 16:38:00', 500, '任务触发类型：Cron触发<br>调度机器：172.18.0.8<br>执行器-注册方式：自动注册<br>执行器-地址列表：[http://192.168.10.80:9999/]<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>心跳检测：<br>address：http://192.168.10.80:9999/<br>code：500<br>msg：xxl-job remoting error(Connection refused (Connection refused)), for url : http://192.168.10.80:9999/beat<br><br>', NULL, 0, NULL, 2);
INSERT INTO `xxl_job_log` VALUES (14864, 3, 20, NULL, 'runningBackGroundJobHandler', '', NULL, 0, '2026-11-03 16:38:10', 500, '任务触发类型：Cron触发<br>调度机器：172.18.0.8<br>执行器-注册方式：自动注册<br>执行器-地址列表：[http://192.168.10.80:9999/]<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>心跳检测：<br>address：http://192.168.10.80:9999/<br>code：500<br>msg：xxl-job remoting error(Connection refused (Connection refused)), for url : http://192.168.10.80:9999/beat<br><br>', NULL, 0, NULL, 2);
INSERT INTO `xxl_job_log` VALUES (14865, 3, 20, NULL, 'runningBackGroundJobHandler', '', NULL, 0, '2026-11-03 16:38:20', 500, '任务触发类型：Cron触发<br>调度机器：172.18.0.8<br>执行器-注册方式：自动注册<br>执行器-地址列表：[http://192.168.10.80:9999/]<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>心跳检测：<br>address：http://192.168.10.80:9999/<br>code：500<br>msg：xxl-job remoting error(Connection refused (Connection refused)), for url : http://192.168.10.80:9999/beat<br><br>', NULL, 0, NULL, 2);
INSERT INTO `xxl_job_log` VALUES (14866, 3, 20, NULL, 'runningBackGroundJobHandler', '', NULL, 0, '2026-11-03 16:38:30', 500, '任务触发类型：Cron触发<br>调度机器：172.18.0.8<br>执行器-注册方式：自动注册<br>执行器-地址列表：null<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>调度失败：执行器地址为空<br><br>', NULL, 0, NULL, 2);
INSERT INTO `xxl_job_log` VALUES (14867, 3, 20, NULL, 'runningBackGroundJobHandler', '', NULL, 0, '2026-11-03 16:38:40', 500, '任务触发类型：Cron触发<br>调度机器：172.18.0.8<br>执行器-注册方式：自动注册<br>执行器-地址列表：null<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>调度失败：执行器地址为空<br><br>', NULL, 0, NULL, 2);
INSERT INTO `xxl_job_log` VALUES (14868, 3, 20, NULL, 'runningBackGroundJobHandler', '', NULL, 0, '2026-11-03 16:38:50', 500, '任务触发类型：Cron触发<br>调度机器：172.18.0.8<br>执行器-注册方式：自动注册<br>执行器-地址列表：null<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>调度失败：执行器地址为空<br><br>', NULL, 0, NULL, 2);
INSERT INTO `xxl_job_log` VALUES (14869, 3, 20, NULL, 'runningBackGroundJobHandler', '', NULL, 0, '2026-11-03 16:39:00', 500, '任务触发类型：Cron触发<br>调度机器：172.18.0.8<br>执行器-注册方式：自动注册<br>执行器-地址列表：null<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>调度失败：执行器地址为空<br><br>', NULL, 0, NULL, 2);
INSERT INTO `xxl_job_log` VALUES (14870, 3, 20, NULL, 'runningBackGroundJobHandler', '', NULL, 0, '2026-11-03 16:39:10', 500, '任务触发类型：Cron触发<br>调度机器：172.18.0.8<br>执行器-注册方式：自动注册<br>执行器-地址列表：null<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>调度失败：执行器地址为空<br><br>', NULL, 0, NULL, 2);
INSERT INTO `xxl_job_log` VALUES (14871, 3, 20, NULL, 'runningBackGroundJobHandler', '', NULL, 0, '2026-11-03 16:39:20', 500, '任务触发类型：Cron触发<br>调度机器：172.18.0.8<br>执行器-注册方式：自动注册<br>执行器-地址列表：null<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>调度失败：执行器地址为空<br><br>', NULL, 0, NULL, 2);
INSERT INTO `xxl_job_log` VALUES (14872, 3, 20, NULL, 'runningBackGroundJobHandler', '', NULL, 0, '2026-11-03 16:39:30', 500, '任务触发类型：Cron触发<br>调度机器：172.18.0.8<br>执行器-注册方式：自动注册<br>执行器-地址列表：null<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>调度失败：执行器地址为空<br><br>', NULL, 0, NULL, 2);
INSERT INTO `xxl_job_log` VALUES (14873, 3, 20, NULL, 'runningBackGroundJobHandler', '', NULL, 0, '2026-11-03 16:39:40', 500, '任务触发类型：Cron触发<br>调度机器：172.18.0.8<br>执行器-注册方式：自动注册<br>执行器-地址列表：null<br>路由策略：故障转移<br>阻塞处理策略：丢弃后续调度<br>任务超时时间：0<br>失败重试次数：0<br><br><span style=\"color:#00c0ef;\" > >>>>>>>>>>>触发调度<<<<<<<<<<< </span><br>调度失败：执行器地址为空<br><br>', NULL, 0, NULL, 2);

-- ----------------------------
-- Table structure for xxl_job_log_report
-- ----------------------------
DROP TABLE IF EXISTS `xxl_job_log_report`;
CREATE TABLE `xxl_job_log_report`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `trigger_day` datetime NULL DEFAULT NULL COMMENT '调度-时间',
  `running_count` int NOT NULL DEFAULT 0 COMMENT '运行中-日志数量',
  `suc_count` int NOT NULL DEFAULT 0 COMMENT '执行成功-日志数量',
  `fail_count` int NOT NULL DEFAULT 0 COMMENT '执行失败-日志数量',
  `update_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `i_trigger_day`(`trigger_day` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 146 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of xxl_job_log_report
-- ----------------------------
INSERT INTO `xxl_job_log_report` VALUES (140, '2026-10-31 00:00:00', 0, 0, 1, NULL);
INSERT INTO `xxl_job_log_report` VALUES (141, '2026-10-30 00:00:00', 0, 0, 0, NULL);
INSERT INTO `xxl_job_log_report` VALUES (142, '2026-10-29 00:00:00', 0, 0, 0, NULL);
INSERT INTO `xxl_job_log_report` VALUES (143, '2026-11-01 00:00:00', 0, 0, 7, NULL);
INSERT INTO `xxl_job_log_report` VALUES (144, '2026-11-03 00:00:00', 0, 13, 31, NULL);
INSERT INTO `xxl_job_log_report` VALUES (145, '2026-11-02 00:00:00', 0, 0, 0, NULL);

-- ----------------------------
-- Table structure for xxl_job_logglue
-- ----------------------------
DROP TABLE IF EXISTS `xxl_job_logglue`;
CREATE TABLE `xxl_job_logglue`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `job_id` int NOT NULL COMMENT '任务，主键ID',
  `glue_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT 'GLUE类型',
  `glue_source` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL COMMENT 'GLUE源代码',
  `glue_remark` varchar(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT 'GLUE备注',
  `add_time` datetime NULL DEFAULT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of xxl_job_logglue
-- ----------------------------

-- ----------------------------
-- Table structure for xxl_job_registry
-- ----------------------------
DROP TABLE IF EXISTS `xxl_job_registry`;
CREATE TABLE `xxl_job_registry`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `registry_group` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `registry_key` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `registry_value` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL,
  `update_time` datetime NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `i_g_k_v`(`registry_group` ASC, `registry_key` ASC, `registry_value` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 793 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of xxl_job_registry
-- ----------------------------

-- ----------------------------
-- Table structure for xxl_job_user
-- ----------------------------
DROP TABLE IF EXISTS `xxl_job_user`;
CREATE TABLE `xxl_job_user`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '账号',
  `password` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '密码',
  `role` tinyint NOT NULL COMMENT '角色：0-普通用户、1-管理员',
  `permission` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '权限：执行器ID列表，多个逗号分割',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `i_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of xxl_job_user
-- ----------------------------
INSERT INTO `xxl_job_user` VALUES (1, 'admin', 'e10adc3949ba59abbe56e057f20f883e', 1, NULL);

SET FOREIGN_KEY_CHECKS = 1;

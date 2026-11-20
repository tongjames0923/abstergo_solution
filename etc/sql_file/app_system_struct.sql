/*
 Navicat Premium Dump SQL

 Source Server         : local docker
 Source Server Type    : MySQL
 Source Server Version : 80411 (8.4.11)
 Source Host           : localhost:3306
 Source Schema         : app_system

 Target Server Type    : MySQL
 Target Server Version : 80411 (8.4.11)
 File Encoding         : 65001

 Date: 09/10/2026 16:15:23
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for app_package
-- ----------------------------
DROP TABLE IF EXISTS `app_package`;
CREATE TABLE `app_package` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花算法生成）',
  `bucket_name` varchar(255) DEFAULT NULL COMMENT 'OSS 存储桶名称',
  `create_by` bigint DEFAULT NULL COMMENT '创建者用户ID',
  `create_time` datetime(6) DEFAULT NULL COMMENT '创建时间',
  `download_url` varchar(255) DEFAULT NULL COMMENT '安装包下载地址 URL',
  `flag` int DEFAULT NULL COMMENT '数据状态标志：1-启用，-1-禁用，0-删除',
  `md5` varchar(255) DEFAULT NULL COMMENT '安装包文件 MD5 校验值',
  `object_name` varchar(255) DEFAULT NULL COMMENT 'OSS 对象名称（文件路径）',
  `update_by` bigint DEFAULT NULL COMMENT '更新者用户ID',
  `update_time` datetime(6) DEFAULT NULL COMMENT '更新时间',
  `version_id` bigint DEFAULT NULL COMMENT '关联的应用版本ID（援引 app_version_info.id）',
  PRIMARY KEY (`id`),
  KEY `idx_version_id` (`version_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='应用安装包表';

-- ----------------------------
-- Table structure for app_version_compatibility_policy
-- ----------------------------
DROP TABLE IF EXISTS `app_version_compatibility_policy`;
CREATE TABLE `app_version_compatibility_policy` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花算法生成）',
  `action` int NOT NULL COMMENT '动作类型（不兼容时的处理策略）：1-force 强制更新，2-recommend 建议更新，3-notify 仅提醒',
  `create_by` bigint DEFAULT NULL COMMENT '创建者用户ID',
  `create_time` datetime(6) DEFAULT NULL COMMENT '创建时间',
  `flag` int DEFAULT NULL COMMENT '数据状态标志：1-启用，-1-禁用，0-删除',
  `min_compat_version` bigint NOT NULL COMMENT '目标应用最低兼容版本号（低于此版本将触发更新）',
  `source_app_key` varchar(64) NOT NULL COMMENT '源应用标识（如 backend-service、web-console），表示哪个应用发布版本触发该策略',
  `source_platform` varchar(32) DEFAULT NULL COMMENT '源平台（如 ANDROID、IOS、WEB、BACKEND）',
  `source_version_code` bigint DEFAULT NULL COMMENT '源应用版本号（可空，为空表示所有版本均适用）',
  `target_app_key` varchar(64) NOT NULL COMMENT '目标应用标识（表示对哪个应用提出版本要求）',
  `target_platform` varchar(32) DEFAULT NULL COMMENT '目标平台（如 ANDROID、IOS、WEB）',
  `update_by` bigint DEFAULT NULL COMMENT '更新者用户ID',
  `update_time` datetime(6) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_source` (`source_app_key`,`source_version_code`,`source_platform`),
  KEY `idx_target` (`target_app_key`,`target_platform`,`flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='应用版本兼容策略表';

-- ----------------------------
-- Table structure for app_version_info
-- ----------------------------
DROP TABLE IF EXISTS `app_version_info`;
CREATE TABLE `app_version_info` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花算法生成）',
  `app_key` varchar(64) DEFAULT NULL COMMENT '应用唯一标识（如 backend-service、mobile-app）',
  `build_info` text COMMENT '构建信息（JSON 格式的构建元数据）',
  `create_by` bigint DEFAULT NULL COMMENT '创建者用户ID',
  `create_time` datetime(6) DEFAULT NULL COMMENT '创建时间',
  `flag` int DEFAULT NULL COMMENT '版本状态：0-草稿，1-发布，2-待审核，-1-下线，-2-回滚，-3-归档',
  `offline_time` datetime(6) DEFAULT NULL COMMENT '版本下线时间',
  `platform` varchar(32) DEFAULT NULL COMMENT '目标平台（如 ANDROID、IOS、WEB）',
  `release_note` text COMMENT '版本更新日志（描述本次版本的变更内容）',
  `release_time` datetime(6) DEFAULT NULL COMMENT '版本发布时间',
  `update_by` bigint DEFAULT NULL COMMENT '更新者用户ID',
  `update_time` datetime(6) DEFAULT NULL COMMENT '更新时间',
  `version_code` bigint DEFAULT NULL COMMENT '内部版本号（递增整数，用于版本比较）',
  `version_name` varchar(64) DEFAULT NULL COMMENT '外部版本名称（用户可见的版本号，如 1.2.0）',
  `version_type` int DEFAULT NULL COMMENT '版本类型：1-alpha，2-beta，3-rc，4-release',
  PRIMARY KEY (`id`),
  KEY `IDXj53bupwoxufnjtu17kpjk5gu2` (`app_key`,`version_code`),
  KEY `IDX9cd1jj6lob0wcwer68ly9akd0` (`version_name`),
  KEY `IDXi9b41sl0tw5yhwj1jkvv9mqgx` (`version_type`,`platform`),
  KEY `IDXl36rwti7637k1aby3jks715yo` (`flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='应用版本信息表';

-- ----------------------------
-- Table structure for dynamic_form_configration
-- ----------------------------
DROP TABLE IF EXISTS `dynamic_form_configration`;
CREATE TABLE `dynamic_form_configration` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID（自增）',
  `expandable` int DEFAULT NULL COMMENT '表单是否支持展开/收起（0:不可，1:可以）',
  `expanded` int DEFAULT NULL COMMENT '表单字段是否展开（0:收起，1:展开）',
  `flag` int DEFAULT NULL COMMENT '表单状态：1-已启用，0-未启用，-1-已删除',
  `gutter` int DEFAULT NULL COMMENT '表单字段之间的间隔（像素值）',
  `label_width` varchar(255) DEFAULT NULL COMMENT '表单标签宽度（如 100px）',
  `span` int DEFAULT NULL COMMENT '表单栅格占据的列数（基于 24 栅格系统）',
  `title` varchar(128) DEFAULT NULL COMMENT '表单标题',
  PRIMARY KEY (`id`),
  KEY `IDXnwmmyxc6ycs9enf76259ukckp` (`flag`),
  KEY `IDXhpd82ymnmm8l26ylo49r363et` (`title`),
  KEY `IDXivdgpn6eg2l95b8qaycymamxe` (`expandable`,`expanded`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='动态表单配置表';

-- ----------------------------
-- Table structure for form_field_configration
-- ----------------------------
DROP TABLE IF EXISTS `form_field_configration`;
CREATE TABLE `form_field_configration` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID（自增）',
  `label` varchar(64) DEFAULT NULL COMMENT '表单域标签（显示在表单中的字段名称）',
  `prop` varchar(64) DEFAULT NULL COMMENT '表单域对应的数据属性名称（绑定字段名）',
  `type` varchar(64) DEFAULT NULL COMMENT '表单域组件类型（如 input、select、date 等）',
  PRIMARY KEY (`id`),
  KEY `gobal_index` (`type`,`prop`,`label`)
) ENGINE=InnoDB AUTO_INCREMENT=128 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='表单域配置表';

-- ----------------------------
-- Table structure for form_field_relation
-- ----------------------------
DROP TABLE IF EXISTS `form_field_relation`;
CREATE TABLE `form_field_relation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID（自增）',
  `field_id` bigint DEFAULT NULL COMMENT '表单域配置ID（援引 form_field_configration.id）',
  `form_id` bigint DEFAULT NULL COMMENT '表单配置ID（援引 dynamic_form_configration.id）',
  PRIMARY KEY (`id`),
  KEY `form_field_relation_form_id_index` (`form_id`)
) ENGINE=InnoDB AUTO_INCREMENT=128 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='表单与表单域关联表';

-- ----------------------------
-- Table structure for oss_download_code
-- ----------------------------
DROP TABLE IF EXISTS `oss_download_code`;
CREATE TABLE `oss_download_code` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花算法生成）',
  `bucket` varchar(255) DEFAULT NULL COMMENT '存储桶名',
  `code` varchar(64) NOT NULL COMMENT '下载唯一码',
  `create_time` datetime(6) DEFAULT NULL COMMENT '创建时间',
  `expire_at` bigint DEFAULT NULL COMMENT '过期时间戳（毫秒），小于等于0表示永不过期',
  `flag` int DEFAULT NULL COMMENT '下载码状态：1-有效，-1-已删除',
  `object_key` varchar(512) NOT NULL COMMENT '存储对象键',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注说明',
  `update_time` datetime(6) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`),
  KEY `idx_flag` (`flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='OSS 公开下载码表';

-- ----------------------------
-- Table structure for property_relation
-- ----------------------------
DROP TABLE IF EXISTS `property_relation`;
CREATE TABLE `property_relation` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID（自增）',
  `property_id` bigint DEFAULT NULL COMMENT '属性ID（援引 property_value.id）',
  `relation_id` bigint DEFAULT NULL COMMENT '关联对象ID（根据 relation_type 指向表单域ID或表单ID）',
  `relation_type` int DEFAULT NULL COMMENT '关联类型（1:绑定到字段，2:绑定到表单）',
  PRIMARY KEY (`id`),
  KEY `property_relation_property_id_index` (`property_id`),
  KEY `property_relation_relation_id_index` (`relation_id`,`relation_type`)
) ENGINE=InnoDB AUTO_INCREMENT=344 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='属性关系表';

-- ----------------------------
-- Table structure for property_value
-- ----------------------------
DROP TABLE IF EXISTS `property_value`;
CREATE TABLE `property_value` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID（自增）',
  `prop_name` varchar(64) DEFAULT NULL COMMENT '属性名称（如 placeholder、disabled、rules 等）',
  `transformer_type` varchar(64) DEFAULT NULL COMMENT '属性值转换器类型（通过 Spring Bean 名称获取实例，进行类型转换）',
  `prop_value` text COMMENT '属性值（JSON 或纯文本格式）',
  `value_class` varchar(256) DEFAULT NULL COMMENT '属性值对应的目标类全限定名（用于反序列化）',
  PRIMARY KEY (`id`),
  KEY `name_idx` (`prop_name`)
) ENGINE=InnoDB AUTO_INCREMENT=344 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='属性设置表';

-- ----------------------------
-- Table structure for push_message
-- ----------------------------
DROP TABLE IF EXISTS `push_message`;
CREATE TABLE `push_message` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键（即业务消息标识，自增）',
  `content` varchar(255) DEFAULT NULL COMMENT '消息正文',
  `create_time` datetime(6) NOT NULL COMMENT '创建时间（即消息发送时间）',
  `ext_info` varchar(255) DEFAULT NULL COMMENT '扩展信息（JSON，业务自定义字段）',
  `msg_type` varchar(32) NOT NULL COMMENT '消息类型（可扩展字典，现有契约值：notify-通知，warning-告警，reminder-提醒）',
  `priority` varchar(16) NOT NULL COMMENT '消息优先级：low-低，normal-普通（缺省），high-高，urgent-紧急',
  `sender` varchar(64) DEFAULT NULL COMMENT '发送者展示标识（审计权威见操作日志表 SEND 记录）',
  `title` varchar(255) DEFAULT NULL COMMENT '消息标题',
  `topic` varchar(128) NOT NULL COMMENT '业务主题（接收人按订阅主题匹配投递）',
  `update_time` datetime(6) DEFAULT NULL COMMENT '更新时间',
  `url` varchar(512) DEFAULT NULL COMMENT '点击跳转地址',
  PRIMARY KEY (`id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=15 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='推送业务消息主表';

-- ----------------------------
-- Table structure for push_message_operation_log
-- ----------------------------
DROP TABLE IF EXISTS `push_message_operation_log`;
CREATE TABLE `push_message_operation_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键（自增）',
  `create_time` datetime(6) NOT NULL COMMENT '创建时间',
  `ext_info` varchar(255) DEFAULT NULL COMMENT '操作上下文扩展（JSON，服务名、调用方标识、点击位置等）',
  `message_id` bigint NOT NULL COMMENT '消息ID（关联 push_message.id）',
  `op_source` varchar(32) DEFAULT NULL COMMENT '操作来源通道（http/rpc/job/system）',
  `op_time` datetime(6) NOT NULL COMMENT '操作发生时间',
  `op_type` varchar(32) NOT NULL COMMENT '操作类型：SEND-发送，READ-已读，CLICK-点击',
  `operator` bigint DEFAULT NULL COMMENT '操作人用户ID（系统/服务发起时可空，身份进 ext_info）',
  `update_time` datetime(6) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_operator_msg` (`operator`,`message_id`),
  KEY `idx_message` (`message_id`),
  KEY `idx_op_time` (`op_time`)
) ENGINE=InnoDB AUTO_INCREMENT=44 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='推送消息全链路操作日志表';

-- ----------------------------
-- Table structure for push_message_receiver
-- ----------------------------
DROP TABLE IF EXISTS `push_message_receiver`;
CREATE TABLE `push_message_receiver` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键（兼游标分页排序键，自增即到达序）',
  `create_time` datetime(6) NOT NULL COMMENT '创建时间（即消息到达时间）',
  `message_id` bigint NOT NULL COMMENT '消息ID（关联 push_message.id）',
  `receiver` bigint NOT NULL COMMENT '接收用户ID',
  `update_time` datetime(6) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_receiver` (`receiver`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=123 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='推送消息接收人关系表';

-- ----------------------------
-- Table structure for sys_company
-- ----------------------------
DROP TABLE IF EXISTS `sys_company`;
CREATE TABLE `sys_company` (
  `id` bigint NOT NULL COMMENT '公司ID（雪花算法生成）',
  `company_name` varchar(128) NOT NULL COMMENT '公司名称',
  `create_by` bigint DEFAULT NULL COMMENT '创建者用户ID',
  `create_time` datetime(6) DEFAULT NULL COMMENT '创建时间',
  `description` text COMMENT '公司描述',
  `flag` int DEFAULT NULL COMMENT '公司状态：0-已删除，1-已激活，2-禁用，5-系统内置',
  `logo_image` varchar(256) DEFAULT NULL COMMENT '公司Logo图片URL',
  `tax_number` varchar(64) NOT NULL COMMENT '公司税号',
  `update_by` bigint DEFAULT NULL COMMENT '更新者用户ID',
  `update_time` datetime(6) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_company_name` (`company_name`),
  KEY `idx_flag` (`flag`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_update_time` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='公司信息表';

-- ----------------------------
-- Table structure for sys_data_dic
-- ----------------------------
DROP TABLE IF EXISTS `sys_data_dic`;
CREATE TABLE `sys_data_dic` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID（自增）',
  `create_time` datetime(6) DEFAULT NULL COMMENT '创建时间',
  `data_dic_code` varchar(255) DEFAULT NULL COMMENT '数据字典编码（业务侧按编码检索字典项）',
  `data_dic_name` varchar(255) DEFAULT NULL COMMENT '数据字典名称',
  `flag` int DEFAULT NULL COMMENT '数据字典状态：1-启用，-1-停用，0-删除',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `update_time` datetime(6) DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_data_dic_name_code` (`data_dic_name`,`data_dic_code`),
  KEY `idx_flag` (`flag`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数据字典主表';

-- ----------------------------
-- Table structure for sys_department
-- ----------------------------
DROP TABLE IF EXISTS `sys_department`;
CREATE TABLE `sys_department` (
  `id` bigint NOT NULL COMMENT '部门ID（雪花算法生成）',
  `company_id` bigint NOT NULL COMMENT '所属公司ID',
  `create_by` bigint DEFAULT NULL COMMENT '创建者用户ID',
  `create_time` datetime(6) DEFAULT NULL COMMENT '创建时间',
  `department_code` varchar(64) DEFAULT NULL COMMENT '部门编码',
  `department_name` varchar(128) NOT NULL COMMENT '部门名称',
  `description` text COMMENT '部门描述',
  `flag` int DEFAULT NULL COMMENT '部门状态：0-已删除，1-已激活，2-禁用',
  `manager` varchar(64) DEFAULT NULL COMMENT '部门负责人姓名',
  `parent_id` bigint DEFAULT NULL COMMENT '父部门ID（用于构建树形结构）',
  `update_by` bigint DEFAULT NULL COMMENT '更新者用户ID',
  `update_time` datetime(6) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_name_code` (`department_name`,`department_code`),
  KEY `idx_flag` (`flag`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_update_time` (`update_time`),
  KEY `idx_company_id` (`company_id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统部门信息表';

-- ----------------------------
-- Table structure for sys_generic_api
-- ----------------------------
DROP TABLE IF EXISTS `sys_generic_api`;
CREATE TABLE `sys_generic_api` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `api_id` varchar(100) NOT NULL COMMENT 'API唯一标识',
  `api_name` varchar(200) NOT NULL COMMENT 'API名称',
  `app_name` varchar(100) NOT NULL COMMENT '服务应用名（用于服务发现）',
  `business` varchar(100) DEFAULT NULL COMMENT '业务分区（Dubbo group），留空只匹配无group的提供者，*表示不限',
  `create_by` bigint DEFAULT NULL COMMENT '创建者用户ID',
  `create_time` datetime(6) DEFAULT NULL COMMENT '创建时间',
  `description` varchar(500) DEFAULT NULL COMMENT '描述信息',
  `flag` int DEFAULT NULL COMMENT '状态标识：1-启用，-1-禁用，0-删除',
  `interface_name` varchar(300) NOT NULL COMMENT '接口全限定名（用于Triple HTTP调用）',
  `method_name` varchar(100) NOT NULL COMMENT '方法名',
  `need_auth` bit(1) NOT NULL COMMENT '是否需要认证',
  `pre_auth_beans` varchar(255) DEFAULT NULL COMMENT '预认证Bean列表（逗号分隔）',
  `update_by` bigint DEFAULT NULL COMMENT '更新者用户ID',
  `update_time` datetime(6) DEFAULT NULL COMMENT '更新时间',
  `version` int DEFAULT NULL COMMENT 'API版本号',
  PRIMARY KEY (`id`),
  KEY `idx_generic_api_api_id` (`api_id`),
  KEY `idx_generic_api_app_name` (`app_name`),
  KEY `idx_generic_api_flag` (`flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='泛化API配置表';

-- ----------------------------
-- Table structure for sys_generic_api_param
-- 与实体 SystemGenericApiParamEntity 的 @Table/@Index 声明一一对应
-- ----------------------------
DROP TABLE IF EXISTS `sys_generic_api_param`;
CREATE TABLE `sys_generic_api_param` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花算法生成）',
  `api_id` varchar(100) DEFAULT NULL COMMENT '所属泛化API标识（援引 sys_generic_api.api_id）',
  `create_by` bigint DEFAULT NULL COMMENT '创建者用户ID',
  `create_time` datetime(6) DEFAULT NULL COMMENT '创建时间',
  `description` varchar(500) DEFAULT NULL COMMENT '参数说明',
  `example` varchar(500) DEFAULT NULL COMMENT '示例值（JSON字面量文本）',
  `flag` int DEFAULT NULL COMMENT '状态标识：1-启用，-1-禁用，0-删除',
  `param_name` varchar(100) DEFAULT NULL COMMENT '参数名，空表示只支持按位置传参',
  `param_order` int DEFAULT NULL COMMENT '参数顺序，从0开始',
  `param_type` varchar(300) DEFAULT NULL COMMENT '参数类型：擦除后的JVM类名或基本类型关键字，不写泛型',
  `required_flag` bit(1) DEFAULT NULL COMMENT '是否必填',
  `update_by` bigint DEFAULT NULL COMMENT '更新者用户ID',
  `update_time` datetime(6) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_generic_api_param_api_id` (`api_id`),
  KEY `idx_generic_api_param_flag` (`flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='泛化API入参定义表';

-- ----------------------------
-- Table structure for sys_login_info
-- ----------------------------
DROP TABLE IF EXISTS `sys_login_info`;
CREATE TABLE `sys_login_info` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `create_time` datetime(6) DEFAULT NULL COMMENT '创建时间',
  `flag` int DEFAULT NULL COMMENT '登录状态：1-正常，0-已删除，-1-已撤销',
  `session_id` varchar(128) DEFAULT NULL COMMENT '会话ID',
  `token` varchar(128) DEFAULT NULL COMMENT '登录令牌',
  `token_expire_time` datetime(6) DEFAULT NULL COMMENT '令牌过期时间',
  `update_time` datetime(6) DEFAULT NULL COMMENT '更新时间',
  `user_id` bigint DEFAULT NULL COMMENT '关联用户ID',
  `current_department_id` bigint DEFAULT NULL COMMENT '当前会话选择的部门ID',
  PRIMARY KEY (`id`),
  KEY `idx_flag` (`flag`),
  KEY `idx_time` (`create_time`,`update_time`,`token_expire_time`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_token` (`token`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统登录信息表';

-- ----------------------------
-- Table structure for sys_operate_record
-- ----------------------------
DROP TABLE IF EXISTS `sys_operate_record`;
CREATE TABLE `sys_operate_record` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `cost_time` bigint DEFAULT NULL COMMENT '操作耗时（毫秒）',
  `method_type` varchar(64) DEFAULT NULL COMMENT 'HTTP方法类型（GET/PUT/POST等）',
  `operate_content` varchar(512) DEFAULT NULL COMMENT '操作内容（调用的方法描述）',
  `operate_error` longtext COMMENT '操作异常信息',
  `operate_error_level` int DEFAULT NULL COMMENT '操作异常级别：1-用户输入错误，2-业务逻辑错误，3-数据访问错误，4-系统错误，5-严重系统错误（仅异常时写入）',
  `operate_ip` varchar(64) DEFAULT NULL COMMENT '操作用户IP地址',
  `operate_param_json` longtext COMMENT '请求参数JSON',
  `operate_result_json` longtext COMMENT '操作结果JSON',
  `operate_time` datetime(6) DEFAULT NULL COMMENT '操作时间',
  `operate_type` varchar(128) DEFAULT NULL COMMENT '操作类型',
  `operate_url` varchar(512) DEFAULT NULL COMMENT '请求URL',
  `operate_user_name` varchar(64) DEFAULT NULL COMMENT '操作用户名称',
  `system_log_flag` int DEFAULT NULL COMMENT '系统日志标志',
  `trace_id` varchar(64) DEFAULT NULL COMMENT '链路追踪ID',
  PRIMARY KEY (`id`),
  KEY `common_index` (`operate_content`,`operate_type`),
  KEY `time_idx` (`operate_time`),
  KEY `error_level_idx` (`operate_error_level`),
  KEY `user_idx` (`operate_user_name`,`operate_ip`),
  KEY `url_idx` (`operate_url`),
  KEY `method_idx` (`method_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统操作记录表';

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `create_by` bigint DEFAULT NULL COMMENT '创建者用户ID',
  `create_time` datetime(6) DEFAULT NULL COMMENT '创建时间',
  `data_scope` int DEFAULT NULL COMMENT '数据权限范围：1-全部数据，2-部门数据，3-仅本人数据，4-自定义权限',
  `description` varchar(255) DEFAULT NULL COMMENT '描述信息',
  `flag` int DEFAULT NULL COMMENT '角色状态：1-激活，0-失效，-1-删除，2-系统内置',
  `name` varchar(255) DEFAULT NULL COMMENT '权限名称',
  `role` varchar(128) NOT NULL COMMENT '权限编码（唯一标识）',
  `type` int DEFAULT NULL COMMENT '权限类型：1-菜单权限，2-数据权限，3-API权限',
  `update_by` bigint DEFAULT NULL COMMENT '更新者用户ID',
  `update_time` datetime(6) DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_role_type` (`role`,`type`),
  KEY `idx_flag` (`flag`),
  KEY `idx_time` (`create_time`,`update_time`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统角色（权限）表';

-- ----------------------------
-- Table structure for sys_role_group
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_group`;
CREATE TABLE `sys_role_group` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `create_by` bigint DEFAULT NULL COMMENT '创建者用户ID',
  `create_time` datetime(6) DEFAULT NULL COMMENT '创建时间',
  `delete_by` bigint DEFAULT NULL COMMENT '删除者用户ID',
  `delete_time` datetime(6) DEFAULT NULL COMMENT '逻辑删除时间',
  `description` varchar(255) DEFAULT NULL COMMENT '角色组描述',
  `flag` int DEFAULT NULL COMMENT '角色组状态标识：1-启用，-1-禁用，0-删除',
  `role_group_name` varchar(255) DEFAULT NULL COMMENT '角色组名称',
  `update_by` bigint DEFAULT NULL COMMENT '更新者用户ID',
  `update_time` datetime(6) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_sys_role_group_flag` (`flag`),
  KEY `idx_sys_role_group_role_group_name` (`role_group_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统角色组表';

-- ----------------------------
-- Table structure for sys_role_group_relation
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_group_relation`;
CREATE TABLE `sys_role_group_relation` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花算法生成）',
  `create_by` bigint DEFAULT NULL COMMENT '创建者用户ID',
  `create_time` datetime(6) DEFAULT NULL COMMENT '创建时间',
  `delete_by` bigint DEFAULT NULL COMMENT '删除者用户ID',
  `delete_time` datetime(6) DEFAULT NULL COMMENT '逻辑删除时间',
  `flag` int DEFAULT NULL COMMENT '逻辑删除标志位：1-启用，-1-禁用，0-删除',
  `group_id` bigint DEFAULT NULL COMMENT '角色组ID（援引 sys_role_group.id）',
  `role_id` bigint DEFAULT NULL COMMENT '角色ID（援引 sys_role.id）',
  `update_by` bigint DEFAULT NULL COMMENT '更新者用户ID',
  `update_time` datetime(6) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_role_group_relation_role_id` (`role_id`),
  KEY `idx_role_group_relation_group_id` (`group_id`),
  KEY `idx_flag` (`flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统角色与角色组关系表';

-- ----------------------------
-- Table structure for sys_token
-- ----------------------------
DROP TABLE IF EXISTS `sys_token`;
CREATE TABLE `sys_token` (
  `id` bigint NOT NULL COMMENT '主键ID',
  `create_time` datetime(6) DEFAULT NULL COMMENT '创建时间',
  `create_type` int DEFAULT NULL COMMENT '令牌创建方式：1-登录，2-刷新替换，3-第三方登录',
  `expire_time` datetime(6) DEFAULT NULL COMMENT '过期时间',
  `flag` int DEFAULT NULL COMMENT '令牌状态：1-有效，0-已删除，-1-已撤销，-2-已过期，-3-无效',
  `ip_address` varchar(64) DEFAULT NULL COMMENT '登录设备IP地址',
  `mac_address` varchar(128) DEFAULT NULL COMMENT '登录设备MAC地址',
  `session_id` varchar(128) DEFAULT NULL COMMENT '会话ID',
  `token` varchar(128) NOT NULL COMMENT '令牌字符串',
  `token_type` int NOT NULL COMMENT '令牌类型：1-访问令牌，2-刷新令牌，3-应用访问令牌，4-第三方访问令牌',
  `update_time` datetime(6) DEFAULT NULL COMMENT '更新时间',
  `user_name` varchar(128) NOT NULL COMMENT '用户名称',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_token` (`token`),
  KEY `ix_user` (`user_name`),
  KEY `ix_time` (`create_time`,`update_time`),
  KEY `ix_flag` (`flag`),
  KEY `ix_expire` (`expire_time`),
  KEY `ix_token_type` (`token_type`),
  KEY `ix_session` (`session_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统令牌表';

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `id` bigint NOT NULL COMMENT '用户ID（雪花算法生成）',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `create_by` bigint DEFAULT NULL COMMENT '创建者用户ID',
  `create_time` datetime(6) DEFAULT NULL COMMENT '创建时间',
  `dept_id` bigint DEFAULT NULL COMMENT '主要所属部门ID',
  `flag` int DEFAULT NULL COMMENT '账号状态：1-激活，2-锁定，0-禁用，-1-删除',
  `nick_name` varchar(32) NOT NULL COMMENT '昵称',
  `password` varchar(72) NOT NULL COMMENT '密码（加密存储）',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `update_by` bigint DEFAULT NULL COMMENT '最后更新者用户ID',
  `update_time` datetime(6) DEFAULT NULL COMMENT '修改时间',
  `user_name` varchar(72) NOT NULL COMMENT '用户名',
  PRIMARY KEY (`id`),
  KEY `idx_username_password` (`user_name`,`password`),
  KEY `idx_nickname` (`nick_name`),
  KEY `idx_flag` (`flag`),
  KEY `idx_time` (`create_time`,`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统用户表';

-- ----------------------------
-- Table structure for sys_user_external_identity
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_external_identity`;
CREATE TABLE `sys_user_external_identity` (
  `id` bigint NOT NULL COMMENT '绑定ID（雪花算法生成）',
  `create_by` bigint DEFAULT NULL COMMENT '创建者用户ID',
  `create_time` datetime(6) DEFAULT NULL COMMENT '创建时间',
  `external_id` varchar(128) NOT NULL COMMENT '外部身份标识（如企微 userid）',
  `flag` int DEFAULT NULL COMMENT '绑定状态（1为有效）',
  `identity_type` varchar(32) NOT NULL COMMENT '外部身份类型（如 wecom）',
  `update_by` bigint DEFAULT NULL COMMENT '最后更新者用户ID',
  `update_time` datetime(6) DEFAULT NULL COMMENT '修改时间',
  `user_id` bigint NOT NULL COMMENT '绑定的系统用户ID',
  PRIMARY KEY (`id`),
  KEY `idx_identity_type_external_id` (`identity_type`,`external_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_flag` (`flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统用户外部身份绑定表';

-- ----------------------------
-- Table structure for system_data_dic_item
-- ----------------------------
DROP TABLE IF EXISTS `system_data_dic_item`;
CREATE TABLE `system_data_dic_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID（自增）',
  `create_time` datetime(6) DEFAULT NULL COMMENT '创建时间',
  `dic_code` varchar(255) DEFAULT NULL COMMENT '所属数据字典编码',
  `flag` int DEFAULT NULL COMMENT '数据字典项状态：1-启用，-1-停用，0-删除',
  `label` varchar(255) DEFAULT NULL COMMENT '字典项显示标签（展示给用户的名称）',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `sort` int DEFAULT NULL COMMENT '排序号（升序）',
  `update_time` datetime(6) DEFAULT NULL COMMENT '修改时间',
  `value` varchar(255) DEFAULT NULL COMMENT '字典项取值（业务存储与比较的实际值）',
  PRIMARY KEY (`id`),
  KEY `idx_system_data_dic_item_dic_code` (`dic_code`),
  KEY `idx_flag` (`flag`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='数据字典项表';

-- ----------------------------
-- Table structure for system_role_group_user_relation
-- ----------------------------
DROP TABLE IF EXISTS `system_role_group_user_relation`;
CREATE TABLE `system_role_group_user_relation` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花算法生成）',
  `role_group_id` bigint DEFAULT NULL COMMENT '角色组ID（援引 sys_role_group.id）',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID（援引 sys_user.id）',
  PRIMARY KEY (`id`),
  KEY `idx_system_role_group_user_relation_role_group_id` (`role_group_id`),
  KEY `idx_system_role_group_user_relation_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='角色组与用户关系表';

-- ----------------------------
-- Table structure for system_user_department_relation
-- ----------------------------
DROP TABLE IF EXISTS `system_user_department_relation`;
CREATE TABLE `system_user_department_relation` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花算法生成）',
  `department_id` bigint DEFAULT NULL COMMENT '部门ID（援引 sys_department.id）',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID（援引 sys_user.id）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id_department_id` (`user_id`,`department_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_department_id` (`department_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统用户与部门关系表';

-- ----------------------------
-- Table structure for third_party_app
-- ----------------------------
DROP TABLE IF EXISTS `third_party_app`;
CREATE TABLE `third_party_app` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花算法生成）',
  `app_id` varchar(64) DEFAULT NULL COMMENT '应用标识（第三方应用唯一标识，未传入时由系统以 UUID 生成）',
  `create_by` bigint DEFAULT NULL COMMENT '创建者用户ID',
  `create_time` datetime(6) DEFAULT NULL COMMENT '创建时间',
  `flag` int DEFAULT NULL COMMENT '数据状态标志：1-启用，-1-禁用，0-删除',
  `grant_type` varchar(255) DEFAULT NULL COMMENT '登录授权类型（对应授权提供方的标识，如 password / wecom / third_party）',
  `proxy_id` bigint DEFAULT NULL COMMENT '代理用户ID（关联用户表，作为该操作的执行人账号）',
  `update_by` bigint DEFAULT NULL COMMENT '更新者用户ID',
  `update_time` datetime(6) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_app_id` (`app_id`),
  KEY `idx_flag` (`flag`),
  KEY `idx_proxy_id` (`proxy_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='第三方应用接出登记表';

-- ----------------------------
-- Table structure for tree_closure
-- ----------------------------
DROP TABLE IF EXISTS `tree_closure`;
CREATE TABLE `tree_closure` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键（自增）',
  `ancestor_id` varchar(64) NOT NULL COMMENT '祖先节点ID',
  `depth` int NOT NULL COMMENT '层级深度（0 为自身，1 为父子，2 为祖孙，依次类推）',
  `descendant_id` varchar(64) NOT NULL COMMENT '后代节点ID',
  `group_name` varchar(64) NOT NULL COMMENT '树组名称（作为树的唯一标识）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK4q1vrtow52u2ih3utui5nxues` (`ancestor_id`,`descendant_id`),
  KEY `IDXlcjh1709srvurk844d5l9jjtu` (`ancestor_id`),
  KEY `IDX5spq70g67u0mhko5tuwntwxa3` (`descendant_id`),
  KEY `IDXgkkwbnbsyakv2u533604sdto1` (`depth`),
  KEY `IDXi7fuflkybyivi3mhmaj5cfynt` (`group_name`)
) ENGINE=InnoDB AUTO_INCREMENT=64 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='树闭包关系表（存储任意祖先-后代关系对）';

-- ----------------------------
-- Table structure for tree_node
-- ----------------------------
DROP TABLE IF EXISTS `tree_node`;
CREATE TABLE `tree_node` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID（自增）',
  `create_time` datetime(6) DEFAULT NULL COMMENT '创建时间',
  `entity_id` bigint DEFAULT NULL COMMENT '节点对应的业务实体ID',
  `group_name` varchar(64) DEFAULT NULL COMMENT '树组名称（作为树的唯一标识，同一树组内节点ID 不重复）',
  `node_id` varchar(64) DEFAULT NULL COMMENT '节点ID（树内唯一标识）',
  `parent_id` varchar(64) DEFAULT NULL COMMENT '父节点ID（关系备份，闭包关系不可靠时可恢复；null 表示根节点）',
  `update_time` datetime(6) DEFAULT NULL COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKj31ps1fvxja2xe7166bjvweme` (`group_name`,`node_id`),
  KEY `IDXdnk5hk5vr111xcunlqkjfni6h` (`group_name`,`parent_id`),
  KEY `IDX5ytbu0cqp6qalcb0fdf9kl31r` (`entity_id`)
) ENGINE=InnoDB AUTO_INCREMENT=29 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='树节点表（闭包式树结构的节点主档）';

-- ----------------------------
-- Table structure for view_route_configration
-- ----------------------------
DROP TABLE IF EXISTS `view_route_configration`;
CREATE TABLE `view_route_configration` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花算法生成）',
  `always_show` int DEFAULT NULL COMMENT '是否始终显示在菜单中（0:否，1:是）',
  `component` varchar(255) DEFAULT NULL COMMENT '前端组件路径（Vue 组件文件路径）',
  `create_by` bigint DEFAULT NULL COMMENT '创建者用户ID',
  `create_time` datetime(6) DEFAULT NULL COMMENT '创建时间',
  `flag` int DEFAULT NULL COMMENT '状态标志（0:禁用，1:启用）',
  `hidden` int DEFAULT NULL COMMENT '是否隐藏（0:否，1:是）',
  `name` varchar(128) DEFAULT NULL COMMENT '路由名称（用于编程式导航）',
  `parent_id` bigint DEFAULT NULL COMMENT '父路由ID（用于构建树形路由结构）',
  `path` varchar(255) NOT NULL COMMENT '路由路径（前端路由 path）',
  `redirect` varchar(255) DEFAULT NULL COMMENT '重定向路径',
  `route_group` varchar(64) NOT NULL COMMENT '路由分组（如 common、admin、develop）',
  `sort_order` int DEFAULT NULL COMMENT '排序顺序（数值越小越靠前）',
  `update_by` bigint DEFAULT NULL COMMENT '更新者用户ID',
  `update_time` datetime(6) DEFAULT NULL COMMENT '更新时间',
  `version` bigint DEFAULT NULL COMMENT '所属版本ID（援引 view_route_version.id）',
  PRIMARY KEY (`id`),
  KEY `idx_route_group` (`route_group`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_flag` (`flag`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='视图路由配置表';

-- ----------------------------
-- Table structure for view_route_meta
-- ----------------------------
DROP TABLE IF EXISTS `view_route_meta`;
CREATE TABLE `view_route_meta` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花算法生成）',
  `active_menu` varchar(255) DEFAULT NULL COMMENT '激活菜单路径（高亮指定的菜单项）',
  `breadcrumb` int DEFAULT NULL COMMENT '是否显示面包屑（0:否，1:是）',
  `create_time` datetime(6) DEFAULT NULL COMMENT '创建时间',
  `flag` int DEFAULT NULL COMMENT '状态标志（0:禁用，1:启用）',
  `icon` varchar(128) DEFAULT NULL COMMENT '路由图标（菜单图标名称）',
  `route_group` varchar(64) DEFAULT NULL COMMENT '路由分组（与路由配置的 route_group 对应）',
  `route_id` bigint NOT NULL COMMENT '关联的路由ID（援引 view_route_configration.id）',
  `title` varchar(128) DEFAULT NULL COMMENT '路由标题（显示在菜单和面包屑中）',
  `update_time` datetime(6) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_route_id` (`route_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='视图路由元数据表';

-- ----------------------------
-- Table structure for view_route_role
-- ----------------------------
DROP TABLE IF EXISTS `view_route_role`;
CREATE TABLE `view_route_role` (
  `id` bigint NOT NULL COMMENT '主键ID（雪花算法生成）',
  `create_time` datetime(6) DEFAULT NULL COMMENT '创建时间',
  `flag` int DEFAULT NULL COMMENT '状态标志（0:禁用，1:启用）',
  `role_code` varchar(128) NOT NULL COMMENT '权限编码（对应角色的 roleCode）',
  `route_id` bigint NOT NULL COMMENT '关联的路由ID（援引 view_route_configration.id）',
  `update_time` datetime(6) DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_route_id` (`route_id`),
  KEY `idx_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='视图路由与角色关联表';

-- ----------------------------
-- Table structure for view_route_version
-- ----------------------------
DROP TABLE IF EXISTS `view_route_version`;
CREATE TABLE `view_route_version` (
  `id` bigint NOT NULL COMMENT '版本ID（雪花算法生成）',
  `create_by` bigint DEFAULT NULL COMMENT '创建者用户ID',
  `create_time` datetime(6) DEFAULT NULL COMMENT '创建时间',
  `description` varchar(500) DEFAULT NULL COMMENT '版本说明（描述本次变更内容）',
  `flag` int DEFAULT NULL COMMENT '状态标志（0:禁用，1:启用）',
  `route_group` varchar(64) NOT NULL COMMENT '路由分组（如 common、admin、develop）',
  `version_no` int NOT NULL COMMENT '组内版本序号（递增，如 v1、v2...）',
  PRIMARY KEY (`id`),
  KEY `idx_group` (`route_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='视图路由版本表';

-- ----------------------------
-- Table structure for sys_condition_rule
-- ----------------------------
DROP TABLE IF EXISTS `sys_condition_rule`;
CREATE TABLE `sys_condition_rule` (
  `id` bigint NOT NULL COMMENT '主键',
  `rule_code` varchar(64) NOT NULL COMMENT '规则编码，全局唯一',
  `rule_name` varchar(128) NOT NULL COMMENT '规则名称',
  `description` varchar(500) DEFAULT NULL COMMENT '规则说明',
  `getter_id` varchar(128) NOT NULL COMMENT '顶层取值器 ID',
  `invocation_json` text NOT NULL COMMENT '运行时调用载荷（ParamValue 列表 JSON）',
  `editor_json` text COMMENT '前端编辑器状态快照 JSON',
  `enabled` int NOT NULL DEFAULT 1 COMMENT '是否启用：1 启用，0 停用',
  `flag` int DEFAULT NULL COMMENT '逻辑删除标志：1 有效，0 删除',
  `create_time` datetime(6) DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) DEFAULT NULL COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  PRIMARY KEY (`id`),
  KEY `idx_condition_rule_code` (`rule_code`),
  KEY `idx_condition_rule_enabled` (`enabled`),
  KEY `idx_condition_rule_flag` (`flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='条件规则表';

-- ----------------------------
-- Table structure for sys_event_subscription
-- ----------------------------
DROP TABLE IF EXISTS `sys_event_subscription`;
CREATE TABLE `sys_event_subscription` (
  `id` bigint NOT NULL COMMENT '主键',
  `reporter_id` varchar(128) NOT NULL COMMENT '事件上报器 ID',
  `event_code` varchar(128) NOT NULL COMMENT '事件编码',
  `trigger_id` varchar(128) NOT NULL COMMENT '触发器 ID',
  `rule_code` varchar(64) DEFAULT NULL COMMENT '条件规则编码，为空表示不做条件判定',
  `sort_order` int DEFAULT NULL COMMENT '执行顺序，升序',
  `enabled` int NOT NULL DEFAULT 1 COMMENT '是否启用：1 启用，0 停用',
  `flag` int DEFAULT NULL COMMENT '逻辑删除标志：1 有效，0 删除',
  `create_time` datetime(6) DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) DEFAULT NULL COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  PRIMARY KEY (`id`),
  KEY `idx_event_subscription_event` (`reporter_id`,`event_code`,`sort_order`),
  KEY `idx_event_subscription_enabled` (`enabled`),
  KEY `idx_event_subscription_flag` (`flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='事件订阅表';

-- ----------------------------
-- Table structure for sys_event_action
-- ----------------------------
DROP TABLE IF EXISTS `sys_event_action`;
CREATE TABLE `sys_event_action` (
  `id` bigint NOT NULL COMMENT '主键',
  `reporter_id` varchar(128) NOT NULL COMMENT '事件上报器 ID',
  `event_code` varchar(128) NOT NULL COMMENT '事件编码',
  `action_id` varchar(128) NOT NULL COMMENT '动作 ID',
  `action_name` varchar(128) DEFAULT NULL COMMENT '动作名称展示快照',
  `param_json` text COMMENT '运行时动作参数（ParamValue 列表 JSON）',
  `sort_order` int DEFAULT NULL COMMENT '执行顺序，升序',
  `enabled` int NOT NULL DEFAULT 1 COMMENT '是否启用：1 启用，0 停用',
  `flag` int DEFAULT NULL COMMENT '逻辑删除标志：1 有效，0 删除',
  `create_time` datetime(6) DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime(6) DEFAULT NULL COMMENT '更新时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建者',
  `update_by` bigint DEFAULT NULL COMMENT '更新者',
  PRIMARY KEY (`id`),
  KEY `idx_event_action_event` (`reporter_id`,`event_code`,`sort_order`),
  KEY `idx_event_action_enabled` (`enabled`),
  KEY `idx_event_action_flag` (`flag`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='事件动作绑定表';

SET FOREIGN_KEY_CHECKS = 1;

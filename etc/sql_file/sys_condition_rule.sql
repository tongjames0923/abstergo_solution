-- ----------------------------
-- 条件规则表：持久化值获取器编排出的判断逻辑
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
-- 条件规则 API 权限（type=3）与开发者组绑定
-- ----------------------------
INSERT INTO `sys_role` (`id`, `create_time`, `description`, `flag`, `role`, `update_time`, `create_by`, `update_by`, `data_scope`, `name`, `type`) VALUES (751900000000000001, NOW(), '', 1, 'API.DEV.CONDITION.RULE.QUERY', NULL, 1, NULL, NULL, '条件规则查询权限', 3);
INSERT INTO `sys_role` (`id`, `create_time`, `description`, `flag`, `role`, `update_time`, `create_by`, `update_by`, `data_scope`, `name`, `type`) VALUES (751900000000000002, NOW(), '', 1, 'API.DEV.CONDITION.RULE.SAVE', NULL, 1, NULL, NULL, '条件规则保存权限', 3);
INSERT INTO `sys_role` (`id`, `create_time`, `description`, `flag`, `role`, `update_time`, `create_by`, `update_by`, `data_scope`, `name`, `type`) VALUES (751900000000000003, NOW(), '', 1, 'API.DEV.CONDITION.RULE.DELETE', NULL, 1, NULL, NULL, '条件规则删除权限', 3);

INSERT INTO `sys_role_group_relation` (`id`, `create_by`, `create_time`, `delete_by`, `delete_time`, `group_id`, `role_id`, `update_by`, `update_time`, `flag`) VALUES (751900000000000011, NULL, NOW(), NULL, NULL, 740721847200382976, 751900000000000001, NULL, NULL, 1);
INSERT INTO `sys_role_group_relation` (`id`, `create_by`, `create_time`, `delete_by`, `delete_time`, `group_id`, `role_id`, `update_by`, `update_time`, `flag`) VALUES (751900000000000012, NULL, NOW(), NULL, NULL, 740721847200382976, 751900000000000002, NULL, NULL, 1);
INSERT INTO `sys_role_group_relation` (`id`, `create_by`, `create_time`, `delete_by`, `delete_time`, `group_id`, `role_id`, `update_by`, `update_time`, `flag`) VALUES (751900000000000013, NULL, NOW(), NULL, NULL, 740721847200382976, 751900000000000003, NULL, NULL, 1);

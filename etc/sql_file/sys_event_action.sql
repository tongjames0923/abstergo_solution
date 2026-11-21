-- ----------------------------
-- 事件动作绑定表：持久化「事件命中条件后执行哪些动作」
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

-- ----------------------------
-- 事件动作 API 权限（type=3）与开发者组绑定
-- ----------------------------
INSERT INTO `sys_role` (`id`, `create_time`, `description`, `flag`, `role`, `update_time`, `create_by`, `update_by`, `data_scope`, `name`, `type`) VALUES (751920000000000001, NOW(), '', 1, 'API.DEV.EVENT.ACTION.QUERY', NULL, 1, NULL, NULL, '事件动作查询权限', 3);
INSERT INTO `sys_role` (`id`, `create_time`, `description`, `flag`, `role`, `update_time`, `create_by`, `update_by`, `data_scope`, `name`, `type`) VALUES (751920000000000002, NOW(), '', 1, 'API.DEV.EVENT.ACTION.SAVE', NULL, 1, NULL, NULL, '事件动作保存权限', 3);
INSERT INTO `sys_role` (`id`, `create_time`, `description`, `flag`, `role`, `update_time`, `create_by`, `update_by`, `data_scope`, `name`, `type`) VALUES (751920000000000003, NOW(), '', 1, 'API.DEV.EVENT.ACTION.DELETE', NULL, 1, NULL, NULL, '事件动作删除权限', 3);

INSERT INTO `sys_role_group_relation` (`id`, `create_by`, `create_time`, `delete_by`, `delete_time`, `group_id`, `role_id`, `update_by`, `update_time`, `flag`) VALUES (751920000000000011, NULL, NOW(), NULL, NULL, 740721847200382976, 751920000000000001, NULL, NULL, 1);
INSERT INTO `sys_role_group_relation` (`id`, `create_by`, `create_time`, `delete_by`, `delete_time`, `group_id`, `role_id`, `update_by`, `update_time`, `flag`) VALUES (751920000000000012, NULL, NOW(), NULL, NULL, 740721847200382976, 751920000000000002, NULL, NULL, 1);
INSERT INTO `sys_role_group_relation` (`id`, `create_by`, `create_time`, `delete_by`, `delete_time`, `group_id`, `role_id`, `update_by`, `update_time`, `flag`) VALUES (751920000000000013, NULL, NOW(), NULL, NULL, 740721847200382976, 751920000000000003, NULL, NULL, 1);

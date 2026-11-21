-- ----------------------------
-- 事件订阅表：持久化「事件 → 触发器 → 条件规则」的绑定关系
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
-- 事件订阅 API 权限（type=3）与开发者组绑定
-- ----------------------------
INSERT INTO `sys_role` (`id`, `create_time`, `description`, `flag`, `role`, `update_time`, `create_by`, `update_by`, `data_scope`, `name`, `type`) VALUES (751910000000000001, NOW(), '', 1, 'API.DEV.EVENT.SUBSCRIPTION.QUERY', NULL, 1, NULL, NULL, '事件订阅查询权限', 3);
INSERT INTO `sys_role` (`id`, `create_time`, `description`, `flag`, `role`, `update_time`, `create_by`, `update_by`, `data_scope`, `name`, `type`) VALUES (751910000000000002, NOW(), '', 1, 'API.DEV.EVENT.SUBSCRIPTION.SAVE', NULL, 1, NULL, NULL, '事件订阅保存权限', 3);
INSERT INTO `sys_role` (`id`, `create_time`, `description`, `flag`, `role`, `update_time`, `create_by`, `update_by`, `data_scope`, `name`, `type`) VALUES (751910000000000003, NOW(), '', 1, 'API.DEV.EVENT.SUBSCRIPTION.DELETE', NULL, 1, NULL, NULL, '事件订阅删除权限', 3);

INSERT INTO `sys_role_group_relation` (`id`, `create_by`, `create_time`, `delete_by`, `delete_time`, `group_id`, `role_id`, `update_by`, `update_time`, `flag`) VALUES (751910000000000011, NULL, NOW(), NULL, NULL, 740721847200382976, 751910000000000001, NULL, NULL, 1);
INSERT INTO `sys_role_group_relation` (`id`, `create_by`, `create_time`, `delete_by`, `delete_time`, `group_id`, `role_id`, `update_by`, `update_time`, `flag`) VALUES (751910000000000012, NULL, NOW(), NULL, NULL, 740721847200382976, 751910000000000002, NULL, NULL, 1);
INSERT INTO `sys_role_group_relation` (`id`, `create_by`, `create_time`, `delete_by`, `delete_time`, `group_id`, `role_id`, `update_by`, `update_time`, `flag`) VALUES (751910000000000013, NULL, NOW(), NULL, NULL, 740721847200382976, 751910000000000003, NULL, NULL, 1);

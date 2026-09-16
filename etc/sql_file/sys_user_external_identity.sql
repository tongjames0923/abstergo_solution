-- ----------------------------
-- 外部身份绑定表：承载第三方身份（如企业微信 userid）与系统用户的映射
-- 供统一登录授权（POST /public/user/login/{grantType}）按外部身份解析系统用户
-- （identity_type, external_id）全局唯一；绑定维护（预绑定/自助绑定）由管理端另行提供
-- ----------------------------
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS `sys_user_external_identity` (
  `id` bigint NOT NULL,
  `identity_type` varchar(32) NOT NULL,
  `external_id` varchar(128) NOT NULL,
  `user_id` bigint NOT NULL,
  `create_by` bigint DEFAULT NULL,
  `create_time` datetime(6) DEFAULT NULL,
  `update_by` bigint DEFAULT NULL,
  `update_time` datetime(6) DEFAULT NULL,
  `flag` int DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_identity_type_external_id` (`identity_type`,`external_id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

SET FOREIGN_KEY_CHECKS = 1;

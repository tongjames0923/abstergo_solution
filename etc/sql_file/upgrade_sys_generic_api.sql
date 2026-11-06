-- ----------------------------------------------------------------------------
-- 泛化API配置增量脚本（新版泛化调用链路）
--
-- 动机：新版链路要求「仅配置即可把 Dubbo 方法声明成 HTTP 接口」，网关不持有目标接口 jar，
--       因此参数的顺序/名称/类型必须由库登记（元信息唯一真源）。
-- 影响：
--   1) sys_generic_api 新增 business 列（Dubbo group），存量数据保持 NULL：新链路下 NULL 只命中未声明 group 的提供者，
--      目标服务带 group 时必须补登 business（不限制则填 '*'），旧 /invoke 入口不受本列影响。
--   2) 新增 sys_generic_api_param 表，存量 API 无参数登记时：无参方法照常调用，
--      有参方法继续用旧的信封式 /invoke（新版 /call 会提示先登记入参定义）。
-- 索引：按「api_id 取整组参数」这一条查询建 idx_generic_api_param_api_id；状态列保留 idx_generic_api_param_flag。
--       不建唯一索引——入参顺序判重是业务规则，由 GenericApiParamServiceImpl 断言保证（总纲铁律 14/15）。
-- 与实体 SystemGenericApiEntity / SystemGenericApiParamEntity 的 @Table/@Index 声明一一对应。
-- ----------------------------------------------------------------------------

ALTER TABLE `sys_generic_api`
    ADD COLUMN `business` varchar(100) DEFAULT NULL COMMENT '业务分区（Dubbo group），留空只匹配无group的提供者，*表示不限' AFTER `app_name`;

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

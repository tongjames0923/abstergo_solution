-- ============================================================================
-- 泛化调用（新版链路）演示配置：logging-service 里的 IStandaloneDemoService
--
-- 演示对象的两个关键事实：
--   1) 接口与实现都只在 logging-impl 模块，网关（app）的 classpath 上看不到 com.tbs.logging.demo.IStandaloneDemoService
--      —— 这正是「零 jar、仅配置」要验证的场景；
--   2) 服务以 group=demo 发布，所以每一行的 business 都必须填 demo（实测证实：business 留空时调不通）。
--      Dubbo 的 group 匹配规则（UrlUtils.shouldMatch）：未声明 group 的消费者只匹配未声明 group 的提供者，
--      要不限分区必须显式登记 '*'；而 version 列是业务配置版本，不映射 Dubbo 服务版本。
--
-- 使用顺序：
--   1) 先执行 upgrade_sys_generic_api.sql（加 business 列 + 建 sys_generic_api_param 表）；
--   2) 启动 logging-service（发布 demo 组服务）与网关；
--   3) 执行本脚本登记；
--   4) 按下面的样例调用（need_auth=0 的行可走 /public 免认证，便于 curl）。
--
-- 说明：flag=1 启用；create_by=1 借用超级管理员账号ID，仅为便于列表页可见，不参与鉴权判定。
--       business 全部填 demo：目标服务带 group 发布，留空会因 group 不匹配而找不到提供者。
-- ============================================================================

-- ---------- 主表：API 声明 ----------
INSERT INTO `sys_generic_api` (`id`, `api_id`, `api_name`, `app_name`, `business`, `interface_name`, `method_name`,
    `need_auth`, `version`, `pre_auth_beans`, `description`, `flag`, `create_by`, `create_time`)
VALUES (820000000000000001, 'DEMO.STANDALONE.PING', '演示-无参应答', 'dubbo-logging-view-service', 'demo',
    'com.tbs.logging.demo.IStandaloneDemoService', 'ping', b'0', NULL, '', '无入参，验证不登记参数也能调用', 1, 1, now()),
    (820000000000000002, 'DEMO.STANDALONE.ECHO_ONE', '演示-回显单参', 'dubbo-logging-view-service', 'demo',
    'com.tbs.logging.demo.IStandaloneDemoService', 'echo', b'0', NULL, '', '同名重载之一：1个参数', 1, 1, now()),
    (820000000000000003, 'DEMO.STANDALONE.ECHO_TIMES', '演示-回显重复', 'dubbo-logging-view-service', 'demo',
    'com.tbs.logging.demo.IStandaloneDemoService', 'echo', b'0', NULL, '', '同名重载之二：2个参数，验证按参数类型定位重载', 1, 1, now()),
    (820000000000000004, 'DEMO.STANDALONE.SUM', '演示-基本类型求和', 'dubbo-logging-view-service', 'demo',
    'com.tbs.logging.demo.IStandaloneDemoService', 'sum', b'0', NULL, '', 'long/int 基本类型，参数类型必须登记为 long', 1, 1, now()),
    (820000000000000005, 'DEMO.STANDALONE.TRANSFORM', '演示-POJO往返', 'dubbo-logging-view-service', 'demo',
    'com.tbs.logging.demo.IStandaloneDemoService', 'transform', b'0', NULL, '', 'POJO 以 Map 传入，服务端还原成 DTO', 1, 1, now()),
    (820000000000000006, 'DEMO.STANDALONE.FILTER', '演示-泛型集合过滤', 'dubbo-logging-view-service', 'demo',
    'com.tbs.logging.demo.IStandaloneDemoService', 'filter', b'0', NULL, '', 'List<DTO> 参数，登记为擦除类型 java.util.List', 1, 1, now()),
    (820000000000000007, 'DEMO.STANDALONE.STATUS', '演示-枚举入参', 'dubbo-logging-view-service', 'demo',
    'com.tbs.logging.demo.IStandaloneDemoService', 'status', b'0', NULL, '', '枚举参数，请求传枚举名字符串', 1, 1, now()),
    (820000000000000008, 'DEMO.STANDALONE.ROUND', '演示-高精度金额', 'dubbo-logging-view-service', 'demo',
    'com.tbs.logging.demo.IStandaloneDemoService', 'round', b'0', NULL, '', 'BigDecimal + Integer 混合', 1, 1, now()),
    (820000000000000009, 'DEMO.STANDALONE.ECHO_AUTH', '演示-需认证回显', 'dubbo-logging-view-service', 'demo',
    'com.tbs.logging.demo.IStandaloneDemoService', 'echo', b'1', NULL, 'AccountActived',
    '需登录且走账号激活预认证，只可经 /private 入口调用', 1, 1, now());

-- ---------- 子表：入参定义（顺序即签名顺序，类型写擦除后的 JVM 名或基本类型关键字） ----------
INSERT INTO `sys_generic_api_param` (`id`, `api_id`, `param_order`, `param_name`, `param_type`, `required_flag`,
    `description`, `example`, `flag`, `create_by`, `create_time`)
VALUES (821000000000000001, 'DEMO.STANDALONE.ECHO_ONE', 0, 'message', 'java.lang.String', b'1', '待回显文本', '"hello"', 1, 1, now()),
    (821000000000000011, 'DEMO.STANDALONE.ECHO_TIMES', 0, 'message', 'java.lang.String', b'1', '待回显文本', '"hello"', 1, 1, now()),
    (821000000000000012, 'DEMO.STANDALONE.ECHO_TIMES', 1, 'times', 'java.lang.Integer', b'0', '重复次数，空按1次', '3', 1, 1, now()),
    (821000000000000021, 'DEMO.STANDALONE.SUM', 0, 'a', 'long', b'1', '加数', '1', 1, 1, now()),
    (821000000000000022, 'DEMO.STANDALONE.SUM', 1, 'b', 'long', b'1', '被加数', '2', 1, 1, now()),
    (821000000000000031, 'DEMO.STANDALONE.TRANSFORM', 0, 'payload', 'com.tbs.logging.demo.StandaloneDemoVo', b'0',
    '演示载荷', '{"code":"C1","name":"item","qty":2,"amount":"1.5","tags":["A","B"]}', 1, 1, now()),
    (821000000000000041, 'DEMO.STANDALONE.FILTER', 0, 'payloads', 'java.util.List', b'1', '载荷列表',
    '[{"code":"C1","tags":["A"]},{"code":"C2","tags":["B"]}]', 1, 1, now()),
    (821000000000000042, 'DEMO.STANDALONE.FILTER', 1, 'tag', 'java.lang.String', b'0', '过滤标签，空则不过滤', '"A"', 1, 1, now()),
    (821000000000000051, 'DEMO.STANDALONE.STATUS', 0, 'status', 'com.tbs.logging.demo.StandaloneDemoStatus', b'0',
    '状态枚举名', '"RUNNING"', 1, 1, now()),
    (821000000000000061, 'DEMO.STANDALONE.ROUND', 0, 'amount', 'java.math.BigDecimal', b'0', '金额', '"3.14159"', 1, 1, now()),
    (821000000000000062, 'DEMO.STANDALONE.ROUND', 1, 'scale', 'java.lang.Integer', b'0', '小数位数，空按2位', '4', 1, 1, now()),
    (821000000000000071, 'DEMO.STANDALONE.ECHO_AUTH', 0, 'message', 'java.lang.String', b'1', '待回显文本', '"hello"', 1, 1, now()),
    (821000000000000072, 'DEMO.STANDALONE.ECHO_AUTH', 1, 'times', 'java.lang.Integer', b'0', '重复次数，空按1次', '2', 1, 1, now());

-- ============================================================================
-- 试调用样例（网关默认 8083；免认证行直接走 /public，需登录的行走 /private 并带 token）
--
-- 1) 无参（无入参登记时建议直接传空对象，避开部分客户端不发送 Content-Type 时的 415）：
--    curl -X POST http://localhost:8083/public/genric/call/DEMO.STANDALONE.PING \
--         -H 'Content-Type: application/json' -d '{}'
--
-- 2) 命名式（推荐：可读、与参数顺序无关）：
--    curl -X POST http://localhost:8083/public/genric/call/DEMO.STANDALONE.SUM \
--         -H 'Content-Type: application/json' -d '{"a":1,"b":2}'
--
-- 3) 位置式（不起参数名也能试）：
--    curl -X POST http://localhost:8083/public/genric/call/DEMO.STANDALONE.SUM \
--         -H 'Content-Type: application/json' -d '[1,2]'
--
-- 4) POJO 参数（对象值直接给 JSON）：
--    curl -X POST http://localhost:8083/public/genric/call/DEMO.STANDALONE.TRANSFORM \
--         -H 'Content-Type: application/json' \
--         -d '{"payload":{"code":"C1","name":"item","qty":2,"amount":"1.5","tags":["A","B"]}}'
--
-- 5) 泛型集合参数（擦除类型登记，元素类型由服务端还原）：
--    curl -X POST http://localhost:8083/public/genric/call/DEMO.STANDALONE.FILTER \
--         -H 'Content-Type: application/json' \
--         -d '{"payloads":[{"code":"C1","tags":["A"]},{"code":"C2","tags":["B"]}],"tag":"A"}'
--
-- 6) 枚举参数：
--    curl -X POST http://localhost:8083/public/genric/call/DEMO.STANDALONE.STATUS \
--         -H 'Content-Type: application/json' -d '{"status":"RUNNING"}'
--
-- 7) 元数据（前端自动表单的依据）：
--    curl http://localhost:8083/public/genric/meta/DEMO.STANDALONE.FILTER
--
-- 8) 需认证的登记行只能走 /private（带登录 token）：
--    curl -X POST http://localhost:8083/private/genric/call/DEMO.STANDALONE.ECHO_AUTH \
--         -H 'Authorization: <token>' -H 'Content-Type: application/json' -d '{"message":"hi","times":2}'
--
-- 9) 同名重载分别命中（ECHO_ONE 一个参数、ECHO_TIMES 两个参数）：
--    curl -X POST http://localhost:8083/public/genric/call/DEMO.STANDALONE.ECHO_ONE \
--         -H 'Content-Type: application/json' -d '{"message":"hi"}'
--
-- 10) 旧的信封式入口仍可用（本脚本不影响它）：
--    curl -X POST http://localhost:8083/public/genric/invoke -H 'Content-Type: application/json' \
--         -d '{"id":"DEMO.STANDALONE.SUM","parameters":[{"parameterName":"a","exceptdClassName":"long","parameterValue":1},{"parameterName":"b","exceptdClassName":"long","parameterValue":2}]}'
-- ============================================================================

# cdc-service

CDC MQ发出服务：独立可运行的 Spring Boot 服务，基于 cdc-mod 捕获 SQL Server 数据库变更（Debezium Embedded），并将 `CdcEvent` 按 可配置的交换机与路由键模板 发出到 RabbitMQ，供下游服务（如 app）消费。

## 架构

```
SQL Server ──Debezium Embedded──> cdc-sqlserver-impl 捕获
          ──> CdcEventBus 进程内总线
          ──> CdcMqPublisher（本服务胶水：模板渲染路由键 + 组装 v2 信封）
          ──> IMqMessageProducer（v2 统一发送契约，RabbitMqMessageProducer 落地） ──> RabbitMQ 交换机 ──> 下游消费者
```

- 职责边界：只做 **捕获 + MQ 发出**；进程内监听器能力保留（cdc-mod 原生支持），业务消费由下游 MQ 消费者对接
- 投递语义：至少一次；`CdcEvent.eventId` 作为 v2 信封 `messageId`（消费端接收段以之作**建档去重**的记录主键），`businessId` 固定为 `cdc`；负载由 v2 默认 `JdkMqPayloadConverter` 序列化（非 JSON，JSON 仅用于发送日志）
- 可靠性：发送失败仅记录日志（含 eventId/table），不影响捕获链路与其他监听器；`auto-declare` 启动时自动声明交换机/队列/绑定，避免发送侧 404

## 启用方式

独立打包部署（与 app 一致的装配风格）：

```bash
mvn -pl cdc-service -am package
java -jar cdc-service/target/cdc-service-1.0-SNAPSHOT.jar
```

启动类 `CdcServiceApplication` 装配：`@EnableTbsCdc` + `@EnableCdcSqlserver` + `@EnableRabbitMq`。

## 配置项

### tbs.cdc.mq（MQ 发出，本服务新增）

| 配置项 | 默认值 | 说明 |
|--------|--------|------|
| `tbs.cdc.mq.enable` | `true` | 总开关；false 时不装配 MQ 发出监听器，事件仅进程内分发 |
| `tbs.cdc.mq.exchange` | `tbs.cdc` | 目标交换机名称 |
| `tbs.cdc.mq.exchange-type` | `topic` | 交换机类型：direct / topic / fanout / headers |
| `tbs.cdc.mq.durable` | `true` | 交换机与自动声明队列是否持久化 |
| `tbs.cdc.mq.routing-pattern` | `cdc.{database}.{table}.{changeType}` | 路由键模板，占位符按事件渲染 |
| `tbs.cdc.mq.auto-declare` | `true` | 启动时自动声明交换机 |
| `tbs.cdc.mq.bindings` | `[]` | 可选队列绑定列表（queue + routing-key），随启动声明并绑定 |

### tbs.cdc（捕获配置，与 app 迁移前一致）

`payload-mode` / `sqlserver.*` / `filter.*` / `batch.*` / `error.*` / `dispatch.in-process-mode`，含义见 `application-tbs.yml` 注释与 `cdc-mod/README.md`。

### 连接配置

- RabbitMQ：`spring.rabbitmq.*`（application.yml 本地默认 + Nacos `RABBITMQ.yml` 覆盖）
- Nacos / Dubbo：`NACOS_ADDRESS` 环境变量，与 app 一致

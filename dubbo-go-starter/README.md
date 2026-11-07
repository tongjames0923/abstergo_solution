# dubbo-go-starter

轻量 Dubbo(Go) 提供者快速开发套件：**一份 JSON 契约配置 + 业务实现结构体 + 一次 `starter.Run` 调用**，即完成 POJO 注册、服务装配、Nacos 注册与 ServiceDefinition 元数据发布，编译产出单二进制可直接运行。协议固定 `dubbo(TCP) + hessian2`、无 IDL 注册，供 **Java 泛化调用（`GenericService.$invoke`）与 Dubbo Admin** 直接消费——替代「为几个简单方法而接入 Spring Boot 工程」的重型方案。

完整示例见仓库 `dubbo-db-demo/`（基于本库的 `go-user-provider`，端口 20001）。

## 一、新服务三步

1. **写业务实现**（导出结构体 + 指针传入）：

```go
type OrderImpl struct{}

// dubbo-go 服务分发的定位依据，必须返回接口全限定名
func (o *OrderImpl) Reference() string { return "com.example.OrderProvider" }

// 方法名与配置声明一致、真实签名（首参可带 context.Context，末位返回 error）
func (o *OrderImpl) GetById(ctx context.Context, orderId int64) (*Order, error) { ... }

// 传输对象实现 hessian POJO 契约，启动时反射扫描自动注册，无需手工 init
type Order struct {
    Id     int64
    Amount float64
}

func (Order) JavaClassName() string { return "com.example.Order" }
```

2. **写契约配置** `service.json`（接口/方法/参数/返回类型，参见 `service.example.json`）：

```json
{
  "application": "go-order-provider",
  "port": 20002,
  "services": [
    {
      "interface": "com.example.OrderProvider",
      "version": "",
      "group": "",
      "methods": [
        {
          "name": "GetById",
          "parameterTypes": ["long"],
          "returnType": "com.example.Order",
          "parameters": [{ "index": 0, "name": "orderId", "type": "long" }]
        }
      ]
    }
  ]
}
```

3. **main 里绑定并启动**：

```go
if err := starter.Run("service.json",
    starter.Binding{Interface: "com.example.OrderProvider", Impl: &OrderImpl{}},
); err != nil {
    log.Fatalf("服务启动失败: %v", err)
}
```

`go.mod` 中以本地方式引用（或发布到私有仓库后直接 require）：

```
require dubbo-go-starter v0.0.0
replace dubbo-go-starter => ../dubbo-go-starter
```

编译运行（Windows PowerShell）：

```powershell
go build -o my-provider.exe .
.\my-provider.exe
```

## 二、配置字段表

| 字段 | 类型 | 必填 | 默认 | 说明 |
|------|------|------|------|------|
| `application` | string | 是 | - | 应用名（Nacos 服务名 / 元数据 dataId 组成部分） |
| `port` | int | 否 | `20001` | dubbo(TCP) 监听端口；可被 `DUBBO_PORT` 环境变量覆盖 |
| `nacosAddress` | string | 否 | `127.0.0.1:8848` | Nacos 注册与元数据地址；可被 `NACOS_ADDRESS` 环境变量覆盖 |
| `metadataGroup` | string | 否 | `dubbo` | ServiceDefinition 在 Nacos 配置中心的 group |
| `services[]` | array | 是 | - | 服务契约，接口不可重复 |
| `services[].interface` | string | 是 | - | 接口全限定名（Java 侧坐标） |
| `services[].version` | string | 否 | `""` | Dubbo 服务版本 |
| `services[].group` | string | 否 | `""` | Dubbo 分组：**Java 泛化引用按 `interface#group` 匹配，空只匹配未声明 group 的提供者**（`*` 才是不限），与登记侧坐标必须一致 |
| `services[].methods[]` | array | 是 | - | 方法签名，方法名不可重复 |
| `methods[].name` | string | 是 | - | 方法名，必须与 Go 实现上的导出方法同名 |
| `methods[].parameterTypes` | []string | 是 | `[]` | 擦除后的 JVM 参数类型串 |
| `methods[].returnType` | string | 是 | - | 擦除后的 JVM 返回类型串，无返回值写 `void` |
| `methods[].parameters[]` | array | 否 | 自动补齐 `argN` | 参数定义（index/name/type），Dubbo Admin 测试调用表单的渲染依据；提供时须与 `parameterTypes` 一一对齐 |
| `parameters[].index` | int | 是 | - | 签名顺序，从 0 起连续 |
| `parameters[].name` | string | 是 | - | 参数名，供命名式调用 |
| `parameters[].type` | string | 是 | - | 与 `parameterTypes[index]` 一致 |

**类型串口径**（与 Java 泛化调用同源，配置校验会拒绝带 `<...>` 的泛型串）：

- 基本类型写关键字：`long` / `int` / `boolean` / `double` 等；
- 对象与集合写擦除 FQCN：`java.lang.String`、`java.util.List`、`java.math.BigDecimal`；
- 自定义 POJO 写其 `JavaClassName()` 返回值（如 `com.example.Order`），两边必须一致，否则 Java 侧无法 realize；
- Go 侧 `string`→`java.lang.String`、`int32`→`int`、`int64`→`long`、`bool`→`boolean`、`float64`→`double`、`[]T`/`map`→`java.util.List`/`java.util.Map`。

## 三、环境变量

| 变量 | 默认 | 说明 |
|------|------|------|
| `NACOS_ADDRESS` | `127.0.0.1:8848` | Nacos 地址（与仓库 Java 服务口径一致） |
| `DUBBO_PORT` | 配置文件 `port` | 覆盖监听端口 |

启动后 `Ctrl+C` 退出（Windows 支持 `os.Interrupt`）；进程直接退出，Nacos 实例随心跳超时摘除。

## 四、Dubbo Admin 查询与测试调用验证

前提：`etc/docker/docker-compose.yml` 启动的 Nacos(8848) 与 Dubbo Admin(9600)。

1. 启动 Go 服务（如 demo）：`go build -o dubbo-db-demo.exe . && .\dubbo-db-demo.exe`；
2. **Nacos 控制台**（`http://127.0.0.1:8848/nacos`）→ 服务列表：出现 `go-user-provider`；
3. **Dubbo Admin**（`http://127.0.0.1:9600`）→ 服务查询：可见 `org.apache.dubbo.samples.api.UserProvider`，方法签名 `Echo(java.lang.String)` / `GetUser(java.lang.String)` 来自启动时发布的 ServiceDefinition；
4. **Admin 测试调用**：选 `GetUser`，参数 `zhangsan`（`parameters` 里的 `name`/`type` 即表单渲染依据）→ 返回 User 数据，Go 控制台打印 `[GetUser] name="zhangsan"`。

Admin 测试调用与 Java 泛化调用共用 `GenericService.$invoke` 泛化机制：`$invoke("GetUser", ["java.lang.String"], ["zhangsan"])`。

## 五、与 app 泛化调用的兼容口径

- Go 服务按标准 Dubbo 语义发布，即满足 app 泛化链路「零 jar、仅配置」标准——**无需 Java 接口工程、不改任何 Java 代码**；Java 侧如何引用/登记由系统既有机制决定（`business` = Dubbo `group`，留空只匹配未声明 group 的提供者，`*` 才是不限）；
- 响应为远端方法返回值本体；Go struct 在 Java 泛化侧呈现为 Map 结构，POJO 参数以 Map 传入、由服务端按真实方法签名 realize。

## 六、常见错误

| 报错（启动即失败） | 原因与处理 |
|---|---|
| `未找到导出方法` | 配置声明的 `name` 与实现上的导出方法名不一致（方法需导出且同名） |
| `实现必须以指针传入` | `Binding.Impl` 应传 `&FooImpl{}` 而非值 |
| `必须提供 Reference() string` | 实现 lacks dubbo-go 的服务定位方法，补上并返回接口全限定名 |
| `Reference() 返回 "..." 与声明的接口名不一致` | Reference 返回值必须等于配置的 `interface` |
| `声明为 void，但实现返回了 1 个结果值` | `returnType` 与实现返回形态不一致，二选一对齐 |
| `与实现不一致`（参数个数） | `parameterTypes` 数量应等于实现入参（可含 1 个 `context.Context`） |
| `含泛型标记` | 类型串写擦除形式：`java.util.List`，不要写 `java.util.List<com.x.Foo>` |
| `警告: 类型 ... 未匹配到已注册 POJO` | 类型串未命中任何 `JavaClassName()`：确认拼写，或该类型确以 Map 形态传输（警告不阻断） |
| 泛化调用 string 无法 realize | 方法签名写成了 `[]interface{}`——必须用真实参数类型 |

## 七、实现说明（维护者向）

- 目录：`config.go`（模型/加载/校验）、`registry.go`（绑定校验 + POJO 反射注册去重）、`metadata.go`（ServiceDefinition + Nacos 发布）、`server.go`（实例/服务端装配）、`starter.go`（Run 门面/信号监听）；
- 元数据 dataId 规则：`interface:version:group:provider:application`，发布到 Nacos group `dubbo`（与原 dubbo-db-demo 实现逐字一致）；
- 启动期顺序（硬约束）：`hessian.RegisterPOJO` 必须先于 `RegisterService` 与 `Serve`；
- 仅发布 `dubbo://`：`WithMetadataServiceProtocol("dubbo")` 使 MetadataService 复用同一端口，注册 URL 不出现 `tri://`，Java 消费端不会误选 Triple；
- 单测 `metadata_test.go` 为纯逻辑测试（配置校验、dataId、元数据拼装、绑定校验、POJO 注册去重），不依赖 Nacos/网络：`go test ./...`。

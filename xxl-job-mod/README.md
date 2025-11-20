# XXL-Job-Mod 分布式任务调度模块

🚀 **基于XXL-Job的分布式任务调度集成解决方案**

## 📋 核心特性

### 🚀 开箱即用
- **零配置启动**：基于Spring Boot自动配置，条件化Bean加载
- **灵活启用**：通过配置属性控制XXL-Job功能启用
- **标准化接口**：提供统一的配置接口和任务处理器接口

### 🔧 智能配置
- **条件化配置**：基于配置属性的条件化Bean创建
- **配置验证**：自动验证关键配置项的完整性
- **日志输出**：详细的启动日志和配置信息输出

### 📊 JSON任务支持
- **通用任务接口**：基于JSON的通用任务执行器
- **动态任务发现**：自动扫描和注册JSON任务处理器
- **任务帮助系统**：内置任务列表查询和帮助信息

### 🔒 企业级特性
- **安全认证**：支持访问令牌认证
- **日志管理**：可配置的日志路径和保留天数
- **网络配置**：灵活的执行器网络配置

## 🚀 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.tbs</groupId>
    <artifactId>xxl-job</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

### 2. 基础配置

#### 启用XXL-Job功能

```yaml
# application.yml
tbs:
  xxl-job:
    enable: true  # 启用XXL-Job功能
    enable-json-job-executor: true  # 启用JSON任务执行器
```

#### 配置XXL-Job执行器

```java
@Configuration
public class XxlJobConfiguration {
    
    @Bean
    public IXXLJobsConfig xxlJobsConfig() {
        return new IXXLJobsConfig() {
            @Override
            public String adminAddress() {
                return "http://127.0.0.1:9898/xxl-job-admin";
            }
            
            @Override
            public String appName() {
                return "your-application-name";
            }
            
            @Override
            public String accessToken() {
                return "your-access-token";
            }
            
            // 其他配置方法使用默认值
            @Override
            public String address() { return ""; }
            
            @Override
            public String ip() { return ""; }
            
            @Override
            public Integer port() { return null; }
            
            @Override
            public String logPath() { return ""; }
            
            @Override
            public Integer logRetentionsDays() { return 30; }
        };
    }
}
```

### 3. 基本使用示例

#### 创建自定义任务处理器

```java
@Component
public class SampleJobHandler implements IJsonJobHandler<SampleParams> {
    
    @Override
    public Class<SampleParams> classType() {
        return SampleParams.class;
    }
    
    @Override
    public String help() {
        return "示例任务处理器，用于演示JSON任务功能";
    }
    
    @Override
    public String handle(SampleParams params) throws Exception {
        // 业务逻辑处理
        log.info("处理任务，参数: {}", params);
        
        // 返回处理结果
        return "任务执行成功，处理数据量: " + params.getData().size();
    }
}

@Data
public class SampleParams {
    private String taskName;
    private List<String> data;
    private int batchSize;
}
```

#### 在XXL-Job管理平台配置任务

1. **登录XXL-Job管理平台**
2. **创建执行器**：应用名称为配置的`appName`
3. **配置任务**：
   - 任务Handler：`jsonJobHandler`
   - 任务参数：`{"method":"sampleJobHandler","params":{"taskName":"demo","data":["item1","item2"],"batchSize":10}}`

## 🏗️ 核心组件详解

### 1. 配置接口层

#### IXXLJobsConfig 接口
<mcsymbol name="IXXLJobsConfig" filename="IXXLJobsConfig.java" path="/Users/abstergo/IdeaProjects/abstergo_solution/xxl-job-mod/src/main/java/com/tbs/xxl/api/IXXLJobsConfig.java" startline="3" type="interface">IXXLJobsConfig</mcsymbol>
- **核心功能**：定义XXL-Job执行器的配置参数
- **必填配置**：`adminAddress()` - 管理平台地址
- **可选配置**：应用名、网络地址、认证令牌等
- **默认实现**：支持部分配置项的默认值

#### XxlJobProperty 配置属性类
<mcsymbol name="XxlJobProperty" filename="XxlJobProperty.java" path="/Users/abstergo/IdeaProjects/abstergo_solution/xxl-job-mod/src/main/java/com/tbs/xxl/config/property/XxlJobProperty.java" startline="7" type="class">XxlJobProperty</mcsymbol>
- **配置绑定**：`@ConfigurationProperties(prefix = "tbs.xxl-job")`
- **功能开关**：`enable` - 总开关，`enableJsonJobExecutor` - JSON执行器开关
- **条件化加载**：基于配置属性的条件化Bean创建

### 2. 核心配置类

#### XxlJobConfig 配置类
<mcsymbol name="XxlJobConfig" filename="XxlJobConfig.java" path="/Users/abstergo/IdeaProjects/abstergo_solution/xxl-job-mod/src/main/java/com/tbs/xxl/config/XxlJobConfig.java" startline="14" type="class">XxlJobConfig</mcsymbol>
- **条件化配置**：`@ConditionalOnProperty` 控制Bean创建
- **执行器配置**：创建和管理XXL-Job Spring执行器
- **配置验证**：验证关键配置项的完整性
- **JSON执行器**：条件化创建JSON任务执行器

### 3. JSON任务执行器

#### JsonJobExecutor 执行器类
<mcsymbol name="JsonJobExecutor" filename="JsonJobExecutor.java" path="/Users/abstergo/IdeaProjects/abstergo_solution/xxl-job-mod/src/main/java/com/tbs/xxl/executor/JsonJobExecutor.java" startline="17" type="class">JsonJobExecutor</mcsymbol>
- **动态发现**：自动扫描和注册`IJsonJobHandler`实现
- **通用接口**：`jsonJobHandler` - 统一的JSON任务执行入口
- **帮助系统**：`available` - 任务列表，`help_json` - 使用帮助
- **错误处理**：完善的异常处理和错误信息返回

#### IJsonJobHandler 任务处理器接口
<mcsymbol name="IJsonJobHandler" filename="IJsonJobHandler.java" path="/Users/abstergo/IdeaProjects/abstergo_solution/xxl-job-mod/src/main/java/com/tbs/xxl/api/IJsonJobHandler.java" startline="7" type="interface">IJsonJobHandler</mcsymbol>
- **泛型支持**：支持任意参数类型的任务处理器
- **参数转换**：自动JSON到Java对象的参数转换
- **帮助信息**：可选的帮助文本描述
- **统一处理**：标准化的任务处理接口

### 4. 数据模型

#### ExecuteInfo 执行信息模型
<mcsymbol name="ExecuteInfo" filename="ExecuteInfo.java" path="/Users/abstergo/IdeaProjects/abstergo_solution/xxl-job-mod/src/main/java/com/tbs/xxl/model/ExecuteInfo.java" startline="8" type="class">ExecuteInfo</mcsymbol>
- **任务标识**：`method` - 任务处理器名称
- **参数传递**：`params` - 任务参数Map
- **JSON序列化**：支持FastJSON2序列化和反序列化

## 🔧 高级用法

### 1. 自定义配置提供器

```java
@Service
public class DynamicXxlJobConfig implements IXXLJobsConfig {
    
    @Value("${xxl.job.admin.address}")
    private String adminAddress;
    
    @Value("${xxl.job.app.name}")
    private String appName;
    
    @Override
    public String adminAddress() {
        return adminAddress;
    }
    
    @Override
    public String appName() {
        return appName;
    }
    
    // 其他配置方法...
}
```

### 2. 批量任务处理器

```java
@Component
public class BatchDataProcessor implements IJsonJobHandler<BatchProcessParams> {
    
    @Override
    public Class<BatchProcessParams> classType() {
        return BatchProcessParams.class;
    }
    
    @Override
    public String help() {
        return "批量数据处理任务，支持分页和并发处理";
    }
    
    @Override
    public String handle(BatchProcessParams params) throws Exception {
        int total = params.getTotal();
        int pageSize = params.getPageSize();
        int pages = (total + pageSize - 1) / pageSize;
        
        // 并行处理各页数据
        List<CompletableFuture<String>> futures = new ArrayList<>();
        for (int i = 0; i < pages; i++) {
            final int page = i;
            futures.add(CompletableFuture.supplyAsync(() -> {
                return processPage(page, pageSize);
            }));
        }
        
        // 等待所有任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        return String.format("批量处理完成，共处理%d页数据", pages);
    }
    
    private String processPage(int page, int pageSize) {
        // 单页数据处理逻辑
        return String.format("第%d页处理完成", page + 1);
    }
}
```

### 3. 任务监控和统计

```java
@Component
public class JobStatisticsHandler implements IJsonJobHandler<StatisticsParams> {
    
    @Resource
    private JobLogService jobLogService;
    
    @Override
    public Class<StatisticsParams> classType() {
        return StatisticsParams.class;
    }
    
    @Override
    public String handle(StatisticsParams params) throws Exception {
        LocalDate startDate = params.getStartDate();
        LocalDate endDate = params.getEndDate();
        
        JobStatistics stats = jobLogService.getStatistics(startDate, endDate);
        
        return JSON.toJSONString(stats);
    }
}
```

## 📊 任务管理

### 1. 内置任务列表

模块提供以下内置任务Handler：

| Handler名称 | 功能描述 | 参数格式 |
|------------|---------|---------|
| `jsonJobHandler` | 通用JSON任务执行器 | `{"method":"handlerName","params":{...}}` |
| `available` | 查询可用任务列表 | 无参数 |
| `help_json` | 获取JSON任务帮助信息 | 无参数 |

### 2. 任务参数示例

```json
{
    "method": "sampleJobHandler",
    "params": {
        "taskName": "数据清洗任务",
        "data": ["item1", "item2", "item3"],
        "batchSize": 100,
        "priority": "HIGH"
    }
}
```

## 🔍 监控与调试

### 1. 日志配置

```yaml
logging:
  level:
    com.tbs.xxl: DEBUG
    com.xxl.job: INFO
```

### 2. 健康检查

```java
@Component
public class XxlJobHealthIndicator implements HealthIndicator {
    
    @Resource
    private XxlJobSpringExecutor executor;
    
    @Override
    public Health health() {
        try {
            // 检查执行器状态
            if (executor != null) {
                return Health.up()
                    .withDetail("appName", executor.getAppname())
                    .withDetail("adminAddress", executor.getAdminAddresses())
                    .build();
            } else {
                return Health.down().withDetail("reason", "XXL-Job执行器未初始化").build();
            }
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
```

## 🛠️ 故障排除

### 1. 常见问题

#### 问题：执行器无法注册到管理平台
**症状**：管理平台显示执行器离线
**解决方案**：
- 检查网络连通性
- 验证`adminAddress`配置是否正确
- 检查访问令牌配置

#### 问题：JSON任务执行失败
**症状**：任务执行返回错误信息
**解决方案**：
```java
// 检查任务处理器注册
@PostConstruct
public void checkHandlers() {
    // 手动检查处理器注册情况
}
```

#### 问题：任务参数解析错误
**症状**：参数转换异常
**解决方案**：
- 验证参数JSON格式
- 检查参数类定义是否匹配
- 确保FastJSON2依赖正确

### 2. 调试技巧

```java
@Component
public class DebugJobHandler implements IJsonJobHandler<DebugParams> {
    
    @Override
    public String handle(DebugParams params) throws Exception {
        log.debug("任务开始执行，参数: {}", params);
        
        try {
            // 业务逻辑
            String result = processBusiness(params);
            log.debug("任务执行成功，结果: {}", result);
            return result;
        } catch (Exception e) {
            log.error("任务执行失败", e);
            throw e;
        }
    }
}
```

## 📈 最佳实践

### 1. 配置管理

- **环境隔离**：不同环境使用不同的管理平台地址
- **安全配置**：访问令牌定期更换
- **网络优化**：内网环境使用内网地址

### 2. 任务设计

- **单一职责**：每个任务处理器专注于单一功能
- **参数验证**：任务参数进行有效性验证
- **异常处理**：完善的异常处理和日志记录

### 3. 性能优化

- **批量处理**：大数据量任务采用分页处理
- **异步执行**：耗时任务使用异步处理
- **资源管理**：合理配置线程池和连接池

## 🔄 版本信息

- **当前版本**：1.0-SNAPSHOT
- **XXL-Job版本**：2.4.0
- **Java版本**：11+
- **Spring Boot**：2.7+

## 🤝 贡献指南

欢迎提交Issue和Pull Request来改进这个模块！

### 开发环境设置

1. 克隆项目
2. 导入IDE
3. 运行测试：`mvn test`
4. 构建项目：`mvn clean package`

### 代码规范

- 遵循Google Java代码风格
- 添加必要的单元测试
- 更新相关文档
- 确保向后兼容性

## 📄 许可证

本项目基于MIT许可证开源。

---

**XXL-Job-Mod模块** - 为您的应用提供强大而灵活的分布式任务调度能力！ 🚀

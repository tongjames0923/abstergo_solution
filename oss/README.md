# OSS 模块 - 对象存储统一门面

## 模块概述

OSS 模块是一个对象存储门面（Facade）框架，为 Abstergo Solution 提供统一的对象存储操作契约。业务代码只依赖 `OssClient` 门面接口，底层可无缝适配阿里云 OSS、MinIO、AWS S3、腾讯云 COS 等不同的第三方对象存储组件，切换存储供应商时业务代码零改动。

模块遵循项目统一的 **api / impl 拆分规范**：

```
oss/
├── oss-api/              # 契约层：门面接口、模型、异常、装配配置（零第三方依赖）
│   └── src/main/java/com/tbs/oss/
│       ├── annotation/
│       │   └── EnableTbsOss.java             # 启用入口注解
│       ├── config/
│       │   ├── OssPackAutoRegister.java      # 自动扫描注册器
│       │   ├── OssConfig.java                # 核心装配类
│       │   └── property/
│       │       └── OssProperty.java          # tbs.oss 配置绑定
│       ├── core/                             # 契约接口
│       │   ├── OssClient.java                # 统一门面（业务唯一依赖入口）
│       │   ├── OssOperations.java            # 对象级操作契约
│       │   ├── BucketOperations.java         # 桶级操作契约
│       │   ├── OssClientFactory.java         # 工厂 SPI
│       │   └── ProgressListener.java         # 进度回调
│       ├── model/                            # 纯 POJO 模型
│       │   ├── PutObjectRequest / PutObjectResult
│       │   ├── ListObjectsRequest / ObjectListing / ObjectInfo / ObjectMetadata
│       │   ├── CopyObjectRequest
│       │   ├── PresignedUrlRequest / PresignedUrlType
│       │   ├── Bucket
│       │   └── OssProperties
│       └── exception/                        # 统一异常体系
│           ├── OssException
│           ├── OssObjectNotFoundException
│           ├── OssBucketNotFoundException
│           └── OssAccessDeniedException
├── (impl 模块规划中，如 oss-minio-impl / oss-aliyun-impl)
└── pom.xml
```

## 核心特性

### 🎯 统一门面
- 业务只依赖 `OssClient` 一个接口，屏蔽所有厂商 SDK 差异
- 上传 / 下载 / 删除 / 复制 / 移动 / 列表 / 预签名 URL / 桶管理全覆盖
- 默认桶模式：常规操作无需传递 bucketName，一行代码完成上传

### 🔌 可插拔实现
- `OssClientFactory` SPI + `tbs.oss.type` 配置驱动，按类型自动匹配实现工厂
- 每个第三方实现一个独立 impl 模块，依赖边界清晰
- 多实现共存时通过 `@ConditionalOnMissingBean` 支持自定义覆盖

### 🛡️ 统一异常体系
- 所有第三方 SDK 异常统一转换为 `OssException` 及其子类
- 异常携带 bucketName / objectKey 上下文，便于定位问题

### 📦 零厂商耦合模型
- model 层全部为纯 POJO，不依赖任何第三方 SDK
- 列表查询采用主流 OSS 通用的 marker 游标分页模型，兼容 S3 / 阿里云 / MinIO

## 设计理念

### 1. 门面模式：业务只认一个接口

对象存储在项目中的典型用法就是"传文件、拿 URL"，但每个厂商 SDK 的 API 形态、异常类型、分页模型各不相同。如果业务直接依赖厂商 SDK，换存储就是全项目改造。

因此本模块将厂商差异全部收敛到 impl 层，业务侧唯一入口是 `OssClient`：

```
业务代码 ──> OssClient（门面）
                │
        OssOperations + BucketOperations（契约拆分）
                │
        OssClientFactory（SPI，按 type 匹配）
                │
    ┌───────────┼───────────┬─────────────┐
    MinIO    阿里云 OSS   AWS S3     腾讯云 COS  ...（impl 模块各自实现）
```

### 2. 契约拆分：对象操作与桶操作分离

- `OssOperations`：对象级操作，是业务 95% 场景用到的契约
- `BucketOperations`：桶管理操作，属于运维低频操作

拆分后职责清晰，impl 也可以只实现对象操作（部分托管服务不允许程序化建桶）。`OssClient` 通过接口聚合对外呈现完整门面。

### 3. 默认桶模式：消灭样板参数

实际业务中绝大多数上传都落在同一个配置好的桶里。因此契约方法在 `bucketName` 缺省时自动回退到 `tbs.oss.default-bucket`，常见调用简化为一行：

```java
ossClient.putObject("report/2026/a.xlsx", file);
```

需要跨桶操作时，通过 Request 对象显式指定桶名即可。

### 4. Request / Result 对象化：接口签名永不膨胀

上传、复制、列表、预签名四类参数较多的操作均使用 Builder 构建的 Request 对象：

```java
PutObjectRequest.builder("report/2026/a.xlsx")
    .bucketName("archive")
    .contentType("application/vnd.ms-excel")
    .userMetadata(Map.of("source", "report-service"))
    .progressListener(listener)
    .file(localFile)
    .build();
```

后续扩展字段（分片参数、回调地址、版本控制等）只加 Request 属性，**不破坏接口签名**，实现模块无需跟随改动。

### 5. 工厂 SPI + 条件装配：装配与实现解耦

装配逻辑收敛在 oss-api 的 `OssConfig` 中：从容器收集所有 `OssClientFactory`，按 `tbs.oss.type`（大小写不敏感）匹配并创建客户端。这意味着：

- impl 模块只需提供一个带 `@Component` 的工厂 Bean，放在 `com.tbs.oss` 子包下即被自动扫描
- oss-api 完全不依赖任何厂商 SDK，pom 保持零第三方依赖
- 找不到匹配工厂时抛出带明确指引的 `OssException`，而不是隐晦的 NPE

### 6. 防重复装配：与 redis-mod 加固方案一致

- `OssPackAutoRegister` 继承 `AbstractAutoBeanRegister`，天然具备 `PACK_MAP` 去重能力，多模块重复 `@EnableTbsOss` 只扫描一次
- `OssProperty` 不使用 `@Component`，由 `@EnableConfigurationProperties` 显式声明，避免重复导入时重复注册
- `ossClient` Bean 带 `@ConditionalOnMissingBean`，业务可自行定义客户端覆盖默认装配
- `@Bean(destroyMethod = "close")` 保证应用关闭时释放底层 SDK 连接资源

### 7. 统一异常与流责任契约

- impl 必须把厂商 SDK 异常转换为 `OssException` 体系，业务层 catch 逻辑与厂商无关
- 流的责任边界在 `OssOperations` Javadoc 中写明：调用方传入的上传流由调用方关闭；`getObject` 返回的流由调用方关闭

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.tbs</groupId>
    <artifactId>oss-api</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
<!-- 再引入对应的实现模块，如 oss-minio-impl（规划中） -->
```

### 2. 启用功能

在启动类或配置类上添加注解：

```java
@EnableTbsOss
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 3. 配置连接信息

```yaml
tbs:
  oss:
    enable: true                     # 启用开关
    type: minio                      # 实现类型，匹配工厂的 supportType()
    endpoint: http://127.0.0.1:9000  # 服务端点
    region: us-east-1                # 地域（按厂商要求填写，可为空）
    access-key-id: minioadmin
    access-key-secret: minioadmin
    default-bucket: app-files        # 默认桶
    path-style-access: true          # MinIO 等自建服务需开启
```

### 4. 业务使用

```java
@Service
public class FileService {

    @Resource
    private OssClient ossClient;

    /** 上传本地文件到默认桶 */
    public PutObjectResult upload(String objectKey, File file) {
        return ossClient.putObject(objectKey, file);
    }

    /** 流式上传，附带元数据与进度回调 */
    public PutObjectResult uploadStream(String objectKey, InputStream in, long length) {
        return ossClient.putObject(PutObjectRequest.builder(objectKey)
            .inputStream(in)
            .contentLength(length)
            .contentType("application/octet-stream")
            .userMetadata(Map.of("uploader", "system"))
            .progressListener(new ProgressListener.Adapter() {
                @Override
                public void onSuccess() {
                    log.info("上传完成: {}", objectKey);
                }
            })
            .build());
    }

    /** 生成一小时有效的下载链接 */
    public String downloadUrl(String objectKey) {
        return ossClient.generatePresignedUrl(objectKey, 3600);
    }

    /** 客户端直传：生成上传预签名 URL */
    public String directUploadUrl(String objectKey) {
        return ossClient.generatePresignedUrl(
            PresignedUrlRequest.upload(objectKey)
                .expirationSeconds(600)
                .contentType("application/pdf")
                .build());
    }

    /** 下载对象（注意：流由调用方关闭） */
    public void downloadTo(String objectKey, File target) {
        ossClient.getObject(null, objectKey, target);
    }

    /** 按前缀分页列出对象 */
    public ObjectListing listReports(String marker) {
        return ossClient.listObjects(ListObjectsRequest.builder()
            .prefix("report/2026/")
            .delimiter("/")
            .marker(marker)
            .maxKeys(100)
            .build());
    }

    /** 桶管理 */
    public void ensureBucket(String bucketName) {
        if (!ossClient.doesBucketExist(bucketName)) {
            ossClient.createBucket(bucketName);
        }
    }
}
```

## 核心组件详解

### OssClient - 统一门面

聚合 `OssOperations` 与 `BucketOperations` 的业务唯一入口，同时提供：

| 方法 | 说明 |
|------|------|
| `getType()` | 当前实现类型标识 |
| `getDefaultBucket()` | 配置的默认桶名 |
| `getProperties()` | 连接配置属性 |
| `close()` | 释放底层 SDK 资源（应用关闭时自动调用） |

### OssOperations - 对象级操作

| 分组 | 方法 |
|------|------|
| 上传 | `putObject(PutObjectRequest)` 及流 / 文件 / 字节数组便捷重载 |
| 下载 | `getObject(...)` 流式 / 落盘两种形态、`getObjectMetadata` |
| 删除 | `deleteObject` / `deleteObjects` 批量删除 |
| 复制移动 | `copyObject` / `moveObject`（复制+删除源对象） |
| 列表 | `listObjects`，支持前缀、delimiter 目录模拟、marker 分页 |
| 预签名 | `generatePresignedUrl`（下载/上传）、`getObjectUrl` 公开读直链 |

### BucketOperations - 桶级操作

`createBucket` / `deleteBucket` / `doesBucketExist` / `listBuckets`。

### OssClientFactory - 实现接入 SPI

```java
public interface OssClientFactory {
    /** 工厂支持的类型标识，与 tbs.oss.type 匹配 */
    String supportType();

    /** 根据配置创建客户端 */
    OssClient create(OssProperties properties);
}
```

### 配置属性一览（OssProperty，前缀 tbs.oss）

| 属性 | 类型 | 默认值 | 描述 |
|------|------|--------|------|
| enable | boolean | false | 是否启用 OSS 功能 |
| type | String | - | 实现类型标识（aliyun / minio / aws-s3 / tencent-cos 等） |
| endpoint | String | - | 服务端点地址 |
| region | String | - | 地域 |
| access-key-id | String | - | 访问密钥 ID |
| access-key-secret | String | - | 访问密钥 Secret |
| default-bucket | String | - | 默认桶名 |
| path-style-access | boolean | false | 是否启用 path-style 访问（MinIO 等自建服务需开启） |

### 异常体系

| 异常 | 触发场景 |
|------|---------|
| `OssException` | 基类，所有 OSS 操作失败均抛出此异常或其子类 |
| `OssObjectNotFoundException` | 对象不存在 |
| `OssBucketNotFoundException` | 桶不存在 |
| `OssAccessDeniedException` | 凭证无效、无权限 |

## 扩展开发：新增一个第三方实现

以接入 MinIO 为例，四步完成：

**第一步**：在 `oss/` 下创建 `oss-minio-impl` 模块，依赖 `oss-api` 与 MinIO SDK。

**第二步**：实现 `OssClient`，内部持有 MinIO SDK 客户端，完成契约到 SDK 的映射：

```java
public class MinioOssClient implements OssClient {
    // 将 PutObjectRequest / ListObjectsRequest 等统一模型
    // 映射为 MinIO SDK 调用，并将 SDK 异常转换为 OssException 体系
}
```

**第三步**：实现工厂并注册为 Bean（**包名必须在 `com.tbs.oss` 子包下**才能被扫描）：

```java
package com.tbs.oss.minio;

@Component
public class MinioOssClientFactory implements OssClientFactory {

    @Override
    public String supportType() {
        return "minio";
    }

    @Override
    public OssClient create(OssProperties properties) {
        MinioClient sdkClient = MinioClient.builder()
            .endpoint(properties.getEndpoint())
            .credentials(properties.getAccessKeyId(), properties.getAccessKeySecret())
            .build();
        return new MinioOssClient(sdkClient, properties);
    }
}
```

**第四步**：业务工程引入 `oss-minio-impl` 依赖，配置 `tbs.oss.type=minio` 即完成切换。

## 最佳实践

### 1. objectKey 命名规范
- 使用 `/` 分层组织：`业务域/时间/文件名`，如 `report/2026/08/xxx.xlsx`
- 配合 `listObjects` 的 `prefix` + `delimiter("/")` 模拟目录浏览

### 2. 大文件上传
- 流式上传尽量显式指定 `contentLength`，避免实现层为计算长度而整体缓冲
- 挂载 `ProgressListener` 监控大文件传输进度

### 3. 直传场景
- 前端直传走 `PresignedUrlRequest.upload(...)` 生成上传预签名 URL，文件不经过应用服务器
- 预签名有效期按需最小化，下载链接建议 5~15 分钟

### 4. 流的关闭责任
- `getObject` 返回的流必须在 finally / try-with-resources 中关闭，避免连接泄漏
- 上传传入的流由调用方关闭，实现层不会代关

## 故障排除

**1. 启动报 `tbs.oss.type未配置`**
```
确认 application.yml 中 tbs.oss.enable=true 且配置了 type
```

**2. 启动报 `未找到类型[xxx]对应的OssClientFactory实现`**
```
确认已引入对应实现模块依赖
确认工厂 Bean 所在包位于 com.tbs.oss 子包下且标注了 @Component
```

**3. `Feature.OSS` 编译找不到**
```
oss-api 依赖的 common 模块有改动，需先 mvn install common（或整体构建）
```

**4. MinIO 访问 403 / 签名错误**
```
自建 MinIO 通常需开启 path-style-access: true
检查 endpoint 协议（http/https）与实际服务是否一致
```

## 版本信息

- **Java**: 11+
- **Spring Boot**: 2.7.x
- **oss-api 第三方依赖**: 无（纯契约 + Spring 装配）

---

**最后更新**: 2026年8月
**版本**: 1.0-SNAPSHOT

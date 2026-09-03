# OSS 模块 - 对象存储统一门面

基于门面（Facade）模式的对象存储统一门面框架。业务代码只依赖 `OssClient` 一个接口，底层可无缝适配 MinIO、阿里云 OSS、AWS S3、腾讯云 COS 等第三方对象存储。切换存储供应商时业务代码零改动。

## 目录

- [模块定位](#模块定位)
- [子模块说明](#子模块说明)
- [核心设计](#核心设计)
- [快速开始](#快速开始)
- [对象操作契约（OssOperations）](#对象操作契约ossoperations)
- [桶操作契约（BucketOperations）](#桶操作契约bucketoperations)
- [统一门面（OssClient）](#统一门面ossclient)
- [数据模型与 Builder](#数据模型与-builder)
- [统一异常体系](#统一异常体系)
- [MinIO 实现](#minio-实现)
- [配置参考](#配置参考)
- [扩展指南：新增第三方实现](#扩展指南新增第三方实现)
- [最佳实践](#最佳实践)
- [注意事项](#注意事项)
- [包结构](#包结构)

## 模块定位

| 维度 | 说明 |
| --- | --- |
| 角色层级 | 基础层（common 之上），聚合 API/impl 两子模块 |
| 核心能力 | 对象上传/下载/删除/复制/移动/列表/预签名 URL/桶管理 的统一契约 |
| 依赖 | `oss` 聚合 POM 依赖 `common`；`oss-api` 自身零第三方依赖 |
| 启用方式 | `@EnableTbsOss` 注解（无 `spring.factories`） |

## 子模块说明

`oss` 为 `packaging=pom` 的聚合模块（**不可作为依赖引入**）：

| 子模块 | artifactId | 说明 |
| --- | --- | --- |
| oss-api | `oss-api` | 契约层：门面接口、模型、异常、装配配置。零第三方依赖，仅依赖 `common` |
| oss-minio-impl | `oss-minio-impl` | MinIO（S3 协议）官方实现，依赖 `oss-api` + `io.minio:minio` |

依赖坐标：

```xml
<dependency>
    <groupId>com.tbs</groupId>
    <artifactId>oss-api</artifactId>
</dependency>
<!-- 引入对应实现，如 MinIO -->
<dependency>
    <groupId>com.tbs</groupId>
    <artifactId>oss-minio-impl</artifactId>
</dependency>
```

## 核心设计

### 1. 门面模式：业务只认一个接口

`OssClient` 聚合 `OssOperations`（对象级）与 `BucketOperations`（桶级），是业务唯一入口。厂商差异全部收敛到各 impl 模块的 `OssClientFactory` SPI 中。

### 2. 默认桶模式：消灭样板参数

契约方法在 `bucketName` 为 null 或空时回退到 `tbs.oss.default-bucket`。若未指定桶名且未配置默认桶，抛 `OssException`：

```java
ossClient.putObject("report/2026/a.xlsx", file);   // 落到默认桶
```

跨桶操作通过 Request 对象显式指定桶名。

### 3. Request / Result 对象化

参数较多的上传/列表/复制/预签名均用不可变的 Request + Builder 构建，扩展字段只加 Request 属性，不破坏接口签名。

### 4. 装配与实现解耦

装配逻辑收敛在 `OssConfig`：从容器收集全部 `OssClientFactory`，按 `tbs.oss.type`（大小写不敏感）匹配并创建客户端；找不到匹配工厂时抛出带明确指引的 `OssException`。

## 快速开始

### 1. 依赖

```xml
<dependency><groupId>com.tbs</groupId><artifactId>oss-api</artifactId></dependency>
<dependency><groupId>com.tbs</groupId><artifactId>oss-minio-impl</artifactId></dependency>
```

### 2. 启动类标注注解

```java
@SpringBootApplication
@EnableTbsOss      // @Import(OssPackAutoRegister) → 继承 AbstractAutoBeanRegister，扫描 com.tbs.oss，注册 Feature.OSS
public class Application { ... }
```

`OssPackAutoRegister` 具备 `PACK_MAP` 去重能力，重复 `@EnableTbsOss` 只扫描一次。

### 3. 配置

```yaml
tbs:
  oss:
    enable: true                     # 总开关（默认 false，关闭则不装配 OssClient Bean）
    type: minio                      # 实现类型，匹配工厂的 supportType()；缺失/不匹配直接启动失败
    endpoint: http://127.0.0.1:9000
    region: us-east-1                # 可空
    access-key-id: minioadmin
    access-key-secret: minioadmin
    default-bucket: app-files        # 默认桶
    path-style-access: true          # MinIO 官方 SDK 天然 path-style，此配置当前对 MinIO 实现不生效
```

### 4. 业务使用

```java
@Service
public class FileService {
    @Resource
    private OssClient ossClient;

    public PutObjectResult upload(String key, File file) { return ossClient.putObject(key, file); }

    // 流式上传 + 进度 + 元数据（Request 对象）
    public PutObjectResult uploadStream(String key, InputStream in, long len) {
        return ossClient.putObject(PutObjectRequest.builder(key)
            .inputStream(in).contentLength(len)
            .contentType("application/octet-stream")
            .userMetadata(Map.of("uploader", "system"))
            .progressListener(new ProgressListener.Adapter() {
                @Override public void onSuccess() { /* ... */ }
            }).build());
    }

    public String downloadUrl(String key) { return ossClient.generatePresignedUrl(key, 3600); }

    public String directUploadUrl(String key) {
        return ossClient.generatePresignedUrl(PresignedUrlRequest.upload(key)
            .expirationSeconds(600).contentType("application/pdf").build());
    }

    public ObjectListing list(String prefix, String marker) {
        return ossClient.listObjects(ListObjectsRequest.builder()
            .prefix(prefix).delimiter("/").marker(marker).maxKeys(100).build());
    }
}
```

## 对象操作契约（OssOperations）

```java
public interface OssOperations {
    // 上传：Request / 流 / 文件 / 字节 4 种（对象流版本 contentLength=-1 表示未知）
    PutObjectResult putObject(PutObjectRequest request);
    PutObjectResult putObject(String objectKey, InputStream inputStream, long contentLength, String contentType);
    PutObjectResult putObject(String objectKey, File file);        // 默认桶
    PutObjectResult putObject(String objectKey, byte[] content);   // 默认桶

    // 下载：流 / 默认桶流 / 落盘
    InputStream getObject(String bucketName, String objectKey);    // 返回流由调用方关闭
    InputStream getObject(String objectKey);
    void getObject(String bucketName, String objectKey, File targetFile);
    ObjectMetadata getObjectMetadata(String bucketName, String objectKey);

    // 删除/存在性
    void deleteObject(String bucketName, String objectKey);
    void deleteObject(String objectKey);
    void deleteObjects(String bucketName, List<String> objectKeys);  // 批量
    boolean doesObjectExist(String bucketName, String objectKey);

    // 复制/移动
    void copyObject(CopyObjectRequest request);
    void moveObject(CopyObjectRequest request);                      // 复制 + 删除源

    // 列表（前缀/delimiter/marker 分页）
    ObjectListing listObjects(ListObjectsRequest request);

    // 预签名 URL
    String generatePresignedUrl(PresignedUrlRequest request);
    String generatePresignedUrl(String objectKey, long expirationSeconds); // 默认桶下载
    String getObjectUrl(String bucketName, String objectKey);              // 公开读直链（行为由实现决定）
}
```

实现约定（接口 javadoc 明示）：
1. 所有第三方 SDK 异常必须转换为 `OssException` 及其子类；
2. 上传传入的流由**调用方**负责关闭，实现只关闭自己新建的内部流；
3. `getObject` 返回的流由**调用方**负责关闭。

## 桶操作契约（BucketOperations）

`createBucket` / `deleteBucket`（桶非空抛异常）/ `doesBucketExist` / `listBuckets`。

## 统一门面（OssClient）

```java
public interface OssClient extends OssOperations, BucketOperations, AutoCloseable {
    String getType();               // 实现类型标识
    String getDefaultBucket();      // 配置的默认桶（未配置返回 null）
    OssProperties getProperties();  // 连接配置
    void close();                   // 释放底层 SDK 资源（@Bean(destroyMethod="close") 自动调用）
}
```

## 数据模型与 Builder

| 模型 | 构建方式 | 关键字段 |
| --- | --- | --- |
| `PutObjectRequest` | `PutObjectRequest.builder(objectKey)` | bucketName、objectKey、inputStream/file/content 三选一、contentLength(默认-1)、contentType、userMetadata、progressListener |
| `PutObjectResult` | 由实现回填 | bucketName、objectKey、etag、url(实现支持时) |
| `ListObjectsRequest` | `ListObjectsRequest.builder()` | bucketName、prefix、marker、maxKeys(默认100)、delimiter |
| `ObjectListing` | 由实现回填 | objects、commonPrefixes、truncated、nextMarker |
| `ObjectInfo` | 由实现回填 | bucketName、objectKey、contentLength、etag、lastModified |
| `ObjectMetadata` | 由实现回填 | contentLength、contentType、etag、lastModified、userMetadata |
| `CopyObjectRequest` | `CopyObjectRequest.builder(srcKey, targetKey)`（默认桶内）| sourceBucketName/sourceObjectKey/targetBucketName/targetObjectKey |
| `PresignedUrlRequest` | `download(objectKey)` / `upload(objectKey)` | bucketName、type、expirationSeconds(默认3600)、contentType |
| `OssProperties` | setter（非 Builder） | 与 OssProperty 字段一一对应的连接模型 |

分页模型为 S3 通行的 marker 游标式（`truncated` + `nextMarker`），兼容 MinIO/S3/阿里云/腾讯云。

## 统一异常体系

| 异常 | 触发场景 | 携带上下文 |
| --- | --- | --- |
| `OssException`（RuntimeException） | 基类，所有 OSS 操作失败 | bucketName/objectKey（可 null） |
| `OssObjectNotFoundException` | 对象不存在（S3 `NoSuchKey`） | 构造器自动拼 message |
| `OssBucketNotFoundException` | 桶不存在（S3 `NoSuchBucket`） | 同上 |
| `OssAccessDeniedException` | 凭证无效/无权限（`AccessDenied`/`InvalidAccessKeyId`/`SignatureDoesNotMatch`） | message |

## MinIO 实现

### MinioOssClientFactory

```java
@Component
public class MinioOssClientFactory implements OssClientFactory {
    public static final String TYPE = "minio";
    @Override public String supportType() { return TYPE; }
    @Override public OssClient create(OssProperties properties) {
        // 校验 endpoint 非空（缺失抛 OssException）；region 可空按需设置
        return new MinioOssClient(MinioClient.builder()
            .endpoint(...).credentials(...) /* .region(...) 可选 */
            .build(), properties);
    }
}
```

必须位于 `com.tbs.oss` 子包下且标注 `@Component` 才能被 `OssPackAutoRegister` 扫描到。

### MinioOssClient 实现要点

- **上传**：文件/字节内部开流并在 finally 关闭；调用方流不代关；未指定任何内容抛 `OssException`。进度经 `ProgressInputStream` 包装，MinIO SDK 自身不支持进度回调。
- **分片**：`PutObjectArgs` 统一用 `DEFAULT_PART_SIZE`（5MiB，MinIO 下限）。
- **批量删除**：SDK 惰性迭代——必须 `for` 遍历 `removeObjects` 结果才会真正触发删除，任一 `DeleteError` 抛异常。
- **列表**：向 SDK 取 `maxKeys + 1` 条判断截断；`delimiter` 为 null 时 `recursive(true)`；目录（公共前缀）与文件条目**共用** maxKeys 额度；`nextMarker` 取本页末条文件与末公共前缀的字典序较大者作为 `startAfter` 游标。
- **预签名**：`UPLOAD`→`Method.PUT`、`DOWNLOAD`→`Method.GET`。
- **公开读 URL**：`getObjectUrl` 直接拼接 `${endpoint}/${bucket}/${key}`（未走签名）。
- **异常翻译**（`translate`）：`ErrorResponseException` 按 code 映射 `NoSuchKey`→`OssObjectNotFoundException`、`NoSuchBucket`→`OssBucketNotFoundException`、`AccessDenied`/`InvalidAccessKeyId`/`SignatureDoesNotMatch`→`OssAccessDeniedException`，其余归 `OssException`；`IllegalArgumentException`→参数错误。
- **元数据头**：标准 HTTP 响应头（`content-disposition`/`content-encoding`/`content-language`/`cache-control`/`expires`）作为对象真实头，其余加 `x-amz-meta-` 前缀。
- **close()**：MinIO SDK 8.5.x 未暴露关闭方法，底层 OkHttp 连接池由空闲超时自动回收，实现为空操作。

### ProgressInputStream

包装上传流按**64KiB 步长**节流通知 `onProgress`（避免高频回调）；总字节已知且传输完成时也会触发一次。

## 配置参考

### `tbs.oss.*`（OssProperty）

| 属性 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `enable` | boolean | `false` | 总开关；`true` 且容器无 `OssClient` 时才装配门面 Bean |
| `type` | String | - | 实现类型标识，缺失抛 "tbs.oss.type未配置"；不匹配抛 "未找到类型[...]对应的OssClientFactory实现" |
| `endpoint` | String | - | 服务端点 |
| `region` | String | - | 地域（可空） |
| `access-key-id` | String | - | 访问密钥 ID |
| `access-key-secret` | String | - | 访问密钥 Secret |
| `default-bucket` | String | - | 默认桶名 |
| `path-style-access` | boolean | `false` | 是否 path-style；MinIO 官方实现天然采用，此配置项当前不消费 |

门面 Bean：`@ConditionalOnMissingBean(OssClient.class)`（业务可自定义覆盖）+ `@ConditionalOnProperty(enable=true)` + `@Bean(destroyMethod="close")`。

## 扩展指南：新增第三方实现

以接入新厂商（如 Aliyun OSS）为例四步完成：

**第一步**：在 `oss/` 下新建 `oss-aliyun-impl`，依赖 `oss-api` 与厂商 SDK。

**第二步**：实现 `OssClient`，把统一模型映射为厂商 SDK 调用，**并将全部 SDK 异常转换为 `OssException` 体系**。

**第三步**：实现工厂并注册为 Bean（**包名必须在 `com.tbs.oss` 子包下**）：

```java
package com.tbs.oss.aliyun;

@Component
public class AliyunOssClientFactory implements OssClientFactory {
    @Override public String supportType() { return "aliyun"; }
    @Override public OssClient create(OssProperties properties) {
        // ... 构建厂商 SDK 客户端
        return new AliyunOssClient(client, properties);
    }
}
```

**第四步**：业务引入 `oss-aliyun-impl`，配置 `tbs.oss.type=aliyun` 即完成切换。

## 最佳实践

1. **objectKey 命名**：`业务域/时间/文件名` 分层；配合 `listObjects` 的 `prefix` + `delimiter("/")` 模拟目录。
2. **大文件/流上传**：尽量显式 `contentLength`（未知时实现需内部缓冲/分片）；挂载 `ProgressListener` 监控进度。
3. **直传场景**：前端直传走 `PresignedUrlRequest.upload(...)`，文件不经过应用服务器；预签名有效期按需最小化。
4. **流的关闭责任**：`getObject` 返回的流务必在 try-with-resources 中关闭；上传传入流由调用方关闭，实现不代关。
5. **MinIO 403/签名错误**：确认 endpoint 协议（http/https）与实际一致；MinIO SDK 天然 path-style，无需额外配置。

## 注意事项

1. **聚合模块不可依赖**：依赖请引 `oss-api` / `oss-minio-impl`，而非 `oss`。
2. **`enable` 默认 false**：不显式配置 `tbs.oss.enable=true` 则 `OssClient` Bean 不装配。
3. **`type` 与工厂 supportType 大小写不敏感**，但缺失或不匹配都会抛 `OssException` 阻止启动（前者 "tbs.oss.type未配置"，后者 "未找到类型[...]对应的OssClientFactory实现"）。
4. **`path-style-access` 对 MinIO 不生效**：配置属性虽定义，MinIO SDK 天然采用 path-style，官方实现未消费该字段；若接入其他需该开关的厂商实现需自行读取。
5. **`getObjectUrl` 是字符串拼接而非签名 URL**：MinIO 实现直接拼 `${endpoint}/${bucket}/${key}`，桶非公开读时该链接不可直接访问，仅对公开读场景有意义。
6. **批量删除必须触发迭代**：MinIO 的 `removeObjects` 返回惰性 `Iterable`，官方实现已通过遍历触发真实删除并逐条校验错误。
7. **流责任严格遵守契约**：`putObject` 传入的流由调用方管理、实现不关；实现内部新建的流（文件/字节来源）自行关闭。
8. **`ProgressInputStream` 无 URL 覆盖全局**：进度回调仅在走 `putObject(PutObjectRequest)` 且显式挂载 listener 时生效。

## 包结构

```
oss/
├── oss-api/src/main/java/com/tbs/oss/
│   ├── annotation/       @EnableTbsOss（启用入口）
│   ├── config/           OssPackAutoRegister（扫描 com.tbs.oss，注册 Feature.OSS）、
│   │                     OssConfig（装配门面，@EnableConfigurationProperties）
│   │   └── property/     OssProperty（tbs.oss 配置）
│   ├── core/             OssClient（门面）、OssOperations、BucketOperations、
│   │                     OssClientFactory（SPI）、ProgressListener（+ Adapter）
│   ├── exception/        OssException（基类）+ Object/BucketNotFound + AccessDenied
│   └── model/            PutObjectRequest/Result、ListObjectsRequest、ObjectListing、
│                         ObjectInfo、ObjectMetadata、CopyObjectRequest、
│                         PresignedUrlRequest/Type、OssProperties、Bucket
└── oss-minio-impl/src/main/java/com/tbs/oss/minio/
    ├── MinioOssClientFactory（@Component，TYPE="minio"）
    ├── MinioOssClient（S3 映射 + 异常翻译）
    └── support/ProgressInputStream（64KiB 步长进度流）
```

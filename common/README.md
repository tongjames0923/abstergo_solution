# Common 模块功能文档

## 项目结构概述

`common`模块是通用工具包，提供了一系列可复用的功能类与接口，主要包含以下目录：

- `config`: 配置类，用于Spring Boot自动装配。
- `constant`: 常量定义。
- `exception`: 自定义异常处理。
- `holder`: 上下文持有者管理，支持多种存储方式（本地、线程）。
- `util`: 工具类，如加密、字符串处理、UUID生成等。
- `uuid`: UUID生成器实现。

---

## 功能详解

### 1. 配置 (`config`)

#### `HolderConfig.java`

上下文持有者配置类，根据配置启用不同的上下文存储方式。

**关键功能：**

- 支持本地存储和线程存储的上下文。
- 使用`@ConditionalOnProperty`控制是否启用特定的上下文。

#### `UuidConfig.java`

UUID生成器配置类，根据配置选择不同的UUID生成策略。

**关键功能：**

- 支持`SimpleUUID`和`SnowFlakeUUID`两种生成策略。
- 可通过配置文件动态切换生成策略。

---

### 2. 常量 (`constant`)

#### `HolderBeanNameConstant.java`

定义了上下文持有者的Bean名称常量。

#### `UuidBuiltinGeneratorTypeNameConstant.java`

定义了内置的UUID生成器类型名称常量。

---

### 3. 异常 (`exception`)

#### `FormattedRuntimeException.java`

自定义运行时异常类，支持格式化错误信息。

**使用示例：**

```java 
throw new FormattedRuntimeException("发生错误: %s","详情");
```

---

### 4. 上下文持有者 (`holder`)

#### `IContextHolder.java`

上下文数据存储接口，提供获取上下文的方法。

#### `AbstractNamedContext.java`

抽象命名上下文，提供基本的上下文操作。

#### `LocalStoredContext.java`

本地存储上下文，适用于单线程环境。

#### `ThreadLocalStoredContext.java`

线程存储上下文，适用于多线程环境。

#### `ImportedContextHolder.java`

手动导入的上下文持有者，允许动态添加和删除上下文。

#### `SpringManagedContextHolder.java`

Spring管理的上下文持有者，通过Spring Bean管理上下文。

---

### 5. 工具类 (`util`)

#### `AESUtil.java`

AES加密解密工具类。

**使用示例：**

```java 
String encrypted = AESUtil.encrypt("key", "data");
String decrypted = AESUtil.decrypt("key", encrypted);
```

#### `BeanUtil.java`

Bean操作工具类，继承自Hutool的`BeanUtil`。

#### `CollUtil.java`

集合操作工具类，继承自Hutool的`CollUtil`。

#### `EncryptUitl.java`

加密工具类，支持SHA-128、SHA-256、SHA-512算法。

**使用示例：**

```java 
String hash = EncryptUitl.hash256Encrypt("data");
```

#### `EnumUtil.java`

枚举操作工具类，提供根据code或value查找枚举值的功能。

#### `HolderUtil.java`

上下文工具类，提供静态方法访问上下文。

**使用示例：**

```java 
IContext context = HolderUtil.getDefaultLocalStoredContext(); 
context.

set("key","value");

Object value = context.get("key");
```

#### `LambdaUtil.java`

Lambda表达式工具类，提取字段名、方法名等功能。

#### `RSAUtil.java`

RSA加密解密工具类。

**使用示例：**

```java
String publicKey = "...";
String privateKey = "...";
String encrypted = RSAUtil.encrypt("data", publicKey);
String decrypted = RSAUtil.decrypt(encrypted, privateKey);
```

#### `SpringUtil.java`

Spring工具类，提供构建Bean的方法。

#### `StrUtil.java`

字符串工具类，继承自Hutool的`StrUtil`并扩展。

#### `UuidUtil.java`

UUID生成工具类，提供静态方法生成UUID。

**使用示例：**

```java
 String uuid = UuidUtil.getUuidString();
```

---

### 6. UUID生成器 (`uuid`)

#### `IUuidGenerator.java`

UUID生成器接口，定义生成UUID的方法。

#### `SimpleUUID.java`

简单UUID生成器，基于哈希算法生成UUID。

#### `SnowFlakeUUID.java`

雪花算法UUID生成器，适用于分布式系统。

**使用示例：**

```java
IUuidGenerator generator = new SnowFlakeUUID(0, 0);
String uuid = generator.getUuidString(32);
```

---

## 总结

`common`模块提供了丰富的工具类和配置类，涵盖了加密、上下文管理、UUID生成等多个方面。这些功能可以方便地集成到其他模块中，提高开发效率和代码质量。

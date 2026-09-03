# Script Engine 模块 - 脚本引擎

## 模块概述

Script Engine 模块（`com.tbs:script-engine`）提供**嵌入式脚本引擎**抽象：统一"引擎 → 编译器 → 上下文 → 执行器"四层模型，屏蔽具体脚本语言差异，内置 SpEL（Spring Expression Language）实现。

核心设计：

- **引擎注册表**：编译器按 `名称:主版本.次版本` 为键注册，运行时按名称与版本取用
- **上下文变量作用域**：全局（Global）/ 局部（Local）双作用域，支持父子上下文继承（子读父、写隔离）
- **执行器封装**：编译产物包装为可执行对象，内置并发保护与执行后上下文重置
- **可扩展**：实现 `AbstractScriptCompiler` 即可接入新的脚本语言（Groovy/JS/QLEXP 等）

模块结构（父 POM `script-engine` 为聚合 POM，依赖 `common` + `async`）：

| 子模块 | artifactId | 职责 |
|--------|------------|------|
| `script-api` | `script-engine-api` | 全部内容：契约接口、SpEL 实现、自动装配（唯一子模块） |

## 模块结构

```
script-engine/
└── script-api/                       # 唯一子模块（artifactId=script-engine-api）
    └── com.tbs.script/
        ├── annotation/               # @EnableScriptEngine
        ├── config/                   # AutoScriptRegister / ScriptEngineConfig / ScriptEngineProperty
        ├── model/                    # ScriptCompilerFeatures / ScriptCompilerSpec / ScriptResult
        ├── util/                     # ScriptContexts / ScriptExecutors
        ├── exception/                # ScriptException 及 3 个子类
        └── api/
            ├── compiler/             # IScriptCompiler / IScriptCompilerMeta / AbstractScriptCompiler
            │   └── impl/             # SpelCompiler
            ├── context/              # IScriptContext / IScriptContextFactory / ScriptScope
            │   └── impl/             # SimpleLocalMapSciptContext / EmptySimpleScriptContextFactory
            ├── engine/               # IScriptEngine / AbstractScriptEngine
            │   └── impl/             # SpelScriptEngine
            └── executor/             # ICompiledExecutor
                └── impl/             # SimpleScriptRunnerExecutor
```

## 快速开始

### 1. 启用

```java
@SpringBootApplication
@EnableScriptEngine   // @Import(AutoScriptRegister)：扫描 com.tbs.script 包，注册 Feature.SCRIPT
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

### 2. 编译并执行 SpEL 脚本

```java
@Resource
private IScriptEngine scriptEngine;

public void demo() {
    // 取默认引擎注册的 SpEL 编译器（名称 "SpEL Compiler"，版本 1.0）
    ScriptCompilerSpec spec = scriptEngine
        .getCompilerSpecWithVersion(SpelCompiler.COMPILER_NAME, 1, 0)
        .orElseThrow(() -> new ScriptException("SpEL 编译器未注册"));

    ICompiledExecutor executor = spec.compile("a + b * 2");

    // 通过执行器上下文注入局部变量（执行后上下文会被重置，见"执行语义"）
    executor.getContext().setLocalVariable("a", 1, Integer.class);
    executor.getContext().setLocalVariable("b", 2, Integer.class);

    ScriptResult result = executor.execute();
    if (result.isSuccess()) {
        // result.getResult() = 5, result.getResultClass() = Integer
    }
}
```

### 3. 指定结果类型

SpEL 编译器支持结果类型特性（经编译器特性传递）：

```java
SpelCompiler compiler = new SpelCompiler();
compiler.setResultType(BigDecimal.class);   // ep.getValue(ctx, BigDecimal.class)
```

### 4. 工具类快捷方式

```java
// 手动创建上下文（脱离引擎装配使用）
IScriptContext ctx = ScriptContexts.createSimpleContext();
IScriptContext child = ScriptContexts.createChildContext(ctx);   // 子上下文读父、写隔离

// 执行器执行（异步走 ForkJoinPool.commonPool）
ScriptResult r = ScriptExecutors.execute(executor, ctx);
CompletableFuture<ScriptResult> rf = ScriptExecutors.executeAsync(executor, ctx);
```

## 分层模型

```
IScriptEngine（引擎：编译器注册表）
    │  registerCompilerSpec / getCompilerSpecWithVersion / getAllCompilerSpec
    ▼
ScriptCompilerSpec（编译规范：compiler + meta + contextFactory 三元组，不可变）
    │  compile(script) → compiler.compile(script, contextFactory.createContextForCompiler(meta))
    ▼
ICompiledExecutor（执行器：绑定上下文与编译产物）
    │  execute() / executeAsync() / isExecuting() / getContext() / getCompiler()
    ▼
ScriptResult（结果：success / result / resultClass / errorMsg）
```

### IScriptEngine / AbstractScriptEngine

- 注册键：`compilerName:major.minor`（版本缺省按 `0.0`）；
- `registerCompilerSpec`：meta 与 compiler 非空校验；contextFactory 为 null 时回退 `EmptySimpleScriptContextFactory`；重复注册覆盖并 warn；
- 实现 `IStartup`：应用启动时执行 `importCompilerSpecs()` 导入内置编译器；
- 默认实现 `SpelScriptEngine`：注册 `SpelCompiler`（同时充当 meta 与 compiler）+ 空上下文工厂。

### IScriptCompiler / AbstractScriptCompiler

```java
public interface IScriptCompiler {
    ICompiledExecutor compile(String script, IScriptContext scriptContext) throws ScriptException;
}

public interface IScriptCompilerMeta {
    String getCompilerName();               // 编译器名称
    Integer getMajorVersion();              // 主版本
    Integer getMinorVersion();              // 次版本
    String getScriptLanguage();             // 脚本语言
    ScriptCompilerFeatures getFeatures();   // 编译器特性
}
```

`AbstractScriptCompiler` 同时实现两者；子类只需实现：

```java
// 返回脚本执行逻辑（闭包捕获 script）
protected abstract IScriptRunner getScriptRunner(String script, ScriptCompilerFeatures features);

// 内部函数式接口
public interface IScriptRunner {
    ScriptResult execute(IScriptContext scriptContext);
}
```

`compile()` 模板方法固定包装为 `SimpleScriptRunnerExecutor(context, runner, this)`。

### ScriptCompilerFeatures

编译器特性键值对（`HashMap<String,Object>` 封装，链式 API）：`putFeatureCode` / `putFeatureCodeIfAbsent` / `getFeatureCode(name)`（`Optional<Object>`）/ `getFeatureNames` / `getFeatures`（`KeyValuePair` 集合）。`AbstractScriptCompiler` 持有实例级特性表，SpEL 用它传递结果类型。

### IScriptContext / ScriptScope

```java
public interface IScriptContext extends IContext {   // destroy/rebuild/isValid 生命周期
    <T> void setVariable(String name, T value, Class<T> type, ScriptScope scope);
    void removeVariable(String name, ScriptScope scope);
    <T> Optional<T> getVariable(String name, Class<T> type, ScriptScope scope);
    IScriptContext createChildScriptContext();
    Collection<String> listLocalVariables();
    Collection<String> listGlobalVariables();
    // Local/Global 两套 default 快捷方法
}
```

- `ScriptScope`：`Global("global", 0)` / `Local("local", 1)`，实现 common 的 `BaseEnum<String>`（value 为字符串、code 为 Integer）；
- 默认实现 `SimpleLocalMapSciptContext`（类名拼写如此）：双 `HashMap` 分作用域存储，父子链回退查询（本作用域未命中则递归查父，同一作用域名）；**值为 null 时返回空 Optional**；`destroy()` 清空变量并失效，`rebuild()` 重置有效并清空变量。

### 异常体系

`ScriptException`（RuntimeException）为基类，3 个语义子类：`ScriptCompilationException`（编译失败）、`ScriptExecutionException`（执行失败）、`ScriptContextException`（上下文异常）——均仅 `(String message)` 与 `(String, Throwable)` 构造器。

## SpEL 实现（SpelCompiler）

| 常量 | 值 |
|------|-----|
| `COMPILER_NAME` | `SpEL Compiler` |
| `FEATURE_RESULT_TYPE` | `resultType` |
| `MAJOR_VERSION` / `MINOR_VERSION` | 1 / 0 |
| `SCRIPT_LANGUAGE` | `SpEL` |

执行逻辑（每次执行时解析）：

- `SpelParserConfiguration(SpelCompilerMode.IMMEDIATE, systemClassLoader)`：IMMEDIATE 模式下 SpEL 会将表达式编译为字节码提升重复执行性能；
- `StandardEvaluationContext`：**SpEL 完整能力**（方法调用、构造器、类型引用、属性导航）；
- 仅将上下文**局部变量**注入为表达式变量（`#a`、`#b`），全局变量不注入；
- 未设置结果类型 → `getValue(ctx)`；设置了 → `getValue(ctx, resultType)` 做类型转换；
- 任何求值异常被捕获并转为 `ScriptResult(false, null, null, 异常消息)`——执行失败不抛异常，通过结果对象传递。

## 执行语义（SimpleScriptRunnerExecutor）

- **并发保护**：`AtomicBoolean isExecuting`，执行中重复调用 `execute()` 抛 `SYSTEM_ERROR` 级断言异常（失败也转为 `ScriptResult` 的情况仅限 Runner 内部异常）；
- **单次语义与上下文重置**：`execute()` 的 `finally` 中调用 `resetContext()`——对上下文先 `destroy()` 再 `rebuild()`。**执行结束后上下文变量被清空**，执行器上下文中设置的变量仅对当次执行可见；跨次传递数据请重新设置变量或自建上下文；
- **异步**：`executeAsync()` 默认走 `CompletableFuture.supplyAsync`（ForkJoinPool.commonPool），重入保护同样生效；
- **Runner 异常**：`scriptRunner.execute` 抛出的任何 `Throwable` 被捕获记日志并转为失败 `ScriptResult`，不向外抛出。

## 配置说明

`ScriptEngineProperty`（`@ConfigurationProperties`，**无前缀**）：

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `engine-class` | Class | `SpelScriptEngine` | 脚本引擎类 |

> **当前实现未消费该属性**：`ScriptEngineConfig` 的 `defaultScriptEngine()` 固定经 `BeanUtil.buildByEmptyArgs(SpelScriptEngine.class)` 构建，`engineClass` 属性（及配置类中声明的 property 字段）实际不生效；如需切换引擎请注册自定义 `IScriptEngine` Bean。

## 扩展开发

### 自定义脚本语言编译器

```java
public class GroovyCompiler extends AbstractScriptCompiler {

    @Override
    public String getCompilerName() { return "Groovy Compiler"; }

    @Override
    public Integer getMajorVersion() { return 1; }

    @Override
    public Integer getMinorVersion() { return 0; }

    @Override
    public String getScriptLanguage() { return "Groovy"; }

    @Override
    protected IScriptRunner getScriptRunner(String script, ScriptCompilerFeatures features) {
        return scriptContext -> {
            try {
                // 预编译/求值，从 scriptContext 读取变量注入绑定
                Object result = evalGroovy(script, scriptContext);
                return new ScriptResult(true, result, result != null ? result.getClass() : null, null);
            } catch (Exception e) {
                return new ScriptResult(false, null, null, e.getMessage());
            }
        };
    }
}
```

### 注册到引擎

```java
@Component
public class GroovyScriptEngine extends AbstractScriptEngine {
    @Override
    protected void importCompilerSpecs() {
        GroovyCompiler compiler = new GroovyCompiler();
        registerCompilerSpec(compiler, compiler, new EmptySimpleScriptContextFactory());
    }
}

// 并注册为 Bean 覆盖默认引擎（或在容器中并存，按名称注入）
@Bean
public IScriptEngine groovyScriptEngine() {
    return new GroovyScriptEngine();
}
```

### 自定义上下文工厂

实现 `IScriptContextFactory.createContextForCompiler(meta)`，可为不同编译器提供差异化上下文（如预置全局变量、对接分布式存储）；默认 `EmptySimpleScriptContextFactory` 每次 `compile()` 创建全新根上下文。

## 使用注意事项

1. **SpEL 安全**：`StandardEvaluationContext` 开放 SpEL 全部能力（含 `T()` 类型引用与任意方法调用），执行不可信脚本等于开放任意代码执行；面向外部输入时务必换用受限上下文或白名单校验
2. **执行后上下文重置**：`SimpleScriptRunnerExecutor.execute()` 结束即清空上下文，变量不跨次保留
3. **上下文非线程安全**：`SimpleLocalMapSciptContext` 基于普通 `HashMap`（接口 javadoc 建议实现保证线程安全，当前实现未保证），多线程共享上下文需外部同步或各自持有
4. **异步执行线程池**：`executeAsync` 默认 `ForkJoinPool.commonPool()`，如需与平台线程池打通需自行包装
5. **全局变量不注入 SpEL**：仅 `listLocalVariables()` 会被注入为表达式变量
6. **ScriptExecutors 的 context 参数未使用**：`execute(executor, context)` 的 context 参数当前被忽略，实际使用 executor 内部持有的上下文
7. **同名重复注册仅 warn 覆盖**：注册表中 `名称:版本` 冲突时后注册者生效

## 包结构一览

```
com.tbs.script
├── annotation        # @EnableScriptEngine
├── config            # AutoScriptRegister / ScriptEngineConfig / ScriptEngineProperty
├── model             # ScriptCompilerFeatures / ScriptCompilerSpec / ScriptResult
├── util              # ScriptContexts / ScriptExecutors
├── exception         # ScriptException / ScriptCompilationException / ScriptExecutionException / ScriptContextException
└── api
    ├── compiler      # IScriptCompiler / IScriptCompilerMeta / AbstractScriptCompiler（+ impl.SpelCompiler）
    ├── context       # IScriptContext / IScriptContextFactory / ScriptScope
    │   └── impl      # SimpleLocalMapSciptContext / EmptySimpleScriptContextFactory
    ├── engine        # IScriptEngine / AbstractScriptEngine（+ impl.SpelScriptEngine）
    └── executor      # ICompiledExecutor（+ impl.SimpleScriptRunnerExecutor）
```

## 许可证

本项目采用 Apache License 2.0 许可证，详情请参阅 [LICENSE](../LICENSE) 文件。

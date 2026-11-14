package com.tbs.sql.annotation.core;

import com.tbs.sql.metadata.EntityScanRegistrar;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * 启动期实体元数据扫描注解。
 *
 * <p>标注在启动类或 {@code @Configuration} 类上，Spring 容器刷新期间扫描指定包下带
 * {@link jakarta.persistence.Entity} 或 {@link jakarta.persistence.Table} 的类，
 * 逐个校验注解完整性，并调用
 * {@link com.tbs.sql.metadata.EntityMetadataExtractor#register(Class)} 把
 * {@link com.tbs.sql.metadata.EntityTableMetadata} 提前写入缓存，避免首次写 SQL 时才惰性解析、
 * 也避免非法实体到运行期才暴露（如缺主键、重复列名）。</p>
 *
 * <p>同时预热读 SQL 侧的两级缓存：按类缓存的 {@code SelectInfoModel}，以及由
 * {@link com.tbs.sql.dialect.SqlDialect} 的 SELECT 提取器渲染后写入模型的片段缓存。
 * 预热与运行期使用同一个提取器实例，首次业务查询即可命中，不再重复遍历字段列表。</p>
 *
 * <h3>扫描包配置</h3>
 * <ul>
 *   <li>{@link #value()} 与 {@link #basePackages()} 取并集，均可指定多个包；
 *       支持 {@code ${...}} 占位符与逗号分隔的多个包名</li>
 *   <li>{@link #basePackageClasses()} 以类的所在包作为扫描包，重命名包时不易失效</li>
 *   <li>三者都不填时，默认扫描标注类所在的包</li>
 *   <li>多个配置类分别标注本注解时，扫描包会合并</li>
 * </ul>
 *
 * <h3>启动期校验（任一失败即中断启动，汇总列出所有问题实体）</h3>
 * <ul>
 *   <li>类中只要出现 {@code @Entity}、{@code @Table} 之一，就必须同时标注两者</li>
 *   <li>{@code @Table} 必须显式声明非空的 {@code name}</li>
 *   <li>不能是接口、枚举或抽象类</li>
 *   <li>至少有一个持久化字段，且必须恰好标注一个 {@code @Id}</li>
 *   <li>持久化列名不能重复</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * @EntityScan(basePackages = {"com.tbs.shared.pojo.entity", "com.tbs.dic.entity"})
 * @SpringBootApplication(scanBasePackages = "com.tbs.app")
 * public class AppApplication {
 *     public static void main(String[] args) {
 *         SpringApplication.run(AppApplication.class, args);
 *     }
 * }
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>与 Spring Boot 的 {@code org.springframework.boot.autoconfigure.domain.EntityScan} 无关，
 *       后者服务于 Spring Data JPA，本项目未引入 JPA Provider，请勿混用</li>
 *   <li>只做类路径扫描与元数据预热，不会注册 Bean、不会建表；建表仍由
 *       {@code jkit.sql.auto.packages} 负责</li>
 *   <li>校验在 {@link org.springframework.beans.factory.SmartInitializingSingleton} 阶段执行，
 *       属于启动期 fail-fast；未标注本注解时框架行为与原先完全一致，仍按类惰性加载元数据</li>
 *   <li>片段预热失败只告警、不中断启动；没有 {@code SqlDialect} Bean 时自动跳过</li>
 * </ul>
 *
 * <h3>启动期日志</h3>
 * <p>预热开始、完成汇总（实体数、命中缓存数、耗时）以 INFO 输出，失败明细以 WARN 输出，
 * 逐个实体的片段长度以 DEBUG 输出。需要逐条明细时，把
 * {@code com.tbs.sql.metadata.EntityTableMetadataInitializer} 的日志级别设为 DEBUG。</p>
 *
 * @author abstergo
 * @since 3.0.0
 * @see EntityScanRegistrar
 * @see com.tbs.sql.metadata.EntityMetadataExtractor#register(Class)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
@Import(EntityScanRegistrar.class)
public @interface SqlEntityScan {

    /**
     * 待扫描的包，与 {@link #basePackages()} 取并集。
     *
     * @return 包名数组
     */
    String[] value() default {};

    /**
     * 待扫描的包，支持 {@code ${...}} 占位符与逗号分隔的多个包名。
     *
     * @return 包名数组
     */
    String[] basePackages() default {};

    /**
     * 以指定类所在包作为扫描包，可与上面两个属性并用。
     *
     * @return 标记类数组
     */
    Class<?>[] basePackageClasses() default {};
}

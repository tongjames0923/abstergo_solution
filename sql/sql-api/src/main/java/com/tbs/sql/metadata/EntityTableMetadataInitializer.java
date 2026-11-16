package com.tbs.sql.metadata;

import com.tbs.sql.dialect.ISelectExtractor;
import com.tbs.sql.dialect.SqlDialect;
import com.tbs.sql.mapper.IFrameworkMapper;
import com.tbs.sql.mapper.MapperEntityBindingRegistry;
import com.tbs.sql.model.select.SelectInfoModel;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;

/**
 * 启动期实体元数据初始化器：扫描 {@link com.tbs.sql.annotation.core.SqlEntityScan} 指定的包，
 * 校验实体注解并预热三类元数据。
 *
 * <p>由 {@link EntityScanRegistrar} 注册为基础设施 Bean，在所有非懒加载单例初始化完成后执行，
 * 因此问题实体会在容器刷新阶段（启动期）就暴露，而不是等到第一次执行 INSERT / UPDATE。</p>
 *
 * <h3>预热内容</h3>
 * <ol>
 *   <li>{@link EntityMetadataExtractor#register(Class)}：校验注解完整性并写入
 *       {@code EntityTableMetadata}（写 SQL 用）；</li>
 *   <li>{@link EntityMetadataExtractor#pullFromClass(Class)}：写入按类缓存的
 *       {@code SelectInfoModel}（读 SQL 用）；</li>
 *   <li>用容器里 {@link SqlDialect} 的 SELECT 提取器渲染一次，把结果写入
 *       {@code SelectInfoModel} 的片段缓存。预热与运行期使用同一个提取器实例，
 *       缓存 owner 才能匹配，后续查询直接命中。</li>
 * </ol>
 *
 * <p>扫描基于 ASM 元数据（{@link CachingMetadataReaderFactory}），不加载业务类、不触发静态初始化；
 * 命中候选后才按类名加载并交给 {@link EntityMetadataExtractor#register(Class)} 完成校验与缓存写入。</p>
 *
 * <h3>日志</h3>
 * <p>预热开始、完成汇总与失败明细分别在 INFO / INFO / WARN 输出；逐个实体的明细在 DEBUG。
 * 例如将 {@code com.tbs.sql.metadata.EntityTableMetadataInitializer} 设为 DEBUG 可看到每个实体
 * 的片段长度。片段预热属于纯性能优化，失败只告警、不中断启动。</p>
 *
 * @author abstergo
 * @since 3.0.0
 * @see com.tbs.sql.annotation.core.SqlEntityScan
 * @see EntityScanRegistrar
 */
public class EntityTableMetadataInitializer implements SmartInitializingSingleton, ApplicationContextAware {

    private static final Logger log = LoggerFactory.getLogger(EntityTableMetadataInitializer.class);
    private static final String ENTITY_ANNOTATION = Entity.class.getName();
    private static final String TABLE_ANNOTATION = Table.class.getName();
    private static final String CLASS_PATTERN_SUFFIX = "/**/*.class";

    private final Collection<String> basePackages;
    private ApplicationContext applicationContext;

    public EntityTableMetadataInitializer(Collection<String> basePackages) {
        this.basePackages = basePackages;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterSingletonsInstantiated() {
        Set<String> packages = resolvePackages();
        CachingMetadataReaderFactory readerFactory = new CachingMetadataReaderFactory(applicationContext);
        Set<String> candidates = new LinkedHashSet<>();
        for (String basePackage : packages) {
            collectCandidates(basePackage, readerFactory, candidates);
        }

        List<String> errors = new ArrayList<>();
        List<Class<?>> registered = new ArrayList<>(candidates.size());
        int initialized = 0;
        for (String className : candidates) {
            try {
                Class<?> entityClass = ClassUtils.forName(className, applicationContext.getClassLoader());
                EntityMetadataExtractor.register(entityClass);
                registered.add(entityClass);
                initialized++;
            } catch (ClassNotFoundException | LinkageError | RuntimeException e) {
                String message = e.getMessage();
                errors.add(className + "：" + (StringUtils.hasText(message) ? message : e.getClass().getName()));
            }
        }
        if (!errors.isEmpty()) {
            throw new IllegalStateException("实体表元数据启动期校验失败，共" + errors.size() + "个实体注解不完整：\n  - "
                + String.join("\n  - ", errors));
        }
        log.info("@SqlEntityScan 启动期已校验并预热 {} 个 EntityTableMetadata，扫描包：{}", initialized, packages);
        warmSelectFragments(registered, packages);
        bindMapperEntities(registered, packages);
    }

    /**
     * 启动期把容器内所有 {@link IFrameworkMapper} 与其实体类型绑定，并校验实体是否已被扫描。
     *
     * <p>先用不触发 FactoryBean 初始化的方式枚举 Bean 类型；MyBatis 的 MapperFactoryBean
     * 在部分 Spring 版本下需要允许初始化才能被识别，此时自动回退一次。绑定失败会汇总全部
     * 问题 Mapper 并中断启动，口径与实体元数据校验一致。</p>
     *
     * @param registered 已通过校验并完成元数据预热的实体类
     * @param packages   本次扫描包，用于日志定位
     */
    private void bindMapperEntities(List<Class<?>> registered, Set<String> packages) {
        String[] beanNames = applicationContext.getBeanNamesForType(IFrameworkMapper.class, true, false);
        if (beanNames.length == 0) {
            beanNames = applicationContext.getBeanNamesForType(IFrameworkMapper.class);
            if (beanNames.length > 0) {
                log.debug("@SqlEntityScan 允许初始化方式枚举到 {} 个 Mapper Bean", beanNames.length);
            }
        }
        Set<Class<?>> mapperTypes = new LinkedHashSet<>();
        for (String beanName : beanNames) {
            Class<?> type = applicationContext.getType(beanName);
            if (type != null && type.isInterface() && IFrameworkMapper.class.isAssignableFrom(type)) {
                mapperTypes.add(type);
            }
        }
        if (mapperTypes.isEmpty()) {
            log.warn("@SqlEntityScan 未发现 IFrameworkMapper Bean，跳过 Mapper 与实体绑定");
            return;
        }
        MapperEntityBindingRegistry.bindAll(mapperTypes, new LinkedHashSet<>(registered), true);
        log.info("@SqlEntityScan 已绑定 {} 个 Mapper 与实体，扫描包：{}", mapperTypes.size(), packages);
    }

    /**
     * 启动期预热 SELECT 片段缓存。
     *
     * <p>对每个已通过校验的实体，先 {@code pullFromClass} 写入（并标记可缓存的）
     * {@code SelectInfoModel}，再用容器里 {@link SqlDialect} 持有的 SELECT 提取器渲染一次。
     * 渲染结果由提取器写入模型片段缓存，运行期 {@code QueryEngine} 经同一个方言 Bean
     * 拿到的是同一提取器实例，owner 匹配即可直接命中。</p>
     *
     * <p>用 {@code cachedSelectSql(extractor)} 复核写入是否生效，因此统计里的
     * "命中缓存" 是实际验证过的数量，而非仅仅"渲染成功"。</p>
     *
     * @param entityClasses 已通过启动期校验的实体类
     * @param packages      本次扫描包，用于日志定位
     */
    private void warmSelectFragments(List<Class<?>> entityClasses, Set<String> packages) {
        SqlDialect dialect;
        try {
            dialect = applicationContext.getBeanProvider(SqlDialect.class).getIfAvailable();
        } catch (RuntimeException e) {
            log.warn("@SqlEntityScan SELECT 片段预热跳过：无法获取唯一 SqlDialect Bean（{}），运行期仍会惰性生成",
                    StringUtils.hasText(e.getMessage()) ? e.getMessage() : e.getClass().getName());
            return;
        }
        if (dialect == null) {
            log.info("@SqlEntityScan SELECT 片段预热跳过：未检测到 SqlDialect Bean，运行期仍会惰性生成");
            return;
        }
        ISelectExtractor extractor = dialect.getSelectExtractor();
        log.info("@SqlEntityScan SELECT 片段预热开始：实体 {} 个，方言={}，提取器={}，扫描包={}",
                entityClasses.size(), dialect.name(), extractor.getClass().getSimpleName(), packages);

        long startNanos = System.nanoTime();
        int warmed = 0;
        int skipped = 0;
        int failed = 0;
        List<String> failures = new ArrayList<>();
        for (Class<?> entityClass : entityClasses) {
            try {
                SelectInfoModel model = EntityMetadataExtractor.pullFromClass(entityClass);
                String fragment = extractor.extract(model);
                String cached = model.cachedSelectSql(extractor);
                if (cached == null) {
                    skipped++;
                    log.debug("@SqlEntityScan 片段未写入缓存（模型未标记可缓存）：{}", entityClass.getName());
                    continue;
                }
                warmed++;
                log.debug("@SqlEntityScan 已预热 SELECT 片段：{} -> {} 字符", entityClass.getName(), fragment.length());
            } catch (LinkageError | RuntimeException e) {
                failed++;
                String reason = StringUtils.hasText(e.getMessage()) ? e.getMessage() : e.getClass().getName();
                failures.add(entityClass.getName() + "：" + reason);
                log.warn("@SqlEntityScan 预热 SELECT 片段失败：{}：{}", entityClass.getName(), reason);
            }
        }
        double elapsedMillis = (System.nanoTime() - startNanos) / 1_000_000.0;
        log.info("@SqlEntityScan SELECT 片段预热完成：命中缓存 {} 个，未缓存 {} 个，失败 {} 个，耗时 {} ms",
                warmed, skipped, failed, String.format(Locale.ROOT, "%.2f", elapsedMillis));
        if (!failures.isEmpty()) {
            log.warn("@SqlEntityScan 预热失败明细（不影响启动）：{}", String.join("；", failures));
        }
    }

    /**
     * 解析包配置中的 {@code ${...}} 占位符，并拆分逗号/分号分隔的多个包名。
     *
     * @return 去重后的扫描包
     */
    private Set<String> resolvePackages() {
        Environment environment = applicationContext.getEnvironment();
        Set<String> resolved = new LinkedHashSet<>();
        for (String declared : basePackages) {
            String value = environment.resolveRequiredPlaceholders(declared);
            for (String basePackage : StringUtils.tokenizeToStringArray(value, ",; \t\n")) {
                resolved.add(basePackage);
            }
        }
        return resolved;
    }

    /**
     * 扫描单个包下所有 class 的 ASM 元数据，收集标注了 {@code @Entity} 或 {@code @Table} 的类名。
     *
     * <p>只认直接标注：与运行期 {@code SqlUtils} 的 {@code isAnnotationPresent} 口径一致，
     * 避免组合注解被扫描命中却在生成 SQL 时表名回退。只标其中一个注解的类也会进入候选，
     * 由 {@link EntityMetadataExtractor#register(Class)} 统一报"注解不完整"。</p>
     *
     * @param basePackage   待扫描包
     * @param readerFactory ASM 元数据读取器
     * @param candidates    候选类名集合
     */
    private void collectCandidates(String basePackage, CachingMetadataReaderFactory readerFactory,
        Set<String> candidates) {
        String location = "classpath*:" + basePackage.replace('.', '/') + CLASS_PATTERN_SUFFIX;
        Resource[] resources;
        try {
            resources = applicationContext.getResources(location);
        } catch (IOException e) {
            throw new IllegalStateException("@SqlEntityScan 扫描包[" + basePackage + "]失败", e);
        }
        if (resources.length == 0) {
            log.warn("@SqlEntityScan 扫描包[{}]未找到任何 class 资源，请确认包名是否正确", basePackage);
        }
        for (Resource resource : resources) {
            if (!resource.isReadable()) {
                continue;
            }
            MetadataReader metadataReader;
            try {
                metadataReader = readerFactory.getMetadataReader(resource);
            } catch (IOException e) {
                throw new IllegalStateException("@SqlEntityScan 读取实体类元数据失败：" + resource, e);
            }
            AnnotationMetadata metadata = metadataReader.getAnnotationMetadata();
            if (metadata.hasAnnotation(ENTITY_ANNOTATION) || metadata.hasAnnotation(TABLE_ANNOTATION)) {
                candidates.add(metadata.getClassName());
            }
        }
    }
}

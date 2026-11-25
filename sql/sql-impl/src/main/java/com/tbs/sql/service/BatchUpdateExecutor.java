package com.tbs.sql.service;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.i18n.I18nUtil;
import com.tbs.sql.metadata.EntityColumnMetadata;
import com.tbs.sql.metadata.EntityMetadataExtractor;
import com.tbs.sql.metadata.EntityTableMetadata;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.binding.MapperProxy;
import org.apache.ibatis.executor.BatchResult;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.sql.Statement;
import java.util.*;

/**
 * 批量更新执行器实现。
 *
 * <p>以 {@code ExecutorType.BATCH} 原生会话逐行复用当前业务 Mapper 命名空间下的单条更新语句
 * （语句 id 为 {@code 子接口全名.updateByPrimaryKey(.Selective)}，由 Mapper 代理解析命名空间得到，
 * 与直连调用完全一致）。不经 {@code SqlSessionUtils}，避免 Spring 事务内 SIMPLE/BATCH 执行器类型
 * 冲突；连接由 mybatis-spring 的 SpringManagedTransaction 提供：存在 Spring 事务时即事务连接，
 * 无事务时为独立连接，按连接自身的 autoCommit 语义执行。</p>
 *
 * <h3>行为口径</h3>
 * <ul>
 *   <li>受影响行数取自 flushStatements 的 BatchResult 求和：正数累加，SUCCESS_NO_INFO(-2) 按 1 计，
 *       其余负值按 0，防止驱动差异污染总数；不取逐行返回值（BATCH 执行器下为无意义的哨兵值）</li>
 *   <li>每 {@link #DEFAULT_FLUSH_SIZE} 行 flush 一次，控制 JDBC 批内存占用与单次网络往返规模</li>
 *   <li>非原子：无事务时逐 flush 片提交，中途失败不回滚已提交分片；需要整体回滚时由调用方开事务</li>
 *   <li>记录允许不同实体类型混排（provider 按运行时类型解析语句），校验按各自类型执行</li>
 * </ul>
 *
 * @author abstergo
 * @since 3.0.0
 * @see IBatchUpdateExecutor
 */
@Slf4j
public class BatchUpdateExecutor implements IBatchUpdateExecutor {

    /** MapperProxy#mapperInterface 反射字段缓存，避免每次批量调用重复查找 */
    private static volatile Field mapperInterfaceField;

    private final SqlSessionFactory sqlSessionFactory;

    /**
     * 构造函数，注入 MyBatis 会话工厂。
     *
     * @param sqlSessionFactory MyBatis 会话工厂
     */
    public BatchUpdateExecutor(SqlSessionFactory sqlSessionFactory) {
        AssertUtil.notNull(sqlSessionFactory, "SqlSessionFactory不能为空");
        this.sqlSessionFactory = sqlSessionFactory;
    }

    /** {@inheritDoc} */
    @Override
    public int updateByPrimaryKeyList(Object mapperProxy, List<?> records) {
        return execute(mapperProxy, records, false);
    }

    /** {@inheritDoc} */
    @Override
    public int updateByPrimaryKeySelectiveList(Object mapperProxy, List<?> records) {
        return execute(mapperProxy, records, true);
    }

    /**
     * 批量执行更新：校验 → 解析语句命名空间 → 打开 BATCH 会话 → 逐行复用语句 → 分片 flush → 提交。
     *
     * @param mapperProxy 当前 Mapper 代理对象
     * @param records     待更新实体列表
     * @param selective   true 时仅更新非空字段
     * @return 受影响行数
     */
    private int execute(Object mapperProxy, List<?> records, boolean selective) {
        if (records == null || records.isEmpty()) {
            return 0;
        }
        validate(records);
        String namespace = resolveMapperNamespace(mapperProxy);
        String statementId = namespace + (selective ? ".updateByPrimaryKeySelective" : ".updateByPrimaryKey");
        int affected = 0;
        int pending = 0;
        SqlSession session = sqlSessionFactory.openSession(ExecutorType.BATCH);
        try {
            for (Object record : records) {
                session.update(statementId, Collections.singletonMap("entity", record));
                if (++pending >= DEFAULT_FLUSH_SIZE) {
                    affected += flush(session);
                    pending = 0;
                }
            }
            if (pending > 0) {
                affected += flush(session);
            }
            session.commit();
            log.debug("批量更新完成，记录数{}，受影响行数{}，语句{}", records.size(), affected, statementId);
            return affected;
        } catch (RuntimeException e) {
            session.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    /**
     * 从 Mapper 代理解析业务子接口全名，作为批量语句的命名空间。
     *
     * <p>单条更新语句声明在 {@code GenericMapper} 上、注册在业务子接口命名空间下，BATCH 会话需要
     * 完整语句 id 才能逐行复用它们；default 方法内的 {@code this} 即 Mapper 代理，经
     * {@link MapperProxy#mapperInterface} 取回接口类型。</p>
     *
     * @param mapperProxy 当前 Mapper 代理对象
     * @return 业务子接口全名
     */
    private String resolveMapperNamespace(Object mapperProxy) {
        AssertUtil.notNull(mapperProxy, "mapperProxy不能为空，请通过MyBatis Mapper代理调用批量更新方法");
        try {
            InvocationHandler handler = Proxy.getInvocationHandler(mapperProxy);
            if (!(handler instanceof MapperProxy)) {
                AssertUtil.throwError(() -> new IllegalStateException(
                    I18nUtil.get("sql.batch.caller.not.mapper.proxy", handler.getClass().getName())));
            }
            Field field = mapperInterfaceField;
            if (field == null) {
                field = MapperProxy.class.getDeclaredField("mapperInterface");
                field.setAccessible(true);
                mapperInterfaceField = field;
            }
            return ((Class<?>)field.get(handler)).getName();
        } catch (ReflectiveOperationException | IllegalArgumentException e) {
            IllegalStateException error = new IllegalStateException(
                I18nUtil.get("sql.batch.mapper.type.unresolved"), e);
            AssertUtil.throwError(() -> error);
            throw error;
        }
    }

    /**
     * 执行并汇总当前累计批次的受影响行数。
     *
     * @param session 批量会话
     * @return 本批受影响行数
     */
    private int flush(SqlSession session) {
        int affected = 0;
        for (BatchResult result : session.flushStatements()) {
            for (int count : result.getUpdateCounts()) {
                // SUCCESS_NO_INFO(-2)：驱动未上报具体行数，按 1 计；其余负值防御为 0
                affected += count == Statement.SUCCESS_NO_INFO ? 1 : Math.max(count, 0);
            }
        }
        return affected;
    }

    /**
     * 逐条校验：记录非空、主键非空（带下标）、同一调用内主键不重复（携带重复主键值）。
     *
     * @param records 待更新实体列表，非空
     */
    private void validate(List<?> records) {
        Set<Object> seenIds = new HashSet<>(records.size());
        Map<Class<?>, EntityTableMetadata> metadataCache = new HashMap<>(4);
        for (int i = 0; i < records.size(); i++) {
            Object record = records.get(i);
            AssertUtil.notNull(record, "批量更新的records第" + i + "条记录不能为null");
            EntityTableMetadata metadata = metadataCache.computeIfAbsent(record.getClass(),
                EntityMetadataExtractor::pullTableMetadata);
            EntityColumnMetadata idColumn = requireIdColumn(metadata);
            Object id = readId(idColumn, record);
            AssertUtil.notNull(id, "批量更新" + metadata.getTableName() + "时第" + i + "条记录的主键"
                + idColumn.getProperty() + "不能为空");
            AssertUtil.isTrue(!seenIds.add(id), "批量更新" + metadata.getTableName() + "时存在重复主键" + id
                + "，同批重复记录将按执行顺序相互覆盖，请检查入参");
        }
    }

    /**
     * 获取主键列，实体未标注主键时中断执行。
     *
     * @param metadata 实体表元数据
     * @return 主键列元数据
     */
    private EntityColumnMetadata requireIdColumn(EntityTableMetadata metadata) {
        EntityColumnMetadata idColumn = metadata.getIdColumn();
        AssertUtil.notNull(idColumn, "实体类" + metadata.getEntityClass().getName() + "没有标注@Id注解");
        return idColumn;
    }

    /**
     * 反射读取实体主键值，反射字段已在元数据提取阶段开放访问权限。
     *
     * @param idColumn 主键列元数据
     * @param entity   实体对象
     * @return 主键值
     */
    private Object readId(EntityColumnMetadata idColumn, Object entity) {
        try {
            return idColumn.getField().get(entity);
        } catch (IllegalAccessException e) {
            IllegalStateException error = new IllegalStateException(
                I18nUtil.get("sql.entity.id.read.failed", idColumn.getProperty()), e);
            AssertUtil.throwError(() -> error);
            throw error;
        }
    }
}

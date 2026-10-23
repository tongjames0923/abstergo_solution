package com.tbs.sql.mapper;

import com.tbs.common.util.framework.SpringUtil;
import com.tbs.sql.dialect.IInsertExtractor;
import com.tbs.sql.dialect.SqlDialect;
import com.tbs.sql.mapper.impl.GenericSqlProvider;
import com.tbs.sql.metadata.EntityMetadataExtractor;
import org.apache.ibatis.annotations.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 通用 Mapper 接口，提供单表增删改查能力，替代外部通用 Mapper 框架。
 *
 * <p>实体映射沿用 {@code jakarta.persistence} 注解：{@code @Table} 提供表名、{@code @Column}
 * 提供列名、{@code @Id} 标识主键、{@code @Transient} 排除非持久化字段。SQL 在运行时由
 * {@link GenericSqlProvider} 基于 {@code ProviderContext} 解析泛型实参后生成，因此子接口
 * 无需声明任何方法即可获得全部能力。</p>
 *
 * <h3>跨库兼容</h3>
 * <ul>
 *   <li>单行 INSERT / UPDATE / DELETE 在 MySQL、PostgreSQL、Oracle、SQL Server 上语法一致</li>
 *   <li>批量 INSERT 的语法差异由 {@link IInsertExtractor} 按方言处理</li>
 *   <li>批量方法的容量上限按方言声明，超出时自动分批，调用方无需关心</li>
 * </ul>
 *
 * <h3>使用方式</h3>
 * <pre>{@code
 * public interface SystemUserMapper extends GenericMapper<SystemUserEntity, Long> {
 * }
 *
 * SystemUserEntity entity = new SystemUserEntity();
 * mapper.insert(entity);
 * // 主键由 @SqlAutoValue 在插入前填充，插入后直接可用
 * Long id = entity.getId();
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>主键由应用侧生成（雪花），本接口不开启数据库主键回填</li>
 *   <li>{@link #insertList}、{@link #selectByIdList}、{@link #deleteByIdList} 收到空集合时直接返回，
 *       不会生成非法的空 IN 或空 VALUES 语句</li>
 *   <li>分批方法按方言上限拆成多次执行，不是原子操作；需要整体回滚时由调用方开启事务，
 *       业务上明确容忍部分成功的批量写入（日志/埋点）可不开事务，但需在写入处注释说明理由</li>
 *   <li>{@link #select} 与 {@link #delete} 只按非空、非空串且非集合类型的字段拼接条件</li>
 * </ul>
 *
 * @param <T> 实体类型
 * @param <K> 主键类型
 * @author abstergo
 * @see GenericSqlProvider
 * @since 3.0.0
 */
public interface GenericMapper<T, K> {

    /**
     * 单条数据库方言的 IN 列表容量上限。
     *
     * <p>取四库最保守值：Oracle 的 IN 列表硬限制为 1000 项（ORA-01795），
     * 其余三库在该规模下均无压力，统一按此值分批可避免按方言维护额外配置。</p>
     */
    int MAX_IN_ITEMS = 1000;

    /**
     * 插入单条记录，包含全部持久化字段。
     *
     * @param entity 待插入实体
     * @return 受影响行数
     */
    @InsertProvider(type = GenericSqlProvider.class, method = "insert")
    int insert(@Param("entity") T entity);

    /**
     * 插入单条记录，仅包含非空字段。
     *
     * @param entity 待插入实体
     * @return 受影响行数
     */
    @InsertProvider(type = GenericSqlProvider.class, method = "insertSelective")
    int insertSelective(@Param("entity") T entity);

    /**
     * 批量插入记录，按方言容量上限自动分批。
     *
     * <p>超出单条SQL上限时拆成多次执行，<b>不是原子操作</b>：中途失败会留下已提交的前几批数据。
     * 需要整体成功或整体回滚时，调用方必须在本方法外层开启事务。</p>
     *
     * @param records 待插入实体列表，为空时直接返回0
     * @return 受影响行数
     */
    default int insertList(List<? extends T> records) {
        if (records == null || records.isEmpty()) {
            return 0;
        }
        IInsertExtractor extractor = SpringUtil.getBean(SqlDialect.class).getInsertExtractor();
        int columnCount = EntityMetadataExtractor.pullTableMetadata(records.get(0).getClass()).getColumns().size();
        int rowsPerBatch = extractor.maxRows();
        if (columnCount > 0) {
            rowsPerBatch = Math.min(rowsPerBatch, Math.max(1, extractor.maxParameters() / columnCount));
        }
        int affected = 0;
        for (int from = 0; from < records.size(); from += rowsPerBatch) {
            int to = Math.min(from + rowsPerBatch, records.size());
            affected += insertBatch(new ArrayList<>(records.subList(from, to)));
        }
        return affected;
    }

    /**
     * 批量插入单批记录，由 {@link #insertList} 按上限拆分后调用。
     *
     * @param records 单批待插入实体列表，行数与参数数必须在上限内且不得为空，业务侧请使用 {@link #insertList}
     * @return 受影响行数
     */
    @InsertProvider(type = GenericSqlProvider.class, method = "insertBatch")
    int insertBatch(@Param("records") List<? extends T> records);

    /**
     * 按主键查询单条记录。
     *
     * @param id 主键值
     * @return 实体对象，不存在时返回null
     */
    @SelectProvider(type = GenericSqlProvider.class, method = "selectByPrimaryKey")
    T selectByPrimaryKey(@Param("id") Object id);

    /**
     * 按主键批量查询，按 IN 列表上限自动分批后合并结果。
     *
     * <p>分批查询之间不构成一致性快照，分批期间数据变更可能被部分命中。</p>
     *
     * @param idList 主键值列表，为空时返回空列表
     * @return 实体列表
     */
    default List<T> selectByIdList(List<K> idList) {
        if (idList == null || idList.isEmpty()) {
            return new ArrayList<>();
        }
        List<T> result = new ArrayList<>(idList.size());
        for (int from = 0; from < idList.size(); from += MAX_IN_ITEMS) {
            int to = Math.min(from + MAX_IN_ITEMS, idList.size());
            result.addAll(selectByIdListBatch(new ArrayList<>(idList.subList(from, to))));
        }
        return result;
    }

    /**
     * 按主键批量查询单批记录，由 {@link #selectByIdList} 按上限拆分后调用。
     *
     * @param idList 单批主键值列表，数量必须在 {@link #MAX_IN_ITEMS} 以内
     * @return 实体列表
     */
    @SelectProvider(type = GenericSqlProvider.class, method = "selectByIdListBatch")
    List<T> selectByIdListBatch(@Param("idList") List<K> idList);

    /**
     * 按实体非空字段等值查询。
     *
     * @param entity 查询条件实体，全部字段为空时查询全表
     * @return 实体列表
     */
    @SelectProvider(type = GenericSqlProvider.class, method = "select")
    List<T> select(@Param("entity") T entity);

    /**
     * 按实体非空字段等值统计。
     *
     * @param entity 查询条件实体，全部字段为空时统计全表
     * @return 记录数
     */
    @SelectProvider(type = GenericSqlProvider.class, method = "selectCount")
    int selectCount(@Param("entity") T entity);

    /**
     * 按主键更新全部持久化字段。
     *
     * @param entity 待更新实体，主键必须非空
     * @return 受影响行数
     */
    @UpdateProvider(type = GenericSqlProvider.class, method = "updateByPrimaryKey")
    int updateByPrimaryKey(@Param("entity") T entity);

    /**
     * 按主键更新非空字段。
     *
     * @param entity 待更新实体，主键必须非空
     * @return 受影响行数
     */
    @UpdateProvider(type = GenericSqlProvider.class, method = "updateByPrimaryKeySelective")
    int updateByPrimaryKeySelective(@Param("entity") T entity);

    /**
     * 按主键删除记录。
     *
     * @param id 主键值
     * @return 受影响行数
     */
    @DeleteProvider(type = GenericSqlProvider.class, method = "deleteByPrimaryKey")
    int deleteByPrimaryKey(@Param("id") Object id);

    /**
     * 按实体非空字段等值删除。
     *
     * @param entity 删除条件实体，全部字段为空时删除全表
     * @return 受影响行数
     */
    @DeleteProvider(type = GenericSqlProvider.class, method = "delete")
    int delete(@Param("entity") T entity);

    /**
     * 按主键批量删除，按 IN 列表上限自动分批。
     *
     * <p>超出单条SQL上限时拆成多次执行，<b>不是原子操作</b>：中途失败会留下已删除的前几批数据。
     * 需要整体成功或整体回滚时，调用方必须在本方法外层开启事务。</p>
     *
     * @param idList 主键值列表，为空时直接返回0
     * @return 受影响行数
     */
    default int deleteByIdList(List<K> idList) {
        if (idList == null || idList.isEmpty()) {
            return 0;
        }
        int affected = 0;
        for (int from = 0; from < idList.size(); from += MAX_IN_ITEMS) {
            int to = Math.min(from + MAX_IN_ITEMS, idList.size());
            affected += deleteByIdListBatch(new ArrayList<>(idList.subList(from, to)));
        }
        return affected;
    }

    /**
     * 按主键批量删除单批记录，由 {@link #deleteByIdList} 按上限拆分后调用。
     *
     * @param idList 单批主键值列表，数量必须在 {@link #MAX_IN_ITEMS} 以内且不得为空，业务侧请使用
     *                {@link #deleteByIdList}
     * @return 受影响行数
     */
    @DeleteProvider(type = GenericSqlProvider.class, method = "deleteByIdListBatch")
    int deleteByIdListBatch(@Param("idList") List<K> idList);
}

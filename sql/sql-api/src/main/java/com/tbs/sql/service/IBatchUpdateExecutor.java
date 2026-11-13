package com.tbs.sql.service;

import java.util.List;

/**
 * 批量更新执行器契约。
 *
 * <p>以 JDBC BATCH 会话（{@code ExecutorType.BATCH}）逐行复用当前业务 Mapper 命名空间下由
 * {@code GenericMapper} 声明的单条更新语句——语句 id 为 {@code 子接口全名.updateByPrimaryKey(.Selective)}，
 * 与直连调用完全一致，SQL 生成层与方言模块零改动。按 flush 片聚合网络往返；受影响行数取自
 * {@code flushStatements()} 返回的 {@code BatchResult} 求和，不取逐行返回值（BATCH 执行器下为
 * 无意义的哨兵值）。</p>
 *
 * <p>该路径与 {@link IQueryEngine} 组件委托同构：SQL 语句全部声明在 {@code GenericMapper} 一个接口，
 * 会话编排收敛在本组件，与 {@code insertList} 等批量方法的校验口径、非原子语义保持一致。</p>
 *
 * <h3>事务语义</h3>
 * <ul>
 *   <li>连接经 mybatis-spring 的 {@code SpringManagedTransaction} 获取：存在 Spring 事务时即事务连接，
 *       提交回滚由外层事务统一裁决；无事务时为独立连接，执行完毕后提交</li>
 *   <li>逐 flush 片提交（非原子）：需要整体成功或整体回滚时，调用方须在外层开启事务</li>
 * </ul>
 *
 * <h3>校验口径</h3>
 * <ul>
 *   <li>空列表直接返回 0</li>
 *   <li>每条记录必须非 null 且主键非空，断言信息带下标</li>
 *   <li>同一调用内不允许出现重复主键，断言中断并携带重复主键值</li>
 * </ul>
 *
 * @author abstergo
 * @since 3.0.0
 */
public interface IBatchUpdateExecutor {

    /**
     * 每次 flushStatements 的累计行数上限，控制 JDBC 批处理内存占用与网络往返粒度。
     */
    int DEFAULT_FLUSH_SIZE = 1000;

    /**
     * 按实体主键批量全字段更新，等价逐行 {@code updateByPrimaryKey}。
     *
     * <p>存在 Spring 事务时自动参与当前事务；无事务时逐 flush 片提交，非原子。</p>
     *
     * @param mapperProxy 当前 Mapper 代理对象，即 default 方法内的 {@code this}，
     *                    执行器据此解析业务子接口命名空间
     * @param records     待更新实体列表，为空时直接返回 0
     * @return 受影响行数
     */
    int updateByPrimaryKeyList(Object mapperProxy, List<?> records);

    /**
     * 按实体主键批量选择性更新，等价逐行 {@code updateByPrimaryKeySelective}，null 字段保留库内现值。
     *
     * <p>存在 Spring 事务时自动参与当前事务；无事务时逐 flush 片提交，非原子。</p>
     *
     * @param mapperProxy 当前 Mapper 代理对象，即 default 方法内的 {@code this}，
     *                    执行器据此解析业务子接口命名空间
     * @param records     待更新实体列表，为空时直接返回 0
     * @return 受影响行数
     */
    int updateByPrimaryKeySelectiveList(Object mapperProxy, List<?> records);
}

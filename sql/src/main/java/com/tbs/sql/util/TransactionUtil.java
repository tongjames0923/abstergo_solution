package com.tbs.sql.util;

import com.tbs.common.constant.CommonI18nConstant;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.i18n.I18nUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.*;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * 事务工具类，提供事务管理的工具方法。
 *
 * <p>该类封装了Spring事务管理的功能，提供了在事务中执行业务逻辑的方法。
 * 支持自定义事务传播行为和事务定义。</p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>在事务中执行业务逻辑 {@link #executeInTransaction(TransactionDefinition, Runnable)}</li>
 *   <li>使用传播行为执行事务 {@link #executeTransaction(int, Runnable)}</li>
 *   <li>使用默认传播行为执行事务 {@link #executeTransaction(Runnable)}</li>
 * </ul>
 *
 * <h3>使用场景</h3>
 * <ul>
 *   <li>需要编程式事务管理时</li>
 *   <li>需要在事务中执行多个操作时</li>
 *   <li>需要自定义事务传播行为时</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 在事务中执行业务逻辑
 * TransactionUtil.getInstance().executeTransaction(() -> {
 *     // 执行数据库操作
 *     userMapper.insert(user);
 *     orderMapper.insert(order);
 * });
 * 
 * // 使用自定义传播行为
 * TransactionUtil.getInstance().executeTransaction(
 *     TransactionDefinition.PROPAGATION_REQUIRES_NEW,
 *     () -> {
 *         // 执行数据库操作
 *     }
 * );
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>异常会被记录并重新抛出</li>
 *   <li>事务失败会自动回滚</li>
 *   <li>使用单例模式</li>
 * </ul>
 *
 * @author abstergo
 * @since 1.0.0
 * @see PlatformTransactionManager
 */
public class TransactionUtil {
    private final PlatformTransactionManager transactionManager;
    private static final Logger logger = LoggerFactory.getLogger(TransactionUtil.class);
    private static volatile TransactionUtil INSTANCE;

    /**
     * 获取事务工具类实例。
     *
     * @return 事务工具类实例
     */
    public static TransactionUtil getInstance() { return INSTANCE; }

    /**
     * 构造函数，初始化事务工具类。
     *
     * @param transactionManager Spring事务管理器
     */
    public TransactionUtil(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
        INSTANCE = this;
    }

    /**
     * 在事务中执行业务逻辑。
     *
     * @param transactionAttributes 事务定义属性，不能为null
     * @param businessLogic 业务逻辑，不能为null
     */
    public void executeInTransaction(TransactionDefinition transactionAttributes, Runnable businessLogic) {
        AssertUtil.isTrue(transactionAttributes == null || businessLogic == null, "Transaction attributes and business logic must not be null");
        TransactionStatus status = null;
        try {
            DefaultTransactionDefinition def = new DefaultTransactionDefinition(transactionAttributes);
            status = transactionManager.getTransaction(def);
            businessLogic.run();
            transactionManager.commit(status);
        } catch (TransactionSystemException tse) { logger.error("TransactionSystemException: {}", tse.getMessage(), tse); rollbackTransaction(status); AssertUtil.throwError(() -> tse); }
        catch (TransactionException te) { logger.error("TransactionException: {}", te.getMessage(), te); rollbackTransaction(status); AssertUtil.throwError(() -> te); }
        catch (Exception e) { logger.error("General exception: {}", e.getMessage(), e); rollbackTransaction(status); AssertUtil.throwError(() -> new FormattedRuntimeException(e, I18nUtil.get(CommonI18nConstant.COMMON_SERVER_ERROR))); }
    }

    /**
     * 使用指定的传播行为执行事务。
     *
     * @param propagationBehavior 事务传播行为，如 TransactionDefinition.PROPAGATION_REQUIRED
     * @param businessLogic 业务逻辑，不能为null
     */
    public void executeTransaction(int propagationBehavior, Runnable businessLogic) {
        AssertUtil.notNull(businessLogic, "Business logic must not be null");
        executeInTransaction(new DefaultTransactionDefinition(propagationBehavior), businessLogic);
    }

    /**
     * 使用默认传播行为（REQUIRED）执行事务。
     *
     * @param businessLogic 业务逻辑，不能为null
     */
    public void executeTransaction(Runnable businessLogic) {
        executeTransaction(TransactionDefinition.PROPAGATION_REQUIRED, businessLogic);
    }

    /**
     * 回滚事务。
     *
     * @param status 事务状态，如果为null则不执行任何操作
     */
    private void rollbackTransaction(TransactionStatus status) {
        if (status != null) { try { transactionManager.rollback(status); } catch (Exception e) { logger.error("Failed to rollback: {}", e.getMessage(), e); } }
    }
}

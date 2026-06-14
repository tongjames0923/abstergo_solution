package com.tbs.sql.util;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.exception.FormattedRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.*;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * 事务工具类
 *
 * @author abstergo
 * @since 1.0.0
 */
public class TransactionUtil {
    private final PlatformTransactionManager transactionManager;
    private static final Logger logger = LoggerFactory.getLogger(TransactionUtil.class);
    private static volatile TransactionUtil INSTANCE;

    public static TransactionUtil getInstance() { return INSTANCE; }

    public TransactionUtil(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
        INSTANCE = this;
    }

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
        catch (Exception e) { logger.error("General exception: {}", e.getMessage(), e); rollbackTransaction(status); AssertUtil.throwError(() -> new FormattedRuntimeException(e, e.getMessage())); }
    }

    public void executeTransaction(int propagationBehavior, Runnable businessLogic) {
        AssertUtil.notNull(businessLogic, "Business logic must not be null");
        executeInTransaction(new DefaultTransactionDefinition(propagationBehavior), businessLogic);
    }

    public void executeTransaction(Runnable businessLogic) {
        executeTransaction(TransactionDefinition.PROPAGATION_REQUIRED, businessLogic);
    }

    private void rollbackTransaction(TransactionStatus status) {
        if (status != null) { try { transactionManager.rollback(status); } catch (Exception e) { logger.error("Failed to rollback: {}", e.getMessage(), e); } }
    }
}

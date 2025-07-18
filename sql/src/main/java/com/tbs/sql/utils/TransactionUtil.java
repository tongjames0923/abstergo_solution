package com.tbs.sql.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.*;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * 事务工具类
 *
 * @author abstergo
 */
public class TransactionUtil {

    private final PlatformTransactionManager transactionManager;

    private static final Logger logger = LoggerFactory.getLogger(TransactionUtil.class);

    private static TransactionUtil INSTANCE;

    public static TransactionUtil getInstance() {
        return INSTANCE;
    }

    /**
     * 通过构造器注入事务管理器
     */
    public TransactionUtil(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
        INSTANCE = this;
    }

    /**
     * 执行事务的方法，允许用户传入自定义的事务参数
     *
     * @param businessLogic         业务逻辑
     * @param transactionAttributes 事务参数
     */
    public void executeInTransaction(TransactionDefinition transactionAttributes, Runnable businessLogic) {
        TransactionStatus status = null;
        try {
            // 创建事务定义对象，这里使用用户提供的参数
            DefaultTransactionDefinition def = new DefaultTransactionDefinition(transactionAttributes);

            // 开启事务
            status = transactionManager.getTransaction(def);
            // 执行业务逻辑
            businessLogic.run();

            // 如果业务逻辑执行成功，提交事务
            transactionManager.commit(status);
        } catch (TransactionSystemException tse) { // 捕获事务系统异常
            logger.error("TransactionSystemException occurred: {}", tse.getMessage(), tse);
            if (status != null) {
                transactionManager.rollback(status);
            }
            throw tse;
        } catch (TransactionException te) { // 捕获事务异常
            logger.error("TransactionException occurred: {}", te.getMessage(), te);
            if (status != null) {
                transactionManager.rollback(status);
            }
            throw te;
        } catch (Exception e) { // 捕获其他异常
            logger.error("General exception occurred: {}", e.getMessage(), e);
            if (status != null) {
                transactionManager.rollback(status);
            }
            throw e;
        }
    }

    /**
     * 执行事务的方法，允许用户传入Propagation的事务参数 propagationBehavior见{@link  TransactionDefinition#PROPAGATION_REQUIRED}等
     *
     * @param propagationBehavior
     * @param businessLogic
     */
    public void executeTransaction(int propagationBehavior, Runnable businessLogic) {
        executeInTransaction(new DefaultTransactionDefinition(propagationBehavior), businessLogic);
    }

    /**
     * 执行事务的方法，默认使用PROPAGATION_REQUIRED
     *
     * @param businessLogic
     */
    public void executeTransaction(Runnable businessLogic) {
        executeTransaction(TransactionDefinition.PROPAGATION_REQUIRED, businessLogic);
    }
}

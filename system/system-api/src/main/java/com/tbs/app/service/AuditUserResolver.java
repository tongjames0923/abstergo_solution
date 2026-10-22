package com.tbs.app.service;

import com.tbs.app.service.user.ISystemUserService;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.web.uitl.AuthUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * 审计主体解析器
 * <p>
 * 统一「当前操作人」的解析口径，按优先级取值：显式声明（{@link #runAs} / {@link #callAs}）
 * → 登录态上下文 → 系统主体 {@link #SYSTEM_USER_ID}。非登录态上下文（定时任务、消息消费、
 * 登录自动建号等）归为系统主体，与真实用户ID区分，不再留空。
 * </p>
 * <p>
 * 审计字段填充失败不阻断业务写入，解析异常一律降级为系统主体并记录告警日志。
 * </p>
 *
 * @author abstergo
 */
@Slf4j
public final class AuditUserResolver {

    /**
     * 系统主体用户ID
     * <p>
     * 表示写入由系统自身发起，不对应 sys_user 表中的真实用户；用户主键自增与雪花策略
     * 均从 1 起始，0 不会与任何真实用户冲突。
     * </p>
     */
    public static final Long SYSTEM_USER_ID = 0L;

    /**
     * 当前线程显式声明的审计主体用户ID，{@code null} 表示未声明
     */
    private static final ThreadLocal<Long> OVERRIDE_USER_ID = new ThreadLocal<>();

    private AuditUserResolver() {
    }

    /**
     * 解析当前审计主体用户ID
     *
     * @return 当前登录用户ID；非登录态或解析失败返回 {@link #SYSTEM_USER_ID}
     */
    public static Long resolveCurrentUserId() {
        Long overrideUserId = OVERRIDE_USER_ID.get();
        if (overrideUserId != null) {
            return overrideUserId;
        }
        try {
            var authUtil = AuthUtil.getInstance();
            if (authUtil == null || !authUtil.isUserLoggedIn()) {
                return SYSTEM_USER_ID;
            }
            var systemUserService = SpringUtil.getBeanOptional(ISystemUserService.class);
            if (systemUserService.isEmpty()) {
                log.warn("系统用户服务未初始化，审计主体降级为系统主体");
                return SYSTEM_USER_ID;
            }
            var userId = CollUtil.getFirst(
                systemUserService.get().queryUserIdsByUserName(authUtil.getCurrentUser().getUsername()));
            return userId == null ? SYSTEM_USER_ID : userId;
        } catch (RuntimeException e) {
            log.warn("解析当前审计主体失败，降级为系统主体: {}", e.getMessage());
            return SYSTEM_USER_ID;
        }
    }

    /**
     * 以指定用户身份执行任务，执行结束后恢复原有声明
     * <p>
     * 用于有明确操作人但线程上下文缺失的场景：消息消费还原发起人、定时任务以系统身份运行、
     * 测试模拟指定用户。优先级高于登录态，支持嵌套调用，内层结束后恢复外层声明；
     * 仅对当前线程生效，不随线程池任务传播，异步任务需在工作线程内重新声明。
     * </p>
     *
     * @param userId 审计主体用户ID，不能为空
     * @param action 待执行任务
     */
    public static void runAs(Long userId, Runnable action) {
        Objects.requireNonNull(action, "action cannot be null");
        callAs(userId, () -> {
            action.run();
            return Boolean.TRUE;
        });
    }

    /**
     * 以指定用户身份执行任务并返回结果，执行结束后恢复原有声明
     * <p>
     * 声明与恢复规则同 {@link #runAs(Long, Runnable)}。
     * </p>
     *
     * @param userId 审计主体用户ID，不能为空
     * @param action 待执行任务
     * @param <T>    任务返回值类型
     * @return 任务执行结果
     */
    public static <T> T callAs(Long userId, Supplier<T> action) {
        Objects.requireNonNull(userId, "userId cannot be null");
        Objects.requireNonNull(action, "action cannot be null");
        Long previousUserId = OVERRIDE_USER_ID.get();
        OVERRIDE_USER_ID.set(userId);
        try {
            return action.get();
        } finally {
            if (previousUserId == null) {
                OVERRIDE_USER_ID.remove();
            } else {
                OVERRIDE_USER_ID.set(previousUserId);
            }
        }
    }
}

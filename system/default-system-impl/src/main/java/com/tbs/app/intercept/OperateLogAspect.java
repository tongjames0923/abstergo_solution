package com.tbs.app.intercept;

import com.tbs.logging.annotation.OperateLog;
import com.tbs.app.config.AppSettingProperty;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.error.IExceptionLevel;
import com.tbs.common.util.base.JsonUtils;
import com.tbs.logging.core.ILogRecorder;
import com.tbs.logging.core.ILoggingContextApplier;
import com.tbs.logging.core.ILoggingContextResolver;
import com.tbs.logging.form.LoggingRecordForm;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import javax.annotation.Resource;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Date;

/**
 * 方法级操作日志切面
 * <p>
 * 拦截标注了 {@link OperateLog} 注解的方法，在方法执行前后构造
 * {@link LoggingRecordForm} 并经 {@link ILogRecorder} 异步落库。
 * 环境维度信息（链路追踪ID、操作用户、IP、请求URL与方法）不直接感知
 * web 等来源模块，统一经 {@link ILoggingContextResolver} 解析上下文快照、
 * 由 {@link ILoggingContextApplier} 回填——HTTP、Dubbo、定时任务等
 * 链路均可适配。
 * </p>
 * <p>
 * <b>职责边界</b>（与 {@link SystemOperateInterceptor} 的分工）：
 * <ul>
 *   <li>接口级（SystemOperateInterceptor）：覆盖全部 Controller 方法（@NoLog 跳过），
 *       一个请求一条</li>
 *   <li>方法级（本切面）：仅覆盖标注 {@code @OperateLog} 的方法（任意层级，典型为 Service），
 *       一个请求下可多条</li>
 * </ul>
 * 两者通过 traceId 关联，互不重复、互不替代。
 * </p>
 * <p>
 * <b>可靠性约定</b>：日志采集的任何环节（上下文解析、参数序列化、记录发送）失败均只记
 * error 日志，绝不影响业务方法执行与结果——异常原样向上抛出。
 * </p>
 * <p>
 * <b>防爆炸护栏</b>：以切面内线程级计数器（嵌套深度配对管理，最外层方法退出时清理）
 * 统计单请求内已记录的方法级日志条数，超过
 * {@code tbs.system.method-log-max-records-per-request} 上限后跳过成功路径的记录
 * （异常路径始终记录——异常是排障关键信息，优先级高于容量控制），并记 warn 一次。
 * </p>
 * <p>
 * <b>装配方式</b>：本类非 Spring 组件，由 DefaultAppConfig 经条件装配创建
 * （{@code tbs.system.enable-method-log=true} 时启用，默认关闭）。
 * </p>
 *
 * @author abstergo
 * @see OperateLog
 * @see SystemOperateInterceptor
 * @see ILoggingContextResolver
 * @see ILogRecorder
 */
@Slf4j
@Aspect
public class OperateLogAspect {

    /**
     * 线程级"单请求"方法级日志计数器
     * <p>
     * 以嵌套深度配对管理生命周期：最外层被拦截方法进入时创建，
     * 其退出时清理，防止线程复用导致计数串扰。
     * </p>
     */
    private static final ThreadLocal<RequestLogCounter> COUNTER = new ThreadLocal<>();

    /**
     * 日志记录器（接口级与方法级共用同一通道）
     */
    @Resource
    private ILogRecorder systemOperateLog;

    /**
     * 日志上下文解析门面（环境维度信息的统一获取入口）
     */
    @Resource
    private ILoggingContextResolver loggingContextResolver;

    /**
     * 日志上下文回填器
     */
    @Resource
    private ILoggingContextApplier loggingContextApplier;

    /**
     * 系统配置（防爆炸上限等）
     */
    @Resource
    private AppSettingProperty appSettingProperty;

    /**
     * 方法级操作日志环绕处理
     * <p>
     * 匹配所有标注了 {@link OperateLog} 注解的方法。构造初始记录后进入一层
     * 调用作用域执行目标方法，按结果（成功/异常）完成记录。
     * </p>
     *
     * @param joinPoint 切点
     * @return 目标方法返回值
     * @throws Throwable 目标方法抛出的异常原样向上抛出
     */
    @Around("@annotation(com.tbs.logging.annotation.OperateLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        RequestLogCounter counter = enterScope();
        try {
            // 构造初始记录（任何失败降级为部分填充，不阻断业务）
            LoggingRecordForm record = buildRecord(joinPoint);
            return proceedAndRecord(joinPoint, record);
        } finally {
            exitScope(counter);
        }
    }

    /**
     * 执行目标方法并按结果完成记录
     * <p>
     * 成功路径受防爆炸上限约束；异常路径始终记录后原样抛出。
     * </p>
     *
     * @param joinPoint 切点
     * @param record    初始记录表单
     * @return 目标方法返回值
     * @throws Throwable 目标方法抛出的异常原样向上抛出
     */
    private Object proceedAndRecord(ProceedingJoinPoint joinPoint, LoggingRecordForm record) throws Throwable {
        OperateLog annotation = resolveAnnotation(joinPoint);
        try {
            Object result = joinPoint.proceed();
            record.setOperateFinishTime(new Date());
            if (annotation != null && annotation.recordResult() && result != null) {
                record.setOperateResult(JsonUtils.toJSONString(result));
            }
            submitRecord(record, false);
            return result;
        } catch (Throwable e) {
            record.setOperateFinishTime(new Date());
            fillError(record, e);
            // 异常路径不受防爆炸上限约束：异常是排障关键，始终记录
            submitRecord(record, true);
            throw e;
        }
    }

    /**
     * 构造初始日志记录
     * <p>
     * 填充方法全名、描述与入参；环境维度信息（链路追踪ID、操作用户、IP、
     * 请求URL与方法）经上下文链路回填。整体捕获异常：上下文链路不可用等
     * 场景下降级为部分填充，不阻断业务。
     * </p>
     *
     * @param joinPoint 切点
     * @return 初始记录表单，不会为 null
     */
    private LoggingRecordForm buildRecord(ProceedingJoinPoint joinPoint) {
        LoggingRecordForm record = new LoggingRecordForm();
        try {
            MethodSignature signature = (MethodSignature)joinPoint.getSignature();
            OperateLog annotation = signature.getMethod().getAnnotation(OperateLog.class);
            String methodFullName =
                joinPoint.getTarget().getClass().getTypeName() + "." + signature.getMethod().getName();

            record.setOperateType(methodFullName);
            record.setOperateTime(new Date());
            record.setOperateDesc(
                annotation != null && !annotation.value().isEmpty() ? annotation.value() : methodFullName);

            // 环境维度信息经上下文链路统一获取并回填（值捕获，不依赖后续执行环境）
            loggingContextApplier.apply(loggingContextResolver.resolveCurrent(), record);

            // 入参
            if (annotation == null || annotation.recordParams()) {
                record.setOperateParam(JsonUtils.toJSONString(joinPoint.getArgs()));
            }
        } catch (Exception e) {
            log.warn("方法级日志记录初始化失败(不影响业务): {}", record.getOperateType(), e);
        }
        return record;
    }

    /**
     * 解析切点方法上的操作日志注解
     *
     * @param joinPoint 切点
     * @return 注解实例，无法解析时为 null
     */
    private OperateLog resolveAnnotation(ProceedingJoinPoint joinPoint) {
        if (!(joinPoint.getSignature() instanceof MethodSignature)) {
            return null;
        }
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        return signature.getMethod().getAnnotation(OperateLog.class);
    }

    /**
     * 填充异常信息与错误级别
     *
     * @param record 记录表单
     * @param e      业务异常
     */
    private void fillError(LoggingRecordForm record, Throwable e) {
        var baos = new ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(baos));
        record.setErrorMessage(String.format("%s:%s", e.getClass().getName(), new String(baos.toByteArray())));
        if (e instanceof IExceptionLevel) {
            record.setErrorCode(((IExceptionLevel)e).getLevel().getCode());
        } else {
            record.setErrorCode(ErrorLevelEnum.SYSTEM_ERROR.getCode());
        }
    }

    /**
     * 提交记录到日志通道
     * <p>
     * 成功路径先做防爆炸上限校验；发送失败仅记 error，不影响业务。
     * </p>
     *
     * @param record  记录表单
     * @param isError 是否异常路径记录（异常路径不受上限约束）
     */
    private void submitRecord(LoggingRecordForm record, boolean isError) {
        try {
            if (!isError && exceedRateLimit()) {
                return;
            }
            systemOperateLog.record(record);
        } catch (Throwable e) {
            log.error("方法级日志记录发送失败(不影响业务): {}", record.getOperateType(), e);
        }
    }

    /**
     * 校验是否超过单请求方法级日志记录上限
     * <p>
     * 以切面内线程级计数器实现；无计数器（不处于被拦截方法作用域）或上限配置
     * 非正数视为不限。首次越界时记 warn 一次。
     * </p>
     *
     * @return true 表示已超限，应跳过本次成功路径记录
     */
    private boolean exceedRateLimit() {
        int maxPerRequest = appSettingProperty.getMethodLogMaxRecordsPerRequest();
        if (maxPerRequest <= 0) {
            return false;
        }
        RequestLogCounter counter = COUNTER.get();
        if (counter == null) {
            return false;
        }
        long count = ++counter.count;
        if (count == maxPerRequest + 1L) {
            log.warn("单请求方法级日志记录数超过上限({}), 后续成功路径记录将被跳过(异常路径不受影响)", maxPerRequest);
        }
        return count > maxPerRequest;
    }

    /**
     * 进入一层被拦截方法作用域
     *
     * @return 当前线程的计数器
     */
    private RequestLogCounter enterScope() {
        RequestLogCounter counter = COUNTER.get();
        if (counter == null) {
            counter = new RequestLogCounter();
            COUNTER.set(counter);
        }
        counter.depth++;
        return counter;
    }

    /**
     * 退出一层被拦截方法作用域
     * <p>
     * 最外层方法退出时清理线程级计数器，防止线程复用导致计数串扰。
     * </p>
     *
     * @param counter 当前线程的计数器
     */
    private void exitScope(RequestLogCounter counter) {
        counter.depth--;
        if (counter.depth <= 0) {
            COUNTER.remove();
        }
    }

    /**
     * 单请求方法级日志计数器（线程内部状态）
     */
    private static final class RequestLogCounter {

        /**
         * 当前被拦截方法的嵌套深度
         */
        private int depth;

        /**
         * 已记录的方法级日志条数
         */
        private long count;
    }
}

package com.tbs.app.intercept;

import com.alibaba.fastjson2.JSON;
import com.tbs.common.data.ValueWrapper;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.error.IExceptionLevel;
import com.tbs.common.util.base.BeanUtil;
import com.tbs.shared.interfaces.log.ISystemOperateLogSender;
import com.tbs.shared.pojo.entity.SystemOperateRecordEntity;
import com.tbs.web.BaseController;
import com.tbs.web.gate.intercept.IApiInvokeInterceptor;
import com.tbs.web.uitl.AuthUtil;
import com.tbs.web.uitl.NetUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Date;

/**
 * 系统操作拦截器实现类
 * <p>
 * 该类实现了IApiInvokeInterceptor接口，用于拦截和记录系统API调用的操作信息。 在方法调用前创建操作记录，在方法调用后或异常时更新操作结果、执行时间和错误信息，
 * 实现对系统操作的完整审计跟踪。主要记录操作类型、操作IP、请求方法、请求URL、 操作时间、操作内容、操作用户、参数信息、执行结果和耗时等信息。
 *
 * @author abstergo
 * @see IApiInvokeInterceptor API调用拦截器接口
 * @see SystemOperateRecordEntity 系统操作记录实体类
 * @see ApiInvokeContext API调用上下文类
 */
@Slf4j
public class SystemOperateInterceptor implements IApiInvokeInterceptor {

    @Resource
    ISystemOperateLogSender systemOperateLog;

    /**
     * 系统操作上下文类
     * <p>
     * 扩展自ApiInvokeContext，增加了系统操作记录实体，用于存储和传递操作相关信息。
     *
     * @see ApiInvokeContext API调用上下文类
     * @see SystemOperateRecordEntity 系统操作记录实体类
     * @see Data Lombok数据注解
     */
    @Data
    public static class SystemOperContext extends ApiInvokeContext {
        /**
         * 系统操作记录实体
         * <p>
         * 存储当前操作的详细信息，包括操作类型、操作IP、请求方法、请求URL、操作时间、 操作内容、操作用户、参数信息、执行结果、异常信息和耗时等。
         *
         * @see SystemOperateRecordEntity 系统操作记录实体类
         */
        private SystemOperateRecordEntity operateRecord;
    }

    /**
     * HTTP请求对象
     * <p>
     * 通过Spring的@Resource注解注入，用于获取当前HTTP请求的相关信息， 如请求URI、请求方法、客户端IP等。
     *
     * @see HttpServletRequest HTTP请求接口
     * @see Resource 资源注入注解
     */
    @Resource
    private HttpServletRequest request;

    /**
     * 方法调用前拦截处理
     * <p>
     * 在目标方法被调用之前执行，创建系统操作记录并填充相关信息，包括操作类型、操作IP、 请求方法、请求URL、操作时间、操作内容、操作用户和参数信息等。
     *
     * @param context API调用上下文，包含目标对象、方法和参数等信息，可能为null
     * @return 包含操作记录的系统操作上下文，如果输入上下文为null则返回null
     * @see ApiInvokeContext API调用上下文类
     * @see SystemOperContext 系统操作上下文类
     * @see SystemOperateRecordEntity 系统操作记录实体类
     * @see BaseController 基础控制器类
     * @see NetUtil 网络工具类
     * @see AuthUtil 认证工具类
     * @see JSON JSON处理工具类
     * @see BeanUtil Bean工具类
     */
    @Override
    public ApiInvokeContext beforeInvoke(ApiInvokeContext context) {
        if (context == null) {
            return null;
        }
        SystemOperContext operateContext = new SystemOperContext();
        BeanUtil.copyProperties(context, operateContext, true);
        BaseController baseController = context.getTarget();
        SystemOperateRecordEntity record = new SystemOperateRecordEntity();
        record.setOperateType(context.getTarget().getClass().getTypeName() + "." + context.getMethod().getName());
        record.setOperateIp(NetUtil.getIp(request));
        record.setMethod(request.getMethod());
        record.setOperateUrl(request.getRequestURI());
        record.setOperateTime(new Date());
        record.setOperateContent(baseController.getBusinessName());
        if (AuthUtil.getInstance().isUserLoggedIn()) {
            record.setOperateUserName(AuthUtil.getInstance().getCurrentUser().getUsername());
        } else {
            record.setOperateUserName("NULL USERNAME:" + NetUtil.getIp(request));
        }
        record.setOperateParamJson(JSON.toJSONString(context.getArgs()));
        operateContext.setOperateRecord(record);
        return operateContext;
    }

    /**
     * 方法调用后拦截处理
     * <p>
     * 在目标方法成功执行完成后调用，更新操作记录中的执行结果和耗时信息。 计算方法执行时间，并将执行结果转换为JSON格式存储。
     *
     * @param context API调用上下文，包含操作记录等信息，可能为null
     * @param result  方法执行后的返回结果包装器，不能为null
     * @see ApiInvokeContext API调用上下文类
     * @see SystemOperContext 系统操作上下文类
     * @see ValueWrapper 值包装类
     * @see SystemOperateRecordEntity 系统操作记录实体类
     * @see JSON JSON处理工具类
     */
    @Override
    public void afterInvoke(ApiInvokeContext context, ValueWrapper<Object> result) {
        if (context == null) {
            return;
        }

        if (context instanceof SystemOperContext) {
            SystemOperContext operateContext = (SystemOperContext)context;
            operateContext.getOperateRecord()
                .setCostTime(System.currentTimeMillis() - operateContext.getOperateRecord().getOperateTime().getTime());
            result.get()
                .ifPresent(val -> operateContext.getOperateRecord().setOperateResultJson(JSON.toJSONString(val)));
            try {
                systemOperateLog.log(operateContext.getOperateRecord());
            } catch (Throwable e) {
                log.error("record log error", e);
            }
        }

    }

    /**
     * 异常处理拦截
     * <p>
     * 在目标方法执行过程中发生异常时调用，更新操作记录中的执行时间和异常信息。 计算方法执行时间，并将异常信息转换为JSON格式存储。
     *
     * @param context API调用上下文，包含操作记录等信息，可能为null
     * @param e       方法执行过程中抛出的异常，不能为null
     * @see ApiInvokeContext API调用上下文类
     * @see SystemOperContext 系统操作上下文类
     * @see SystemOperateRecordEntity 系统操作记录实体类
     * @see JSON JSON处理工具类
     */
    @Override
    public void onException(ApiInvokeContext context, Throwable e) {
        if (context == null) {
            return;
        }

        if (context instanceof SystemOperContext) {
            SystemOperContext operateContext = (SystemOperContext)context;
            operateContext.getOperateRecord()
                .setCostTime(System.currentTimeMillis() - operateContext.getOperateRecord().getOperateTime().getTime());
            var baos = new ByteArrayOutputStream();
            e.printStackTrace(new PrintStream(baos));

            operateContext.getOperateRecord()
                .setOperateError(String.format("%s:%s", e.getClass().getName(), new String(baos.toByteArray())));
            if (e instanceof IExceptionLevel) {
                operateContext.getOperateRecord().setOperateErrorLevel(((IExceptionLevel)e).getLevel().getCode());
            } else {
                operateContext.getOperateRecord().setOperateErrorLevel(ErrorLevelEnum.SYSTEM_ERROR.getCode());
            }

            try {
                systemOperateLog.log(operateContext.getOperateRecord());
            } catch (Throwable ex) {
                log.error("record log error", ex);
            }
        }
    }
}

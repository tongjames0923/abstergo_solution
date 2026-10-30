package com.tbs.common.error.filter;

import com.tbs.common.error.IExceptionLevel;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.filter.ExceptionFilter;

/**
 * 提供端异常透传过滤器
 * <p>
 * 以 SPI 同名扩展 {@code exception} 覆盖 Dubbo 内置 {@link ExceptionFilter}。内置实现在「异常未在接口方法
 * {@code throws} 中声明、且异常类与接口类不在同一 jar」时，会用 {@code new RuntimeException(堆栈文本)}
 * 重新包装，导致 {@link com.tbs.common.exception.FormattedRuntimeException} 的异常类型与 errorLevel 丢失，
 * 前端还会收到带内部类名和堆栈的文案。
 * <p>
 * 本实现只把实现了 {@link IExceptionLevel} 的业务异常原样透传，其余异常仍沿用内置逻辑（未受检未声明异常的
 * 日志告警、第三方不可序列化异常包装为 RuntimeException）。
 *
 * @author abstergo
 */
@Activate(group = CommonConstants.PROVIDER)
public class ProviderExceptionFilter extends ExceptionFilter {

    @Override
    public void onResponse(Result appResponse, Invoker<?> invoker, Invocation invocation) {
        // 业务异常透传，保留异常类型与 errorLevel；无异常时 getException() 为 null，同样交回父类处理
        if (appResponse.getException() instanceof IExceptionLevel) {
            return;
        }
        super.onResponse(appResponse, invoker, invocation);
    }
}

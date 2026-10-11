package com.tbs.web.gate.center;

import jakarta.servlet.http.HttpServletRequest;

import com.tbs.web.gate.ITokenPicker;
import com.tbs.web.security.token.ITokenModel;
import org.springframework.core.Ordered;

/**
 * 网关中心
 * <p>
 * 定义某一类请求路径的认证处理单元：声明是否支持当前请求、如何提取令牌、无令牌/合法令牌/异常时的处理策略。
 * 通过 {@link Ordered} 决定多中心间的执行顺序（默认最低优先级）。
 * </p>
 *
 * @author abstergo
 */
public interface IGateWayCenter extends Ordered {
	/**
	 * 获取 Token 解析器
	 *
	 * @return 该中心使用的令牌拾取器，不能为空
	 */
	ITokenPicker getTokenPicker();

	/**
	 * 无 Token 时的处理
	 * <p>
	 * 请求到达但提取不到令牌时调用，典型实现为抛 {@link NotFoundTokenException} 阻断后续处理。
	 * </p>
	 */
	void onNoneToken();

	/**
	 * 是否支持处理该请求
	 *
	 * @param request 当前 HTTP 请求，不能为空
	 * @return 支持返回 {@code true}（将参与令牌处理），否则 {@code false}
	 */
	boolean support(HttpServletRequest request);

	/**
	 * 处理已校验的 Token
	 * <p>
	 * 基于令牌模型构造并写入认证上下文，供后续业务读取。
	 * </p>
	 *
	 * @param tokenModel 已通过基础校验的令牌模型，不能为空
	 */
	void handleToken(ITokenModel tokenModel);

	/**
	 * 判断异常是否导致请求中断
	 *
	 * @param e 处理过程中抛出的异常，不能为空
	 * @return 返回 {@code true} 时过滤器直接写错误响应并中断；{@code false} 时仅告警放行
	 */
	boolean onErrorWillBroken(Throwable e);

	@Override
	default int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}
}

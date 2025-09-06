package com.tbs.web.gate.center;

import javax.servlet.http.HttpServletRequest;

import com.tbs.web.gate.ITokenPicker;
import com.tbs.web.security.token.ITokenModel;
import org.springframework.core.Ordered;

/**
 *
 * @author abstergo
 */
public interface IGateWayCenter extends Ordered {
	/**
	 * 获取Token解析器
	 *
	 * @return
	 */
	ITokenPicker getTokenPicker();

	/**
	 * 无Token处理
	 */
	void onNoneToken();

	/**
	 * 支持处理
	 *
	 * @param request
	 * @return
	 */
	boolean support(HttpServletRequest request);

	/**
	 * 处理Token
	 *
	 * @param tokenModel
	 */
	void handleToken(ITokenModel tokenModel);

	/**
	 * 错误处理
	 *
	 * @param e
	 * @return
	 */
	boolean onErrorWillBroken(Throwable e);

	@Override
	default int getOrder() {
		return Ordered.LOWEST_PRECEDENCE;
	}
}

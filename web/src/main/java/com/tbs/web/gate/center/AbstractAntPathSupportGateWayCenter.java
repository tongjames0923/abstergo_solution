package com.tbs.web.gate.center;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.util.matcher.RequestMatcher;

import lombok.Getter;

/**
 * 抽象的基于路径匹配的权限管理器
 * @author abstergo
 */
@Getter
public abstract class AbstractAntPathSupportGateWayCenter implements IGateWayCenter {
	RequestMatcher matcher;

	public AbstractAntPathSupportGateWayCenter setMatcher(RequestMatcher matcher) {
		this.matcher = matcher;
		return this;
	}

	public AbstractAntPathSupportGateWayCenter(RequestMatcher matcher) {
		this.matcher = matcher;
	}

	@Override
	public boolean support(HttpServletRequest request) {
		return matcher.matches(request);
	}
}

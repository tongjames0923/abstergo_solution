package com.tbs.web.security;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.web.util.matcher.RequestMatcher;

import lombok.Getter;

/**
 * 抽象的基于路径匹配的权限管理器
 */
@Getter
public abstract class AbstractAntPathSupportSecurityManager implements ISecurityManager {
	RequestMatcher matcher;

	public AbstractAntPathSupportSecurityManager setMatcher(RequestMatcher matcher) {
		this.matcher = matcher;
		return this;
	}

	public AbstractAntPathSupportSecurityManager(RequestMatcher matcher) {
		this.matcher = matcher;
	}

	@Override
	public boolean support(HttpServletRequest request) {
		return matcher.matches(request);
	}
}

package com.tbs.web.security.impl;

import com.tbs.web.security.token.AbstractTokenCreator;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.security.token.model.SimpleTokenModel;

/**
 * 简单的Token创建器
 *
 **/
public class SimpleTokenCreator extends AbstractTokenCreator {
	@Override
	protected ITokenModel interceptToken(SimpleTokenModel token) {
		return token;
	}
}

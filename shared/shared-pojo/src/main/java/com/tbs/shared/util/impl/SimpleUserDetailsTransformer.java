package com.tbs.shared.util.impl;

import com.tbs.common.data.impl.JsonTransformer;
import com.tbs.shared.util.IUserDetailsTransformer;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * 默认用户详情转换器
 *
 * @author abstergo
 */
@Component
public class SimpleUserDetailsTransformer extends JsonTransformer<UserDetails> implements IUserDetailsTransformer {
}

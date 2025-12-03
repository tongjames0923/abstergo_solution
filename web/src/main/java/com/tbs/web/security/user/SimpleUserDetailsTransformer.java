package com.tbs.web.security.user;

import com.tbs.common.data.impl.JsonTransformer;
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

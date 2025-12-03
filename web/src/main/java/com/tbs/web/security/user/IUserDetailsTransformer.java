package com.tbs.web.security.user;

import com.tbs.common.data.ITransformer;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 用户详情转换器
 *
 * @author abstergo
 */
public interface IUserDetailsTransformer extends ITransformer<UserDetails> {

}

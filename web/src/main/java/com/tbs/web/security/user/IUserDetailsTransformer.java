package com.tbs.web.security.user;

import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;

/**
 * 用户详情转换器
 *
 * @author abstergo
 */
public interface IUserDetailsTransformer<T extends Serializable> {

    /**
     * 转换
     *
     * @param userDetails
     * @return
     */
    T transform(UserDetails userDetails);

    /**
     * 反向转换
     *
     * @param userDetails
     * @return
     */
    UserDetails reverseTransform(T userDetails);

}

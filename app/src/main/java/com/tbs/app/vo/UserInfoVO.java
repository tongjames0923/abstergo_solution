package com.tbs.app.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Collection;

/**
 * 用户信息VO
 *
 * @author tongj
 */
@Data
public class UserInfoVO implements Serializable {
    private String userName;
    private String nickName;
    private String avatar;
    private Collection<String> avaliableRoles;
}

package com.tbs.app.account.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountVO implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 账户状态
     */
    private Integer flag;
}

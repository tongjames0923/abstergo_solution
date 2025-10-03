package com.tbs.app.form;

import com.tbs.app.service.user.model.ISystemUserDetails;

import java.io.Serializable;
import java.util.Date;

public interface IRemoteRequestForm<FORM extends Serializable> extends Serializable {

    /**
     * 获取访问令牌
     *
     * @return
     */
    String getAccessToken();

    /**
     * 获取请求时间
     *
     * @return
     */
    Date getRequestTime();

    /**
     * 获取用户信息
     *
     * @return
     */
    ISystemUserDetails getUserDetails();

    /**
     * 获取表单
     *
     * @return
     */
    FORM getForm();
}

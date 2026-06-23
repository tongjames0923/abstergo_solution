package com.tbs.app.form;

import com.tbs.shared.interfaces.annoation.PasswordValid;
import com.tbs.shared.interfaces.annoation.UserNameValid;
import com.tbs.web.security.user.form.IUsernameAndPasswordForm;
import lombok.Data;

/**
 * 用户登录表单
 * <p>
 * 用于接收用户登录请求参数，包含用户名和密码
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
public class LoginForm implements IUsernameAndPasswordForm {

    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     * <p>
     * 用户登录账号，需符合用户名格式验证规则
     * </p>
     */
    @UserNameValid
    private String username;

    /**
     * 密码
     * <p>
     * 用户登录密码，需符合密码格式验证规则
     * </p>
     */
    @PasswordValid
    private String password;

    /**
     * 登录类型，0/null web登录，1客户端登录
     */
    private Integer loginType;
}

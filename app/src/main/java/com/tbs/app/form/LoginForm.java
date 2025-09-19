package com.tbs.app.form;

import com.tbs.shared.interfaces.annoation.PasswordValid;
import com.tbs.shared.interfaces.annoation.UserNameValid;
import com.tbs.web.security.user.form.IUsernameAndPasswordForm;
import lombok.Data;

/**
 * 登录表单
 *
 * @author abstergo
 */
@Data
public class LoginForm implements IUsernameAndPasswordForm {

    private static final long serialVersionUID = 1L;

    @UserNameValid
    private String username;
    @PasswordValid
    private String password;
}

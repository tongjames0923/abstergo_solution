package com.tbs.app.form;

import com.tbs.app.annoation.PasswordValid;
import com.tbs.app.annoation.UserNameValid;
import lombok.Data;

import java.io.Serializable;


@Data
public class LoginForm implements Serializable {

    private static final long serialVersionUID = 1L;

    @UserNameValid
    private String username;
    @PasswordValid
    private String password;
}

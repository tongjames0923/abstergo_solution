package com.tbs.app.form;

import java.io.Serializable;

import com.tbs.shared.interfaces.annoation.PasswordValid;
import com.tbs.shared.interfaces.annoation.UserNameValid;

import lombok.Data;


@Data
public class LoginForm implements Serializable {

    private static final long serialVersionUID = 1L;

    @UserNameValid
    private String username;
    @PasswordValid
    private String password;
}

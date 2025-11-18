package com.tbs.app.form;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author tongj
 */
@Data
@ToString
public class DisableTokenForm implements Serializable {
    private static final long serialVersionUID = 1L;
    @NotBlank
    private String token;
    @NotNull
    private Long userId;
}

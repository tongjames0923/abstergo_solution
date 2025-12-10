package com.tbs.app.form;

import lombok.Data;
import lombok.ToString;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * 禁用Token表单
 * <p>
 * 用于接收禁用用户Token的请求参数
 * </p>
 *
 * @author tongj
 * @since 1.0.0
 */
@Data
@ToString
public class DisableTokenForm implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Token值
     * <p>
     * 需要禁用的用户登录令牌
     * </p>
     */
    @NotBlank
    private String token;

    /**
     * 用户ID
     * <p>
     * 关联的用户ID
     * </p>
     */
    @NotNull
    private Long userId;
}

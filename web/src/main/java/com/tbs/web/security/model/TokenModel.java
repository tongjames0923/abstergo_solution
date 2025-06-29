package com.tbs.web.security.model;

import com.tbs.web.enums.TokenSource;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * token实体模型
 *
 * @author tongj
 */
@Data
public class TokenModel implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * token
     */
    private String token;

    /**
     * token来源
     */
    private TokenSource source;

    /**
     * token来源字段
     */
    private String sourceField;

    /**
     * token来源字段值
     */
    private String sourceValue;

    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 创建时间
     */
    private Date createTime;

    protected TokenModel() {
    }

    /**
     * 创建TokenModel
     *
     * @param token
     * @param source
     * @param sourceField
     * @param sourceValue
     * @param expireTime
     * @return
     */
    public static TokenModel create(String token, TokenSource source, String sourceField, String sourceValue,
        Date expireTime) {
        TokenModel model = new TokenModel();
        model.token = token;
        model.source = source;
        model.sourceField = sourceField;
        model.sourceValue = sourceValue;
        model.expireTime = expireTime;
        model.createTime = new Date();
        return model;
    }
}

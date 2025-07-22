package com.tbs.web.security.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.tbs.web.enums.TokenSource;
import lombok.Data;

import java.util.Date;

/**
 * token实体模型
 *
 * @author tongj
 */
@Data
public class SimpleTokenModel implements com.tbs.web.security.ITokenModel {
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date expireTime;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    protected SimpleTokenModel() {
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
    public static SimpleTokenModel create(String token, TokenSource source, String sourceField, String sourceValue,
        Date expireTime) {
        SimpleTokenModel model = new SimpleTokenModel();
        model.token = token;
        model.source = source;
        model.sourceField = sourceField;
        model.sourceValue = sourceValue;
        model.expireTime = expireTime;
        model.createTime = new Date();
        return model;
    }
}

package com.tbs.app.form;

import lombok.Data;

import java.io.Serializable;

/**
 * OSS公开下载码创建/更新表单
 * <p>
 * HTTP层专属入参对象，创建时objectKey必填；更新时仅传需要修改的字段
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
public class OssDownloadCodeForm implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 存储对象键（创建时必填）
     */
    private String objectKey;

    /**
     * 下载唯一码（可选，创建时传入则使用自定义码，须为16位Base64字符；缺省由服务端自动生成）
     */
    private String code;

    /**
     * 存储桶名（可选，缺省使用默认桶）
     */
    private String bucket;

    /**
     * 有效期秒数（可选，空或小于等于0表示永不过期）
     */
    private Long expireSeconds;

    /**
     * 备注说明（可选）
     */
    private String remark;
}

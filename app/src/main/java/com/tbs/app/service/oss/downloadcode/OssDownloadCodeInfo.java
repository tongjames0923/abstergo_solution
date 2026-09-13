package com.tbs.app.service.oss.downloadcode;

import lombok.Data;

import java.io.Serializable;

/**
 * OSS公开下载码信息模型
 * <p>
 * 描述一条下载码记录的完整生命周期数据，为契约包自包含模型，
 * 不感知HTTP与具体存储实现，便于未来整体抽离为独立模块
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
public class OssDownloadCodeInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 永不过期语义的时间戳取值：expireAt小于等于该值表示下载码永久有效
     */
    public static final long NEVER_EXPIRE = 0L;

    /**
     * 下载唯一码（16位Base64字符随机值）
     */
    private String code;

    /**
     * 存储桶名（创建时未指定则记录解析后的默认桶）
     */
    private String bucket;

    /**
     * 存储对象键
     */
    private String objectKey;

    /**
     * 过期时间戳（毫秒），小于等于{@link #NEVER_EXPIRE}表示永不过期
     */
    private long expireAt;

    /**
     * 创建时间戳（毫秒）
     */
    private long createTime;

    /**
     * 备注说明（可选）
     */
    private String remark;

    /**
     * 判断下载码是否已过期
     *
     * @param nowMillis 当前时间戳（毫秒）
     * @return true表示已过期
     */
    public boolean isExpired(long nowMillis) {
        return expireAt > NEVER_EXPIRE && nowMillis > expireAt;
    }
}

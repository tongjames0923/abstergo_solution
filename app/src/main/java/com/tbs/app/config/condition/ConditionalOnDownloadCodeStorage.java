package com.tbs.app.config.condition;

import com.tbs.app.config.property.OssDownloadCodeStorageType;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * 下载码存储类型条件注解
 * <p>
 * 当 tbs.oss.download-code.storage 的值匹配指定的任意一个存储类型时，条件成立，Bean 被注册。
 * 支持多值"或"语义，例如 {@code @ConditionalOnDownloadCodeStorage({REDIS, DB})} 表示
 * 配置为 redis 或 db 时均激活
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(DownloadCodeStorageCondition.class)
public @interface ConditionalOnDownloadCodeStorage {

    /**
     * 允许激活的存储类型（任意一个匹配即生效）
     */
    OssDownloadCodeStorageType[] value();
}

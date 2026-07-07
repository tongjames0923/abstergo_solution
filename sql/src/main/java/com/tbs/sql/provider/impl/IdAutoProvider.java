package com.tbs.sql.provider.impl;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.framework.UuidUtil;
import com.tbs.sql.provider.IAutoValueProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

/**
 * 雪花ID自动值提供者，用于在INSERT操作时自动填充雪花算法生成的唯一ID。
 *
 * <p>该类实现了 {@link IAutoValueProvider} 接口，通过 {@link UuidUtil} 获取雪花算法生成的Long型ID。
 * 仅在字段值为null时填充，确保不会覆盖已有ID值。通常配合 {@code insertOnly = true} 使用， 避免UPDATE操作时重新生成ID。</p>
 *
 * <h3>使用方式</h3>
 * <pre>{@code
 * @Id
 * @SqlAutoValue(value = SnowflakeIdAutoProvider.class, insertOnly = true)
 * private Long id;
 * }</pre>
 *
 * <h3>注意事项</h3>
 * <ul>
 *   <li>需要配合 {@code insertOnly = true} 使用，避免UPDATE时重新生成ID</li>
 *   <li>依赖 {@link UuidUtil} 的静态实例，需确保Spring容器已初始化</li>
 *   <li>仅支持 {@link Long} 类型字段</li>
 * </ul>
 *
 * @author abstergo
 * @see IAutoValueProvider
 * @see UuidUtil
 * @since 2.0.0
 */
public class IdAutoProvider implements IAutoValueProvider {

    private static final Logger logger = LoggerFactory.getLogger(IdAutoProvider.class);

    @Override
    public Object getValue(Object target, Field field) {
        if (field.getType().equals(Long.class)) {
            return UuidUtil.getInstance().getUuidLong();
        } else if (field.getType().equals(String.class)) {
            return UuidUtil.getInstance().getUuidString();
        } else {
            AssertUtil.isTrueSystemError(true).message("击穿支持方法的异常类型{}", field.getType().getName()).throwIt();
            return null;
        }
    }

    @Override
    public boolean support(Class<?> type, Object oldValue) {
        logger.debug("雪花ID提供者支持类型：{}", type.getName());
        //支持long和string类型
        return Long.class.isAssignableFrom(type) || String.class.isAssignableFrom(type);
    }
}

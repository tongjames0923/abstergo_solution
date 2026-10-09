package com.tbs.common.util.framework;

import com.tbs.common.config.property.UuidProperty;
import com.tbs.common.uuid.IUuidGenerator;
import lombok.Getter;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;

/**
 * @author abstergo
 */
@Component
public class UuidUtil {
    @Getter
    private static UuidUtil instance;
    @Resource
    private IUuidGenerator uuidGenerator;
    @Resource
    private UuidProperty uuidProperty;

    public UuidUtil() {
        instance = this;
    }

    /**
     * 获取UUID
     *
     * @return
     */
    public String getUuidString() {
        return uuidGenerator.getUuidString(uuidProperty.getExceptLength());
    }

    /**
     * 获取UUID
     *
     * @return
     */
    public Long getUuidLong() {
        return uuidGenerator.getUuidLong();
    }
}

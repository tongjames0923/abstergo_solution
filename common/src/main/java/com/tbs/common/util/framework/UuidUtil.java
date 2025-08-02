package com.tbs.common.util.framework;

import com.tbs.common.config.property.UuidProperty;
import com.tbs.common.uuid.IUuidGenerator;
import lombok.Getter;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

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

    public String getUuidString() {
        return uuidGenerator.getUuidString(uuidProperty.getExceptLength());
    }
}

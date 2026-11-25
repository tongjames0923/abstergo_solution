package com.tbs.common.config;

import com.tbs.common.config.property.UuidProperty;
import com.tbs.common.util.base.BeanUtil;
import com.tbs.common.uuid.IUuidGenerator;
import com.tbs.common.uuid.impl.PooledUuidGenerator;
import com.tbs.common.uuid.impl.SnowFlakeUUID;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * UUID配置
 *
 * @author abstergo
 */
@Configuration
public class UuidConfig {
    @Resource
    private UuidProperty uuidProperty;

    private IUuidGenerator makeUUID() {
        if (uuidProperty.getType() == SnowFlakeUUID.class) {
            return new SnowFlakeUUID(uuidProperty.getSnowFlakeWorkerId(), uuidProperty.getSnowFlakeDataCenterId());
        } else {
            return BeanUtil.buildBeanFromProperties(() -> {
                return new SnowFlakeUUID();
            }, uuidProperty.getType(), BeanUtil::buildByEmptyArgs);
        }
    }

    @Bean
    public IUuidGenerator mainUuidGenerator() {
        var uuidGenerator = makeUUID();
        if (uuidGenerator instanceof PooledUuidGenerator) {
            return uuidGenerator;
        }
        return new PooledUuidGenerator(uuidGenerator);
    }

}

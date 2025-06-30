package com.tbs.common.config;

import com.tbs.common.config.property.UuidProperty;
import com.tbs.common.util.SpringUtil;
import com.tbs.common.uuid.IUuidGenerator;
import com.tbs.common.uuid.impl.SnowFlakeUUID;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;

/**
 * UUID配置
 *
 * @author abstergo
 */
@Configuration
public class UuidConfig {
    @Resource
    private UuidProperty uuidProperty;
    
    @Bean
    public IUuidGenerator mainUuidGenerator() {
        if (uuidProperty.getType() == SnowFlakeUUID.class) {
            return new SnowFlakeUUID(uuidProperty.getSnowFlakeWorkerId(), uuidProperty.getSnowFlakeDataCenterId());
        } else {
            return SpringUtil.buildBeanFromProperties(() -> {
                return new SnowFlakeUUID();
            }, uuidProperty.getType(), SpringUtil::buildByEmptyArgs);
        }
    }

}

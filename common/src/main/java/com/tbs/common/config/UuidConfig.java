package com.tbs.common.config;

import com.tbs.common.config.property.UuidProperty;
import com.tbs.common.constant.UuidBuiltinGeneratorTypeNameConstant;
import com.tbs.common.util.SpringUtil;
import com.tbs.common.uuid.IUuidGenerator;
import com.tbs.common.uuid.impl.SimpleUUID;
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
        if (UuidBuiltinGeneratorTypeNameConstant.SNOW_FLAKE.equals(uuidProperty.getType())) {
            return new SnowFlakeUUID(uuidProperty.getSnowFlakeWorkerId(), uuidProperty.getSnowFlakeDataCenterId());
        } else if (UuidBuiltinGeneratorTypeNameConstant.SIMPLE.equals(uuidProperty.getType())) {
            return new SimpleUUID();
        } else {
            return SpringUtil.getBean(uuidProperty.getType(), IUuidGenerator.class);
        }
    }

}

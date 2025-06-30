package com.tbs.common.config.property;

import com.tbs.common.uuid.IUuidGenerator;
import com.tbs.common.uuid.impl.SimpleUUID;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * UUID属性
 *
 * @author abstergo
 */
@Data
@Component
@ConfigurationProperties("tbs.uuid")
public class UuidProperty {
    /**
     * UUID类型
     */
    private Class<? extends IUuidGenerator> type = SimpleUUID.class;
    /**
     * 雪花算法参数,type=SnowFlake 时生效
     */
    private long snowFlakeWorkerId = 0;
    /**
     * 雪花算法参数,type=SnowFlake 时生效
     **/
    private long snowFlakeDataCenterId = 0;

    /**
     * 期望的UUID长度
     */
    private int exceptLength = 32;
}

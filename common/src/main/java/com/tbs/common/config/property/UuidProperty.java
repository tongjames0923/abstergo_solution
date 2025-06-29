package com.tbs.common.config.property;

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
     * UUID类型，自带有simple,snowFlake，其他的则对应继承自IUuidGenerator 的实现类的Bean
     */
    private String type = "simple";
    /**
     * 雪花算法参数
     */
    private long snowFlakeWorkerId = 0;
    /**
     * 雪花算法参数
     **/
    private long snowFlakeDataCenterId = 0;

    /**
     * 期望的UUID长度
     */
    private int exceptLength = 32;
}

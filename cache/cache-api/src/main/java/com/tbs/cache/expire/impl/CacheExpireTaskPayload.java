package com.tbs.cache.expire.impl;

import com.tbs.schedule.api.TaskPayload;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class CacheExpireTaskPayload implements TaskPayload {

    public static final String TYPE = "cache-expire";

    /**
     * 缓存名称
     */
    private String cacheName;

    /**
     * key
     */

    private Serializable key;

    /**
     * 参数map
     */
    private Map<String, Serializable> paramMap = new HashMap<>();

}

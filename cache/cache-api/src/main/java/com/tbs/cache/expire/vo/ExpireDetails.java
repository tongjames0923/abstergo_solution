package com.tbs.cache.expire.vo;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

/**
 * 缓存过期详细信息内部类 用于存储缓存项的过期时间、是否全部过期以及相关键的信息
 */
@Data
@AllArgsConstructor
public class ExpireDetails {
    /**
     * 过期时间
     */
    private Date expireTime;

    /**
     * 是否全部缓存项过期
     */
    private boolean isAll;

    /**
     * 相关缓存键
     */
    private Object key;
}

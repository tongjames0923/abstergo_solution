package com.tbs.app.form;

import lombok.Data;

import java.io.Serializable;


/**
 * 缓存清除表单
 * @author abstergo
 */
@Data
public class CacheForm implements Serializable {
    private String cacheName;
    private String key;
}

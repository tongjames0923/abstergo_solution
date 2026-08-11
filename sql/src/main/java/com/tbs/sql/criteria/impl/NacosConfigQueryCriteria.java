package com.tbs.sql.criteria.impl;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.AbstractListener;
import com.alibaba.nacos.api.exception.NacosException;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.sql.criteria.AbstractRemoteConfigQueryCriteria;
import com.tbs.sql.model.select.SelectInfoModel;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Nacos config query criteria
 *
 * @author abstergo
 */
@Slf4j
public class NacosConfigQueryCriteria extends AbstractRemoteConfigQueryCriteria {
    public NacosConfigQueryCriteria(String configId) {
        super(configId);
    }

    private static final Map<String, SelectInfoModel> cache = new ConcurrentHashMap<>();

    /**
     * 默认构造函数
     */
    public NacosConfigQueryCriteria() {
    }

    /**
     * @param configId
     * @return
     */
    @Override
    protected SelectInfoModel getSelectSQLInfoFromRemote(String configId) {
        return cache.computeIfAbsent(configId, k -> {
            String config = null;
            try {
                var configService = SpringUtil.getBean(ConfigService.class);
                config = configService.getConfig(configId, "SQL-QUERY", 3000L);
                log.info("Query Config {} from Nacos", configId);
                configService.addListener(configId, "SQL-QUERY", new AbstractListener() {
                    @Override
                    public void receiveConfigInfo(String s) {
                        log.info("Query Config {} changed : {}", configId, s);
                        cache.put(configId, SelectInfoModel.rawSql(s));
                    }
                });
            } catch (NacosException e) {
                AssertUtil.isTrue(true).errorLevel(ErrorLevelEnum.DATA_ACCESS_ERROR).cause(e).throwIt();
            }
            return SelectInfoModel.rawSql(config);
        });
    }
}

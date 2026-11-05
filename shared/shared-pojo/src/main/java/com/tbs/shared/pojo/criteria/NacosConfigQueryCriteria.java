package com.tbs.shared.pojo.criteria;

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
 * 基于 Nacos 配置中心的查询条件类。
 *
 * <p>以 configId 作为 Nacos dataId、固定分组 {@code SQL-QUERY} 读取原始 SQL，并注册监听器实现热更新：
 * 配置变更时刷新本地缓存。读取到的 SQL 交由父类作为 SELECT 主体，条件仍由子类的
 * {@code @QueryField} 注解拼装。</p>
 *
 * <p>本类是 Nacos 这一具体配置源的实现，故不下沉到 sql 框架模块，改由使用方所在的 shared-pojo 承载；
 * sql 模块只保留 provider 中立的抽象 {@link AbstractRemoteConfigQueryCriteria}。</p>
 *
 * @author abstergo
 */
@Slf4j
public class NacosConfigQueryCriteria extends AbstractRemoteConfigQueryCriteria {

    /**
     * 配置内容缓存，键为 configId，值为解析出的 SELECT 信息；命中监听回调时整体替换。
     */
    private static final Map<String, SelectInfoModel> cache = new ConcurrentHashMap<>();

    /**
     * 指定 Nacos dataId 构造查询条件。
     *
     * @param configId Nacos 配置的 dataId
     */
    public NacosConfigQueryCriteria(String configId) {
        super(configId);
    }

    /**
     * 默认构造函数，configId 由后续设置。
     */
    public NacosConfigQueryCriteria() {
    }

    /**
     * 从 Nacos 读取指定配置对应的 SQL，并注册变更监听。
     *
     * @param configId Nacos 配置的 dataId
     * @return SELECT 信息模型，包装读取到的原始 SQL
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

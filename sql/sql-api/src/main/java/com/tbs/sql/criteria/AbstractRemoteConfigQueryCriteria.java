package com.tbs.sql.criteria;

import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.sql.model.select.SelectInfoModel;
import lombok.Getter;
import lombok.Setter;

/**
 * 抽象的远程配置查询类
 *
 * @author abstergo
 */
public abstract class AbstractRemoteConfigQueryCriteria implements IQueryCriteria {

    /**
     * 配置ID
     */
    @Getter
    @Setter
    private String configId;

    public AbstractRemoteConfigQueryCriteria(String configId) {
        this.configId = configId;
    }

    /**
     * 默认构造函数
     */
    public AbstractRemoteConfigQueryCriteria() {
    }

    /**
     * 从远程获取SQL信息
     *
     * @param configId 配置ID
     * @return SelectInfoModel
     */
    protected abstract SelectInfoModel getSelectSQLInfoFromRemote(String configId);

    /**
     * 获取查询SQL信息
     *
     * @return SelectInfoModel
     */
    @Override
    public SelectInfoModel getSelectSQLInfo() {
        AssertUtil.isTrue(StrUtil.isEmpty(getConfigId()), "未实现getSelectSQLInfo方法");
        return getSelectSQLInfoFromRemote(getConfigId());
    }
}

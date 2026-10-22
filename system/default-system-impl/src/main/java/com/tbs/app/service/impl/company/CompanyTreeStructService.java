package com.tbs.app.service.impl.company;

import com.tbs.app.service.company.ISystemCompanyService;
import com.tbs.common.provider.annotation.RoutingRef;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.shared.interfaces.service.AbstractTreeStructService;
import com.tbs.shared.pojo.entity.SystemCompanyEntity;
import com.tbs.struct.tree.api.persistence.ITreeStore;

/**
 * 公司树形结构服务
 * <p>
 * 通过 IObjectProvider 解析 ITreeStore：本地有 Bean 时直取（零协议开销）， 本地无 Bean 时自动降级 Dubbo 远程引用。
 * </p>
 *
 * @author abstergo
 */

public class CompanyTreeStructService extends AbstractTreeStructService<SystemCompanyEntity> {

    @RoutingRef(business = "prod")
    private ITreeStore<Long> treeStore;

    @Override
    @SuppressWarnings("unchecked")
    protected ITreeStore<Long> getTreeStore() {
        return treeStore;
    }

    private static final String COMPANY_TREE_GROUP = "COMPANY_TREE";

    @Override
    protected String getGroupName() {
        return COMPANY_TREE_GROUP;
    }

    @Override
    protected String getNodeId(Long d, SystemCompanyEntity systemCompanyEntity) {
        return COMPANY_TREE_GROUP + "::" + String.valueOf(d);
    }

    @Override
    protected SystemCompanyEntity queryById(Long aLong) {
        return SpringUtil.getBean(ISystemCompanyService.class).queryById(aLong);
    }
}

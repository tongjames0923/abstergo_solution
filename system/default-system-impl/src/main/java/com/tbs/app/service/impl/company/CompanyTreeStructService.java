package com.tbs.app.service.impl.company;

import com.tbs.app.service.company.ISystemCompanyService;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.shared.interfaces.service.AbstractTreeStructService;
import com.tbs.shared.mapper.SystemCompanyMapper;
import com.tbs.shared.pojo.entity.SystemCompanyEntity;
import com.tbs.struct.tree.api.persistence.ITreeStore;
import org.apache.dubbo.config.annotation.DubboReference;

import javax.annotation.Resource;

/**
 * @author abstergo
 */

public class CompanyTreeStructService extends AbstractTreeStructService<SystemCompanyEntity> {

    @DubboReference
    private ITreeStore treeStore;

    @Resource
    SystemCompanyMapper companyMapper;

    @Override
    protected ITreeStore getTreeStore() {
        return treeStore;
    }

    private static final String COMPANY_TREE_GROUP = "COT:";

    @Override
    protected String getGroupName(Long d) {
        return COMPANY_TREE_GROUP + String.valueOf(d);
    }

    @Override
    protected String getNodeId(Long d, SystemCompanyEntity systemCompanyEntity) {
        return COMPANY_TREE_GROUP + String.valueOf(d);
    }

    @Override
    protected SystemCompanyEntity queryById(Long aLong) {
        return SpringUtil.getBean(ISystemCompanyService.class).queryById(aLong);
    }
    @Override
    public SystemCompanyEntity queryByNodeId(String nodeId, String group) {
        return companyMapper.getByTree(nodeId, group);
    }
}

package com.tbs.app.remote.company.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.tbs.app.remote.company.ICompanyRemoteService;
import com.tbs.app.service.company.ISystemCompanyService;
import com.tbs.app.service.impl.company.CompanyTreeStructService;
import com.tbs.app.vo.RemoteCompanyInfoVO;
import com.tbs.shared.pojo.criteria.CommonSystemCompanyQueryCriteria;
import com.tbs.shared.pojo.entity.SystemCompanyEntity;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 默认的公司远程服务实现
 *
 * @author abstergo
 */
public class DefaultCompanyRemoteServiceImpl implements ICompanyRemoteService {

    @Resource
    private ISystemCompanyService companyService;

    @Resource
    private CompanyTreeStructService companyTreeStructService;

    @Override
    public RemoteCompanyInfoVO getCompanyInfoById(Long companyId) {
        if (companyId == null) {
            return null;
        }
        SystemCompanyEntity company = companyService.queryById(companyId);
        if (company == null) {
            return null;
        }
        return convertToVO(company);
    }

    @Override
    public List<RemoteCompanyInfoVO> getCompanyInfoByName(String companyName) {
        if (StrUtil.isBlank(companyName)) {
            return Collections.emptyList();
        }
        var qo = new CommonSystemCompanyQueryCriteria();
        qo.setLikeName(companyName);
        List<SystemCompanyEntity> companies = companyService.queryAndPage(qo, null).getData();
        if (CollUtil.isEmpty(companies)) {
            return Collections.emptyList();
        }

        return companies.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public RemoteCompanyInfoVO getCompanyInfoByTaxNumber(String taxNumber) {
        if (StrUtil.isBlank(taxNumber)) {
            return null;
        }
        var qo = new CommonSystemCompanyQueryCriteria();
        qo.setEqualsTaxNumber(taxNumber);
        List<SystemCompanyEntity> companies = companyService.queryAndPage(qo, null).getData();
        if (CollUtil.isEmpty(companies)) {
            return null;
        }
        return convertToVO(companies.get(0));
    }

    @Override
    public List<RemoteCompanyInfoVO> getChildCompanies(Long parentId) {
        if (parentId == null) {
            return Collections.emptyList();
        }
        List<SystemCompanyEntity> childCompanies = companyTreeStructService.getChildren(parentId).stream().map(node -> {
            return companyTreeStructService.queryByNodeId(node.getNodeId(), node.getTreeGroup());
        }).collect(Collectors.toList());
        if (CollUtil.isEmpty(childCompanies)) {
            return Collections.emptyList();
        }
        return childCompanies.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    /**
     * 将公司实体转换为VO
     *
     * @param entity 公司实体
     * @return 公司VO
     */
    private RemoteCompanyInfoVO convertToVO(SystemCompanyEntity entity) {
        RemoteCompanyInfoVO vo = new RemoteCompanyInfoVO();
        vo.setCompanyId(entity.getId());
        vo.setCompanyName(entity.getCompanyName());
        vo.setLogoImage(entity.getLogoImage());
        vo.setTaxNumber(entity.getTaxNumber());
        vo.setDescription(entity.getDescription());
        vo.setFlag(entity.getFlag());
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        vo.setChildrenCount(entity.getChildrenCount());
        return vo;
    }
}

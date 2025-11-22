package com.tbs.app.service.company;

import com.tbs.shared.interfaces.service.IServiceTemplate;
import com.tbs.shared.pojo.entity.SystemCompanyEntity;

import java.util.List;

public interface ISystemCompanyService extends IServiceTemplate<SystemCompanyEntity, Long> {

    /**
     * 添加子公司
     *
     * @param parentId 父公司ID
     * @param childId  子公司ID
     */
    void appendChildCompany(Long parentId, Long childId);

    /**
     * 获取下一级子公司列表
     *
     * @param id
     * @return
     */
    List<SystemCompanyEntity> getChildrenCompanies(Long id);

}

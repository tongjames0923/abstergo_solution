package com.tbs.app.service.user.model;

import com.tbs.shared.pojo.entity.SystemCompanyEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface ISystemUserDetails extends UserDetails {

    /**
     * 获取当前用户所属公司列表
     *
     * @return
     */
    List<SystemCompanyEntity> getCompanies();

    /**
     * 获取当前用户所属公司
     *
     * @return
     */
    SystemCompanyEntity getCurrentCompany();

    /**
     * 设置当前用户所属公司
     *
     * @param company
     */
    void setCurrentCompany( SystemCompanyEntity company);
}

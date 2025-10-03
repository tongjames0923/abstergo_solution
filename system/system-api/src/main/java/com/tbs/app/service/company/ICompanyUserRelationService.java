package com.tbs.app.service.company;

import com.tbs.shared.pojo.entity.SystemCompanyEntity;
import com.tbs.shared.pojo.entity.SystemUserEntity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ICompanyUserRelationService {

    /**
     * 查询用户所属公司
     *
     * @param userId
     * @return
     */
    List<SystemCompanyEntity> queryUserCompanies(@NotNull Long userId);

    /**
     * 查询公司所属用户
     *
     * @param companyId
     * @return
     */
    List<SystemUserEntity> queryCompanyUsers(@NotNull Long companyId);

    /**
     * 添加用户到公司
     *
     * @param userId
     * @param companyId
     */
    void addUserToCompany(@NotNull Long userId, @NotNull Long companyId);

    /**
     * 删除用户从公司
     *
     * @param userId
     * @param companyId
     */
    void deleteUserFromCompany(@NotNull Long userId, @NotNull Long companyId);
}

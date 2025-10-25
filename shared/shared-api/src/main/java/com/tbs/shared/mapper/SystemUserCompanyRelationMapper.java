package com.tbs.shared.mapper;

import com.tbs.shared.pojo.entity.SystemCompanyEntity;
import com.tbs.shared.pojo.entity.SystemUserCompanyRelationEntity;
import com.tbs.shared.pojo.entity.SystemUserEntity;
import com.tbs.sql.mapper.IFrameworkMapper;
import org.apache.ibatis.annotations.Select;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 系统用户公司关系Mapper
 */
@Repository
public interface SystemUserCompanyRelationMapper extends IFrameworkMapper<SystemUserCompanyRelationEntity, Long> {

    @Select(
        "SELECT sc.* FROM sys_company sc RIGHT JOIN system_user_company_relation sucr ON sucr.user_id=#{userId}  AND sucr.company_id=sc.id;")
    List<SystemCompanyEntity> queryUserCompanies(@NotNull Long userId);

    @Select(
        "SELECT * FROM sys_user su RIGHT JOIN system_user_company_relation sucr ON sucr.company_id=#{companyId}  and sucr.user_id=su.id")
    List<SystemUserEntity> queryCompanyUsers(@NotNull Long companyId);

    @Select("SELECT * FROM sys_user_company_relation WHERE user_id=#{userId} AND company_id=#{companyId}")
    List<SystemUserCompanyRelationEntity> queryUserCompanyRelation(@NotNull Long userId, @NotNull Long companyId);
}

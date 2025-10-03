package com.tbs.app.service.impl.company;

import com.tbs.app.service.company.ICompanyUserRelationService;
import com.tbs.app.service.company.ISystemCompanyService;
import com.tbs.app.service.user.ISystemUserService;
import com.tbs.cache.annotation.ExpireOn;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.CollUtil;
import com.tbs.shared.mapper.SystemUserCompanyRelationMapper;
import com.tbs.shared.pojo.entity.SystemCompanyEntity;
import com.tbs.shared.pojo.entity.SystemUserCompanyRelationEntity;
import com.tbs.shared.pojo.entity.SystemUserEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class SimpleCompanyUserRelationService implements ICompanyUserRelationService {
    @Resource
    private SystemUserCompanyRelationMapper companyRelationMapper;
    @Resource
    private ISystemCompanyService companyService;

    @Resource
    private ISystemUserService userService;

    @Cacheable(cacheNames = "user_companies", key = "#userId")
    @ExpireOn(value = 3, timeUnit = TimeUnit.MINUTES)
    @Override
    public List<SystemCompanyEntity> queryUserCompanies(@NotNull Long userId) {
        SystemUserEntity user = userService.queryById(userId);
        AssertUtil.notNull(user, "用户不存在");
        return companyRelationMapper.queryUserCompanies(userId);
    }

    @Override
    public List<SystemUserEntity> queryCompanyUsers(@NotNull Long companyId) {
        SystemCompanyEntity company = companyService.queryById(companyId);
        AssertUtil.notNull(company, "公司不存在");
        return companyRelationMapper.queryCompanyUsers(companyId);
    }

    @CacheEvict(cacheNames = "user_companies", key = "#userId")
    @Override
    public void addUserToCompany(@NotNull Long userId, @NotNull Long companyId) {
        SystemUserEntity user = userService.queryById(userId);
        AssertUtil.notNull(user, "用户不存在");
        SystemCompanyEntity company = companyService.queryById(companyId);
        AssertUtil.notNull(company, "公司不存在");
        var relations = companyRelationMapper.queryUserCompanyRelation(userId, companyId);
        AssertUtil.isTrue(CollUtil.isEmpty(relations), "用户已加入 {} ", company.getCompanyName());
        SystemUserCompanyRelationEntity relation = new SystemUserCompanyRelationEntity();
        relation.setUserId(userId);
        relation.setCompanyId(companyId);
        companyRelationMapper.insert(relation);
    }

    @Override
    @CacheEvict(cacheNames = "user_companies", key = "#userId")
    public void deleteUserFromCompany(@NotNull Long userId, @NotNull Long companyId) {
        SystemUserEntity user = userService.queryById(userId);
        AssertUtil.notNull(user, "用户不存在");
        SystemCompanyEntity company = companyService.queryById(companyId);
        AssertUtil.notNull(company, "公司不存在");
        var relations = companyRelationMapper.queryUserCompanyRelation(userId, companyId);
        AssertUtil.isTrue(CollUtil.isNotEmpty(relations), "用户未加入 {} ", company.getCompanyName());
        companyRelationMapper.deleteByIdList(
            relations.stream().map(SystemUserCompanyRelationEntity::getId).collect(Collectors.toList()));
    }
}

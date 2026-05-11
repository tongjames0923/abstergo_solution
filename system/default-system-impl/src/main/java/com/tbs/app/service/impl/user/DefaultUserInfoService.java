package com.tbs.app.service.impl.user;

import com.tbs.app.service.company.ICompanyUserRelationService;
import com.tbs.app.service.department.IDepartmentUserRelationService;
import com.tbs.app.service.role.IUserRoleRelationService;
import com.tbs.app.service.user.ISystemUserService;
import com.tbs.app.service.user.model.impl.DefaultGrantedAuthority;
import com.tbs.app.service.user.model.impl.DefaultSystemUserDetails;
import com.tbs.app.vo.DepartmentInfoVO;
import com.tbs.cache.expire.annotation.ExpireOn;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.shared.enums.SystemUserStatusEnum;
import com.tbs.shared.mapper.SystemCompanyMapper;
import com.tbs.shared.pojo.entity.SystemCompanyEntity;
import com.tbs.shared.pojo.entity.SystemDepartmentEntity;
import com.tbs.web.exception.SimpleAuthException;
import com.tbs.web.security.user.IUserInfoService;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 默认用户信息服务
 */
public class DefaultUserInfoService implements IUserInfoService {

    @Resource
    private ISystemUserService systemUserService;
    @Resource
    private IUserRoleRelationService userRoleRelationService;
    @Resource
    private ICompanyUserRelationService companyUserRelationService;
    @Resource
    private IDepartmentUserRelationService departmentUserRelationService;
    @Resource
    private SystemCompanyMapper companyMapper;

    @Override
    @Cacheable(cacheNames = IUserInfoService.USER_DETAILS_CACHE_NAME, key = "#uniqueKey")
    @ExpireOn(value = 5, timeUnit = TimeUnit.MINUTES)
    public UserDetails loadUserInfoByUniqueKey(@NotNull String uniqueKey) {
        var users = systemUserService.queryByUserName(uniqueKey);
        AssertUtil.isTrue(CollUtil.isEmpty(users), () -> new SimpleAuthException("用户不存在"));
        var user = CollUtil.of(users).findFirstOrNull();

        var builder = DefaultSystemUserDetails.builder().userName(user.getUserName()).password(user.getPassword())
            .accountNonLocked(!SystemUserStatusEnum.LOCKED.getCode().equals(user.getFlag())).accountNonExpired(true)
            .credentialsNonExpired(true).enabled(SystemUserStatusEnum.ACTIVITE.getCode().equals(user.getFlag()));
        var roles = userRoleRelationService.queryUserRoles(user.getId());
        if (CollUtil.isNotEmpty(roles)) {
            builder.authorities(roles.stream().map(role -> {
                return new DefaultGrantedAuthority(role.getId().toString());
            }).collect(Collectors.toList()));
        }
        var companies = companyUserRelationService.queryUserCompanies(user.getId());
        if (CollUtil.isNotEmpty(companies)) {
            builder.companies(companies);
        }
        var departments = departmentUserRelationService.queryUserDepartments(user.getId());
        if (CollUtil.isNotEmpty(departments)) {
            builder.departments(convertToDepartmentVOs(departments));
        }
        return builder.build();
    }

    /**
     * 将部门实体列表转换为部门VO列表
     *
     * @param departments 部门实体列表
     * @return 部门VO列表
     */
    private List<DepartmentInfoVO> convertToDepartmentVOs(List<SystemDepartmentEntity> departments) {
        if (CollUtil.isEmpty(departments)) {
            return CollUtil.newArrayList();
        }
        // 收集所有公司ID
        List<Long> companyIds = departments.stream()
            .map(SystemDepartmentEntity::getCompanyId)
            .distinct()
            .collect(Collectors.toList());

        // 批量查询公司信息
        List<SystemCompanyEntity> companies = companyIds.stream()
            .map(companyMapper::selectByPrimaryKey)
            .filter(company -> company != null)
            .collect(Collectors.toList());

        // 构建公司ID到公司实体的映射
        java.util.Map<Long, SystemCompanyEntity> companyMap = companies.stream()
            .collect(Collectors.toMap(SystemCompanyEntity::getId, company -> company));

        // 转换为VO
        return departments.stream().map(dept -> {
            DepartmentInfoVO vo = new DepartmentInfoVO();
            vo.setDepartmentId(dept.getId());
            vo.setDepartmentName(dept.getDepartmentName());
            vo.setDepartmentCode(dept.getDepartmentCode());
            vo.setManager(dept.getManager());
            vo.setDescription(dept.getDescription());
            vo.setFlag(dept.getFlag());
            vo.setCreateTime(dept.getCreateTime());
            vo.setUpdateTime(dept.getUpdateTime());
            vo.setTreeRootFlag(dept.getTreeRootFlag());
            vo.setCompanyId(dept.getCompanyId());

            // 设置公司信息
            SystemCompanyEntity company = companyMap.get(dept.getCompanyId());
            if (company != null) {
                vo.setCompanyName(company.getCompanyName());
                vo.setCompanyLogoImage(company.getLogoImage());
                vo.setCompanyTaxNumber(company.getTaxNumber());
            }

            return vo;
        }).collect(Collectors.toList());
    }
}

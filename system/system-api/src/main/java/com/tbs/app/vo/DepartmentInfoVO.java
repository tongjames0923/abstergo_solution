package com.tbs.app.vo;

import com.tbs.app.service.company.ISystemCompanyService;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.shared.pojo.entity.SystemCompanyEntity;
import com.tbs.shared.pojo.entity.SystemDepartmentEntity;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 部门信息VO
 * <p>
 * 用于封装部门信息，包含部门基本信息和所属公司信息。
 * </p>
 *
 * @author abstergo
 */
@Data
public class DepartmentInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 部门ID
     */
    private Long departmentId;

    /**
     * 部门名称
     */
    private String departmentName;

    /**
     * 部门编码
     */
    private String departmentCode;

    /**
     * 部门负责人
     */
    private String manager;

    /**
     * 部门描述
     */
    private String description;

    /**
     * 部门状态标记
     * @see com.tbs.shared.enums.SystemDepartmentFlagEnum
     */
    private Integer flag;

    /**
     * 部门创建时间
     */
    private Date createTime;

    /**
     * 部门更新时间
     */
    private Date updateTime;

    /**
     * 是否是树根节点
     */
    private Integer treeRootFlag;

    /**
     * 子部门数量
     */
    private Long childrenCount;

    /**
     * 所属公司ID
     */
    private Long companyId;

    /**
     * 所属公司名称
     */
    private String companyName;

    /**
     * 所属公司logo URL
     */
    private String companyLogoImage;

    /**
     * 所属公司税号
     */
    private String companyTaxNumber;

    /**
     * 将部门实体列表转换为部门VO列表
     *
     * @param departments 部门实体列表
     * @return 部门VO列表
     */
    public static final List<DepartmentInfoVO> convertToDepartmentVOs(List<SystemDepartmentEntity> departments) {
        if (CollUtil.isEmpty(departments)) {
            return CollUtil.newArrayList();
        }
        // 收集所有公司ID
        List<Long> companyIds =
            departments.stream().filter(dept -> dept != null).map(SystemDepartmentEntity::getCompanyId).distinct()
                .collect(Collectors.toList());

        if (CollUtil.isEmpty(companyIds)) {
            return CollUtil.newArrayList();
        }
        // 批量查询公司信息
        List<SystemCompanyEntity> companies =
            companyIds.stream().map(SpringUtil.getBean(ISystemCompanyService.class)::queryById)
                .filter(company -> company != null).collect(Collectors.toList());

        // 构建公司ID到公司实体的映射
        java.util.Map<Long, SystemCompanyEntity> companyMap =
            companies.stream().collect(Collectors.toMap(SystemCompanyEntity::getId, company -> company));

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

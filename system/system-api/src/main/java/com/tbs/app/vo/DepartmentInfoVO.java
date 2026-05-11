package com.tbs.app.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

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
}

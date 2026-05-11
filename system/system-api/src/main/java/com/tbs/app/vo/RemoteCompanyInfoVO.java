package com.tbs.app.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 默认的远程公司信息VO
 * <p>
 * 默认的远程公司信息VO，用于封装远程公司信息。
 * </p>
 *
 * @author abstergo
 */
@Data
public class RemoteCompanyInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    /**
     * 公司ID
     */
    private Long companyId;
    
    /**
     * 公司名称
     */
    private String companyName;
    
    /**
     * 公司logo URL
     */
    private String logoImage;
    
    /**
     * 公司税号
     */
    private String taxNumber;
    
    /**
     * 公司描述
     */
    private String description;
    
    /**
     * 公司状态标记
     * @see com.tbs.shared.enums.SystemCompanyFlagEnum
     */
    private Integer flag;
    
    /**
     * 公司创建时间
     */
    private Date createTime;
    
    /**
     * 公司更新时间
     */
    private Date updateTime;
    
    /**
     * 是否是树根节点
     */
    private Integer treeRootFlag;
    
    /**
     * 子公司数量
     */
    private Long childrenCount;
}

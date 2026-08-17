package com.tbs.shared.pojo.criteria;

import com.tbs.shared.pojo.entity.version.AppVersionCompatibilityPolicyEntity;
import com.tbs.sql.annotation.condition.QueryField;
import com.tbs.sql.annotation.mapping.QueryMapping;
import com.tbs.sql.annotation.order.QueryOrderField;
import com.tbs.sql.constants.OrderConstants;
import com.tbs.sql.criteria.impl.DefaultEntityCriteria;
import com.tbs.sql.enums.ContrastEnum;
import com.tbs.sql.order.impl.OrderedModel;
import lombok.Data;

import java.util.List;

/**
 * 应用版本兼容策略查询条件
 * <p>
 * 提供对 AppVersionCompatibilityPolicyEntity 实体的查询条件支持
 *
 * @author abstergo
 */
@Data
public class AppVersionCompatibilityPolicyCriteria extends DefaultEntityCriteria {

    public AppVersionCompatibilityPolicyCriteria() {
        super(AppVersionCompatibilityPolicyEntity.class);
    }

    /**
     * 按ID精确查询
     */
    @QueryMapping(value = "id")
    @QueryField(contrast = ContrastEnum.EQUAL)
    private Long idEquals;

    /**
     * 按源应用标识精确查询
     */
    @QueryMapping(value = "source_app_key")
    @QueryField(contrast = ContrastEnum.EQUAL)
    private String sourceAppKeyEquals;

    /**
     * 按目标应用标识精确查询
     */
    @QueryMapping(value = "target_app_key")
    @QueryField(contrast = ContrastEnum.EQUAL)
    private String targetAppKeyEquals;

    /**
     * 按目标平台精确查询
     */
    @QueryMapping(value = "target_platform")
    @QueryField(contrast = ContrastEnum.EQUAL)
    private String targetPlatformEquals;

    /**
     * 按动作类型精确查询
     */
    @QueryMapping(value = "action")
    @QueryField(contrast = ContrastEnum.EQUAL)
    private Integer actionEquals;

    /**
     * 按状态IN查询
     */
    @QueryMapping(value = "flag")
    @QueryField(contrast = ContrastEnum.IN)
    private List<Integer> flagIn;

    /**
     * 按创建时间排序
     */
    @QueryOrderField(mappedName = "create_time", order = OrderConstants.ORDERED_MODEL)
    private OrderedModel createTimeOrder;
}

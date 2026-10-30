package com.tbs.shared.pojo.criteria;

import com.tbs.sql.annotation.LogicDeleteEntity;
import com.tbs.shared.pojo.entity.version.AppVersionEntity;
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
 * 应用版本查询条件
 * <p>
 * 提供对 AppVersionEntity 实体的查询条件支持，包括精确查询、模糊查询、范围查询和排序功能
 *
 * @author abstergo
 */
@Data
@LogicDeleteEntity(activeFlagValues = {0, 1, 2, -1, -2})
public class AppVersionCriteria extends DefaultEntityCriteria {

    public AppVersionCriteria() {
        super(AppVersionEntity.class);
    }

    /**
     * 按ID精确查询
     */
    @QueryMapping(value = "id")
    @QueryField(contrast = ContrastEnum.EQUAL)
    private Long idEquals;

    /**
     * 按应用标识精确查询
     */
    @QueryMapping(value = "app_key")
    @QueryField(contrast = ContrastEnum.EQUAL)
    private String appKeyEquals;

    /**
     * 按外部版本名称模糊查询
     */
    @QueryMapping(value = "version_name")
    @QueryField(contrast = ContrastEnum.LLIKE)
    private String versionNameLike;

    /**
     * 按内部版本号精确查询
     */
    @QueryMapping(value = "version_code")
    @QueryField(contrast = ContrastEnum.EQUAL)
    private Long versionCodeEquals;

    /**
     * 按版本类型精确查询
     */
    @QueryMapping(value = "version_type")
    @QueryField(contrast = ContrastEnum.EQUAL)
    private Integer versionTypeEquals;

    /**
     * 按平台精确查询
     */
    @QueryMapping(value = "platform")
    @QueryField(contrast = ContrastEnum.EQUAL)
    private String platformEquals;

    /**
     * 按生命周期状态IN查询
     */
    @QueryMapping(value = "flag")
    @QueryField(contrast = ContrastEnum.IN)
    private List<Integer> flagIn;

    /**
     * 按创建时间排序
     */
    @QueryOrderField(mappedName = "create_time", order = OrderConstants.ORDERED_MODEL)
    private OrderedModel createTimeOrder;

    /**
     * 按版本号排序
     */
    @QueryOrderField(mappedName = "version_code", order = OrderConstants.ORDERED_MODEL)
    private OrderedModel versionCodeOrder;
}

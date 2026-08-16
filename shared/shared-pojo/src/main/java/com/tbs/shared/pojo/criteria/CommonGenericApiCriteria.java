package com.tbs.shared.pojo.criteria;

import com.tbs.shared.annotations.LogicDeleteEntity;
import com.tbs.shared.pojo.entity.SystemGenericApiEntity;
import com.tbs.sql.annotation.condition.QueryField;
import com.tbs.sql.annotation.mapping.QueryMapping;
import com.tbs.sql.annotation.order.QueryOrderField;
import com.tbs.sql.constants.OrderConstants;
import com.tbs.sql.criteria.impl.DefaultEntityCriteria;
import com.tbs.sql.enums.ContrastEnum;
import com.tbs.sql.order.impl.OrderedModel;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 泛化API配置查询条件
 * <p>
 * 提供对SystemGenericApiEntity实体的各种查询条件支持， 包括精确查询、模糊查询、范围查询和排序功能。
 * </p>
 *
 * @author abstergo
 */
@Data
@LogicDeleteEntity(activeFlagValues = {1, -1})
public class CommonGenericApiCriteria extends DefaultEntityCriteria {

    public CommonGenericApiCriteria() {
        super(SystemGenericApiEntity.class);
    }

    /**
     * 按ID精确查询条件
     */
    @QueryMapping(value = "id")
    @QueryField(contrast = ContrastEnum.EQUAL)
    private Long idEquals;

    /**
     * 按API ID精确查询条件
     */
    @QueryMapping(value = "api_id")
    @QueryField(contrast = ContrastEnum.EQUAL)
    private String apiIdEquals;

    /**
     * 按API名称模糊查询条件
     */
    @QueryMapping(value = "api_name")
    @QueryField(contrast = ContrastEnum.LLIKE)
    private String apiNameLike;

    /**
     * 按服务应用名精确查询条件
     */
    @QueryMapping(value = "app_name")
    @QueryField(contrast = ContrastEnum.EQUAL)
    private String appNameEquals;

    /**
     * 按服务应用名模糊查询条件
     */
    @QueryMapping(value = "app_name")
    @QueryField(contrast = ContrastEnum.LLIKE)
    private String appNameLike;

    /**
     * 按接口名模糊查询条件
     */
    @QueryMapping(value = "interface_name")
    @QueryField(contrast = ContrastEnum.LLIKE)
    private String interfaceNameLike;

    /**
     * 按方法名精确查询条件
     */
    @QueryMapping(value = "method_name")
    @QueryField(contrast = ContrastEnum.EQUAL)
    private String methodNameEquals;

    /**
     * 按是否需要认证精确查询条件
     */
    @QueryMapping(value = "need_auth")
    @QueryField(contrast = ContrastEnum.EQUAL)
    private Boolean needAuthEquals;

    /**
     * 按状态标志IN查询条件
     */
    @QueryMapping(value = "flag")
    @QueryField(contrast = ContrastEnum.IN)
    private List<Integer> flagIn;

    /**
     * 按ID集合IN查询条件
     */
    @QueryMapping(value = "id")
    @QueryField(contrast = ContrastEnum.IN)
    private List<Long> idIn;

    /**
     * 按API ID模糊查询条件
     */
    @QueryMapping(value = "api_id")
    @QueryField(contrast = ContrastEnum.LLIKE)
    private String apiIdLike;

    /**
     * 按API ID集合IN查询条件
     */
    @QueryMapping(value = "api_id")
    @QueryField(contrast = ContrastEnum.IN)
    private List<String> apiIdIn;

    /**
     * 按API名称精确查询条件
     */
    @QueryMapping(value = "api_name")
    @QueryField(contrast = ContrastEnum.EQUAL)
    private String apiNameEquals;

    /**
     * 按接口全限定名精确查询条件
     */
    @QueryMapping(value = "interface_name")
    @QueryField(contrast = ContrastEnum.EQUAL)
    private String interfaceNameEquals;

    /**
     * 按方法名模糊查询条件
     */
    @QueryMapping(value = "method_name")
    @QueryField(contrast = ContrastEnum.LLIKE)
    private String methodNameLike;

    /**
     * 按API版本精确查询条件
     */
    @QueryMapping(value = "version")
    @QueryField(contrast = ContrastEnum.EQUAL)
    private Integer versionEquals;

    /**
     * 按描述信息模糊查询条件
     */
    @QueryMapping(value = "description")
    @QueryField(contrast = ContrastEnum.LLIKE)
    private String descriptionLike;

    /**
     * 按预认证Bean模糊查询条件
     */
    @QueryMapping(value = "pre_auth_beans")
    @QueryField(contrast = ContrastEnum.LLIKE)
    private String preAuthBeansLike;

    /**
     * 按创建者ID精确查询条件
     */
    @QueryMapping(value = "create_by")
    @QueryField(contrast = ContrastEnum.EQUAL)
    private Long createByEquals;

    /**
     * 按更新者ID精确查询条件
     */
    @QueryMapping(value = "update_by")
    @QueryField(contrast = ContrastEnum.EQUAL)
    private Long updateByEquals;

    /**
     * 按创建时间大于查询条件
     */
    @QueryMapping(value = "create_time")
    @QueryField(contrast = ContrastEnum.GREATER)
    private Date createTimeGreaterThan;

    /**
     * 按创建时间小于查询条件
     */
    @QueryMapping(value = "create_time")
    @QueryField(contrast = ContrastEnum.LESS)
    private Date createTimeLessThan;

    /**
     * 按更新时间大于查询条件
     */
    @QueryMapping(value = "update_time")
    @QueryField(contrast = ContrastEnum.GREATER)
    private Date updateTimeGreaterThan;

    /**
     * 按更新时间小于查询条件
     */
    @QueryMapping(value = "update_time")
    @QueryField(contrast = ContrastEnum.LESS)
    private Date updateTimeLessThan;

    /**
     * 按创建时间排序条件
     */
    @QueryOrderField(mappedName = "create_time", order = OrderConstants.ORDERED_MODEL)
    private OrderedModel createTimeOrder;

    /**
     * 按更新时间排序条件
     */
    @QueryOrderField(mappedName = "update_time", order = OrderConstants.ORDERED_MODEL)
    private OrderedModel updateTimeOrder;
}

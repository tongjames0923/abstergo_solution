package com.tbs.shared.pojo.criteria;

import com.tbs.shared.pojo.entity.version.AppPackageEntity;
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
 * 应用安装包查询条件
 * <p>
 * 提供对 AppPackageEntity 实体的查询条件支持
 *
 * @author abstergo
 */
@Data
public class AppPackageCriteria extends DefaultEntityCriteria {

    public AppPackageCriteria() {
        super(AppPackageEntity.class);
    }

    /**
     * 按ID精确查询
     */
    @QueryMapping(value = "id")
    @QueryField(contrast = ContrastEnum.EQUAL)
    private Long idEquals;

    /**
     * 按所属版本ID精确查询
     */
    @QueryMapping(value = "version_id")
    @QueryField(contrast = ContrastEnum.EQUAL)
    private Long versionIdEquals;

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

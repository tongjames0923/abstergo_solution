package com.tbs.shared.pojo.criteria;

import com.tbs.shared.pojo.entity.ThirdPartyAppEntity;
import com.tbs.sql.annotation.condition.QueryField;
import com.tbs.sql.annotation.mapping.QueryMapping;
import com.tbs.sql.criteria.impl.DefaultEntityCriteria;
import com.tbs.sql.enums.ConnectorEnum;
import com.tbs.sql.enums.ContrastEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 第三方应用查询类
 *
 * @author Boolean
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommonThirdPartyAppCriteria extends DefaultEntityCriteria {
    /**
     * 构造方法
     */
    public CommonThirdPartyAppCriteria() {
        super(ThirdPartyAppEntity.class);
    }

    /**
     * 按应用标识精确匹配（等于）。
     * <p>对应 {@code third_party_app.app_id}，用于依据请求中的应用标识定位第三方应用。</p>
     */
    @QueryMapping(value = "app_id")
    @QueryField(connector = ConnectorEnum.AND, contrast = ContrastEnum.EQUAL)
    private String EqAppId;

    /**
     * 按代理用户 ID 集合匹配（IN）。
     * <p>对应 {@code third_party_app.proxy_id}，用于批量检索某批代理用户所关联的第三方应用。</p>
     */
    @QueryMapping(value = "proxy_id")
    @QueryField(connector = ConnectorEnum.AND, contrast = ContrastEnum.IN)
    private List<Long> proxyUserIdList;

}

package com.tbs.web.security.issuance.center.impl;

import com.tbs.common.util.EnumUtil;
import com.tbs.common.util.framework.UuidUtil;
import com.tbs.web.config.property.WebAuthProperty;
import com.tbs.web.enums.TokenCreateTypeEnum;
import com.tbs.web.enums.TokenStatusEnum;
import com.tbs.web.enums.TokenTypeEnum;
import com.tbs.web.security.issuance.center.AbstractDataHolderTokenIssuanceCenter;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.security.token.model.SimpleTokenModel;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 默认数据持有者令牌颁发中心
 *
 * @author abstergo
 */
public class SimpleDataHolderTokenIssuanceCenter extends AbstractDataHolderTokenIssuanceCenter {

    @Resource
    WebAuthProperty webAuthProperty;

    @Override
    protected ITokenModel createModel(@NotNull UserDetails userDetails, int tokenType, int tokenCreateType) {
        Date now = new Date();
        return new SimpleTokenModel(UuidUtil.getInstance().getUuidString(), userDetails.getUsername(),
            TokenStatusEnum.ACTIVE, EnumUtil.valueOf(TokenTypeEnum.class, tokenType),
            recommendExpireTime(now, tokenType), now, EnumUtil.valueOf(TokenCreateTypeEnum.class, tokenCreateType));

    }

}

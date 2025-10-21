package com.tbs.app.web.model.bridge;

import com.tbs.common.data.IDataBridge;
import com.tbs.common.util.function.EnumUtil;
import com.tbs.web.enums.TokenCreateTypeEnum;
import com.tbs.web.enums.TokenStatusEnum;
import com.tbs.web.enums.TokenTypeEnum;
import com.tbs.app.web.model.SimpleTokenModel;
import com.tbs.app.web.model.dto.TokenModelDto;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

@Component
public class SimpleTokenDtoBridge implements IDataBridge<TokenModelDto, SimpleTokenModel> {
    @Override
    public TokenModelDto bridge(@NotNull SimpleTokenModel obj) {
        return new TokenModelDto(obj);
    }

    @Override
    public SimpleTokenModel unbridge(@NotNull TokenModelDto obj) {
        return new SimpleTokenModel(obj.getToken(), obj.getUserName(),
            obj.getStatus() != null ? EnumUtil.valueOf(TokenStatusEnum.class, obj.getStatus()) : null,
            obj.getTokenType() == null ? null : EnumUtil.valueOf(TokenTypeEnum.class, obj.getTokenType()),
            obj.getExpireTime(), obj.getCreateTime(),
            obj.getCreateType() == null ? null : EnumUtil.valueOf(TokenCreateTypeEnum.class, obj.getCreateType()),
            obj.getSessionId(), obj.getMacAddr(), obj.getIpAddr());
    }
}

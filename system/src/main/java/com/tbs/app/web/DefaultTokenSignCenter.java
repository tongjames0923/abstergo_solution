package com.tbs.app.web;

import com.tbs.app.service.business.token.ISystemTokenService;
import com.tbs.common.util.CollUtil;
import com.tbs.common.util.EnumUtil;
import com.tbs.common.util.framework.UuidUtil;
import com.tbs.shared.pojo.criteria.CommonTokenQueryCriteria;
import com.tbs.shared.pojo.entity.SystemTokenEntity;
import com.tbs.shared.util.NetUtil;
import com.tbs.web.config.property.WebAuthProperty;
import com.tbs.web.enums.TokenCreateTypeEnum;
import com.tbs.web.enums.TokenStatusEnum;
import com.tbs.web.enums.TokenTypeEnum;
import com.tbs.web.security.issuance.center.AbstractTokenIssuanceCenter;
import com.tbs.web.security.token.ITokenModel;
import com.tbs.web.security.token.model.SimpleTokenModel;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.springframework.security.core.userdetails.UserDetails;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DefaultTokenSignCenter extends AbstractTokenIssuanceCenter {

    @Resource
    ISystemTokenService tokenService;

    @Resource
    WebAuthProperty webAuthProperty;

    @Data
    private class AdapterTokenModel extends SimpleTokenModel {
        private String macAddr;
        private String ipAddr;

        public AdapterTokenModel(String token, String userName, TokenStatusEnum status, TokenTypeEnum type,
            Date expireTime, Date createTime, TokenCreateTypeEnum createType, String macAddr, String ipAddr) {
            super(token, userName, status, type, expireTime, createTime, createType);
            this.macAddr = macAddr;
            this.ipAddr = ipAddr;
        }
    }

    private ITokenModel toModel(SystemTokenEntity entity) {
        return new AdapterTokenModel(entity.getToken(), entity.getUserName(),
            EnumUtil.valueOf(TokenStatusEnum.class, entity.getFlag()),
            EnumUtil.valueOf(TokenTypeEnum.class, entity.getTokenType()), entity.getExpireTime(),
            entity.getCreateTime(), EnumUtil.valueOf(TokenCreateTypeEnum.class, entity.getCreateType()),
            entity.getMacAddress(), entity.getIpAddress());
    }

    private SystemTokenEntity toEntity(ITokenModel model) {
        var entity = new SystemTokenEntity();
        entity.setToken(model.getToken());
        entity.setUserName(model.getUserName());
        entity.setFlag(model.getStatus().getCode());
        entity.setTokenType(model.getTokenType().getCode());
        entity.setExpireTime(model.getExpireTime());
        entity.setCreateTime(model.getCreateTime());
        entity.setCreateType(model.getCreateType().getCode());
        if (model instanceof AdapterTokenModel) {
            var m = (AdapterTokenModel)model;
            entity.setMacAddress(m.getMacAddr());
            entity.setIpAddress(m.getIpAddr());
        }
        return entity;
    }

    @Override
    protected ITokenModel getModel(@NotNull String token) {
        var vals = tokenService.queryByToken(token);
        if (CollUtil.isEmpty(vals)) {
            return null;
        }
        var val = CollUtil.getFirst(vals);
        return toModel(val);
    }

    @Override
    protected void saveToken(ITokenModel model) {
        var ls = tokenService.queryByToken(model.getToken());
        if (CollUtil.isEmpty(ls)) {
            tokenService.save(toEntity(model));
        } else {
            SystemTokenEntity entity = toEntity(model);
            entity.setId(CollUtil.getFirst(ls).getId());
            tokenService.save(entity);
        }
    }

    @Override
    protected void saveManageInfo(String userName, int tokenType, String token) {

    }

    /**
     * 获取用户名、token类型对应的token列表,仅28天内有效
     *
     * @param userName
     * @param tokenType
     * @return
     */
    @Override
    protected Collection<ITokenModel> getUserNameAndTokenType(@NotNull String userName, int tokenType) {
        return tokenService.query(
                new CommonTokenQueryCriteria().setUserNameEquals(userName).setTokenTypeEquals(tokenType)
                    .setCreateTimeAfter(new Date(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(28))), null).stream()
            .map(this::toModel).collect(Collectors.toList());
    }

    @Resource
    HttpServletRequest request;

    @Override
    protected ITokenModel createModel(@NotNull UserDetails userDetails, int tokenType, int tokenCreateType) {
        Date now = new Date();
        return new AdapterTokenModel(UuidUtil.getInstance().getUuidString(), userDetails.getUsername(),
            TokenStatusEnum.ACTIVE, EnumUtil.valueOf(TokenTypeEnum.class, tokenType),
            recommendExpireTime(now, tokenType), now, EnumUtil.valueOf(TokenCreateTypeEnum.class, tokenCreateType),
            NetUtil.getMac(request), NetUtil.getIp(request));
    }
}

package com.tbs.app.service.business.auth.impl

import com.tbs.common.IStartup
import com.tbs.common.data.IDataHolder
import com.tbs.common.data.IDataHolderExpireActuator
import com.tbs.common.data.IDataHolderManager
import com.tbs.common.exception.FormattedRuntimeException
import com.tbs.web.config.property.WebAuthProperty
import com.tbs.web.exception.NotFoundTokenException
import com.tbs.web.security.ITokenManager
import com.tbs.web.security.ITokenModel
import com.tbs.web.security.IUserInfoService
import org.springframework.context.annotation.Lazy
import org.springframework.security.core.userdetails.UserDetails
import javax.annotation.Resource


class DefaultTokenManager : ITokenManager, IStartup {


    @Lazy
    @Resource
    private lateinit var userInfoService: IUserInfoService

    private var dataHolder: IDataHolder? = null

    @Resource
    private lateinit var dataHolderManager: IDataHolderManager


    @Resource
    private lateinit var webAuthProperty: WebAuthProperty

    @Resource
    private lateinit var dataHolderExpiredActuator: IDataHolderExpireActuator;

    private fun dataHolder(): IDataHolder {
        return dataHolder ?: throw FormattedRuntimeException("令牌数据源不存在");
    }

    override fun submitToken(
        token: ITokenModel, userDetails: UserDetails
    ) {
        val set = dataHolder().set(token.token, userDetails, false);
        if (!set) {
            throw FormattedRuntimeException("令牌添加失败")
        }
        dataHolderExpiredActuator.expire(dataHolder(), token.token, token.expireTime);
    }

    override fun removeToken(token: ITokenModel) {
        dataHolder().remove(token.token)
    }

    override fun loadUserInfoByToken(token: ITokenModel): UserDetails {
        return dataHolder().get<UserDetails>(token.token)
            .orElseThrow { return@orElseThrow NotFoundTokenException("令牌不存在") };
    }

    override fun startUp() {
        dataHolder = dataHolderManager.getDataHolder(webAuthProperty.tokenDataSourceName)
            .orElseThrow { return@orElseThrow FormattedRuntimeException("令牌数据源不存在") };
    }
}

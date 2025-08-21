package com.tbs.app.service.business.auth.impl

import com.tbs.app.service.business.user.IPasswordEncrypt
import com.tbs.common.error.AssertUtil
import com.tbs.common.util.StrUtil
import com.tbs.common.util.framework.UuidUtil
import com.tbs.web.config.property.WebAuthProperty
import com.tbs.web.exception.InvalidTokenException
import com.tbs.web.exception.SimpleAuthException
import com.tbs.web.security.token.ITokenManager
import com.tbs.web.security.token.ITokenModel
import com.tbs.web.security.user.IUserAuthService
import com.tbs.web.security.user.IUserInfoService
import com.tbs.web.security.token.model.SimpleTokenModel
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UserDetails
import java.time.Duration
import java.util.*
import java.util.function.Supplier
import javax.annotation.Resource

/**
 * @author abstergo
 */
class SimpleUserAuthServiceImpl : IUserAuthService {
    @Resource
    var authProperty: WebAuthProperty? = null

    @Resource
    private val passwordEncrypt: IPasswordEncrypt? = null

    @Resource
    private val userInfoService: IUserInfoService? = null

    @Resource
    private val tokenManager: ITokenManager? = null

    override fun login(username: String, password: String?): ITokenModel {
        var password = password
        val details = userInfoService!!.loadUserInfoByUniqueKey(username)
        password = passwordEncrypt!!.encrypt(password)
        AssertUtil.isTrue(details.getPassword() != password, Supplier { SimpleAuthException("用户名或密码错误") })
        checkUserDetails(details)
        val exp = Date(
            System.currentTimeMillis() + Duration.ofSeconds(authProperty!!.getTokenExpireTime().toLong()).toMillis()
        )
        val model: ITokenModel = SimpleTokenModel.create(
            UuidUtil.getInstance().getUuidString(),
            authProperty!!.getTokenSource(), authProperty!!.getTokenFiledName(), details.getUsername(), exp
        )
        tokenManager!!.submitToken(model, details)
        return model
    }

    private fun checkUserDetails(details: UserDetails) {
        AssertUtil.notNull(details, Supplier { SimpleAuthException("未获得有效的用户数据") })
        AssertUtil.isTrue(!details.isEnabled(), Supplier { SimpleAuthException("用户被禁用") })
    }

    @Throws(AuthenticationException::class)
    override fun authToken(iTokenModel: ITokenModel) {
        AssertUtil.notNull(iTokenModel, Supplier { InvalidTokenException("未接收到有效Token") })
        AssertUtil.isTrue(StrUtil.isEmpty(iTokenModel.getToken()), Supplier { InvalidTokenException("Token值为空") })
        AssertUtil.isTrue(
            iTokenModel.getExpireTime() != null
                    && iTokenModel.getExpireTime().getTime() < System.currentTimeMillis(),
            Supplier { InvalidTokenException("Token已过期") })
        val details = tokenManager!!.loadUserInfoByToken(iTokenModel)
        AssertUtil.isTrue(details == null, Supplier { InvalidTokenException("Token已失效") })
        checkUserDetails(details)
    }

    override fun logout(iTokenModel: ITokenModel): Boolean {
        AssertUtil.notNull(iTokenModel, Supplier { InvalidTokenException("未接收到有效Token") })
        tokenManager!!.removeToken(iTokenModel)
        return true
    }
}

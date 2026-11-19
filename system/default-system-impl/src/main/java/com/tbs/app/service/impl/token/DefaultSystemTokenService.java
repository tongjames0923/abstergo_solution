package com.tbs.app.service.impl.token;

import com.tbs.app.service.DefaultAuditService;
import com.tbs.app.service.token.ISystemTokenService;
import com.tbs.async.delegate.AsyncDelegateInvoker;
import com.tbs.cache.expire.annotation.ExpireOn;
import com.tbs.common.delegate.IDelegateInvoker;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.StrUtil;
import com.tbs.common.util.function.EnumUtil;
import com.tbs.shared.interfaces.service.AbstractLogicDeleteFullFeatureServiceTemplate;
import com.tbs.shared.interfaces.service.IAuditService;
import com.tbs.shared.mapper.SystemTokenMapper;
import com.tbs.shared.pojo.criteria.CommonTokenQueryCriteria;
import com.tbs.shared.pojo.entity.SystemTokenEntity;
import com.tbs.web.enums.TokenStatusEnum;
import jakarta.annotation.Resource;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.tbs.app.service.impl.CacheNamesConstants.TOKEN_ENTITY;

public class DefaultSystemTokenService
    extends AbstractLogicDeleteFullFeatureServiceTemplate<SystemTokenEntity, Long, SystemTokenMapper>
    implements ISystemTokenService {

    private static final DefaultAuditService<SystemTokenEntity, Long> auditService = new DefaultAuditService<>();
    @Resource
    private SystemTokenMapper systemTokenMapper;

    @Override
    protected Integer getFlagDeleted() {
        return 0;
    }

    @Override
    protected Class<? extends IDelegateInvoker> pageVoBuildType() {
        return AsyncDelegateInvoker.class;
    }

    @Override
    protected SystemTokenEntity insertNew(SystemTokenEntity entity) {
        AssertUtil.isTrue(StrUtil.isEmpty(entity.getUserName())).message("用户名不能为空")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(StrUtil.isEmpty(entity.getToken())).message("token不能为空")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(entity.getTokenType() == null).message("令牌类型不能为空")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        entity.setFlag(TokenStatusEnum.ACTIVE.getCode());
        entity.setCreateTime(new Date());
        getMapper().insert(entity);
        return entity;
    }

    @CacheEvict(cacheNames = TOKEN_ENTITY, key = "#entity.token")
    @Override
    public List<SystemTokenEntity> save(SystemTokenEntity entity) {
        return super.save(entity);
    }

    @Override
    protected SystemTokenEntity compareAndUpdateValue(SystemTokenEntity newOne, SystemTokenEntity oldOne) {
        if (newOne.getFlag() != null) {
            AssertUtil.isTrue(!EnumUtil.isValidCode(TokenStatusEnum.class, newOne.getFlag()))
                .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).message("令牌状态无效").throwIt();
            oldOne.setFlag(newOne.getFlag());
        }
        if (newOne.getExpireTime() != null) {
            oldOne.setExpireTime(newOne.getExpireTime());
        }
        if (StrUtil.isNotEmpty(newOne.getIpAddress())) {
            oldOne.setIpAddress(newOne.getIpAddress());
        }
        if (StrUtil.isNotEmpty(newOne.getMacAddress())) {
            oldOne.setMacAddress(newOne.getMacAddress());
        }
        return oldOne;
    }

    @Override
    protected SystemTokenMapper getMapper() {
        return systemTokenMapper;
    }

    @Override
    protected SystemTokenEntity newEntity() {
        return new SystemTokenEntity();
    }

    @Cacheable(cacheNames = TOKEN_ENTITY, key = "#token")
    @ExpireOn(value = 10, timeUnit = TimeUnit.MINUTES)
    @Override
    public List<SystemTokenEntity> queryByToken(String token) {
        return getInstance().query(new CommonTokenQueryCriteria().setTokenEquals(token), null);
    }

    @Override
    protected Optional<IAuditService<SystemTokenEntity, Long, ?>> getAuditServiceSupport() {
        return Optional.of(auditService);
    }
}

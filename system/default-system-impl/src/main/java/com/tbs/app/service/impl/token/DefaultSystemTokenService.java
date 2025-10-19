package com.tbs.app.service.impl.token;

import com.tbs.app.service.token.ISystemTokenService;
import com.tbs.async.delegate.AsyncDelegateInvoker;
import com.tbs.cache.expire.annotation.ExpireOn;
import com.tbs.common.delegate.IDelegateInvoker;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.function.EnumUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.shared.interfaces.service.AbstractLogicDeleteFullFeatureServiceTemplate;
import com.tbs.shared.mapper.SystemTokenMapper;
import com.tbs.shared.pojo.criteria.CommonTokenQueryCriteria;
import com.tbs.shared.pojo.entity.SystemTokenEntity;
import com.tbs.web.enums.TokenStatusEnum;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class DefaultSystemTokenService
    extends AbstractLogicDeleteFullFeatureServiceTemplate<SystemTokenEntity, Long, SystemTokenMapper>
    implements ISystemTokenService {

    @Resource
    private SystemTokenMapper systemTokenMapper;

    @Override
    protected Integer getFlagDisable() {
        return 0;
    }

    @Override
    protected Class<? extends IDelegateInvoker> pageVoBuildType() {
        return AsyncDelegateInvoker.class;
    }

    @Override
    protected SystemTokenEntity insertNew(SystemTokenEntity entity) {
        AssertUtil.hasText(entity.getUserName(), "用户名不能为空");
        AssertUtil.hasText(entity.getToken(), "token不能为空");
        AssertUtil.isTrue(entity.getTokenType() == null, "令牌类型不能为空");
        entity.setFlag(TokenStatusEnum.ACTIVE.getCode());
        entity.setCreateTime(new Date());
        getMapper().insert(entity);
        return entity;
    }

    @CacheEvict(cacheNames = "token_entity", key = "#entity.token")
    @Override
    public List<SystemTokenEntity> save(SystemTokenEntity entity) {
        return super.save(entity);
    }

    @Override
    protected List<SystemTokenEntity> updateBy(SystemTokenEntity entity) {
        entity.setUpdateTime(new Date());
        getMapper().updateByPrimaryKey(entity);
        return List.of(entity);
    }

    @Override
    protected SystemTokenEntity compareAndUpdateValue(SystemTokenEntity newOne, SystemTokenEntity oldOne) {
        if (newOne.getFlag() != null) {
            if (EnumUtil.isValidCode(TokenStatusEnum.class, newOne.getFlag())) {
                oldOne.setFlag(newOne.getFlag());
            } else {
                AssertUtil.isTrue(false, "无效的令牌状态");
            }
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

    @Cacheable(cacheNames = "token_entity", key = "#token")
    @ExpireOn(value = 10, timeUnit = TimeUnit.MINUTES)
    @Override
    public List<SystemTokenEntity> queryByToken(String token) {
        return getInstance().query(new CommonTokenQueryCriteria().setTokenEquals(token), null);
    }
}

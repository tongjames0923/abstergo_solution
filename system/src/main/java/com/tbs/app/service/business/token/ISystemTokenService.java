package com.tbs.app.service.business.token;

import com.tbs.shared.interfaces.service.IServiceTemplate;
import com.tbs.shared.pojo.entity.SystemTokenEntity;

import java.util.List;

public interface ISystemTokenService extends IServiceTemplate<SystemTokenEntity, Long> {

    /**
     * 根据token查询
     *
     * @param token
     * @return
     */
    public List<SystemTokenEntity> queryByToken(String token);
}

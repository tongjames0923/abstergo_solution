package com.tbs.shared.mapper;

import org.springframework.stereotype.Repository;

import com.tbs.shared.pojo.entity.SystemAccessTokenEntity;
import com.tbs.sql.mapper.IFrameworkMapper;

@Repository
public interface AccessTokenMapper extends IFrameworkMapper<SystemAccessTokenEntity, Long> {
}

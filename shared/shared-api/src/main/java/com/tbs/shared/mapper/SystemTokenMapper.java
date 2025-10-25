package com.tbs.shared.mapper;

import org.springframework.stereotype.Repository;

import com.tbs.shared.pojo.entity.SystemTokenEntity;
import com.tbs.sql.mapper.IFrameworkMapper;

@Repository
public interface SystemTokenMapper extends IFrameworkMapper<SystemTokenEntity, Long> {
}

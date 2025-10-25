package com.tbs.shared.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.tbs.shared.pojo.entity.SystemUserEntity;
import com.tbs.sql.mapper.IFrameworkMapper;
import org.springframework.stereotype.Repository;

/**
 * @author abstergo
 */
@Repository
public interface SystemUserMapper extends IFrameworkMapper<SystemUserEntity, Long> {
}

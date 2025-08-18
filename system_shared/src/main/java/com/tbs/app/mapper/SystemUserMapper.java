package com.tbs.app.mapper;

import com.tbs.app.pojo.entity.SystemUserEntity;
import com.tbs.sql.mapper.IFrameworkMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author abstergo
 */
@Mapper
public interface SystemUserMapper extends IFrameworkMapper<SystemUserEntity, Long> {
}

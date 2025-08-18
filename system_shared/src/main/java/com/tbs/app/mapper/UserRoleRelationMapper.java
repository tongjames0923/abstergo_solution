package com.tbs.app.mapper;

import com.tbs.app.pojo.entity.UserRoleRelationEntity;
import com.tbs.sql.mapper.IFrameworkMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserRoleRelationMapper extends IFrameworkMapper<UserRoleRelationEntity, Long> {
}

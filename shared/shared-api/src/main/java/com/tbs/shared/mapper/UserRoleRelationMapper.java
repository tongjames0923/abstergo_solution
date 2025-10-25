package com.tbs.shared.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.tbs.shared.pojo.entity.UserRoleRelationEntity;
import com.tbs.sql.mapper.IFrameworkMapper;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleRelationMapper extends IFrameworkMapper<UserRoleRelationEntity, Long> {
}

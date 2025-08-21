package com.tbs.shared.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.tbs.shared.pojo.entity.SystemRoleEntity;
import com.tbs.sql.mapper.IFrameworkMapper;
import org.springframework.stereotype.Repository;

/**
 * @author abstergo
 */
@Repository
public interface SystemRoleMapper extends IFrameworkMapper<SystemRoleEntity, Long> {
}

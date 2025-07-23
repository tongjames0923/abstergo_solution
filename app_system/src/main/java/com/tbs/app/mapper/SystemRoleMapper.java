package com.tbs.app.mapper;

import com.tbs.app.pojo.entity.SystemRole;
import com.tbs.sql.mapper.IFrameworkMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author abstergo
 */
@Mapper
public interface SystemRoleMapper extends IFrameworkMapper<SystemRole, Long> {
}

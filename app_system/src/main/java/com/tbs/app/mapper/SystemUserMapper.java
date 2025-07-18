package com.tbs.app.mapper;

import com.tbs.app.pojo.entity.SystemUser;
import com.tbs.sql.mapper.IFrameworkMapper;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

/**
 * @author abstergo
 */
@Mapper
public interface SystemUserMapper extends IFrameworkMapper<SystemUser, Long> {
}

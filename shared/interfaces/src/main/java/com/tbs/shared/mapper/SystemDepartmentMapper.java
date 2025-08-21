package com.tbs.shared.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.tbs.shared.pojo.entity.SystemDepartmentEntity;
import com.tbs.sql.mapper.IFrameworkMapper;

@Repository
public interface SystemDepartmentMapper extends IFrameworkMapper<SystemDepartmentEntity, Long> {

}

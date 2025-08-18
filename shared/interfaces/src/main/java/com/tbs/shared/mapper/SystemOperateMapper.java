package com.tbs.shared.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.tbs.shared.pojo.entity.SystemOperateRecordEntity;
import com.tbs.sql.mapper.IFrameworkMapper;

@Mapper
public interface SystemOperateMapper extends IFrameworkMapper<SystemOperateRecordEntity, Long> {
}

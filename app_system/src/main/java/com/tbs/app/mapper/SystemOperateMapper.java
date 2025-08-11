package com.tbs.app.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.tbs.app.pojo.entity.SystemOperateRecordEntity;
import com.tbs.sql.mapper.IFrameworkMapper;

@Mapper
public interface SystemOperateMapper extends IFrameworkMapper<SystemOperateRecordEntity, Long> {
}

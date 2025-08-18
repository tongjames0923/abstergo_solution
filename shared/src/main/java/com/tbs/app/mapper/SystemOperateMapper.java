package com.tbs.app.mapper;

import com.tbs.app.pojo.entity.SystemOperateRecordEntity;
import com.tbs.sql.mapper.IFrameworkMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SystemOperateMapper extends IFrameworkMapper<SystemOperateRecordEntity, Long> {
}

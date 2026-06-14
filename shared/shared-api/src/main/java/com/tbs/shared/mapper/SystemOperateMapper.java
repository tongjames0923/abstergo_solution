package com.tbs.shared.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.tbs.shared.pojo.entity.SystemOperateRecordEntity;
import com.tbs.sql.mapper.IFrameworkMapper;
import org.springframework.stereotype.Repository;

/**
 * 系统操作日志
 * @author abstergo
 */
@Repository
public interface SystemOperateMapper extends IFrameworkMapper<SystemOperateRecordEntity, Long> {
}

package com.tbs.app.mapper.push;

import com.tbs.app.entity.push.PushMessageOperationLogEntity;
import com.tbs.sql.mapper.IFrameworkMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 消息全链路操作日志表 Mapper
 *
 * @author abstergo
 * @since 1.0.0
 */
@Mapper
public interface PushMessageOperationLogMapper extends IFrameworkMapper<PushMessageOperationLogEntity, Long> {

}

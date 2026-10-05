package com.tbs.shared.mapper.push;

import com.tbs.shared.pojo.entity.push.PushMessageEntity;
import com.tbs.sql.mapper.IFrameworkMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 推送业务消息主表 Mapper
 *
 * @author abstergo
 * @since 1.0.0
 */
@Mapper
public interface PushMessageMapper extends IFrameworkMapper<PushMessageEntity, Long> {

}

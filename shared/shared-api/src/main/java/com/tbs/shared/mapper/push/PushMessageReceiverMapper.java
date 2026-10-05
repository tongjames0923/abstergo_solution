package com.tbs.shared.mapper.push;

import com.tbs.shared.pojo.entity.push.PushMessageReceiverEntity;
import com.tbs.sql.mapper.IFrameworkMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 推送消息接收人表 Mapper
 *
 * @author abstergo
 * @since 1.0.0
 */
@Mapper
public interface PushMessageReceiverMapper extends IFrameworkMapper<PushMessageReceiverEntity, Long> {

}

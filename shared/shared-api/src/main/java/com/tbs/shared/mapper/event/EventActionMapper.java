package com.tbs.shared.mapper.event;

import com.tbs.shared.pojo.entity.event.SystemEventActionEntity;
import com.tbs.sql.mapper.IFrameworkMapper;
import org.springframework.stereotype.Repository;

/**
 * 事件动作绑定 Mapper
 *
 * @author abstergo
 */
@Repository
public interface EventActionMapper extends IFrameworkMapper<SystemEventActionEntity, Long> {
}

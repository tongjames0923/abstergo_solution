package com.tbs.shared.mapper.event;

import com.tbs.shared.pojo.entity.event.SystemEventSubscriptionEntity;
import com.tbs.sql.mapper.IFrameworkMapper;
import org.springframework.stereotype.Repository;

/**
 * 事件订阅 Mapper
 *
 * @author abstergo
 */
@Repository
public interface EventSubscriptionMapper extends IFrameworkMapper<SystemEventSubscriptionEntity, Long> {
}

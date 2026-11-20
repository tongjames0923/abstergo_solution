package com.tbs.shared.mapper.condition;

import com.tbs.shared.pojo.entity.condition.SystemConditionRuleEntity;
import com.tbs.sql.mapper.IFrameworkMapper;
import org.springframework.stereotype.Repository;

/**
 * 条件规则 Mapper
 *
 * @author abstergo
 */
@Repository
public interface ConditionRuleMapper extends IFrameworkMapper<SystemConditionRuleEntity, Long> {
}

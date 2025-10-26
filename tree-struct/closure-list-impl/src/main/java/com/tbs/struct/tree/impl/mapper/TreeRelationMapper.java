package com.tbs.struct.tree.impl.mapper;

import com.tbs.sql.mapper.IFrameworkMapper;
import com.tbs.struct.tree.impl.entity.TreeRelationEntity;
import org.springframework.stereotype.Repository;

/**
 * 树关系映射接口
 *
 * @author abstergo
 */
@Repository
public interface TreeRelationMapper extends IFrameworkMapper<TreeRelationEntity, Long> {
}

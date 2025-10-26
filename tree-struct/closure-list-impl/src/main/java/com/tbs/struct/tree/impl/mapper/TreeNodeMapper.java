package com.tbs.struct.tree.impl.mapper;

import com.tbs.sql.mapper.IFrameworkMapper;
import com.tbs.struct.tree.impl.entity.TreeNodeEntity;
import org.springframework.stereotype.Repository;

@Repository
public interface TreeNodeMapper extends IFrameworkMapper<TreeNodeEntity, String> {
}

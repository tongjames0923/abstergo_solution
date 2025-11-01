package com.tbs.struct.tree.impl.mapper;

import com.tbs.cache.expire.annotation.ExpireOn;
import com.tbs.sql.mapper.IFrameworkMapper;
import com.tbs.struct.tree.impl.entity.TreeNodeEntity;
import org.apache.ibatis.annotations.Select;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Repository
public interface TreeNodeMapper extends IFrameworkMapper<TreeNodeEntity, String> {

    @Cacheable(cacheNames = "tree_node_cache", key = "#nodeId")
    @ExpireOn(value = 2, timeUnit = TimeUnit.MINUTES)
    @Select("select * from tree_node where id = #{nodeId}")
    List<TreeNodeEntity> getNodeById(String nodeId);

    @Select("select group_name from tree_node where parent_id is null group by group_name")
    List<String> listAllGroup();

    @Select("select * from tree_node where parent_id is null and group_name = #{groupName}")
    List<TreeNodeEntity> getRootNodeByGroupName(String groupName);

    @Select("select * from tree_node where parent_id = #{nodeId}")
    List<TreeNodeEntity> getChildren(String nodeId);
}

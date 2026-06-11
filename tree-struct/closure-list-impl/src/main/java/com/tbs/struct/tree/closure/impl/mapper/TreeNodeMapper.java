package com.tbs.struct.tree.closure.impl.mapper;

import com.tbs.cache.expire.annotation.ExpireOn;
import com.tbs.sql.mapper.IFrameworkMapper;
import com.tbs.struct.tree.closure.impl.entity.TreeNodeEntity;
import org.apache.ibatis.annotations.Select;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;
import java.util.concurrent.TimeUnit;

public interface TreeNodeMapper extends IFrameworkMapper<TreeNodeEntity, Long> {

    @Select("select * from tree_node where id = #{id}")
    TreeNodeEntity getNodeById(Long id);

    @Select("select * from tree_node where node_id = #{nodeId} and group_name=#{treeGroup}")
    List<TreeNodeEntity> getNodeByNodeIdAndTreeGroup(String nodeId, String treeGroup);

    @Select("select group_name from tree_node where parent_id is null group by group_name")
    List<String> listAllGroup();

    @Select("select * from tree_node where parent_id is null and group_name = #{groupName}")
    List<TreeNodeEntity> getRootNodeByGroupName(String groupName);

    @Select("select * from tree_node where entity_id = #{entityId}")
    List<TreeNodeEntity> getNodesByEntityId(Long entityId);
}

package com.tbs.struct.tree.closure.impl.mapper;

import com.tbs.sql.mapper.IFrameworkMapper;
import com.tbs.struct.tree.closure.impl.entity.TreeRelationEntity;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 树关系映射接口
 *
 * @author abstergo
 */
@Repository
public interface TreeRelationMapper extends IFrameworkMapper<TreeRelationEntity, Long> {

    /**
     * 根据后代节点ID查询所有祖先节点关系
     *
     * @param descendantId 后代节点ID
     * @return 祖先节点关系列表
     */
    @Select("SELECT * FROM tree_closure WHERE descendant_id = #{descendantId} and group_name=#{groupName}")
    List<TreeRelationEntity> selectAncestorsByDescendantId(String descendantId, String groupName);

    /**
     * 根据祖先节点ID查询所有后代节点关系
     *
     * @param ancestorId 祖先节点ID
     * @return 后代节点关系列表
     */
    @Select("SELECT * FROM tree_closure WHERE ancestor_id = #{ancestorId} and group_name=#{groupName}")
    List<TreeRelationEntity> selectDescendantsByAncestorId(String ancestorId, String groupName);

    /**
     * 根据祖先节点和后代节点更新关系
     *
     * @param ancestorId   祖先节点ID
     * @param descendantId 后代节点ID
     * @param depth        深度
     * @return 影响行数
     */
    @Update(
        "UPDATE tree_closure SET depth = #{depth} WHERE ancestor_id = #{ancestorId} AND descendant_id = #{descendantId} and group_name=#{groupName}")
    int updateByAncestorIdAndDescendantId(String ancestorId, String descendantId, int depth, String groupName);

    /**
     * 根据祖先节点和后代节点查询关系
     *
     * @param ancestorId   祖先节点ID
     * @param descendantId 后代节点ID
     * @return 祖先节点关系列表
     */
    @Select(
        "SELECT * FROM tree_closure WHERE ancestor_id = #{ancestorId} AND descendant_id = #{descendantId} and group_name=#{groupName}")
    List<TreeRelationEntity> selectByAncestorIdAndDescendantId(String ancestorId, String descendantId,
        String groupName);

    /**
     * 根据祖先节点和后代节点删除关系
     *
     * @param ancestorId   祖先节点ID
     * @param descendantId 后代节点ID
     */
    @Delete(
        "DELETE FROM tree_closure WHERE ancestor_id = #{ancestorId} AND descendant_id = #{descendantId} and group_name=#{groupName}")
    void deleteByAncestorIdAndDescendantId(String ancestorId, String descendantId, String groupName);

    /**
     * 根据节点ID删除所有相关的祖先关系
     *
     * @param descendantId 后代节点ID
     */
    @Delete("DELETE FROM tree_closure WHERE descendant_id = #{descendantId} and group_name=#{groupName}")
    void deleteByDescendantId(String descendantId, String groupName);

    /**
     * 根据节点ID删除所有相关的后代关系
     *
     * @param ancestorId 祖先节点ID
     */
    @Delete("DELETE FROM tree_closure WHERE ancestor_id = #{ancestorId} and group_name=#{groupName}")
    void deleteByAncestorId(String ancestorId, String groupName);

    /**
     * 删除所有关系
     */
    @Delete("DELETE FROM tree_closure")
    void deleteAllRelations();

    /**
     * 删除指定树组的所有关系
     *
     * @param groupName 树组名
     */
    @Delete("DELETE FROM tree_closure WHERE group_name = #{groupName}")
    void deleteRelationsByGroup(String groupName);
}

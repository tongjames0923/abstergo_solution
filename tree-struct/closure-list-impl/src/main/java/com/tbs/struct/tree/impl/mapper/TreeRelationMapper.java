package com.tbs.struct.tree.impl.mapper;

import com.tbs.sql.mapper.IFrameworkMapper;
import com.tbs.struct.tree.impl.entity.TreeRelationEntity;
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
    @Select("SELECT * FROM tree_closure WHERE descendant_id = #{descendantId}")
    List<TreeRelationEntity> selectAncestorsByDescendantId(String descendantId);

    /**
     * 根据祖先节点ID查询所有后代节点关系
     *
     * @param ancestorId 祖先节点ID
     * @return 后代节点关系列表
     */
    @Select("SELECT * FROM tree_closure WHERE ancestor_id = #{ancestorId}")
    List<TreeRelationEntity> selectDescendantsByAncestorId(String ancestorId);

    /**
     * 根据祖先节点和后代节点更新关系
     *
     * @param ancestorId   祖先节点ID
     * @param descendantId 后代节点ID
     * @param depth        深度
     * @return 影响行数
     */
    @Update(
        "UPDATE tree_closure SET depth = #{depth} WHERE ancestor_id = #{ancestorId} AND descendant_id = #{descendantId}")
    int updateByAncestorIdAndDescendantId(String ancestorId, String descendantId, int depth);

    /**
     * 根据祖先节点和后代节点查询关系
     *
     * @param ancestorId   祖先节点ID
     * @param descendantId 后代节点ID
     * @return 祖先节点关系列表
     */
    @Select("SELECT * FROM tree_closure WHERE ancestor_id = #{ancestorId} AND descendant_id = #{descendantId}")
    List<TreeRelationEntity> selectByAncestorIdAndDescendantId(String ancestorId, String descendantId);

    /**
     * 根据祖先节点和后代节点删除关系
     *
     * @param ancestorId   祖先节点ID
     * @param descendantId 后代节点ID
     */
    @Delete("DELETE FROM tree_closure WHERE ancestor_id = #{ancestorId} AND descendant_id = #{descendantId}")
    void deleteByAncestorIdAndDescendantId(String ancestorId, String descendantId);

    /**
     * 根据节点ID删除所有相关的祖先关系
     *
     * @param descendantId 后代节点ID
     */
    @Delete("DELETE FROM tree_closure WHERE descendant_id = #{descendantId}")
    void deleteByDescendantId(String descendantId);

    /**
     * 根据节点ID删除所有相关的后代关系
     *
     * @param ancestorId 祖先节点ID
     */
    @Delete("DELETE FROM tree_closure WHERE ancestor_id = #{ancestorId}")
    void deleteByAncestorId(String ancestorId);
}

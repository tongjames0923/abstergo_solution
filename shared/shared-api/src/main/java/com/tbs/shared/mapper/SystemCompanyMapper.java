package com.tbs.shared.mapper;

import com.tbs.shared.pojo.entity.SystemCompanyEntity;
import com.tbs.sql.mapper.IFrameworkMapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemCompanyMapper extends IFrameworkMapper<SystemCompanyEntity, Long> {

    /**
     * 根据树节点ID获取ID
     *
     * @param treeNodeId
     * @param group
     * @return
     */
    @Select("select * from sys_company where tree_node_group=#{group} and tree_node_id=#{treeNodeId}")
    SystemCompanyEntity getByTree(String treeNodeId, String group);
}

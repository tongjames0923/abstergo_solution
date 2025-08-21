package com.tbs.shared.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Select;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

import com.tbs.shared.pojo.entity.SystemDepartmentTreePathEntity;
import com.tbs.sql.mapper.IFrameworkMapper;

@Repository
public interface DepartmentTreePathMapper extends IFrameworkMapper<SystemDepartmentTreePathEntity, Long> {

	@Select("select * from department_tree_path where parent_id=#{parentId} ")
	public List<SystemDepartmentTreePathEntity> queryByParentId(@NotNull Long parentId);

	@Delete("delete from department_tree_path where parent_id=#{parentId}")
	void deleteByParentId(Long parentId);

}

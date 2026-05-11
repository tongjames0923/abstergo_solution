package com.tbs.app.service.user.model;

import com.tbs.app.vo.DepartmentInfoVO;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

public interface ISystemUserDetails extends UserDetails {

    /**
     * 获取当前用户所属部门列表
     *
     * @return
     */
    List<DepartmentInfoVO> getDepartments();

    /**
     * 获取当前用户所属部门
     *
     * @return
     */
    DepartmentInfoVO getCurrentDepartment();

    /**
     * 设置当前用户所属部门
     *
     * @param department
     */
    void setCurrentDepartment(long department);
}

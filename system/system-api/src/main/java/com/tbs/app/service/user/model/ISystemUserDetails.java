package com.tbs.app.service.user.model;

import com.tbs.app.vo.DepartmentInfoVO;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;
import java.util.List;
import java.util.Set;

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

    /**
     * 获取 API 权限编码集合
     * <p>
     * 权限编码为扁平字符串，鉴权时直接做等值匹配，不需要再追溯角色组与权限项。
     * 默认返回空集合，便于未装配权限编码的实现按“无 API 权限”降级。
     * </p>
     *
     * @return API 权限编码集合，无权限时返回空集合
     */
    default Set<String> getApiPermissions() {
        return Collections.emptySet();
    }

    /**
     * 获取菜单权限编码集合
     * <p>
     * 用于前端路由与菜单的可见性过滤，语义与 {@link #getApiPermissions()} 一致。
     * </p>
     *
     * @return 菜单权限编码集合，无权限时返回空集合
     */
    default Set<String> getMenuPermissions() {
        return Collections.emptySet();
    }
}

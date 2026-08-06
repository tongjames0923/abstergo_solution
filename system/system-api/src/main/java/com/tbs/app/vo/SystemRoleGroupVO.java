package com.tbs.app.vo;

import com.tbs.shared.pojo.entity.SystemRoleEntity;
import com.tbs.shared.pojo.entity.SystemRoleGroupEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SystemRoleGroupVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 角色组信息
     */
    private SystemRoleGroupEntity group;

    /**
     * 角色组下的权限列表
     */
    private List<SystemRoleEntity> permissions;

}

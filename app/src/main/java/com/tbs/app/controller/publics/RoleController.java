package com.tbs.app.controller.publics;

import com.tbs.app.AbstractCrudController;
import com.tbs.app.pojo.criteria.CommonSystemRoleQueryCriteria;
import com.tbs.app.pojo.entity.SystemRole;
import com.tbs.app.service.IServiceTemplate;
import com.tbs.app.service.business.ISystemRoleService;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.result.annotation.NoWrapped;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author tongj
 */
@RestController
@RequestMapping("public/role")
public class RoleController extends AbstractCrudController<SystemRole, Long, CommonSystemRoleQueryCriteria> {

    @Resource
    private ISystemRoleService roleService;

    @Override
    public IServiceTemplate<SystemRole, Long> getService() {
        return roleService;
    }


    @Override
    public AjaxResult query(@RequestBody CommonSystemRoleQueryCriteria criteria, long pageNum, long pageSize) {
        return super.query(criteria, pageNum, pageSize);
    }
}

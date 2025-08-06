package com.tbs.app.controller.privates;

import com.tbs.app.AbstractCrudController;
import com.tbs.app.constant.CommonConstant;
import com.tbs.app.pojo.criteria.CommonSystemRoleQueryCriteria;
import com.tbs.app.pojo.entity.SystemRole;
import com.tbs.app.service.IServiceTemplate;
import com.tbs.app.service.business.ISystemRoleService;
import com.tbs.web.result.AjaxResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author tongj
 */
@RestController
@RequestMapping("private/admin/role")
@PreAuthorize("hasAuthority('" + CommonConstant.SUPER_ADMIN_ROLE_NAME + "')")
public class RoleController extends AbstractCrudController<SystemRole, Long, CommonSystemRoleQueryCriteria> {

    @Resource
    private ISystemRoleService roleService;

    @Override
    public IServiceTemplate<SystemRole, Long> getService() {
        return roleService;
    }

    @Override
    @GetMapping("get/{id}")
    @ResponseBody
    public AjaxResult get(@PathVariable Long id) {
        return super.get(id);
    }

    @Override
    @PostMapping("save")
    @ResponseBody
    public AjaxResult save(@RequestBody SystemRole entity) {
        return super.save(entity);
    }

    @Override
    @PostMapping("delete/{id}")
    @ResponseBody
    public AjaxResult delete(@PathVariable Long id) {
        return super.delete(id);
    }

    @Override
    @PostMapping("query")
    @ResponseBody
    public AjaxResult query(@RequestBody CommonSystemRoleQueryCriteria criteria, long pageNum, long pageSize) {
        return super.query(criteria, pageNum, pageSize);
    }

}

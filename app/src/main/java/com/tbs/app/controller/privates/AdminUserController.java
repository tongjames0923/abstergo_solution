package com.tbs.app.controller.privates;

import com.tbs.app.AbstractCrudController;
import com.tbs.app.constant.CommonConstant;
import com.tbs.app.pojo.criteria.CommonSystemUserQueryCriteria;
import com.tbs.app.pojo.entity.SystemUserEntity;
import com.tbs.app.service.IServiceTemplate;
import com.tbs.app.service.business.ISystemUserService;
import com.tbs.web.result.AjaxResult;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 管理员用户管理
 *
 * @author abstergo
 */
@RestController
@RequestMapping("/private/admin/user")
@PreAuthorize("hasAuthority('" + CommonConstant.SUPER_ADMIN_ROLE_NAME + "')")
public class AdminUserController extends AbstractCrudController<SystemUserEntity, Long, CommonSystemUserQueryCriteria> {
    @Resource
    private ISystemUserService service;

    @Override
    public IServiceTemplate<SystemUserEntity, Long> getService() {
        return service;
    }

    @Override
    @GetMapping("get/{id}")
    public AjaxResult get(@PathVariable Long id) {
        return super.get(id);
    }

    @Override
    @PostMapping("save")
    public AjaxResult save(@RequestBody SystemUserEntity entity) {
        return super.save(entity);
    }

    @Override
    @PostMapping("delete/{id}")
    public AjaxResult delete(@PathVariable Long id) {
        return super.delete(id);
    }

    @Override
    @PostMapping("query")
    public AjaxResult query(@RequestBody CommonSystemUserQueryCriteria criteria, Long pageNum, Long pageSize) {
        return super.query(criteria, pageNum, pageSize);
    }

    @Override
    public String getBusinessName() {
        return "管理员用户管理";
    }
}

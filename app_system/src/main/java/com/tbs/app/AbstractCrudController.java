package com.tbs.app;

import com.tbs.app.service.IServiceTemplate;
import com.tbs.sql.model.QueryPageModel;
import com.tbs.sql.query.IQueryCriteria;
import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.uitl.AjaxUtil;
import org.springframework.web.bind.annotation.*;

/**
 * 抽象的CRUD控制器
 *
 * @author tongj
 */
public abstract class AbstractCrudController<T, K, Q extends IQueryCriteria> extends BaseController {

    /**
     * 获取服务
     *
     * @return
     */
    public abstract IServiceTemplate<T, K> getService();

    @GetMapping("get/{id}")
    @ResponseBody
    public AjaxResult get(@PathVariable K id) {
        T entity = getService().queryById(id);
        return AjaxUtil.getInstance().success(entity);
    }

    @PostMapping("save")
    @ResponseBody
    public AjaxResult save(@RequestBody T entity) {
        return AjaxUtil.getInstance().success(getService().save(entity));
    }

    @PostMapping("delete/{id}")
    @ResponseBody
    public AjaxResult delete(@PathVariable K id) {
        return AjaxUtil.getInstance().success(getService().deleteById(id));
    }

    @PostMapping("query")
    @ResponseBody
    public AjaxResult query(@RequestBody Q criteria, long pageNum, long pageSize) {
        return AjaxUtil.getInstance().success(getService().queryBy(criteria, QueryPageModel.of(pageNum, pageSize)));
    }

}

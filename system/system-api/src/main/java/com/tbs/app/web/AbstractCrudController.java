package com.tbs.app.web;

import com.tbs.shared.interfaces.service.IServiceTemplate;
import com.tbs.sql.criteria.IQueryCriteria;
import com.tbs.sql.model.query.QueryPageModel;
import com.tbs.web.BaseController;
import com.tbs.web.enums.WebMessageTypeEnum;
import com.tbs.web.result.AjaxResult;
import com.tbs.web.uitl.AjaxUtil;

/**
 * 抽象的CRUD控制器 提供基本的增删改查操作的RESTful接口实现
 *
 * @param <T> 实体类型
 * @param <K> 主键类型
 * @param <Q> 查询条件类型，必须实现IQueryCriteria接口
 * @author tongj
 */
public abstract class AbstractCrudController<T, K, Q extends IQueryCriteria> extends BaseController {

    /**
     * 获取服务实例 子类必须实现此方法以提供具体的服务实例
     *
     * @return 服务实例，用于执行具体的业务逻辑
     */
    public abstract IServiceTemplate<T, K> getService();

    /**
     * 根据主键获取实体对象
     *
     * @param id 实体主键
     * @return 包含实体对象的AjaxResult
     */

    public AjaxResult get(K id) {
        T entity = getService().queryById(id);
        return AjaxUtil.getInstance().success(entity);
    }

    /**
     * 保存实体对象
     *
     * @param entity 实体对象
     * @return 包含保存结果的AjaxResult
     */
    public AjaxResult save(T entity) {
        return AjaxUtil.builder().success(getService().save(entity)).type(WebMessageTypeEnum.SUCCESS)
            .message("保存成功").build();
    }

    /**
     * 根据主键删除实体对象
     *
     * @param id 实体主键
     * @return 包含删除结果的AjaxResult
     */
    public AjaxResult delete(K id) {
        return AjaxUtil.builder().success(getService().deleteById(id)).type(WebMessageTypeEnum.SUCCESS)
            .message("删除成功").build();
    }

    /**
     * 根据查询条件分页查询实体对象
     *
     * @param criteria 查询条件
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 包含分页查询结果的AjaxResult
     */
    public AjaxResult query(Q criteria, Long pageNum, Long pageSize) {
        return AjaxUtil.getInstance()
            .success(getService().queryAndPage(criteria, QueryPageModel.of(pageNum, pageSize)));
    }

}

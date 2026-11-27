package com.tbs.app.service.view;

import com.tbs.shared.pojo.model.view.CrudControllerOption;
import com.tbs.shared.pojo.model.view.FormConfig;

import java.util.List;

/**
 * 表单配置生成服务
 * <p>
 * 基于 {@code AbstractCrudController} 的泛型与字段元信息生成默认表单配置，
 * 生成结果作为草稿返回，由前端确认后继续微调与保存。
 * </p>
 *
 * @author abstergo
 */
public interface IFormConfigGenerateService {

    /**
     * 列出可生成默认表单配置的 CRUD 控制器
     *
     * @return 控制器选项列表
     */
    List<CrudControllerOption> listControllers();

    /**
     * 根据控制器生成默认表单配置
     *
     * @param controller 控制器类名或类级路径
     * @param scene      场景：search / save / edit
     * @return 默认表单配置草稿
     */
    FormConfig generate(String controller, String scene);
}

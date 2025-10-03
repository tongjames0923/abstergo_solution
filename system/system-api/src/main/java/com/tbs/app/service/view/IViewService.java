package com.tbs.app.service.view;

import com.tbs.shared.pojo.entity.view.DynamicFormConfigrationEntity;
import com.tbs.shared.pojo.model.view.FormConfig;
import org.jetbrains.annotations.NotNull;

public interface IViewService {
    /**
     * 获取表单配置
     *
     * @param id
     * @return
     */
    FormConfig getForm(Long id);

    /**
     * 保存表单配置
     *
     * @param formConfig
     * @return
     */
    DynamicFormConfigrationEntity saveForm(@NotNull FormConfig formConfig);
}

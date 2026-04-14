package com.tbs.app.service.view;

import com.tbs.shared.pojo.entity.view.DynamicFormConfigrationEntity;
import com.tbs.shared.pojo.model.view.FormConfig;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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


    /**
     * 更新表单配置
     *
     * @param formConfigId 表单配置ID
     * @param formConfig 表单配置
     * @return
     */
    DynamicFormConfigrationEntity updateForm(Long formConfigId, @NotNull FormConfig formConfig);

    /**
     * 删除表单配置
     *
     * @param formConfigId
     * @return
     */
    DynamicFormConfigrationEntity deleteForm(Long formConfigId);


    /**
     * 获取表单列表
     *
     * @param pageNum
     * @param pageSize
     * @return
     */
    List<FormConfig> listForms(int pageNum, int pageSize);
}

package com.tbs.shared.pojo.model.view;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 表单配置类 用于定义表单的整体配置信息
 */
@Setter
@Getter
public class FormConfig implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 是否可展开 -- GETTER -- 获取是否可展开
     *
     *
     * -- SETTER -- 设置是否可展开
     *
     * @return 是否可展开
     * @param expandable 是否可展开
     *
     */
    private Boolean expandable;
    /**
     * 是否已展开 -- GETTER -- 获取是否已展开
     *
     *
     * -- SETTER -- 设置是否已展开
     *
     * @return 是否已展开
     * @param expanded 是否已展开
     *
     */
    private Boolean expanded;
    /**
     * 表单字段间距 -- GETTER -- 获取表单字段间距
     *
     *
     * -- SETTER -- 设置表单字段间距
     *
     * @return 表单字段间距
     * @param gutter 表单字段间距
     *
     */
    private Integer gutter;
    /**
     * 标签宽度 -- GETTER -- 获取标签宽度
     *
     *
     * -- SETTER -- 设置标签宽度
     *
     * @return 标签宽度
     * @param labelWidth 标签宽度
     *
     */
    private String labelWidth;
    /**
     * 表单栅格占据的列数 -- GETTER -- 获取表单栅格占据的列数
     *
     *
     * -- SETTER -- 设置表单栅格占据的列数
     *
     * @return 表单栅格占据的列数
     * @param span 表单栅格占据的列数
     *
     */
    private Integer span;
    /**
     * 表单字段列表 -- GETTER -- 获取表单字段列表
     *
     *
     * -- SETTER -- 设置表单字段列表
     *
     * @return 表单字段列表
     * @param fields 表单字段列表
     *
     */
    private List<FormField> fields;
    /**
     * 表单属性映射 -- GETTER -- 获取表单属性映射
     *
     *
     * -- SETTER -- 设置表单属性映射
     *
     * @return 表单属性映射
     * @param props 表单属性映射
     *
     */
    private Map<String, Object> props;

}

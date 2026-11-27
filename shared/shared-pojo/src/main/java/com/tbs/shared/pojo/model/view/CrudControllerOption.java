package com.tbs.shared.pojo.model.view;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * CRUD 控制器选项，供前端选择生成默认表单配置的目标控制器
 */
@Setter
@Getter
public class CrudControllerOption implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 控制器类名
     */
    private String name;

    /**
     * 控制器类级请求路径
     */
    private String path;

    /**
     * 控制器业务名称
     */
    private String businessName;
}

package com.tbs.script.api.context;

import com.tbs.common.BaseEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 脚本作用域枚举
 *
 * @author tboss
 */
@AllArgsConstructor
@Getter
public enum ScriptScope implements BaseEnum<String> {

    /**
     * 全局
     */
    Global("global", 0),
    /**
     * 局部
     */
    Local("local", 1);

    private String value;
    private Integer code;

}

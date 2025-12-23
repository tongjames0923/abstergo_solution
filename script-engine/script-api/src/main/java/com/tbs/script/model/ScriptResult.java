package com.tbs.script.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 脚本执行结果
 *
 * @author abstergo
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScriptResult implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 脚本执行结果是否成功
     */
    private boolean success;

    /**
     * 脚本执行结果
     */
    private Object result;

    /**
     * 脚本执行结果类型
     */
    private Class<?> resultClass;

    /**
     * 错误信息
     */
    private String errorMsg;
}

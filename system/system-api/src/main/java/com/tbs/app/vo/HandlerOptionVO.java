package com.tbs.app.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 数据处理器选项视图对象
 * <p>
 * 用于前端下拉列表展示：{@code id} 为处理器唯一标识（可作为请求参数），
 * {@code name} 为处理器业务名称（用于界面展示）。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class HandlerOptionVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 处理器ID
     */
    private String id;

    /**
     * 处理器名称
     */
    private String name;
}

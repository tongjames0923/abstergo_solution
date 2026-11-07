package com.tbs.app.form;

import com.tbs.shared.pojo.entity.SystemGenericApiEntity;
import com.tbs.shared.pojo.entity.SystemGenericApiParamEntity;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 泛化API整组保存表单
 * <p>
 * 配置面的一个弹窗同时提交「API 声明」与「入参定义」两件事，服务端在同一事务内完成，
 * 避免出现「有 API 声明但入参定义缺失」这种无法调用的半成品配置。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
public class GenericApiSaveForm implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * API 声明：无 id 则新增，有 id 则更新
     */
    private SystemGenericApiEntity apiConfig;

    /**
     * 入参定义，按 {@code paramOrder} 顺序；为空表示目标方法无入参（同时清除旧登记）
     */
    private List<SystemGenericApiParamEntity> params;
}

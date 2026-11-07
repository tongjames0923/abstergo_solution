package com.tbs.app.service.genericapi;

import com.tbs.shared.interfaces.service.IServiceTemplate;
import com.tbs.shared.pojo.entity.SystemGenericApiParamEntity;

import java.util.List;

/**
 * 泛化API入参定义服务接口
 * <p>
 * 新版泛化调用链路的元信息真源：一次泛化调用的参数顺序、名称与类型全部来自这里的登记，
 * 网关因此不需要目标接口的 jar，服务方也不需要实现任何自定义契约。
 * </p>
 * <p>
 * 标准增删改查沿用服务模板，逐行删除走模板的 {@code deleteById}；本接口只追加按业务标识读取的能力。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public interface ISystemGenericApiParamService extends IServiceTemplate<SystemGenericApiParamEntity, Long> {

    /**
     * 按泛化API业务标识读取入参定义
     *
     * @param apiId API唯一标识，不能为空
     * @return 按 {@code paramOrder} 升序排列的参数定义，无登记时返回空列表
     */
    List<SystemGenericApiParamEntity> listByApiId(String apiId);
}

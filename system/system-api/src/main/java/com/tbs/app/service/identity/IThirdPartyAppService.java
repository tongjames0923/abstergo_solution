package com.tbs.app.service.identity;

import com.tbs.shared.interfaces.service.IServiceTemplate;
import com.tbs.shared.pojo.entity.ThirdPartyAppEntity;

/**
 * 第三方应用接出能力服务接口。
 * <p>
 * 继承通用服务模板 {@code IServiceTemplate}，对外提供第三方应用的登记与查询能力；
 * 核心字段一经创建不允许直接更新，更新类操作统一抛出 {@code third.party.app.update.not.supported} 业务异常。
 * 默认实现见 {@code DefaultThirdPartyServiceImpl}。
 * </p>
 */
public interface IThirdPartyAppService extends IServiceTemplate<ThirdPartyAppEntity, Long> {

}

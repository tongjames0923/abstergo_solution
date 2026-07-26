package com.tbs.app.controller.publics;

import com.tbs.app.service.genericapi.IGenericApiService;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.util.base.CollUtil;
import com.tbs.dic.enums.DataDicFlagEnum;
import com.tbs.shared.pojo.entity.SystemGenericApiEntity;
import com.tbs.web.genric.AbstractGenricApiInstance;
import com.tbs.web.genric.AbstractNoAuthGenricApiController;
import com.tbs.web.genric.HttpTriGenricApiInstance;
import com.tbs.web.uitl.DubboUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * 公开泛化调用接口控制器
 * <p>
 * 提供无需认证的泛化API调用功能，配置信息从数据库实体中获取
 * </p>
 *
 * @author abstergo
 */
@Slf4j
@RestController
@RequestMapping("/public/genric")
public class PublicGenricController extends AbstractNoAuthGenricApiController {

    @Resource
    IGenericApiService genericApiService;

    @Override
    protected Collection<AbstractGenricApiInstance> getGenricApiInstance(String id) {
        // 从数据库获取API配置，而不是从字典
        SystemGenericApiEntity apiConfig = genericApiService.getByApiId(id);
        AssertUtil.isTrue(apiConfig == null).errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR)
            .i18n("generic.api.not.exists").params(id).throwIt();
        AssertUtil.isTrue(!Objects.equals(DataDicFlagEnum.ENABLE.getCode(), apiConfig.getFlag()))
            .errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR).i18n("generic.api.disabled").params(apiConfig.getApiName())
            .throwIt();
        // 通过服务发现获取实例地址
        var services = DubboUtils.getServiceInstance(apiConfig.getAppName());
        AssertUtil.isTrue(CollUtil.isEmpty(services)).errorLevel(ErrorLevelEnum.BUSINESS_LOGIC_ERROR)
            .i18n("generic.api.service.instance.not.found").params(apiConfig.getAppName()).throwIt();

        List<AbstractGenricApiInstance> instances = new ArrayList<>();
        for (var service : services) {
            String baseUrl = service.getHost() + ":" + service.getPort();
            instances.add(new HttpTriGenricApiInstance(id, apiConfig.getVersion(), apiConfig.getNeedAuth(), baseUrl,
                apiConfig.getInterfaceName(), apiConfig.getMethodName()));
        }
        return instances;
    }

    @Override
    public String getBusinessName() {
        return "公开泛化调用接口";
    }
}

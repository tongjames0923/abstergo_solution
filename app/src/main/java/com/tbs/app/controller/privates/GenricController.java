package com.tbs.app.controller.privates;

import com.tbs.app.service.genericapi.IGenericApiService;
import com.tbs.common.util.base.CollUtil;
import com.tbs.dic.enums.DataDicFlagEnum;
import com.tbs.shared.pojo.entity.SystemGenericApiEntity;
import com.tbs.web.genric.AbstractAuthGenricApiController;
import com.tbs.web.genric.AbstractGenricApiInstance;
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
 * 泛化接口控制器（需要认证）
 * <p>
 * 提供需要认证的泛化API调用功能，配置信息从数据库实体中获取
 * </p>
 *
 * @author abstergo
 */
@Slf4j
@RestController
@RequestMapping("/private/genric")
public class GenricController extends AbstractAuthGenricApiController {

    @Resource
    IGenericApiService genericApiService;

    @Override
    public String getBusinessName() {
        return "泛化调用接口";
    }

    @Override
    protected Collection<AbstractGenricApiInstance> getGenricApiInstance(String id) {
        // 从数据库获取API配置
        SystemGenericApiEntity apiConfig = genericApiService.getByApiId(id);
        if (apiConfig == null) {
            log.warn("未找到API配置: {}", id);
            return List.of();
        }

        // 检查API是否启用
        if (!Objects.equals(DataDicFlagEnum.ENABLE.getCode(), apiConfig.getFlag())) {
            log.warn("API已被禁用: {}", id);
            return List.of();
        }

        // 通过服务发现获取实例地址
        var services = DubboUtils.getServiceInstance(apiConfig.getAppName());
        if (CollUtil.isEmpty(services)) {
            log.warn("未找到可用的服务实例: {}", apiConfig.getAppName());
            return List.of();
        }

        List<AbstractGenricApiInstance> instances = new ArrayList<>();
        for (var service : services) {
            String baseUrl = service.getHost() + ":" + service.getPort();
            instances.add(
                new HttpTriGenricApiInstance(id, apiConfig.getVersion() != null ? apiConfig.getVersion() : null,
                    apiConfig.getNeedAuth(), baseUrl, apiConfig.getInterfaceName(), apiConfig.getMethodName()));
        }
        return instances;
    }
}

package com.tbs.app.controller.publics;

import com.tbs.common.util.base.CollUtil;
import com.tbs.dic.enums.DataDicFlagEnum;
import com.tbs.dic.service.IDataDicService;
import com.tbs.web.genric.AbstractGenricApiInstance;
import com.tbs.web.genric.AbstractNoAuthGenricApiController;
import com.tbs.web.genric.HttpTriGenricApiInstance;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.registry.client.ServiceDiscovery;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
@RequestMapping("/public/genric")
public class PublicGenricController extends AbstractNoAuthGenricApiController {

    @Resource
    ServiceDiscovery serviceDiscovery;

    @Resource
    IDataDicService dataDicService;

    /** API ID -> 应用名（用于服务发现） */
    private static final String DIC_CODE = "GENRIC_API_ID_DIC";

    /** API ID -> 接口全限定名（用于Triple HTTP调用） */
    private static final String DIC_INTERFACE_CODE = "GENRIC_API_INTERFACE_DIC";

    /** API ID -> 方法名 */
    private static final String DIC_METHOD_CODE = "GENRIC_API_METHOD_DIC";

    @Override
    protected Collection<AbstractGenricApiInstance> getGenricApiInstance(String id) {
        var dic = CollUtil.getFirst(dataDicService.getDicByCode(DIC_CODE, true));
        if (dic == null || CollUtil.isEmpty(dic.getItems())) {
            return List.of();
        }
        var i = dic.getItems().stream().filter(
            item -> Objects.equals(DataDicFlagEnum.ENABLE.getCode(), item.getFlag()) &&
                Objects.equals(item.getLabel(), id)).findFirst();
        if (i.isEmpty()) {
            return List.of();
        }
        var item = i.get();
        // 从字典获取接口名
        String interfaceName = resolveInterfaceName(id);
        if (interfaceName == null) {
            log.warn("未找到API[{}]对应的接口名配置(DIC={})", id, DIC_INTERFACE_CODE);
            return List.of();
        }
        // 通过服务发现获取实例地址
        var services = serviceDiscovery.getInstances(item.getValue());
        if (CollUtil.isEmpty(services)) {
            return List.of();
        }
        String methodName = resolveMethodName(id);
        List<AbstractGenricApiInstance> instances = new ArrayList<>();
        for (var service : services) {
            String baseUrl = service.getHost() + ":" + service.getPort();
            instances.add(new HttpTriGenricApiInstance(id, null, true, baseUrl, interfaceName, methodName));
        }
        return instances;
    }

    private String resolveInterfaceName(String apiId) {
        var dic = CollUtil.getFirst(dataDicService.getDicByCode(DIC_INTERFACE_CODE, true));
        if (dic == null || CollUtil.isEmpty(dic.getItems())) {
            return null;
        }
        return dic.getItems().stream().filter(
            item -> Objects.equals(DataDicFlagEnum.ENABLE.getCode(), item.getFlag()) &&
                Objects.equals(item.getLabel(), apiId)).findFirst().map(item -> item.getValue()).orElse(null);
    }

    private String resolveMethodName(String apiId) {
        var methodDic = CollUtil.getFirst(dataDicService.getDicByCode(DIC_METHOD_CODE, true));
        if (methodDic == null || CollUtil.isEmpty(methodDic.getItems())) {
            return apiId;
        }
        return methodDic.getItems().stream().filter(
            item -> Objects.equals(DataDicFlagEnum.ENABLE.getCode(), item.getFlag()) &&
                Objects.equals(item.getLabel(), apiId)).findFirst().map(item -> item.getValue()).orElse(apiId);
    }

    @Override
    public String getBusinessName() {
        return "公开泛化调用接口";
    }
}

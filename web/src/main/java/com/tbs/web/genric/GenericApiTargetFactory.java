package com.tbs.web.genric;

import com.tbs.web.genric.config.GenericApiConfig;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 泛化调用目标工厂（新版链路）
 * <p>
 * 按登记配置产出一个调用目标，并负责两件一次性工作：注入泛化引用持有器、把容器内的预认证处理器建成索引，
 * 避免每请求一次容器类型扫描。目标对象本身很轻（重量件是引用，已由 {@link GenericReferenceHolder} 复用），
 * 因此这里不做目标缓存。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Component
public class GenericApiTargetFactory {

    /**
     * 泛化引用持有器
     */
    private final GenericReferenceHolder referenceHolder;

    /**
     * 预认证处理器索引：标识到实例
     */
    private final Map<String, IApiInstancePreAuth> preAuthMap;

    public GenericApiTargetFactory(GenericReferenceHolder referenceHolder,
        ObjectProvider<IApiInstancePreAuth> preAuthProvider) {
        this.referenceHolder = referenceHolder;
        Map<String, IApiInstancePreAuth> map = new LinkedHashMap<>();
        preAuthProvider.orderedStream().forEach(preAuth -> map.put(preAuth.getPreAuthId(), preAuth));
        this.preAuthMap = map;
    }

    /**
     * 按登记配置构建调用目标
     *
     * @param config 泛化调用配置，不能为空
     * @return 泛化调用目标
     */
    public ConfiguredGenricApiInstance create(GenericApiConfig config) {
        return new ConfiguredGenricApiInstance(config, referenceHolder, preAuthMap);
    }
}

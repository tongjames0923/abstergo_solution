package com.tbs.app.data;

import com.tbs.common.data.ITableDataHandler;
import com.tbs.common.provider.IRegisterContributor;
import com.tbs.common.provider.IRegistryCenter;
import com.tbs.common.provider.vo.InstanceProxy;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 表格数据处理器选择层
 * <p>
 * 经注册中心取贡献者链逐来源盘点 {@link ITableDataHandler}，贡献者顺序即来源优先级：
 * 本地来源在链首、远端来源在链尾，同一处理器 ID 在多来源并存时取靠前者。
 * </p>
 * <p>
 * 处理器 ID 即 business 坐标（{@link ITableDataHandler#getMeta()} 默认映射），按 ID 查找直接走坐标寻址，
 * 无需逐实例取 ID 比对。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Component
public class TableDataHandlerRegistry {

    /**
     * 注册中心，贡献者链的唯一入口
     */
    @Resource
    private IRegistryCenter registryCenter;

    /**
     * 枚举全部处理器
     * <p>
     * 坐标不约束：本选择层按业务 ID 而非 business/version 选用处理器。
     * </p>
     *
     * @return 全部处理器的实例代理，本地来源在前
     */
    public List<InstanceProxy> getAllHandlers() {
        List<InstanceProxy> handlers = new ArrayList<>();
        for (IRegisterContributor contributor : registryCenter.getAllContributors()) {
            List<InstanceProxy> instances =
                contributor.getAllInstanceByTypeAndBusinessAndVersion(ITableDataHandler.class, null, null);
            handlers.addAll(instances);
        }
        return handlers;
    }

    /**
     * 按 ID 查找处理器
     *
     * @param id 处理器 ID
     * @return 处理器实例，未找到返回 null
     */
    public ITableDataHandler getHandler(String id) {
        InstanceProxy instanceProxy = resolve(id);
        return instanceProxy == null ? null : (ITableDataHandler)instanceProxy.getInstance();
    }

    /**
     * 按 ID 查找处理器并回带命中来源
     *
     * @param id 处理器 ID
     * @return 命中时返回来源贡献者标识（spring / dubbo），未找到返回 null
     */
    public String getHandlerSource(String id) {
        InstanceProxy instanceProxy = resolve(id);
        if (instanceProxy == null || instanceProxy.getContributor() == null) {
            return null;
        }
        return instanceProxy.getContributor().getContributorId();
    }

    /**
     * 按处理器 ID 解析实例
     * <p>
     * 首选坐标寻址：处理器 ID 即 business 坐标，经注册中心按该坐标取首个命中，不必对每个实例回调 {@code getId()}； 未命中再退回逐来源盘点按 ID 匹配，覆盖 business 尚未对齐处理器 ID 的来源。
     * </p>
     *
     * @param id 处理器 ID
     * @return 命中的实例代理，未找到返回 null
     */
    private InstanceProxy resolve(String id) {
        InstanceProxy instanceProxy = registryCenter.getInstanceProxy(ITableDataHandler.class, id, null, null);
        if (instanceProxy != null) {
            return instanceProxy;
        }
        return getAllHandlers().stream()
            .filter(handler -> id.equals(((ITableDataHandler)handler.getInstance()).getId())).findFirst().orElse(null);
    }
}

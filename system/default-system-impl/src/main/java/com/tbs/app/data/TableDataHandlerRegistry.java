package com.tbs.app.data;

import com.tbs.common.data.ITableDataHandler;
import com.tbs.common.provider.IObjectProvider;
import com.tbs.common.provider.ObjectSource;
import com.tbs.common.provider.ResolvedObject;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.Optional;

/**
 * 表格数据处理器注册中心
 * <p>
 * 基于 {@link IObjectProvider#getObjects(Class)} 统一枚举 Spring 本地 Bean 与 Dubbo 远程实例，
 * 按 {@link ITableDataHandler#getId()} 精确匹配目标处理器。来源解析链天然 Spring 优先，
 * 本地有实现时直取（零协议开销），无本地实现时自动降级到 Dubbo 发现。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Component
public class TableDataHandlerRegistry {

    @Resource
    private IObjectProvider objectProvider;

    /**
     * 枚举全部来源的处理器（含来源元数据）
     * <p>
     * 返回 Spring 本地 Bean 与 Dubbo 注册中心发现实例的完整集合，顺序为 Spring 在前、Dubbo 在后。
     * </p>
     *
     * @return 带来源元数据的处理器集合
     */
    public Collection<ResolvedObject<ITableDataHandler>> getAllResolvedHandlers() {
        return objectProvider.getObjects(ITableDataHandler.class);
    }

    /**
     * 按 ID 查找处理器
     * <p>
     * 遍历全部来源解析结果，匹配 {@link ITableDataHandler#getId()}。
     * 由于 getObjects 按 Spring → Dubbo 顺序返回，同 ID 本地实现优先命中。
     * </p>
     *
     * @param id 处理器 ID
     * @return 处理器实例，未找到返回 null
     */
    public ITableDataHandler getHandler(String id) {
        return findResolved(id).map(ResolvedObject::getInstance).orElse(null);
    }

    /**
     * 按 ID 查找处理器并判定来源
     *
     * @param id 处理器 ID
     * @return "local" 或 "remote"，未找到返回 null
     */
    public String getHandlerSource(String id) {
        return findResolved(id).map(r -> r.isSource(ObjectSource.SPRING) ? "local" : "remote").orElse(null);
    }

    /**
     * 内部复用：按 ID 从全量枚举中定位首个匹配的 ResolvedObject
     */
    private Optional<ResolvedObject<ITableDataHandler>> findResolved(String id) {
        return getAllResolvedHandlers().stream().filter(r -> id.equals(r.getInstance().getId())).findFirst();
    }
}

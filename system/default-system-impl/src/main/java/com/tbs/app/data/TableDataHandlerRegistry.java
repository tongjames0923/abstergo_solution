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
 * 表格数据处理器选择层
 * <p>
 * 基于 {@link IObjectProvider#getObjects(Class)} 枚举注册中心内的 {@link ITableDataHandler} 条目，
 * 本地 Bean 与 Dubbo 逐实例条目并存（本地优先），按 {@link ITableDataHandler#getId()} 精确匹配目标处理器。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Component
public class TableDataHandlerRegistry {

    /**
     * 命中来源标识：本地处理器，前端据此展示「处理器可用（本地）」
     */
    private static final String SOURCE_LOCAL = "local";

    /**
     * 命中来源标识：Dubbo 远程处理器，前端据此展示「处理器可用（远程）」
     */
    private static final String SOURCE_REMOTE = "remote";

    @Resource
    private IObjectProvider objectProvider;

    /**
     * 枚举全部处理器（含来源元数据）
     * <p>
     * 返回注册中心内全部 {@link ITableDataHandler} 条目，按本地来源优先排序。
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
     * 遍历全部枚举结果，匹配 {@link ITableDataHandler#getId()}，同 ID 取首个命中。
     * </p>
     *
     * @param id 处理器 ID
     * @return 处理器实例，未找到返回 null
     */
    public ITableDataHandler getHandler(String id) {
        return findResolved(id).map(ResolvedObject::getInstance).orElse(null);
    }

    /**
     * 按 ID 查找处理器并判定命中来源
     * <p>
     * 按枚举结果携带的来源坐标判定：Dubbo 逐实例条目记 "remote"，其余记 "local"；
     * 未命中返回 null（前端据此展示「处理器不可用」）。
     * </p>
     *
     * @param id 处理器 ID
     * @return 命中时返回 "local" 或 "remote"，未找到返回 null
     */
    public String getHandlerSource(String id) {
        return findResolved(id)
            .map(resolved -> resolved.getCoordinate() != null
                && ObjectSource.DUBBO.equals(resolved.getCoordinate().getSource()) ? SOURCE_REMOTE : SOURCE_LOCAL)
            .orElse(null);
    }

    /**
     * 内部复用：按 ID 从全量枚举中定位首个匹配的 ResolvedObject
     */
    private Optional<ResolvedObject<ITableDataHandler>> findResolved(String id) {
        return getAllResolvedHandlers().stream().filter(r -> id.equals(r.getInstance().getId())).findFirst();
    }
}

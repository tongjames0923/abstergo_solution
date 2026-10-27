package com.tbs.app.data;

import com.tbs.common.data.ITableDataHandler;
import com.tbs.common.provider.IObjectProvider;
import com.tbs.common.provider.ResolvedObject;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.Collection;
import java.util.Optional;

/**
 * 表格数据处理器选择层
 * <p>
 * 基于 {@link IObjectProvider#getObjects(Class)} 枚举容器内的 {@link ITableDataHandler} 实现，
 * 按 {@link ITableDataHandler#getId()} 精确匹配目标处理器。枚举结果即统一注册中心中的 Spring 来源条目，
 * Dubbo 不参与逐实例枚举，故命中来源恒为本地。
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

    @Resource
    private IObjectProvider objectProvider;

    /**
     * 枚举全部处理器（含来源元数据）
     * <p>
     * 返回容器内全部 {@link ITableDataHandler} 实现，按本地来源优先排序。
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
     * 当前枚举结果仅含本地来源，命中即返回 {@link #SOURCE_LOCAL}；保留来源维度用于前端校验提示，
     * 并为后续接入其他来源留位。
     * </p>
     *
     * @param id 处理器 ID
     * @return 命中时返回 "local"，未找到返回 null
     */
    public String getHandlerSource(String id) {
        return findResolved(id).isPresent() ? SOURCE_LOCAL : null;
    }

    /**
     * 内部复用：按 ID 从全量枚举中定位首个匹配的 ResolvedObject
     */
    private Optional<ResolvedObject<ITableDataHandler>> findResolved(String id) {
        return getAllResolvedHandlers().stream().filter(r -> id.equals(r.getInstance().getId())).findFirst();
    }
}

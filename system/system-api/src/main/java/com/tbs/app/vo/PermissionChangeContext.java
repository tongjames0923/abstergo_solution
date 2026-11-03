package com.tbs.app.vo;

import com.tbs.app.service.role.IPermissionChangeListener;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 权限变更上下文
 * <p>
 * {@link IPermissionChangeListener} 的回调入参，承载一次权限组变更的最小事实：
 * 哪个方向改了什么类型、哪个权限组、影响到哪些用户、涉及哪些权限项、由谁操作。
 * 不携带权限项编码与名称等可派生信息，实现方按需回查，避免事件体与被查数据版本错位。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Data
public class PermissionChangeContext implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 变更类型
     * <p>
     * 类型本身即携带动作方向（得/丢、创建/更新/删除），与 {@link #roleIds}、{@link #userIds} 配合即可
     * 定位到具体的人与具体的权限，无需回查前后状态。
     * </p>
     */
    private IPermissionChangeListener.ChangeType type;

    /**
     * 发生变更的权限组标识
     */
    private Long groupId;

    /**
     * 发生变更的权限组名称
     * <p>
     * 权限组已被删除等取不到名称的场景下为 {@code null}，实现方不得依赖其非空。
     * </p>
     */
    private String groupName;

    /**
     * 受影响的用户标识列表
     * <p>
     * 方向由 {@link #type} 表达：权限项增删时为该组当前绑定的全部用户，用户归属变更时为该单个用户。
     * 列表可能为空（新建组下暂无绑定用户）。
     * </p>
     */
    private List<Long> userIds;

    /**
     * 涉及的权限项标识列表
     * <p>
     * 仅在权限项增删时有值；用户归属变更、权限组自身变更为 {@code null}。
     * </p>
     */
    private List<Long> roleIds;

    /**
     * 操作人用户名
     * <p>
     * 由登录会话内的操作触发时记录；无会话场景（启动任务、定时任务、内部调用）为 {@code null}。
     * </p>
     */
    private String operateBy;
}

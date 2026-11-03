package com.tbs.app.service.role;

import com.tbs.app.vo.PermissionChangeContext;

/**
 * 权限变更监听器（扩展点）
 * <p>
 * 供外部在权限组数据发生变更时挂接自己的处理逻辑，典型场景：审计留痕、向受影响用户推送「权限已变更」
 * 提醒、清理外部侧保存的权限副本（网关、缓存、SDK）。
 * </p>
 * <p>
 * 接入方式与 {@code ITokenCleanner} 同构：实现本接口并注册为 Spring Bean 即自动接入，
 * 无需额外配置项或开关；容器内无实现时不产生任何调用开销。
 * </p>
 * <p>
 * 调用约定（契约的一部分，实现方须按此假设编写）：
 * <ul>
 *     <li>派发时机在权限数据写库成功、且相关缓存已清除之后，实现方回读权限得到的一定是最新值；</li>
 *     <li>同步同线程调用，不异步，因此实现方不得在此做耗时操作；需要异步由实现方自行声明；</li>
 *     <li>单个监听器抛出的异常由派发方捕获并记录日志，不影响其他监听器，也不影响业务主流程；</li>
 *     <li>监听器只读不写权限数据，不得在此再次触发权限变更写入，避免递归派发。</li>
 * </ul>
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 * @see PermissionChangeContext
 */
public interface IPermissionChangeListener {

    /**
     * 权限变更类型
     * <p>
     * 每个取值对应一个真实写入入口，且自身即携带动作方向（新增还是移除），实现方一个 switch 就能
     * 得出「谁得了还是丢了哪项权限」，不需要与其他字段组合解读，也不依赖前后状态 diff（事件在写库后
     * 派发，变更前状态已不可取回）。
     * </p>
     */
    enum ChangeType {

        /**
         * 权限组新增了一项权限（组内全部用户取得该权限）
         */
        ROLE_APPENDED,

        /**
         * 权限组移除了一项权限（组内全部用户失去该权限）
         */
        ROLE_REMOVED,

        /**
         * 用户被加入权限组（该用户取得整组权限）
         */
        USER_APPENDED,

        /**
         * 用户被移出权限组（该用户失去整组权限）
         */
        USER_REMOVED,

        /**
         * 新建权限组（此时组下尚无绑定用户）
         */
        GROUP_CREATED,

        /**
         * 权限组自身变更（名称、启用/停用状态）
         */
        GROUP_UPDATED,

        /**
         * 权限组被删除（组下原有绑定用户失去该组权限）
         */
        GROUP_DELETED
    }

    /**
     * 获取监听器名称
     * <p>
     * 仅用于日志定位，须能唯一标识一个实现，抛异常时靠它定位是哪一路监听器失败。
     * </p>
     *
     * @return 监听器名称，不能为空
     */
    String getName();

    /**
     * 权限发生变更时回调
     *
     * @param context 变更上下文，不能为空；含变更类型、权限组、受影响的用户与权限项
     */
    void onPermissionChanged(PermissionChangeContext context);
}

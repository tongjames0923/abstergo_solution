package com.tbs.sdk.model;

/**
 * 升级动作类型常量
 * <p>
 * 对齐后端AppVersionCompatibilityActionEnum的code值：升级检查命中时服务端返回的动作强度。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public final class AppUpdateAction {

    /**
     * 强制更新：低于最低兼容版本时阻断使用直至升级
     */
    public static final int FORCE = 1;

    /**
     * 推荐更新：提示用户升级但不阻断使用
     */
    public static final int RECOMMEND = 2;

    /**
     * 仅提示：告知存在新版本，不做进一步干预
     */
    public static final int NOTIFY = 3;

    private AppUpdateAction() {
    }

    /**
     * 动作编码转可读名称（force/recommend/notify）。
     *
     * @param action 动作编码（可为null）
     * @return 可读名称，未知值或null时返回null
     */
    public static String toName(Integer action) {
        if (action == null) {
            return null;
        }
        switch (action) {
            case FORCE:
                return "force";
            case RECOMMEND:
                return "recommend";
            case NOTIFY:
                return "notify";
            default:
                return null;
        }
    }
}

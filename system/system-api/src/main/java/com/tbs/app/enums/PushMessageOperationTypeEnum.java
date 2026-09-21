package com.tbs.app.enums;

import com.tbs.common.BaseEnum;

import java.util.Set;

/**
 * 推送消息操作类型枚举
 * <p>
 * 消息全链路操作日志（push_message_operation_log）的 op_type 取值域： 数据库存 {@link #getValue()} 字符串值（SEND/READ/CLICK/...），允许业务后续扩展新操作类型。
 * 该枚举仅枚举框架内置的终态操作，扩展操作可经白名单机制登记。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public enum PushMessageOperationTypeEnum implements BaseEnum<String> {

    /**
     * 发送：消息全链路起点，发送事务内与消息本体同事务落库
     */
    SEND("SEND", "发送"),

    /**
     * 已读：派生读状态的唯一依据，存在READ记录即视为已读
     */
    READ("READ", "已读"),

    /**
     * 点击：用户点击消息跳转等交互行为
     */
    CLICK("CLICK", "点击");

    /**
     * 内置操作类型取值白名单（校验与派生规则使用）
     */
    public static final Set<String> SUPPORTED_VALUES = Set.of(SEND.value, READ.value, CLICK.value);

    /**
     * 枚举编码（按声明顺序，从1开始）
     */
    private final Integer code;

    /**
     * 枚举值（数据库存储的字符串标识）
     */
    private final String value;
    /**
     * 操作类型展示标签
     */
    private final String label;

    /**
     * 构造操作类型枚举
     *
     * @param value 数据库存储的字符串标识
     * @param label 展示标签
     */
    PushMessageOperationTypeEnum(String value, String label) {
        this.value = value;
        this.code = ordinal() + 1;
        this.label = label;
    }

    /**
     * 判断取值是否为内置合法操作类型
     *
     * @param value 操作类型字符串
     * @return true表示合法
     */
    public static boolean isSupported(String value) {
        return value != null && SUPPORTED_VALUES.contains(value);
    }

    /**
     * 按字符串取值解析枚举
     *
     * @param value 操作类型字符串
     * @return 对应枚举，不合法时返回null
     */
    public static PushMessageOperationTypeEnum fromValue(String value) {
        if (value == null) {
            return null;
        }
        for (PushMessageOperationTypeEnum type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 获取数据库存储的字符串标识
     *
     * @return 操作类型字符串（SEND/READ/CLICK）
     */
    @Override
    public String getValue() {
        return value;
    }

    /**
     * 获取枚举编码
     *
     * @return 枚举编码
     */
    @Override
    public Integer getCode() {
        return code;
    }

    /**
     * 获取展示标签
     *
     * @return 展示标签
     */
    public String getLabel() {
        return label;
    }

}

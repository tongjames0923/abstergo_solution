package com.tbs.boardcast.enums;

import com.tbs.common.BaseEnum;

/**
 * 事件状态枚举
 * <p>
 * 用于标识事件处理生命周期中的不同状态，包含以下状态：
 * <ul>
 *     <li>IDLE: 初始空闲状态</li>
 *     <li>IN_QUEUE: 已进入队列等待处理</li>
 *     <li>IN_PROCESS: 正在处理中</li>
 *     <li>DONE: 处理成功完成</li>
 *     <li>FAILED: 处理异常失败</li>
 *     <li>CANCELED: 已被主动取消</li>
 *     <li>REQUIRE_CANCEL: 需要取消处理</li>
 *     <li>REQUIRE_RETRY: 需要重试处理</li>
 * </ul>
 * 终态(isTerminal)表示该状态为处理最终状态，后续不可再变更
 *
 * @author abstergo
 */
public enum EventStatusEnum implements BaseEnum<String> {

    /**
     * 空闲状态
     */
    IDLE("空闲", 0),
    /**
     * 等待处理状态
     */
    IN_QUEUE("等待处理", 1),

    /**
     * 处理中状态
     */
    IN_PROCESS("处理中", 2),

    /**
     * 处理完成状态
     */
    DONE("处理完成", 3, true),
    /**
     * 处理异常失败状态
     */
    FAILED("处理失败", 4, true),

    /**
     * 取消处理状态
     */
    CANCELED("取消处理", 5, true),
    /**
     * 需要取消处理状态
     */
    REQUIRE_CANCEL("需要取消处理", 6),
    /**
     * 需要重试处理状态
     */
    REQUIRE_RETRY("需要重试", 7);

    /**
     * 是否为终态标志 终态表示该状态为处理最终状态，后续不可再变更
     */
    private boolean isTerminal;

    /**
     * 构造函数(带终态标识)
     *
     * @param value      状态描述文本
     * @param code       状态编码
     * @param isTerminal 是否为终态
     */
    EventStatusEnum(String value, int code, boolean isTerminal) {
        this.value = value;
        this.code = code;
        this.isTerminal = isTerminal;
    }

    /**
     * 构造函数(默认非终态)
     *
     * @param value 状态描述文本
     * @param code  状态编码
     */
    EventStatusEnum(String value, int code) {
        this.value = value;
        this.code = code;
        this.isTerminal = false;
    }

    /**
     * 状态描述文本
     */
    private String value;

    /**
     * 状态编码
     */
    private int code;

    /**
     * 获取状态描述文本
     *
     * @return 状态描述字符串
     */
    @Override
    public String getValue() {
        return value;
    }

    /**
     * 获取状态编码
     *
     * @return 状态编码数值
     */
    @Override
    public int getCode() {
        return code;
    }

    /**
     * 判断是否为终态 终态表示该状态为处理最终状态，后续不可再变更
     *
     * @return true表示是终态，false表示非终态
     */
    public boolean isTerminal() {
        return isTerminal;
    }
}

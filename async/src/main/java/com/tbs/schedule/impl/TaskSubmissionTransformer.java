package com.tbs.schedule.impl;

import com.tbs.common.data.ITransformer;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.JsonUtils;
import com.tbs.common.util.base.StrUtil;
import com.tbs.schedule.api.ExecutionPolicy;
import com.tbs.schedule.constant.ScheduleI18nConstant;
import com.tbs.schedule.api.TaskPayload;
import com.tbs.schedule.api.TaskSubmission;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;

/**
 * 任务提交描述持久化转换器
 * <p>
 * 基于 common 模块数据转换 SPI（{@link ITransformer}）实现 {@link TaskSubmission} 与 JSON 字节线格式的互转， 供 {@code TaskStore} 实现（如
 * redis-mod 的 {@code RedisTaskStore}）完成任务落盘与重启恢复， 符合框架"生产序列化采用 JSON"的持久化规范。
 * </p>
 * <p>
 * <b>线格式：</b>内部 {@link Snapshot} 快照承载全部状态；触发时刻以 epoch 毫秒存储，规避不同 ObjectMapper 日期配置差异；
 * 多态载荷与策略覆盖均以"实现类全限定名 + JSON 内容"成对存储——不全局开启 Jackson 多态类型信息（控制爆炸半径）， 且可实例化类型限定为 {@link TaskPayload} /
 * {@link ExecutionPolicy} 实现类，阻断任意类实例化。
 * </p>
 * <p>
 * <b>策略覆盖降级规则：</b>{@link ExecutionPolicy} 为行为型接口（含失败处理器引用），无法保证可靠还原，
 * 序列化与还原任一失败均告警并降级为空，任务恢复后由三级策略解析（单任务 &gt; 按任务类型 &gt; 全局默认）兜底。
 * </p>
 * <p>
 * 本转换器无状态、线程安全，可直接复用单实例。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
public class TaskSubmissionTransformer implements ITransformer<TaskSubmission> {

    /**
     * 序列化：任务提交描述转 JSON 字节
     * <p>
     * 入口防御：拒绝空对象、内存态载荷（{@link TaskPayload#isPersistable()} 为 {@code false}， 如 {@link LocalRunnablePayload} 持有的 Runnable
     * 闭包）与空触发时刻；快照构建或 JSON 化失败抛出格式化断言异常。
     * </p>
     *
     * @param value 任务提交描述
     * @return JSON 线格式字节
     */
    @Override
    public byte[] transform(TaskSubmission value) {
        AssertUtil.isTrue(value == null).i18n(ScheduleI18nConstant.TRANSFORM_SUBMISSION_NULL).throwIt();
        AssertUtil.isTrue(!value.isPersistable()).i18n(ScheduleI18nConstant.TRANSFORM_SUBMISSION_NOT_PERSISTABLE)
            .params(value.getTaskId()).throwIt();
        AssertUtil.isTrue(value.getTriggerAt() == null).i18n(ScheduleI18nConstant.TRANSFORM_SUBMISSION_TRIGGER_AT_NULL)
            .params(value.getTaskId()).throwIt();
        Snapshot snapshot = new Snapshot();
        snapshot.setTaskId(value.getTaskId());
        snapshot.setTaskType(value.getTaskType());
        snapshot.setTriggerAt(value.getTriggerAt().getTime());
        snapshot.setPayloadType(value.getPayload().getClass().getName());
        snapshot.setPayloadJson(JsonUtils.toJSONString(value.getPayload()));
        AssertUtil.isTrue(snapshot.getPayloadJson() == null).i18n(ScheduleI18nConstant.TRANSFORM_SERIALIZE_FAILED)
            .params(value.getTaskId()).throwIt();
        fillPolicySnapshot(snapshot, value);
        byte[] bytes = JsonUtils.toJSONBytes(snapshot);
        AssertUtil.isTrue(bytes == null).i18n(ScheduleI18nConstant.TRANSFORM_SERIALIZE_FAILED).params(value.getTaskId())
            .throwIt();
        return bytes;
    }

    /**
     * 反序列化：JSON 字节还原任务提交描述
     * <p>
     * 快照结构不完整、载荷类型不可解析、载荷内容还原失败均抛出格式化断言异常； 策略覆盖按降级规则尽力还原，失败不阻断任务本身的恢复。
     * </p>
     *
     * @param value JSON 线格式字节
     * @param clazz 目标类型（须为 {@link TaskSubmission} 及其子类）
     * @return 还原后的任务提交描述
     */
    @Override
    public <T extends TaskSubmission> T reverseTransform(byte[] value, Class<T> clazz) {
        AssertUtil.isTrue(value == null || value.length == 0).i18n(ScheduleI18nConstant.TRANSFORM_DESERIALIZE_FAILED).throwIt();
        Snapshot snapshot = JsonUtils.parseObject(value, Snapshot.class);
        AssertUtil.isTrue(snapshot == null ||
            StrUtil.isEmpty(snapshot.getTaskId()) ||
            StrUtil.isEmpty(snapshot.getTaskType()) ||
            StrUtil.isEmpty(snapshot.getPayloadType()) ||
            StrUtil.isEmpty(snapshot.getPayloadJson()) ||
            snapshot.getTriggerAt() <= 0).i18n(ScheduleI18nConstant.TRANSFORM_DESERIALIZE_FAILED).throwIt();
        TaskPayload payload = restorePayload(snapshot);
        ExecutionPolicy policy = restorePolicy(snapshot);
        Date triggerAt = new Date(snapshot.getTriggerAt());
        return clazz.cast(new TaskSubmission(snapshot.getTaskId(), snapshot.getTaskType(), payload, triggerAt, policy));
    }

    /**
     * 还原多态任务载荷
     * <p>
     * 按快照携带的实现类全限定名反射定位类型，校验其为 {@link TaskPayload} 实现后经 JSON 绑定还原， 任意模块扩展的载荷类型均可恢复；类型不可解析或非 {@link TaskPayload}
     * 实现、内容还原失败均抛出断言异常。
     * </p>
     *
     * @param snapshot 持久化快照
     * @return 任务载荷
     */
    private TaskPayload restorePayload(Snapshot snapshot) {
        Class<?> payloadType = resolveClass(snapshot.getPayloadType());
        AssertUtil.isTrue(payloadType == null || !TaskPayload.class.isAssignableFrom(payloadType))
            .i18n(ScheduleI18nConstant.TRANSFORM_PAYLOAD_TYPE_INVALID).params(snapshot.getPayloadType()).throwIt();
        TaskPayload payload =
            JsonUtils.parseObject(snapshot.getPayloadJson(), payloadType.asSubclass(TaskPayload.class));
        AssertUtil.isTrue(payload == null).i18n(ScheduleI18nConstant.TRANSFORM_PAYLOAD_PARSE_FAILED).params(snapshot.getTaskId())
            .throwIt();
        return payload;
    }

    /**
     * 尽力还原策略覆盖（降级规则）
     * <p>
     * {@link ExecutionPolicy} 为行为型接口，还原失败不阻断任务恢复：类型不可用、内容还原失败或还原异常 均记 warn 日志并返回 {@code null}，任务恢复后由三级策略解析兜底。
     * </p>
     *
     * @param snapshot 持久化快照
     * @return 策略覆盖（不可恢复时返回 null）
     */
    private ExecutionPolicy restorePolicy(Snapshot snapshot) {
        if (snapshot.getPolicyType() == null || snapshot.getPolicyJson() == null) {
            return null;
        }
        try {
            Class<?> policyType = resolveClass(snapshot.getPolicyType());
            if (policyType == null || !ExecutionPolicy.class.isAssignableFrom(policyType)) {
                log.warn("策略覆盖类型不可用，降级为空（恢复后按默认三级策略解析）: policyType={}, taskId={}",
                    snapshot.getPolicyType(), snapshot.getTaskId());
                return null;
            }
            ExecutionPolicy policy =
                JsonUtils.parseObject(snapshot.getPolicyJson(), policyType.asSubclass(ExecutionPolicy.class));
            if (policy == null) {
                log.warn("策略覆盖内容还原失败，降级为空（恢复后按默认三级策略解析）: policyType={}, taskId={}",
                    snapshot.getPolicyType(), snapshot.getTaskId());
                return null;
            }
            return policy;
        } catch (Exception e) {
            log.warn("策略覆盖还原异常，降级为空（恢复后按默认三级策略解析）: policyType={}, taskId={}",
                snapshot.getPolicyType(), snapshot.getTaskId(), e);
            return null;
        }
    }

    /**
     * 尽力快照策略覆盖（降级规则）
     * <p>
     * 序列化失败不阻断任务落盘：仅记 warn 日志并跳过策略覆盖，任务恢复后由三级策略解析兜底。
     * </p>
     *
     * @param snapshot 持久化快照
     * @param value    任务提交描述
     */
    private void fillPolicySnapshot(Snapshot snapshot, TaskSubmission value) {
        value.getPolicyOverride().ifPresent(policy -> {
            try {
                String policyJson = JsonUtils.toJSONString(policy);
                if (policyJson == null) {
                    log.warn("策略覆盖序列化失败，跳过快照（恢复后按默认三级策略解析）: taskId={}, policyType={}",
                        value.getTaskId(), policy.getClass().getName());
                    return;
                }
                snapshot.setPolicyType(policy.getClass().getName());
                snapshot.setPolicyJson(policyJson);
            } catch (Exception e) {
                log.warn("策略覆盖序列化异常，跳过快照（恢复后按默认三级策略解析）: taskId={}, policyType={}",
                    value.getTaskId(), policy.getClass().getName(), e);
            }
        });
    }

    /**
     * 按全限定名解析类型
     *
     * @param className 类型全限定名
     * @return 类型（不可解析返回 null）
     */
    private Class<?> resolveClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * 持久化线格式快照
     * <p>
     * 承载任务提交描述的可序列化状态：触发时刻以 epoch 毫秒存储规避日期配置差异， 多态载荷与策略覆盖以"实现类全限定名 + JSON 内容"成对存储支持开放多态还原。
     * </p>
     */
    @Data
    @NoArgsConstructor
    private static class Snapshot {

        /**
         * 任务唯一标识
         */
        private String taskId;

        /**
         * 任务类型
         */
        private String taskType;

        /**
         * 绝对触发时刻（epoch 毫秒）
         */
        private long triggerAt;

        /**
         * 载荷实现类全限定名
         */
        private String payloadType;

        /**
         * 载荷 JSON 内容
         */
        private String payloadJson;

        /**
         * 策略覆盖实现类全限定名（未携带或降级时为空）
         */
        private String policyType;

        /**
         * 策略覆盖 JSON 内容（未携带或降级时为空）
         */
        private String policyJson;
    }
}

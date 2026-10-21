package com.tbs.common.provider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.provider.impl.DefaultRegistryEntry;

/**
 * 统一注册中心阶段 1 契约自检：验证 {@link SourceCoordinate} 的 null 归一、三元组等值、来源必填，
 * 以及 {@link DefaultRegistryEntry} 的唯一身份等值、懒解析只构建一次、必填校验。
 * <p>
 * 非 Spring 上下文级测试——直接驱动值对象与条目实现；缺失字段异常走 AssertUtil.i18n，
 * 无 messageSource 时回退为以 key 作文本并抛出 {@link FormattedRuntimeException}，故只断言异常类型。
 * </p>
 *
 * @author abstergo
 */
class RegistryCenterContractTest {

    /**
     * 坐标 null 归一为空串，且空串与 null 构造的坐标等值
     */
    @Test
    void coordinateNullsCoercedToEmptyAndEquals() {
        SourceCoordinate a = SourceCoordinate.of(ObjectSource.SPRING, null, null);
        SourceCoordinate b = SourceCoordinate.of(ObjectSource.SPRING, "", "");

        assertEquals("", a.getBusiness());
        assertFalse(a.hasBusiness());
        assertFalse(a.hasVersion());
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    /**
     * 同来源同 host 但 business/version 不同的坐标互不相等（同机多实例区分根据）
     */
    @Test
    void coordinateDistinguishesBusinessAndVersion() {
        SourceCoordinate order = SourceCoordinate.of(ObjectSource.DUBBO, "order", "1.0");
        SourceCoordinate inventory = SourceCoordinate.of(ObjectSource.DUBBO, "inventory", "1.0");
        SourceCoordinate orderV2 = SourceCoordinate.of(ObjectSource.DUBBO, "order", "2.0");

        assertNotEquals(order, inventory);
        assertNotEquals(order, orderV2);
        assertTrue(order.hasBusiness() && order.hasVersion());
    }

    /**
     * 坐标来源必填，缺失快速失败
     */
    @Test
    void coordinateRequiresSource() {
        assertThrows(FormattedRuntimeException.class, () -> SourceCoordinate.of(null, "order", "1.0"));
    }

    /**
     * 条目身份仅由 (key, coordinate, instanceKey) 决定，order 与实例本体不参与等值
     */
    @Test
    void entryIdentityByTripleEquals() {
        SourceCoordinate coordinate = SourceCoordinate.of(ObjectSource.DUBBO, "order", "1.0");
        RegistryEntry e1 = DefaultRegistryEntry.ofInstance(CharSequence.class, coordinate, "10.0.0.5:20880", 0, "impl-A");
        RegistryEntry e2 = DefaultRegistryEntry.ofInstance(CharSequence.class, coordinate, "10.0.0.5:20880", 100, "impl-B");
        RegistryEntry other = DefaultRegistryEntry.ofInstance(CharSequence.class,
            SourceCoordinate.of(ObjectSource.DUBBO, "inventory", "1.0"), "10.0.0.5:20880", 0, "impl-A");

        assertEquals(e1, e2);
        assertEquals(e1.hashCode(), e2.hashCode());
        assertNotEquals(e1, other);
    }

    /**
     * 懒解析条目首次 resolve 触发供给器一次并缓存，后续复用同一实例
     */
    @Test
    void entryLazyResolveBuildsOnceAndCaches() {
        AtomicInteger buildCount = new AtomicInteger();
        SourceCoordinate coordinate = SourceCoordinate.ofSource(ObjectSource.DUBBO);
        RegistryEntry entry = DefaultRegistryEntry.of(CharSequence.class, coordinate, "10.0.0.5:20880", 0,
            () -> {
                buildCount.incrementAndGet();
                return new Object();
            });

        Object first = entry.resolve();
        Object second = entry.resolve();

        assertSame(first, second);
        assertEquals(1, buildCount.get());
    }

    /**
     * 条目关键字段（接口键、实例标识）缺失均快速失败
     */
    @Test
    void entryRequiresKeyFields() {
        SourceCoordinate coordinate = SourceCoordinate.ofSource(ObjectSource.SPRING);
        assertThrows(FormattedRuntimeException.class,
            () -> DefaultRegistryEntry.ofInstance(null, coordinate, "k", 0, new Object()));
        assertThrows(FormattedRuntimeException.class,
            () -> DefaultRegistryEntry.ofInstance(CharSequence.class, coordinate, null, 0, new Object()));
    }
}

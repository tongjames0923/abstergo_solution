package com.tbs.common.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.tbs.common.exception.FormattedRuntimeException;

/**
 * {@link KeyedRegistry} 自检：验证进程内注册表的单/多值语义、唯一性快速失败、注销回收、
 * 由集合构建，以及 {@link IRegistryStore} 扩展位可注入。
 * <p>
 * 非 Spring 上下文级测试——直接驱动注册表核心方法；重复键等异常走 AssertUtil.i18n，
 * 在无 messageSource 时回退为以 key 作为消息文本并抛出 {@link FormattedRuntimeException}，故只断言异常类型。
 * </p>
 *
 * @author abstergo
 */
class KeyedRegistryTest {

    /**
     * 多值模式：同一键下可挂载多个值，getAll 取回全部，getFirst 命中首个
     */
    @Test
    void multiRegistryAllowsSameKeyMultipleValues() {
        KeyedRegistry<String, String> registry = KeyedRegistry.multiInMemory();
        registry.register("topic", "consumer-A");
        registry.register("topic", "consumer-B");

        assertEquals(2, registry.getAll("topic").size());
        assertTrue(registry.getFirst("topic").isPresent());
        assertTrue(registry.supports("topic"));
        assertFalse(registry.supports("absent"));
    }

    /**
     * 单值模式：同键重复注册快速失败，首次注册的值保持不变
     */
    @Test
    void uniqueRegistryRejectsDuplicateKey() {
        KeyedRegistry<String, String> registry = KeyedRegistry.uniqueInMemory();
        registry.register("grantType", "provider-1");

        assertThrows(FormattedRuntimeException.class, () -> registry.register("grantType", "provider-2"));
        assertEquals("provider-1", registry.getFirst("grantType").orElse(null));
    }

    /**
     * 注销最后一个值后回收该键，回查为空
     */
    @Test
    void unregisterRemovesValueAndReclaimsKey() {
        KeyedRegistry<String, String> registry = KeyedRegistry.multiInMemory();
        registry.register("key", "value");
        registry.unregister("key", "value");

        assertFalse(registry.supports("key"));
        assertTrue(registry.getAll("key").isEmpty());
    }

    /**
     * 由值集合按键提取器构建单值注册表：正常建表可查，源集合内键冲突则快速失败
     */
    @Test
    void buildUniqueFromCollectionWithKeyExtractor() {
        List<Handler> handlers = List.of(new Handler("a"), new Handler("b"));
        KeyedRegistry<String, Handler> registry = KeyedRegistry.uniqueFrom(handlers, Handler::getKey);

        assertEquals(2, registry.keys().size());
        assertEquals("a", registry.getFirst("a").orElseThrow().getKey());

        List<Handler> conflicted = List.of(new Handler("x"), new Handler("x"));
        assertThrows(FormattedRuntimeException.class,
            () -> KeyedRegistry.uniqueFrom(conflicted, Handler::getKey));
    }

    /**
     * 未命中键时 getFirst 返回空 Optional，getAll 返回空列表
     */
    @Test
    void readAbsentKeyReturnsEmpty() {
        KeyedRegistry<String, String> registry = KeyedRegistry.uniqueInMemory();
        assertTrue(registry.getFirst("missing").isEmpty());
        assertTrue(registry.getAll("missing").isEmpty());
    }

    /**
     * 扩展位校验：注入自定义存储实现后，门面读写直传该存储，证明可替换后端
     */
    @Test
    void customStoreCanBeInjectedAsExtensionSeam() {
        InMemoryRegistryStore<String, String> store = new InMemoryRegistryStore<>();
        KeyedRegistry<String, String> registry = new KeyedRegistry<>(store, true);
        registry.register("k", "v");

        assertTrue(store.containsKey("k"));
        assertEquals("v", registry.getFirst("k").orElse(null));
    }

    /**
     * 自检辅助类型：仅携带一个作为路由键的字符串
     */
    static class Handler {

        private final String key;

        Handler(String key) {
            this.key = key;
        }

        String getKey() {
            return key;
        }
    }
}

package com.tbs.common.util.base;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.tbs.common.util.framework.SpringUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * JSON工具类 - 统一封装Jackson操作
 * <p>
 * 提供线程安全的JSON序列化/反序列化操作，隔离底层实现。
 * 如果需要更换JSON库，只需修改此类即可。
 * </p>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li>对象与JSON字符串互转</li>
 *   <li>对象与JSON字节数组互转</li>
 *   <li>JSON字符串反序列化为泛型对象</li>
 *   <li>JSON字符串反序列化为List/Map等集合类型</li>
 *   <li>格式化输出JSON</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 序列化
 * String json = JsonUtils.toJSONString(user);
 *
 * // 反序列化
 * User user = JsonUtils.parseObject(json, User.class);
 *
 * // 反序列化为List
 * List<User> users = JsonUtils.parseArray(json, User.class);
 *
 * // 格式化输出
 * String prettyJson = JsonUtils.toPrettyJSONString(user);
 * }</pre>
 *
 * @author abstergo
 * @since 1.0.0
 */
@Slf4j
public final class JsonUtils {

    private JsonUtils() {
        // 工具类禁止实例化
    }

    /**
     * 默认ObjectMapper实例（兜底，Spring容器未初始化时使用）
     */
    private static final ObjectMapper DEFAULT_MAPPER = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
        .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

    /**
     * 默认格式化ObjectMapper实例（兜底）
     */
    private static final ObjectMapper DEFAULT_PRETTY_MAPPER = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, true)
        .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
        .configure(SerializationFeature.INDENT_OUTPUT, true);

    /**
     * 获取ObjectMapper实例（优先使用Spring容器中的，保证配置统一）
     *
     * @return ObjectMapper实例
     */
    private static ObjectMapper getMapper() {
        try {
            ObjectMapper springMapper = SpringUtil.getBean(ObjectMapper.class);
            if (springMapper != null) {
                return springMapper;
            }
        } catch (Exception e) {
            log.error("获取Spring容器中的ObjectMapper失败", e);
            // Spring容器未初始化或未配置ObjectMapper，使用默认实例
        }
        log.warn("未找到Spring容器中的ObjectMapper，使用默认实例");
        return DEFAULT_MAPPER;
    }

    /**
     * 获取格式化ObjectMapper实例
     *
     * @return 格式化输出的ObjectMapper实例
     */
    private static ObjectMapper getPrettyMapper() {
        try {
            ObjectMapper springMapper = SpringUtil.getBean(ObjectMapper.class);
            if (springMapper != null) {
                // 使用Spring的Mapper并启用格式化
                return springMapper.copy().configure(SerializationFeature.INDENT_OUTPUT, true);
            }
        } catch (Exception e) {
            // Spring容器未初始化或未配置ObjectMapper，使用默认实例
        }
        return DEFAULT_PRETTY_MAPPER;
    }

    // ==================== 序列化方法 ====================

    /**
     * 将对象序列化为JSON字符串
     *
     * @param obj 要序列化的对象
     * @return JSON字符串，序列化失败返回null
     */
    public static String toJSONString(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return getMapper().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("JSON序列化失败: {}", obj.getClass().getName(), e);
            return null;
        }
    }

    /**
     * 将对象序列化为格式化的JSON字符串
     *
     * @param obj 要序列化的对象
     * @return 格式化的JSON字符串，序列化失败返回null
     */
    public static String toPrettyJSONString(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return getPrettyMapper().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("JSON序列化失败: {}", obj.getClass().getName(), e);
            return null;
        }
    }

    /**
     * 将对象序列化为JSON字节数组
     *
     * @param obj 要序列化的对象
     * @return JSON字节数组，序列化失败返回null
     */
    public static byte[] toJSONBytes(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return getMapper().writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            log.error("JSON序列化失败: {}", obj.getClass().getName(), e);
            return null;
        }
    }

    // ==================== 反序列化方法 ====================

    /**
     * 将JSON字符串反序列化为指定类型
     *
     * @param json  JSON字符串
     * @param clazz 目标类型
     * @param <T>   目标类型泛型
     * @return 反序列化后的对象，失败返回null
     */
    public static <T> T parseObject(String json, Class<T> clazz) {
        if (json == null || json.isEmpty() || clazz == null) {
            return null;
        }
        try {
            return getMapper().readValue(json, clazz);
        } catch (IOException e) {
            log.error("JSON反序列化失败: targetClass={}, json={}", clazz.getName(), json, e);
            return null;
        }
    }

    /**
     * 将JSON字节数组反序列化为指定类型
     *
     * @param bytes JSON字节数组
     * @param clazz 目标类型
     * @param <T>   目标类型泛型
     * @return 反序列化后的对象，失败返回null
     */
    public static <T> T parseObject(byte[] bytes, Class<T> clazz) {
        if (bytes == null || bytes.length == 0 || clazz == null) {
            return null;
        }
        try {
            return getMapper().readValue(bytes, clazz);
        } catch (IOException e) {
            log.error("JSON反序列化失败: targetClass={}", clazz.getName(), e);
            return null;
        }
    }

    /**
     * 将JSON字符串反序列化为复杂泛型对象
     * <p>
     * 适用于带泛型的类型，如 {@code List<User>}、{@code Map<String, Object>} 等
     * </p>
     *
     * <h3>使用示例</h3>
     * <pre>{@code
     * // 反序列化为List<User>
     * List<User> users = JsonUtils.parseObject(json, new TypeReference<List<User>>(){});
     *
     * // 反序列化为Map<String, Object>
     * Map<String, Object> map = JsonUtils.parseObject(json, new TypeReference<Map<String, Object>>(){});
     * }</pre>
     *
     * @param json          JSON字符串
     * @param typeReference 类型引用
     * @param <T>           目标类型泛型
     * @return 反序列化后的对象，失败返回null
     */
    public static <T> T parseObject(String json, TypeReference<T> typeReference) {
        if (json == null || json.isEmpty() || typeReference == null) {
            return null;
        }
        try {
            return getMapper().readValue(json, typeReference);
        } catch (IOException e) {
            log.error("JSON反序列化失败: typeRef={}", typeReference.getType(), e);
            return null;
        }
    }

    /**
     * 将JSON字符串反序列化为List
     *
     * <h3>使用示例</h3>
     * <pre>{@code
     * List<User> users = JsonUtils.parseArray(json, User.class);
     * }</pre>
     *
     * @param json  JSON字符串
     * @param clazz List元素类型
     * @param <T>   元素类型泛型
     * @return List对象，失败返回null
     */
    public static <T> List<T> parseArray(String json, Class<T> clazz) {
        if (json == null || json.isEmpty() || clazz == null) {
            return null;
        }
        try {
            ObjectMapper mapper = getMapper();
            return mapper.readValue(json, mapper.getTypeFactory().constructCollectionType(List.class, clazz));
        } catch (IOException e) {
            log.error("JSON反序列化List失败: elementClass={}", clazz.getName(), e);
            return null;
        }
    }

    /**
     * 将JSON字符串反序列化为Map
     *
     * @param json JSON字符串
     * @return Map对象，失败返回null
     */
    public static Map<String, Object> parseMap(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return getMapper().readValue(json, new TypeReference<Map<String, Object>>() {});
        } catch (IOException e) {
            log.error("JSON反序列化Map失败", e);
            return null;
        }
    }

    // ==================== 工具方法 ====================

    /**
     * 创建空的ObjectNode
     *
     * @return 空的ObjectNode
     */
    public static ObjectNode createObjectNode() {
        return getMapper().createObjectNode();
    }

    /**
     * 获取全局ObjectMapper实例
     * <p>
     * 优先返回Spring容器中的ObjectMapper，保证与项目配置一致
     * </p>
     *
     * @return ObjectMapper实例
     */
    public static ObjectMapper getMapperInstance() {
        return getMapper();
    }

    /**
     * 判断字符串是否为合法JSON
     *
     * @param json 字符串
     * @return true表示是合法JSON
     */
    public static boolean isJson(String json) {
        if (json == null || json.isEmpty()) {
            return false;
        }
        try {
            getMapper().readTree(json);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}

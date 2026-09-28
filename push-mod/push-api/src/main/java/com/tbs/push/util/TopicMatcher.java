package com.tbs.push.util;

/**
 * topic通配符匹配工具
 * <p>
 * 订阅/发布 topic 按 {@code .} 分段，支持两种通配符：
 * <ul>
 *     <li>{@code *} — 匹配恰好一个分段（单级通配）</li>
 *     <li>{@code #} — 匹配零个或多个分段（多级通配，约定作为尾段；
 *     若出现在非尾段，按"匹配其后全部"的宽松语义处理）</li>
 * </ul>
 * 匹配为纯函数、无状态，订阅侧与发布侧的通配符共用同一算法（双向对称）：
 * pattern 为含通配符的模式串，candidate 为待校验的具体 topic，
 * 二者角色在注册表遍历时按"哪一侧含通配符"互换即可。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public final class TopicMatcher {

    /**
     * 单级通配符（匹配恰好一个分段）
     */
    private static final String SINGLE_WILDCARD = "*";

    /**
     * 多级通配符（匹配零个或多个分段）
     */
    private static final String MULTI_WILDCARD = "#";

    /**
     * 分段分隔符
     */
    private static final String SEPARATOR = ".";

    /**
     * 私有构造，工具类禁止实例化
     */
    private TopicMatcher() {
    }

    /**
     * 判断候选topic是否匹配指定模式
     * <p>
     * 逐段比较：模式段为{@code #}则命中其后全部候选段并直接返回匹配；
     * 模式段为{@code *}则恰好消费一个候选段；字面段需与候选段相等。
     * 循环结束后要求两侧分段均耗尽，以保证{@code *}严格单段、{@code #}外其余段严格对齐。
     * </p>
     *
     * @param pattern   模式串（可含{@code *}/{@code #}通配符）
     * @param candidate 待校验的具体topic
     * @return 匹配返回true，否则false
     */
    public static boolean matches(String pattern, String candidate) {
        if (pattern == null || candidate == null) {
            return false;
        }
        String[] pSegs = pattern.split("\\" + SEPARATOR, -1);
        String[] cSegs = candidate.split("\\" + SEPARATOR, -1);
        int p = 0;
        int c = 0;
        while (p < pSegs.length && c < cSegs.length) {
            String pSeg = pSegs[p];
            if (MULTI_WILDCARD.equals(pSeg)) {
                return true;
            }
            if (SINGLE_WILDCARD.equals(pSeg)) {
                p++;
                c++;
            } else if (pSeg.equals(cSegs[c])) {
                p++;
                c++;
            } else {
                return false;
            }
        }
        return p == pSegs.length && c == cSegs.length;
    }

    /**
     * 判断topic是否含有通配符（{@code *}或{@code #}）
     *
     * @param topic 待检测topic
     * @return 含通配符返回true，否则false
     */
    public static boolean containsWildcard(String topic) {
        return topic != null && (topic.indexOf(SINGLE_WILDCARD) >= 0 || topic.indexOf(MULTI_WILDCARD) >= 0);
    }

}

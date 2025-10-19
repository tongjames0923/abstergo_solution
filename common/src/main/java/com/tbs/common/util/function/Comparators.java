package com.tbs.common.util.function;

import org.springframework.core.Ordered;

/**
 * 比较器工具类
 */
public class Comparators {
    /**
     * 按照Ordered接口的order值进行降序比较
     *
     * @param o1 第一个Ordered对象
     * @param o2 第二个Ordered对象
     * @return 当o1的order值小于o2的order值时返回正数，当o1的order值大于o2的order值时返回负数，相等时返回0
     */
    public static int desc(Ordered o1, Ordered o2) {
        if (o1 == null && o2 == null) {
            return 0;
        } else if (o1 == null) {
            return 1;
        } else if (o2 == null) {
            return -1;
        } else {
            return o2.getOrder() - o1.getOrder();
        }
    }

    /**
     * 按照Ordered接口的order值进行升序比较
     *
     * @param o1 第一个Ordered对象
     * @param o2 第二个Ordered对象
     * @return 当o1的order值小于o2的order值时返回负数，当o1的order值大于o2的order值时返回正数，相等时返回0
     */
    public static int asc(Ordered o1, Ordered o2) {
        if (o1 == null && o2 == null) {
            return 0;
        } else if (o1 == null) {
            return -1;
        } else if (o2 == null) {
            return 1;
        } else {
            return o1.getOrder() - o2.getOrder();
        }
    }
}

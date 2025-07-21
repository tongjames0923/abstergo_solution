package com.tbs.app.util;

import com.tbs.app.pojo.model.PageVO;
import com.tbs.common.function.SSupplier;

import java.util.List;

/**
 * 分页工具类
 *
 * @author tongj
 */
public class PageUtil {

    /**
     * 构建分页结果
     *
     * @param listS  数据列表
     * @param totalS 数据总数
     * @return
     */
    public static <T> PageVO<T> build(List<T> list, Long total) {
        return new PageVO<T>().setData(list).setTotal(total);
    }

    /**
     * 构建分页结果
     *
     * @param listS  数据列表
     * @param totalS 数据总数
     * @return
     */
    public static <T> PageVO<T> build(SSupplier<List<T>> listS, SSupplier<Long> totalS) {
        return build(listS.get(), totalS.get());
    }
}

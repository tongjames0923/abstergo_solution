package com.tbs.app.util;

import com.tbs.app.pojo.model.PageVO;
import com.tbs.common.delegate.IDelegateInvoker;
import com.tbs.common.function.SSupplier;
import com.tbs.common.util.framework.DelegateUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 分页工具类
 *
 * @author tongj
 */
@Slf4j
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

    /**
     * 构建分页结果
     *
     * @param listS  数据列表
     * @param totalS 数据总数
     * @return
     */
    public static <T> PageVO<T> build(Class<? extends IDelegateInvoker> type, SSupplier<List<T>> listS,
        SSupplier<Long> totalS) {
        log.debug("构建分页结果");
        PageVO<T> vo = new PageVO<>();
        DelegateUtil.getInstance().delegateWithTarget(vo).build(type).takeIn(PageVO::setData, listS)
            .takeIn(PageVO::setTotal, totalS).executeAll().forEach(flag -> {
                flag.waitUntilFinished().expceptionThenThrow().get();
            });
        log.debug("构建分页结果完成");
        return vo;
    }
}

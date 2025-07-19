package com.tbs.app.pojo.model;

import com.alibaba.fastjson2.JSON;
import lombok.Getter;

import java.io.Serializable;
import java.util.List;

/**
 * 分页实体基类
 *
 * @author tongj
 */
@Getter
public class PageVO<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 总数
     */
    private Long total;
    /**
     * 数据
     */
    private List<T> data;

    public static <T> PageVO<T> build(List<T> list, Long total) {
        return new PageVO<T>().setData(list).setTotal(total);
    }

    //    public static <T> PageVO<T> buildAsyncAndWait(Supplier<List<T>> listSupplier, Supplier<Long> totalSupplier) {
    //        PageVO<T> pageVO = new PageVO<>();
    //        return ThreadUtil.getInstance().<PageVO<T>>createAsyncCall(pageVO).run(PageVO::setTotal, totalSupplier)
    //            .run(PageVO::setData, listSupplier).buildAndExecute();
    //    }

    public PageVO<T> setTotal(Long total) {
        this.total = total;
        return this;
    }

    public PageVO<T> setData(List<T> data) {
        this.data = data;
        return this;
    }

    @Override
    public String toString() {
        return "PageVO{" + "total=" + total + ", data=" + JSON.toJSONString(data) + '}';
    }
}

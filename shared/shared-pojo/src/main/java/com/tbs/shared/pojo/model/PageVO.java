package com.tbs.shared.pojo.model;

import com.tbs.common.util.base.JsonUtils;
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
    private Integer total;
    /**
     * 数据
     */
    private List<T> data;

    //    public static <T> PageVO<T> buildAsyncAndWait(Supplier<List<T>> listSupplier, Supplier<Long> totalSupplier) {
    //        PageVO<T> pageVO = new PageVO<>();
    //        return ThreadUtil.getInstance().<PageVO<T>>createAsyncCall(pageVO).run(PageVO::setTotal, totalSupplier)
    //            .run(PageVO::setData, listSupplier).buildAndExecute();
    //    }

    public PageVO<T> setTotal(Integer total) {
        this.total = total;
        return this;
    }

    public PageVO<T> setData(List<T> data) {
        this.data = data;
        return this;
    }

    @Override
    public String toString() {
        return "PageVO{" + "total=" + total + ", data=" + JsonUtils.toJSONString(data) + '}';
    }
}

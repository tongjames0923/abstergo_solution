package com.tbs.app.service.getter;

import com.tbs.common.constant.DataHolderBeanNameConstant;
import com.tbs.common.data.IDataHolder;
import com.tbs.common.data.IDataHolderManager;
import com.tbs.script.api.expressible.vo.ParamType;
import com.tbs.script.api.expressible.vo.ParamValue;
import com.tbs.script.api.expressible.vo.ResultValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 数据持有者变量取值器。
 *
 * <p>按持有者名称与 key 读取本地或 Redis 数据持有者中的运行时变量，作为状态类事件条件来源。
 * 本取值器只读不写；持有者不存在、变量未命中或取值异常时返回空字符串，不中断脚本执行。</p>
 *
 * @author abstergo
 */
@Component
public class DataHolderGetter extends AbstractStringValueGetter {

    /**
     * 数据持有者管理器
     */
    @Autowired(required = false)
    private IDataHolderManager dataHolderManager;

    /**
     * 获取取值器名称。
     *
     * @return 国际化后的取值器名称
     */
    @Override
    public String getGetterName() {
        return text("value.getter.data-holder.name");
    }

    /**
     * 获取取值器 ID。
     *
     * @return 取值器 ID
     */
    @Override
    public String getGetterId() {
        return "DataHolderGetter";
    }

    /**
     * 获取取值器描述。
     *
     * @return 国际化后的取值器描述
     */
    @Override
    public String getDescription() {
        return text("value.getter.data-holder.description");
    }

    /**
     * 声明参数定义：可选持有者名称与必填变量 key。
     *
     * @return 参数定义列表
     */
    @Override
    public List<ParamType> requestParamTypes() {
        return List.of(ParamType.builder().type("string").name(text("value.getter.data-holder.param.holder.name"))
            .description(text("value.getter.data-holder.param.holder.description")).sort(1).build(),
            ParamType.builder().type("string").name(text("value.getter.data-holder.param.key.name"))
                .description(text("value.getter.data-holder.param.key.description")).sort(2).build());
    }

    /**
     * 读取数据持有者变量。
     *
     * @param paramValues 参数值集合，首个参数为持有者名称（可空），第二个参数为变量 key
     * @return 字符串结果；未命中或异常时返回空字符串
     */
    @Override
    public ResultValue getValue(Collection<ParamValue> paramValues) {
        String holderName = paramString(paramValues, 0);
        String key = paramString(paramValues, 1);
        if (key == null || key.isBlank()) {
            return emptyResult();
        }
        if (dataHolderManager == null) {
            return emptyResult();
        }
        try {
            String name = holderName == null || holderName.isBlank() ? DataHolderBeanNameConstant.DEFAULT_DATA_HOLDER
                : holderName;
            Optional<IDataHolder> holder = dataHolderManager.getDataHolder(name);
            if (holder.isEmpty()) {
                return emptyResult();
            }
            Optional<Object> value = holder.get().get(key);
            return stringResult(value.map(String::valueOf).orElse(""));
        } catch (Exception e) {
            return emptyResult();
        }
    }
}

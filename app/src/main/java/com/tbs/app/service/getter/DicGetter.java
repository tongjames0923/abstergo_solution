package com.tbs.app.service.getter;

import com.tbs.common.provider.annotation.RoutingRef;
import com.tbs.dic.model.DicItemModel;
import com.tbs.dic.service.IDataDicService;
import com.tbs.script.api.expressible.vo.*;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * 数据字典取值器。
 *
 * <p>按字典编码与字典项标签读取启用状态的字典值，字典项未命中或取值异常时返回调用方给定的默认值，
 * 未提供默认值则为空字符串，不中断脚本执行。</p>
 *
 * @author abstergo
 */
@Component
public class DicGetter extends AbstractDicValueGetter {

    /**
     * 文案 key 前缀
     */
    private static final String I18N_PREFIX = "value.getter.dic.";

    /**
     * 数据字典服务，本地优先、Dubbo 兜底
     */
    @RoutingRef
    private IDataDicService dataDicService;

    /**
     * 获取取值器名称。
     *
     * @return 国际化后的取值器名称
     */
    @Override
    public String getGetterName() {
        return text(I18N_PREFIX + "name");
    }

    /**
     * 获取取值器 ID。
     *
     * @return 取值器 ID
     */
    @Override
    public String getGetterId() {
        return "DicGetter";
    }

    /**
     * 获取取值器描述。
     *
     * @return 国际化后的取值器描述
     */
    @Override
    public String getDescription() {
        return text(I18N_PREFIX + "description");
    }

    /**
     * 声明字典结果类型。
     *
     * @return 字典结果类型元信息
     */
    @Override
    public ResultType responseResultType() {
        return ResultType.builder().type("string").name(text(I18N_PREFIX + "result.name"))
            .description(text(I18N_PREFIX + "result.description")).sort(1).build();
    }

    /**
     * 声明参数定义：字典编码、字典项标签与默认值。
     *
     * @return 参数定义列表
     */
    @Override
    public List<ParamType> requestParamTypes() {
        return List.of(
            ParamType.builder().type("string").name(text(I18N_PREFIX + "param.code.name"))
                .description(text(I18N_PREFIX + "param.code.description")).required(true).sort(1)
                .ui(ParamUiMeta.builder().inputType("text").maxLength(128)
                    .placeholder(text(I18N_PREFIX + "param.code.placeholder")).build()).build(),
            ParamType.builder().type("string").name(text(I18N_PREFIX + "param.label.name"))
                .description(text(I18N_PREFIX + "param.label.description")).required(true).sort(2)
                .ui(ParamUiMeta.builder().inputType("text").maxLength(128)
                    .placeholder(text(I18N_PREFIX + "param.label.placeholder")).build()).build(),
            ParamType.builder().type("string").name(text(I18N_PREFIX + "param.fallback.name"))
                .description(text(I18N_PREFIX + "param.fallback.description")).sort(3)
                .ui(ParamUiMeta.builder().inputType("text")
                    .placeholder(text(I18N_PREFIX + "param.fallback.placeholder")).build()).build());
    }

    /**
     * 按字典编码与标签读取字典值。
     *
     * @param paramValues 参数值集合，依次为字典编码、字典项标签、默认值
     * @return 字典值；未命中时返回默认值或空字符串
     */
    @Override
    public ResultValue getValue(Collection<ParamValue> paramValues) {
        String code = paramString(paramValues, 0);
        String label = paramString(paramValues, 1);
        String fallback = paramString(paramValues, 2);
        Optional<DicItemModel> item = findItem(dataDicService, code, label);
        String value = item.map(DicItemModel::getValue).orElse(fallback);
        return stringResult(value == null ? "" : value);
    }
}

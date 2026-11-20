package com.tbs.app.service.getter;

import com.tbs.common.provider.annotation.RoutingRef;
import com.tbs.dic.service.IDataDicService;
import com.tbs.script.api.expressible.vo.ParamType;
import com.tbs.script.api.expressible.vo.ParamUiMeta;
import com.tbs.script.api.expressible.vo.ParamValue;
import com.tbs.script.api.expressible.vo.ResultValue;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * 字典项存在性取值器。
 *
 * <p>判断指定字典中是否存在启用状态的目标字典项，作为配置类布尔条件来源。
 * 字典编码或标签为空、取值异常时返回 false，不中断脚本执行。</p>
 *
 * @author abstergo
 */
@Component
public class DicExistsGetter extends AbstractBooleanValueGetter {

    /**
     * 文案 key 前缀
     */
    private static final String I18N_PREFIX = "value.getter.dic-exists.";

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
        return "DicExistsGetter";
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
     * 声明参数定义：字典编码与字典项标签。
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
                    .placeholder(text(I18N_PREFIX + "param.label.placeholder")).build()).build());
    }

    /**
     * 判断字典项是否存在且启用。
     *
     * @param paramValues 参数值集合，依次为字典编码、字典项标签
     * @return 存在且启用返回 true，否则返回 false
     */
    @Override
    public ResultValue getValue(Collection<ParamValue> paramValues) {
        return booleanResult(AbstractDicValueGetter.findItem(dataDicService,
            paramString(paramValues, 0), paramString(paramValues, 1)).isPresent());
    }
}

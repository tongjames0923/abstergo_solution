package com.tbs.app.service.getter;

import com.tbs.dic.enums.DataDicFlagEnum;
import com.tbs.dic.model.DicItemModel;
import com.tbs.dic.model.DicModel;
import com.tbs.dic.service.IDataDicService;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 数据字典取值器公共基类。
 *
 * <p>只承担字典项查找：按字典编码加载字典，再按字典项 label 匹配启用状态的项；
 * 字典服务由各子类按自身装配方式注入，查找异常统一视为未命中。</p>
 *
 * @author abstergo
 */
abstract class AbstractDicValueGetter extends AbstractStringValueGetter {

    /**
     * 按字典编码与字典项标签查找启用状态的字典项。
     *
     * @param service 数据字典服务
     * @param code    字典编码
     * @param label   字典项标签
     * @return 字典项；未命中、已停用或异常时为空
     */
    protected static Optional<DicItemModel> findItem(IDataDicService service, String code, String label) {
        if (service == null || code == null || code.isBlank() || label == null || label.isBlank()) {
            return Optional.empty();
        }
        try {
            List<DicModel> models = service.getDicByCode(code.trim(), true);
            if (models == null) {
                return Optional.empty();
            }
            Integer enabled = DataDicFlagEnum.ENABLE.getCode();
            return models.stream().filter(Objects::nonNull)
                .flatMap(model -> model.getItems() == null ? Stream.empty() : model.getItems().stream())
                .filter(Objects::nonNull)
                .filter(item -> label.equals(item.getLabel()))
                .filter(item -> Objects.equals(item.getFlag(), enabled))
                .findFirst();
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}

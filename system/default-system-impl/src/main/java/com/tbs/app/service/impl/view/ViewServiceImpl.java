package com.tbs.app.service.impl.view;

import com.tbs.app.service.view.IViewService;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.util.CollUtil;
import com.tbs.common.util.SpringUtil;
import com.tbs.shared.constant.CommonConstant;
import com.tbs.shared.constant.ViewConstants;
import com.tbs.shared.interfaces.view.IPropertyValueTransformer;
import com.tbs.shared.mapper.view.*;
import com.tbs.shared.pojo.entity.view.*;
import com.tbs.shared.pojo.model.view.FormConfig;
import com.tbs.shared.pojo.model.view.FormField;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.Cacheable;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ViewServiceImpl implements IViewService {

    @Resource
    private FormConfigMapper formConfigMapper;

    @Resource
    private FieldConfigMapper fieldConfigMapper;

    @Resource
    private PropertyValueMapper propertyValueMapper;

    @Resource
    private PropertyRelationMapper propertyRelationMapper;

    @Resource
    private FormFieldRelationMapper formFieldRelationMapper;

    @Cacheable(cacheNames = "view_form_cache", key = "#id")
    @Override
    public FormConfig getForm(Long id) {
        AssertUtil.notNull(id, "表单ID不能为空");
        DynamicFormConfigrationEntity dynamicFormConfigrationEntity = formConfigMapper.selectByPrimaryKey(id);
        AssertUtil.notNull(dynamicFormConfigrationEntity, "表单配置不存在");
        FormConfig formConfig = new FormConfig();
        IPropertyValueTransformer transformer = SpringUtil.getBean(IPropertyValueTransformer.class);
        formConfig.setSpan(dynamicFormConfigrationEntity.getSpan());
        formConfig.setExpandable(CommonConstant.FLAG_ON.equals(dynamicFormConfigrationEntity.getExpandable()));
        formConfig.setExpanded(CommonConstant.FLAG_ON.equals(dynamicFormConfigrationEntity.getExpanded()));
        formConfig.setGutter(dynamicFormConfigrationEntity.getGutter());
        formConfig.setLabelWidth(dynamicFormConfigrationEntity.getLabelWidth());
        PropertyRelationEntity formSelection = new PropertyRelationEntity();
        formSelection.setRelationType(ViewConstants.PROPERTY_FORM_TYPE_RELATION);
        formSelection.setRelationId(dynamicFormConfigrationEntity.getId());
        var formPropsRelations = propertyRelationMapper.select(formSelection);
        if (CollUtil.isNotEmpty(formPropsRelations)) {
            var propertyValueEntities = propertyValueMapper.selectByIdList(
                formPropsRelations.stream().map(PropertyRelationEntity::getPropertyId).collect(Collectors.toList()));
            formConfig.setProps(
                propertyValueEntities.stream().collect(Collectors.toMap(PropertyValueEntity::getName, (v) -> {
                    try {
                        return transformer.reverseTransformStr(v.getValue(), Class.forName(v.getValueClass()));
                    } catch (ClassNotFoundException e) {
                        AssertUtil.throwError(() -> new FormattedRuntimeException("属性值转换失败: {}", v.getValue()));
                    }
                    return null;
                })));
        }

        FormFieldRelationEntity formFieldSelection = new FormFieldRelationEntity();
        formFieldSelection.setFormId(dynamicFormConfigrationEntity.getId());
        var fieldRelations = formFieldRelationMapper.select(formFieldSelection);
        if (CollUtil.isNotEmpty(fieldRelations)) {
            var fieldConfigrationEntities = fieldConfigMapper.selectByIdList(
                fieldRelations.stream().map(FormFieldRelationEntity::getFieldId).collect(Collectors.toList()));

            if (CollUtil.isNotEmpty(fieldConfigrationEntities)) {
                formConfig.setFields(fieldConfigrationEntities.stream().map((v) -> {
                    FormField formField = new FormField();
                    formField.setLabel(v.getLabel());
                    formField.setType(v.getType());
                    formField.setProp(v.getProp());
                    PropertyRelationEntity fieldSelection = new PropertyRelationEntity();
                    fieldSelection.setRelationType(ViewConstants.PROPERTY_FIELD_TYPE_RELATION);
                    fieldSelection.setRelationId(v.getId());
                    var fieldPropsRelations = propertyRelationMapper.select(fieldSelection);
                    if (CollUtil.isNotEmpty(fieldPropsRelations)) {
                        var propertyValueEntities = propertyValueMapper.selectByIdList(
                            fieldPropsRelations.stream().map(PropertyRelationEntity::getPropertyId)
                                .collect(Collectors.toList()));
                        formField.setProps(propertyValueEntities.stream()
                            .collect(Collectors.toMap(PropertyValueEntity::getName, (v1) -> {
                                try {
                                    return transformer.reverseTransformStr(v1.getValue(),
                                        Class.forName(v1.getValueClass()));
                                } catch (ClassNotFoundException e) {
                                    AssertUtil.throwError(
                                        () -> new FormattedRuntimeException("属性值转换失败: {}", v1.getValue()));
                                }
                                return null;
                            })));
                    }
                    return formField;
                }).collect(Collectors.toList()));
            }
        }
        return formConfig;
    }

    @Transactional(rollbackOn = RuntimeException.class)
    @Override
    public DynamicFormConfigrationEntity saveForm(@NotNull FormConfig formConfig) {
        AssertUtil.notNull(formConfig, "表单配置不能为空");
        DynamicFormConfigrationEntity dynamicFormConfigrationEntity = createDynamicFormEntity(formConfig);
        formConfigMapper.insert(dynamicFormConfigrationEntity);
        IPropertyValueTransformer transformer = SpringUtil.getBean(IPropertyValueTransformer.class);
        List<PropertyRelationEntity> relationEntities = new LinkedList<>();
        if (CollUtil.isNotEmpty(formConfig.getProps())) {
            List<PropertyValueEntity> propertyValueEntities = formConfig.getProps().entrySet().stream().map(entry -> {
                PropertyValueEntity propertyValueEntity = new PropertyValueEntity();
                propertyValueEntity.setName(entry.getKey());
                propertyValueEntity.setValue(transformer.transformStr(entry.getValue()));
                propertyValueEntity.setValueClass(entry.getValue().getClass().getName());
                propertyValueEntity.setTransformerType(transformer.getClass().getName());
                return propertyValueEntity;
            }).collect(Collectors.toList());
            propertyValueMapper.insertList(propertyValueEntities);
            relationEntities.addAll(propertyValueEntities.stream().map(entity -> {
                PropertyRelationEntity relationEntity = new PropertyRelationEntity();
                relationEntity.setPropertyId(entity.getId());
                relationEntity.setRelationType(ViewConstants.PROPERTY_FORM_TYPE_RELATION);
                relationEntity.setRelationId(dynamicFormConfigrationEntity.getId());
                return relationEntity;
            }).collect(Collectors.toList()));
        }
        if (CollUtil.isNotEmpty(formConfig.getFields())) {
            List<FormFieldConfigrationEntity> fieldConfigrationEntities = formConfig.getFields().stream().map(field -> {
                FormFieldConfigrationEntity fieldConfigrationEntity = new FormFieldConfigrationEntity();
                fieldConfigrationEntity.setType(field.getType());
                fieldConfigrationEntity.setProp(field.getProp());
                fieldConfigrationEntity.setLabel(field.getLabel());
                return fieldConfigrationEntity;
            }).collect(Collectors.toList());
            fieldConfigMapper.insertList(fieldConfigrationEntities);
            List<FormFieldRelationEntity> formFieldRelations = fieldConfigrationEntities.stream().map(entity -> {
                FormFieldRelationEntity relationEntity = new FormFieldRelationEntity();
                relationEntity.setFormId(dynamicFormConfigrationEntity.getId());
                relationEntity.setFieldId(entity.getId());
                return relationEntity;
            }).collect(Collectors.toList());
            formFieldRelationMapper.insertList(formFieldRelations);

            // 修复字段属性关联问题
            Map<Long, FormField> fieldMap = formConfig.getFields().stream()
                .collect(Collectors.toMap(field -> (long)formConfig.getFields().indexOf(field), field -> field));

            for (int i = 0; i < fieldConfigrationEntities.size(); i++) {
                FormField field = fieldMap.get((long)i);
                FormFieldConfigrationEntity fieldConfigrationEntity = fieldConfigrationEntities.get(i);

                if (CollUtil.isEmpty(field.getProps())) {
                    continue;
                }
                var props = field.getProps().entrySet().stream().map(prop -> {
                    PropertyValueEntity propertyValueEntity = new PropertyValueEntity();
                    propertyValueEntity.setName(prop.getKey());
                    propertyValueEntity.setValue(transformer.transformStr(prop.getValue()));
                    propertyValueEntity.setValueClass(prop.getValue().getClass().getName());
                    propertyValueEntity.setTransformerType(transformer.getClass().getName());
                    return propertyValueEntity;
                }).collect(Collectors.toList());
                propertyValueMapper.insertList(props);
                relationEntities.addAll(props.stream().map(entity -> {
                    PropertyRelationEntity relationEntity = new PropertyRelationEntity();
                    relationEntity.setPropertyId(entity.getId());
                    relationEntity.setRelationType(ViewConstants.PROPERTY_FIELD_TYPE_RELATION);
                    relationEntity.setRelationId(fieldConfigrationEntity.getId());
                    return relationEntity;
                }).collect(Collectors.toList()));
            }
        }
        propertyRelationMapper.insertList(relationEntities);
        return dynamicFormConfigrationEntity;
    }

    private static @NotNull DynamicFormConfigrationEntity createDynamicFormEntity(@NotNull FormConfig formConfig) {
        DynamicFormConfigrationEntity dynamicFormConfigrationEntity = new DynamicFormConfigrationEntity();
        dynamicFormConfigrationEntity.setExpanded(
            formConfig.getExpanded() ? CommonConstant.FLAG_ON : CommonConstant.FLAG_OFF);
        dynamicFormConfigrationEntity.setExpandable(
            formConfig.getExpandable() ? CommonConstant.FLAG_ON : CommonConstant.FLAG_OFF);
        dynamicFormConfigrationEntity.setGutter(formConfig.getGutter());
        dynamicFormConfigrationEntity.setLabelWidth(formConfig.getLabelWidth());
        dynamicFormConfigrationEntity.setSpan(formConfig.getSpan());
        return dynamicFormConfigrationEntity;
    }
}

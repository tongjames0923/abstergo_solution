package com.tbs.app.service.impl.view;

import com.tbs.app.service.view.IViewService;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.shared.constant.CommonConstant;
import com.tbs.shared.enums.DynamicFormConfigFlagEnum;
import com.tbs.shared.mapper.view.*;
import com.tbs.shared.pojo.entity.view.*;
import com.tbs.shared.pojo.model.view.FormConfig;
import com.tbs.shared.pojo.model.view.FormField;
import com.tbs.sql.utils.QueryUtil;
import com.tbs.view.IPropertyValueTransformer;
import com.tbs.view.constant.ViewConstants;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
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
        DynamicFormConfigrationEntity dynamicFormConfigrationEntity =
            QueryUtil.getEntityQueryWrapper(DynamicFormConfigrationEntity.class).query().where()
                .equal(DynamicFormConfigrationEntity::getId, id).pour().fetchOne();
        AssertUtil.notNull(dynamicFormConfigrationEntity, "表单配置不存在");
        FormConfig formConfig = new FormConfig();
        IPropertyValueTransformer transformer = SpringUtil.getBean(IPropertyValueTransformer.class);
        formConfig.setSpan(dynamicFormConfigrationEntity.getSpan());
        formConfig.setTitle(dynamicFormConfigrationEntity.getTitle());
        formConfig.setId(dynamicFormConfigrationEntity.getId());
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

    @Override
    @Transactional(rollbackOn = RuntimeException.class)
    @CacheEvict(cacheNames = "view_form_cache", key = "#formConfigId")
    public DynamicFormConfigrationEntity updateForm(Long formConfigId, @NotNull FormConfig formConfig) {
        AssertUtil.notNull(formConfigId, "表单配置ID不能为空");
        AssertUtil.notNull(formConfig, "表单配置不能为空");

        // 获取现有的表单配置实体
        DynamicFormConfigrationEntity existingEntity =
            QueryUtil.getEntityQueryWrapper(DynamicFormConfigrationEntity.class).query().where()
                .equal(DynamicFormConfigrationEntity::getId, formConfigId).pour().fetchOne();

        AssertUtil.notNull(existingEntity, "表单配置不存在");

        // 更新基本属性
        existingEntity.setTitle(formConfig.getTitle());
        existingEntity.setExpanded(formConfig.getExpanded() ? CommonConstant.FLAG_ON : CommonConstant.FLAG_OFF);
        existingEntity.setExpandable(formConfig.getExpandable() ? CommonConstant.FLAG_ON : CommonConstant.FLAG_OFF);
        existingEntity.setGutter(formConfig.getGutter());
        existingEntity.setLabelWidth(formConfig.getLabelWidth());
        existingEntity.setSpan(formConfig.getSpan());

        // 更新表单配置实体
        formConfigMapper.updateByPrimaryKey(existingEntity);

        // 删除现有的关联关系和属性值
        // 删除表单属性关联
        PropertyRelationEntity formRelationQuery = new PropertyRelationEntity();
        formRelationQuery.setRelationType(ViewConstants.PROPERTY_FORM_TYPE_RELATION);
        formRelationQuery.setRelationId(formConfigId);
        propertyRelationMapper.delete(formRelationQuery);

        // 删除字段关联
        FormFieldRelationEntity fieldRelationQuery = new FormFieldRelationEntity();
        fieldRelationQuery.setFormId(formConfigId);
        formFieldRelationMapper.delete(fieldRelationQuery);

        // 删除字段配置
        // 先查询现有的字段配置ID
        List<FormFieldRelationEntity> existingFieldRelations = formFieldRelationMapper.select(fieldRelationQuery);
        if (CollUtil.isNotEmpty(existingFieldRelations)) {
            List<Long> fieldIds =
                existingFieldRelations.stream().map(FormFieldRelationEntity::getFieldId).collect(Collectors.toList());

            // 删除字段属性关联
            if (CollUtil.isNotEmpty(fieldIds)) {
                PropertyRelationEntity fieldPropertyQuery = new PropertyRelationEntity();
                fieldPropertyQuery.setRelationType(ViewConstants.PROPERTY_FIELD_TYPE_RELATION);
                // 这里需要逐个删除，因为可能关联到不同的字段
                fieldIds.forEach(fieldId -> {
                    fieldPropertyQuery.setRelationId(fieldId);
                    propertyRelationMapper.delete(fieldPropertyQuery);
                });

                // 删除字段配置
                fieldConfigMapper.deleteByIdList(fieldIds);
            }
        }

        // 重新保存配置，复用saveForm的逻辑
        IPropertyValueTransformer transformer = SpringUtil.getBean(IPropertyValueTransformer.class);
        List<PropertyRelationEntity> relationEntities = new LinkedList<>();

        // 保存表单属性
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
                relationEntity.setRelationId(existingEntity.getId());
                return relationEntity;
            }).collect(Collectors.toList()));
        }

        // 保存字段配置
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
                relationEntity.setFormId(existingEntity.getId());
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

        // 保存新的关联关系
        if (CollUtil.isNotEmpty(relationEntities)) {
            propertyRelationMapper.insertList(relationEntities);
        }

        return existingEntity;
    }

    @Override
    @CacheEvict(cacheNames = "view_form_cache", key = "#formConfigId")
    public DynamicFormConfigrationEntity deleteForm(Long formConfigId) {
        AssertUtil.notNull(formConfigId, "表单配置ID不能为空");
        DynamicFormConfigrationEntity form =
            QueryUtil.getEntityQueryWrapper(DynamicFormConfigrationEntity.class).query().where()
                .equal(DynamicFormConfigrationEntity::getId, formConfigId).pour().fetchOne();
        if (form == null) {
            return null;
        }
        form.setFlag(DynamicFormConfigFlagEnum.DELETED.getCode());
        formConfigMapper.updateByPrimaryKey(form);
        return null;
    }

    @Override
    public List<FormConfig> listForms(int pageNum, int pageSize) {
        if (pageNum < 1) {
            pageNum = 1;
        }
        if (pageSize < 1) {
            pageSize = 1;
        }
        int finalPageNum = pageNum;
        int finalPageSize = pageSize;
        var query = QueryUtil.getEntityQueryWrapper(DynamicFormConfigrationEntity.class).query();
        query.getPageLimitSupport().ifPresentOrElse(pageLimit -> {
            pageLimit.pageOf((long)finalPageNum, (long)finalPageSize);
        }, () -> {
            log.warn("数据库未支持分页，将全局查询");
        });
        return query.fetch().stream().map((e) -> {
            return SpringUtil.getBean(IViewService.class).getForm(e.getId());
        }).collect(Collectors.toList());
    }

    private static @NotNull DynamicFormConfigrationEntity createDynamicFormEntity(@NotNull FormConfig formConfig) {
        DynamicFormConfigrationEntity dynamicFormConfigrationEntity = new DynamicFormConfigrationEntity();
        dynamicFormConfigrationEntity.setTitle(formConfig.getTitle());
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

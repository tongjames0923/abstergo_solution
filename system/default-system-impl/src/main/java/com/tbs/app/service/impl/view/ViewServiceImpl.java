package com.tbs.app.service.impl.view;

import com.tbs.app.service.view.IViewService;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.common.i18n.I18nUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.common.util.base.StrUtil;
import com.tbs.common.util.framework.SpringUtil;
import com.tbs.shared.constant.CommonConstant;
import com.tbs.shared.enums.DynamicFormConfigFlagEnum;
import com.tbs.shared.mapper.view.*;
import com.tbs.shared.pojo.entity.view.*;
import com.tbs.shared.pojo.model.view.FormConfig;
import com.tbs.shared.pojo.model.view.FormField;
import com.tbs.sql.service.QueryEngine;
import com.tbs.view.IPropertyValueTransformer;
import com.tbs.view.constant.ViewConstants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.tbs.app.service.impl.CacheNamesConstants.VIEW_FORM_CACHE;

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

    // ==================== 公共方法 ====================

    /**
     * 将属性值实体列表转换为 Map（name -> 反序列化后的值）
     * <p>
     * 无存储值（prop_value 为空）、无目标类型或转换结果为 null 的属性直接跳过，不下发给前端，
     * 让组件保留自身默认值。另不能使用 Collectors.toMap：其内部 HashMap.merge 不允许 null 值。
     * </p>
     */
    private Map<String, Object> convertPropertyValuesToMap(List<PropertyValueEntity> propertyValueEntities,
        IPropertyValueTransformer transformer) {
        Map<String, Object> result = new LinkedHashMap<>();
        for (PropertyValueEntity propertyValue : propertyValueEntities) {
            if (StrUtil.isBlank(propertyValue.getValue()) || StrUtil.isBlank(propertyValue.getValueClass())) {
                continue;
            }
            try {
                Object value = transformer.reverseTransformStr(propertyValue.getValue(),
                    Class.forName(propertyValue.getValueClass()));
                if (value != null) {
                    result.put(propertyValue.getName(), value);
                }
            } catch (ClassNotFoundException e) {
                AssertUtil.throwError(() -> new RuntimeException(
                    I18nUtil.get("system.property.value.convert.failed", propertyValue.getValue()), e));
            }
        }
        return result;
    }

    /**
     * 根据关联关系查询并转换属性值为 Map
     */
    private Map<String, Object> loadPropertiesByRelation(Long relationId, Integer relationType,
        IPropertyValueTransformer transformer) {
        PropertyRelationEntity query = new PropertyRelationEntity();
        query.setRelationType(relationType);
        query.setRelationId(relationId);
        var relations = propertyRelationMapper.select(query);
        if (CollUtil.isEmpty(relations)) {
            return null;
        }
        var propertyValueEntities = propertyValueMapper.selectByIdList(
            relations.stream().map(PropertyRelationEntity::getPropertyId).collect(Collectors.toList()));
        if (CollUtil.isEmpty(propertyValueEntities)) {
            return null;
        }
        return convertPropertyValuesToMap(propertyValueEntities, transformer);
    }

    /**
     * 构建单个 FormField
     */
    private FormField buildFormField(FormFieldConfigrationEntity fieldEntity, IPropertyValueTransformer transformer) {
        FormField formField = new FormField();
        formField.setLabel(fieldEntity.getLabel());
        formField.setType(fieldEntity.getType());
        formField.setProp(fieldEntity.getProp());
        formField.setProps(
            loadPropertiesByRelation(fieldEntity.getId(), ViewConstants.PROPERTY_FIELD_TYPE_RELATION, transformer));
        return formField;
    }

    /**
     * 保存属性值并创建关联关系
     */
    private List<PropertyRelationEntity> savePropertiesAndRelations(Map<String, Object> props,
        IPropertyValueTransformer transformer, Long relationId, Integer relationType) {
        if (CollUtil.isEmpty(props)) {
            return new LinkedList<>();
        }
        List<PropertyValueEntity> propertyValueEntities = props.entrySet().stream().map(entry -> {
            Object value = entry.getValue();
            PropertyValueEntity entity = new PropertyValueEntity();
            entity.setName(entry.getKey());
            entity.setValue(value == null ? null : transformer.transformStr(value));
            entity.setValueClass(value == null ? null : value.getClass().getName());
            entity.setTransformerType(transformer.getClass().getName());
            return entity;
        }).collect(Collectors.toList());
        propertyValueMapper.insertList(propertyValueEntities);
        return propertyValueEntities.stream().map(entity -> {
            PropertyRelationEntity relationEntity = new PropertyRelationEntity();
            relationEntity.setPropertyId(entity.getId());
            relationEntity.setRelationType(relationType);
            relationEntity.setRelationId(relationId);
            return relationEntity;
        }).collect(Collectors.toList());
    }

    /**
     * 保存表单字段配置及属性
     */
    private void saveFormFields(FormConfig formConfig, DynamicFormConfigrationEntity formEntity,
        IPropertyValueTransformer transformer, List<PropertyRelationEntity> allRelations) {
        if (CollUtil.isEmpty(formConfig.getFields())) {
            return;
        }
        // 保存字段配置
        List<FormFieldConfigrationEntity> fieldEntities = formConfig.getFields().stream().map(field -> {
            FormFieldConfigrationEntity entity = new FormFieldConfigrationEntity();
            entity.setType(field.getType());
            entity.setProp(field.getProp());
            entity.setLabel(field.getLabel());
            return entity;
        }).collect(Collectors.toList());
        fieldConfigMapper.insertList(fieldEntities);

        // 保存字段关联
        List<FormFieldRelationEntity> formFieldRelations = fieldEntities.stream().map(entity -> {
            FormFieldRelationEntity relationEntity = new FormFieldRelationEntity();
            relationEntity.setFormId(formEntity.getId());
            relationEntity.setFieldId(entity.getId());
            return relationEntity;
        }).collect(Collectors.toList());
        formFieldRelationMapper.insertList(formFieldRelations);

        // 保存字段属性
        for (int i = 0; i < fieldEntities.size(); i++) {
            FormField field = formConfig.getFields().get(i);
            if (CollUtil.isNotEmpty(field.getProps())) {
                allRelations.addAll(
                    savePropertiesAndRelations(field.getProps(), transformer, fieldEntities.get(i).getId(),
                        ViewConstants.PROPERTY_FIELD_TYPE_RELATION));
            }
        }
    }

    @Cacheable(cacheNames = VIEW_FORM_CACHE, key = "#id")
    @Override
    public FormConfig getForm(Long id) {
        AssertUtil.isTrue(id == null).message("表单ID不能为空").errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        DynamicFormConfigrationEntity formEntity =
            QueryEngine.getEntityQueryWrapper(DynamicFormConfigrationEntity.class).query().where()
                .equal(DynamicFormConfigrationEntity::getId, id)
                .equal(DynamicFormConfigrationEntity::getFlag, DynamicFormConfigFlagEnum.ACTIVITED.getCode()).pour()
                .fetchOne();
        AssertUtil.isTrue(formEntity == null).message("表单配置不存在")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();

        IPropertyValueTransformer transformer = SpringUtil.getBean(IPropertyValueTransformer.class);

        // 构建基本信息
        FormConfig formConfig = new FormConfig();
        formConfig.setId(formEntity.getId());
        formConfig.setTitle(formEntity.getTitle());
        formConfig.setSpan(formEntity.getSpan());
        formConfig.setGutter(formEntity.getGutter());
        formConfig.setLabelWidth(formEntity.getLabelWidth());
        formConfig.setExpandable(CommonConstant.FLAG_ON.equals(formEntity.getExpandable()));
        formConfig.setExpanded(CommonConstant.FLAG_ON.equals(formEntity.getExpanded()));

        // 加载表单属性
        formConfig.setProps(loadPropertiesByRelation(id, ViewConstants.PROPERTY_FORM_TYPE_RELATION, transformer));

        // 加载字段配置
        FormFieldRelationEntity fieldRelationQuery = new FormFieldRelationEntity();
        fieldRelationQuery.setFormId(id);
        var fieldRelations = formFieldRelationMapper.select(fieldRelationQuery);
        if (CollUtil.isNotEmpty(fieldRelations)) {
            var fieldEntities = fieldConfigMapper.selectByIdList(
                fieldRelations.stream().map(FormFieldRelationEntity::getFieldId).collect(Collectors.toList()));
            if (CollUtil.isNotEmpty(fieldEntities)) {
                formConfig.setFields(fieldEntities.stream().map(entity -> buildFormField(entity, transformer))
                    .collect(Collectors.toList()));
            }
        }

        return formConfig;
    }

    @Transactional(rollbackFor = RuntimeException.class)
    @Override
    public DynamicFormConfigrationEntity saveForm(FormConfig formConfig) {
        AssertUtil.isTrue(formConfig == null).message("表单配置不能为空").errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .throwIt();

        // 保存表单基本信息
        DynamicFormConfigrationEntity formEntity = createDynamicFormEntity(formConfig);
        formConfigMapper.insert(formEntity);

        IPropertyValueTransformer transformer = SpringUtil.getBean(IPropertyValueTransformer.class);
        List<PropertyRelationEntity> allRelations = new LinkedList<>();

        // 保存表单属性
        allRelations.addAll(savePropertiesAndRelations(formConfig.getProps(), transformer, formEntity.getId(),
            ViewConstants.PROPERTY_FORM_TYPE_RELATION));

        // 保存字段配置及属性
        saveFormFields(formConfig, formEntity, transformer, allRelations);

        // 批量保存关联关系
        if (CollUtil.isNotEmpty(allRelations)) {
            propertyRelationMapper.insertList(allRelations);
        }

        return formEntity;
    }

    @Override
    @Transactional(rollbackFor = RuntimeException.class)
    @CacheEvict(cacheNames = VIEW_FORM_CACHE, key = "#formConfigId")
    public DynamicFormConfigrationEntity updateForm(Long formConfigId, FormConfig formConfig) {
        AssertUtil.isTrue(formConfigId == null).message("表单配置ID不能为空")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        AssertUtil.isTrue(formConfig == null).message("表单配置不能为空").errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .throwIt();

        // 获取现有的表单配置实体
        DynamicFormConfigrationEntity existingEntity =
            QueryEngine.getEntityQueryWrapper(DynamicFormConfigrationEntity.class).query().where()
                .equal(DynamicFormConfigrationEntity::getId, formConfigId).pour().fetchOne();
        AssertUtil.isTrue(existingEntity == null).message("表单配置不存在").errorLevel(ErrorLevelEnum.USER_INPUT_ERROR)
            .throwIt();

        // 更新基本属性
        existingEntity.setTitle(formConfig.getTitle());
        existingEntity.setExpanded(formConfig.getExpanded() ? CommonConstant.FLAG_ON : CommonConstant.FLAG_OFF);
        existingEntity.setExpandable(formConfig.getExpandable() ? CommonConstant.FLAG_ON : CommonConstant.FLAG_OFF);
        existingEntity.setGutter(formConfig.getGutter());
        existingEntity.setLabelWidth(formConfig.getLabelWidth());
        existingEntity.setSpan(formConfig.getSpan());
        formConfigMapper.updateByPrimaryKey(existingEntity);

        // 删除旧的关联关系
        deleteFormRelations(formConfigId);

        // 重新保存配置
        IPropertyValueTransformer transformer = SpringUtil.getBean(IPropertyValueTransformer.class);
        List<PropertyRelationEntity> allRelations = new LinkedList<>();

        // 保存表单属性
        allRelations.addAll(savePropertiesAndRelations(formConfig.getProps(), transformer, existingEntity.getId(),
            ViewConstants.PROPERTY_FORM_TYPE_RELATION));

        // 保存字段配置及属性
        saveFormFields(formConfig, existingEntity, transformer, allRelations);

        // 批量保存关联关系
        if (CollUtil.isNotEmpty(allRelations)) {
            propertyRelationMapper.insertList(allRelations);
        }

        return existingEntity;
    }

    /**
     * 删除表单的所有关联关系（属性关联、字段关联、字段配置）
     */
    private void deleteFormRelations(Long formConfigId) {
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
        List<FormFieldRelationEntity> existingFieldRelations = formFieldRelationMapper.select(fieldRelationQuery);
        if (CollUtil.isNotEmpty(existingFieldRelations)) {
            List<Long> fieldIds =
                existingFieldRelations.stream().map(FormFieldRelationEntity::getFieldId).collect(Collectors.toList());
            if (CollUtil.isNotEmpty(fieldIds)) {
                // 删除字段属性关联
                PropertyRelationEntity fieldPropertyQuery = new PropertyRelationEntity();
                fieldPropertyQuery.setRelationType(ViewConstants.PROPERTY_FIELD_TYPE_RELATION);
                fieldIds.forEach(fieldId -> {
                    fieldPropertyQuery.setRelationId(fieldId);
                    propertyRelationMapper.delete(fieldPropertyQuery);
                });
                // 删除字段配置
                fieldConfigMapper.deleteByIdList(fieldIds);
            }
        }
    }

    @Override
    @CacheEvict(cacheNames = VIEW_FORM_CACHE, key = "#formConfigId")
    public DynamicFormConfigrationEntity deleteForm(Long formConfigId) {
        AssertUtil.isTrue(formConfigId == null).message("表单配置ID不能为空")
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).throwIt();
        DynamicFormConfigrationEntity form =
            QueryEngine.getEntityQueryWrapper(DynamicFormConfigrationEntity.class).query().where()
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
        var query = QueryEngine.getEntityQueryWrapper(DynamicFormConfigrationEntity.class).query()
            .where()
            .pour();
        query.getPageLimitSupport().ifPresentOrElse(pageLimit -> {
            pageLimit.pageOf((long)finalPageNum, (long)finalPageSize);
        }, () -> {
            log.warn("数据库未支持分页，将全局查询");
        });

        List<DynamicFormConfigrationEntity> formEntities = query.fetch();
        if (CollUtil.isEmpty(formEntities)) {
            return new LinkedList<>();
        }

        return batchAssembleFormConfigs(formEntities);
    }

    // ==================== 批量查询组装方法 ====================

    /**
     * 批量组装表单配置（将 N+1 查询优化为固定 7 次查询）
     *
     * @param formEntities 表单实体列表
     * @return 表单配置列表
     */
    private List<FormConfig> batchAssembleFormConfigs(List<DynamicFormConfigrationEntity> formEntities) {
        List<Long> formIds = formEntities.stream().map(DynamicFormConfigrationEntity::getId).collect(Collectors.toList());
        IPropertyValueTransformer transformer = SpringUtil.getBean(IPropertyValueTransformer.class);

        // 批量加载所有数据（固定 6 次查询）
        Map<Long, List<Long>> formPropertyRelationMap = batchLoadPropertyRelations(formIds, ViewConstants.PROPERTY_FORM_TYPE_RELATION);
        Map<Long, PropertyValueEntity> formPropertyValueMap = batchLoadPropertyValues(formPropertyRelationMap);
        Map<Long, List<Long>> formFieldRelationMap = batchLoadFieldRelations(formIds);
        Map<Long, FormFieldConfigrationEntity> fieldConfigMap = batchLoadFieldConfigs(formFieldRelationMap);
        Map<Long, List<Long>> fieldPropertyRelationMap = batchLoadPropertyRelations(
            new ArrayList<>(fieldConfigMap.keySet()), ViewConstants.PROPERTY_FIELD_TYPE_RELATION);
        Map<Long, PropertyValueEntity> fieldPropertyValueMap = batchLoadPropertyValues(fieldPropertyRelationMap);

        // 组装结果
        return formEntities.stream()
            .map(entity -> assembleFormConfig(entity, transformer,
                formPropertyRelationMap, formPropertyValueMap,
                formFieldRelationMap, fieldConfigMap,
                fieldPropertyRelationMap, fieldPropertyValueMap))
            .collect(Collectors.toList());
    }

    /**
     * 批量加载属性关联关系
     *
     * @param relationIds  关联目标ID列表
     * @param relationType 关联类型
     * @return relationId -> propertyId列表 的映射
     */
    private Map<Long, List<Long>> batchLoadPropertyRelations(List<Long> relationIds, Integer relationType) {
        if (CollUtil.isEmpty(relationIds)) {
            return new HashMap<>();
        }
        PropertyRelationEntity query = new PropertyRelationEntity();
        query.setRelationType(relationType);
        return propertyRelationMapper.select(query).stream()
            .filter(r -> relationIds.contains(r.getRelationId()))
            .collect(Collectors.groupingBy(PropertyRelationEntity::getRelationId,
                Collectors.mapping(PropertyRelationEntity::getPropertyId, Collectors.toList())));
    }

    /**
     * 批量加载属性值
     *
     * @param propertyRelationMap relationId -> propertyId列表 的映射
     * @return propertyId -> PropertyValueEntity 的映射
     */
    private Map<Long, PropertyValueEntity> batchLoadPropertyValues(Map<Long, List<Long>> propertyRelationMap) {
        List<Long> propertyIds = propertyRelationMap.values().stream()
            .flatMap(Collection::stream).collect(Collectors.toList());
        if (CollUtil.isEmpty(propertyIds)) {
            return new HashMap<>();
        }
        Map<Long, PropertyValueEntity> result = new HashMap<>();
        propertyValueMapper.selectByIdList(propertyIds).forEach(v -> result.put(v.getId(), v));
        return result;
    }

    /**
     * 批量加载字段关联关系
     *
     * @param formIds 表单ID列表
     * @return formId -> fieldId列表 的映射
     */
    private Map<Long, List<Long>> batchLoadFieldRelations(List<Long> formIds) {
        if (CollUtil.isEmpty(formIds)) {
            return new HashMap<>();
        }
        return formFieldRelationMapper.select(new FormFieldRelationEntity()).stream()
            .filter(r -> formIds.contains(r.getFormId()))
            .collect(Collectors.groupingBy(FormFieldRelationEntity::getFormId,
                Collectors.mapping(FormFieldRelationEntity::getFieldId, Collectors.toList())));
    }

    /**
     * 批量加载字段配置
     *
     * @param fieldRelationMap formId -> fieldId列表 的映射
     * @return fieldId -> FormFieldConfigrationEntity 的映射
     */
    private Map<Long, FormFieldConfigrationEntity> batchLoadFieldConfigs(Map<Long, List<Long>> fieldRelationMap) {
        List<Long> fieldIds = fieldRelationMap.values().stream()
            .flatMap(Collection::stream).collect(Collectors.toList());
        if (CollUtil.isEmpty(fieldIds)) {
            return new HashMap<>();
        }
        Map<Long, FormFieldConfigrationEntity> result = new HashMap<>();
        fieldConfigMapper.selectByIdList(fieldIds).forEach(f -> result.put(f.getId(), f));
        return result;
    }

    /**
     * 组装单个表单配置
     */
    private FormConfig assembleFormConfig(DynamicFormConfigrationEntity formEntity,
        IPropertyValueTransformer transformer,
        Map<Long, List<Long>> formPropertyRelationMap,
        Map<Long, PropertyValueEntity> formPropertyValueMap,
        Map<Long, List<Long>> formFieldRelationMap,
        Map<Long, FormFieldConfigrationEntity> fieldConfigMap,
        Map<Long, List<Long>> fieldPropertyRelationMap,
        Map<Long, PropertyValueEntity> fieldPropertyValueMap) {

        FormConfig formConfig = new FormConfig();
        formConfig.setId(formEntity.getId());
        formConfig.setTitle(formEntity.getTitle());
        formConfig.setSpan(formEntity.getSpan());
        formConfig.setGutter(formEntity.getGutter());
        formConfig.setLabelWidth(formEntity.getLabelWidth());
        formConfig.setExpandable(CommonConstant.FLAG_ON.equals(formEntity.getExpandable()));
        formConfig.setExpanded(CommonConstant.FLAG_ON.equals(formEntity.getExpanded()));

        // 设置表单属性
        formConfig.setProps(resolveProperties(formEntity.getId(), formPropertyRelationMap,
            formPropertyValueMap, transformer));

        // 设置字段
        List<Long> fieldIds = formFieldRelationMap.getOrDefault(formEntity.getId(), Collections.emptyList());
        if (CollUtil.isNotEmpty(fieldIds)) {
            formConfig.setFields(fieldIds.stream()
                .map(fieldConfigMap::get)
                .filter(Objects::nonNull)
                .map(fieldEntity -> assembleFormField(fieldEntity, transformer,
                    fieldPropertyRelationMap, fieldPropertyValueMap))
                .collect(Collectors.toList()));
        }

        return formConfig;
    }

    /**
     * 组装单个字段配置
     */
    private FormField assembleFormField(FormFieldConfigrationEntity fieldEntity,
        IPropertyValueTransformer transformer,
        Map<Long, List<Long>> fieldPropertyRelationMap,
        Map<Long, PropertyValueEntity> fieldPropertyValueMap) {

        FormField formField = new FormField();
        formField.setLabel(fieldEntity.getLabel());
        formField.setType(fieldEntity.getType());
        formField.setProp(fieldEntity.getProp());
        formField.setProps(resolveProperties(fieldEntity.getId(), fieldPropertyRelationMap,
            fieldPropertyValueMap, transformer));
        return formField;
    }

    /**
     * 从预加载的Map中解析属性值
     *
     * @param ownerId           所有者ID（表单ID或字段ID）
     * @param relationMap       ownerId -> propertyId列表 的映射
     * @param propertyValueMap  propertyId -> PropertyValueEntity 的映射
     * @param transformer       属性值转换器
     * @return 属性Map，如果没有属性则返回null
     */
    private Map<String, Object> resolveProperties(Long ownerId,
        Map<Long, List<Long>> relationMap,
        Map<Long, PropertyValueEntity> propertyValueMap,
        IPropertyValueTransformer transformer) {

        List<Long> propertyIds = relationMap.getOrDefault(ownerId, Collections.emptyList());
        if (CollUtil.isEmpty(propertyIds)) {
            return null;
        }
        List<PropertyValueEntity> propertyValues = propertyIds.stream()
            .map(propertyValueMap::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        return CollUtil.isEmpty(propertyValues) ? null : convertPropertyValuesToMap(propertyValues, transformer);
    }

    private static  DynamicFormConfigrationEntity createDynamicFormEntity( FormConfig formConfig) {
        DynamicFormConfigrationEntity dynamicFormConfigrationEntity = new DynamicFormConfigrationEntity();
        dynamicFormConfigrationEntity.setTitle(formConfig.getTitle());
        dynamicFormConfigrationEntity.setExpanded(
            formConfig.getExpanded() ? CommonConstant.FLAG_ON : CommonConstant.FLAG_OFF);
        dynamicFormConfigrationEntity.setExpandable(
            formConfig.getExpandable() ? CommonConstant.FLAG_ON : CommonConstant.FLAG_OFF);
        dynamicFormConfigrationEntity.setGutter(formConfig.getGutter());
        dynamicFormConfigrationEntity.setLabelWidth(formConfig.getLabelWidth());
        dynamicFormConfigrationEntity.setSpan(formConfig.getSpan());
        dynamicFormConfigrationEntity.setFlag(DynamicFormConfigFlagEnum.ACTIVITED.getCode());
        return dynamicFormConfigrationEntity;
    }
}

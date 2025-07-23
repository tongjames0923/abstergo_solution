package com.tbs.app.service;

import com.tbs.app.config.SystemAppProperty;
import com.tbs.app.pojo.IIdentifiable;
import com.tbs.app.pojo.model.PageVO;
import com.tbs.app.util.PageUtil;
import com.tbs.common.delegate.IDelegateInvoker;
import com.tbs.common.delegate.impl.SimpleDelegateInvoker;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.exception.FormattedRuntimeException;
import com.tbs.common.util.CollUtil;
import com.tbs.sql.mapper.IFrameworkMapper;
import com.tbs.sql.query.IPage;
import com.tbs.sql.query.IQueryCriteria;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 全功能服务模板类 实现了基础的CRUD操作模板，提供可扩展的业务逻辑钩子方法
 *
 * @param <T> 实体类型（必须实现IBaseEntity接口）
 * @param <K> 主键类型
 * @param <M> Mapper类型
 * @author abstergo
 */
public abstract class AbstractFullFeatureServiceTemplate<T extends IIdentifiable<K>, K, M extends IFrameworkMapper<T, K>>
    extends AbstractService<T, K, M>
    implements ISearchableService<T, K>, ISaveableService<T, K>, IDeleteableService<T, K> {

    protected AbstractFullFeatureServiceTemplate<T, K, M> instance;

    public AbstractFullFeatureServiceTemplate() {
        this.instance = this;
    }

    @Resource
    private SystemAppProperty systemAppProperty;

    protected Class<? extends IDelegateInvoker> pageVoBuildType() {
        return systemAppProperty.getMainDelegateInvokerImpl();
    }

    /**
     * 删除逻辑处理
     *
     * @param entity
     */
    protected abstract void handleDelete(List<T> entity);

    /**
     * 新增逻辑处理
     *
     * @param entity
     * @return
     */
    protected abstract T insertNew(T entity);

    /**
     * 更新逻辑处理
     *
     * @param entity
     * @return
     */
    protected abstract List<T> updateBy(T entity);

    /**
     * 对比新值与旧值的区别后返回最终的值
     *
     * @param newOne
     * @param oldOne
     * @return
     */
    protected abstract T compareAndUpdateValue(T newOne, T oldOne);

    @Transactional(rollbackFor = Exception.class)
    @Override
    public T deleteById(K id) {
        T obj = this.instance.queryById((K)id);
        AssertUtil.notNull(obj, () -> new FormattedRuntimeException("待删除的ID{} 不存在", id));
        handleDelete(Arrays.asList(obj));
        return obj;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int deleteByIds(Collection<K> ids) {
        if (CollUtil.isEmpty(ids)) {
            return 0;
        }
        List<T> objs = ids.stream().map(this::queryById).filter(obj -> obj != null).collect(Collectors.toList());
        handleDelete(objs);
        return objs.size();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<T> save(T entity) {
        AssertUtil.notNull(entity, "空对象无法被保存");
        if (entity.getId() == null) {
            T r = instance.insertNew(entity);
            return List.of(r);
        } else {
            T oldEntity = instance.queryById(entity.getId());
            AssertUtil.notNull(oldEntity, "实体数据不存在，无法更新");
            T newEntity = instance.compareAndUpdateValue(entity, oldEntity);
            List<T> r = instance.updateBy(newEntity);
            return r;
        }
    }

    @Override
    public PageVO<T> queryBy(IQueryCriteria qo, IPage page) {
        return PageUtil.build(pageVoBuildType(), () -> getMapper().queryByQO(qo, page, getEntityClass()),
            () -> getMapper().countByQO(qo));
    }

    @Override
    public T queryById(K id) {
        return getMapper().selectByPrimaryKey(id);
    }
}

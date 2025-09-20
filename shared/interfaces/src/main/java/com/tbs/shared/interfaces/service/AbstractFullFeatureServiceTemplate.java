package com.tbs.shared.interfaces.service;

import com.tbs.common.delegate.IDelegateInvoker;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.CollUtil;
import com.tbs.shared.pojo.IIdentifiable;
import com.tbs.shared.pojo.model.PageVO;
import com.tbs.shared.util.PageUtil;
import com.tbs.sql.mapper.IFrameworkMapper;
import com.tbs.sql.query.IPage;
import com.tbs.sql.query.IQueryCriteria;
import org.springframework.aop.framework.AopContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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

    /**
     * 获取PageVO构建类
     *
     * @return
     */
    protected abstract Class<? extends IDelegateInvoker> pageVoBuildType();

    /**
     * 删除逻辑处理
     *
     * @param id
     */
    protected abstract T handleDelete(K id);

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
        return getInstance().handleDelete(id);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int deleteByIds(Collection<K> ids) {
        if (CollUtil.isEmpty(ids)) {
            return 0;
        }
        AtomicInteger atomicInteger = new AtomicInteger();
        ids.stream().forEach(id -> {
            getInstance().handleDelete(id);
            atomicInteger.incrementAndGet();
        });

        return atomicInteger.get();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<T> save(T entity) {
        AssertUtil.notNull(entity, "空对象无法被保存");
        if (entity.getId() == null) {
            T r = getInstance().insertNew(entity);
            return List.of(r);
        } else {
            T oldEntity = getInstance().queryById(entity.getId());
            AssertUtil.notNull(oldEntity, "实体数据不存在，无法更新");
            T newEntity = getInstance().compareAndUpdateValue(entity, oldEntity);
            List<T> r = getInstance().updateBy(newEntity);
            return r;
        }
    }

    @Override
    public PageVO<T> queryAndPage(IQueryCriteria qo, IPage page) {
        Class<T> entityClass = (Class<T>)getEntity().getClass();
        return PageUtil.build(pageVoBuildType(), () -> getMapper().queryByQO(qo, page, entityClass),
            () -> getMapper().countByQO(qo, entityClass));
    }

    @Override
    public List<T> query(IQueryCriteria qo, IPage page) {
        Class<T> entityClass = (Class<T>)getEntity().getClass();
        var r = getMapper().queryByQO(qo, page, entityClass);
        return r;
    }

    @Override
    public T queryById(K id) {
        return getMapper().selectByPrimaryKey(id);
    }

    /**
     * 获取代理对象
     *
     * @return
     */
    protected AbstractFullFeatureServiceTemplate<T, K, M> getInstance() {
        var r = (AbstractFullFeatureServiceTemplate<T, K, M>)AopContext.currentProxy();
        return r;
    }
}

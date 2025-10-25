package com.tbs.shared.interfaces.service;

import com.tbs.common.data.ValueWrapper;
import com.tbs.common.delegate.IDelegateInvoker;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.shared.pojo.IIdentifiable;
import com.tbs.shared.pojo.model.PageVO;
import com.tbs.shared.util.PageUtil;
import com.tbs.sql.mapper.IFrameworkMapper;
import com.tbs.sql.query.IPage;
import com.tbs.sql.query.IQueryCriteria;
import com.tbs.web.uitl.AuthUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
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
@Slf4j
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
        AssertUtil.notNull(id, "空Id无法被删除");
        var entity = getInstance().queryById(id);
        if (getInstance().isDeleted(entity)) {
            return null;
        }
        var value = ValueWrapper.of(entity);
        this.auditFunction(value, DELETE);
        entity = getInstance().handleDelete(id);
        return entity;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int deleteByIds(Collection<K> ids) {
        if (CollUtil.isEmpty(ids)) {
            return 0;
        }
        AtomicInteger atomicInteger = new AtomicInteger();
        ids.stream().forEach(id -> {
            getInstance().deleteById(id);
            atomicInteger.incrementAndGet();
        });

        return atomicInteger.get();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<T> save(T entity) {
        AssertUtil.notNull(entity, "空对象无法被保存");
        var r = ValueWrapper.of(entity);
        if (entity.getId() == null) {
            auditFunction(r, CREATE);
            if (r.get().isPresent()) {
                r.set(getInstance().insertNew(r.get().get()));
            } else {
                log.warn("实体数据不存在，无法保存");
            }
            if (r.get().isEmpty()) {
                log.warn("实体数据不存在，无法保存");
                return Collections.EMPTY_LIST;
            } else {
                return List.of(r.get().orElse(null));
            }

        } else {
            r.set(getInstance().queryById(entity.getId()));
            AssertUtil.notNull(r.get().isEmpty(), "实体数据不存在，无法更新");
            r.set(getInstance().compareAndUpdateValue(entity, r.get().get()));
            auditFunction(r, UPDATE);
            if (r.get().isEmpty()) {
                log.warn("实体数据不存在，无法保存");
                return Collections.EMPTY_LIST;
            } else {
                r.set(CollUtil.first(getInstance().updateBy(r.get().get())));
                return List.of(r.get().get());
            }
        }
    }

    private static final int CREATE = 0;
    private static final int UPDATE = 1;
    private static final int DELETE = 2;

    private void auditFunction(ValueWrapper<T> r, int flag) {
        getTimeAuditServiceSupport().ifPresent((s) -> {
            if (r.get().isPresent()) {
                if (flag == UPDATE) {
                    r.set(s.triggerUpdateTime(r.get().get(), new Date()));
                } else if (flag == CREATE) {
                    r.set(s.triggerCreateTime(r.get().get(), new Date()));
                }
                if (flag == DELETE) {
                    r.set(s.triggerDeleteTime(r.get().get(), new Date()));
                }
            } else {
                log.warn("实体数据不存在，无法处理时间审计");
            }
        });
        getOperatorAuditServiceSupport().ifPresent((s) -> {
            if (r.get().isPresent()) {
                if (flag == UPDATE) {
                    r.set(s.triggerUpdateOperator(r.get().get(),
                        AuthUtil.getInstance().isUserLoggedIn() ? AuthUtil.getInstance().getCurrentUser().getUsername()
                            : null));
                } else if (flag == CREATE) {
                    r.set(s.triggerCreateOperator(r.get().get(),
                        AuthUtil.getInstance().isUserLoggedIn() ? AuthUtil.getInstance().getCurrentUser().getUsername()
                            : null));
                }
                if (flag == DELETE) {
                    r.set(s.triggerDeleteOperator(r.get().get(),
                        AuthUtil.getInstance().isUserLoggedIn() ? AuthUtil.getInstance().getCurrentUser().getUsername()
                            : null));
                }

            } else {
                log.warn("实体数据不存在，无法处理操作人审计");
            }
        });
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

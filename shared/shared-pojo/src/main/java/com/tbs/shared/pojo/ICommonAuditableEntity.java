package com.tbs.shared.pojo;

/**
 * 常规支持增改等基础审计的实体
 *
 * @param <K>
 */
public interface ICommonAuditableEntity<K>
    extends IBaseEntity<K>, ICommonCreateAuditable<K>, ICommonUpdateAuditable<K> {
}

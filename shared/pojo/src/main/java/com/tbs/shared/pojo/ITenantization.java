package com.tbs.shared.pojo;

/**
 * 租户化接口
 *
 */
public interface ITenantization {

	/**
	 * 获取租户ID
	 *
	 * @return
	 */
	Long getTenantId();

	/**
	 * 设置租户ID
	 *
	 * @param tenantId
	 */
	void setTenantId(Long tenantId);

	/**
	 * 是否为公开租户，1-公开，0-私有
	 *
	 * @return
	 */
	Integer getOpenFlag();

	/**
	 * 设置是否为公开租户，1-公开，0-私有
	 *
	 * @param openFlag
	 */
	void setOpenFlag(Integer openFlag);
}

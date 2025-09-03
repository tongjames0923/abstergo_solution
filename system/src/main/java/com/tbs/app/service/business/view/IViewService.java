package com.tbs.app.service.business.view;

import com.tbs.shared.pojo.model.view.FormConfig;

public interface IViewService {
	/**
	 * 获取表单配置
	 *
	 * @param key
	 * @return
	 */
	FormConfig getForm(String key);
}

package com.tbs.shared.pojo.model.view;

public class DynamicOptionsFormField extends FormField {
	private String apiUrl;

	public DynamicOptionsFormField() {
		setType("dynamic-options");
	}

	public String getApiUrl() {
		return apiUrl;
	}

	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}
}

package com.tbs.shared.pojo.model.view;

import java.util.List;

public class SelectFormField extends FormField {
	private List<OptionValue<String>> options;

	public SelectFormField() {
		setType("select");
	}

	public List<OptionValue<String>> getOptions() {
		return options;
	}

	public void setOptions(List<OptionValue<String>> options) {
		this.options = options;
	}
}

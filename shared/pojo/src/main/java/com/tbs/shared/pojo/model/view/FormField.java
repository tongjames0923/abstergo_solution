package com.tbs.shared.pojo.model.view;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({ @JsonSubTypes.Type(value = FormField.class, name = "input"),
		@JsonSubTypes.Type(value = SelectFormField.class, name = "select"),
		@JsonSubTypes.Type(value = DateRangeFormField.class, name = "date-range"),
		@JsonSubTypes.Type(value = DynamicOptionsFormField.class, name = "dynamic-options") })
public class FormField implements Serializable {
	private static final long serialVersionUID = 1L;
	private String prop;
	private String label;
	private String type;
	private String placeholder;
	private Boolean clearable;
	private String prefixIcon;

	// Getters and setters
	public String getProp() {
		return prop;
	}

	public void setProp(String prop) {
		this.prop = prop;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPlaceholder() {
		return placeholder;
	}

	public void setPlaceholder(String placeholder) {
		this.placeholder = placeholder;
	}

	public Boolean getClearable() {
		return clearable;
	}

	public void setClearable(Boolean clearable) {
		this.clearable = clearable;
	}

	public String getPrefixIcon() {
		return prefixIcon;
	}

	public void setPrefixIcon(String prefixIcon) {
		this.prefixIcon = prefixIcon;
	}
}

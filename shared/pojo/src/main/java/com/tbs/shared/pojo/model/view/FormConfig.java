package com.tbs.shared.pojo.model.view;

import java.util.List;
import java.util.Map;

public class FormConfig {
	private Boolean expandable;
	private Boolean expanded;
	private Integer gutter;
	private String labelWidth;
	private Integer span;
	private List<FormField> fields;
	private Map<String, Object> props;

	// Getters and setters
	public Boolean getExpandable() {
		return expandable;
	}

	public void setExpandable(Boolean expandable) {
		this.expandable = expandable;
	}

	public Boolean getExpanded() {
		return expanded;
	}

	public void setExpanded(Boolean expanded) {
		this.expanded = expanded;
	}

	public Integer getGutter() {
		return gutter;
	}

	public void setGutter(Integer gutter) {
		this.gutter = gutter;
	}

	public String getLabelWidth() {
		return labelWidth;
	}

	public void setLabelWidth(String labelWidth) {
		this.labelWidth = labelWidth;
	}

	public Integer getSpan() {
		return span;
	}

	public void setSpan(Integer span) {
		this.span = span;
	}

	public List<FormField> getFields() {
		return fields;
	}

	public void setFields(List<FormField> fields) {
		this.fields = fields;
	}

	public Map<String, Object> getProps() {
		return props;
	}

	public void setProps(Map<String, Object> props) {
		this.props = props;
	}
}

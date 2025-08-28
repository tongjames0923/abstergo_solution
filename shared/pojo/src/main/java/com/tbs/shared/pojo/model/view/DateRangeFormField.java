package com.tbs.shared.pojo.model.view;

public class DateRangeFormField extends FormField {
    private String valueFormat;
    private String format;
    private String startPlaceholder;
    private String endPlaceholder;
    private String rangeSeparator;

    public DateRangeFormField() {
        setType("date-range");
    }

    public String getValueFormat() {
        return valueFormat;
    }

    public void setValueFormat(String valueFormat) {
        this.valueFormat = valueFormat;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getStartPlaceholder() {
        return startPlaceholder;
    }

    public void setStartPlaceholder(String startPlaceholder) {
        this.startPlaceholder = startPlaceholder;
    }

    public String getEndPlaceholder() {
        return endPlaceholder;
    }

    public void setEndPlaceholder(String endPlaceholder) {
        this.endPlaceholder = endPlaceholder;
    }

    public String getRangeSeparator() {
        return rangeSeparator;
    }

    public void setRangeSeparator(String rangeSeparator) {
        this.rangeSeparator = rangeSeparator;
    }
}

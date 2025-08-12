package com.tbs.transaction;

import com.tbs.common.BaseEnum;

public enum TransactionStatus implements BaseEnum<String> {

    BEGIN(0, "开始"), COMMIT(1, "提交"), ROLLBACK(2, "回滚"), COMPLETE(3, "完成");

    private Integer code;
    private String value;

    TransactionStatus(Integer code, String value) {
        this.code = code;
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public Integer getCode() {
        return code;
    }
}

package com.tbs.shared.pojo.entity;

import com.tbs.shared.pojo.IIdentifiable;
import com.tbs.sql.annotation.autofill.SqlAutoValue;
import com.tbs.sql.provider.impl.IdAutoProvider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * 系统操作记录表
 *
 * @author tongj
 */
@Table(name = "sys_operate_record",
    indexes = {@Index(name = "common_index", columnList = "operate_content,operate_type"),
        @Index(name = "time_idx", columnList = "operate_time"),
        @Index(name = "error_level_idx", columnList = "operate_error_level"),
        @Index(name = "user_idx", columnList = "operate_user_name,operate_ip"),
        @Index(name = "url_idx", columnList = "operate_url"), @Index(name = "method_idx", columnList = "method_type")})
@Entity
@Getter
@Setter
@ToString
public class SystemOperateRecordEntity implements IIdentifiable<Long>, Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @Column(name = "id")
    @jakarta.persistence.Id
    @SqlAutoValue(insertOnly = true, value = IdAutoProvider.class)
    private Long id;
    /**
     * 操作类型
     */
    @jakarta.persistence.Column(name = "operate_type", length = 128)
    private String operateType;

    /**
     * 操作内容,即调用的方法
     */
    @jakarta.persistence.Column(name = "operate_content", length = 512)
    private String operateContent;
    /**
     * 操作时间
     */
    @jakarta.persistence.Column(name = "operate_time")
    private Date operateTime;

    /**
     * 操作用户名称
     */
    @jakarta.persistence.Column(name = "operate_user_name", length = 64)
    private String operateUserName;
    /**
     * 操作ip
     */
    @jakarta.persistence.Column(name = "operate_ip", length = 64)
    private String operateIp;

    /**
     * 操作方法 GET,PUT等操作方法类型
     */
    @jakarta.persistence.Column(name = "method_type", length = 64)
    private String method;

    /**
     * 操作url
     */
    @jakarta.persistence.Column(name = "operate_url", length = 512)
    private String operateUrl;

    /**
     * 操作参数json
     */
    @jakarta.persistence.Column(name = "operate_param_json", columnDefinition = "longtext")
    private String operateParamJson;
    /**
     * 操作结果json
     */
    @jakarta.persistence.Column(name = "operate_result_json", columnDefinition = "longtext")
    private String operateResultJson;

    /**
     * 操作耗时
     */
    @jakarta.persistence.Column(name = "cost_time")
    private Long costTime;

    /**
     * 操作异常信息
     */
    @jakarta.persistence.Column(name = "operate_error", columnDefinition = "longtext")
    private String operateError;

    /**
     * 操作异常级别
     */
    @Column(name = "operate_error_level")
    private Integer operateErrorLevel;

    /**
     * 系统日志标志
     */
    @Column(name = "system_log_flag")
    private Integer systemLogFlag;

    /**
     * 链路追踪ID
     */
    @Column(name = "trace_id", length = 64)
    private String traceId;
}

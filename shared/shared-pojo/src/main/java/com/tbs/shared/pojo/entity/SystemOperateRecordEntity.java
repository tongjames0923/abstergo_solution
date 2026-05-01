package com.tbs.shared.pojo.entity;

import com.tbs.shared.pojo.IIdentifiable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Date;

/**
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
    @javax.persistence.Id
    @javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    private Long id;
    /**
     * 操作类型
     */
    @javax.persistence.Column(name = "operate_type", length = 128)
    private String operateType;

    /**
     * 操作内容,即调用的方法
     */
    @javax.persistence.Column(name = "operate_content", length = 512)
    private String operateContent;
    /**
     * 操作时间
     */
    @javax.persistence.Column(name = "operate_time")

    private Date operateTime;

    /**
     * 操作用户名称
     */
    @javax.persistence.Column(name = "operate_user_name", length = 64)
    private String operateUserName;
    /**
     * 操作ip
     */
    @javax.persistence.Column(name = "operate_ip", length = 64)
    private String operateIp;

    /**
     * 操作方法 GET,PUT等操作方法类型
     */
    @javax.persistence.Column(name = "method_type", length = 64)
    private String method;

    /**
     * 操作url
     */
    @javax.persistence.Column(name = "operate_url", length = 512)
    private String operateUrl;

    /**
     * 操作参数json
     */
    @javax.persistence.Column(name = "operate_param_json", columnDefinition = "longtext")
    private String operateParamJson;
    /**
     * 操作结果json
     */
    @javax.persistence.Column(name = "operate_result_json", columnDefinition = "longtext")
    private String operateResultJson;

    /**
     * 操作耗时
     */
    @javax.persistence.Column(name = "cost_time")

    private Long costTime;

    /**
     * 操作异常信息
     */
    @javax.persistence.Column(name = "operate_error", columnDefinition = "longtext")
    private String operateError;

    /**
     * 操作异常级别
     */
    @Column(name = "operate_error_level")
    private Integer operateErrorLevel;

}

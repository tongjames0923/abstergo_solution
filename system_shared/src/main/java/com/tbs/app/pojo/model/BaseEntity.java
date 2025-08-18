package com.tbs.app.pojo.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.persistence.Column;
import java.io.Serializable;
import java.util.Date;

/**
 * 基础的数据库实体类
 *
 * @author abstergo
 */
@Data
public class BaseEntity implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "create_time")
    private Date createTime;
    /**
     * 修改时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Column(name = "update_time")
    private Date updateTime;
    /**
     * 状态
     */
    @Column(name = "flag")
    private Integer flag;

}

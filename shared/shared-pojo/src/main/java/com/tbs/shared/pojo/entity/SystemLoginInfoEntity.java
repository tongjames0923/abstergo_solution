package com.tbs.shared.pojo.entity;

import com.tbs.shared.annotations.LogicDeleteEntity;
import com.tbs.shared.pojo.IBaseEntity;
import com.tbs.shared.pojo.ICreateTimeAuditable;
import com.tbs.shared.pojo.IUpdateTimeAuditable;
import com.tbs.sql.annotation.autofill.SqlAutoValue;
import com.tbs.sql.provider.impl.IdAutoProvider;
import org.hibernate.annotations.Comment;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.util.Date;

/**
 * 系统登录信息
 *
 * @author abstergo
 */
@Entity
@Table(name = "sys_login_info", indexes = {@Index(name = "idx_flag", columnList = "flag"),
    @Index(name = "idx_time", columnList = "create_time,update_time,token_expire_time"),
    @Index(name = "idx_user_id", columnList = "user_id"), @Index(name = "idx_token", columnList = "token")})
@Slf4j
@LogicDeleteEntity
@Comment("系统登录信息表")
public class SystemLoginInfoEntity implements IBaseEntity<Long>, ICreateTimeAuditable, IUpdateTimeAuditable {

    /**
     * 主键
     */
    @Comment("主键ID")
    @jakarta.persistence.Id
    @Getter
    @Setter
    @SqlAutoValue(insertOnly = true, value = IdAutoProvider.class)
    private Long id;

    /**
     * 用户ID
     */
    @Comment("关联用户ID")
    @jakarta.persistence.Column(name = "user_id")
    @Getter
    @Setter
    private Long userId;

    /**
     * 登录令牌
     */
    @Comment("登录令牌")
    @jakarta.persistence.Column(name = "token", length = 128)
    @Getter
    @Setter
    private String token;

    /**
     * 用户详情
     */
    @Comment("用户详情（序列化字节数组）")
    @jakarta.persistence.Column(name = "user_details", columnDefinition = "MEDIUMBLOB")
    @Getter
    @Setter
    private byte[] userDetails;

    /**
     * 状态
     */
    @Comment("登录状态：1-正常，0-已删除，-1-已撤销")
    @jakarta.persistence.Column(name = "flag")
    @Getter
    @Setter
    private Integer flag;

    /**
     * 创建时间
     */
    @Comment("创建时间")
    @jakarta.persistence.Column(name = "create_time")
    @Getter
    @Setter
    private Date createTime;

    /**
     * 更新时间
     */
    @Comment("更新时间")
    @jakarta.persistence.Column(name = "update_time")
    @Getter
    @Setter
    private Date updateTime;

    /**
     * 令牌过期时间
     */
    @Comment("令牌过期时间")
    @Column(name = "token_expire_time")
    @Getter
    @Setter
    private Date tokenExpireTime;

    /**
     * sessionId
     */
    @Comment("会话ID")
    @Column(name = "session_id", length = 128)
    @Getter
    @Setter
    private String sessionId;

}

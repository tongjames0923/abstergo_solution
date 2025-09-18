package com.tbs.shared.pojo.entity;

import com.tbs.common.util.SpringUtil;
import com.tbs.common.util.StrUtil;
import com.tbs.shared.IUserDetailsTransformer;
import com.tbs.shared.pojo.IBaseEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.Date;

/**
 * 系统登录信息
 *
 * @author abstergo
 */
@Entity
@Table(name = "sys_login_info", indexes = {@Index(name = "idx_flag", columnList = "flag"),
    @Index(name = "idx_time", columnList = "create_time,update_time"),
    @Index(name = "idx_user_id", columnList = "user_id"), @Index(name = "idx_token", columnList = "token")})
@Slf4j
public class SystemLoginInfoEntity implements IBaseEntity<Long> {

    /**
     * 主键
     */
    @javax.persistence.Id
    @javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.IDENTITY)
    @Getter
    @Setter
    private Long id;

    /**
     * 用户ID
     */
    @javax.persistence.Column(name = "user_id")
    @Getter
    @Setter
    private Long userId;

    /**
     * 登录令牌
     */
    @javax.persistence.Column(name = "token", length = 128)
    @Getter
    @Setter
    private String token;

    /**
     * 用户详情
     */
    @javax.persistence.Column(name = "user_details", columnDefinition = "MEDIUMBLOB")
    @Getter
    @Setter
    private byte[] userDetails;

    /**
     * 登录信息转换器实现类
     */
    @javax.persistence.Column(name = "user_details_transformer_impl", length = 128)
    @Getter
    @Setter
    private String userDetailsTransformerImpl;

    @javax.persistence.Column(name = "user_details_target_class", length = 256)
    @Getter
    @Setter
    private String userDetailsTargetClass;

    /**
     * 状态
     */
    @javax.persistence.Column(name = "flag")
    @Getter
    @Setter
    private Integer flag;

    /**
     * 创建时间
     */
    @javax.persistence.Column(name = "create_time")
    @Getter
    @Setter
    private Date createTime;

    /**
     * 更新时间
     */
    @javax.persistence.Column(name = "update_time")
    @Getter
    @Setter
    private Date updateTime;

    @Transient
    private UserDetails userDetailsObject;

    @Transient
    public UserDetails getUserDetailsObject() {
        if (getUserDetails() == null || getUserDetails().length == 0) {
            log.warn("用户详情转换失败: userDetails is null");
            return null;
        }
        if (StrUtil.isEmpty(getUserDetailsTransformerImpl()) || StrUtil.isEmpty(getUserDetailsTargetClass())) {
            log.warn("用户详情转换失败: userDetailsTransformerImpl or userDetailsTargetClass is null");
            return null;
        }
        if (userDetailsObject == null) {
            try {
                IUserDetailsTransformer transformer = SpringUtil.getBean(getUserDetailsTransformerImpl());
                userDetailsObject = transformer.reverseTransform(getUserDetails(),
                    (Class<? extends UserDetails>)Class.forName(getUserDetailsTargetClass()));
            } catch (Exception e) {
                log.error("用户详情转换失败: {}", e.getMessage());
            }
        }
        return userDetailsObject;
    }

    @Transient
    public void setUserDetailsObject(UserDetails userDetailsObject) {
        if (userDetailsObject == null) {
            setUserDetails(null);
            setUserDetailsTargetClass(null);
            return;
        }
        this.userDetailsObject = userDetailsObject;
        if (StrUtil.isEmpty(getUserDetailsTransformerImpl())) {
            log.warn("用户详情转换失败: userDetailsTransformerImpl is null");
        }
        try {
            IUserDetailsTransformer transformer = SpringUtil.getBean(getUserDetailsTransformerImpl());
            setUserDetails(transformer.transform(getUserDetailsObject()));
            setUserDetailsTargetClass(getUserDetailsObject().getClass().getName());
        } catch (Exception e) {
            log.warn("用户详情转换失败: {}", e.getMessage());
            setUserDetails(null);
            setUserDetailsTargetClass(null);
        }
    }

}

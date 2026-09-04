package com.tbs.web.result;

import com.tbs.common.i18n.I18nUtil;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;
import com.tbs.web.constant.WebI18nConstant;

/**
 * 统一 Ajax 响应结果
 * <p>
 * 作为 Web 层控制器与全局异常处理器的标准返回载体，内部以 {@code Map} 存放任意业务字段，
 * 约定键名见 {@link #CODE_KEY}/{@link #MESSAGE_KEY}/{@link #DATA_KEY}。提供一组默认状态码与对应国际化消息常量，
 * 由 {@code ResponseAjaxAspect} 在返回时统一包装。
 * </p>
 *
 * @author tongj
 * @see AjaxResultFluentBuilder 流式构建器
 */
@Data
public class AjaxResult implements Serializable {
	private static final long serialVersionUID = 1L;

	// ==================== 常量键名 ====================
	/**
	 * 默认数据键
	 */
	public static final String MESSAGE_KEY = "message";
	/**
	 * 默认数据键
	 */
	public static final String CODE_KEY = "code";
	/**
	 * 默认数据键
	 */
	public static final String DATA_KEY = "data";

	// ==================== 默认码值 ====================
	/**
	 * 默认成功码
	 */
	public static final int DEFAULT_SUCCESS_CODE = 200;
	/**
	 * 默认错误码
	 */
	public static final int DEFAULT_ERROR_CODE = 500;
	/**
	 * 默认未授权码
	 */
	public static final int DEFAULT_UNAUTHORIZED_CODE = 401;
	/**
	 * 默认重新登录码
	 */
	public static final int RELOGIN_CODE = 405;
	/**
	 * 默认刷新失败码
	 */
	public static final int REFRESH_FAIL_CODE = 468;
	/**
	 * 默认调试错误码
	 */
	public static final int DEBUG_ERROR_CODE = 601;
	/**
	 * 默认通知码
	 */
	public static final int NOTICE_CODE = 701;
	/**
	 * 默认无权限码
	 */
	public static final int DEFAULT_FORBIDDEN_CODE = 403;

	// ==================== 国际化消息常量Key ====================
	/** 默认成功消息key */
	public static final String DEFAULT_SUCCESS_MESSAGE_KEY = com.tbs.web.constant.WebI18nConstant.COMMON_SUCCESS;
	/** 默认失败消息key */
	public static final String DEFAULT_FAIL_MESSAGE_KEY = com.tbs.web.constant.WebI18nConstant.COMMON_FAIL;
	/** 默认错误消息key */
	public static final String DEFAULT_ERROR_MESSAGE_KEY = com.tbs.web.constant.WebI18nConstant.COMMON_ERROR;
	/** 默认重新登录消息key */
	public static final String DEFAULT_RELOGIN_MESSAGE_KEY = com.tbs.web.constant.WebI18nConstant.COMMON_RELOGIN;
	/** 刷新失败消息key */
	public static final String REFRESH_FAIL_MESSAGE_KEY = com.tbs.web.constant.WebI18nConstant.TOKEN_REFRESH_FAIL;
	/** 默认未授权消息key */
	public static final String DEFAULT_UNAUTHORIZED_MESSAGE_KEY = com.tbs.web.constant.WebI18nConstant.COMMON_UNAUTHORIZED;
	/** 默认无权限消息key */
	public static final String DEFAULT_FORBIDDEN_MESSAGE_KEY = com.tbs.web.constant.WebI18nConstant.COMMON_FORBIDDEN;

	// ==================== 静态方法获取国际化消息 ====================

	/**
	 * 获取默认成功消息
	 * @return 成功消息
	 */
	public static String getDefaultSuccessMessage() {
		return I18nUtil.get(DEFAULT_SUCCESS_MESSAGE_KEY);
	}

	/**
	 * 获取默认失败消息
	 * @return 失败消息
	 */
	public static String getDefaultFailMessage() {
		return I18nUtil.get(DEFAULT_FAIL_MESSAGE_KEY);
	}

	/**
	 * 获取默认错误消息
	 * @return 错误消息
	 */
	public static String getDefaultErrorMessage() {
		return I18nUtil.get(DEFAULT_ERROR_MESSAGE_KEY);
	}

	/**
	 * 获取默认重新登录消息
	 * @return 重新登录消息
	 */
	public static String getDefaultReLoginMessage() {
		return I18nUtil.get(DEFAULT_RELOGIN_MESSAGE_KEY);
	}

	/**
	 * 获取刷新失败消息
	 * @return 刷新失败消息
	 */
	public static String getRefreshFailMessage() {
		return I18nUtil.get(REFRESH_FAIL_MESSAGE_KEY);
	}

	/**
	 * 获取默认未授权消息
	 * @return 未授权消息
	 */
	public static String getDefaultUnauthorizedMessage() {
		return I18nUtil.get(DEFAULT_UNAUTHORIZED_MESSAGE_KEY);
	}

	/**
	 * 获取默认无权限消息
	 * @return 无权限消息
	 */
	public static String getDefaultForbiddenMessage() {
		return I18nUtil.get(DEFAULT_FORBIDDEN_MESSAGE_KEY);
	}

	private Map<String, Object> map;
}
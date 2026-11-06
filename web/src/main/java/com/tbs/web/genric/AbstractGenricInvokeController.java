package com.tbs.web.genric;

import com.fasterxml.jackson.databind.JsonNode;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.error.ErrorLevelEnum;
import com.tbs.web.BaseController;
import com.tbs.web.constant.WebI18nConstant;
import com.tbs.web.genric.config.GenericApiConfig;
import com.tbs.web.result.AjaxResult;
import jakarta.annotation.Resource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 抽象泛化调用控制器（新版链路）
 * <p>
 * 只提供两个入口，与旧的信封式 {@code POST /invoke} 并存、互不影响：
 * </p>
 * <ol>
 *     <li>{@code POST /call/{apiId}}：请求体是命名式 JSON 对象或位置式 JSON 数组，类型与顺序取登记定义；</li>
 *     <li>{@code GET /meta/{apiId}}：透出登记的入参定义，供前端渲染表单与预填示例。</li>
 * </ol>
 * <p>
 * 子类只负责两件事：从配置存储取出「可调用的泛化API」（含合法性判定），以及按认证维度给出当前用户。
 * 响应只有一层：{@code AjaxResult.data} 就是远端返回值本体，执行失败不再写进内层成功标志，
 * 而是抛给全局异常处理转成统一错误码（旧 {@code /invoke} 的双层结构保持原样不变）。
 * </p>
 *
 * @author abstergo
 * @since 1.0.0
 */
public abstract class AbstractGenricInvokeController extends BaseController {

    /**
     * 泛化调用目标工厂
     */
    @Resource
    protected GenericApiTargetFactory genericApiTargetFactory;

    /**
     * 取出可被泛化调用的 API 配置
     * <p>
     * 存在性、启用态、认证约束等规则在 Service 层断言，本方法只做取数与模型映射。
     * </p>
     *
     * @param apiId API 唯一标识
     * @return 泛化调用配置（含入参定义）
     */
    protected abstract GenericApiConfig loadApiConfig(String apiId);

    /**
     * 获取当前登录用户
     *
     * @return 当前用户详情；匿名入口返回 {@code null}
     */
    protected abstract UserDetails getCurrentUser();

    /**
     * 按登记定义泛化调用目标方法
     *
     * @param apiId   API 唯一标识
     * @param version 业务配置版本，可空；与登记版本不一致时报错，不参与 Dubbo 坐标
     * @param body    请求体：命名式 JSON 对象或位置式 JSON 数组，无参方法可省略
     * @return 统一响应，{@code data} 直接是远端方法的返回值本体（失败时 code≠200）
     */
    @PostMapping("/call/{apiId}")
    public AjaxResult call(@PathVariable String apiId, @RequestParam(required = false) Integer version,
        @RequestBody(required = false) JsonNode body) {
        ConfiguredGenricApiInstance instance = resolveInstance(apiId, version);
        return successResult(instance.invokeByConfig(body, getCurrentUser()));
    }

    /**
     * 查询 API 登记的入参定义
     *
     * @param apiId   API 唯一标识
     * @param version 业务配置版本，可空；只用于校验登记版本，不参与 Dubbo 坐标
     * @return 统一响应，数据为按签名顺序排列的参数定义
     */
    @GetMapping("/meta/{apiId}")
    public AjaxResult meta(@PathVariable String apiId, @RequestParam(required = false) Integer version) {
        ConfiguredGenricApiInstance instance = resolveInstance(apiId, version);
        return successResult(instance.paramsMeta());
    }

    /**
     * 取出配置并构建调用目标，同时校验请求的业务配置版本与登记版本
     * <p>
     * 登记版本为空时不校验（一个 apiId 只有一行配置，版本只用于区分登记批次）；
     * Dubbo 侧的分流只靠 business（group），本方法不读版本。
     *
     * @param apiId   API 唯一标识
     * @param version 请求版本，可以为 {@code null}
     * @return 泛化调用目标
     */
    private ConfiguredGenricApiInstance resolveInstance(String apiId, Integer version) {
        GenericApiConfig config = loadApiConfig(apiId);
        AssertUtil.isTrue(version != null && config.getVersion() != null && !version.equals(config.getVersion()))
            .errorLevel(ErrorLevelEnum.USER_INPUT_ERROR).i18n(WebI18nConstant.GENERIC_API_API_VERSION_NOT_FOUND)
            .throwIt();
        return genericApiTargetFactory.create(config);
    }
}

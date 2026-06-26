package com.tbs.app.controller.privates;

import com.tbs.app.form.DicItemSearchForm;
import com.tbs.app.form.DicSearchForm;
import com.tbs.common.error.AssertUtil;
import com.tbs.common.util.base.CollUtil;
import com.tbs.dic.model.DicModel;
import com.tbs.dic.service.IDataDicService;
import com.tbs.shared.constant.RoleConstants;
import com.tbs.web.BaseController;
import com.tbs.web.result.AjaxResult;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedList;
import java.util.List;

@RestController
@PreAuthorize("@authUtil.userLoggedIn and @hasAnyAuthority(" +
    RoleConstants.SUPER_ADMIN_ROLE_NAME +
    "," +
    RoleConstants.DEVELOPER_ROLE_NAME +
    ")")
@RequestMapping("private/admin/dic")
public class SystemDataDicController extends BaseController {

    @DubboReference(lazy = true)
    private IDataDicService dataDicService;

    @PostMapping("query")
    public AjaxResult listDicByDicNameLike(@RequestBody DicSearchForm form) {
        if (form == null) {
            form = new DicSearchForm();
        }
        if (form.getDicCode() == null) {
            form.setDicCode("");
        }
        if (form.getDicName() == null) {
            form.setDicName("");
        }
        var dicCodes = dataDicService.getDicCodesByNamesLike(form.getDicName());
        if (CollUtil.isEmpty(dicCodes)) {
            return successResult(CollUtil.newArrayList());
        }
        List<DicModel> dicModels = new LinkedList<>();
        for (var dicCode : dicCodes) {
            if (dicCode.contains(form.getDicCode())) {
                dicModels.addAll(dataDicService.getDicByCode(dicCode, true));
            }
        }
        return successResult(dicModels);
    }

    @PostMapping("enableDic")
    public AjaxResult enableDic(@RequestBody DicSearchForm form) {
        AssertUtil.notNull(form, "字典编号不能为空");
        AssertUtil.hasText(form.getDicCode(), "字典编号不能为空");
        dataDicService.enableDataDic(form.getDicCode());
        return successResult(null);
    }

    @PostMapping("disableDic")
    public AjaxResult disableDic(@RequestBody DicSearchForm form) {
        AssertUtil.notNull(form, "字典编号不能为空");
        AssertUtil.hasText(form.getDicCode(), "字典编号不能为空");
        dataDicService.disableDataDic(form.getDicCode());
        return successResult(null);
    }

    @PostMapping("enableDicItem")
    public AjaxResult enableDicItem(@RequestBody DicItemSearchForm form) {
        AssertUtil.notNull(form, "字典编号不能为空");
        AssertUtil.hasText(form.getDicCode(), "字典编号不能为空");
        AssertUtil.hasText(form.getLabel(), "字典项不能为空");
        dataDicService.enableDataDicItem(form.getDicCode(), form.getLabel());
        return successResult(null);
    }

    @PostMapping("disableDicItem")
    public AjaxResult disableDicItem(@RequestBody DicItemSearchForm form) {
        AssertUtil.notNull(form, "字典编号不能为空");
        AssertUtil.hasText(form.getDicCode(), "字典编号不能为空");
        AssertUtil.hasText(form.getLabel(), "字典项不能为空");
        dataDicService.disableDataDicItem(form.getDicCode(), form.getLabel());
        return successResult(null);
    }

    @PostMapping("deleteDic")
    public AjaxResult deleteDic(@RequestBody DicSearchForm form) {
        AssertUtil.notNull(form, "字典编号不能为空");
        AssertUtil.hasText(form.getDicCode(), "字典编号不能为空");
        dataDicService.deleteDataDic(form.getDicCode());
        return successResult(null);
    }

    @PostMapping("deleteDicItem")
    public AjaxResult deleteDicItem(@RequestBody DicItemSearchForm form) {
        AssertUtil.notNull(form, "字典编号不能为空");
        AssertUtil.hasText(form.getDicCode(), "字典编号不能为空");
        AssertUtil.hasText(form.getLabel(), "字典项不能为空");
        dataDicService.deleteDataDicItem(form.getDicCode(), form.getLabel());
        return successResult(null);
    }

    @Override
    public String getBusinessName() {
        return "数据字典";
    }
}

package com.tbs.app.controller.privates;

import com.tbs.common.util.framework.SpringUtil;
import com.tbs.web.genric.AbstractAuthGenricApiController;
import com.tbs.web.genric.AbstractGenricApiInstance;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.rpc.service.GenericService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;

/**
 * 泛化接口控制器
 *
 * @author abstergo
 */
@RestController
@RequestMapping("/private/genric")
public class GenricController extends AbstractAuthGenricApiController {



    @Override
    public String getBusinessName() {
        return "泛化调用接口";
    }

    @Override
    protected Collection<AbstractGenricApiInstance> getGenricApiInstance(String id) {
        return List.of();
    }
}

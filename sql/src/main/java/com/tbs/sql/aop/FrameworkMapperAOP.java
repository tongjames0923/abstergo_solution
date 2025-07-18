package com.tbs.sql.aop;

import cn.hutool.core.bean.copier.CopyOptions;
import com.tbs.common.util.BeanUtil;
import com.tbs.common.util.CollUtil;
import com.tbs.sql.mapper.impl.EntitySelectFetchMapper;
import com.tbs.sql.query.IPage;
import com.tbs.sql.query.IQuery;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.context.annotation.Lazy;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * framework mapper aop, 用于处理查询结果集的aop
 */
@Aspect
public class FrameworkMapperAOP {

    @Pointcut("execution(* com.tbs.sql.mapper.IFrameworkMapper.queryByQO(..))")
    public void queryPointCut() {

    }

    @Pointcut("execution(* com.tbs.sql.mapper.IFrameworkMapper.countByQO(..))")
    public void countPointCut2() {
    }

    @Lazy
    @Resource
    EntitySelectFetchMapper entitySelectFetchMapper;

    @Around("queryPointCut()")
    Object query(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Map<String, Object> f = new HashMap<>();
        List<Map<String, Object>> list = entitySelectFetchMapper.queryByQO((IQuery)args[0], (IPage)args[1], f);
        if (CollUtil.isEmpty(list)) {
            return new ArrayList<>(0);
        }
        return list.stream().map(m -> {
            return BeanUtil.mapToBean(m, (Class<? extends Object>)args[2], true, CopyOptions.create());
        }).collect(Collectors.toList());

    }

    @Around("countPointCut2()")
    Object count(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] args = joinPoint.getArgs();
        Map<String, Object> f = new HashMap<>();
        return entitySelectFetchMapper.countByQO((IQuery)args[0], f);
    }
}

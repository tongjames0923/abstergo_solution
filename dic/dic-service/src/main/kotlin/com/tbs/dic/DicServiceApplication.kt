package com.tbs.dic

import com.tbs.async.annotation.EnableTbsAsyncPack
import com.tbs.cache.annotation.EnableTbsCache
import com.tbs.redis.annotation.EnableTbsRedis
import com.tbs.shared.annotation.EnableSharedLibBean
import com.tbs.sql.annotation.core.EnableTbsSql
import com.tbs.sql.annotation.dialect.UseMysql
import org.apache.dubbo.config.spring.context.annotation.EnableDubbo
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.EnableAspectJAutoProxy
import tk.mybatis.spring.annotation.MapperScan


@SpringBootApplication(scanBasePackages = ["com.tbs.dic"])
@EnableTbsSql
@EnableTbsAsyncPack
@EnableTbsCache
@EnableSharedLibBean
@UseMysql
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableTbsRedis
@EnableDubbo
@MapperScan("com.tbs.dic.mapper")
@EntityScan("com.tbs.dic.entity")
class DicServiceApplication


fun main(args: Array<String>) {
    runApplication<DicServiceApplication>(*args)
}

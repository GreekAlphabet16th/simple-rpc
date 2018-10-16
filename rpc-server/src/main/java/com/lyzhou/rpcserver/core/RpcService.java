package com.lyzhou.rpcserver.core;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * rpc接口服务注解
 * @author zhouliyu
 * */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Component //可被Spring扫描
public @interface RpcService {
    Class<?> value();
}

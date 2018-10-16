package com.lyzhou.rpcserver.service.impl;

import com.lyzhou.rpcserver.core.RpcService;
import com.lyzhou.rpccommon.service.HelloService;

@RpcService(HelloService.class)
public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(String name) {
        return "hello: " + name;
    }
}

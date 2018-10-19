package com.lyzhou.rpcserver.service.impl;

import com.lyzhou.rpccommon.service.UserInfo;
import com.lyzhou.rpcserver.core.RpcService;
import com.lyzhou.rpccommon.service.HelloService;

@RpcService(HelloService.class)
public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(String name) {
        return "hello: " + name;
    }

    @Override
    public UserInfo getUser(long id) {
        UserInfo user = new UserInfo();
        user.setId(1L);
        user.setAge(16);
        user.setName("zhouliyu");
        if(user.getId() == id){
            return user;
        }else {
            return null;
        }
    }
}

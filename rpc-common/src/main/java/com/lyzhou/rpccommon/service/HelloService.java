package com.lyzhou.rpccommon.service;

public interface HelloService {

    String hello(String name);

    UserInfo getUser(long id);
}

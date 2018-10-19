package com.lyzhou.rpcclient.async;

/**
 * RPC回调函数
 * @author zhouliyu
 * */
public interface RpcCallback {

    void success(Object result);
    void error(Throwable throwable);
}

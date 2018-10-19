package com.lyzhou.rpcregistry;

public interface Constant {

    int ZK_SESSION_TIMEOUT = 5000;

    //服务注册zk根节点
    String ZK_REGISTRY_PATH = "/registry";
    //服务数据存储zk根节点
    String ZK_DATA_PATH = ZK_REGISTRY_PATH + "/data";


}

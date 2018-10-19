package com.lyzhou.rpcregistry;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * zookeeper 实现服务注册管理
 * @author zhouliyu
 * */
public class ServiceRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceRegistry.class);

    private CountDownLatch latch = new CountDownLatch(1);
    private String registryAddress;

    public ServiceRegistry(String registryAddress) {
        this.registryAddress = registryAddress;
    }

    public void register(String data){
        if(data != null){
            ZooKeeper zk = connectServer();
            if(zk != null){
                addRootNode(zk);//判断是否有根节点，不能直接创建子节点
                createNode(zk, data);
            }
        }
    }

    /**
     * zookeeper服务连接
     * @return Zookeeper
     * */
    private ZooKeeper connectServer(){
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(registryAddress, Constant.ZK_SESSION_TIMEOUT,event -> {
                if(event.getState() == Watcher.Event.KeeperState.SyncConnected){
                    latch.countDown();
                }
            });
            latch.await();
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Can't get connection for zookeeper,registryAddress:{}", registryAddress, e);
        }
        return zk;
    }

    /**
     * 创建根节点
     * @param zk
     * */
    private void addRootNode(ZooKeeper zk){
        try {
            Stat stat = zk.exists(Constant.ZK_REGISTRY_PATH, false);
            if(null == stat){
                zk.create(Constant.ZK_REGISTRY_PATH, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                LOGGER.debug("create zookeeper root node)");
            }
        } catch (KeeperException | InterruptedException e) {
            LOGGER.error("", e);
        }
    }
    /**
     * 创建数据存储节点
     * @param zk
     * @param data
     * */
    private void createNode(ZooKeeper zk, String data){
        try {
            byte[] bytes = data.getBytes();
            String path = zk.create(Constant.ZK_DATA_PATH, bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            LOGGER.debug("create zookeeper node ({} => {})", path, data);
        } catch (KeeperException | InterruptedException e) {
            LOGGER.error("", e);
        }
    }
}

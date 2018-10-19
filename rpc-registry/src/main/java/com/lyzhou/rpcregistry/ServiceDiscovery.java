package com.lyzhou.rpcregistry;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

/**
 * zookeeper 实现服务发现管理
 * @author zhouliyu
 * */
public class ServiceDiscovery {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceDiscovery.class);

    private CountDownLatch latch = new CountDownLatch(1);

    private String registryAddress;
    private ZooKeeper zooKeeper;

    private volatile List<String> dataList = new ArrayList<>();

    public ServiceDiscovery(String registryAddress) {
        this.registryAddress = registryAddress;
        zooKeeper = connectServer();
        if (zooKeeper != null) {
            watchNode(zooKeeper);
        }

    }

    /**
     * 查找服务
     * */
    public String discover(){
        String data = null;
        int size = dataList.size();
        if (size > 0) {
            if (size == 1) {
                data = dataList.get(0);
                LOGGER.debug("using only data: {}", data);
            } else {
                data = dataList.get(ThreadLocalRandom.current().nextInt(size));
                LOGGER.debug("using random data: {}", data);
            }
        }

        return data;
    }

    /**
     * zookeeper服务连接
     * @return Zookeeper
     * */
    private ZooKeeper connectServer(){
        ZooKeeper zk = null;
        try {
            zk = new ZooKeeper(registryAddress, Constant.ZK_SESSION_TIMEOUT, event -> {
                if(event.getState() == Watcher.Event.KeeperState.SyncConnected){
                    latch.countDown();
                }
            });
            latch.await();
        } catch (IOException | InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return zk;
    }

    private void watchNode(final ZooKeeper zk) {
        try {
            List<String> nodeList = zk.getChildren(Constant.ZK_REGISTRY_PATH, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getType() == Event.EventType.NodeChildrenChanged) {
                        watchNode(zk); //服务治理，通过watch来监控子节点的变化
                    }
                }
            });
            List<String> dataList = new ArrayList<>();
            for (String node : nodeList) {
                byte[] bytes = zk.getData(Constant.ZK_REGISTRY_PATH + "/" + node, false, null);
                dataList.add(new String(bytes));
            }
            LOGGER.debug("node data: {}", dataList);
            this.dataList = dataList;
        } catch (KeeperException | InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void stop(){
        if(zooKeeper != null){
            try {
                zooKeeper.close();
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
    }
}

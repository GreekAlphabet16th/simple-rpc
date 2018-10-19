package com.lyzhou.rpcclient.async;

import com.lyzhou.rpccommon.protocol.RpcRequest;
import com.lyzhou.rpccommon.protocol.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 自定义同步组件(独占锁)
 * @author zhouliyu
 * */

public class RpcFutureTask implements Future<Object> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcFutureTask.class);

    private RpcRequest request;
    private RpcResponse response;
    private long startTime;
    //响应时间阈值：5秒
    private long responseThreshold = 5000;
    //同步器
    private final Sync sync = new Sync();

    //回调函数处理
    private List<RpcCallback> callbacks = new ArrayList<>();
    private ReentrantLock lock = new ReentrantLock();

    public RpcFutureTask(RpcRequest request) {
        this.request = request;
        startTime = System.currentTimeMillis();
    }

    //是否通过中断释放锁(不支持中断操作判断)
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCancelled() {
        return sync.tryRelease(1);
    }

    @Override
    public boolean isDone() {
        return sync.isHeldExclusively();
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        sync.acquire(1); //同步获取
        if(response != null){
            return response.getResult();
        }
        return null;
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        boolean success = sync.tryAcquireNanos(1, unit.toNanos(timeout)); //超时同步获取
        if (success){
            if(response != null){
                return response.getResult();
            }
            return null;
        } else {
            throw new RuntimeException("Timeout exception. Request id: " + request.getRequestId()
                    + ". Request class name: " + request.getClassName()
                    + ". Request method: " + request.getMethodName());
        }
    }

    /**
     * 响应处理
     * @param response
     * */
    public void getResponse(RpcResponse response){
        this.response = response;
        sync.release(1);//同步释放
        invokeCallback();
        long remaining = System.currentTimeMillis() - startTime;
        if(remaining > responseThreshold){
            LOGGER.warn("Service response time is too slow. Request id = " + response.getRequestId() + ". Response Time = " + remaining + "ms");
        }
    }

    /**
     * 调用回调函数
     * */
    private void invokeCallback(){
        lock.lock();
        try {
            for (final RpcCallback callback : callbacks) {
                runCallback(callback);
            }
        }finally {
            lock.unlock();
        }
    }

    /**
     * 添加回调函数
     * @param callback
     * */
   public void addCallback(final RpcCallback callback){
       lock.lock();
       try {
           if(isDone()){
               runCallback(callback);
           }else {
               callbacks.add(callback);
           }
       }finally {
           lock.unlock();
       }
   }

    /**
     * 运行回调函数
     * @param callback
     * */
    private void runCallback(final RpcCallback callback){
        final RpcResponse response = this.response;
        if(!response.isError()){
            callback.success(response.getResult());
        } else {
            callback.error(response.getError());
        }
    }




    //独有锁同步器
    private static class Sync extends AbstractQueuedSynchronizer {

        private static final long serialVersionUID = -7594763935547917256L;

        //是否处于占用状态
        @Override
        protected boolean isHeldExclusively() {
            return getState() == 1;
        }

        //当状态为0时获取锁
        @Override
        protected boolean tryAcquire(int arg) {
            if(compareAndSetState(0, 1)){
                return true;
            }
            return false;
        }

        //释放锁，将状态设置为0
        @Override
        protected boolean tryRelease(int arg) {
            if(getState() == 0) throw new IllegalMonitorStateException();
            setExclusiveOwnerThread(null);//关闭独有线程
            setState(0);
            return true;
        }
    }
}

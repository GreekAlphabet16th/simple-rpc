package com.lyzhou.rpcclient.async;

import com.lyzhou.rpccommon.protocol.RpcRequest;
import com.lyzhou.rpccommon.protocol.RpcResponse;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;

/**
 * PRC客户端处理
 * @author zhouliyu
 * */
public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcClientHandler.class);

    private ConcurrentMap<String, RpcFutureTask> futures = new ConcurrentHashMap<>();
    private volatile Channel channel;

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        channel = ctx.channel();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse response) throws Exception {
        String requestId = response.getRequestId();
        RpcFutureTask task = futures.get(requestId);
        if(task != null) {
            futures.remove(requestId);
            task.getResponse(response);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("client caught exception", cause);
        ctx.close();
    }

    public RpcFutureTask sendRequest(RpcRequest request){
        final CountDownLatch latch = new CountDownLatch(1);
        RpcFutureTask task = new RpcFutureTask(request);
        futures.put(request.getRequestId(), task);
        channel.writeAndFlush(request).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                latch.countDown();
            }
        });
        try {
            latch.wait();
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        }
        return task;
    }
}

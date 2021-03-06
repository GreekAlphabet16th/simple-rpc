package com.lyzhou.rpcclient;

import com.lyzhou.rpccommon.protocol.RpcRequest;
import com.lyzhou.rpccommon.protocol.RpcResponse;
import com.lyzhou.rpcregistry.ServiceDiscovery;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * RPC动态代理
 * @author zhouliyu
 * */
public class RpcProxy {
    private String serverAddress;
    private ServiceDiscovery serviceDiscovery;

    public RpcProxy(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    @SuppressWarnings("unchecked")
    public <T> T create(Class<?> interfaceClass){
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[]{interfaceClass},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        RpcRequest request = new RpcRequest(); // 创建并初始化RPC请求
                        request.setRequestId(UUID.randomUUID().toString());
                        request.setClassName(method.getDeclaringClass().getName());
                        request.setMethodName(method.getName());
                        request.setParameterTypes(method.getParameterTypes());
                        request.setParameters(args);

                        if (serviceDiscovery != null) {
                            serverAddress = serviceDiscovery.discover(); // 发现服务
                        }

                        if(serverAddress != null) {
                            String[] array = serverAddress.split(":");
                            String host = array[0];
                            int port = Integer.parseInt(array[1]);

                            RpcClient client = new RpcClient(host, port); // 初始化 RPC 客户端
                            RpcResponse response = client.send(request); // 通过 RPC客户端发送RPC请求并获取RPC响应
                            if (response.isError()) {
                                throw new RuntimeException("Response error.",new Throwable(response.getError()));
                            } else {
                                return response.getResult();
                            }
                        }else {
                            throw new RuntimeException("No serverAddress found");
                        }
                    }

                });
    }
}

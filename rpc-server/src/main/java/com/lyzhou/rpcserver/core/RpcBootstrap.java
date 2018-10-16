package com.lyzhou.rpcserver.core;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * RPC服务启动入口
 * @author zhouliyu
 */
public class RpcBootstrap {
	@SuppressWarnings("resource")
	public static void main(String[] args) {
		new ClassPathXmlApplicationContext("spring-server.xml");
	}
}

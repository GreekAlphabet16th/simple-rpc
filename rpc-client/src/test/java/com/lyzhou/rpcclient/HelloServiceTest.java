package com.lyzhou.rpcclient;

import com.lyzhou.rpccommon.service.HelloService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:spring-client.xml")
public class HelloServiceTest {

    @Autowired
    private RpcProxy rpcProxy;

    @Test
    public void helloText() {
        HelloService helloService = rpcProxy.create(HelloService.class);
        String result = helloService.hello("lyzhou");
        System.out.println(result);
    }
}

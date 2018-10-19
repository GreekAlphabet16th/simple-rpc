## simple-rpc

> 简单的rpc通信

- zookeeper
```
用于服务的注册与发现
```
- netty
```
用于协议解析、编码与NIO
```
- protostuff

```
用于序列化/反序列化POJO
```

### 更新

#### server
- handler处理操作，添加多线程异步操作

> Netty4中的Handler处理在IO线程中，如果Handler处理中有耗时的操作（如数据库相关），会让IO线程等待，影响性能。

#### client
- 客户端异步处理,不需要同步等待

> 发送一个异步请求，返回Feature，通过Feature的callback机制获取结果。

- 客户端使用长连接（多次调用共享连接）
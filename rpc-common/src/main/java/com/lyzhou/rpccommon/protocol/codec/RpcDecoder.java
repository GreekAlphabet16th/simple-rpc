package com.lyzhou.rpccommon.protocol.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;
/**
 * RPC消息解析
 * 消息格式：消息体长度+消息体
 * @author zhouliyu
 * */
public class RpcDecoder extends ByteToMessageDecoder {

    private Class<?> genericClass;

    public RpcDecoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> list) throws Exception {
        if(in.readableBytes() < 4){
            return;
        }
        //调用mark操作，将当前位置指针本分到mark中，调用rest后，重新将指针的当前位置恢复到mark位置
        in.markReaderIndex(); //标记
        int dataLength = in.readInt();
        if(dataLength < 0){
           ctx.close();
        }
        if(in.readableBytes() < dataLength){
            in.resetReaderIndex(); //回滚
        }
        byte[] data = new byte[dataLength];
        in.readBytes(data);

        //Object反序列化
        Object obj = SerializationUtil.deserialize(data, genericClass);
        list.add(obj);
    }
}

/**
 * Project Name: my-projects
 * Package Name: com.taotao.rpc.server
 * Date: 2020/2/27 14:40
 * Author: dengtao
 */
package com.taotao.rpc.server;

import com.taotao.rpc.common.RpcReponse;
import com.taotao.rpc.common.RpcRequest;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * rpc 核心处理器<br>
 *
 * @author dengtao
 * @version v1.0.0
 * @create 2020/2/27 14:40
 */
public class RpcHandler extends SimpleChannelInboundHandler<RpcRequest> {
    public static final Logger logger = LoggerFactory.getLogger(RpcHandler.class);
    private Map<String, Object> handlerMap;

    public RpcHandler(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }

    /**
     * 接受消息 处理消息 返回结果
     *
     * @param ctx     ctx
     * @param request request
     * @return void
     * @author dengtao
     * @date 2020/2/27 14:42
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest request) throws Exception {
        RpcReponse response = new RpcReponse();
        response.setRequestId(request.getRequestId());

        try {
            Object obj = doRequestHandle(request);
            response.setResult(obj);
        } catch (Throwable e) {
            response.setErrorMsg(e);
        }

        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * 处理请求
     *
     * @param request request
     * @return java.lang.Object
     * @author dengtao
     * @date 2020/2/27 14:46
     */
    private Object doRequestHandle(RpcRequest request) throws Throwable {
        String className = request.getClassName();
        Object handler = handlerMap.get(className);

        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();

        Class<?> clazz = Class.forName(className);
        Method method = clazz.getMethod(methodName, parameterTypes);
        return method.invoke(handler, parameters);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("处理异常");
        ctx.close();
    }
}

/**
 * Project Name: my-projects
 * Package Name: com.taotao.rpc.client
 * Date: 2020/2/27 15:14
 * Author: dengtao
 */
package com.taotao.rpc.client;

import com.taotao.rpc.common.RpcDecoder;
import com.taotao.rpc.common.RpcEncoder;
import com.taotao.rpc.common.RpcReponse;
import com.taotao.rpc.common.RpcRequest;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * client<br>
 *
 * @author dengtao
 * @version v1.0.0
 * @create 2020/2/27 15:14
 */
public class RpcClient extends SimpleChannelInboundHandler<RpcReponse> {

    private RpcReponse response;
    private final Object object = new Object();
    private String ip;
    private int port;

    public RpcClient(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    /**
     * 发送数据
     *
     * @param request request
     * @return com.taotao.rpc.common.RpcReponse
     * @author dengtao
     * @date 2020/2/27 15:19
     */
    public RpcReponse send(RpcRequest request) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();

        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap
                    .group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new RpcEncoder(RpcRequest.class))
                                    .addLast(new RpcDecoder(RpcReponse.class))
                                    .addLast(RpcClient.this);
                        }
                    })
                    .option(ChannelOption.SO_KEEPALIVE, true);

            ChannelFuture channelFuture = bootstrap.connect(ip, port).sync();
            // 发送request请求
            channelFuture.channel().writeAndFlush(request).sync();

            // 等待讲数据读取完毕 阻塞线程
            synchronized (object) {
                object.wait();
            }

            if (null != response) {
                channelFuture.channel().closeFuture().sync();
            }

            return response;
        } finally {
            group.shutdownGracefully();
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcReponse response) throws Exception {
        this.response = response;
        // 当数据获取完毕 通知主线程释放
        synchronized (object) {
            object.notifyAll();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}

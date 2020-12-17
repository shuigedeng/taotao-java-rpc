/**
 * Project Name: my-projects
 * Package Name: com.taotao.rpc.client
 * Date: 2020/2/27 15:08
 * Author: dengtao
 */
package com.taotao.rpc.client;

import com.taotao.rpc.common.RpcReponse;
import com.taotao.rpc.common.RpcRequest;
import com.taotao.rpc.registry.ServiceDiscovery;

import java.lang.reflect.Proxy;
import java.util.UUID;

/**
 * <br>
 *
 * @author dengtao
 * @version v1.0.0
 * @create 2020/2/27 15:08
 */
public class RpcProxy {
    private String serverAddress;
    private ServiceDiscovery serviceDiscovery;

    public RpcProxy(String serverAddress) {
        this.serverAddress = serverAddress;
    }

    public RpcProxy(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
    }

    /**
     * 创建客户端代理
     *
     * @param interfaceClass interfaceClass
     * @return T
     * @author dengtao
     * @date 2020/2/27 15:09
     */
    public <T> T create(Class<?> interfaceClass) {
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                (proxy, method, args) -> {
                    RpcRequest rpcRequest = new RpcRequest();
                    rpcRequest.setRequestId(UUID.randomUUID().toString());
                    rpcRequest.setClassName(method.getDeclaringClass().getName());
                    rpcRequest.setMethodName(method.getName());
                    rpcRequest.setParameterTypes(method.getParameterTypes());
                    rpcRequest.setParameters(args);

                    if (null != serviceDiscovery) {
                        serverAddress = serviceDiscovery.discover();
                    }

                    String[] array = serverAddress.split(":");
                    String ip = array[0];
                    int port = Integer.parseInt(array[1]);

                    RpcClient client = new RpcClient(ip, port);
                    RpcReponse rpcReponse = client.send(rpcRequest);

                    if (rpcReponse.isError()) {
                        throw rpcReponse.getErrorMsg();
                    } else {
                        return rpcReponse.getResult();
                    }
                }
        );
    }
}

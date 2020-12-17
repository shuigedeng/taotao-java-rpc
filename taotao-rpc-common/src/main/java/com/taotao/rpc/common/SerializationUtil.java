/**
 * Project Name: my-projects
 * Package Name: com.taotao.rpc.common
 * Date: 2020/2/27 10:44
 * Author: dengtao
 */
package com.taotao.rpc.common;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtobufIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;
import org.objenesis.Objenesis;
import org.objenesis.ObjenesisStd;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 序列化utils
 *
 * @author dengtao
 * @version v1.0.0
 * @create 2020/2/27 10:44
 */
public class SerializationUtil {
    private static Map<Class<?>, Schema<?>> cachedMap = new ConcurrentHashMap<>();
    private static Objenesis objenesis = new ObjenesisStd(true);

    /**
     * 获取类型的 Schema
     *
     * @param clazz 类型
     * @return com.dyuproject.protostuff.Schema<T>
     * @author dengtao
     * @date 2020/2/27 11:03
     */
    public static <T> Schema<T> getSchema(Class<T> clazz) {
        Schema<T> schema = (Schema<T>) cachedMap.get(clazz);
        if (Objects.isNull(schema)) {
            schema = RuntimeSchema.createFrom(clazz);
            cachedMap.put(clazz, schema);
        }
        return schema;
    }

    /**
     * 序列号对象
     *
     * @param obj 类型
     * @return byte[]
     * @author dengtao
     * @date 2020/2/27 11:02
     */
    public static <T> byte[] serialize(T obj) throws IllegalStateException {
        Class<T> clazz = (Class<T>) obj.getClass();
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            Schema<T> schema = getSchema(clazz);
            return ProtobufIOUtil.toByteArray(obj, schema, buffer);
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        } finally {
            buffer.clear();
        }
    }

    /**
     * 反序列化对象
     *
     * @param data  数据
     * @param clazz 类型
     * @return T 类
     * @author dengtao
     * @date 2020/2/27 11:03
     */
    public static <T> T deserialize(byte[] data, Class<T> clazz) throws IllegalStateException {
        try {
            T message = objenesis.newInstance(clazz);
            Schema<T> schema = getSchema(clazz);
            ProtobufIOUtil.mergeFrom(data, message, schema);
            return message;
        } catch (Exception e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}

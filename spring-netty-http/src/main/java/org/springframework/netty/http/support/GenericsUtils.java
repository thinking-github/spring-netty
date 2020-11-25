package org.springframework.netty.http.support;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-03-16
 */
public class GenericsUtils {

    private static final Logger log = LoggerFactory.getLogger(GenericsUtils.class);

    public static Class<?> getInterfaceGenericType(Class<?> clazz, int indexInterface, int index) {
        Type[] genericInterfaces = clazz.getGenericInterfaces();
        if (indexInterface >= genericInterfaces.length || indexInterface < 0) {
            throw new IllegalArgumentException("index: " + index + ", size of " + clazz.getSimpleName() + "'s interfaces: " + genericInterfaces.length);
        }
        Type genType = genericInterfaces[indexInterface];
        if (!(genType instanceof ParameterizedType)) {
            //log.warn(clazz.getSimpleName() + "'s interfaces not ParameterizedType");
            return Object.class;
        }

        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        if (index >= params.length || index < 0) {
            throw new IllegalArgumentException("index: " + index + ", size of " + clazz.getSimpleName() + "'s Parameterized Type: " + params.length);
        }

        Type type = params[index];
        //public class ClickController implements HttpRequestHandler<Map<String,String>>
        if ((type instanceof ParameterizedType)) {
            Type rawType = ((ParameterizedType) type).getRawType();
            return (Class<?>) rawType;
        }

        //public class ClickController implements HttpRequestHandler<ClickTracking>
        if ((type instanceof Class)) {
            return (Class<?>)type;
        }
        return Object.class;
    }
}

package org.springframework.netty.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ResolvableType;
import org.springframework.netty.http.support.GenericsUtils;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-03-16
 */

public class Demo {

    private static final Logger log = LoggerFactory.getLogger(Demo.class);

    public static void main(String[] args) {

        ResolvableType resolvedType = ResolvableType.forType(PostRequestController.class);


        Class cc = GenericsUtils.getInterfaceGenericType(PostRequestController.class, 0, 0);
        Class cc1 = GenericsUtils.getInterfaceGenericType(HelloRequestController.class, 0, 0);

        System.out.println("--");

        System.out.println(String.format("{\"code\": %s,\"message\": \"%s\",\"exception\": \"%s\"}",0,null,""));
    }


}

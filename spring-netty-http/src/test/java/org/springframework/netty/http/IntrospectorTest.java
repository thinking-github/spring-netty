package org.springframework.netty.http;

import java.beans.Introspector;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-04-24
 */
public class IntrospectorTest {


    public static void main(String[] args) {
        System.out.println(Introspector.decapitalize("Javabean"));
    }

}

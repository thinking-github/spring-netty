package org.springframework.netty.http.utils;

import org.junit.Test;
import org.springframework.netty.http.util.CountSampling;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-08-18
 */
public class CountSamplingTest {

    @Test
    public void countSampling() {
        CountSampling countSampling = new CountSampling(8, 10);
        for (int i = 0; i < 120; i++) {
            System.out.println(i + ":" + countSampling.next() + " ");
        }
    }


    @Test
    public void countMov() {

        System.out.println(104 % 8);
        System.out.println(112 % 8);
    }
}

package org.springframework.netty.http.util;

import io.netty.util.internal.PlatformDependent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-04-23
 */
public class NettyMetricUtils {

    private static final Logger logger = LoggerFactory.getLogger(NettyMetricUtils.class);
    private static AtomicLong directMemory;

    static {
        try {
            PlatformDependent.usedDirectMemory();
        } catch (NoSuchMethodError e) {
            logger.warn("PlatformDependent class No Such Method usedDirectMemory. " + e.getMessage());
            Field field = ReflectionUtils.findField(PlatformDependent.class, "DIRECT_MEMORY_COUNTER");
            field.setAccessible(true);
            try {
                directMemory = (AtomicLong) field.get(PlatformDependent.class);
            } catch (IllegalAccessException ex) {

            }
        }
    }

    /**
     * netty  < 4.1.35
     *
     * @return
     */
    public static long usedDirectMemory() {
        //netty  < 4.1.35
        if (directMemory != null) {
            return directMemory.get();
        }

        // netty  >= 4.1.35
        long used = PlatformDependent.usedDirectMemory();
        return used;
    }


}

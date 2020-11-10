package org.springframework.netty.http.util;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-08-18
 */
public class CountSampling {

    /**
     * 执行计数
     */
    private AtomicLong count = new AtomicLong(0);

    //There is a minor performance benefit in TLR if this is a power of 2.
    /**
     * 抽样间隔
     */
    private int interval = 8;


    /**
     * 最小抽样 超过该数才启动抽样开始 ; < 0 关闭抽样
     */
    private int min = 10000;

    public CountSampling(int interval, int min) {
        this.interval = interval;
        this.min = min;
    }

    public CountSampling() {
    }

    private static boolean isPowerOfTwo(int val) {
        return (val & -val) == val;
    }


    public long getCount() {
        return count.get();
    }

    /**
     * 返回true 匹配采样
     *
     * @return
     */
    public boolean next() {
        if (min < 0 || count.get() < min) {
            count.getAndIncrement();
            return true;
        }
        return (count.getAndIncrement() & interval - 1) == 0;
    }


    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        if (isPowerOfTwo(interval)) {
            this.interval = interval;
        } else {
            this.interval = 8;
        }
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

}

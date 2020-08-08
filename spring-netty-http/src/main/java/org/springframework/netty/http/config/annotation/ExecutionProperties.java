package org.springframework.netty.http.config.annotation;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-03-23
 */
public class ExecutionProperties {
    /**
     * Prefix to use for the names of newly created threads.
     * http-nio-
     */
    private String threadNamePrefix = "nio-";

    /**
     * Queue capacity. An unbounded capacity does not increase the pool and therefore
     * ignores the "max-size" property.
     */
    private int queueCapacity = 10000;

    /**
     * Core number of threads.
     */
    private int coreSize = 8;

    /**
     * Maximum allowed number of threads. If tasks are filling up the queue, the pool
     * can expand up to that size to accommodate the load. Ignored if the queue is
     * unbounded.
     */
    private int maxSize = 400;

    /**
     * Whether core threads are allowed to time out. This enables dynamic growing and
     * shrinking of the pool.
     */
    private boolean allowCoreThreadTimeout = true;

    /**
     * Time limit for which threads may remain idle before being terminated.
     */
    private int keepAlive = 60;


    public String getThreadNamePrefix() {
        return this.threadNamePrefix;
    }

    public void setThreadNamePrefix(String threadNamePrefix) {
        this.threadNamePrefix = threadNamePrefix;
    }

    public int getQueueCapacity() {
        return this.queueCapacity;
    }

    public void setQueueCapacity(int queueCapacity) {
        this.queueCapacity = queueCapacity;
    }

    public int getCoreSize() {
        return this.coreSize;
    }

    public void setCoreSize(int coreSize) {
        this.coreSize = coreSize;
    }

    public int getMaxSize() {
        return this.maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    public boolean isAllowCoreThreadTimeout() {
        return this.allowCoreThreadTimeout;
    }

    public void setAllowCoreThreadTimeout(boolean allowCoreThreadTimeout) {
        this.allowCoreThreadTimeout = allowCoreThreadTimeout;
    }

    public int getKeepAlive() {
        return this.keepAlive;
    }

    public void setKeepAlive(int keepAlive) {
        this.keepAlive = keepAlive;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ExecutionProperties{");
        sb.append("threadNamePrefix='").append(threadNamePrefix).append('\'');
        sb.append(", queueCapacity=").append(queueCapacity);
        sb.append(", coreSize=").append(coreSize);
        sb.append(", maxSize=").append(maxSize);
        sb.append(", allowCoreThreadTimeout=").append(allowCoreThreadTimeout);
        sb.append(", keepAlive=").append(keepAlive);
        sb.append('}');
        return sb.toString();
    }


}

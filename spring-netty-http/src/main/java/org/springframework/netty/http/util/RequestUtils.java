package org.springframework.netty.http.util;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-09-01
 */
public abstract class RequestUtils {

    /**
     * 获取Http请求的源IP。
     *
     * @param request http请求。
     * @return 对应的IP地址。
     */
    public static String getRemoteIp(FullHttpRequest request) {
        if (request == null) {
            return "request is null. unknown";
        }

        HttpHeaders httpHeaders = request.headers();
        // 集群环境下负载均衡器的’x-forwarded-for‘的属性值应该设置为on否则只能获得代理服务器的ip不是客户端真实的ip
        String ip = null;
        String ips = httpHeaders.get("x-forwarded-for");
        if (ips != null && ips.trim().length() > 0) {
            String[] array = ips.split(",");
            ip = array[0].trim();
        }
        //apache http
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = httpHeaders.get("Proxy-Client-IP");
        }
        // WebLogic web server proxy plug-in  WebLogic plugin Enabled
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = httpHeaders.get("WL-Proxy-Client-IP");
        }
        //其他代理服务器
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip.trim())) {
            ip = httpHeaders.get("HTTP_CLIENT_IP");
        }
        //nginx XXX:2016-05-08
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = httpHeaders.get("X-Real-IP");
        }

        return ip;
    }


}

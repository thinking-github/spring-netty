package org.springframework.boot.actuate.netty.endpoint;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import org.nbone.mvc.rest.ApiResponse;
import org.springframework.boot.actuate.endpoint.LoggersEndpoint;
import org.springframework.boot.actuate.endpoint.mvc.MvcEndpoint;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.netty.http.HandlerMapping;
import org.springframework.netty.http.HttpRestHandler;
import org.springframework.netty.http.codec.QueryDecoder;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-08-25
 */
@Controller
@RequestMapping(value = "/loggers", produces = MediaType.APPLICATION_JSON_VALUE)
public class LoggersMvcEndpoint extends HttpRestHandler<Map<String, String>> {

    private static final String LOGGER_TAG = "logging.level.";

    @Resource
    private LoggersEndpoint loggersEndpoint;

    @ResponseBody
    public Object get(@PathVariable String name) {
        if (!this.loggersEndpoint.isEnabled()) {
            // Shouldn't happen - MVC endpoint shouldn't be registered when delegate's
            // disabled
            return MvcEndpoint.DISABLED_RESPONSE;
        }
        LoggersEndpoint.LoggerLevels levels = this.loggersEndpoint.invoke(name);
        return (levels != null) ? levels : ResponseEntity.notFound().build();
    }

    @ResponseBody
    public Object set(@PathVariable String name, @RequestBody Map<String, String> configuration) {
        if (!this.loggersEndpoint.isEnabled()) {
            // Shouldn't happen - MVC endpoint shouldn't be registered when delegate's
            // disabled
            return MvcEndpoint.DISABLED_RESPONSE;
        }
        LogLevel logLevel = getLogLevel(configuration);
        this.loggersEndpoint.setLogLevel(name, logLevel);
        return ResponseEntity.ok().build();
    }

    private LogLevel getLogLevel(Map<String, String> configuration) {
        String level = configuration.get("configuredLevel");
        try {
            return (level != null) ? LogLevel.valueOf(level.toUpperCase(Locale.ENGLISH))
                    : null;
        } catch (IllegalArgumentException ex) {
            throw new org.springframework.boot.actuate.endpoint.mvc.LoggersMvcEndpoint.InvalidLogLevelException(level);
        }
    }


    private LogLevel getLogLevel(String level) {
        try {
            return (level != null) ? LogLevel.valueOf(level.toUpperCase(Locale.ENGLISH))
                    : null;
        } catch (IllegalArgumentException ex) {
            throw new org.springframework.boot.actuate.endpoint.mvc.LoggersMvcEndpoint.InvalidLogLevelException(level);
        }
    }


    // set logger level
    @Override
    public <R> R post(ChannelHandlerContext ctx, FullHttpRequest request, Map<String, String> configuration)
            throws IOException {
        QueryDecoder queryDecoder = ctx.channel().attr(HandlerMapping.REQUEST_QUERY_URI).get();
        String level = queryDecoder.getParameter("level");
        String[] nameLevel = StringUtils.delimitedListToStringArray(level, "=");
        String name = nameLevel[0].replaceAll(LOGGER_TAG, "");
        LogLevel logLevel = getLogLevel(nameLevel[1]);
        this.loggersEndpoint.setLogLevel(name, logLevel);
        return (R) ApiResponse.success();
    }

    @Override
    public <R> R get(ChannelHandlerContext ctx, FullHttpRequest request) throws IOException {
        QueryDecoder queryDecoder = ctx.channel().attr(HandlerMapping.REQUEST_QUERY_URI).get();
        String name = queryDecoder.getParameter("name");
        if (StringUtils.hasLength(name)) {
            return (R) get(name.replaceAll(LOGGER_TAG, ""));
        }
        return (R) loggersEndpoint.invoke();
    }

    /**
     * Exception thrown when the specified log level cannot be found.
     */
    @SuppressWarnings("serial")
    @ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "No such log level")
    public static class InvalidLogLevelException extends RuntimeException {

        public InvalidLogLevelException(String level) {
            super("Log level '" + level + "' is invalid");
        }

    }


}

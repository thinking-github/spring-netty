package org.springframework.netty.http.mvc;

import org.springframework.netty.http.converter.HttpMessageConverter;

import java.util.List;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-03-21
 */
public class MessageConverterMethodProcessor extends AbstractMessageConverterMethodProcessor {


    public MessageConverterMethodProcessor(List<HttpMessageConverter<?>> converters) {
        super(converters);
    }
}

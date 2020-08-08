package org.springframework.netty.http.converter.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-03-15
 */
public class MappingJackson2HttpMessageConverter extends AbstractJackson2HttpMessageConverter {

    /**
     * Construct a new {@link MappingJackson2HttpMessageConverter} with a custom {@link ObjectMapper}.
     * You can use  to build it easily.
     */
    public MappingJackson2HttpMessageConverter(ObjectMapper objectMapper) {
        super(objectMapper, MediaType.APPLICATION_JSON, new MediaType("application", "*+json"));
    }


}

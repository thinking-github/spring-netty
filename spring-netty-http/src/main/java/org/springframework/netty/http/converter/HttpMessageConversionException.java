package org.springframework.netty.http.converter;

/**
 *
 * @author thinking
 * @version 1.0
 * @since 2020-03-15
 */
public class HttpMessageConversionException extends RuntimeException {

    /**
     * Create a new HttpMessageConversionException.
     * @param msg the detail message
     */
    public HttpMessageConversionException(String msg) {
        super(msg);
    }

    /**
     * Create a new HttpMessageConversionException.
     * @param msg the detail message
     * @param cause the root cause (if any)
     */
    public HttpMessageConversionException(String msg, Throwable cause) {
        super(msg, cause);
    }


}

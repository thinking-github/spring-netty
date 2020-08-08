package org.springframework.netty.http.converter;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-03-15
 */
public class HttpMessageNotWritableException extends HttpMessageConversionException {

    /**
     * Create a new HttpMessageNotWritableException.
     *
     * @param msg the detail message
     */
    public HttpMessageNotWritableException(String msg) {
        super(msg);
    }

    /**
     * Create a new HttpMessageNotWritableException.
     *
     * @param msg   the detail message
     * @param cause the root cause (if any)
     */
    public HttpMessageNotWritableException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
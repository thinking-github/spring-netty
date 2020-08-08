package org.springframework.netty.http.converter;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-03-15
 */
public class HttpMessageNotReadableException extends HttpMessageConversionException {


    /**
     * Create a new HttpMessageNotReadableException.
     * @param msg the detail message
     */
    public HttpMessageNotReadableException(String msg) {
        super(msg);
    }

    /**
     * Create a new HttpMessageNotReadableException.
     * @param msg the detail message
     * @param cause the root cause (if any)
     */
    public HttpMessageNotReadableException(String msg, Throwable cause) {
        super(msg, cause);
    }


}

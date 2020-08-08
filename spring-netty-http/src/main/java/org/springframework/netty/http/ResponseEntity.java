package org.springframework.netty.http;

import io.netty.handler.codec.http.HttpResponseStatus;
import org.springframework.http.HttpEntity;
import org.springframework.util.MultiValueMap;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-03-20
 */
public class ResponseEntity<T> extends HttpEntity<T> {

    private HttpResponseStatus status;

    public ResponseEntity(HttpResponseStatus status) {
        this.status = status;
    }

    public ResponseEntity(T body, HttpResponseStatus status) {
        super(body);
        this.status = status;
    }

    public ResponseEntity(MultiValueMap<String, String> headers, HttpResponseStatus status) {
        super(headers);
        this.status = status;
    }

    public ResponseEntity(T body, MultiValueMap<String, String> headers, HttpResponseStatus status) {
        super(body, headers);
        this.status = status;
    }



    public HttpResponseStatus getStatus() {
        return status;
    }

    public void setStatus(HttpResponseStatus status) {
        this.status = status;
    }
}

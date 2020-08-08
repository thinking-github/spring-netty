package org.springframework.netty.http;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpUtil;
import org.springframework.http.MediaType;

import java.util.Collections;
import java.util.List;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-03-15
 */
public class HttpUtils {

    /**
     * Fetch MIME type part from message's Content-Type header as a char sequence.
     *
     * @param headers entity to fetch Content-Type header from
     * @return the MIME type as a {@code CharSequence} from message's Content-Type header
     * or {@code null} if content-type header or MIME type part of this header are not presented
     * <p/>
     * "content-type: text/html; charset=utf-8" - "text/html" will be returned <br/>
     * "content-type: text/html" - "text/html" will be returned <br/>
     * "content-type: " or no header - {@code null} we be returned
     */
    public static CharSequence getMimeType(HttpHeaders headers) {
        CharSequence contentTypeValue = headers.get(HttpHeaderNames.CONTENT_TYPE);
        if (contentTypeValue != null) {
            return HttpUtil.getMimeType(contentTypeValue);
        } else {
            return null;
        }
    }


    public static MediaType getContentType(HttpMessage message) {
        CharSequence contentTypeValue = message.headers().get(HttpHeaderNames.CONTENT_TYPE);
        if (contentTypeValue != null) {
            return MediaType.parseMediaType(contentTypeValue.toString());
        } else {
            return null;
        }
    }

    /**
     * Sets the {@code "Content-Type"} header.
     */
    public static void setContentType(HttpMessage message, String contentType) {
        message.headers().set(HttpHeaderNames.CONTENT_TYPE, contentType);
    }


    /**
     * Sets the {@code "Content-Type"} header.
     */
    public static void setContentType(HttpHeaders headers, String contentType) {
        headers.set(HttpHeaderNames.CONTENT_TYPE, contentType);
    }

    /**
     * Sets the {@code "Content-Length"} header.
     */
    public static void setContentLength(HttpHeaders headers, long length) {
        headers.set(HttpHeaderNames.CONTENT_LENGTH, length);
    }

    public static void setContentLength(HttpHeaders headers, Long length) {
        headers.set(HttpHeaderNames.CONTENT_LENGTH, length);
    }


    public static long getContentLength(HttpHeaders headers) {
        String value = headers.get(HttpHeaderNames.CONTENT_LENGTH);
        return (value != null ? Long.parseLong(value) : -1);
    }



    public static MediaType getAccept(HttpMessage httpMessage) {
        CharSequence accept = httpMessage.headers().get(HttpHeaderNames.ACCEPT);
        if (accept != null) {
            return MediaType.parseMediaType(accept.toString());
        } else {
            return null;
        }
    }
    public static List<MediaType> getAccepts(HttpMessage httpMessage) {
        CharSequence accept = httpMessage.headers().get(HttpHeaderNames.ACCEPT);

        if (accept != null) {
            List<MediaType> mediaTypes = MediaType.parseMediaTypes(accept.toString());
            MediaType.sortBySpecificityAndQuality(mediaTypes);
            return mediaTypes;
        } else {
            return Collections.emptyList();
        }
    }


}

package org.springframework.netty.http;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.util.IllegalReferenceCountException;

import java.io.IOException;

import static io.netty.util.internal.ObjectUtil.checkNotNull;

/**
 * Default implementation of a {@link FullHttpResponse}.
 *
 * @author thinking
 * @version 1.0
 * @since 2020-03-20
 */
public class HttpResponseImpl extends DefaultHttpResponse implements FullHttpResponse,HttpOutputMessage {

    private  ByteBuf content;
    private final HttpHeaders trailingHeaders;

    /**
     * Used to cache the value of the hash code and avoid {@link IllegalReferenceCountException}.
     */
    private int hash;

    public HttpResponseImpl() {
        this(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
    }
    public HttpResponseImpl(HttpResponseStatus status) {
        this(HttpVersion.HTTP_1_1, status == null ? HttpResponseStatus.OK : status);
    }

    public HttpResponseImpl(HttpVersion version, HttpResponseStatus status) {
        this(version, status, Unpooled.EMPTY_BUFFER);
    }

    public HttpResponseImpl(HttpVersion version, HttpResponseStatus status, ByteBuf content) {
        this(version, status, content, true);
    }

    public HttpResponseImpl(HttpVersion version, HttpResponseStatus status, boolean validateHeaders) {
        this(version, status, Unpooled.EMPTY_BUFFER, validateHeaders, false);
    }

    public HttpResponseImpl(HttpVersion version, HttpResponseStatus status, boolean validateHeaders,
                                   boolean singleFieldHeaders) {
        this(version, status, Unpooled.EMPTY_BUFFER, validateHeaders, singleFieldHeaders);
    }

    public HttpResponseImpl(HttpVersion version, HttpResponseStatus status,
                                   ByteBuf content, boolean validateHeaders) {
        this(version, status, content, validateHeaders, false);
    }

    public HttpResponseImpl(HttpVersion version, HttpResponseStatus status,
                                   ByteBuf content, boolean validateHeaders, boolean singleFieldHeaders) {
        super(version, status, validateHeaders, singleFieldHeaders);
        this.content = checkNotNull(content, "content");
        this.trailingHeaders = singleFieldHeaders ? new CombinedHttpHeaders(validateHeaders)
                : new DefaultHttpHeaders(validateHeaders);
    }

    public HttpResponseImpl(HttpVersion version, HttpResponseStatus status,
                                   ByteBuf content, HttpHeaders headers, HttpHeaders trailingHeaders) {
        super(version, status, headers);
        this.content = checkNotNull(content, "content");
        this.trailingHeaders = checkNotNull(trailingHeaders, "trailingHeaders");
    }

    @Override
    public HttpHeaders trailingHeaders() {
        return trailingHeaders;
    }

    @Override
    public ByteBuf content() {
        return content;
    }

    @Override
    public int refCnt() {
        return content.refCnt();
    }

    @Override
    public FullHttpResponse retain() {
        content.retain();
        return this;
    }

    @Override
    public FullHttpResponse retain(int increment) {
        content.retain(increment);
        return this;
    }

    @Override
    public FullHttpResponse touch() {
        content.touch();
        return this;
    }

    @Override
    public FullHttpResponse touch(Object hint) {
        content.touch(hint);
        return this;
    }

    @Override
    public boolean release() {
        return content.release();
    }

    @Override
    public boolean release(int decrement) {
        return content.release(decrement);
    }

    @Override
    public FullHttpResponse setProtocolVersion(HttpVersion version) {
        super.setProtocolVersion(version);
        return this;
    }

    @Override
    public FullHttpResponse setStatus(HttpResponseStatus status) {
        super.setStatus(status);
        return this;
    }

    @Override
    public FullHttpResponse copy() {
        return replace(content().copy());
    }

    @Override
    public FullHttpResponse duplicate() {
        return replace(content().duplicate());
    }

    @Override
    public FullHttpResponse retainedDuplicate() {
        return replace(content().retainedDuplicate());
    }

    @Override
    public FullHttpResponse replace(ByteBuf content) {
        FullHttpResponse response = new HttpResponseImpl(protocolVersion(), status(), content,
                headers().copy(), trailingHeaders().copy());
        response.setDecoderResult(decoderResult());
        return response;
    }

    @Override
    public int hashCode() {
        int hash = this.hash;
        if (hash == 0) {
            if (content().refCnt() != 0) {
                try {
                    hash = 31 + content().hashCode();
                } catch (IllegalReferenceCountException ignored) {
                    // Handle race condition between checking refCnt() == 0 and using the object.
                    hash = 31;
                }
            } else {
                hash = 31;
            }
            hash = 31 * hash + trailingHeaders().hashCode();
            hash = 31 * hash + super.hashCode();
            this.hash = hash;
        }
        return hash;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof HttpResponseImpl)) {
            return false;
        }

        HttpResponseImpl other = (HttpResponseImpl) o;

        return super.equals(other) &&
                content().equals(other.content()) &&
                trailingHeaders().equals(other.trailingHeaders());
    }

    @Override
    public String toString() {
        return new StringBuilder().append(protocolVersion()).append(" ").append(status()).toString();
    }



    @Override
    public ByteBuf getBody() throws IOException {
        return content;
    }

    @Override
    public void setBody(ByteBuf content) throws IOException {
        if (content == null) {
            return;
        }
        this.hash = 0;
        this.content = content;
    }
}


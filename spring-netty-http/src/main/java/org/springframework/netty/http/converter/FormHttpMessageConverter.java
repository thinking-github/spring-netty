package org.springframework.netty.http.converter;

import io.netty.handler.codec.http.FullHttpRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.netty.http.HttpOutputMessage;
import org.springframework.netty.http.HttpUtils;
import org.springframework.util.*;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.*;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-11-24
 */
public class FormHttpMessageConverter implements HttpMessageConverter<Map<String, ?>> {

    public static final Charset DEFAULT_CHARSET = Charset.forName("UTF-8");

    private List<MediaType> supportedMediaTypes = new ArrayList<MediaType>();

    private List<HttpMessageConverter<?>> partConverters = new ArrayList<HttpMessageConverter<?>>();

    private Charset charset = DEFAULT_CHARSET;

    private Charset multipartCharset;


    public FormHttpMessageConverter() {
        this.supportedMediaTypes.add(MediaType.APPLICATION_FORM_URLENCODED);
        //this.supportedMediaTypes.add(MediaType.MULTIPART_FORM_DATA);

        applyDefaultCharset();
    }


    /**
     * Set the list of {@link MediaType} objects supported by this converter.
     */
    public void setSupportedMediaTypes(List<MediaType> supportedMediaTypes) {
        this.supportedMediaTypes = supportedMediaTypes;
    }

    @Override
    public List<MediaType> getSupportedMediaTypes() {
        return Collections.unmodifiableList(this.supportedMediaTypes);
    }

    /**
     * Set the message body converters to use. These converters are used to
     * convert objects to MIME parts.
     */
    public void setPartConverters(List<HttpMessageConverter<?>> partConverters) {
        Assert.notEmpty(partConverters, "'partConverters' must not be empty");
        this.partConverters = partConverters;
    }

    /**
     * Add a message body converter. Such a converter is used to convert objects
     * to MIME parts.
     */
    public void addPartConverter(HttpMessageConverter<?> partConverter) {
        Assert.notNull(partConverter, "'partConverter' must not be null");
        this.partConverters.add(partConverter);
    }

    /**
     * Set the default character set to use for reading and writing form data when
     * the request or response Content-Type header does not explicitly specify it.
     * <p>By default this is set to "UTF-8". As of 4.3, it will also be used as
     * the default charset for the conversion of text bodies in a multipart request.
     * In contrast to this, {@link #setMultipartCharset} only affects the encoding of
     * <i>file names</i> in a multipart request according to the encoded-word syntax.
     */
    public void setCharset(Charset charset) {
        if (charset != this.charset) {
            this.charset = (charset != null ? charset : DEFAULT_CHARSET);
            applyDefaultCharset();
        }
    }

    /**
     * Apply the configured charset as a default to registered part converters.
     */
    private void applyDefaultCharset() {
        for (HttpMessageConverter<?> candidate : this.partConverters) {
            if (candidate instanceof AbstractHttpMessageConverter) {
                AbstractHttpMessageConverter<?> converter = (AbstractHttpMessageConverter<?>) candidate;
                // Only override default charset if the converter operates with a charset to begin with...
                if (converter.getDefaultCharset() != null) {
                    converter.setDefaultCharset(this.charset);
                }
            }
        }
    }

    /**
     * Set the character set to use when writing multipart data to encode file
     * names. Encoding is based on the encoded-word syntax defined in RFC 2047
     * and relies on {@code MimeUtility} from "javax.mail".
     * <p>If not set file names will be encoded as US-ASCII.
     *
     * @see <a href="http://en.wikipedia.org/wiki/MIME#Encoded-Word">Encoded-Word</a>
     * @since 4.1.1
     */
    public void setMultipartCharset(Charset charset) {
        this.multipartCharset = charset;
    }


    @Override
    public boolean canRead(Class<?> clazz, MediaType mediaType) {
        if (!Map.class.isAssignableFrom(clazz)) {
            return false;
        }
        if (mediaType == null) {
            return true;
        }
        for (MediaType supportedMediaType : getSupportedMediaTypes()) {
            // We can't read multipart....
            if (!supportedMediaType.equals(MediaType.MULTIPART_FORM_DATA) && supportedMediaType.includes(mediaType)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return false;
    }

    @Override
    public Map<String, ?> read(Class<? extends Map<String, ?>> clazz,
                               FullHttpRequest request) throws IOException, HttpMessageNotReadableException {

        MediaType contentType = HttpUtils.getContentType(request);
        Charset charset = (contentType.getCharset() != null ? contentType.getCharset() : this.charset);
        String body = request.content().toString(charset);

        String[] pairs = StringUtils.tokenizeToStringArray(body, "&");
        Map<String, ?> stringMap = null;
        if (MultiValueMap.class.isAssignableFrom(clazz)) {
            MultiValueMap<String, String> result = new LinkedMultiValueMap<String, String>(pairs.length);
            for (String pair : pairs) {
                int idx = pair.indexOf('=');
                if (idx == -1) {
                    result.add(URLDecoder.decode(pair, charset.name()), null);
                } else {
                    String name = URLDecoder.decode(pair.substring(0, idx), charset.name());
                    String value = URLDecoder.decode(pair.substring(idx + 1), charset.name());
                    result.add(name, value);
                }
            }
            stringMap = result;

        } else {
            Map<String, String> result = new LinkedHashMap<String, String>(pairs.length);
            for (String pair : pairs) {
                int idx = pair.indexOf('=');
                if (idx == -1) {
                    result.put(URLDecoder.decode(pair, charset.name()), null);
                } else {
                    String name = URLDecoder.decode(pair.substring(0, idx), charset.name());
                    String value = URLDecoder.decode(pair.substring(idx + 1), charset.name());
                    result.put(name, value);
                }
            }
            stringMap = result;
        }

        return stringMap;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void write(Map<String, ?> map, MediaType contentType, HttpOutputMessage outputMessage)
            throws IOException, HttpMessageNotWritableException {


    }


    /**
     * Generate a multipart boundary.
     * <p>This implementation delegates to
     * {@link MimeTypeUtils#generateMultipartBoundary()}.
     */
    protected byte[] generateMultipartBoundary() {
        return MimeTypeUtils.generateMultipartBoundary();
    }

    /**
     * Return an {@link HttpEntity} for the given part Object.
     *
     * @param part the part to return an {@link HttpEntity} for
     * @return the part Object itself it is an {@link HttpEntity},
     * or a newly built {@link HttpEntity} wrapper for that part
     */
    protected HttpEntity<?> getHttpEntity(Object part) {
        return (part instanceof HttpEntity ? (HttpEntity<?>) part : new HttpEntity<Object>(part));
    }


}

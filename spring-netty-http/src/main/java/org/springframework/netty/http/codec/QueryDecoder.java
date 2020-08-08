package org.springframework.netty.http.codec;

import io.netty.handler.codec.http.QueryStringDecoder;

import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-07-22
 */
public class QueryDecoder extends QueryStringDecoder {

    private Map<String, String> uriTemplateVariables;

    public QueryDecoder(String uri) {
        super(uri);
    }

    public QueryDecoder(String uri, boolean hasPath) {
        super(uri, hasPath);
    }

    public QueryDecoder(String uri, Charset charset) {
        super(uri, charset);
    }

    public QueryDecoder(String uri, Charset charset, boolean hasPath) {
        super(uri, charset, hasPath);
    }

    public QueryDecoder(String uri, Charset charset, boolean hasPath, int maxParams) {
        super(uri, charset, hasPath, maxParams);
    }


    public void setUriTemplateVariables(Map<String, String> uriTemplateVariables) {
        this.uriTemplateVariables = uriTemplateVariables;
    }

    // apache tomcat
    public String getParameter(final String name) {
        List<String> values = this.parameters().get(name);

        if (values != null) {
            if (values.size() == 0) {
                return "";
            }
            return values.get(0);
        } else {
            return null;
        }

    }

    // spring web RequestParamMapMethodArgumentResolver
    public Map<String, String> parameterMap() {

        Map<String, List<String>> parameterMap = this.parameters();
        Map<String, String> result = new LinkedHashMap<String, String>(parameterMap.size());
        for (Map.Entry<String, List<String>> entry : parameterMap.entrySet()) {
            if (entry.getValue().size() > 0) {
                result.put(entry.getKey(), entry.getValue().get(0));
            }
        }
        return result;
    }


    public Map<String, String> pathVariable() {
        return uriTemplateVariables;
    }

    public String pathVariable(String name) {
        if (uriTemplateVariables != null) {
            return uriTemplateVariables.get(name);
        }
        return null;
    }

}

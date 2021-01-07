package org.springframework.netty.http.codec;

import io.netty.handler.codec.http.QueryStringDecoder;

import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-07-22
 */
public class QueryDecoder extends QueryStringDecoder {

    private Map<String, String> uriTemplateVariables;

    private Map<String, String> bodyMap;

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

    public Map<String, String> getBodyMap() {
        return bodyMap;
    }

    public void setBodyMap(Map<String, String> bodyMap) {
        this.bodyMap = bodyMap;
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
            if (bodyMap != null) {
                return bodyMap.get(name);
            }
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
        if (bodyMap != null) {
            result.putAll(bodyMap);
        }
        return result;
    }

    /**
     * Return a map containing all parameters with the given prefix.
     * Maps single values to String and multiple values to String array.
     * <p>For example, with a prefix of "spring_", "spring_param1" and
     * "spring_param2" result in a Map with "param1" and "param2" as keys.
     *
     * @param prefix the beginning of parameter names
     *               (if this is null or the empty string, all parameters will match)
     * @return map containing request parameters <b>without the prefix</b>,
     * containing either a String or a String array as values
     */
    public Map<String, Object> getParametersStartingWith(String prefix) {
        Map<String, List<String>> parameterMap = this.parameters();
        Map<String, Object> params = new TreeMap<String, Object>();
        if (prefix == null) {
            prefix = "";
        }
        for (Map.Entry<String, List<String>> entry : parameterMap.entrySet()) {
            String paramName = entry.getKey();
            if ("".equals(prefix) || paramName.startsWith(prefix)) {
                String unprefixed = paramName.substring(prefix.length());
                List<String> values = parameterMap.get(paramName);
                if (values == null || values.size() == 0) {
                    // Do nothing, no values found at all.
                } else if (values.size() > 1) {
                    params.put(unprefixed, values);
                } else {
                    params.put(unprefixed, values.get(0));
                }
            }

        }
        return params;
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

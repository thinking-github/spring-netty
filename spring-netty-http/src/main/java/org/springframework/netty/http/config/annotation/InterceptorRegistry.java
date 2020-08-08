package org.springframework.netty.http.config.annotation;

import org.springframework.netty.http.HandlerInterceptor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author thinking
 * @version 1.0
 * @since 2020-03-16
 */
public class InterceptorRegistry {

    private final List<InterceptorRegistration> registrations = new ArrayList<InterceptorRegistration>();


    /**
     * Adds the provided {@link HandlerInterceptor}.
     *
     * @param interceptor the interceptor to add
     * @return An {@link InterceptorRegistration} that allows you optionally configure the
     * registered interceptor further for example adding URL patterns it should apply to.
     */
    public InterceptorRegistration addInterceptor(HandlerInterceptor interceptor) {
        InterceptorRegistration registration = new InterceptorRegistration(interceptor);
        this.registrations.add(registration);
        return registration;
    }


    /**
     * Return all registered interceptors.
     */
    protected List<Object> getInterceptors() {
        List<Object> interceptors = new ArrayList<Object>(this.registrations.size());
        for (InterceptorRegistration registration : this.registrations) {
            interceptors.add(registration.getInterceptor());
        }
        return interceptors;
    }

}

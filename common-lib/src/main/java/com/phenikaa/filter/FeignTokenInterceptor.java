package com.phenikaa.filter;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignTokenInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String token = RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes
                ? ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getHeader("Authorization")
                : null;
        if (token != null) {
            requestTemplate.header("Authorization", token);
        }
    }
}

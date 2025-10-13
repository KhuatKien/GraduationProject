package com.phenikaa.bookingService.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "sepay")
public class SepayProperties {
    private String account;
    private String bank;
    private String description;
    private String webhookSecret;
}

package com.phenikaa.notificationService.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Test configuration cho unit tests
 * Cung cấp mock executors cho testing
 */
@TestConfiguration
public class TestConfig {

    @Bean
    @Primary
    public Executor notificationExecutor() {
        return Executors.newFixedThreadPool(2);
    }

    @Bean
    @Primary
    public Executor batchExecutor() {
        return Executors.newFixedThreadPool(1);
    }

    @Bean
    @Primary
    public Executor emailExecutor() {
        return Executors.newFixedThreadPool(1);
    }
}

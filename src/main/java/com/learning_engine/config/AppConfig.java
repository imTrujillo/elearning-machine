package com.learning_engine.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AppConfig {
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Bean("wordpressWebClient")
    public WebClient wordpressWebClient(
            @Value("${wordpress.base-url}") String baseUrl,
            @Value("${wordpress.username}") String username,
            @Value("${wordpress.password}") String password
    ){
        return WebClient.builder().
                baseUrl(baseUrl)
                .defaultHeaders(h -> h.setBasicAuth(username, password.replace(" ","")))
                .codecs(c -> c.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                .build();
    }

    @Bean("wooWebClient")
    public WebClient wooWebClient(
            @Value("${wordpress.base-url}") String baseUrl,
            @Value("${woocommerce.consumer-key}") String key,
            @Value("${woocommerce.consumer-secret}") String secret
    ){
        return WebClient.builder().
                baseUrl(baseUrl)
                .defaultHeaders(h -> h.setBasicAuth(key, secret))
                .codecs(c -> c.defaultCodecs().maxInMemorySize(2 * 1024 * 1024))
                .build();
    }
}

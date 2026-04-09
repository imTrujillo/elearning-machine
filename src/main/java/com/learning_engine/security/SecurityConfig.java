package com.learning_engine.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sess ->
                        sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs/**",
                                "/api/woocommerce/webhook",
                                "/api/courses",
                                "/api/courses/{id}",
                                "/api/categories",
                                "/api/categories/**"
                        ).permitAll()

                        // Rutas que SÍ requieren token:
                        .requestMatchers("/api/my-courses").authenticated()
                        .requestMatchers("/api/enrollments/**").authenticated()
                        .requestMatchers("/api/enrollments/verify").authenticated()
                        .requestMatchers("/api/courses/*/modules/**").authenticated()
                        .requestMatchers("/api/modules/*/lessons/*/complete").authenticated()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
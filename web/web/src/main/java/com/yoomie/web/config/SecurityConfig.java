package com.yoomie.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // karena kamu pakai fetch JSON (bukan form login spring security)
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/", "/login", "/register", "/css/**", "/img/**", "/js/**"
                        ).permitAll()
                        .anyRequest().permitAll() // sementara; nanti bisa kamu ketatkan
                )
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}

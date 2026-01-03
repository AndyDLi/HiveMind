package com.andydli.hivemind.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.context.annotation.Bean;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {
    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login", "/register").permitAll() // public view endpoints
                        .requestMatchers("/api/users/register", "/api/users/login").permitAll() // public user endpoints
                        .anyRequest().authenticated()) // authenticated endpoints
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable()) // disable form login
                .httpBasic(httpBasic -> httpBasic.disable()); // disable HTTP Basic

        return http.build();
    }
}
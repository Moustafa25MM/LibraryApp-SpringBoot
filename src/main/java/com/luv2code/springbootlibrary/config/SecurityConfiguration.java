package com.luv2code.springbootlibrary.config;

import com.okta.spring.boot.oauth.Okta;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.accept.ContentNegotiationStrategy;
import org.springframework.web.accept.HeaderContentNegotiationStrategy;

@Configuration
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // Disable CSRF
//         http.csrf().disable();

        // protect endpoints at /api/<type>/secure
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/books/secure/**"
                                ,"/api/reviews/secure/**",
                                "/api/messages/secure/**",
                                "/api/admin/secure/**").authenticated()
                        .anyRequest().permitAll())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt());

        // Add Cors filters
        http.cors(Customizer.withDefaults());

        //Add content negotiation strategy
        http.setSharedObject(ContentNegotiationStrategy.class,
                new HeaderContentNegotiationStrategy());

        // force a non-empty response to 401
        Okta.configureResourceServer401ResponseBody(http);
        return http.build();
    }
}

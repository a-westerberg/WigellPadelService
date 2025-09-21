package com.skrt.wigellpadelservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/wigellpadel/listcourts",
                                "/api/wigellpadel/v1/listcanceled",
                                "/api/wigellpadel/v1/listupcoming",
                                "/api/wigellpadel/v1/listpast",
                                "/api/wigellpadel/v1/addcourt",
                                "/api/wigellpadel/v1/remcourt/**",
                                "/api/wigellpadel/v1/updatecourt"
                        ).hasRole("ADMIN")
                        /* TODO är det rätt att ha sammma endpoint (listcourts) såhär på 2 ställen?   */
                        .requestMatchers(
                                "/api/wigellpadel/listcourts",
                                "/api/wigellpadel/checkavailability/**",
                                "/api/wigellpadel/v1/booking/bookcourt",
                                "/api/wigellpadel/v1/mybookings",
                                "/api/wigellpadel/v1/updatebooking",
                                "/api/wigellpadel/v1/cancelbooking"
                        ).hasRole("USER")

                        .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults());
        return http.build();
    }
}

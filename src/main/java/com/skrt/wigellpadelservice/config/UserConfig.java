package com.skrt.wigellpadelservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

@Configuration
public class UserConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder){
        return new InMemoryUserDetailsManager(
                User.withUsername("alex").password(passwordEncoder.encode("alex")).roles("USER").build(),
                User.withUsername("sara").password(passwordEncoder.encode("sara")).roles("USER").build(),
                User.withUsername("amanda").password(passwordEncoder.encode("amanda")).roles("USER").build(),
                User.withUsername("simon").password(passwordEncoder.encode("simon")).roles("ADMIN").build()
                );
    }

}

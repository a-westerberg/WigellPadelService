package com.skrt.wigellpadelservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean("currencyRestClient")
    public RestClient currencyRestClient(@Value("${wigell.currency.apiplugin.baseUrl}") String baseUrl
    ) {
        return RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
}

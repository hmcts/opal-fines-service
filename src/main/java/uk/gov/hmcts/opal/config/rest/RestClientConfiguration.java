package uk.gov.hmcts.opal.config.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

import java.util.Base64;

@Configuration
public class RestClientConfiguration {

    @Value("${legacy-gateway.username}")
    String legacyUsername;

    @Value("${legacy-gateway.password}")
    String legacyPassword;

    @Bean
    public RestClient restClient() {
        return RestClient.create();
    }

    @Bean RestClient legacyRestClient() {
        return RestClient.builder()
            .defaultHeader(
                HttpHeaders.AUTHORIZATION,
                encodeBasic(legacyUsername,
                            legacyPassword)
            ).build();
    }

    private String encodeBasic(String username, String password) {
        return "Basic " + Base64
            .getEncoder()
            .encodeToString((username + ":" + password).getBytes());
    }

}

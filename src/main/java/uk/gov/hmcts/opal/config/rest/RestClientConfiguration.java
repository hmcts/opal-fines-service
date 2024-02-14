package uk.gov.hmcts.opal.config.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;
import uk.gov.hmcts.opal.config.properties.LegacyGatewayProperties;

import java.util.Base64;

@Configuration
@RequiredArgsConstructor
public class RestClientConfiguration {

    private final LegacyGatewayProperties properties;

    @Bean
    public RestClient restClient() {
        return RestClient.create();
    }

    @Bean
    RestClient legacyRestClient() {
        return RestClient.builder()
            .defaultHeader(
                HttpHeaders.AUTHORIZATION,
                encodeBasic(
                    properties.getUsername(),
                    properties.getPassword()
                )
            ).build();
    }

    private String encodeBasic(String username, String password) {
        return "Basic " + Base64
            .getEncoder()
            .encodeToString((username + ":" + password).getBytes());
    }

}

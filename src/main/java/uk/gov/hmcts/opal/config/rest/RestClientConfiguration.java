package uk.gov.hmcts.opal.config.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@RequiredArgsConstructor
public class RestClientConfiguration {

    @Bean
    public RestClient restClient() {
        return RestClient.create();
    }

}

package uk.gov.hmcts.opal.config;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class JacksonCompatibilityConfiguration {

    @Bean
    @Primary
    public ObjectMapper jackson2ObjectMapper() {
        return JsonMapper.builder()
            .findAndAddModules()
            .disable(WRITE_DATES_AS_TIMESTAMPS)
            .build();
    }
}

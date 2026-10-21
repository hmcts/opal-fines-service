package uk.gov.hmcts.opal.config;

import static tools.jackson.databind.cfg.DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class ObjectMapperConfiguration {

    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    public ObjectMapper jacksonObjectMapper() {
        return JsonMapper.builder()
            .findAndAddModules()
            .disable(WRITE_DATES_AS_TIMESTAMPS)
            .build();
    }

    @Bean(name = "snakeCaseObjectMapper")
    public ObjectMapper snakeCaseObjectMapper() {
        return JsonMapper.builder()
            .findAndAddModules()
            .disable(WRITE_DATES_AS_TIMESTAMPS)
            .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            .build();
    }
}

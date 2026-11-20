package uk.gov.hmcts.opal.hmrc;

import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HmrcApiClientConfig {

    @Bean
    public Retryer retryer() {
        return new Retryer.Default(1, 20, 3);
    }
}
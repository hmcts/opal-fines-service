package uk.gov.hmcts.opal.config;

import static org.assertj.core.api.Assertions.assertThat;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.junit.jupiter.api.Test;

class FeignConfigurationTest {

    private final FeignConfiguration configuration = new FeignConfiguration();

    @Test
    void userServiceFmeaHeaderInterceptor_shouldAddHeaderWhenEnabledForUserStatePath() {
        UserServiceFmeaProperties properties = new UserServiceFmeaProperties(true, "X-FMEA", "true");
        RequestInterceptor interceptor = configuration.userServiceFmeaHeaderInterceptor(properties);
        RequestTemplate template = new RequestTemplate();
        template.method("GET");
        template.uri("/v2/users/0/state");

        interceptor.apply(template);

        assertThat(template.headers()).containsEntry("X-FMEA", java.util.List.of("true"));
    }

    @Test
    void userServiceFmeaHeaderInterceptor_shouldNotAddHeaderForNonUserStatePath() {
        UserServiceFmeaProperties properties = new UserServiceFmeaProperties(true, "X-FMEA", "true");
        RequestInterceptor interceptor = configuration.userServiceFmeaHeaderInterceptor(properties);
        RequestTemplate template = new RequestTemplate();
        template.method("GET");
        template.uri("/testing-support/token/user");

        interceptor.apply(template);

        assertThat(template.headers()).doesNotContainKey("X-FMEA");
    }

    @Test
    void userServiceFmeaHeaderInterceptor_shouldNotAddHeaderWhenDisabled() {
        UserServiceFmeaProperties properties = new UserServiceFmeaProperties(false, "X-FMEA", "true");
        RequestInterceptor interceptor = configuration.userServiceFmeaHeaderInterceptor(properties);
        RequestTemplate template = new RequestTemplate();
        template.method("GET");
        template.uri("/v2/users/0/state");

        interceptor.apply(template);

        assertThat(template.headers()).doesNotContainKey("X-FMEA");
    }
}

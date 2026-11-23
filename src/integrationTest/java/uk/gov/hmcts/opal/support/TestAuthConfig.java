package uk.gov.hmcts.opal.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import uk.gov.hmcts.opal.common.spring.security.OpalJwtAuthenticationProvider;
import uk.gov.hmcts.opal.common.user.authentication.service.SystemUserAuthenticationService;
import uk.gov.hmcts.opal.common.user.authorisation.client.service.UserStateClientService;

@TestConfiguration
public class TestAuthConfig {
    static class TestJwtDecoder implements JwtDecoder {
        @Override
        public Jwt decode(String token) throws JwtException {
            return null;
        }
    }

    @Bean
    OpalJwtAuthenticationProvider opalJwtAuthenticationProvider() {
        return new OpalJwtAuthenticationProvider(
            new TestJwtDecoder(),
            new UserStateClientService(null, null, null, null),
            new JwtGrantedAuthoritiesConverter(),
            uk.gov.hmcts.opal.common.user.authorisation.model.Domain.MAINTENANCE);
    }

    @Bean
    @Primary
    SystemUserAuthenticationService systemUserAuthenticationService() {
        return new TestSystemUserAuthenticationService(new UserStateStub());
    }
}

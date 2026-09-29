package uk.gov.hmcts.opal;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.Collection;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import uk.gov.hmcts.opal.common.config.OpalCommonConfiguration;
import uk.gov.hmcts.opal.common.spring.security.OpalJwtAuthenticationToken;
import uk.gov.hmcts.opal.common.user.authorisation.client.service.UserStateClientService;
import uk.gov.hmcts.opal.common.user.authorisation.model.Domain;
import uk.gov.hmcts.opal.common.user.authorisation.model.UserStateV2;

@TestConfiguration
//@Profile("integration")
@Profile("integration-with-spring-security")
public class IntegrationSecurityConfiguration {

    // The common-lib security handler serializes ProblemDetail with custom properties under properties.retriable,
    // while the MVC advice serializes them at the top level. To avoid spreading odd JSON-path changes through tests,
    // the integration config patch is switched to use a local Spring-serialized handler shape instead of
    // the common-lib serializer.
    // This is test-profile only.
    /*@Bean
    @SuppressWarnings({"PMD.SignatureDeclareThrowsException", "squid:S4502"})
    public SecurityFilterChain integrationFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
            .exceptionHandling(exceptionHandling -> exceptionHandling
                .authenticationEntryPoint((request, response, authException) -> writeForbidden(response))
                .accessDeniedHandler((request, response, accessDeniedException) -> writeForbidden(response)))
            .build();
    }*/
    @Bean
    SecurityFilterChain integrationFilterChain(HttpSecurity http,
        AuthenticationManager integrationAuthenticationManager) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
            .oauth2ResourceServer(oauth2 ->
                oauth2.authenticationManagerResolver(request -> integrationAuthenticationManager))
            .exceptionHandling(exceptionHandling ->
                exceptionHandling.authenticationEntryPoint((request, response,
                        ex) -> writeForbidden(response))
                .accessDeniedHandler((request, response,
                    ex) -> writeForbidden(response))).build();
    }

    @Bean
    AuthenticationManager integrationAuthenticationManager(
        UserStateClientService userStateClientService,
        OpalCommonConfiguration commonConfiguration
    ) {

        Domain domain =
            Domain.findByDisplayName(commonConfiguration.getDomain());

        JwtGrantedAuthoritiesConverter authoritiesConverter =
            new JwtGrantedAuthoritiesConverter();

        return authentication -> {

            BearerTokenAuthenticationToken bearer =
                (BearerTokenAuthenticationToken) authentication;

            Instant now = Instant.now();

            Jwt jwt = Jwt.withTokenValue(bearer.getToken())
                .header("alg", "none")
                .claim("sub", "opal-test@hmcts.net")
                // Add the claim(s) UserStateClientService actually requires
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .build();

            UserStateV2 userState = userStateClientService
                .getUserStateByAuthenticationToken(jwt)
                .orElseThrow(() ->
                    new BadCredentialsException("User state not found"));

            Collection<GrantedAuthority> authorities =
                authoritiesConverter.convert(jwt);

            return new OpalJwtAuthenticationToken(
                userState,
                domain,
                jwt,
                authorities,
                bearer.getDetails()
            );
        };
    }

    private static void writeForbidden(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        response.getWriter().write("""
            {
              "type": "https://hmcts.gov.uk/problems/forbidden",
              "title": "Forbidden",
              "status": 403,
              "detail": "You do not have permission to access this resource",
              "retriable": false
            }
            """);
    }
}

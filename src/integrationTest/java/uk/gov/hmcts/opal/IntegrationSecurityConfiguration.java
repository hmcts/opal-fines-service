package uk.gov.hmcts.opal;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
@Profile("integration")
public class IntegrationSecurityConfiguration {

    // The common-lib security handler serializes ProblemDetail with custom properties under properties.retriable,
    // while the MVC advice serializes them at the top level. To avoid spreading odd JSON-path changes through tests,
    // the integration config patch is switched to use a local Spring-serialized handler shape instead of
    // the common-lib serializer.
    // This is test-profile only.
    @Bean
    @SuppressWarnings({"PMD.SignatureDeclareThrowsException", "squid:S4502"})
    public SecurityFilterChain integrationFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
            .exceptionHandling(exceptionHandling -> exceptionHandling
                .authenticationEntryPoint((request, response, authException) -> writeForbidden(response))
                .accessDeniedHandler((request, response, accessDeniedException) -> writeForbidden(response)))
            .build();
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

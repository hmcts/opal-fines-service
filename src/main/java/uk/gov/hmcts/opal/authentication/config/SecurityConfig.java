package uk.gov.hmcts.opal.authentication.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.filter.OncePerRequestFilter;
import uk.gov.hmcts.opal.authentication.config.internal.InternalAuthConfigurationProperties;
import uk.gov.hmcts.opal.authentication.config.internal.InternalAuthConfigurationPropertiesStrategy;
import uk.gov.hmcts.opal.authentication.config.internal.InternalAuthProviderConfigurationProperties;
import uk.gov.hmcts.opal.authentication.exception.AuthenticationError;
import uk.gov.hmcts.opal.exception.OpalApiException;

import java.io.IOException;
import java.util.Map;


@Slf4j(topic = "SecurityConfig")
@Configuration
@EnableWebSecurity
//@EnableWebSecurity(debug = true)
@RequiredArgsConstructor
@Profile("!integration")
public class SecurityConfig {

    private final AuthStrategySelector locator;

    private final InternalAuthConfigurationPropertiesStrategy fallbackConfiguration;
    private final InternalAuthConfigurationProperties internalAuthConfigurationProperties;
    private final InternalAuthProviderConfigurationProperties internalAuthProviderConfigurationProperties;

    private static final String[] AUTH_WHITELIST = {
        "/swagger-ui.html",
        "/swagger-ui/**",
        "/swagger-resources/**",
        "/v3/**",
        "/favicon.ico",
        "/health/**",
        "/mappings",
        "/info",
        "/metrics",
        "/metrics/**",
        "/internal-user/login-or-refresh",
        "/internal-user/logout",
        "/internal-user/handle-oauth-code",
        "/api/testing-support/**",
        "/api/s2s/**",
        "/"
    };

    @Bean
    @SuppressWarnings({"PMD.SignatureDeclareThrowsException", "squid:S4502"})
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        applyCommonConfig(http)
            .authorizeHttpRequests(authorize ->
                                       authorize.requestMatchers(PathRequest.toStaticResources().atCommonLocations())
                                           .permitAll()
                                           .requestMatchers(AUTH_WHITELIST)
                                           .permitAll()
            )
            .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
            .oauth2ResourceServer(oauth2 ->
                                      oauth2.authenticationManagerResolver(jwtIssuerAuthenticationManagerResolver())
            );

        return http.build();
    }

    private HttpSecurity applyCommonConfig(HttpSecurity http) throws Exception {
        return http
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(FormLoginConfigurer::disable)
            .logout(LogoutConfigurer::disable);
    }

    private JwtIssuerAuthenticationManagerResolver jwtIssuerAuthenticationManagerResolver() {
        Map<String, AuthenticationManager> authenticationManagers = Map.ofEntries(
            createAuthenticationEntry(
                internalAuthConfigurationProperties.getIssuerUri(),
                internalAuthProviderConfigurationProperties.getJwkSetUri()
            )
        );
        return new JwtIssuerAuthenticationManagerResolver(authenticationManagers::get);
    }

    private Map.Entry<String, AuthenticationManager> createAuthenticationEntry(String issuer,
                                                                               String jwkSetUri) {
        var jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri)
            .jwsAlgorithm(SignatureAlgorithm.RS256)
            .build();

        OAuth2TokenValidator<Jwt> jwtValidator = JwtValidators.createDefaultWithIssuer(issuer);
        jwtDecoder.setJwtValidator(jwtValidator);

        var authenticationProvider = new JwtAuthenticationProvider(jwtDecoder);

        return Map.entry(issuer, authenticationProvider::authenticate);
    }

    private class AuthorisationTokenExistenceFilter extends OncePerRequestFilter {

        @Override
        protected void doFilterInternal(HttpServletRequest request,
                                        HttpServletResponse response,
                                        FilterChain filterChain
        ) throws ServletException, IOException {

            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer")) {
                filterChain.doFilter(request, response);
                return;
            }
            
            log.warn(".AuthorisationTokenExistenceFilter:doFilterInternal: No Bearer Token.");
            throw new OpalApiException(AuthenticationError.FAILED_TO_OBTAIN_ACCESS_TOKEN);
        }
    }

}

package uk.gov.hmcts.opal.authentication.config;

import feign.FeignException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.security.autoconfigure.web.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.Authentication;
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
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.web.SecurityFilterChain;
import uk.gov.hmcts.opal.authentication.config.internal.InternalAuthConfigurationProperties;
import uk.gov.hmcts.opal.authentication.config.internal.InternalAuthProviderConfigurationProperties;
import uk.gov.hmcts.opal.common.config.OpalCommonConfiguration;
import uk.gov.hmcts.opal.common.spring.security.OpalJwtAuthenticationProvider;
import uk.gov.hmcts.opal.common.user.authentication.exception.CustomAuthenticationExceptions;
import uk.gov.hmcts.opal.common.user.authentication.exception.CustomOauth2AuthenticationEntryPoint;
import uk.gov.hmcts.opal.common.user.authorisation.client.service.UserStateClientService;
import uk.gov.hmcts.opal.common.user.authorisation.model.Domain;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Profile("!integration")
public class SecurityConfig {

    private final CustomAuthenticationExceptions customAuthenticationExceptions;
    private final CustomOauth2AuthenticationEntryPoint customOauth2AuthenticationEntryPoint;

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
        "/testing-support/**",
        "/s2s/**",
        "/"
    };

    @Bean
    @SuppressWarnings({"PMD.SignatureDeclareThrowsException", "squid:S4502"})
    public SecurityFilterChain filterChain(HttpSecurity http,
        JwtIssuerAuthenticationManagerResolver jwtIssuerAuthenticationManagerResolver) {
        applyCommonConfig(http)
            .authorizeHttpRequests(authorize ->
                authorize.requestMatchers(PathRequest.toStaticResources().atCommonLocations())
                    .permitAll()
                    .requestMatchers(AUTH_WHITELIST)
                    .permitAll()
                    .anyRequest().authenticated()
            )
            .exceptionHandling(exceptionHandling ->
                exceptionHandling
                    .authenticationEntryPoint(customAuthenticationExceptions)
                    .accessDeniedHandler(customAuthenticationExceptions)
            )
            .oauth2ResourceServer(oauth2 -> {
                oauth2.authenticationManagerResolver(jwtIssuerAuthenticationManagerResolver);
                oauth2.authenticationEntryPoint(customOauth2AuthenticationEntryPoint);
            });
        return http.build();
    }

    @Bean
    JwtIssuerAuthenticationManagerResolver jwtIssuerAuthenticationManagerResolver(
        InternalAuthConfigurationProperties authProps,
        OpalJwtAuthenticationProvider finesJwtAuthenticationProvider) {

        AuthenticationManager manager = authentication ->
            authenticateWithUserServiceUnauthorizedHandling(finesJwtAuthenticationProvider, authentication);

        Map<String, AuthenticationManager> managers = Map.of(authProps.getIssuerUri(), manager);
        return new JwtIssuerAuthenticationManagerResolver(managers::get);
    }

    private Authentication authenticateWithUserServiceUnauthorizedHandling(
        OpalJwtAuthenticationProvider finesJwtAuthenticationProvider,
        Authentication authentication) {

        try {
            return finesJwtAuthenticationProvider.authenticate(authentication);
        } catch (FeignException.Unauthorized ex) {
            throw new InvalidBearerTokenException("User Service rejected bearer token", ex);
        }
    }

    @Bean
    OpalJwtAuthenticationProvider finesJwtAuthenticationProvider(
        NimbusJwtDecoder internalJwtDecoder,
        UserStateClientService userStateClientService,
        JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter,
        OpalCommonConfiguration commonConfiguration) {

        Domain domain = Domain.findByDisplayName(commonConfiguration.getDomain());

        return new OpalJwtAuthenticationProvider(
            internalJwtDecoder,
            userStateClientService,
            jwtGrantedAuthoritiesConverter,
            domain
        );
    }

    @Bean
    NimbusJwtDecoder internalJwtDecoder(
        InternalAuthProviderConfigurationProperties providerProps,
        InternalAuthConfigurationProperties authProps) {

        var jwtDecoder = NimbusJwtDecoder.withJwkSetUri(providerProps.getJwkSetUri())
            .jwsAlgorithm(SignatureAlgorithm.RS256)
            .build();

        OAuth2TokenValidator<Jwt> jwtValidator =
            JwtValidators.createDefaultWithIssuer(authProps.getIssuerUri());
        jwtDecoder.setJwtValidator(jwtValidator);
        return jwtDecoder;
    }

    @Bean
    JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter() {
        return new JwtGrantedAuthoritiesConverter();
    }

    private HttpSecurity applyCommonConfig(HttpSecurity http) {
        return http
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(AbstractHttpConfigurer::disable)
            .formLogin(FormLoginConfigurer::disable)
            .logout(LogoutConfigurer::disable);
    }
}

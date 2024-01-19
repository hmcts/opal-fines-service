package uk.gov.hmcts.opal.authentication.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;
import org.springframework.security.web.debug.DebugFilter;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.RequestRejectedHandler;
import org.springframework.web.filter.CompositeFilter;
import org.springframework.web.filter.OncePerRequestFilter;
import uk.gov.hmcts.opal.authentication.config.internal.InternalAuthConfigurationProperties;
import uk.gov.hmcts.opal.authentication.config.internal.InternalAuthConfigurationPropertiesStrategy;
import uk.gov.hmcts.opal.authentication.config.internal.InternalAuthProviderConfigurationProperties;

import java.io.IOException;
import java.util.List;
import java.util.Map;


@Slf4j
@Configuration
@EnableWebSecurity(debug = true)
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
        "/api/**",
        "/metrics",
        "/metrics/**",
        "/internal-user/login-or-refresh",
        "/internal-user/logout",
        "/internal-user/handle-oauth-code",
        "/"
    };

    @Bean
    @Order(1)
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public SecurityFilterChain patternFilterChain(HttpSecurity http) throws Exception {

        applyCommonConfig(http)
            .authorizeHttpRequests(authorize ->
                                       authorize.requestMatchers(PathRequest.toStaticResources().atCommonLocations())
                                           .permitAll()
                                           .requestMatchers(AUTH_WHITELIST)
                                           .permitAll()
            );

        return http.build();
    }

    @Bean
    @SuppressWarnings({"PMD.SignatureDeclareThrowsException", "squid:S4502"})
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        applyCommonConfig(http)
            .addFilterBefore(new AuthorisationTokenExistenceFilter(), OAuth2LoginAuthenticationFilter.class)
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

            response.sendRedirect(locator.locateAuthenticationConfiguration(req -> fallbackConfiguration).getLoginUri(
                null).toString());
        }
    }

    // TODO - remove this temporary fix with next Spring upgrade. This allows @EnableWebSecurity(debug = true)
    @Bean
    static BeanDefinitionRegistryPostProcessor beanDefinitionRegistryPostProcessor() {
        return registry -> registry.getBeanDefinition(AbstractSecurityWebApplicationInitializer.DEFAULT_FILTER_NAME)
            .setBeanClassName(CompositeFilterChainProxy.class.getName());
    }

    // TODO - remove this temporary fix with next Spring upgrade. This allows @EnableWebSecurity(debug = true)
    static class CompositeFilterChainProxy extends FilterChainProxy {

        private final Filter doFilterDelegate;

        private final FilterChainProxy springSecurityFilterChain;

        CompositeFilterChainProxy(List<? extends Filter> filters) {
            this.doFilterDelegate = createDoFilterDelegate(filters);
            this.springSecurityFilterChain = findFilterChainProxy(filters);
        }

        @Override
        public void afterPropertiesSet() {
            this.springSecurityFilterChain.afterPropertiesSet();
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
            this.doFilterDelegate.doFilter(request, response, chain);
        }

        @Override
        public List<Filter> getFilters(String url) {
            return this.springSecurityFilterChain.getFilters(url);
        }

        @Override
        public List<SecurityFilterChain> getFilterChains() {
            return this.springSecurityFilterChain.getFilterChains();
        }

        @Override
        public void setSecurityContextHolderStrategy(SecurityContextHolderStrategy securityContextHolderStrategy) {
            this.springSecurityFilterChain.setSecurityContextHolderStrategy(securityContextHolderStrategy);
        }

        @Override
        public void setFilterChainValidator(FilterChainValidator filterChainValidator) {
            this.springSecurityFilterChain.setFilterChainValidator(filterChainValidator);
        }

        @Override
        public void setFilterChainDecorator(FilterChainDecorator filterChainDecorator) {
            this.springSecurityFilterChain.setFilterChainDecorator(filterChainDecorator);
        }

        @Override
        public void setFirewall(HttpFirewall firewall) {
            this.springSecurityFilterChain.setFirewall(firewall);
        }

        @Override
        public void setRequestRejectedHandler(RequestRejectedHandler requestRejectedHandler) {
            this.springSecurityFilterChain.setRequestRejectedHandler(requestRejectedHandler);
        }

        private static Filter createDoFilterDelegate(List<? extends Filter> filters) {
            CompositeFilter delegate = new CompositeFilter();
            delegate.setFilters(filters);
            return delegate;
        }

        private static FilterChainProxy findFilterChainProxy(List<? extends Filter> filters) {
            for (Filter filter : filters) {
                if (filter instanceof FilterChainProxy fcp) {
                    return fcp;
                }
                if (filter instanceof DebugFilter debugFilter) {
                    return debugFilter.getFilterChainProxy();
                }
            }
            throw new IllegalStateException("Couldn't find FilterChainProxy in " + filters);
        }

    }

}

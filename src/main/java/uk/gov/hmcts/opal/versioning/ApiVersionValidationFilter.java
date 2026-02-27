package uk.gov.hmcts.opal.versioning;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import uk.gov.hmcts.opal.versioning.ApiVersionRequestCondition.SemanticVersion;

@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiVersionValidationFilter extends OncePerRequestFilter {

    private final RequestVersionResolver resolver;
    @Nullable
    private final String defaultVersion;
    private final boolean requireHeader;
    private final ApiVersionSupport apiVersionSupport;
    @Nullable
    private final HandlerExceptionResolver handlerExceptionResolver;

    public ApiVersionValidationFilter(RequestVersionResolver resolver,
        @Nullable String defaultVersion,
        boolean requireHeader,
        ApiVersionSupport apiVersionSupport,
        @Nullable HandlerExceptionResolver handlerExceptionResolver) {
        this.resolver = resolver;
        this.defaultVersion = (defaultVersion == null || defaultVersion.isBlank()) ? null : defaultVersion;
        this.requireHeader = requireHeader;
        this.apiVersionSupport = apiVersionSupport;
        this.handlerExceptionResolver = handlerExceptionResolver;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain) throws ServletException, IOException {

        try {
            String raw = resolver.resolveVersion(request);

            if (!StringUtils.hasText(raw)) {
                if (requireHeader && defaultVersion == null) {
                    throw new ApiVersionException("Missing required API-Version header");
                }
                raw = (raw == null) ? defaultVersion : raw;
            }

            if (!StringUtils.hasText(raw)) {
                filterChain.doFilter(request, response);
                return;
            }

            SemanticVersion parsed;
            try {
                parsed = SemanticVersion.parse(raw);
            } catch (Exception ex) {
                throw new ApiVersionException("Malformed API-Version header: " + raw);
            }

            boolean supported = apiVersionSupport.isSupported(raw);
            if (!supported) {
                throw new ApiVersionException("Unsupported API-Version: " + raw);
            }

            filterChain.doFilter(request, response);

        } catch (Exception ex) {
            if (handlerExceptionResolver != null) {
                handlerExceptionResolver.resolveException(request, response, null, ex);
                return;
            }

            if (ex instanceof ServletException) {
                throw (ServletException) ex;
            } else if (ex instanceof IOException) {
                throw (IOException) ex;
            } else {
                throw new ServletException(ex);
            }
        }
    }
}
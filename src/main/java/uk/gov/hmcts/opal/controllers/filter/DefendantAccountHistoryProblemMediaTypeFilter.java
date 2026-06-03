package uk.gov.hmcts.opal.controllers.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

@Component
@Order(Ordered.LOWEST_PRECEDENCE)
public class DefendantAccountHistoryProblemMediaTypeFilter extends OncePerRequestFilter {

    private static final String LEGACY_PROBLEM_JSON = "application/json+problem";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        if (!isDefendantAccountHistoryPath(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        try {
            filterChain.doFilter(request, wrappedResponse);
        } finally {
            if (wrappedResponse.getStatus() >= 400) {
                String contentType = wrappedResponse.getContentType();
                if (contentType != null && contentType.startsWith(MediaType.APPLICATION_PROBLEM_JSON_VALUE)) {
                    wrappedResponse.setContentType(LEGACY_PROBLEM_JSON);
                }
            }

            wrappedResponse.copyBodyToResponse();
        }
    }

    private boolean isDefendantAccountHistoryPath(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path != null && path.contains("/defendant-accounts/") && path.endsWith("/history");
    }
}

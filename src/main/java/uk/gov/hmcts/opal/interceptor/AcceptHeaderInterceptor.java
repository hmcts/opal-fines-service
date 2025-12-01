package uk.gov.hmcts.opal.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.hmcts.opal.annotation.CheckAcceptHeader;


@Component
public class AcceptHeaderInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
        throws Exception {
        if (handler instanceof HandlerMethod handlerMethod
            && handlerMethod.hasMethodAnnotation(CheckAcceptHeader.class)) {
            String acceptHeader = request.getHeader("Accept");
            if (!isAcceptableMediaType(acceptHeader)) {
                response.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
                response.getWriter().write("{\"error\":\"Not Acceptable\",\"message\""
                    + ":\"The requested media type is not supported\"}");
                return false;
            }
        }
        return true;
    }

    private boolean isAcceptableMediaType(String acceptHeader) {
        return acceptHeader == null || (acceptHeader.contains("application/json") || acceptHeader.contains("*/*"));
    }
}

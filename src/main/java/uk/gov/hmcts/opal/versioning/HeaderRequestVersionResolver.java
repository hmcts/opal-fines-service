package uk.gov.hmcts.opal.versioning;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.lang.Nullable;

public class HeaderRequestVersionResolver implements RequestVersionResolver {
    private final String headerName;

    public HeaderRequestVersionResolver(String headerName) {
        this.headerName = headerName;
    }

    @Override
    @Nullable
    public String resolveVersion(HttpServletRequest request) {
        String v = request.getHeader(headerName);
        return (v == null || v.isBlank()) ? null : v.trim();
    }
}

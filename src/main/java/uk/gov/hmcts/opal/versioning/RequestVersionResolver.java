package uk.gov.hmcts.opal.versioning;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.lang.Nullable;

public interface RequestVersionResolver {
    @Nullable
    String resolveVersion(HttpServletRequest request);
}


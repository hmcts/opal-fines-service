package uk.gov.hmcts.opal.versioning;

import org.springframework.stereotype.Component;

import java.util.List;
import uk.gov.hmcts.opal.versioning.ApiVersionRequestCondition.SemanticVersion;
import uk.gov.hmcts.opal.versioning.ApiVersionRequestCondition.VersionExpression;


@Component
public class ApiVersionSupport {

    private final ApiVersionScanner scanner;

    public ApiVersionSupport(ApiVersionScanner scanner) {
        this.scanner = scanner;
    }

    public boolean isSupported(String rawVersion) {
        if (rawVersion == null || rawVersion.isBlank()) {
            return false;
        }

        SemanticVersion sv;
        try {
            sv = SemanticVersion.parse(rawVersion.trim());
        } catch (Exception ex) {
            return false;
        }

        List<VersionExpression> exprs = scanner.getVersionExpressions();
        if (exprs.isEmpty()) {
            return true;
        }

        return exprs.stream().anyMatch(expr -> expr.matches(sv));
    }
}

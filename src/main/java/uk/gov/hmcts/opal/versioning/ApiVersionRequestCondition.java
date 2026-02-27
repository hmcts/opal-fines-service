package uk.gov.hmcts.opal.versioning;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.web.servlet.mvc.condition.RequestCondition;

public class ApiVersionRequestCondition implements RequestCondition<ApiVersionRequestCondition> {

    private static final String VERSION_HEADER = "API-Version";

    private final List<VersionExpression> expressions;

    public ApiVersionRequestCondition(String[] versions) {
        this.expressions = Arrays.stream(versions)
            .map(VersionExpression::parse)
            .sorted()
            .toList();
    }

    private ApiVersionRequestCondition(List<VersionExpression> expressions) {
        this.expressions = expressions;
    }

    @Override
    public ApiVersionRequestCondition combine(ApiVersionRequestCondition other) {
        return (other.expressions.isEmpty()) ? this : other;
    }

    @Override
    public @Nullable ApiVersionRequestCondition getMatchingCondition(HttpServletRequest request) {

        String header = request.getHeader(VERSION_HEADER);
        if (header == null || header.isBlank()) {
            return null;
        }

        SemanticVersion requestVersion = SemanticVersion.parse(header);

        List<VersionExpression> matches = expressions.stream()
            .filter(expr -> expr.matches(requestVersion))
            .toList();

        if (matches.isEmpty()) {
            return null;
        }

        return new ApiVersionRequestCondition(matches);
    }

    @Override
    public int compareTo(ApiVersionRequestCondition other, HttpServletRequest request) {
        SemanticVersion requestVersion =
            SemanticVersion.parse(request.getHeader(VERSION_HEADER));

        VersionExpression thisBest =
            this.expressions.stream()
                .max(Comparator.naturalOrder())
                .orElse(null);

        VersionExpression otherBest =
            other.expressions.stream()
                .max(Comparator.naturalOrder())
                .orElse(null);

        if (thisBest == null && otherBest == null) {
            return 0;
        }
        if (thisBest == null) {
            return 1;
        }
        if (otherBest == null) {
            return -1;
        }

        return otherBest.compareTo(thisBest);
    }

    public record SemanticVersion(int major, int minor, int patch)
        implements Comparable<SemanticVersion> {

        static SemanticVersion parse(String value) {
            String[] parts = value.split("\\.");
            int major = Integer.parseInt(parts[0]);
            int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            int patch = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;
            return new SemanticVersion(major, minor, patch);
        }

        @Override
        public int compareTo(SemanticVersion other) {
            int majorCmp = Integer.compare(this.major, other.major);
            if (majorCmp != 0) {
                return majorCmp;
            }

            int minorCmp = Integer.compare(this.minor, other.minor);
            if (minorCmp != 0) {
                return minorCmp;
            }

            return Integer.compare(this.patch, other.patch);
        }
    }

    public static final class VersionExpression
        implements Comparable<VersionExpression> {

        private final SemanticVersion min;
        private final SemanticVersion max; // null if open-ended
        private final boolean exact;

        private VersionExpression(SemanticVersion min,
            SemanticVersion max,
            boolean exact) {
            this.min = min;
            this.max = max;
            this.exact = exact;
        }

        static VersionExpression parse(String value) {

            value = value.trim();

            if (value.endsWith("+")) {
                SemanticVersion min =
                    SemanticVersion.parse(value.substring(0, value.length() - 1));
                return new VersionExpression(min, null, false);
            }

            if (value.contains("-")) {
                String[] parts = value.split("-");
                SemanticVersion min = SemanticVersion.parse(parts[0]);
                SemanticVersion max = SemanticVersion.parse(parts[1]);
                return new VersionExpression(min, max, false);
            }

            SemanticVersion exact = SemanticVersion.parse(value);
            return new VersionExpression(exact, exact, true);
        }

        boolean matches(SemanticVersion version) {
            if (version.compareTo(min) < 0) {
                return false;
            }
            return max == null || version.compareTo(max) <= 0;
        }

        @Override
        public int compareTo(VersionExpression other) {

            if (this.exact && !other.exact) {
                return 1;
            }
            if (!this.exact && other.exact) {
                return -1;
            }

            return this.min.compareTo(other.min);
        }
    }
}

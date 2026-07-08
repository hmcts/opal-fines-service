package uk.gov.hmcts.opal.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public final class SearchResultLimits {

    public static final int DEFAULT_SEARCH_RESULTS_LIMIT = 100;

    private SearchResultLimits() {
    }

    public static Pageable defaultPage() {
        return PageRequest.of(0, DEFAULT_SEARCH_RESULTS_LIMIT);
    }

    public static int cappedLimit(Integer requestedLimit) {
        if (requestedLimit == null || requestedLimit <= 0) {
            return DEFAULT_SEARCH_RESULTS_LIMIT;
        }
        return Math.min(requestedLimit, DEFAULT_SEARCH_RESULTS_LIMIT);
    }
}

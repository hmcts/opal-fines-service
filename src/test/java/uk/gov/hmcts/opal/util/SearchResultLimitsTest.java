package uk.gov.hmcts.opal.util;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SearchResultLimitsTest {

    @Test
    void defaultPage_usesDefaultSearchLimit() {
        Pageable pageable = SearchResultLimits.defaultPage();

        assertEquals(0, pageable.getPageNumber());
        assertEquals(SearchResultLimits.DEFAULT_SEARCH_RESULTS_LIMIT, pageable.getPageSize());
    }

    @Test
    void cappedLimit_defaultsWhenRequestLimitIsMissingOrInvalid() {
        assertEquals(SearchResultLimits.DEFAULT_SEARCH_RESULTS_LIMIT, SearchResultLimits.cappedLimit(null));
        assertEquals(SearchResultLimits.DEFAULT_SEARCH_RESULTS_LIMIT, SearchResultLimits.cappedLimit(0));
        assertEquals(SearchResultLimits.DEFAULT_SEARCH_RESULTS_LIMIT, SearchResultLimits.cappedLimit(-1));
    }

    @Test
    void cappedLimit_usesRequestedLimitUpToDefaultSearchLimit() {
        assertEquals(25, SearchResultLimits.cappedLimit(25));
        assertEquals(SearchResultLimits.DEFAULT_SEARCH_RESULTS_LIMIT, SearchResultLimits.cappedLimit(101));
    }
}

package uk.gov.hmcts.opal.dto;


import java.util.List;

import lombok.Builder;
import lombok.Getter;


@Builder
@Getter
public class AccountSearchResultsDto {

    /** The number of AccountSummary objects returned within this response. */
    private Integer count;
    /** The total number of matching Accounts found in the database. */
    private Integer totalCount;
    /** The position of the first AccountSummary in this response within the total search of the database. */
    private Integer cursor;
    /** The maximum number of AccountSummary objects that can be returned in a single search response. */
    private final Integer pageSize = 100;
    /** A list of AccountSummary objects, limited to a maximum of pageSize. */
    private List<AccountSummaryDto> searchResults;

    public static class AccountSearchResultsDtoBuilder {
        public AccountSearchResultsDtoBuilder searchResults(List<AccountSummaryDto> searchResults) {
            this.searchResults = searchResults;
            this.count = searchResults.size();
            return this;
        }

        private AccountSearchResultsDtoBuilder count(Integer count) {
            return this;
        }
    }
}

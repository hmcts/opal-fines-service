package uk.gov.hmcts.opal.dto;


import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class AccountSearchResultsDto implements ToJsonString {

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
            this.count(searchResults.size());
            return this;
        }

        private AccountSearchResultsDtoBuilder count(Integer count) {
            this.count = count;
            return this;
        }
    }
}

package uk.gov.hmcts.opal.dto.search;


import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.AccountSummaryDto;
import uk.gov.hmcts.opal.dto.ToJsonString;


@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class AccountSearchResultsDto implements ToJsonString {

    /** The number of AccountSummary objects returned within this response. */
    private Integer count;
    /** The total number of matching Accounts found in the database. */
    private Long totalCount;
    /** The position of the first AccountSummary in this response within the total search of the database. */
    private Integer cursor;
    /** The maximum number of AccountSummary objects that can be returned in a single search response. */
    private final Integer pageSize = 100;
    /** A list of AccountSummary objects, limited to a maximum of pageSize. */
    private List<AccountSummaryDto> searchResults;

    public static class AccountSearchResultsDtoBuilder {
        public AccountSearchResultsDtoBuilder searchResults(List<AccountSummaryDto> searchResults) {
            this.searchResults = searchResults;
            this.count(Optional.ofNullable(searchResults).map(List::size).orElse(0));
            return this;
        }

        private AccountSearchResultsDtoBuilder count(Integer count) {
            this.count = count;
            return this;
        }
    }
}

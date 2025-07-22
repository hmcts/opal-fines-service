package uk.gov.hmcts.opal.dto.search;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.DefendantAccountSummaryDto;
import uk.gov.hmcts.opal.dto.ToJsonString;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
@Builder(builderClassName = "DefendantAccountSearchResultsDtoBuilder")
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class DefendantAccountSearchResultsDto implements ToJsonString {

    /** A list of defendant accounts. Null if none are found. */
    @JsonProperty("defendant_accounts")
    @Builder.Default
    private List<DefendantAccountSummaryDto> defendantAccounts = new ArrayList<>();

    /** The total number of records identified. */
    private Integer count;

    /**
     * Custom builder to populate count automatically from list size.
     */
    public static class DefendantAccountSearchResultsDtoBuilder {

        private List<DefendantAccountSummaryDto> defendantAccounts;
        private Integer count;

        public DefendantAccountSearchResultsDtoBuilder defendantAccounts(List<DefendantAccountSummaryDto>
                                                                             defendantAccounts) {
            this.defendantAccounts = defendantAccounts;
            return count(Optional.ofNullable(defendantAccounts).map(List::size).orElse(0));
        }

        public DefendantAccountSearchResultsDtoBuilder count(Integer count) {
            this.count = count;
            return this;
        }

        public DefendantAccountSearchResultsDto build() {
            return new DefendantAccountSearchResultsDto(defendantAccounts, count);
        }


    }

}

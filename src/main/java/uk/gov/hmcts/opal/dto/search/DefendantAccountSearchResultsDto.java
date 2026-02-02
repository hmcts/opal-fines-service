package uk.gov.hmcts.opal.dto.search;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.DefendantAccountSummaryDto;
import uk.gov.hmcts.opal.dto.ToJsonString;

import java.util.List;
import java.util.Optional;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DefendantAccountSearchResultsDto implements ToJsonString {

    private Integer count;

    @JsonProperty("defendant_accounts")
    private List<DefendantAccountSummaryDto> defendantAccounts;

    public static class DefendantAccountSearchResultsDtoBuilder {

        public DefendantAccountSearchResultsDtoBuilder defendantAccounts(List<DefendantAccountSummaryDto>
                                                                             defendantAccounts) {
            this.defendantAccounts = defendantAccounts;
            return this.count(Optional.ofNullable(defendantAccounts).map(List::size).orElse(0));
        }

        private DefendantAccountSearchResultsDtoBuilder count(Integer count) {
            this.count = count;
            return this;
        }
    }

}

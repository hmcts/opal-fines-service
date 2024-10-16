package uk.gov.hmcts.opal.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Optional;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL) // This line forces the HTTP Response to be of type 'application/json'
public class DraftAccountsResponseDto {
    private Integer count;
    private List<DraftAccountSummaryDto> summaries;

    public static class DraftAccountsResponseDtoBuilder {
        public DraftAccountsResponseDto.DraftAccountsResponseDtoBuilder summaries(
            List<DraftAccountSummaryDto> summaries) {
            this.summaries = summaries;
            return this.count(Optional.ofNullable(summaries).map(List::size).orElse(0));
        }

        private DraftAccountsResponseDto.DraftAccountsResponseDtoBuilder count(Integer count) {
            this.count = count;
            return this;
        }
    }
}

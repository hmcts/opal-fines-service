package uk.gov.hmcts.opal.service.legacy.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.AccountSearchResultsDto;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefendantAccountsSearchResults {

    List<DefendantAccountSearchResult> defendantAccountsSearchResult;

    private Long totalCount;

    public AccountSearchResultsDto toAccountSearchResultsDto() {
        return AccountSearchResultsDto.builder()
            .totalCount(getTotalCount())
            .searchResults(Optional.ofNullable(defendantAccountsSearchResult).orElse(Collections.emptyList())
                 .stream()
                 .map(DefendantAccountSearchResult::toAccountSummaryDto)
                 .collect(Collectors.toList()))
            .build();
    }
}

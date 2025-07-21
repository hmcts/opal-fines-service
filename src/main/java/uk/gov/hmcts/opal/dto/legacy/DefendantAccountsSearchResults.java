package uk.gov.hmcts.opal.dto.legacy;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.search.AccountSearchResultsDto;
import uk.gov.hmcts.opal.dto.ToJsonString;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@XmlRootElement(name = "defendantAccountsSearchResults")
@XmlAccessorType(XmlAccessType.FIELD)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefendantAccountsSearchResults implements ToJsonString {
    @XmlElement(name = "defendantAccountEntity")
    List<DefendantAccountSearchResult> defendantAccountsSearchResult;
    @XmlElement(name = "count")
    private Long totalCount;

    public AccountSearchResultsDto toAccountSearchResultsDto() {
        AccountSearchResultsDto accountsSearchResultsDto = AccountSearchResultsDto.builder()
            .totalCount(getTotalCount())
            .searchResults(Optional.ofNullable(defendantAccountsSearchResult).orElse(Collections.emptyList())
                 .stream()
                 .map(DefendantAccountSearchResult::toAccountSummaryDto)
                 .collect(Collectors.toList()))
            .build();
        return accountsSearchResultsDto;
    }
}

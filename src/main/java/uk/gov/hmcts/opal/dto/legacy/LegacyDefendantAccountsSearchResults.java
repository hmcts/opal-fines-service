package uk.gov.hmcts.opal.dto.legacy;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.hmcts.opal.dto.ToJsonString;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.FIELD)
public class LegacyDefendantAccountsSearchResults implements ToJsonString {

    @XmlElementWrapper(name = "defendant_accounts")
    @XmlElement(name = "defendant_accounts_element")
    List<LegacyDefendantAccountSearchResult> defendantAccountsSearchResult;

    @XmlElement(name = "count")
    private Long totalCount;

    public DefendantAccountSearchResultsDto toDefendantAccountSearchResultsDto() {
        DefendantAccountSearchResultsDto results = DefendantAccountSearchResultsDto.builder()
            .defendantAccounts(Optional.ofNullable(defendantAccountsSearchResult).orElse(Collections.emptyList())
                 .stream()
                 .map(LegacyDefendantAccountSearchResult::toDefendantAccountSummaryDto)
                 .toList())
            .build();
        return results;
    }
}

package uk.gov.hmcts.opal.service.opal;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;
import uk.gov.hmcts.opal.service.iface.DefendantAccountServiceInterface;

@Service
@Slf4j(topic = "opal.OpalDefendantAccountService")
@RequiredArgsConstructor
public class OpalDefendantAccountService implements DefendantAccountServiceInterface {

    public DefendantAccountHeaderSummary getHeaderSummary(Long defendantAccountId) {
        log.debug(":getHeaderSummary: id: {} - NOT YET IMPLEMENTED.", defendantAccountId);
        // TODO: implement this when Opal mode is supported
        throw new EntityNotFoundException("Defendant Account not found with id: " + defendantAccountId);
    }

    @Override
    public DefendantAccountSearchResultsDto searchDefendantAccounts(AccountSearchDto accountSearchDto) {
        log.debug(":searchDefendantAccounts: Opal implementation not yet provided.");
        // TODO: implement this when Opal mode is supported
        return DefendantAccountSearchResultsDto.builder()
                .defendantAccounts(null)
                .count(0)
                .build();
    }
}

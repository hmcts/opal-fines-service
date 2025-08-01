package uk.gov.hmcts.opal.disco;

import uk.gov.hmcts.opal.dto.AccountDetailsDto;
import uk.gov.hmcts.opal.dto.AccountEnquiryDto;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;
import uk.gov.hmcts.opal.entity.DefendantAccountEntity;

import java.util.List;

public interface DiscoDefendantAccountServiceInterface {
    DefendantAccountEntity getDefendantAccount(AccountEnquiryDto request);

    DefendantAccountEntity putDefendantAccount(DefendantAccountEntity defendantAccountEntity);

    List<DefendantAccountEntity> getDefendantAccountsByBusinessUnit(Short businessUnitId);

    DefendantAccountSearchResultsDto searchDefendantAccounts(AccountSearchDto accountSearchDto);

    AccountDetailsDto getAccountDetailsByDefendantAccountId(Long defendantAccountId);
}

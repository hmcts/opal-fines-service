package uk.gov.hmcts.opal.service;

import uk.gov.hmcts.opal.dto.AccountDetailsDto;
import uk.gov.hmcts.opal.dto.AccountEnquiryDto;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.AccountSearchResultsDto;
import uk.gov.hmcts.opal.entity.defendant.DefendantAccountCore;

import java.util.List;

public interface DefendantAccountServiceInterface {
    DefendantAccountCore getDefendantAccount(AccountEnquiryDto request);

    DefendantAccountCore putDefendantAccount(DefendantAccountCore defendantAccountEntity);

    List<DefendantAccountCore> getDefendantAccountsByBusinessUnit(Short businessUnitId);

    AccountSearchResultsDto searchDefendantAccounts(AccountSearchDto accountSearchDto);

    AccountDetailsDto getAccountDetailsByDefendantAccountId(Long defendantAccountId);
}

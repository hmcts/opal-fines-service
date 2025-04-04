package uk.gov.hmcts.opal.service;

import uk.gov.hmcts.opal.dto.AccountDetailsDto;
import uk.gov.hmcts.opal.dto.AccountEnquiryDto;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.AccountSearchResultsDto;
import uk.gov.hmcts.opal.entity.defendant.DefendantAccount;

import java.util.List;

public interface DefendantAccountServiceInterface {
    DefendantAccount.Lite getDefendantAccount(AccountEnquiryDto request);

    DefendantAccount.Lite putDefendantAccount(DefendantAccount.Lite defendantAccountEntity);

    List<DefendantAccount.Lite> getDefendantAccountsByBusinessUnit(Short businessUnitId);

    AccountSearchResultsDto searchDefendantAccounts(AccountSearchDto accountSearchDto);

    AccountDetailsDto getAccountDetailsByDefendantAccountId(Long defendantAccountId);
}

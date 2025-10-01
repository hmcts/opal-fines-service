package uk.gov.hmcts.opal.service.iface;

import uk.gov.hmcts.opal.dto.AddDefendantAccountPaymentTermsRequest;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPartyResponse;
import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.GetDefendantAccountPaymentTermsResponse;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;

public interface DefendantAccountServiceInterface {
    DefendantAccountHeaderSummary getHeaderSummary(Long defendantAccountId);

    DefendantAccountSearchResultsDto searchDefendantAccounts(AccountSearchDto accountSearchDto);

    GetDefendantAccountPartyResponse getDefendantAccountParty(Long defendantAccountId, Long defendantAccountPartyId);

    GetDefendantAccountPaymentTermsResponse getPaymentTerms(Long defendantAccountId);

    String addPaymentTerms(Long defendantAccountId,  short businessUnitId, String ifMatch, AddDefendantAccountPaymentTermsRequest request);
}

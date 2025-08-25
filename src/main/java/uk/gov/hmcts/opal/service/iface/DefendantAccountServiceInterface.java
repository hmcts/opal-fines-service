package uk.gov.hmcts.opal.service.iface;

import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.response.GetHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;

public interface DefendantAccountServiceInterface {
    DefendantAccountHeaderSummary getHeaderSummary(Long defendantAccountId);

    DefendantAccountSearchResultsDto searchDefendantAccounts(AccountSearchDto accountSearchDto);

    GetHeaderSummaryResponse getHeaderSummaryWithVersion(Long defendantAccountId, String authHeader);

}

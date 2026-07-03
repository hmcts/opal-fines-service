package uk.gov.hmcts.opal.service.iface;

import uk.gov.hmcts.opal.dto.DefendantAccountHeaderSummary;
import uk.gov.hmcts.opal.dto.UpdateDefendantAccountRequest;
import uk.gov.hmcts.opal.dto.UpdateDefendantAccountResponse;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryFilter;
import uk.gov.hmcts.opal.dto.history.DefendantAccountHistoryResponse;
import uk.gov.hmcts.opal.dto.response.DefendantAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.search.AccountSearchDto;
import uk.gov.hmcts.opal.dto.search.DefendantAccountSearchResultsDto;

public interface DefendantAccountServiceInterface {
    DefendantAccountHeaderSummary getHeaderSummary(Long defendantAccountId);

    DefendantAccountHistoryResponse getHistory(Long defendantAccountId, DefendantAccountHistoryFilter filter);

    DefendantAccountSearchResultsDto searchDefendantAccounts(AccountSearchDto accountSearchDto);

    DefendantAccountAtAGlanceResponse getAtAGlance(Long defendantAccountId);

    UpdateDefendantAccountResponse updateDefendantAccount(Long defendantAccountId,
                                                    String businessUnitId,
                                                    UpdateDefendantAccountRequest request,
                                                    String postedBy,
                                                    String postedByName);

    default UpdateDefendantAccountResponse updateDefendantAccount(Long defendantAccountId,
                                                    String businessUnitId,
                                                    UpdateDefendantAccountRequest request,
                                                    String postedBy) {
        return updateDefendantAccount(defendantAccountId, businessUnitId, request, postedBy, postedBy);
    }
}

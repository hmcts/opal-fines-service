package uk.gov.hmcts.opal.service.iface;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.MinorCreditorAccountResponse;
import uk.gov.hmcts.opal.dto.MinorCreditorSearch;
import uk.gov.hmcts.opal.dto.PostMinorCreditorAccountsSearchResponse;
import uk.gov.hmcts.opal.dto.response.GetMinorCreditorHistoryResponse;
import uk.gov.hmcts.opal.generated.model.PatchMinorCreditorAccountRequest;

public interface MinorCreditorServiceInterface {

    PostMinorCreditorAccountsSearchResponse searchMinorCreditors(MinorCreditorSearch minorCreditorSearchDto);

    MinorCreditorAccountResponse getMinorCreditorAccount(Long minorCreditorAccountId);

    GetMinorCreditorHistoryResponse getMinorCreditorHistory(
        Long minorCreditorAccountId,
        LocalDate dateFrom,
        LocalDate dateTo,
        List<String> itemTypes);

    GetMinorCreditorAccountAtAGlanceResponse getMinorCreditorAtAGlance(Long minorCreditorId);

    GetMinorCreditorAccountHeaderSummaryResponse getHeaderSummary(
        Long minorCreditorAccountId
    );

    MinorCreditorAccountResponse updateMinorCreditorAccount(
        Long minorCreditorAccountId,
        PatchMinorCreditorAccountRequest request,
        BigInteger etag,
        String postedBy,
        String postedByName,
        Short businessUnitId);

    default MinorCreditorAccountResponse updateMinorCreditorAccount(
        Long minorCreditorAccountId,
        PatchMinorCreditorAccountRequest request,
        BigInteger etag,
        String postedBy,
        Short businessUnitId) {
        return updateMinorCreditorAccount(minorCreditorAccountId, request, etag, postedBy, postedBy, businessUnitId);
    }
}

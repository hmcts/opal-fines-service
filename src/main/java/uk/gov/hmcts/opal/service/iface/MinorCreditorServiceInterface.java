package uk.gov.hmcts.opal.service.iface;

import java.math.BigInteger;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.dto.MinorCreditorAccountResponse;
import uk.gov.hmcts.opal.dto.response.GetMinorCreditorHistoryResponse;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorHistoryFilters;
import uk.gov.hmcts.opal.generated.model.MinorCreditorAccountsSearchResponse;
import uk.gov.hmcts.opal.generated.model.MinorCreditorSearchRequest;
import uk.gov.hmcts.opal.generated.model.PatchMinorCreditorAccountRequest;
import uk.gov.hmcts.opal.generated.model.MinorCreditorAccountAtAGlanceResponse;

public interface MinorCreditorServiceInterface {

    MinorCreditorAccountsSearchResponse searchMinorCreditors(MinorCreditorSearchRequest minorCreditorSearchDto);

    MinorCreditorAccountResponse getMinorCreditorAccount(Long minorCreditorAccountId);

    GetMinorCreditorHistoryResponse getMinorCreditorHistory(
        Long minorCreditorAccountId,
        MinorCreditorHistoryFilters filters);

    MinorCreditorAccountAtAGlanceResponse getMinorCreditorAtAGlance(Long minorCreditorId);

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

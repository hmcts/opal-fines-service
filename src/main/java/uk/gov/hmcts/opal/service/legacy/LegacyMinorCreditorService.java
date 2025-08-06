package uk.gov.hmcts.opal.service.legacy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyMinorCreditorSearchResultsRequest;
import uk.gov.hmcts.opal.dto.legacy.search.LegacyMinorCreditorSearchResultsResponse;
import uk.gov.hmcts.opal.entity.minorcreditor.MinorCreditorEntity;
import uk.gov.hmcts.opal.service.MinorCreditorServiceInterface;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "opal.LegacyMinorCreditorService")
public class LegacyMinorCreditorService implements MinorCreditorServiceInterface {

    private final GatewayService gatewayService;
private static final String SEARCH_MINOR_CREDITORS = "searchMinorCreditors";

    @Override
    public MinorCreditorEntity searchMinorCreditors(MinorCreditorEntity minorCreditorEntity) {

        LegacyMinorCreditorSearchResultsRequest request = LegacyMinorCreditorSearchResultsRequest.builder().build();

        CompletableFuture<GatewayService.Response<LegacyMinorCreditorSearchResultsResponse>> future =
            gatewayService.postToGatewayAsync(SEARCH_MINOR_CREDITORS, LegacyMinorCreditorSearchResultsResponse.class, request
        );

        try {
            GatewayService.Response<LegacyMinorCreditorSearchResultsResponse> response = future.get();
            if (response.isError()) {
                log.error(
                    ":postSearchMinorCreditorSearch: Legacy Gateway response: HTTP Response Code: {}",
                    response.code
                );
                if (response.isException()) {
                    log.error(":postSearchMinorCreditorSearch:", response.exception);
                    String errorResponse = response.responseEntity.getErrorResponse();
                } else if (response.isSuccessful()) {
                    log.info(":postSearchMinorCreditorSearch: Legacy Gateway response: Success.");
                    minorCreditorEntity.setBusinessUnitId(response.responseEntity.getBusinessUnitId());
                }
            }
        } catch (ExecutionException e) {
            log.error(":searchMinorCreditors: ExecutionException occurred while searching minor creditors.", e);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
        return minorCreditorEntity;
    }
}


package uk.gov.hmcts.opal.controllers;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.dto.UpdateDefendantAccountResponse;
import uk.gov.hmcts.opal.generated.http.api.DefendantAccountApi;
import uk.gov.hmcts.opal.generated.model.GetEnforcementStatusResponse;
import uk.gov.hmcts.opal.generated.model.UpdateDefendantAccountRequestPayload;
import uk.gov.hmcts.opal.generated.model.UpdateDefendantAccountResponsePayload;
import uk.gov.hmcts.opal.service.DefendantAccountService;
import uk.gov.hmcts.opal.util.VersionUtils;

@RestController
@Slf4j(topic = "opal.DefendantAccountApiController")
@RequiredArgsConstructor
public class DefendantAccountApiController implements DefendantAccountApi {

    private final DefendantAccountService defendantAccountService;

    @Override
    public ResponseEntity<GetEnforcementStatusResponse> getEnforcementStatus(
        Long id, String authHeaderValue) {
        log.debug(":GET:getDefendantAccountEnforcementStatus: for defendant id: {}", id);

        return buildResponse(defendantAccountService.getEnforcementStatus(id, authHeaderValue));
    }

    @Override
    public ResponseEntity<UpdateDefendantAccountResponsePayload> updateDefendantAccount(
        @NonNull Long defendantAccountId,
        @NonNull String authHeaderValue,
        @NonNull String businessUnitId,
        @NonNull UpdateDefendantAccountRequestPayload request,
        @NonNull String ifMatch
    ) {
        log.debug(":PATCH:updateDefendantAccount: id={}", defendantAccountId);

        UpdateDefendantAccountResponse response = defendantAccountService.updateDefendantAccount(
            defendantAccountId, businessUnitId, request, authHeaderValue, ifMatch
        );

        return ResponseEntity.ok()
            .eTag(VersionUtils.createETag(response))
            .body(response.getPayload());
    }
}

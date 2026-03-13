package uk.gov.hmcts.opal.controllers;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;
import uk.gov.hmcts.opal.dto.DefendantAccountResponse;
import uk.gov.hmcts.opal.dto.UpdateDefendantAccountRequest;
import uk.gov.hmcts.opal.generated.http.api.DefendantAccountApi;
import uk.gov.hmcts.opal.generated.model.GetEnforcementStatusResponse;
import uk.gov.hmcts.opal.service.DefendantAccountService;

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
    public ResponseEntity<DefendantAccountResponse> updateDefendantAccount(
        Long id,
        UpdateDefendantAccountRequest body,
        NativeWebRequest nativeWebRequest
    ) {
        // read headers from the NativeWebRequest (OpenAPI generator sometimes gives NativeWebRequest)
        String authHeader = nativeWebRequest.getHeader("Authorization");
        String businessUnitId = nativeWebRequest.getHeader("Business-Unit-Id");
        String ifMatch = nativeWebRequest.getHeader("If-Match");

        log.debug(":PATCH:updateDefendantAccount (OpenAPI): id={}", id);

        // delegate to existing service layer (same call used in manual controller)
        // NOTE: this assumes the generated model 'UpdateDefendantAccountRequest' is compatible with your service method
        return buildResponse(defendantAccountService.updateDefendantAccount(
            id,
            businessUnitId,
            body,      // request object
            ifMatch,
            authHeader
        ));
    }
}

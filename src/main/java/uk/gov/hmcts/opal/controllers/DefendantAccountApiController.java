package uk.gov.hmcts.opal.controllers;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.generated.http.api.DefendantAccountApi;
import uk.gov.hmcts.opal.generated.model.GetDefendantAccountEnforcementStatusResponse;
import uk.gov.hmcts.opal.service.DefendantAccountService;

@RestController
@Slf4j(topic = "opal.DefendantAccountApiController")
@RequiredArgsConstructor
public class DefendantAccountApiController implements DefendantAccountApi {

    private final DefendantAccountService defendantAccountService;

    @Override
    public ResponseEntity<GetDefendantAccountEnforcementStatusResponse> getDefendantAccountEnforcementStatus(
        Long id, String authHeaderValue) {
        log.debug(":GET:getDefendantAccountEnforcementStatus: for defendant id: {}", id);

        return buildResponse(defendantAccountService.getEnforcementStatus(id, authHeaderValue));
    }
}

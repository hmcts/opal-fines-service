package uk.gov.hmcts.opal.controllers;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.generated.model.GetEnforcementStatusResponse;
import uk.gov.hmcts.opal.service.DefendantAccountService;

@RestController
@Slf4j(topic = "opal.DefendantAccountApiController")
@RequiredArgsConstructor
public class DefendantAccountApiController {

    private final DefendantAccountService defendantAccountService;

    @GetMapping(
        value = "/defendant-accounts/{id}/enforcement-status",
        produces = {MediaType.APPLICATION_JSON_VALUE, "application/json+problem"}
    )
    public ResponseEntity<GetEnforcementStatusResponse> getEnforcementStatus(
        @PathVariable("id") Long id,
        @RequestHeader(value = "Authorization", required = false) String authHeaderValue) {
        log.debug(":GET:getDefendantAccountEnforcementStatus: for defendant id: {}", id);

        return buildResponse(defendantAccountService.getEnforcementStatus(id, authHeaderValue));
    }
}

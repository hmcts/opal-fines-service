package uk.gov.hmcts.opal.controllers;

import static uk.gov.hmcts.opal.util.VersionUtils.createETag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureDisabledException;
import uk.gov.hmcts.opal.common.launchdarkly.service.FeatureToggleService;
import uk.gov.hmcts.opal.dto.CentralFundResponse;
import uk.gov.hmcts.opal.generated.model.GetCentralFundByBusinessUnit200Response;
import uk.gov.hmcts.opal.service.CentralFundService;

@RestController
@Slf4j(topic = "opal.CentralFundController")
@RequiredArgsConstructor
public class CentralFundController {

    private static final String RELEASE_1B_FEATURE = "release-1b";

    private final CentralFundService centralFundService;
    private final FeatureToggleService featureToggleService;

    @GetMapping(value = "/central-funds/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<GetCentralFundByBusinessUnit200Response> getCentralFundByBusinessUnit(
        @PathVariable("id") Integer businessUnitId,
        @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeaderValue) {

        log.debug(":GET:getCentralFundByBusinessUnit: businessUnitId={}", businessUnitId);
        checkRelease1bEnabled();

        CentralFundResponse response = centralFundService.getCentralFundByBusinessUnit(
            businessUnitId,
            authHeaderValue
        );

        return ResponseEntity.ok()
            .eTag(createETag(response))
            .body(response.getPayload());
    }

    private void checkRelease1bEnabled() {
        if (!featureToggleService.isFeatureEnabled(RELEASE_1B_FEATURE)) {
            String message = "Feature release-1b is not enabled for method getCentralFundByBusinessUnit";
            log.debug(message);
            throw new FeatureDisabledException(message);
        }
    }
}

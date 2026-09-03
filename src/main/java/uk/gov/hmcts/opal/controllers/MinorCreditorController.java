package uk.gov.hmcts.opal.controllers;

import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1B;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1B_ENABLED_PROPERTY;
import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureToggle;
import uk.gov.hmcts.opal.dto.GetMinorCreditorAccountHeaderSummaryResponse;
import uk.gov.hmcts.opal.service.MinorCreditorService;

@RestController
@RequestMapping("/minor-creditor-accounts")
@Slf4j(topic = "opal.MinorCreditorController")
@Tag(name = "Minor Creditor Controller")
public class MinorCreditorController {

    private final MinorCreditorService minorCreditorService;


    public MinorCreditorController(MinorCreditorService minorCreditorService) {
        this.minorCreditorService = minorCreditorService;
    }

    @GetMapping(value = "/{minorCreditorId}/header-summary", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Gets Minor Creditor account header summary for the given minorCreditorId")
    @FeatureToggle(feature = RELEASE_1B, defaultValueProperty = RELEASE_1B_ENABLED_PROPERTY)
    public ResponseEntity<GetMinorCreditorAccountHeaderSummaryResponse> getMinorCreditorAccountHeaderSummary(
        @PathVariable Long minorCreditorId) {

        log.debug(":GET:getMinorCreditorAccountHeaderSummary: minorCreditorId: {}", minorCreditorId);

        GetMinorCreditorAccountHeaderSummaryResponse response =
            minorCreditorService.getMinorCreditorAccountHeaderSummary(minorCreditorId);

        return buildResponse(response);
    }
}

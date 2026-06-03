package uk.gov.hmcts.opal.controllers;

import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1B;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1B_ENABLED_PROPERTY;
import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureToggle;
import uk.gov.hmcts.opal.dto.GetMajorCreditorAccountAtAGlanceResponse;
import uk.gov.hmcts.opal.service.MajorCreditorAccountService;

@RestController
@RequestMapping("/major-creditor-accounts")
@Slf4j(topic = "opal.MajorCreditorAccountController")
@RequiredArgsConstructor
@Tag(name = "Major Creditor Account Controller")
public class MajorCreditorAccountController {

    private final MajorCreditorAccountService majorCreditorAccountService;

    @GetMapping(value = "/{creditorAccountId}/at-a-glance")
    @Operation(summary = "Get Major Creditor Account At A Glance")
    @FeatureToggle(feature = RELEASE_1B, defaultValueProperty = RELEASE_1B_ENABLED_PROPERTY)
    public ResponseEntity<GetMajorCreditorAccountAtAGlanceResponse> getAtAGlance(
        @PathVariable Long creditorAccountId,
        @RequestHeader(value = "Authorization", required = false) String authHeaderValue
    ) {
        log.debug(":GET:getAtAGlance: creditorAccountId={}", creditorAccountId);

        return buildResponse(majorCreditorAccountService.getAtAGlance(creditorAccountId, authHeaderValue));
    }
}

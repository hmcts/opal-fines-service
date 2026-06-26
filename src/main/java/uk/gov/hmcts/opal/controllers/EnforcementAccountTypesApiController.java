package uk.gov.hmcts.opal.controllers;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureToggle;
import uk.gov.hmcts.opal.generated.http.api.EnforcementAccountTypesApi;
import uk.gov.hmcts.opal.generated.model.PatchEnforcementAccountType200Response;
import uk.gov.hmcts.opal.generated.model.PatchEnforcementAccountTypeRequestInner;
import uk.gov.hmcts.opal.service.EnforcementAccountTypesService;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1C_AUTO_ENFORCEMENT_CONFIG;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1C_AUTO_ENFORCEMENT_CONFIG_ENABLED_PROPERTY;
import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;

@RestController
@Slf4j(topic = "opal.EnforcementAccountTypesController")
@RequiredArgsConstructor
@Tag(name = "Enforcement Account Types Controller")
public class EnforcementAccountTypesApiController implements EnforcementAccountTypesApi {

    private final EnforcementAccountTypesService enforcementAccountTypesService;

    @FeatureToggle(feature = RELEASE_1C_AUTO_ENFORCEMENT_CONFIG,
        defaultValueProperty = RELEASE_1C_AUTO_ENFORCEMENT_CONFIG_ENABLED_PROPERTY)
    @Operation(summary = "Update Enforcement Account Types")
    @Override
    public ResponseEntity<PatchEnforcementAccountType200Response> patchEnforcementAccountType(
        @RequestBody List<PatchEnforcementAccountTypeRequestInner> request) {
        log.debug(":PATCH:patchEnforcementAccountType");

        PatchEnforcementAccountType200Response response =
            enforcementAccountTypesService.updateEnforcementAccountType(request);
        return buildResponse(response, HttpStatus.OK);
    }
}

package uk.gov.hmcts.opal.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureToggle;
import uk.gov.hmcts.opal.generated.http.api.EnforcementAccountTypesApi;
import uk.gov.hmcts.opal.generated.model.EnforcementAccountTypeCommon;
import uk.gov.hmcts.opal.generated.model.GetEnforcementAccountTypes200Response;
import uk.gov.hmcts.opal.generated.model.PatchEnforcementAccountType200Response;
import uk.gov.hmcts.opal.generated.model.PatchEnforcementAccountTypeRequestInner;
import uk.gov.hmcts.opal.service.opal.EnforcementAccountTypeService;

import java.util.List;

import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1C_AUTO_ENFORCEMENT_CONFIG;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1C_AUTO_ENFORCEMENT_CONFIG_ENABLED_PROPERTY;
import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;

@RestController
@Slf4j(topic = "opal.EnforcementAccountTypesApiController")
@RequiredArgsConstructor
@Tag(name = "Enforcement Account Types Controller")
public class EnforcementAccountTypesApiController implements EnforcementAccountTypesApi {
    private final EnforcementAccountTypeService service;

    @FeatureToggle(
        feature = RELEASE_1C_AUTO_ENFORCEMENT_CONFIG,
        defaultValueProperty = RELEASE_1C_AUTO_ENFORCEMENT_CONFIG_ENABLED_PROPERTY
    )
    @Override
    public ResponseEntity<GetEnforcementAccountTypes200Response> getEnforcementAccountTypes() {
        List<EnforcementAccountTypeCommon> enfAccountTypes = service.getAllEnforcementAccountTypes();
        GetEnforcementAccountTypes200Response response = GetEnforcementAccountTypes200Response.builder()
            .enforcementAccountTypes(enfAccountTypes)
            .build();
        return buildResponse(response);
    }

    @FeatureToggle(feature = RELEASE_1C_AUTO_ENFORCEMENT_CONFIG,
        defaultValueProperty = RELEASE_1C_AUTO_ENFORCEMENT_CONFIG_ENABLED_PROPERTY)
    @Operation(summary = "Update Enforcement Account Types")
    @Override
    public ResponseEntity<PatchEnforcementAccountType200Response> patchEnforcementAccountType(
        @RequestBody List<PatchEnforcementAccountTypeRequestInner> request) {
        log.debug(":PATCH:patchEnforcementAccountType");

        List<EnforcementAccountTypeCommon> updatedEntities =
            service.updateEnforcementAccountType(request);

        PatchEnforcementAccountType200Response response = PatchEnforcementAccountType200Response.builder()
            .enforcementAccountTypes(updatedEntities)
            .build();
        return buildResponse(response, HttpStatus.OK);
    }
}

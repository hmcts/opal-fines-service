package uk.gov.hmcts.opal.controllers;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.generated.http.api.EnforcementAccountTypesApi;
import uk.gov.hmcts.opal.generated.model.EnforcementAccountTypeCommon;
import uk.gov.hmcts.opal.generated.model.GetEnforcementAccountTypes200Response;
import uk.gov.hmcts.opal.service.opal.EnforcementAccountTypeService;

import java.util.List;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;

@RestController
@Slf4j(topic = "opal.EnforcementAccountTypesApiController")
@RequiredArgsConstructor
public class EnforcementAccountTypesApiController implements EnforcementAccountTypesApi {
    private final EnforcementAccountTypeService service;

    // TODO - user must have the auto-enforcement permission in at least one BU
    // TODO add feature flag release-1d-auto-enforcement-config
    @Override
    public ResponseEntity<GetEnforcementAccountTypes200Response> getEnforcementAccountTypes() {
        List<EnforcementAccountTypeCommon> enfAccountTypes = service.getAllEnforcementAccountTypes();
        GetEnforcementAccountTypes200Response response = GetEnforcementAccountTypes200Response.builder()
            .enforcementAccountTypes(enfAccountTypes)
            .build();
        return buildResponse(response);
    }
}

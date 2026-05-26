package uk.gov.hmcts.opal.controllers;

import static uk.gov.hmcts.opal.util.HttpUtil.buildCreatedResponse;
import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;
import static uk.gov.hmcts.opal.util.VersionUtils.extractOptionalBigInteger;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureToggle;
import uk.gov.hmcts.opal.dto.MinorCreditorAccountResponse;
import uk.gov.hmcts.opal.generated.http.api.MinorCreditorApi;
import uk.gov.hmcts.opal.generated.model.MinorCreditorAccountResponseMinorCreditor;
import uk.gov.hmcts.opal.generated.model.PatchMinorCreditorAccountRequest;
import uk.gov.hmcts.opal.service.MinorCreditorService;
import uk.gov.hmcts.opal.util.FeatureFlags;

@RestController
@Slf4j(topic = "opal.MinorCreditorApiController")
@RequiredArgsConstructor
public class MinorCreditorApiController implements MinorCreditorApi {

    private final MinorCreditorService minorCreditorService;

    @Override
    public ResponseEntity<MinorCreditorAccountResponseMinorCreditor> getMinorCreditorAccount(Long id) {
        log.debug(":GET:getMinorCreditorAccount: id={}", id);

        MinorCreditorAccountResponse result = minorCreditorService.getMinorCreditorAccount(id);

        return buildResponse(result);
    }

    @Override
    @FeatureToggle(
        feature = FeatureFlags.RELEASE_1B,
        defaultValueProperty = FeatureFlags.RELEASE_1B_ENABLED_PROPERTY
    )
    public ResponseEntity<MinorCreditorAccountResponseMinorCreditor> patchMinorCreditorAccount(
        Long id,
        String businessUnitId,
        String ifMatch,
        String authHeaderValue,
        PatchMinorCreditorAccountRequest patchMinorCreditorAccountRequest) {

        log.debug(":PATCH:patchMinorCreditorAccount: id={}", id);

        MinorCreditorAccountResponse result =
            minorCreditorService.updateMinorCreditorAccount(id, patchMinorCreditorAccountRequest,
                extractOptionalBigInteger(ifMatch).orElse(null),
                authHeaderValue, businessUnitId);

        return buildCreatedResponse(result);
    }
}

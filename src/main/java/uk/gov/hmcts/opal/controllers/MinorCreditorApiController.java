package uk.gov.hmcts.opal.controllers;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;
import static uk.gov.hmcts.opal.util.VersionUtils.extractOptionalBigInteger;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.NativeWebRequest;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureDisabledException;
import uk.gov.hmcts.opal.common.launchdarkly.config.LaunchDarklyProperties;
import uk.gov.hmcts.opal.common.launchdarkly.service.FeatureToggleService;
import uk.gov.hmcts.opal.dto.MinorCreditorAccountResponse;
import uk.gov.hmcts.opal.generated.http.api.MinorCreditorApi;
import uk.gov.hmcts.opal.generated.model.MinorCreditorAccountResponseMinorCreditor;
import uk.gov.hmcts.opal.generated.model.PatchMinorCreditorAccountRequest;
import uk.gov.hmcts.opal.service.MinorCreditorService;

@RestController
@Slf4j(topic = "opal.MinorCreditorApiController")
@RequiredArgsConstructor
public class MinorCreditorApiController implements MinorCreditorApi {

    private static final String RELEASE_1B = "release-1b";

    private final MinorCreditorService minorCreditorService;
    private final FeatureToggleService featureToggleService;
    private final LaunchDarklyProperties launchDarklyProperties;
    private final NativeWebRequest request;

    @Value("${release-1b.enabled:false}")
    private boolean release1bEnabled;

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return Optional.ofNullable(request);
    }

    @Override
    public ResponseEntity<MinorCreditorAccountResponseMinorCreditor> patchMinorCreditorAccount(
        Long id,
        String businessUnitId,
        String ifMatch,
        String authHeaderValue,
        PatchMinorCreditorAccountRequest patchMinorCreditorAccountRequest) {

        log.debug(":PATCH:patchMinorCreditorAccount: id={}", id);
        checkRelease1bFlag();

        MinorCreditorAccountResponse result =
            minorCreditorService.updateMinorCreditorAccount(id, patchMinorCreditorAccountRequest,
                extractOptionalBigInteger(ifMatch).orElse(null),
                authHeaderValue, businessUnitId);

        return buildResponse(result);
    }

    private void checkRelease1bFlag() {
        boolean enabled = launchDarklyProperties.isEnabled()
            ? featureToggleService.isFeatureEnabled(RELEASE_1B)
            : release1bEnabled;

        log.debug("Feature flag '{}' evaluated to {}", RELEASE_1B, enabled);

        if (!enabled) {
            log.debug("Attempt to execute disabled feature '{}'", RELEASE_1B);
            throw new FeatureDisabledException(
                String.format("Feature %s is not enabled for method %s", RELEASE_1B, "patchMinorCreditorAccount")
            );
        }
    }
}

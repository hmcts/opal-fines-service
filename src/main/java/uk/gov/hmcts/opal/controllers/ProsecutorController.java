package uk.gov.hmcts.opal.controllers;

import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1A;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1A_ENABLED_PROPERTY;
import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureToggle;
import uk.gov.hmcts.opal.generated.http.api.ProsecutorsApi;
import uk.gov.hmcts.opal.generated.model.ProsecutorsObject;
import uk.gov.hmcts.opal.generated.model.ProsecutorsReferenceDataResponse;
import uk.gov.hmcts.opal.service.proxy.ProsecutorServiceProxy;

@RestController
@Slf4j(topic = "opal.ProsecutorController")
@RequiredArgsConstructor
public class ProsecutorController implements ProsecutorsApi {

    private final ProsecutorServiceProxy prosecutorServiceProxy;

    @Override
    @FeatureToggle(feature = RELEASE_1A, defaultValueProperty = RELEASE_1A_ENABLED_PROPERTY)
    public ResponseEntity<ProsecutorsObject> getProsecutorByKey(Long prosecutorKey) {
        log.debug(":GET:getProsecutorByKey: prosecutorKey: {}", prosecutorKey);
        return buildResponse(prosecutorServiceProxy.getProsecutorByKey(prosecutorKey));
    }

    @Override
    @FeatureToggle(feature = RELEASE_1A, defaultValueProperty = RELEASE_1A_ENABLED_PROPERTY)
    public ResponseEntity<ProsecutorsReferenceDataResponse> getProsecutorsRefData(@Nullable String q) {
        log.debug(":GET:getProsecutorsRefData: q: {}", q);
        return buildResponse(prosecutorServiceProxy.getProsecutorsRefData(q));
    }
}

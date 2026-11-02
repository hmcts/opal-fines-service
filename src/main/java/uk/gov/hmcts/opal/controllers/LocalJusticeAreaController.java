package uk.gov.hmcts.opal.controllers;

import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1A;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1A_ENABLED_PROPERTY;
import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureToggle;
import uk.gov.hmcts.opal.generated.http.api.LocalJusticeAreasApi;
import uk.gov.hmcts.opal.generated.model.LocalJusticeArea;
import uk.gov.hmcts.opal.generated.model.LocalJusticeAreaType;
import uk.gov.hmcts.opal.generated.model.LocalJusticeAreasResponse;
import uk.gov.hmcts.opal.service.proxy.LocalJusticeAreaServiceProxy;

@RestController
@Slf4j(topic = "opal.LocalJusticeAreaController")
@RequiredArgsConstructor
public class LocalJusticeAreaController implements LocalJusticeAreasApi {

    private final LocalJusticeAreaServiceProxy localJusticeAreaServiceProxy;

    @Override
    @FeatureToggle(feature = RELEASE_1A, defaultValueProperty = RELEASE_1A_ENABLED_PROPERTY)
    public ResponseEntity<LocalJusticeArea> getLocalJusticeAreaById(Short localJusticeAreaId) {
        log.debug(":GET:getLocalJusticeAreaById: localJusticeAreaId: {}", localJusticeAreaId);
        return buildResponse(localJusticeAreaServiceProxy.getLocalJusticeAreaById(localJusticeAreaId));
    }

    @Override
    @FeatureToggle(feature = RELEASE_1A, defaultValueProperty = RELEASE_1A_ENABLED_PROPERTY)
    public ResponseEntity<LocalJusticeAreasResponse> getLocalJusticeAreaRefData(
        @Nullable String q, @Nullable List<LocalJusticeAreaType> ljaType) {

        log.debug(":GET:getLocalJusticeAreaRefData: q: {} ljaType: {}", q, ljaType);
        return buildResponse(localJusticeAreaServiceProxy.getLocalJusticeAreaRefData(q, ljaType));
    }
}

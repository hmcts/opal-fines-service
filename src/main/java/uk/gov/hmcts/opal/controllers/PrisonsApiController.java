package uk.gov.hmcts.opal.controllers;

import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1C;
import static uk.gov.hmcts.opal.util.FeatureFlags.RELEASE_1C_ENABLED_PROPERTY;
import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureToggle;
import uk.gov.hmcts.opal.generated.http.api.PrisonsApi;
import uk.gov.hmcts.opal.generated.model.PrisonsPrison;
import uk.gov.hmcts.opal.generated.model.PrisonsReferenceDataResponse;
import uk.gov.hmcts.opal.service.opal.PrisonService;

@RestController
@RequiredArgsConstructor
@Slf4j(topic = "opal.PrisonsApiController")
public class PrisonsApiController implements PrisonsApi {

    private final PrisonService prisonService;

    @Override
    @FeatureToggle(feature = RELEASE_1C, defaultValueProperty = RELEASE_1C_ENABLED_PROPERTY)
    public ResponseEntity<PrisonsPrison> getPrisonById(Long id) {
        log.debug(":GET:getPrisonById: prisonId: {}", id);
        return buildResponse(prisonService.getPrisonById(id));
    }

    @Override
    @FeatureToggle(feature = RELEASE_1C, defaultValueProperty = RELEASE_1C_ENABLED_PROPERTY)
    public ResponseEntity<PrisonsReferenceDataResponse> getPrisonsRefData() {
        log.debug(":GET:getPrisonsRefData");
        return buildResponse(prisonService.getPrisonsRefData());
    }
}

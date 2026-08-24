package uk.gov.hmcts.opal.controllers;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureToggle;
import uk.gov.hmcts.opal.exception.MissingMappingTypeException;
import uk.gov.hmcts.opal.generated.http.api.MappingsApi;
import uk.gov.hmcts.opal.generated.model.MappingItemMappings;
import uk.gov.hmcts.opal.service.opal.MappingsService;
import uk.gov.hmcts.opal.util.FeatureFlags;

@RestController
@Slf4j(topic = "opal.MappingsController")
@RequiredArgsConstructor
public class MappingsController implements MappingsApi {

    private final MappingsService mappingsService;

    @Override
    @FeatureToggle(
        feature = FeatureFlags.RELEASE_1B,
        defaultValueProperty = FeatureFlags.RELEASE_1B_ENABLED_PROPERTY
    )
    public ResponseEntity<List<MappingItemMappings>> getMappings(String type) {
        log.debug(":GET:getMappings: type: {}", type);
        return buildResponse(mappingsService.getMappings(type));
    }

    @GetMapping("/mappings")
    @FeatureToggle(
        feature = FeatureFlags.RELEASE_1B,
        defaultValueProperty = FeatureFlags.RELEASE_1B_ENABLED_PROPERTY
    )
    public ResponseEntity<Void> getMappingsWithoutType() {
        throw new MissingMappingTypeException(mappingsService.getSupportedMappingTypes());
    }
}

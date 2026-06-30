package uk.gov.hmcts.opal.controllers;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.common.launchdarkly.FeatureToggle;
import uk.gov.hmcts.opal.dto.reference.MappingItem;
import uk.gov.hmcts.opal.service.opal.MappingsService;
import uk.gov.hmcts.opal.util.FeatureFlags;

@RestController
@RequestMapping("/mappings")
@Slf4j(topic = "opal.MappingsController")
@Tag(name = "Mappings Controller")
public class MappingsController {

    private final MappingsService mappingsService;

    public MappingsController(MappingsService mappingsService) {
        this.mappingsService = mappingsService;
    }

    @GetMapping("/{type}")
    @Operation(summary = "Returns the supported mappings for the requested type.")
    @FeatureToggle(
        feature = FeatureFlags.RELEASE_1B,
        defaultValueProperty = FeatureFlags.RELEASE_1B_ENABLED_PROPERTY
    )
    public ResponseEntity<List<MappingItem>> getMappings(@PathVariable String type) {
        log.debug(":GET:getMappings: type: {}", type);
        return buildResponse(mappingsService.getMappings(type));
    }
}

package uk.gov.hmcts.opal.controllers;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.exception.MissingMappingTypeException;
import uk.gov.hmcts.opal.generated.http.api.MappingsApi;
import uk.gov.hmcts.opal.generated.model.MappingItemMappings;
import uk.gov.hmcts.opal.service.opal.MappingsService;

@RestController
@Slf4j(topic = "opal.MappingsController")
@RequiredArgsConstructor
public class MappingsController implements MappingsApi {

    private final MappingsService mappingsService;

    @Override
    public ResponseEntity<List<MappingItemMappings>> getMappings(String type) {
        log.debug(":GET:getMappings: type: {}", type);
        return buildResponse(mappingsService.getMappings(type));
    }

    @GetMapping("/mappings")
    public ResponseEntity<Void> getMappingsWithoutType() {
        throw new MissingMappingTypeException(mappingsService.getMappingTypesOrThrowIfNoneEnabled());
    }
}

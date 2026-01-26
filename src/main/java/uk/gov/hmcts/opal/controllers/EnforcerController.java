package uk.gov.hmcts.opal.controllers;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.dto.reference.EnforcerReferenceDataResults;
import uk.gov.hmcts.opal.dto.search.EnforcerSearchDto;
import uk.gov.hmcts.opal.entity.EnforcerEntity;
import uk.gov.hmcts.opal.entity.projection.EnforcerReferenceData;
import uk.gov.hmcts.opal.service.opal.EnforcerService;


@RestController
@RequestMapping("/enforcers")
@Slf4j(topic = "opal.EnforcerController")
@Tag(name = "Enforcer Controller")
public class EnforcerController {

    private final EnforcerService enforcerService;

    public EnforcerController(EnforcerService enforcerService) {
        this.enforcerService = enforcerService;
    }

    @GetMapping(value = "/{enforcerId}")
    @Operation(summary = "Returns the Enforcer for the given enforcerId.")
    public ResponseEntity<EnforcerEntity> getEnforcerById(@PathVariable Long enforcerId) {

        log.debug(":GET:getEnforcerById: enforcerId: {}", enforcerId);

        EnforcerEntity response = enforcerService.getEnforcerById(enforcerId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches Enforcers based upon criteria in request body")
    public ResponseEntity<List<EnforcerEntity>> postEnforcersSearch(@RequestBody EnforcerSearchDto criteria) {
        log.debug(":POST:postEnforcersSearch: query: \n{}", criteria);

        List<EnforcerEntity> response = enforcerService.searchEnforcers(criteria);

        return buildResponse(response);
    }

    @GetMapping
    @Operation(summary = "Returns Enforcers as reference data with an optional filter applied")
    public ResponseEntity<EnforcerReferenceDataResults> getEnforcersRefData(
        @RequestParam("q") Optional<String> filter) {
        log.debug(":GET:getEnforcersRefData: query: \n{}", filter);

        List<EnforcerReferenceData> refData = enforcerService.getReferenceData(filter);

        log.debug(":GET:getEnforcersRefData: enforcer reference data count: {}", refData.size());
        return ResponseEntity.ok(EnforcerReferenceDataResults.builder().refData(refData).build());
    }
}

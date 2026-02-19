package uk.gov.hmcts.opal.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import uk.gov.hmcts.opal.dto.reference.LjaReferenceDataResults;
import uk.gov.hmcts.opal.dto.search.LocalJusticeAreaSearchDto;
import uk.gov.hmcts.opal.entity.LocalJusticeAreaEntity;
import uk.gov.hmcts.opal.dto.reference.LjaReferenceData;
import uk.gov.hmcts.opal.service.opal.LocalJusticeAreaService;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/local-justice-areas")
@Slf4j(topic = "opal.LocalJusticeAreaController")
@Tag(name = "LocalJusticeArea Controller")
public class LocalJusticeAreaController {

    private final LocalJusticeAreaService opalLocalJusticeAreaService;

    public LocalJusticeAreaController(LocalJusticeAreaService opalLocalJusticeAreaService) {
        this.opalLocalJusticeAreaService = opalLocalJusticeAreaService;
    }

    @GetMapping(value = "/{localJusticeAreaId}")
    @Operation(summary = "Returns the LocalJusticeArea for the given localJusticeAreaId.")
    public ResponseEntity<LocalJusticeAreaEntity> getLocalJusticeAreaById(@PathVariable Short localJusticeAreaId) {

        log.debug(":GET:getLocalJusticeAreaById: localJusticeAreaId: {}", localJusticeAreaId);

        LocalJusticeAreaEntity response = opalLocalJusticeAreaService.getLocalJusticeAreaById(localJusticeAreaId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches LocalJusticeAreas based upon criteria in request body")
    public ResponseEntity<List<LocalJusticeAreaEntity>> postLocalJusticeAreasSearch(
        @RequestBody LocalJusticeAreaSearchDto criteria) {
        log.debug(":POST:postLocalJusticeAreasSearch: query: \n{}", criteria);

        List<LocalJusticeAreaEntity> response = opalLocalJusticeAreaService.searchLocalJusticeAreas(criteria);

        return buildResponse(response);
    }

    @GetMapping
    @Operation(summary = "Returns Local Justice Area as reference data with an optional filter applied")
    public ResponseEntity<LjaReferenceDataResults> getLocalJusticeAreaRefData(
        @RequestParam("q") Optional<String> filter, @RequestParam("lja_type") Optional<List<String>> ljaType) {
        log.debug(":GET:getLocalJusticeAreaRefData: filter: {}  ljaType: {}", filter, ljaType);

        List<LjaReferenceData> refData = opalLocalJusticeAreaService.getReferenceData(filter, ljaType);

        log.debug(":GET:getLocalJusticeAreaRefData: local justice area reference data count: {}", refData.size());
        return ResponseEntity.ok(LjaReferenceDataResults.builder().refData(refData).build());
    }
}

package uk.gov.hmcts.opal.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.opal.dto.reference.OffenceReferenceDataResults;
import uk.gov.hmcts.opal.dto.reference.OffenceSearchDataResults;
import uk.gov.hmcts.opal.dto.search.OffenceSearchDto;
import uk.gov.hmcts.opal.entity.OffenceEntity;
import uk.gov.hmcts.opal.entity.projection.OffenceReferenceData;
import uk.gov.hmcts.opal.entity.projection.OffenceSearchData;
import uk.gov.hmcts.opal.service.OffenceServiceInterface;
import uk.gov.hmcts.opal.service.opal.OffenceService;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/offences")
@Slf4j(topic = "opal.OffenceController")
@Tag(name = "Offence Controller")
public class OffenceController {

    private final OffenceServiceInterface offenceService;
    private final OffenceService opalOffenceService;

    public OffenceController(@Qualifier("offenceServiceProxy") OffenceServiceInterface offenceService,
                             OffenceService opalOffenceService) {
        this.offenceService = offenceService;
        this.opalOffenceService = opalOffenceService;
    }

    @GetMapping(value = "/{offenceId}")
    @Operation(summary = "Returns the Offence for the given offenceId.")
    public ResponseEntity<OffenceEntity> getOffenceById(@PathVariable Long offenceId) {

        log.debug(":GET:getOffenceById: offenceId: {}", offenceId);

        OffenceEntity response = opalOffenceService.getOffence(offenceId);
        if (response == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches Offences based upon criteria in request body")
    public ResponseEntity<OffenceSearchDataResults> postOffencesSearch(@RequestBody OffenceSearchDto criteria) {
        log.debug(":POST:postOffencesSearch: query: \n{}", criteria.toPrettyJson());

        List<OffenceSearchData> searchData = opalOffenceService.searchOffences(criteria);

        log.debug(":GET:postOffencesSearch: offences reference data count: {}", searchData.size());
        return ResponseEntity.ok(OffenceSearchDataResults.builder().searchData(searchData).build());
    }

    @GetMapping
    @Operation(summary = "Returns 'global' Offences as reference data with an optional filter applied. "
        + "If the business unit is provided, then that is used to return only 'local' offences "
        + "for that business unit, or ALL local offences if the business unit provided is zero.")
    public ResponseEntity<OffenceReferenceDataResults> getOffenceRefData(
        @RequestParam("q") Optional<String> filter,
        @RequestParam("business_unit_id") Optional<Short> businessUnit,
        @RequestParam(value = "cjs_code") Optional<List<String>> optionalCjsCode) {

        log.debug(":GET:getOffenceRefData: business unit: {}, filter string: {}", businessUnit, filter);

        List<OffenceReferenceData> refData = opalOffenceService.getReferenceData(filter, businessUnit, optionalCjsCode);

        log.debug(":GET:getOffenceRefData: offences reference data count: {}", refData.size());
        return ResponseEntity.ok(OffenceReferenceDataResults.builder().refData(refData).build());
    }
}
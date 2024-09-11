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
import uk.gov.hmcts.opal.dto.reference.MajorCreditorReferenceDataResults;
import uk.gov.hmcts.opal.dto.search.MajorCreditorSearchDto;
import uk.gov.hmcts.opal.entity.MajorCreditorEntity;
import uk.gov.hmcts.opal.entity.projection.MajorCreditorReferenceData;
import uk.gov.hmcts.opal.service.opal.MajorCreditorService;

import java.util.List;
import java.util.Optional;

import static uk.gov.hmcts.opal.util.HttpUtil.buildResponse;


@RestController
@RequestMapping("/major-creditors")
@Slf4j(topic = "MajorCreditorController")
@Tag(name = "Major Creditor Controller")
public class MajorCreditorController {

    private final MajorCreditorService opalMajorCreditorService;

    public MajorCreditorController(MajorCreditorService opalMajorCreditorService) {
        this.opalMajorCreditorService = opalMajorCreditorService;
    }

    @GetMapping(value = "/{majorCreditorId}")
    @Operation(summary = "Returns the MajorCreditor for the given majorCreditorId.")
    public ResponseEntity<MajorCreditorEntity> getMajorCreditorById(@PathVariable Long majorCreditorId) {

        log.info(":GET:getMajorCreditorById: majorCreditorId: {}", majorCreditorId);

        MajorCreditorEntity response = opalMajorCreditorService.getMajorCreditor(majorCreditorId);

        return buildResponse(response);
    }

    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Searches MajorCreditors based upon criteria in request body")
    public ResponseEntity<List<MajorCreditorEntity>> postMajorCreditorsSearch(
        @RequestBody MajorCreditorSearchDto criteria) {
        log.info(":POST:postMajorCreditorsSearch: query: \n{}", criteria);

        List<MajorCreditorEntity> response = opalMajorCreditorService.searchMajorCreditors(criteria);

        return buildResponse(response);
    }

    @GetMapping
    @Operation(summary = "Returns MajorCreditors as reference data with an optional filter applied")
    public ResponseEntity<MajorCreditorReferenceDataResults> getMajorCreditorRefData(
        @RequestParam("q") Optional<String> filter, @RequestParam Optional<Short> businessUnit) {
        log.info(":GET:getMajorCreditorRefData: business unit: {}, filter string: {}", businessUnit, filter);

        List<MajorCreditorReferenceData> refData = opalMajorCreditorService.getReferenceData(filter, businessUnit);

        log.info(":GET:getMajorCreditorRefData: major creditor reference data count: {}", refData.size());
        return ResponseEntity.ok(MajorCreditorReferenceDataResults.builder().refData(refData).build());
    }
}
